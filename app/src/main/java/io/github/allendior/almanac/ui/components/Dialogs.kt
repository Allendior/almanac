package io.github.allendior.almanac.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.allendior.almanac.ui.theme.Hairline
import io.github.allendior.almanac.ui.theme.Ink
import io.github.allendior.almanac.ui.theme.Radius
import io.github.allendior.almanac.ui.theme.Space
import io.github.allendior.almanac.ui.theme.Type

/**
 * The Classical dialog: a bordered card on a 50% neutral-900 scrim, confined to the
 * screen rather than to the system window, so it reads as part of the page.
 */
@Composable
fun ClassicalDialog(
    title: String,
    body: String,
    onDismiss: () -> Unit,
    confirmLabel: String? = null,
    onConfirm: (() -> Unit)? = null,
    dismissLabel: String = "Close",
    destructive: Boolean = false,
    extra: (@Composable () -> Unit)? = null,
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Ink.scrim)
            .accessibleClick(onClick = onDismiss, label = "Dismiss")
            .semantics { },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            Modifier
                .padding(horizontal = Space.s6)
                .widthIn(max = 340.dp)
                .clip(RoundedCornerShape(Radius.lg))
                .background(Ink.bg)
                .border(Hairline, Ink.divider, RoundedCornerShape(Radius.lg))
                .padding(Space.s4),
        ) {
            Text(title, style = Type.title20, color = Ink.text)
            Box(Modifier.height(Space.s2))
            Text(
                body,
                style = Type.body125.copy(textAlign = androidx.compose.ui.text.style.TextAlign.Justify),
                color = Ink.textMuted,
            )
            if (extra != null) {
                Box(Modifier.height(Space.s3))
                extra()
            }
            Box(Modifier.height(Space.s4))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Space.s2),
            ) {
                ClassicalButton(
                    label = dismissLabel,
                    onClick = onDismiss,
                    tone = ButtonTone.Secondary,
                    modifier = Modifier.weight(1f),
                )
                if (confirmLabel != null && onConfirm != null) {
                    ClassicalButton(
                        label = confirmLabel,
                        onClick = onConfirm,
                        tone = if (destructive) ButtonTone.Destructive else ButtonTone.Primary,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

/** A row of the import report: a count against a plain label. */
@Composable
fun ReportRow(label: String, value: String) {
    Column {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = Space.s1),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(label, style = Type.body13, color = Ink.text)
            Text(value, style = Type.figures(13f), color = Ink.textMuted)
        }
        Hairline()
    }
}
