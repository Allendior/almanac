package com.almanac.portrait.data.archive

import android.content.Context
import android.net.Uri
import com.almanac.portrait.data.PhotoStore
import com.almanac.portrait.data.db.PortraitDao
import com.almanac.portrait.data.db.PortraitEntryEntity
import com.almanac.portrait.domain.EntryValidation
import com.almanac.portrait.domain.PortraitEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

/**
 * Export writes to a location the owner picked through the system file picker. The app
 * holds no storage permission and cannot see that folder before or after — it is handed
 * exactly one URI, writes to it, and forgets it.
 */
class ArchiveExporter(
    private val context: Context,
    private val photos: PhotoStore,
) {

    data class Result(val entryCount: Int, val fileCount: Int, val bytes: Long, val missing: List<String>)

    fun suggestedFileName(today: LocalDate = LocalDate.now()): String = "almanac-$today.zip"

    suspend fun export(target: Uri, entries: List<PortraitEntry>): Result = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val offset = ZoneId.systemDefault().rules.getOffset(Instant.ofEpochMilli(now)).totalSeconds / 60
        val missing = mutableListOf<String>()
        var fileCount = 0
        var bytes = 0L

        val output = context.contentResolver.openOutputStream(target, "wt")
            ?: throw java.io.IOException("Could not open the chosen file for writing")

        output.use { raw ->
            ZipOutputStream(raw.buffered()).use { zip ->
                val index = ArchiveFormat.writeIndex(entries, now, offset).toByteArray()
                zip.putNextEntry(ZipEntry(ArchiveFormat.INDEX_NAME))
                zip.write(index)
                zip.closeEntry()

                entries.forEach { entry ->
                    val file = photos.originalFile(entry.fileName)
                    if (!file.exists()) {
                        missing += entry.dayId
                        return@forEach
                    }
                    zip.putNextEntry(ZipEntry(ArchiveFormat.ORIGINALS_PREFIX + entry.fileName))
                    file.inputStream().use { it.copyTo(zip) }
                    zip.closeEntry()
                    fileCount++
                    bytes += file.length()
                }
            }
        }
        Result(entryCount = entries.size, fileCount = fileCount, bytes = bytes, missing = missing)
    }
}

/**
 * Import is additive and idempotent. The day id is the stable identity, so importing
 * the same archive twice adds nothing the second time, and an existing day is never
 * overwritten by an incoming one — the archive on this phone always wins.
 */
class ArchiveImporter(
    private val context: Context,
    private val photos: PhotoStore,
    private val dao: PortraitDao,
) {

    suspend fun import(source: Uri): ImportReport = withContext(Dispatchers.IO) {
        val staged = File(context.cacheDir, "import-${System.currentTimeMillis()}.zip")
        try {
            context.contentResolver.openInputStream(source)?.use { input ->
                staged.outputStream().use { input.copyTo(it) }
            } ?: return@withContext ImportReport(failureReason = "Could not open the chosen file")

            readFrom(staged)
        } catch (e: java.util.zip.ZipException) {
            ImportReport(failureReason = "That file is not a readable ZIP archive")
        } catch (e: java.io.IOException) {
            ImportReport(failureReason = e.message ?: "The file could not be read")
        } finally {
            staged.delete()
        }
    }

    private suspend fun readFrom(staged: File): ImportReport {
        ZipFile(staged).use { zip ->
            val indexEntry = zip.getEntry(ArchiveFormat.INDEX_NAME)
                ?: return ImportReport(failureReason = "The archive has no index.json")

            val json = zip.getInputStream(indexEntry).bufferedReader().use { it.readText() }
            val parsed = when (val result = ArchiveFormat.readIndex(json)) {
                is ArchiveFormat.ParseResult.NotAnArchive ->
                    return ImportReport(failureReason = result.reason)
                is ArchiveFormat.ParseResult.Parsed -> result.rows
            }

            var added = 0
            var duplicate = 0
            var unreadable = 0
            var rejected = 0
            val details = mutableListOf<String>()

            parsed.forEach { row ->
                when (row) {
                    is EntryValidation.Result.Rejected -> {
                        rejected++
                        details += "${row.dayId ?: "a row"}: ${row.reason}"
                    }
                    is EntryValidation.Result.Valid -> {
                        val entry = row.entry
                        if (dao.findByDay(entry.dayId) != null) {
                            duplicate++
                            return@forEach
                        }
                        val zipEntry = zip.getEntry(ArchiveFormat.ORIGINALS_PREFIX + entry.fileName)
                        if (zipEntry == null) {
                            unreadable++
                            details += "${entry.dayId}: the photograph is not in the archive"
                            return@forEach
                        }
                        val bytes = runCatching {
                            zip.getInputStream(zipEntry).use { it.readBytes() }
                        }.getOrNull()
                        if (bytes == null || bytes.isEmpty()) {
                            unreadable++
                            details += "${entry.dayId}: the photograph could not be read"
                            return@forEach
                        }
                        val actual = photos.sha256(bytes)
                        if (actual != entry.sha256) {
                            unreadable++
                            details += "${entry.dayId}: the photograph does not match its recorded hash"
                            return@forEach
                        }
                        val stored = runCatching { photos.writeOriginal(entry.dayId, bytes) }.getOrNull()
                        if (stored == null) {
                            unreadable++
                            details += "${entry.dayId}: could not be written to this phone"
                            return@forEach
                        }
                        val inserted = dao.insertIfAbsent(
                            PortraitEntryEntity.fromDomain(entry.copy(fileName = stored.fileName)),
                        )
                        if (inserted == -1L) {
                            photos.delete(stored.fileName, entry.dayId)
                            duplicate++
                        } else {
                            added++
                        }
                    }
                }
            }
            return ImportReport(
                added = added,
                duplicate = duplicate,
                unreadable = unreadable,
                rejected = rejected,
                details = details,
            )
        }
    }
}
