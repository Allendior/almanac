package io.github.allendior.almanac.ui

import io.github.allendior.almanac.data.Settings
import io.github.allendior.almanac.data.archive.ImportReport
import io.github.allendior.almanac.domain.CameraFacing
import io.github.allendior.almanac.domain.Mood
import io.github.allendior.almanac.domain.PortraitEntry
import java.time.LocalDate
import java.time.YearMonth

/** The five places the bottom navigation can take you. */
enum class Destination(val label: String) {
    TODAY("Today"),
    CALENDAR("Calendar"),
    TIMELINE("Timeline"),
    COMPARE("Compare"),
    ARCHIVE("Archive"),
}

/**
 * Screens that take over the whole surface and hide the navigation, because each is a
 * single task with a single way out.
 */
sealed interface Overlay {
    data object Capture : Overlay
    data object Review : Overlay
    data object Lock : Overlay
    data object Welcome : Overlay
    data object Introduction : Overlay
    data object Tips : Overlay
    data class Entry(val entryId: String) : Overlay

    /** A day with more than one portrait: pick which one to open. */
    data class DayEntries(val dayId: String) : Overlay
}

/** A portrait that has been taken but not yet saved. It lives only in memory. */
data class CaptureDraft(
    val bytes: ByteArray,
    val capturedAtEpochMs: Long,
    val utcOffsetMinutes: Int,
    val dayId: String,
    val cameraFacing: CameraFacing,
    val mood: Mood? = null,
    val number: String = "",
    val note: String = "",
) {
    // Identity on a ByteArray field: compared by content so recomposition is honest.
    override fun equals(other: Any?): Boolean =
        other is CaptureDraft &&
            bytes.contentEquals(other.bytes) &&
            capturedAtEpochMs == other.capturedAtEpochMs &&
            dayId == other.dayId &&
            cameraFacing == other.cameraFacing &&
            mood == other.mood &&
            number == other.number &&
            note == other.note

    override fun hashCode(): Int = bytes.contentHashCode() * 31 + dayId.hashCode()
}

sealed interface DialogState {
    data class ConfirmDelete(val entry: PortraitEntry) : DialogState
    data class ExportDone(val fileName: String, val entryCount: Int, val fileCount: Int) : DialogState
    data class ImportDone(val report: ImportReport) : DialogState
    data class SaveFailed(val reason: String) : DialogState
    data class Problem(val title: String, val message: String) : DialogState
}

enum class CompareTab { TWO_DATES, BY_YEAR }

data class CompareState(
    val tab: CompareTab = CompareTab.TWO_DATES,
    val leftEntryId: String? = null,
    val rightEntryId: String? = null,
    /** Which plate's date picker is open, if any. */
    val picking: ComparePane? = null,
)

enum class ComparePane { LEFT, RIGHT }

data class AlmanacUiState(
    val loading: Boolean = true,
    val entries: List<PortraitEntry> = emptyList(),
    val settings: Settings = Settings(),
    val today: LocalDate = LocalDate.now(),
    val destination: Destination = Destination.TODAY,
    val overlay: Overlay? = null,
    val draft: CaptureDraft? = null,
    val dialog: DialogState? = null,
    val calendarMonth: YearMonth = YearMonth.now(),
    val compare: CompareState = CompareState(),
    val noteEditorOpen: Boolean = false,
    val busy: Boolean = false,
    val onDiskBytes: Long = 0L,
    val missingOriginals: Int = 0,
) {
    val todayId: String get() = today.toString()

    /** All of today's entries, most recent first. A day may hold more than one. */
    val todayEntries: List<PortraitEntry> get() = entries.filter { it.dayId == todayId }

    /** The one to show prominently on Today — the most recently taken. */
    val todayLatest: PortraitEntry? get() = todayEntries.firstOrNull()

    /** Entries are held newest-first by capture time, so the first is the most recent. */
    val mostRecent: PortraitEntry? get() = entries.firstOrNull()

    val oldest: PortraitEntry? get() = entries.lastOrNull()

    fun entriesForDay(dayId: String): List<PortraitEntry> = entries.filter { it.dayId == dayId }

    fun entry(id: String?): PortraitEntry? =
        id?.let { target -> entries.firstOrNull { it.id == target } }
}
