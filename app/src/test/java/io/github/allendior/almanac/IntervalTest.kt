package io.github.allendior.almanac

import io.github.allendior.almanac.domain.Interval
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class IntervalTest {

    @Test
    fun `years and months together`() {
        assertEquals(
            "6 years, 5 months",
            Interval.between(LocalDate.of(2020, 3, 6), LocalDate.of(2026, 8, 13)),
        )
    }

    @Test
    fun `whole years drop the months`() {
        assertEquals("2 years", Interval.between(LocalDate.of(2024, 8, 14), LocalDate.of(2026, 8, 14)))
    }

    @Test
    fun `singular units are not pluralised`() {
        assertEquals("1 year", Interval.between(LocalDate.of(2025, 8, 14), LocalDate.of(2026, 8, 14)))
        assertEquals("1 month", Interval.between(LocalDate.of(2026, 7, 14), LocalDate.of(2026, 8, 14)))
        assertEquals("1 day", Interval.between(LocalDate.of(2026, 8, 13), LocalDate.of(2026, 8, 14)))
    }

    @Test
    fun `short gaps fall back to days rather than reading zero`() {
        assertEquals("9 days", Interval.between(LocalDate.of(2026, 8, 5), LocalDate.of(2026, 8, 14)))
    }

    @Test
    fun `the same day is named, not counted`() {
        assertEquals("The same day", Interval.between(LocalDate.of(2026, 8, 14), LocalDate.of(2026, 8, 14)))
    }

    @Test
    fun `argument order does not matter`() {
        val a = LocalDate.of(2020, 3, 6)
        val b = LocalDate.of(2026, 8, 13)
        assertEquals(Interval.between(a, b), Interval.between(b, a))
    }
}
