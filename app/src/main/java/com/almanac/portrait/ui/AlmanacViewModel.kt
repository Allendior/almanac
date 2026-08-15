package com.almanac.portrait.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.almanac.portrait.AlmanacApp
import com.almanac.portrait.data.FramingGuide
import com.almanac.portrait.data.PortraitRepository
import com.almanac.portrait.data.Settings
import com.almanac.portrait.data.SettingsStore
import com.almanac.portrait.data.StorageFullException
import com.almanac.portrait.data.archive.ArchiveExporter
import com.almanac.portrait.data.archive.ArchiveImporter
import com.almanac.portrait.domain.CameraFacing
import com.almanac.portrait.domain.DayId
import com.almanac.portrait.domain.Mood
import com.almanac.portrait.domain.PortraitEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

class AlmanacViewModel(
    private val repository: PortraitRepository,
    private val settingsStore: SettingsStore,
    private val exporter: ArchiveExporter,
    private val importer: ArchiveImporter,
) : ViewModel() {

    private val _state = MutableStateFlow(AlmanacUiState())
    val state: StateFlow<AlmanacUiState> = _state.asStateFlow()

    /**
     * True once this process has decided what to show at cold start (Lock, Welcome, or
     * straight to Today). Settings load asynchronously from DataStore, so this decision
     * cannot be made synchronously in `onCreate` — it has to wait for the first real
     * emission and then be made exactly once, or a later preference change (e.g.
     * enabling the lock from Archive) would incorrectly re-trigger it mid-session.
     */
    private var coldStartHandled = false

    init {
        viewModelScope.launch {
            combine(repository.entries, settingsStore.settings) { entries, settings ->
                entries to settings
            }.collect { (entries, settings) ->
                _state.update { current ->
                    val next = current.copy(loading = false, entries = entries, settings = settings)
                    if (!coldStartHandled) {
                        coldStartHandled = true
                        next.copy(overlay = coldStartOverlay(settings))
                    } else {
                        next
                    }
                }
            }
        }
        viewModelScope.launch {
            repository.repairThumbnails()
            refreshDiskFacts()
        }
    }

    private fun coldStartOverlay(settings: Settings): Overlay? = when {
        settings.biometricLock -> Overlay.Lock
        !settings.hasSeenWelcome -> Overlay.Welcome
        else -> null
    }

    /** Called on every resume: the calendar day can change while the app is open. */
    fun refreshToday() {
        val today = LocalDate.now()
        _state.update {
            if (it.today == today) it else it.copy(today = today, calendarMonth = YearMonth.from(today))
        }
    }

    /** After a successful unlock (or when no lock was required), the welcome screen is next if unseen. */
    fun unlock() = _state.update {
        it.copy(overlay = if (!it.settings.hasSeenWelcome) Overlay.Welcome else null)
    }

    fun dismissWelcome() {
        viewModelScope.launch { settingsStore.setHasSeenWelcome(true) }
        _state.update { it.copy(overlay = null) }
    }

    // ---- navigation -------------------------------------------------------

    /** Changing destination clears dialogs, editors and pickers, per the handoff. */
    fun navigate(destination: Destination) = _state.update {
        it.copy(
            destination = destination,
            overlay = null,
            dialog = null,
            noteEditorOpen = false,
            compare = it.compare.copy(picking = null),
        )
    }

    fun openEntry(dayId: String) = _state.update {
        it.copy(overlay = Overlay.Entry(dayId), dialog = null, noteEditorOpen = false)
    }

    fun closeOverlay() = _state.update { it.copy(overlay = null, noteEditorOpen = false) }

    fun openCapture() = _state.update { it.copy(overlay = Overlay.Capture, dialog = null) }

    fun dismissDialog() = _state.update { it.copy(dialog = null) }

    /**
     * Phase 3 is a separate approval gate and has not been built. The row exists so the
     * boundary is visible, and says plainly that it does nothing yet rather than
     * offering a screen that pretends.
     */
    fun showBackupNotBuilt() = _state.update {
        it.copy(
            dialog = DialogState.Problem(
                "Home server backup isn't built yet",
                "This is Phase 3 and needs a design you have approved before any of it is " +
                    "written. Until then this app declares no INTERNET permission, so nothing " +
                    "can leave the phone at all. Exporting an archive to a folder is your backup.",
            ),
        )
    }

    fun showCalendarMonth(month: YearMonth) = _state.update { it.copy(calendarMonth = month) }

    fun setNoteEditorOpen(open: Boolean) = _state.update { it.copy(noteEditorOpen = open) }

    // ---- capture and review ----------------------------------------------

    fun onCaptured(bytes: ByteArray, facing: CameraFacing) {
        val now = System.currentTimeMillis()
        _state.update {
            it.copy(
                overlay = Overlay.Review,
                draft = CaptureDraft(
                    bytes = bytes,
                    capturedAtEpochMs = now,
                    utcOffsetMinutes = DayId.offsetMinutesAt(now),
                    dayId = DayId.at(now),
                    cameraFacing = facing,
                ),
            )
        }
    }

    fun onCaptureFailed(message: String) = _state.update {
        it.copy(overlay = null, dialog = DialogState.Problem("The camera didn't return a photograph", message))
    }

    fun updateDraft(mood: Mood? = null, number: String? = null, note: String? = null, clearMood: Boolean = false) {
        _state.update { s ->
            val draft = s.draft ?: return@update s
            s.copy(
                draft = draft.copy(
                    mood = if (clearMood) null else mood ?: draft.mood,
                    number = number ?: draft.number,
                    note = note ?: draft.note,
                ),
            )
        }
    }

    fun retake() = _state.update { it.copy(overlay = Overlay.Capture) }

    fun discardDraft() = _state.update { it.copy(overlay = null, draft = null, destination = Destination.TODAY) }

    fun saveDraft() {
        val draft = _state.value.draft ?: return
        if (_state.value.busy) return
        _state.update { it.copy(busy = true) }
        viewModelScope.launch {
            try {
                repository.save(
                    dayId = draft.dayId,
                    capturedAtEpochMs = draft.capturedAtEpochMs,
                    utcOffsetMinutes = draft.utcOffsetMinutes,
                    bytes = draft.bytes,
                    mood = draft.mood,
                    numberValue = draft.number.trim().replace(',', '.').toDoubleOrNull(),
                    cameraFacing = draft.cameraFacing,
                    note = draft.note,
                )
                _state.update {
                    it.copy(busy = false, draft = null, overlay = null, destination = Destination.TODAY)
                }
                refreshDiskFacts()
            } catch (e: StorageFullException) {
                // The draft is kept in memory so "Try again" is a real option.
                _state.update {
                    it.copy(
                        busy = false,
                        dialog = DialogState.SaveFailed(
                            "There is under 40 MB free on this phone, so nothing was written. " +
                                "The portrait is still held in memory — free some space and try again.",
                        ),
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        busy = false,
                        dialog = DialogState.SaveFailed(
                            e.message ?: "The portrait could not be written to this phone.",
                        ),
                    )
                }
            }
        }
    }

    // ---- entries ----------------------------------------------------------

    fun saveNote(dayId: String, note: String) {
        viewModelScope.launch {
            repository.updateNote(dayId, note)
            _state.update { it.copy(noteEditorOpen = false) }
        }
    }

    fun confirmDelete(entry: PortraitEntry) = _state.update {
        it.copy(dialog = DialogState.ConfirmDelete(entry))
    }

    fun deleteConfirmed(entry: PortraitEntry) {
        viewModelScope.launch {
            repository.delete(entry)
            _state.update { it.copy(dialog = null, overlay = null) }
            refreshDiskFacts()
        }
    }

    // ---- compare ----------------------------------------------------------

    fun setCompareTab(tab: CompareTab) = _state.update {
        it.copy(compare = it.compare.copy(tab = tab, picking = null))
    }

    fun openComparePicker(pane: ComparePane) = _state.update {
        it.copy(compare = it.compare.copy(picking = if (it.compare.picking == pane) null else pane))
    }

    fun chooseCompareDate(pane: ComparePane, dayId: String) = _state.update {
        it.copy(
            compare = when (pane) {
                ComparePane.LEFT -> it.compare.copy(leftDayId = dayId, picking = null)
                ComparePane.RIGHT -> it.compare.copy(rightDayId = dayId, picking = null)
            },
        )
    }

    /** "Compare with today": this entry becomes *then*, the newest entry becomes *now*. */
    fun compareWithToday(entry: PortraitEntry) = _state.update {
        it.copy(
            destination = Destination.COMPARE,
            overlay = null,
            compare = CompareState(
                tab = CompareTab.TWO_DATES,
                leftDayId = entry.dayId,
                rightDayId = it.mostRecent?.dayId,
            ),
        )
    }

    // ---- settings ---------------------------------------------------------

    fun cycleGuide() {
        viewModelScope.launch { settingsStore.setGuide(_state.value.settings.guide.next()) }
    }

    fun setGuide(guide: FramingGuide) {
        viewModelScope.launch { settingsStore.setGuide(guide) }
    }

    fun setBiometricLock(enabled: Boolean) {
        viewModelScope.launch { settingsStore.setBiometricLock(enabled) }
    }

    // ---- export and import ------------------------------------------------

    fun suggestedExportName(): String = exporter.suggestedFileName(_state.value.today)

    fun export(target: Uri) {
        _state.update { it.copy(busy = true) }
        viewModelScope.launch {
            try {
                val entries = repository.entriesNow()
                val result = exporter.export(target, entries)
                _state.update {
                    it.copy(
                        busy = false,
                        dialog = DialogState.ExportDone(
                            fileName = exporter.suggestedFileName(it.today),
                            entryCount = result.entryCount,
                            fileCount = result.fileCount,
                        ),
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        busy = false,
                        dialog = DialogState.Problem(
                            "The archive wasn't written",
                            e.message ?: "The chosen file could not be written. Nothing on this phone changed.",
                        ),
                    )
                }
            }
        }
    }

    fun import(source: Uri) {
        _state.update { it.copy(busy = true) }
        viewModelScope.launch {
            val report = importer.import(source)
            _state.update { it.copy(busy = false, dialog = DialogState.ImportDone(report)) }
            refreshDiskFacts()
        }
    }

    private suspend fun refreshDiskFacts() {
        val bytes = repository.onDiskBytes()
        val missing = repository.missingOriginals().size
        _state.update { it.copy(onDiskBytes = bytes, missingOriginals = missing) }
    }

    private inline fun MutableStateFlow<AlmanacUiState>.update(block: (AlmanacUiState) -> AlmanacUiState) {
        value = block(value)
    }

    class Factory(private val app: AlmanacApp) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val c = app.container
            return AlmanacViewModel(c.repository, c.settings, c.exporter, c.importer) as T
        }
    }
}
