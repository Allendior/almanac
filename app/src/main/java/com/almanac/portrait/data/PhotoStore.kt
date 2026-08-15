package com.almanac.portrait.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.security.MessageDigest

/**
 * Every byte of every portrait lives here, in app-private storage, and nowhere else.
 *
 * Two directories, and the distinction between them matters:
 *   originals/  the file the camera produced, written once, never re-encoded.
 *   thumbnails/ a small derived JPEG, disposable and regenerable, never the record.
 *
 * If a thumbnail is lost or corrupt the archive is intact; if an original is lost,
 * that day is gone. The code treats the two accordingly.
 */
class PhotoStore(context: Context) {

    private val root = context.filesDir
    val originalsDir = File(root, "originals").apply { mkdirs() }
    val thumbnailsDir = File(root, "thumbnails").apply { mkdirs() }

    /** Refuse to start a save that could end with a half-written portrait. */
    fun hasHeadroom(): Boolean = originalsDir.usableSpace > MIN_FREE_BYTES

    fun originalFile(fileName: String) = File(originalsDir, fileName)

    fun thumbnailFile(dayId: String) = File(thumbnailsDir, "$dayId.jpg")

    /**
     * Names are built from the day and a prefix of the content hash only.
     * A note is private prose; it never reaches a filename.
     */
    fun fileNameFor(dayId: String, sha256: String): String = "${dayId}_${sha256.take(12)}.jpg"

    /**
     * Writes the camera's bytes verbatim: no decode, no re-encode, no rotation baked
     * in, no filter. What the sensor produced is what lands on disk.
     */
    suspend fun writeOriginal(dayId: String, bytes: ByteArray): StoredPhoto = withContext(Dispatchers.IO) {
        if (originalsDir.usableSpace <= bytes.size + MIN_FREE_BYTES) {
            throw StorageFullException()
        }
        val sha = sha256(bytes)
        val name = fileNameFor(dayId, sha)
        val target = File(originalsDir, name)
        val temp = File(originalsDir, "$name.part")
        try {
            temp.writeBytes(bytes)
            if (!temp.renameTo(target)) throw IOException("Could not commit $name")
        } finally {
            if (temp.exists()) temp.delete()
        }
        runCatching { generateThumbnail(target, dayId) }
        StoredPhoto(fileName = name, sha256 = sha, sizeBytes = bytes.size.toLong())
    }

    suspend fun delete(fileName: String, dayId: String) = withContext(Dispatchers.IO) {
        File(originalsDir, fileName).delete()
        thumbnailFile(dayId).delete()
        Unit
    }

    suspend fun sha256Of(file: File): String = withContext(Dispatchers.IO) {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(64 * 1024)
            while (true) {
                val read = input.read(buffer)
                if (read <= 0) break
                digest.update(buffer, 0, read)
            }
        }
        digest.digest().toHex()
    }

    fun sha256(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-256").digest(bytes).toHex()

    /** Total bytes held by originals — what the Archive screen reports as "on disk". */
    suspend fun originalsSizeBytes(): Long = withContext(Dispatchers.IO) {
        originalsDir.listFiles()?.sumOf { it.length() } ?: 0L
    }

    /**
     * The derived thumbnail. Orientation IS applied here, because a thumbnail exists
     * only to be looked at; the original keeps its EXIF and its untouched pixels.
     */
    suspend fun generateThumbnail(original: File, dayId: String): File? = withContext(Dispatchers.IO) {
        if (!original.exists()) return@withContext null
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(original.absolutePath, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return@withContext null

        var sample = 1
        while (bounds.outWidth / (sample * 2) >= THUMB_EDGE && bounds.outHeight / (sample * 2) >= THUMB_EDGE) {
            sample *= 2
        }
        val decoded = BitmapFactory.decodeFile(
            original.absolutePath,
            BitmapFactory.Options().apply { inSampleSize = sample },
        ) ?: return@withContext null

        val rotated = applyExifOrientation(decoded, original)
        val out = thumbnailFile(dayId)
        val temp = File(out.parentFile, "${out.name}.part")
        temp.outputStream().use { rotated.compress(Bitmap.CompressFormat.JPEG, 82, it) }
        if (rotated !== decoded) rotated.recycle()
        decoded.recycle()
        if (!temp.renameTo(out)) {
            temp.delete()
            return@withContext null
        }
        out
    }

    /** Rebuilds any thumbnail that is missing — cheap self-healing on startup. */
    suspend fun regenerateMissingThumbnails(entries: List<Pair<String, String>>) = withContext(Dispatchers.IO) {
        entries.forEach { (dayId, fileName) ->
            if (!thumbnailFile(dayId).exists()) {
                runCatching { generateThumbnail(File(originalsDir, fileName), dayId) }
            }
        }
    }

    private fun applyExifOrientation(bitmap: Bitmap, file: File): Bitmap {
        val orientation = runCatching {
            ExifInterface(file.absolutePath).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL,
            )
        }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)

        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            else -> return bitmap
        }
        return runCatching {
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        }.getOrDefault(bitmap)
    }

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }

    companion object {
        /** The handoff's storage-full threshold: below this the save is refused up front. */
        const val MIN_FREE_BYTES = 40L * 1024 * 1024
        private const val THUMB_EDGE = 480
    }
}

data class StoredPhoto(val fileName: String, val sha256: String, val sizeBytes: Long)

class StorageFullException : IOException("Not enough free space to write the portrait")
