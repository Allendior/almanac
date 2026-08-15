package io.github.allendior.almanac.ui

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.allendior.almanac.data.PortraitRepository
import io.github.allendior.almanac.domain.PortraitEntry
import io.github.allendior.almanac.ui.components.BottomNav
import io.github.allendior.almanac.ui.components.ClassicalDialog
import io.github.allendior.almanac.ui.components.Lucide
import io.github.allendior.almanac.ui.components.NavItem
import io.github.allendior.almanac.ui.components.ReportRow
import io.github.allendior.almanac.ui.screens.ArchiveScreen
import io.github.allendior.almanac.ui.screens.CalendarScreen
import io.github.allendior.almanac.ui.screens.CaptureScreen
import io.github.allendior.almanac.ui.screens.CompareScreen
import io.github.allendior.almanac.ui.screens.EntryScreen
import io.github.allendior.almanac.ui.screens.LockScreen
import io.github.allendior.almanac.ui.screens.WelcomeScreen
import io.github.allendior.almanac.ui.screens.ReviewScreen
import io.github.allendior.almanac.ui.screens.TimelineScreen
import io.github.allendior.almanac.ui.screens.TodayScreen
import io.github.allendior.almanac.ui.theme.Ink
import java.io.File

private val navItems = listOf(
    NavItem("Today", Lucide.Camera),
    NavItem("Calendar", Lucide.Calendar),
    NavItem("Timeline", Lucide.Rows),
    NavItem("Compare", Lucide.Columns2),
    NavItem("Archive", Lucide.Archive),
)

@Composable
fun AlmanacRoot(
    state: AlmanacUiState,
    viewModel: AlmanacViewModel,
    repository: PortraitRepository,
    onRequestUnlock: () -> Unit,
) {
    val originalOf: (PortraitEntry) -> File = { repository.originalFile(it) }
    val thumbnailOf: (PortraitEntry) -> File = { entry ->
        val thumb = repository.thumbnailFile(entry)
        if (thumb.exists()) thumb else repository.originalFile(entry)
    }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/zip"),
    ) { uri: Uri? -> uri?.let(viewModel::export) }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri: Uri? -> uri?.let(viewModel::import) }

    Box(
        Modifier
            .fillMaxSize()
            .background(Ink.bg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
    ) {
        when (val overlay = state.overlay) {
            is Overlay.Lock -> LockScreen(onUnlock = onRequestUnlock)

            is Overlay.Welcome -> WelcomeScreen(onBegin = viewModel::dismissWelcome)

            is Overlay.Capture -> {
                BackHandler { viewModel.closeOverlay() }
                CaptureScreen(
                    guide = state.settings.guide,
                    today = state.today,
                    onGuideChange = viewModel::setGuide,
                    onCaptured = viewModel::onCaptured,
                    onFailed = viewModel::onCaptureFailed,
                    onCancel = viewModel::closeOverlay,
                )
            }

            is Overlay.Review -> {
                val draft = state.draft
                if (draft == null) {
                    viewModel.closeOverlay()
                } else {
                    BackHandler { viewModel.retake() }
                    ReviewScreen(
                        draft = draft,
                        numberLabel = state.settings.numberLabel,
                        busy = state.busy,
                        onMood = { viewModel.updateDraft(mood = it, clearMood = it == null) },
                        onNumber = { viewModel.updateDraft(number = it) },
                        onNote = { viewModel.updateDraft(note = it) },
                        onRetake = viewModel::retake,
                        onSave = viewModel::saveDraft,
                        onDiscard = viewModel::discardDraft,
                    )
                }
            }

            is Overlay.Entry -> {
                val entry = state.entry(overlay.dayId)
                if (entry == null) {
                    viewModel.closeOverlay()
                } else {
                    BackHandler { viewModel.closeOverlay() }
                    EntryScreen(
                        entry = entry,
                        originalFile = originalOf(entry),
                        thumbnailFile = thumbnailOf(entry),
                        numberLabel = state.settings.numberLabel,
                        noteEditorOpen = state.noteEditorOpen,
                        onBack = viewModel::closeOverlay,
                        onNoteEditorOpen = viewModel::setNoteEditorOpen,
                        onSaveNote = viewModel::saveNote,
                        onCompareWithToday = viewModel::compareWithToday,
                        onDelete = viewModel::confirmDelete,
                    )
                }
            }

            null -> Destinations(
                state = state,
                viewModel = viewModel,
                originalOf = originalOf,
                thumbnailOf = thumbnailOf,
                onExport = { exportLauncher.launch(viewModel.suggestedExportName()) },
                onImport = { importLauncher.launch(arrayOf("application/zip", "application/octet-stream")) },
            )
        }

        state.dialog?.let { dialog ->
            BackHandler { viewModel.dismissDialog() }
            Dialogs(dialog, viewModel)
        }
    }
}

