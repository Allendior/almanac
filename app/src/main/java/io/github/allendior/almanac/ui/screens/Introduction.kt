package io.github.allendior.almanac.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.allendior.almanac.ui.components.ButtonTone
import io.github.allendior.almanac.ui.components.ClassicalButton
import io.github.allendior.almanac.ui.components.Hairline
import io.github.allendior.almanac.ui.components.Kicker
import io.github.allendior.almanac.ui.components.Lucide
import io.github.allendior.almanac.ui.theme.Ink
import io.github.allendior.almanac.ui.theme.Space
import io.github.allendior.almanac.ui.theme.Type

private data class IntroRow(val icon: ImageVector, val title: String, val body: String)

private val rows = listOf(
    IntroRow(Lucide.Camera, "Today", "The one daily action. Take a portrait, or add another if today already has one."),
    IntroRow(Lucide.Calendar, "Calendar", "Filled squares are days with a portrait. A small number means more than one that day."),
    IntroRow(Lucide.Rows, "Timeline", "Every portrait, newest first, grouped by month."),
    IntroRow(Lucide.Columns2, "Compare", "Two dates side by side, or one portrait per year, to see how much has changed."),
    IntroRow(Lucide.Archive, "Archive", "Export or import your archive, and everything the app does and does not do."),
)

/**
 * Shown once, right after Welcome, on first launch only — how the app is laid out, not
 * what it is. Skippable at any point; nothing here is graded or timed.
 */
@Composable
fun IntroductionScreen(onDone: () -> Unit) {
    ScreenColumn(gap = Space.s4) {
        Kicker("Almanac")
        Text("Five places, always the same", style = Type.title27, color = Ink.text)
        Text(
            "A short look at where things live. You can always come back to this app the " +
                "way it looks below — nothing changes after today.",
            style = Type.body125,
            color = Ink.textMuted,
        )
        Hairline()

        rows.forEach { row ->
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Icon(row.icon, contentDescription = null, tint = Ink.accent, modifier = Modifier.size(22.dp))
                Box(Modifier.size(Space.s3))
                Column(Modifier.weight(1f)) {
                    Text(row.title, style = Type.heading16, color = Ink.text)
                    Box(Modifier.height(2.dp))
                    Text(row.body, style = Type.body125, color = Ink.textMuted)
                }
            }
            Box(Modifier.height(Space.s3))
        }

        ClassicalButton(
            label = "Got it",
            onClick = onDone,
            tone = ButtonTone.Primary,
            height = 52.dp,
            fontSize = 15.sp,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
