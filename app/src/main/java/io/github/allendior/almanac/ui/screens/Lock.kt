package io.github.allendior.almanac.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.allendior.almanac.ui.components.ButtonTone
import io.github.allendior.almanac.ui.components.ClassicalButton
import io.github.allendior.almanac.ui.components.Kicker
import io.github.allendior.almanac.ui.components.Lucide
import io.github.allendior.almanac.ui.theme.Ink
import io.github.allendior.almanac.ui.theme.Space
import io.github.allendior.almanac.ui.theme.Type

/** Shown at cold start only, and only when the owner has asked for it. */
@Composable
fun LockScreen(onUnlock: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Ink.bg)
            .padding(horizontal = 40.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Kicker("Almanac")
        Box(Modifier.height(Space.s6))
        Icon(Lucide.Lock, contentDescription = null, tint = Ink.accent, modifier = Modifier.size(34.dp))
        Box(Modifier.height(Space.s4))
        Text(
            "Your archive is locked. Unlock with your fingerprint to continue.",
            style = Type.body135.copy(textAlign = TextAlign.Center),
            color = Ink.textMuted,
        )
        Box(Modifier.height(Space.s6))
        ClassicalButton(
            label = "Unlock",
            onClick = onUnlock,
            tone = ButtonTone.Primary,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
