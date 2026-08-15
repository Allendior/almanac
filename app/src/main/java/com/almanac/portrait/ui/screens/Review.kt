package com.almanac.portrait.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.almanac.portrait.domain.Mood
import com.almanac.portrait.ui.CaptureDraft
import com.almanac.portrait.ui.Fmt
import com.almanac.portrait.ui.components.ButtonTone
import com.almanac.portrait.ui.components.ClassicalButton
import com.almanac.portrait.ui.components.ClassicalField
import com.almanac.portrait.ui.components.Footnote
import com.almanac.portrait.ui.components.Kicker
import com.almanac.portrait.ui.components.Plate
import com.almanac.portrait.ui.components.ReadOnlyField
import com.almanac.portrait.ui.components.SelectChip
import com.almanac.portrait.ui.theme.Ink
import com.almanac.portrait.ui.theme.Space
import com.almanac.portrait.ui.theme.Type
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.time.LocalDate

/**
 * The last look before anything is written. Nothing has touched disk at this point:
 * the portrait exists only in memory, and Discard really does mean gone.
 */
@Composable
fun ReviewScreen(
    draft: CaptureDraft,
    numberLabel: String,
    busy: Boolean,
    onMood: (Mood?) -> Unit,
    onNumber: (String) -> Unit,
    onNote: (String) -> Unit,
    onRetake: () -> Unit,
    onSave: () -> Unit,
    onDiscard: () -> Unit,
) {
    var number by remember { mutableStateOf(TextFieldValue(draft.number)) }
    var note by remember { mutableStateOf(TextFieldValue(draft.note)) }

    ScreenColumn(gap = 14.dp) {
        Kicker("Before saving")
        Text(Fmt.long(LocalDate.parse(draft.dayId)), style = Type.title24, color = Ink.text)

        Plate(Modifier.fillMaxWidth()) {
            DraftImage(
                draft.bytes,
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(3f / 4f),
            )
        }

        Column {
            Text("Mood", style = Type.body125, color = Ink.textMuted)
            Box(Modifier.height(Space.s1))
            Row(horizontalArrangement = Arrangement.spacedBy(Space.s1)) {
                Mood.entries.forEach { mood ->
                    SelectChip(
                        label = mood.label,
                        selected = draft.mood == mood,
                        onClick = { onMood(if (draft.mood == mood) null else mood) },
                        height = 44.dp,
                        modifier = Modifier.weight(1f),
                        contentPadding = 2.dp,
                        textStyle = Type.body125,
                    )
                }
            }
        }

        ClassicalField(
            label = "$numberLabel — optional",
            value = number,
            onValueChange = {
                number = it
                onNumber(it.text)
            },
            keyboardType = KeyboardType.Decimal,
        )

        ReadOnlyField(label = "Camera", value = draft.cameraFacing.label, kicker = "auto")

        ClassicalField(
            label = "A line to your later self (optional)",
            value = note,
            onValueChange = {
                note = it
                onNote(it.text)
            },
            placeholder = "One line, only for you",
        )

        Box(Modifier.height(Space.s1))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Space.s2)) {
            ClassicalButton(
                label = "Retake",
                onClick = onRetake,
                modifier = Modifier.weight(1f),
                height = 52.dp,
                enabled = !busy,
            )
            ClassicalButton(
                label = if (busy) "Saving…" else "Save to archive",
                onClick = onSave,
                modifier = Modifier.weight(1.4f),
                tone = ButtonTone.Primary,
                height = 52.dp,
                fontSize = 15.sp,
                enabled = !busy,
            )
        }

        ClassicalButton(
            label = "Discard",
            onClick = onDiscard,
            tone = ButtonTone.Ghost,
            modifier = Modifier.fillMaxWidth(),
            enabled = !busy,
        )

        Footnote(
            "Saving writes the original file to private app storage and one row to the " +
                "local database. Nothing leaves the phone.",
        )
    }
}

/**
 * Decodes a downsampled copy for the screen only. The bytes that get saved are the
 * untouched originals held in the draft — this preview never becomes the record.
 */
@Composable
private fun DraftImage(bytes: ByteArray, modifier: Modifier) {
    var bitmap by remember(bytes) { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(bytes) {
        bitmap = withContext(Dispatchers.IO) { decodePreview(bytes) }
    }
    Box(modifier.background(Ink.textGhost), contentAlignment = Alignment.Center) {
        bitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = "The portrait you just took",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

private fun decodePreview(bytes: ByteArray): Bitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
    var sample = 1
    while (bounds.outWidth / (sample * 2) >= 1080) sample *= 2
    val decoded = BitmapFactory.decodeByteArray(
        bytes,
        0,
        bytes.size,
        BitmapFactory.Options().apply { inSampleSize = sample },
    ) ?: return null

    val orientation = runCatching {
        ExifInterface(ByteArrayInputStream(bytes))
            .getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
    }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)

    val matrix = Matrix()
    when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
        ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
        ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        else -> return decoded
    }
    return runCatching {
        Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)
    }.getOrDefault(decoded)
}
