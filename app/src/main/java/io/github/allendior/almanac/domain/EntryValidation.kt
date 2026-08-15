package io.github.allendior.almanac.domain

/**
 * The rules an imported row must satisfy before it is allowed into the archive.
 *
 * Import is the one place where data the app did not create gets to become a record,
 * so every field is checked rather than trusted. Kept pure and free of Android types
 * so it can be tested directly.
 */
object EntryValidation {

    const val MAX_NOTE_LENGTH = 500
    private val SHA256 = Regex("^[0-9a-f]{64}$")
    private val SAFE_FILE_NAME = Regex("^[0-9]{4}-[0-9]{2}-[0-9]{2}_[0-9a-f]{6,32}\\.jpg$")

    sealed interface Result {
        data class Valid(val entry: PortraitEntry) : Result
        data class Rejected(val dayId: String?, val reason: String) : Result
    }

    fun validate(
        dayId: String?,
        capturedAtEpochMs: Long?,
        utcOffsetMinutes: Int?,
        fileName: String?,
        sha256: String?,
        mood: String?,
        numberValue: Double?,
        cameraFacing: String?,
        note: String?,
    ): Result {
        if (dayId.isNullOrBlank() || !DayId.isValid(dayId)) {
            return Result.Rejected(dayId, "not a valid calendar day")
        }
        if (capturedAtEpochMs == null || capturedAtEpochMs <= 0) {
            return Result.Rejected(dayId, "missing capture time")
        }
        if (utcOffsetMinutes == null || utcOffsetMinutes < -18 * 60 || utcOffsetMinutes > 18 * 60) {
            return Result.Rejected(dayId, "impossible UTC offset")
        }
        if (fileName.isNullOrBlank() || !isSafeFileName(fileName)) {
            return Result.Rejected(dayId, "unsafe or malformed file name")
        }
        if (sha256.isNullOrBlank() || !SHA256.matches(sha256)) {
            return Result.Rejected(dayId, "missing or malformed content hash")
        }
        if (numberValue != null && (numberValue.isNaN() || numberValue.isInfinite())) {
            return Result.Rejected(dayId, "number is not a real value")
        }
        val trimmedNote = note?.trim()?.takeIf { it.isNotEmpty() }
        if (trimmedNote != null && trimmedNote.length > MAX_NOTE_LENGTH) {
            return Result.Rejected(dayId, "note longer than $MAX_NOTE_LENGTH characters")
        }
        return Result.Valid(
            PortraitEntry(
                dayId = dayId,
                capturedAtEpochMs = capturedAtEpochMs,
                utcOffsetMinutes = utcOffsetMinutes,
                fileName = fileName,
                sha256 = sha256,
                mood = Mood.fromStorage(mood),
                numberValue = numberValue,
                cameraFacing = CameraFacing.fromStorage(cameraFacing),
                note = trimmedNote,
            ),
        )
    }

    /**
     * A name from an archive is used to open a file inside app-private storage, so it
     * must be a plain name in the shape this app writes — no traversal, no separators,
     * no surprises.
     */
    fun isSafeFileName(name: String): Boolean =
        !name.contains('/') &&
            !name.contains('\\') &&
            !name.contains("..") &&
            !name.startsWith(".") &&
            SAFE_FILE_NAME.matches(name)
}
