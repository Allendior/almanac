package io.github.allendior.almanac.ui

import io.github.allendior.almanac.domain.PortraitEntry
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Every date the owner reads is formatted here, in one voice: long dates in prose,
 * ISO in the record line. Locale is fixed to en-GB so "14 August 2026" never becomes
 * "August 14, 2026" halfway through the archive.
 */
object Fmt {

    private val archiveLocale: Locale = Locale.UK

    private val longDate = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", archiveLocale)
    private val longDateNoWeekday = DateTimeFormatter.ofPattern("d MMMM yyyy", archiveLocale)
    private val shortDate = DateTimeFormatter.ofPattern("d MMM", archiveLocale)
    private val monthYear = DateTimeFormatter.ofPattern("MMMM yyyy", archiveLocale)
    private val monthOnly = DateTimeFormatter.ofPattern("MMMM", archiveLocale)
    private val isoDate = DateTimeFormatter.ofPattern("yyyy-MM-dd", archiveLocale)
    private val clock = DateTimeFormatter.ofPattern("H:mm", archiveLocale)

    fun long(date: LocalDate): String = date.format(longDate)
    fun longNoWeekday(date: LocalDate): String = date.format(longDateNoWeekday)
    fun short(date: LocalDate): String = date.format(shortDate)
    fun monthYear(year: Int, month: Int): String = LocalDate.of(year, month, 1).format(monthYear)
    fun month(date: LocalDate): String = date.format(monthOnly)
    fun iso(date: LocalDate): String = date.format(isoDate)

    fun clock(entry: PortraitEntry): String = entry.capturedAtLocal.format(clock)

    /**
     * The record line under an entry: the day, the wall-clock time it was taken, and
     * the offset it was taken at — so a portrait taken at 07:54 in Kolkata still reads
     * 07:54 when the owner looks at it years later from anywhere else.
     */
    fun record(entry: PortraitEntry): String {
        val local = entry.capturedAtLocal
        return "${entry.dayId} · ${local.format(clock)} (UTC${offset(entry.utcOffsetMinutes)})"
    }

    fun offset(minutes: Int): String {
        val sign = if (minutes < 0) "-" else "+"
        val abs = kotlin.math.abs(minutes)
        return String.format(archiveLocale, "%s%02d:%02d", sign, abs / 60, abs % 60)
    }

    /** Figures group with a thin space: "2 342 days", never "2,342". */
    fun grouped(value: Long): String {
        val digits = value.toString()
        if (digits.length <= 3) return digits
        return digits.reversed().chunked(3).joinToString(" ").reversed()
    }

    fun grouped(value: Int): String = grouped(value.toLong())

    /** "1 day", "2 342 days" — the count never reads as a broken plural. */
    fun days(count: Int): String = if (count == 1) "1 day" else "${grouped(count)} days"

    fun portraits(count: Int): String =
        if (count == 1) "1 portrait" else "${grouped(count)} portraits"

    fun number(value: Double): String =
        if (value == value.toLong().toDouble()) value.toLong().toString()
        else String.format(archiveLocale, "%.1f", value)

    fun bytes(value: Long): String = when {
        value >= 1_000_000_000L -> String.format(archiveLocale, "%.1f GB", value / 1_000_000_000.0)
        value >= 1_000_000L -> String.format(archiveLocale, "%.0f MB", value / 1_000_000.0)
        value >= 1_000L -> String.format(archiveLocale, "%.0f kB", value / 1_000.0)
        else -> "$value B"
    }
}
