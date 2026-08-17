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
import androidx.compose.ui.unit.dp
import io.github.allendior.almanac.ui.components.Hairline
import io.github.allendior.almanac.ui.components.Kicker
import io.github.allendior.almanac.ui.components.Lucide
import io.github.allendior.almanac.ui.components.accessibleClick
import io.github.allendior.almanac.ui.theme.Ink
import io.github.allendior.almanac.ui.theme.Space
import io.github.allendior.almanac.ui.theme.Type

private data class Tip(val title: String, val body: String)

private val tips = listOf(
    Tip(
        "Same spot, same distance",
        "A timelapse reads as change in the face, not the room. Anywhere that's easy to " +
            "repeat works — what matters is repeating it.",
    ),
    Tip(
        "Face the light, don't chase it",
        "A window you're facing, at roughly the same hour each time, does more than any " +
            "amount of care with a lamp. Avoid a bright window or light behind you.",
    ),
    Tip(
        "Rest the phone, don't hold it",
        "Propped against something still beats held in hand — less blur, and the framing " +
            "stays close to identical day to day, which is what makes the comparison work.",
    ),
    Tip(
        "One expression you can repeat",
        "Not a performance — whatever's easiest to do the same way each time. A neutral " +
            "look holds up better over years than a held smile does.",
    ),
    Tip(
        "Whenever suits you, but whenever",
        "The same rough time of day removes one more variable. Otherwise, no rule here — " +
            "this app will never tell you that you're late.",
    ),
)

/**
 * Plain advice for a more comparable portrait over time — not rules, and nothing here is
 * enforced or checked. Reachable from Archive, at any point, as many times as wanted.
 */
@Composable
fun TipsScreen(onBack: () -> Unit) {
    ScreenColumn(gap = Space.s4) {
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

        Kicker("Almanac")
        Text("A more comparable portrait", style = Type.title27, color = Ink.text)
        Text(
            "None of this is required, and nothing here is checked or scored. Consistency " +
                "just makes the years easier to compare side by side.",
            style = Type.body125,
            color = Ink.textMuted,
        )
        Hairline()

        tips.forEach { tip ->
            Column {
                Text(tip.title, style = Type.heading16, color = Ink.text)
                Box(Modifier.height(2.dp))
                Text(tip.body, style = Type.body125, color = Ink.textMuted)
            }
            Box(Modifier.height(Space.s3))
        }
    }
}
