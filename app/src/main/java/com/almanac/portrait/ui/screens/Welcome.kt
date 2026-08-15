package com.almanac.portrait.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.almanac.portrait.ui.components.ButtonTone
import com.almanac.portrait.ui.components.ClassicalButton
import com.almanac.portrait.ui.components.Hairline
import com.almanac.portrait.ui.components.Kicker
import com.almanac.portrait.ui.theme.Ink
import com.almanac.portrait.ui.theme.Space
import com.almanac.portrait.ui.theme.Type

/**
 * Shown exactly once, on the very first launch — never again, and never on a later
 * cold start once dismissed. This is the app's one moment of introducing itself; after
 * this it gets out of the way and lets the archive speak for itself.
 *
 * No streak language, no call to action beyond "Begin", no claim about who the owner
 * is or is becoming — just what the app is, plainly, and who made it.
 */
@Composable
fun WelcomeScreen(onBegin: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Ink.bg)
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Kicker("Almanac")
        Box(Modifier.height(Space.s4))
        Text(
            "A quiet place to keep your face.",
            style = Type.title32.copy(textAlign = TextAlign.Center),
            color = Ink.text,
        )
        Box(Modifier.height(Space.s4))
        Text(
            "One portrait, most days, kept only on this phone. No accounts, no ads, " +
                "no feed, nothing sent anywhere. Years from now, this is what today looked like.",
            style = Type.body135.copy(textAlign = TextAlign.Center),
            color = Ink.textMuted,
        )
        Box(Modifier.height(Space.s6))
        Hairline(Modifier.fillMaxWidth())
        Box(Modifier.height(Space.s4))
        Text(
            "Made by Allen, with love, for you.",
            style = Type.italicColophon,
            color = Ink.textMuted,
            modifier = Modifier.fillMaxWidth(),
        )
        Box(Modifier.height(Space.s6))
        ClassicalButton(
            label = "Begin",
            onClick = onBegin,
            tone = ButtonTone.Primary,
            height = 52.dp,
            fontSize = 15.sp,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
