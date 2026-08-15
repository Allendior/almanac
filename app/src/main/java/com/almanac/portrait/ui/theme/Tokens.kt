package com.almanac.portrait.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The Classical design system's tokens, transcribed from _ds/.../styles.css.
 * The prototype frame is 412x892 CSS px, which is 1:1 with dp on a phone,
 * so every px in the handoff is carried over as dp unchanged.
 */
object Ink {
    val bg = Color(0xFFF3F2F2)
    val surface = Color(0xFFEAE9E9)
    val text = Color(0xFF201F1D)
    val accent = Color(0xFFB68235)

    /** text @ 16% — the hairline that carries the whole structure. */
    val divider = text.copy(alpha = 0.16f)

    // Text opacities used verbatim by the handoff.
    val textMuted = text.copy(alpha = 0.55f)
    val textFaint = text.copy(alpha = 0.45f)
    val textGhost = text.copy(alpha = 0.03f)

    val neutral100 = Color(0xFFF8F4F4)
    val neutral300 = Color(0xFFD7D3D3)
    val neutral500 = Color(0xFF9B9797)
    val neutral900 = Color(0xFF2D2B2B)

    val accent100 = Color(0xFFFFF3E4)
    val accent400 = Color(0xFFE1AD66)
    val accent600 = Color(0xFFA06F24)

    /** Accent at paragraph size needs a deep ramp step to clear contrast. */
    val accent700 = Color(0xFF7D5411)

    // The capture screen inverts the ground.
    val darkBg = Color(0xFF191817)
    val darkText = Color(0xFFF3F2F2)
    val guideGold = Color(0xFFE6BE78)

    /** Dialog scrim: neutral-900 at 50%. */
    val scrim = neutral900.copy(alpha = 0.5f)
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
