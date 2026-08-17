package io.github.allendior.almanac.domain

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

/**
 * One portrait.
 *
 * [id] is a UUID, decided once at capture and never recomputed — it is the entry's true
 * identity, and what export/import and delete/edit operate on. [dayId] is the ISO local
 * date at the moment of capture — also decided once and never recomputed, so travelling
 * across timezones can never move an existing entry to a different day — but it is no
 * longer unique: a day may hold more than one entry.
 */
data class PortraitEntry(
    val id: String,
    val dayId: String,
    val capturedAtEpochMs: Long,
    val utcOffsetMinutes: Int,
    val fileName: String,
    val sha256: String,
    val mood: Mood?,
    val numberValue: Double?,
    val cameraFacing: CameraFacing,
    val note: String?,
) {
    val date: LocalDate get() = LocalDate.parse(dayId)

    /** The wall-clock time the owner actually saw, regardless of where the phone is now. */
    val capturedAtLocal
        get() = Instant.ofEpochMilli(capturedAtEpochMs)
            .atOffset(ZoneOffset.ofTotalSeconds(utcOffsetMinutes * 60))
}

enum class Mood(val label: String) {
    FLAT("Flat"),
    STEADY("Steady"),
    TIRED("Tired"),
    BRIGHT("Bright"),
    RESTLESS("Restless");

    companion object {
        fun fromStorage(raw: String?): Mood? = raw?.let { v -> entries.firstOrNull { it.name == v } }
    }
}

enum class CameraFacing(val label: String) {
    FRONT("Front camera"),
    REAR("Rear camera");

    companion object {
        fun fromStorage(raw: String?): CameraFacing =
            entries.firstOrNull { it.name == raw } ?: FRONT
    }
}

/** The identity of "today" as the device sees it right now. */
object DayId {
    fun of(date: LocalDate): String = date.toString()

    fun today(zone: ZoneId = ZoneId.systemDefault()): String = LocalDate.now(zone).toString()

    fun at(epochMs: Long, zone: ZoneId = ZoneId.systemDefault()): String =
        Instant.ofEpochMilli(epochMs).atZone(zone).toLocalDate().toString()

    fun offsetMinutesAt(epochMs: Long, zone: ZoneId = ZoneId.systemDefault()): Int =
        zone.rules.getOffset(Instant.ofEpochMilli(epochMs)).totalSeconds / 60

    /** True only for a well-formed ISO date; used to reject junk on import. */
    fun isValid(raw: String): Boolean = runCatching { LocalDate.parse(raw).toString() == raw }
        .getOrDefault(false)
}
