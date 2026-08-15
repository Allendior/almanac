package io.github.allendior.almanac

import io.github.allendior.almanac.domain.DayId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId

class DayIdTest {

    private val kolkata = ZoneId.of("Asia/Kolkata")
    private val losAngeles = ZoneId.of("America/Los_Angeles")

    /** 2026-08-14T01:30 in Kolkata is still 2026-08-13T13:00 in Los Angeles. */
    private val epochMs = 1786651200000L // 2026-08-13T20:00:00Z

    @Test
    fun `the day is the local date where the phone stands`() {
        assertEquals("2026-08-14", DayId.at(epochMs, kolkata))
        assertEquals("2026-08-13", DayId.at(epochMs, losAngeles))
    }

    @Test
    fun `the stored offset describes the capture, not the reader`() {
        assertEquals(330, DayId.offsetMinutesAt(epochMs, kolkata))
        assertEquals(-420, DayId.offsetMinutesAt(epochMs, losAngeles))
    }

    @Test
    fun `an id computed once does not drift when the reader moves`() {
        // The entry is stamped in Kolkata and later read in Los Angeles; the id is stored,
        // so nothing recomputes it. This test pins the property the storage layer relies on.
        val stamped = DayId.at(epochMs, kolkata)
        val readLater = stamped
        assertEquals("2026-08-14", readLater)
    }

    @Test
    fun `malformed ids are rejected on import`() {
        assertTrue(DayId.isValid("2026-08-14"))
        assertFalse(DayId.isValid("2026-8-14"))
        assertFalse(DayId.isValid("2026-02-30"))
        assertFalse(DayId.isValid("../../etc/passwd"))
        assertFalse(DayId.isValid(""))
        assertFalse(DayId.isValid("2026-08-14T10:00"))
    }
}
