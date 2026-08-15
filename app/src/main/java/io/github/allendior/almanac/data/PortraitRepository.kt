package io.github.allendior.almanac.data

import io.github.allendior.almanac.data.db.PortraitDao
import io.github.allendior.almanac.data.db.PortraitEntryEntity
import io.github.allendior.almanac.domain.CameraFacing
import io.github.allendior.almanac.domain.DayId
import io.github.allendior.almanac.domain.Mood
import io.github.allendior.almanac.domain.PortraitEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File

/**
 * The single boundary between the archive and everything above it. Screens never
 * touch Room or the filesystem directly, which is what keeps "the phone is the source
 * of truth" a checkable statement rather than an intention.
 */
class PortraitRepository(
    private val dao: PortraitDao,
    private val photos: PhotoStore,
) {

    val entries: Flow<List<PortraitEntry>> = dao.observeAll().map { rows ->
        rows.map { it.toDomain() }
    }

    suspend fun entriesNow(): List<PortraitEntry> = dao.getAll().map { it.toDomain() }

    suspend fun find(dayId: String): PortraitEntry? = dao.findByDay(dayId)?.toDomain()

    fun originalFile(entry: PortraitEntry): File = photos.originalFile(entry.fileName)

    fun thumbnailFile(entry: PortraitEntry): File = photos.thumbnailFile(entry.dayId)

    /**
     * Saves today's portrait, replacing an existing one for the same day.
     *
     * Order matters: the new file is written and committed to the database before the
     * superseded file is removed, so a crash mid-save can leave an orphan file but can
     * never leave a row pointing at nothing.
     */
    suspend fun save(
        dayId: String,
        capturedAtEpochMs: Long,
        utcOffsetMinutes: Int,
        bytes: ByteArray,
        mood: Mood?,
        numberValue: Double?,
        cameraFacing: CameraFacing,
        note: String?,
    ): PortraitEntry {
        val previous = dao.findByDay(dayId)
        val stored = photos.writeOriginal(dayId, bytes)
        val entry = PortraitEntry(
            dayId = dayId,
            capturedAtEpochMs = capturedAtEpochMs,
            utcOffsetMinutes = utcOffsetMinutes,
            fileName = stored.fileName,
            sha256 = stored.sha256,
            mood = mood,
            numberValue = numberValue,
            cameraFacing = cameraFacing,
            note = note?.takeIf { it.isNotBlank() },
        )
        dao.upsert(PortraitEntryEntity.fromDomain(entry))
        if (previous != null && previous.fileName != stored.fileName) {
            photos.delete(previous.fileName, dayId)
            photos.generateThumbnail(photos.originalFile(stored.fileName), dayId)
        }
        return entry
    }

    suspend fun updateNote(dayId: String, note: String?) {
        dao.updateNote(dayId, note?.takeIf { it.isNotBlank() })
    }

    /** Deleting is deliberate and complete: the row and the file both go. */
    suspend fun delete(entry: PortraitEntry) {
        dao.delete(entry.dayId)
        photos.delete(entry.fileName, entry.dayId)
    }

    suspend fun count(): Int = dao.count()

    suspend fun onDiskBytes(): Long = photos.originalsSizeBytes()

    /** A row whose file has vanished is reported, never quietly hidden. */
    suspend fun missingOriginals(): List<PortraitEntry> =
        entriesNow().filter { !photos.originalFile(it.fileName).exists() }

    suspend fun repairThumbnails() {
        photos.regenerateMissingThumbnails(entriesNow().map { it.dayId to it.fileName })
    }

    fun todayId(): String = DayId.today()
}
