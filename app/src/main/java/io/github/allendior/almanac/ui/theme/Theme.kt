package io.github.allendior.almanac.ui.theme

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

/**
 * Material is present only to supply the plumbing Compose expects — ripples, text
 * defaults, selection colours. Every visible decision comes from the Classical tokens,
 * and no Material component is used with its stock styling.
 */
@Composable
fun AlmanacTheme(content: @Composable () -> Unit) {
    val scheme = lightColorScheme(
        primary = Ink.accent,
        onPrimary = Ink.bg,
        secondary = Ink.accent600,
        background = Ink.bg,
        onBackground = Ink.text,
        surface = Ink.surface,
        onSurface = Ink.text,
        outline = Ink.divider,
        error = Ink.accent700,
    )
    MaterialTheme(colorScheme = scheme) {
        CompositionLocalProvider(LocalTextStyle provides Type.body) {
            content()
        }
    }
}
