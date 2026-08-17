package io.github.allendior.almanac.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.allendior.almanac.domain.GapLine
import io.github.allendior.almanac.domain.PortraitEntry
import io.github.allendior.almanac.ui.AlmanacUiState
import io.github.allendior.almanac.ui.Fmt
import io.github.allendior.almanac.ui.components.ButtonTone
import io.github.allendior.almanac.ui.components.ClassicalButton
import io.github.allendior.almanac.ui.components.EmptyFrame
import io.github.allendior.almanac.ui.components.Hairline
import io.github.allendior.almanac.ui.components.Kicker
import io.github.allendior.almanac.ui.components.Plate
import io.github.allendior.almanac.ui.components.PortraitImage
import io.github.allendior.almanac.ui.components.Tag
import io.github.allendior.almanac.ui.components.TagTone
import io.github.allendior.almanac.ui.components.accessibleClick
import io.github.allendior.almanac.ui.theme.Ink
import io.github.allendior.almanac.ui.theme.Space
import io.github.allendior.almanac.ui.theme.Type
import java.io.File

@Composable
fun TodayScreen(
    state: AlmanacUiState,
    thumbnailOf: (PortraitEntry) -> File,
    originalOf: (PortraitEntry) -> File,
    onCapture: () -> Unit,
    onOpenEntry: (String) -> Unit,
    onOpenDay: (String) -> Unit,
    onNoteEditorOpen: (Boolean) -> Unit,
    onSaveNote: (String, String) -> Unit,
) {
    val todayEntries = state.todayEntries
    val latest = todayEntries.firstOrNull()
    val recorded = latest != null

    ScreenColumn {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Kicker("Almanac")
            // Nothing is claimed about the archive until it has actually been read.
            if (!state.loading) {
                Text(
                    Fmt.days(state.entries.map { it.dayId }.distinct().size),
                    style = Type.figures(11f),
                    color = Ink.textMuted,
                )
            }
        }

        Column {
            Text(Fmt.long(state.today), style = Type.title27, color = Ink.text)
            Box(Modifier.height(Space.s1))
            Text(
                if (recorded) "Recorded. Nothing else is asked of you." else "One portrait, whenever suits you.",
                style = Type.body125,
                color = Ink.textMuted,
            )
        }

        if (latest == null) {
            EmptyFrame(
                label = "Not recorded yet",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(3f / 4f),
            )
            ClassicalButton(
                label = "Take today's portrait",
                onClick = onCapture,
                tone = ButtonTone.Primary,
                height = 52.dp,
                fontSize = 15.sp,
                modifier = Modifier.fillMaxWidth(),
            )
            if (!state.loading) {
                Text(
                    GapLine.forArchive(state.mostRecent?.date, state.today),
                    style = Type.body125.copy(textAlign = TextAlign.Center),
                    color = Ink.textMuted,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        } else {
            RecordedToday(
                entry = latest,
                others = todayEntries.drop(1),
                originalOf = originalOf,
                thumbnailOf = thumbnailOf,
                numberLabel = state.settings.numberLabel,
                noteEditorOpen = state.noteEditorOpen,
                onNoteEditorOpen = onNoteEditorOpen,
                onSaveNote = onSaveNote,
                onAddAnother = onCapture,
                onOpenEntry = onOpenEntry,
            )
        }

        Box(Modifier.height(Space.s2))
        Hairline()
        Kicker("Nearby days")

        val nearby = state.entries
            .filter { it.dayId != state.todayId }
            .distinctBy { it.dayId }
            .take(4)
        if (state.loading) {
            Box(Modifier.height(Space.s2))
        } else if (nearby.isEmpty()) {
            Text(
                "Your archive is empty. Today's portrait will be the first page of it — " +
                    "in ten years this screen is the one that will matter.",
                style = Type.body125.copy(textAlign = TextAlign.Justify),
                color = Ink.textMuted,
            )
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(Space.s2)) {
                nearby.forEach { entry ->
                    Box(Modifier.weight(1f)) {
                        DayThumbnail(entry, thumbnailOf(entry)) { onOpenDay(entry.dayId) }
                    }
                }
                repeat(4 - nearby.size) { Box(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun RecordedToday(
    entry: PortraitEntry,
    others: List<PortraitEntry>,
    originalOf: (PortraitEntry) -> File,
    thumbnailOf: (PortraitEntry) -> File,
    numberLabel: String,
    noteEditorOpen: Boolean,
    onNoteEditorOpen: (Boolean) -> Unit,
    onSaveNote: (String, String) -> Unit,
    onAddAnother: () -> Unit,
    onOpenEntry: (String) -> Unit,
) {
    Plate(Modifier.fillMaxWidth()) {
        PortraitImage(
            file = originalOf(entry),
            contentDescription = "Today's portrait",
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(3f / 4f),
            thumbnailFile = thumbnailOf(entry),
        )
    }

    Box(Modifier.height(Space.s2))
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "Saved ${Fmt.clock(entry)} · kept on this phone only",
            style = Type.body115,
            color = Ink.textMuted,
        )
        Tag("Today recorded", TagTone.Accent)
    }

    Box(Modifier.height(Space.s2))
    Row(horizontalArrangement = Arrangement.spacedBy(Space.s1)) {
        entry.mood?.let { Tag(it.label, TagTone.Neutral) }
        entry.numberValue?.let { Tag("${Fmt.number(it)} · $numberLabel", TagTone.Neutral) }
        Tag(entry.cameraFacing.label, TagTone.Outline)
    }

    Box(Modifier.height(Space.s2))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Space.s2)) {
        ClassicalButton(
            label = if (entry.note.isNullOrBlank()) "Add a note" else "Edit note",
            onClick = { onNoteEditorOpen(!noteEditorOpen) },
            modifier = Modifier.weight(1f),
        )
        ClassicalButton(
            // Always additive — a day can hold more than one portrait, so there is
            // nothing here to replace.
            label = "Add another portrait",
            onClick = onAddAnother,
            modifier = Modifier.weight(1f),
        )
    }

    if (noteEditorOpen) {
        var draft by remember(entry.id) { mutableStateOf(TextFieldValue(entry.note.orEmpty())) }
        Box(Modifier.height(Space.s3))
        io.github.allendior.almanac.ui.components.ClassicalField(
            label = "A line to your later self",
            value = draft,
            onValueChange = { draft = it },
            placeholder = "One line, only for you",
            singleLine = false,
            minHeight = 84.dp,
            keyboardType = KeyboardOptions.Default.keyboardType,
        )
        Box(Modifier.height(Space.s2))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Space.s2)) {
            ClassicalButton("Cancel", { onNoteEditorOpen(false) }, Modifier.weight(1f))
            ClassicalButton(
                "Save note",
                { onSaveNote(entry.id, draft.text) },
                Modifier.weight(1f),
                tone = ButtonTone.Primary,
            )
        }
    } else if (!entry.note.isNullOrBlank()) {
        Box(Modifier.height(Space.s2))
        Text(entry.note, style = Type.noteItalic, color = Ink.text.copy(alpha = 0.75f))
    }

    if (others.isNotEmpty()) {
        Box(Modifier.height(Space.s3))
        Kicker("Also today")
        Box(Modifier.height(Space.s2))
        Row(horizontalArrangement = Arrangement.spacedBy(Space.s2)) {
            others.take(4).forEach { other ->
                Box(Modifier.weight(1f)) {
                    DayThumbnail(
                        other,
                        thumbnailOf(other),
                        label = Fmt.clock(other),
                    ) { onOpenEntry(other.id) }
                }
            }
            repeat((4 - others.size).coerceAtLeast(0)) { Box(Modifier.weight(1f)) }
        }
    }
}

/** A square thumbnail with a small caption set over the image, bottom-left. */
@Composable
fun DayThumbnail(
    entry: PortraitEntry,
    thumbnail: File,
    label: String = entry.date.dayOfMonth.toString(),
    onOpen: () -> Unit,
) {
    Plate(
        matWidth = 5.dp,
        modifier = Modifier
            .fillMaxWidth()
            .accessibleClick(
                onClick = onOpen,
                label = "${Fmt.longNoWeekday(entry.date)}, recorded",
            ),
    ) {
        Box {
            PortraitImage(
                file = thumbnail,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                placeholderMark = 22.dp,
            )
            Text(
                label,
                style = Type.record(9f).copy(
                    shadow = androidx.compose.ui.graphics.Shadow(
                        color = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.5f),
                        offset = androidx.compose.ui.geometry.Offset(0f, 1f),
                        blurRadius = 3f,
                    ),
                ),
                color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.92f),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(4.dp),
            )
        }
    }
}
