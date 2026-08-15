package io.github.allendior.almanac.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.allendior.almanac.ui.theme.Hairline
import io.github.allendior.almanac.ui.theme.Ink
import io.github.allendior.almanac.ui.theme.Radius
import io.github.allendior.almanac.ui.theme.Space
import io.github.allendior.almanac.ui.theme.Type

/** A selectable outlined chip: mood, guide, compare tab, date choice. */
@Composable
fun SelectChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp = 44.dp,
    accentOnDark: Boolean = false,
    /** Tightened when several chips share one row, so no label ever wraps. */
    contentPadding: androidx.compose.ui.unit.Dp = Space.s3,
    textStyle: androidx.compose.ui.text.TextStyle = Type.body13,
) {
    val border = when {
        selected && accentOnDark -> Ink.guideGold
        selected -> Ink.accent
        accentOnDark -> Ink.darkText.copy(alpha = 0.30f)
        else -> Ink.divider
    }
    val fg = when {
        selected && accentOnDark -> Ink.guideGold
        selected -> Ink.accent700
        accentOnDark -> Ink.darkText.copy(alpha = 0.75f)
        else -> Ink.text.copy(alpha = 0.75f)
    }
    Box(
        modifier
            .heightIn(min = height)
            .height(height)
            .clip(RoundedCornerShape(Radius.md))
            .border(Hairline, border, RoundedCornerShape(Radius.md))
            .accessibleClick(onClick = onClick, label = label, role = Role.RadioButton)
            .padding(horizontal = contentPadding),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, style = textStyle, color = fg, maxLines = 1, softWrap = false)
    }
}

/**
 * The 42x24 switch from the handoff. 180ms is the system's one animation budget —
 * calm and short, no bounce.
 */
@Composable
fun ClassicalSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
) {
    val knobOffset by animateDpAsState(if (checked) 21.dp else 3.dp, tween(180), label = "knob")
    val border by animateColorAsState(if (checked) Ink.accent else Ink.divider, tween(180), label = "border")
    val fill by animateColorAsState(
        if (checked) Ink.accent.copy(alpha = 0.12f) else Color.Transparent,
        tween(180),
        label = "fill",
    )
    Box(
        modifier
            .size(TouchTargetWidth, TouchTargetHeight)
            .accessibleClick(
                onClick = { onCheckedChange(!checked) },
                label = label,
                role = Role.Switch,
            ),
        contentAlignment = Alignment.CenterEnd,
    ) {
        Box(
            Modifier
                .width(42.dp)
                .height(24.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(fill)
                .border(Hairline, border, RoundedCornerShape(12.dp)),
        ) {
            Box(
                Modifier
                    .offset(x = knobOffset)
                    .align(Alignment.CenterStart)
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(if (checked) Ink.accent else Ink.text.copy(alpha = 0.35f)),
            )
        }
    }
}

private val TouchTargetWidth = 64.dp
private val TouchTargetHeight = 48.dp

/** A labelled single-line field on the Classical hairline. */
@Composable
fun ClassicalField(
    label: String,
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    minHeight: androidx.compose.ui.unit.Dp = 48.dp,
    enabled: Boolean = true,
    kicker: String? = null,
) {
    Column(modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = Type.body125, color = Ink.textMuted)
            if (kicker != null) {
                Box(Modifier.width(Space.s2))
                Kicker(kicker, color = Ink.text.copy(alpha = 0.35f))
            }
        }
        Box(Modifier.height(Space.s1))
        Box(
            Modifier
                .fillMaxWidth()
                .heightIn(min = minHeight)
                .clip(RoundedCornerShape(Radius.md))
                .border(Hairline, Ink.divider, RoundedCornerShape(Radius.md))
                .padding(horizontal = Space.s3, vertical = Space.s2),
            contentAlignment = Alignment.CenterStart,
        ) {
            if (value.text.isEmpty() && placeholder.isNotEmpty()) {
                Text(placeholder, style = Type.body14, color = Ink.text.copy(alpha = 0.32f))
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                enabled = enabled,
                singleLine = singleLine,
                textStyle = Type.body14.copy(color = if (enabled) Ink.text else Ink.textMuted),
                cursorBrush = SolidColor(Ink.accent),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = keyboardType),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/** A read-only field: shows a value the app decided, and says so. */
@Composable
fun ReadOnlyField(label: String, value: String, kicker: String? = null, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = Type.body125, color = Ink.textMuted)
            if (kicker != null) {
                Box(Modifier.width(Space.s2))
                Kicker(kicker, color = Ink.text.copy(alpha = 0.35f))
            }
        }
        Box(Modifier.height(Space.s1))
        Box(
            Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .clip(RoundedCornerShape(Radius.md))
                .border(Hairline, Ink.divider, RoundedCornerShape(Radius.md))
                .padding(horizontal = Space.s3, vertical = Space.s2),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(value, style = Type.body14, color = Ink.textMuted)
        }
    }
}

/** A label/value row from the Archive screen. */
@Composable
fun LabelValueRow(label: String, value: String, valueStyle: androidx.compose.ui.text.TextStyle = Type.figures(13.5f)) {
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 40.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = Type.body135, color = Ink.text)
        Text(value, style = valueStyle, color = Ink.textMuted)
    }
}

/** A tappable settings row: title, muted subtitle, accent arrow, hairline below. */
@Composable
fun ActionRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector = Lucide.ArrowRight,
    enabled: Boolean = true,
) {
    Column {
        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .accessibleClick(onClick = onClick, enabled = enabled, label = title)
                .padding(vertical = Space.s2),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(title, style = Type.body14, color = Ink.text)
                Box(Modifier.height(2.dp))
                Text(subtitle, style = Type.body115, color = Ink.textMuted)
            }
            Icon(icon, contentDescription = null, tint = Ink.accent, modifier = Modifier.size(18.dp))
        }
        Hairline()
    }
}

/** Muted explanatory prose set justified, the system's footnote voice. */
@Composable
fun Footnote(text: String, modifier: Modifier = Modifier, center: Boolean = false) {
    Text(
        text,
        style = if (center) {
            Type.body115.copy(textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        } else {
            Type.body115.copy(textAlign = androidx.compose.ui.text.style.TextAlign.Justify)
        },
        color = Ink.textMuted,
        modifier = modifier.fillMaxWidth(),
    )
}
