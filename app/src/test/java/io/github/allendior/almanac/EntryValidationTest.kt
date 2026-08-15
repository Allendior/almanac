package io.github.allendior.almanac

import io.github.allendior.almanac.domain.CameraFacing
import io.github.allendior.almanac.domain.EntryValidation
import io.github.allendior.almanac.domain.Mood
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EntryValidationTest {

    private val hash = "a".repeat(64)

    private fun validate(
        dayId: String? = "2026-08-14",
        capturedAt: Long? = 1786757400000L,
        offset: Int? = 330,
        fileName: String? = "2026-08-14_aaaaaaaaaaaa.jpg",
        sha: String? = hash,
        mood: String? = "BRIGHT",
        number: Double? = 72.4,
        facing: String? = "FRONT",
        note: String? = "A line to my later self",
    ) = EntryValidation.validate(dayId, capturedAt, offset, fileName, sha, mood, number, facing, note)

    @Test
    fun `a well-formed row is accepted whole`() {
        val result = validate() as EntryValidation.Result.Valid
        assertEquals("2026-08-14", result.entry.dayId)
        assertEquals(Mood.BRIGHT, result.entry.mood)
        assertEquals(CameraFacing.FRONT, result.entry.cameraFacing)
        assertEquals(72.4, result.entry.numberValue!!, 0.0001)
    }

    @Test
    fun `optional fields may be absent`() {
        val result = validate(mood = null, number = null, note = null) as EntryValidation.Result.Valid
        assertEquals(null, result.entry.mood)
        assertEquals(null, result.entry.numberValue)
        assertEquals(null, result.entry.note)
    }

    @Test
    fun `an unknown mood degrades to no mood rather than failing the row`() {
        val result = validate(mood = "ECSTATIC") as EntryValidation.Result.Valid
        assertEquals(null, result.entry.mood)
    }

    @Test
    fun `a blank note becomes no note`() {
        val result = validate(note = "   ") as EntryValidation.Result.Valid
        assertEquals(null, result.entry.note)
    }

    @Test
    fun `path traversal in a file name is rejected`() {
        listOf(
            "../../../data/data/io.github.allendior.almanac/databases/almanac.db",
            "originals/../2026-08-14_aaaaaaaaaaaa.jpg",
            "/etc/passwd",
            ".hidden.jpg",
            "2026-08-14_aaaaaaaaaaaa.jpg.exe",
        ).forEach { name ->
            assertFalse("$name must not be accepted", EntryValidation.isSafeFileName(name))
            assertTrue(validate(fileName = name) is EntryValidation.Result.Rejected)
        }
    }

    @Test
    fun `the file name this app writes is accepted`() {
        assertTrue(EntryValidation.isSafeFileName("2026-08-14_3f7ac91b2d4e.jpg"))
    }

    @Test
    fun `a malformed day is rejected with a stated reason`() {
        val rejected = validate(dayId = "yesterday") as EntryValidation.Result.Rejected
        assertEquals("not a valid calendar day", rejected.reason)
    }

    @Test
    fun `impossible offsets are rejected`() {
        assertTrue(validate(offset = 20 * 60) is EntryValidation.Result.Rejected)
        assertTrue(validate(offset = -19 * 60) is EntryValidation.Result.Rejected)
        assertTrue(validate(offset = 0) is EntryValidation.Result.Valid)
    }

    @Test
    fun `a malformed hash is rejected`() {
        assertTrue(validate(sha = "not-a-hash") is EntryValidation.Result.Rejected)
        assertTrue(validate(sha = "A".repeat(64)) is EntryValidation.Result.Rejected)
        assertTrue(validate(sha = null) is EntryValidation.Result.Rejected)
    }

    @Test
    fun `non-finite numbers are rejected`() {
        assertTrue(validate(number = Double.NaN) is EntryValidation.Result.Rejected)
        assertTrue(validate(number = Double.POSITIVE_INFINITY) is EntryValidation.Result.Rejected)
    }

    @Test
    fun `an overlong note is rejected rather than truncated`() {
        val rejected = validate(note = "x".repeat(501)) as EntryValidation.Result.Rejected
        assertTrue(rejected.reason.contains("longer than"))
    }

    @Test
    fun `missing capture time is rejected`() {
        assertTrue(validate(capturedAt = null) is EntryValidation.Result.Rejected)
        assertTrue(validate(capturedAt = 0L) is EntryValidation.Result.Rejected)
    }
}