@Composable
private fun Destinations(
    state: AlmanacUiState,
    viewModel: AlmanacViewModel,
    originalOf: (PortraitEntry) -> File,
    thumbnailOf: (PortraitEntry) -> File,
    onExport: () -> Unit,
    onImport: () -> Unit,
) {
    if (state.destination != Destination.TODAY) {
        BackHandler { viewModel.navigate(Destination.TODAY) }
    }

    Column(Modifier.fillMaxSize()) {
        Box(Modifier.weight(1f)) {
            when (state.destination) {
                Destination.TODAY -> TodayScreen(
                    state = state,
                    thumbnailOf = thumbnailOf,
                    originalOf = originalOf,
                    onCapture = viewModel::openCapture,
                    onOpenEntry = viewModel::openEntry,
                    onNoteEditorOpen = viewModel::setNoteEditorOpen,
                    onSaveNote = viewModel::saveNote,
                )

                Destination.CALENDAR -> CalendarScreen(
                    state = state,
                    thumbnailOf = thumbnailOf,
                    onMonthChange = viewModel::showCalendarMonth,
                    onOpenEntry = viewModel::openEntry,
                )

                Destination.TIMELINE -> TimelineScreen(
                    state = state,
                    thumbnailOf = thumbnailOf,
                    onOpenEntry = viewModel::openEntry,
                    onGoToToday = {
                        viewModel.navigate(Destination.TODAY)
                        viewModel.openCapture()
                    },
                )

                Destination.COMPARE -> CompareScreen(
                    state = state,
                    originalOf = originalOf,
                    thumbnailOf = thumbnailOf,
                    onTab = viewModel::setCompareTab,
                    onOpenPicker = viewModel::openComparePicker,
                    onChooseDate = viewModel::chooseCompareDate,
                    onOpenEntry = viewModel::openEntry,
                )

                Destination.ARCHIVE -> ArchiveScreen(
                    state = state,
                    onExport = onExport,
                    onImport = onImport,
                    onToggleLock = viewModel::setBiometricLock,
                    onCycleGuide = viewModel::cycleGuide,
                    onBackup = {
                        viewModel.showBackupNotBuilt()
                    },
                )
            }
        }
        BottomNav(
            items = navItems,
            selectedIndex = state.destination.ordinal,
            onSelect = { viewModel.navigate(Destination.entries[it]) },
        )
    }
}

@Composable
private fun Dialogs(dialog: DialogState, viewModel: AlmanacViewModel) {
    when (dialog) {
        is DialogState.ConfirmDelete -> ClassicalDialog(
            title = "Delete this portrait?",
            body = "${Fmt.long(dialog.entry.date)} will be removed from the archive and its " +
                "file deleted from this phone. This cannot be undone, and no copy exists " +
                "unless you have exported one.",
            onDismiss = viewModel::dismissDialog,
            dismissLabel = "Keep it",
            confirmLabel = "Delete",
            onConfirm = { viewModel.deleteConfirmed(dialog.entry) },
            destructive = true,
        )

        is DialogState.ExportDone -> ClassicalDialog(
            title = "Archive written",
            body = "${Fmt.grouped(dialog.entryCount)} entries and ${Fmt.grouped(dialog.fileCount)} " +
                "photographs were written to ${dialog.fileName}. The originals stay on this phone — " +
                "an export is a copy, never a move.",
            onDismiss = viewModel::dismissDialog,
            dismissLabel = "Close",
        )

        is DialogState.ImportDone -> {
            val report = dialog.report
            ClassicalDialog(
                title = if (report.failed) "That archive couldn't be read" else "Import finished",
                body = report.failureReason
                    ?: "Nothing already in your archive was overwritten, and anything that " +
                    "could not be read was left untouched.",
                onDismiss = viewModel::dismissDialog,
                dismissLabel = "Close",
                extra = if (report.failed) null else {
                    {
                        Column {
                            ReportRow("Added", Fmt.grouped(report.added))
                            ReportRow("Already in archive", Fmt.grouped(report.duplicate))
                            ReportRow("Unreadable files", Fmt.grouped(report.unreadable))
                            ReportRow("Rejected metadata", Fmt.grouped(report.rejected))
                        }
                    }
                },
            )
        }

        is DialogState.SaveFailed -> ClassicalDialog(
            title = "Couldn't save the portrait",
            body = dialog.reason,
            onDismiss = viewModel::discardDraft,
            dismissLabel = "Discard",
            confirmLabel = "Try again",
            onConfirm = {
                viewModel.dismissDialog()
                viewModel.saveDraft()
            },
        )

        is DialogState.Problem -> ClassicalDialog(
            title = dialog.title,
            body = dialog.message,
            onDismiss = viewModel::dismissDialog,
            dismissLabel = "Close",
        )
    }
}
