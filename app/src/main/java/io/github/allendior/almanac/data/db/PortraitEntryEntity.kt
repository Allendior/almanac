package io.github.allendior.almanac.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import io.github.allendior.almanac.domain.CameraFacing
import io.github.allendior.almanac.domain.Mood
import io.github.allendior.almanac.domain.PortraitEntry

/**
 * One row per portrait. The primary key is [id], a UUID decided once at capture — not
 * [dayId], since a day may hold more than one entry. [dayId] is indexed rather than
 * unique, so "today's entries" and calendar/timeline grouping stay fast.
 */
@Entity(tableName = "portrait_entries", indices = [Index(value = ["day_id"])])
data class PortraitEntryEntity(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "day_id") val dayId: String,
    @ColumnInfo(name = "captured_at_epoch_ms") val capturedAtEpochMs: Long,
    @ColumnInfo(name = "utc_offset_minutes") val utcOffsetMinutes: Int,
    @ColumnInfo(name = "file_name") val fileName: String,
    @ColumnInfo(name = "sha256") val sha256: String,
    @ColumnInfo(name = "mood") val mood: String?,
    @ColumnInfo(name = "number_value") val numberValue: Double?,
    @ColumnInfo(name = "camera_facing") val cameraFacing: String,
    @ColumnInfo(name = "note") val note: String?,
) {
    fun toDomain() = PortraitEntry(
        id = id,
        dayId = dayId,
        capturedAtEpochMs = capturedAtEpochMs,
        utcOffsetMinutes = utcOffsetMinutes,
        fileName = fileName,
        sha256 = sha256,
        mood = Mood.fromStorage(mood),
        numberValue = numberValue,
        cameraFacing = CameraFacing.fromStorage(cameraFacing),
        note = note,
    )

    companion object {
        fun fromDomain(entry: PortraitEntry) = PortraitEntryEntity(
            id = entry.id,
            dayId = entry.dayId,
            capturedAtEpochMs = entry.capturedAtEpochMs,
            utcOffsetMinutes = entry.utcOffsetMinutes,
            fileName = entry.fileName,
            sha256 = entry.sha256,
            mood = entry.mood?.name,
            numberValue = entry.numberValue,
            cameraFacing = entry.cameraFacing.name,
            note = entry.note,
        )
    }
}
