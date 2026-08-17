package io.github.allendior.almanac.ui.screens

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.allendior.almanac.domain.PortraitEntry
import io.github.allendior.almanac.ui.Fmt
import io.github.allendior.almanac.ui.components.Hairline
import io.github.allendior.almanac.ui.components.Kicker
import io.github.allendior.almanac.ui.components.Lucide
import io.github.allendior.almanac.ui.components.Plate
import io.github.allendior.almanac.ui.components.PortraitImage
import io.github.allendior.almanac.ui.components.accessibleClick
import io.github.allendior.almanac.ui.theme.Ink
import io.github.allendior.almanac.ui.theme.Space
import io.github.allendior.almanac.ui.theme.Type
import java.io.File
import java.time.LocalDate

/**
 * Shown when a calendar day holds more than one portrait — a plain list, newest
 * first, so the owner can pick which one to open. Nothing here ranks or judges the
 * entries against each other; they are just the day's own, in the order they happened.
 */
@Composable
fun DayEntriesScreen(
    date: LocalDate,
    entries: List<PortraitEntry>,
    thumbnailOf: (PortraitEntry) -> File,
    onBack: () -> Unit,
    onOpenEntry: (String) -> Unit,
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

        Text(Fmt.long(date), style = Type.title26, color = Ink.text)
        Kicker("${entries.size} portraits this day")
        Hairline()

        entries.forEach { entry ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .accessibleClick(
                        onClick = { onOpenEntry(entry.id) },
                        label = "${Fmt.clock(entry)}, open",
                    ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Plate(matWidth = 4.dp, modifier = Modifier.size(72.dp)) {
                    PortraitImage(
                        file = thumbnailOf(entry),
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                        placeholderMark = 18.dp,
                    )
                }
                Box(Modifier.size(Space.s3))
                Text(Fmt.clock(entry), style = Type.body14, color = Ink.text)
            }
        }
    }
}
