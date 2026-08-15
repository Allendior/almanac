package com.almanac.portrait.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
import com.almanac.portrait.ui.theme.Hairline
import com.almanac.portrait.ui.theme.Ink
import com.almanac.portrait.ui.theme.Radius
import com.almanac.portrait.ui.theme.Space
import com.almanac.portrait.ui.theme.Type

/** 10px uppercase tracking-wide label. The system's smallest voice. */
@Composable
fun Kicker(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Ink.text.copy(alpha = 0.45f),
    letterSpacing: Float = 0.14f,
) {
    Text(
        text = text.uppercase(),
        style = Type.kicker(letterSpacing),
        color = color,
        modifier = modifier,
    )
}

/** The hairline rule that carries the structure everywhere. */
@Composable
fun Hairline(modifier: Modifier = Modifier, color: Color = Ink.divider) {
    Box(
        modifier
            .fillMaxWidth()
            .height(Hairline)
            .background(color),
    )
}

/**
 * The screen header used on every destination: kicker, Cormorant title,
 * optional subline, hairline.
 */
@Composable
fun ScreenHeader(
    kicker: String,
    title: String,
    subline: String? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    Column(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Kicker(kicker)
            trailing?.invoke()
        }
        Box(Modifier.height(Space.s2))
        Text(title, style = Type.title27, color = Ink.text)
        if (subline != null) {
            Box(Modifier.height(Space.s1))
            Text(subline, style = Type.body125, color = Ink.textMuted)
        }
        Box(Modifier.height(Space.s3))
        Hairline()
    }
}

/**
 * The plate: a photograph tipped into the page inside a 6dp surface mat with a
 * hairline outline.
 *
 * The prototype's CSS also applies a warm archival grade (sepia .22) to the image
 * itself. That grade is deliberately NOT reproduced: this archive's whole promise
 * is an unaltered record of a face over years, and a warm filter on display would
 * quietly change what the owner sees when comparing 2020 with 2030. The mat carries
 * the archival feeling; the photograph is left alone.
 */
@Composable
fun Plate(
    modifier: Modifier = Modifier,
    matWidth: Dp = 6.dp,
    content: @Composable () -> Unit,
) {
    Box(
        modifier
            .border(Hairline, Ink.divider)
            .background(Ink.surface)
            .padding(matWidth),
    ) {
        content()
    }
}

enum class ButtonTone { Primary, Secondary, Ghost, Destructive }

/**
 * Outlined actions. Colour is applied as stroke, never as a fill — there is no
 * solid accent block anywhere in this system.
 */
@Composable
fun ClassicalButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tone: ButtonTone = ButtonTone.Secondary,
    height: Dp = 48.dp,
    fontSize: androidx.compose.ui.unit.TextUnit = androidx.compose.ui.unit.TextUnit.Unspecified,
    enabled: Boolean = true,
    leading: ImageVector? = null,
    contentDescription: String? = null,
    /** Set on the dark capture screen, where the light-ground label colours vanish. */
    labelColorOverride: Color? = null,
) {
    val borderColor = when (tone) {
        ButtonTone.Primary -> Ink.accent
        ButtonTone.Secondary -> Ink.divider
        ButtonTone.Ghost -> Color.Transparent
        ButtonTone.Destructive -> Ink.divider
    }
    val labelColor = labelColorOverride ?: when (tone) {
        ButtonTone.Primary -> Ink.accent700
        ButtonTone.Secondary -> Ink.text
        ButtonTone.Ghost -> Ink.textMuted
        ButtonTone.Destructive -> Ink.accent700
    }
    // Scale the existing alpha rather than replacing it: the divider colour is already
    // text @ 16%, and copy(alpha = 1f) would turn every secondary border solid black.
    val alpha = if (enabled) 1f else 0.45f
    fun androidx.compose.ui.graphics.Color.dim() = copy(alpha = this.alpha * alpha)

    Box(
        modifier
            .heightIn(min = height)
            .height(height)
            .clip(RoundedCornerShape(Radius.md))
            .then(if (tone == ButtonTone.Ghost) Modifier else Modifier.border(Hairline, borderColor.dim(), RoundedCornerShape(Radius.md)))
            .accessibleClick(onClick = onClick, enabled = enabled, label = contentDescription ?: label)
            .padding(horizontal = Space.s3),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            if (leading != null) {
                Icon(leading, contentDescription = null, tint = labelColor.dim(), modifier = Modifier.size(16.dp))
                Box(Modifier.size(Space.s2))
            }
            Text(
                label,
                style = if (fontSize.isSpecified) Type.body14.copy(fontSize = fontSize) else Type.body14,
                color = labelColor.dim(),
                textAlign = TextAlign.Center,
            )
        }
    }
}

enum class TagTone { Accent, Neutral, Outline }

/** Small label tinted from the ramps. Tints are light steps, never the base fill. */
@Composable
fun Tag(text: String, tone: TagTone = TagTone.Neutral, modifier: Modifier = Modifier) {
    val (bg, border, fg) = when (tone) {
        TagTone.Accent -> Triple(Ink.accent.copy(alpha = 0.10f), Ink.accent.copy(alpha = 0.45f), Ink.accent700)
        TagTone.Neutral -> Triple(Ink.surface, Ink.divider, Ink.text.copy(alpha = 0.75f))
        TagTone.Outline -> Triple(Color.Transparent, Ink.divider, Ink.textMuted)
    }
    Box(
        modifier
            .clip(RoundedCornerShape(Radius.sm))
            .background(bg)
            .border(Hairline, border, RoundedCornerShape(Radius.sm))
            .padding(horizontal = Space.s2, vertical = 3.dp),
    ) {
        Text(text, style = Type.record(11f), color = fg)
    }
}
