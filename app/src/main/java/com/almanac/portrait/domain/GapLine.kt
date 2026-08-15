package com.almanac.portrait.domain

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * The line under the capture button on Today.
 *
 * This is the emotional core of the app: it acknowledges a gap without ever scoring it.
 * There is deliberately no streak, no completion percentage, and no tier that expresses
 * disappointment. A long absence is met with welcome, not accounting.
 */
object GapLine {

    const val OWNER_ADDRESS = "Ghanghas sahab"

    /**
     * @param lastRecorded the most recent recorded day, or null if the archive is empty
     * @param today the device's current local date
     */
    fun forArchive(lastRecorded: LocalDate?, today: LocalDate): String {
        if (lastRecorded == null) return "Your first portrait."
        val days = ChronoUnit.DAYS.between(lastRecorded, today).coerceAtLeast(0)
        return when {
            days <= 1L -> "Yesterday is recorded."
            days < 10L -> "Last portrait $days days ago. That is fine."
            else -> "It's been a while, $OWNER_ADDRESS. The archive kept your place."
        }
    }
}

/**
 * The figure under the two plates on Compare: "6 years, 5 months" apart.
 * Falls back to months, then to days, so a two-day gap never reads "0 years, 0 months".
 */
object Interval {

    fun between(a: LocalDate, b: LocalDate): String {
        val (from, to) = if (a.isBefore(b)) a to b else b to a
        val period = java.time.Period.between(from, to)
        val years = period.years
        val months = period.months
        return when {
            years > 0 && months > 0 -> "${plural(years, "year")}, ${plural(months, "month")}"
            years > 0 -> plural(years, "year")
            months > 0 -> plural(months, "month")
            else -> {
                val days = ChronoUnit.DAYS.between(from, to)
                if (days == 0L) "The same day" else plural(days.toInt(), "day")
            }
        }
    }

    private fun plural(value: Int, unit: String): String =
        if (value == 1) "1 $unit" else "$value ${unit}s"
}
