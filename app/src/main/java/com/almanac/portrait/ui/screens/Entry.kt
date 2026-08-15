package com.almanac.portrait.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.almanac.portrait.domain.PortraitEntry
import com.almanac.portrait.ui.Fmt
import com.almanac.portrait.ui.components.ButtonTone
import com.almanac.portrait.ui.components.ClassicalButton
import com.almanac.portrait.ui.components.ClassicalField
import com.almanac.portrait.ui.components.Hairline
import com.almanac.portrait.ui.components.Lucide
import com.almanac.portrait.ui.components.Plate
import com.almanac.portrait.ui.components.PortraitImage
import com.almanac.portrait.ui.components.Tag
import com.almanac.portrait.ui.components.TagTone
import com.almanac.portrait.ui.components.accessibleClick
import com.almanac.portrait.ui.theme.Ink
import com.almanac.portrait.ui.theme.Space
import com.almanac.portrait.ui.theme.Type
import java.io.File

@Composable
fun EntryScreen(
    entry: PortraitEntry,
    originalFile: File,
    thumbnailFile: File,
    numberLabel: String,
    noteEditorOpen: Boolean,
    onBack: () -> Unit,
    onNoteEditorOpen: (Boolean) -> Unit,
    onSaveNote: (String, String) -> Unit,
    onCompareWithToday: (PortraitEntry) -> Unit,
    onDelete: (PortraitEntry) -> Unit,
) {
    ScreenColumn(gap = 14.dp) {
        Row(
            Modifier
                .height(48.dp)
                .accessibleClick(onClick = onBack, label = "Back"),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Lucide.ChevronLeft, contentDescription = null, tint = Ink.textMuted, modifier = Modifier.size(16.dp))
            Box(Modifier.size(4.dp))
            Text("Back", style = Type.body13, color = Ink.textMuted)
        }

        Text(Fmt.long(entry.date), style = Type.title26, color = Ink.text)
        Text(Fmt.record(entry), style = Type.record(11.5f), color = Ink.textMuted)

        Row(horizontalArrangement = Arrangement.spacedBy(Space.s1)) {
            entry.mood?.let { Tag(it.label, TagTone.Neutral) }
            entry.numberValue?.let { Tag("${Fmt.number(it)} · $numberLabel", TagTone.Neutral) }
            Tag(entry.cameraFacing.label, TagTone.Outline)
        }

        Plate(Modifier.fillMaxWidth()) {
            PortraitImage(
                file = originalFile,
                contentDescription = "Portrait of ${Fmt.long(entry.date)}",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(3f / 4f),
                thumbnailFile = thumbnailFile,
            )
        }

        if (!originalFile.exists()) {
            Text(
                "The photograph for this day is missing from this phone. The record " +
                    "remains, but the image cannot be shown — restore it from an exported archive.",
                style = Type.body125,
                color = Ink.accent700,
            )
        }

        if (noteEditorOpen) {
            var draft by remember(entry.dayId) { mutableStateOf(TextFieldValue(entry.note.orEmpty())) }
            ClassicalField(
                label = "A line to your later self",
                value = draft,
                onValueChange = { draft = it },
                placeholder = "One line, only for you",
                singleLine = false,
                minHeight = 84.dp,
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Space.s2)) {
                ClassicalButton("Cancel", { onNoteEditorOpen(false) }, Modifier.weight(1f))
                ClassicalButton(
                    "Save note",
                    { onSaveNote(entry.dayId, draft.text) },
                    Modifier.weight(1f),
                    tone = ButtonTone.Primary,
                )
            }
        } else if (entry.note.isNullOrBlank()) {
            Text("No note on this day.", style = Type.body14, color = Ink.textFaint)
        } else {
            Text(entry.note, style = Type.noteItalic, color = Ink.text.copy(alpha = 0.8f))
        }

        Box(Modifier.height(Space.s1))
        Hairline()

        ClassicalButton(
            label = "Compare with today",
            onClick = { onCompareWithToday(entry) },
            modifier = Modifier.fillMaxWidth(),
        )
        ClassicalButton(
            label = if (entry.note.isNullOrBlank()) "Add a note" else "Edit note",
            onClick = { onNoteEditorOpen(!noteEditorOpen) },
            modifier = Modifier.fillMaxWidth(),
        )
        ClassicalButton(
            label = "Delete this portrait",
            onClick = { onDelete(entry) },
            tone = ButtonTone.Destructive,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
