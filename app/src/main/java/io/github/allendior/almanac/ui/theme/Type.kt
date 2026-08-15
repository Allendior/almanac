package io.github.allendior.almanac.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import io.github.allendior.almanac.R

/**
 * Cormorant Garamond over Lora, both bundled in res/font — the app must set type
 * with no network, so nothing is fetched at runtime.
 */
val Cormorant = FontFamily(
    Font(R.font.cormorant_garamond_regular, FontWeight.Normal),
    Font(R.font.cormorant_garamond_semibold, FontWeight.SemiBold),
    Font(R.font.cormorant_garamond_italic, FontWeight.Normal, FontStyle.Italic),
)

val Lora = FontFamily(
    Font(R.font.lora_regular, FontWeight.Normal),
    Font(R.font.lora_semibold, FontWeight.SemiBold),
    Font(R.font.lora_italic, FontWeight.Normal, FontStyle.Italic),
)

/**
 * Figures set tabular wherever they stand as figures or columns — kickers, counts,
 * meta lines, the calendar, stats. Running prose keeps its text figures, because
 * Lora's tabular feature also widens word-spaces and would loosen the paragraph.
 */
object Type {

    /** Display and section headings: the bigger the text, the lighter it sets. */
    fun heading(size: Int, weight: FontWeight = FontWeight.Normal) = TextStyle(
        fontFamily = Cormorant,
        fontWeight = weight,
        fontSize = size.sp,
        lineHeight = (size * 1.15f).sp,
        letterSpacing = (-0.015).em,
    )

    val display42 = heading(42)
    val title32 = heading(32)
    val title27 = heading(27)
    val title26 = heading(26)
    val title25 = heading(25)
    val title24 = heading(24)
    val title20 = heading(20)
    val title19 = heading(19)
    val title17 = heading(17)
    val heading16 = heading(16, FontWeight.SemiBold)

    val italicColophon = TextStyle(
        fontFamily = Cormorant,
        fontStyle = FontStyle.Italic,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        textAlign = TextAlign.Center,
    )

    /** Body: Lora 15/1.55, justified at a comfortable measure. */
    val body = TextStyle(fontFamily = Lora, fontSize = 15.sp, lineHeight = (15 * 1.55f).sp)
    val body14 = TextStyle(fontFamily = Lora, fontSize = 14.sp, lineHeight = (14 * 1.55f).sp)
    val body13 = TextStyle(fontFamily = Lora, fontSize = 13.sp, lineHeight = (13 * 1.5f).sp)
    val body135 = TextStyle(fontFamily = Lora, fontSize = 13.5f.sp, lineHeight = (13.5f * 1.5f).sp)
    val body125 = TextStyle(fontFamily = Lora, fontSize = 12.5f.sp, lineHeight = (12.5f * 1.5f).sp)
    val body115 = TextStyle(fontFamily = Lora, fontSize = 11.5f.sp, lineHeight = (11.5f * 1.45f).sp)

    val noteItalic = TextStyle(
        fontFamily = Lora,
        fontStyle = FontStyle.Italic,
        fontSize = 14.sp,
        lineHeight = (14 * 1.6f).sp,
        textAlign = TextAlign.Justify,
    )

    /** 10px uppercase, letter-spacing .08-.14em. Always tabular. */
    fun kicker(letterSpacing: Float = 0.12f) = TextStyle(
        fontFamily = Lora,
        fontWeight = FontWeight.SemiBold,
        fontSize = 10.sp,
        lineHeight = 13.sp,
        letterSpacing = letterSpacing.em,
        fontFeatureSettings = "tnum",
    )

    val kickerNav = TextStyle(
        fontFamily = Lora,
        fontWeight = FontWeight.SemiBold,
        fontSize = 9.5f.sp,
        lineHeight = 12.sp,
        letterSpacing = 0.07f.em,
        fontFeatureSettings = "tnum",
    )

    /** Figures that stand as figures: counts, calendar numerals, stats. */
    fun figures(size: Float, weight: FontWeight = FontWeight.Normal) = TextStyle(
        fontFamily = Lora,
        fontWeight = weight,
        fontSize = size.sp,
        lineHeight = (size * 1.4f).sp,
        fontFeatureSettings = "tnum",
    )

    /**
     * "Mono" in this system means the monospaced-feeling record voice: tabular Lora
     * with open tracking. No sans-serif is admitted anywhere, including here.
     */
    fun record(size: Float) = TextStyle(
        fontFamily = Lora,
        fontSize = size.sp,
        lineHeight = (size * 1.5f).sp,
        letterSpacing = 0.04f.em,
        fontFeatureSettings = "tnum",
    )
}
