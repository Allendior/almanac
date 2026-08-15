package com.almanac.portrait

import com.almanac.portrait.domain.GapLine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import java.time.LocalDate

class GapLineTest {

    private val today = LocalDate.of(2026, 8, 14)

    @Test
    fun `empty archive invites the first portrait`() {
        assertEquals("Your first portrait.", GapLine.forArchive(null, today))
    }

    @Test
    fun `recorded today reads as yesterday tier`() {
        assertEquals("Yesterday is recorded.", GapLine.forArchive(today, today))
    }

    @Test
    fun `one day ago reads as yesterday`() {
        assertEquals("Yesterday is recorded.", GapLine.forArchive(today.minusDays(1), today))
    }

    @Test
    fun `two to nine days is stated without judgement`() {
        assertEquals("Last portrait 2 days ago. That is fine.", GapLine.forArchive(today.minusDays(2), today))
        assertEquals("Last portrait 9 days ago. That is fine.", GapLine.forArchive(today.minusDays(9), today))
    }

    @Test
    fun `ten days or more is met with welcome, not accounting`() {
        val expected = "It's been a while, Ghanghas sahab. The archive kept your place."
        assertEquals(expected, GapLine.forArchive(today.minusDays(10), today))
        assertEquals(expected, GapLine.forArchive(today.minusYears(3), today))
    }

    @Test
    fun `a future last-recorded date never produces a negative count`() {
        // Can happen if the owner travels backwards across the date line.
        assertEquals("Yesterday is recorded.", GapLine.forArchive(today.plusDays(1), today))
    }

    @Test
    fun `no tier ever mentions a streak or a failure`() {
        val banned = listOf("streak", "missed", "failed", "broke", "don't", "should")
        val lines = listOf(null, today, today.minusDays(3), today.minusDays(400))
            .map { GapLine.forArchive(it, today) }
        lines.forEach { line ->
            banned.forEach { word ->
                assertFalse("\"$line\" must not contain \"$word\"", line.lowercase().contains(word))
            }
        }
    }
}
