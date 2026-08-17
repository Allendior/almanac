package io.github.allendior.almanac.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The Classical design system's tokens, transcribed from _ds/.../styles.css.
 * The prototype frame is 412x892 CSS px, which is 1:1 with dp on a phone,
 * so every px in the handoff is carried over as dp unchanged.
 *
 * The theme-reactive tokens (bg, surface, text, accent, accent600, accent700, scrim)
 * are backed by Compose state rather than plain `val`s, so every existing call site
 * that reads e.g. `Ink.text` stays exactly as it was written — only the values behind
 * it change when the system switches between light and dark. [applyDarkMode] is the
 * one place that decides which palette is live; call it once per composition from the
 * theme root ([AlmanacTheme]).
 */
object Ink {
    private val lightBg = Color(0xFFF3F2F2)
    private val lightSurface = Color(0xFFEAE9E9)
    private val lightText = Color(0xFF201F1D)
    private val lightAccent = Color(0xFFB68235)
    private val lightAccent600 = Color(0xFFA06F24)
    private val lightAccent700 = Color(0xFF7D5411)
    private val lightScrim = Color(0xFF2D2B2B).copy(alpha = 0.5f)

    // The capture screen's always-dark ground, reused here as the dark theme's palette
    // — it was already tuned for exactly this: warm near-black, warm near-white text,
    // a brighter gold that reads on a dark ground instead of sinking into it.
    private val darkGroundBg = Color(0xFF191817)
    private val darkGroundSurface = Color(0xFF242220)
    private val darkGroundText = Color(0xFFF3F2F2)
    private val darkGroundAccent = Color(0xFFE6BE78)
    private val darkGroundScrim = Color.Black.copy(alpha = 0.6f)

    var bg by mutableStateOf(lightBg)
        private set
    var surface by mutableStateOf(lightSurface)
        private set
    var text by mutableStateOf(lightText)
        private set
    var accent by mutableStateOf(lightAccent)
        private set
    var accent600 by mutableStateOf(lightAccent600)
        private set
    var accent700 by mutableStateOf(lightAccent700)
        private set
    var scrim by mutableStateOf(lightScrim)
        private set

    /** text @ 16% — the hairline that carries the whole structure. */
    val divider: Color get() = text.copy(alpha = 0.16f)

    // Text opacities used verbatim by the handoff, now derived from whichever text
    // colour is live so they never drift out of sync with a theme switch.
    val textMuted: Color get() = text.copy(alpha = 0.55f)
    val textFaint: Color get() = text.copy(alpha = 0.45f)
    val textGhost: Color get() = text.copy(alpha = 0.03f)

    val neutral100 = Color(0xFFF8F4F4)
    val neutral300 = Color(0xFFD7D3D3)
    val neutral500 = Color(0xFF9B9797)
    val neutral900 = Color(0xFF2D2B2B)

    val accent100 = Color(0xFFFFF3E4)
    val accent400 = Color(0xFFE1AD66)

    /**
     * The capture screen always inverts the ground, regardless of system theme — that
     * was true before dark mode existed and stays true now, so these three are plain
     * constants, not theme-reactive.
     */
    val darkBg = darkGroundBg
    val darkText = darkGroundText
    val guideGold = darkGroundAccent

    /** Swaps every theme-reactive token to the light or dark palette. */
    fun applyDarkMode(dark: Boolean) {
        if (dark) {
            bg = darkGroundBg
            surface = darkGroundSurface
            text = darkGroundText
            accent = darkGroundAccent
            accent600 = darkGroundAccent
            accent700 = darkGroundAccent
            scrim = darkGroundScrim
        } else {
            bg = lightBg
            surface = lightSurface
            text = lightText
            accent = lightAccent
            accent600 = lightAccent600
            accent700 = lightAccent700
            scrim = lightScrim
        }
    }
}

/** Density 1.15x, straight from the token sheet. */
object Space {
    val s1: Dp = 4.6.dp
    val s2: Dp = 9.2.dp
    val s3: Dp = 13.8.dp
    val s4: Dp = 18.4.dp
    val s6: Dp = 27.6.dp
    val s8: Dp = 36.8.dp
}

object Radius {
    val sm: Dp = 2.dp
    val md: Dp = 4.dp
    val lg: Dp = 7.dp
}

/** The one hairline weight. Everything structural is drawn, not filled. */
val Hairline: Dp = 1.dp

/** Minimum touch target, applied to every interactive element. */
val TouchTarget: Dp = 48.dp
