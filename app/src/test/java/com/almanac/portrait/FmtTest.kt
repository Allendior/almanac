package com.almanac.portrait

import com.almanac.portrait.ui.Fmt
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class FmtTest {

    @Test
    fun `counts are never a broken plural`() {
        assertEquals("1 day", Fmt.days(1))
        assertEquals("0 days", Fmt.days(0))
        assertEquals("2 days", Fmt.days(2))
        assertEquals("1 portrait", Fmt.portraits(1))
        assertEquals("2 portraits", Fmt.portraits(2))
    }

    @Test
    fun `figures group with a thin space, not a comma`() {
        val thin = "\u2009"
        assertEquals("342", Fmt.grouped(342))
        assertEquals("2${thin}342", Fmt.grouped(2342))
        assertEquals("12${thin}342", Fmt.grouped(12342))
        assertEquals("1${thin}234${thin}567", Fmt.grouped(1234567))
        assertEquals("2${thin}342 days", Fmt.days(2342))
    }

    @Test
    fun `dates read the same way for the whole archive`() {
        val date = LocalDate.of(2026, 8, 14)
        assertEquals("Friday, 14 August 2026", Fmt.long(date))
        assertEquals("14 August 2026", Fmt.longNoWeekday(date))
        assertEquals("2026-08-14", Fmt.iso(date))
    }

    @Test
    fun `utc offsets are signed and zero-padded`() {
        assertEquals("+05:30", Fmt.offset(330))
        assertEquals("-07:00", Fmt.offset(-420))
        assertEquals("+00:00", Fmt.offset(0))
    }

    @Test
    fun `a whole number loses its decimal, a fraction keeps one`() {
        assertEquals("72", Fmt.number(72.0))
        assertEquals("72.4", Fmt.number(72.4))
    }
}
