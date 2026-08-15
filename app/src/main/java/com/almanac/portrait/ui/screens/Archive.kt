package com.almanac.portrait.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.almanac.portrait.ui.AlmanacUiState
import com.almanac.portrait.ui.Fmt
import com.almanac.portrait.ui.components.ActionRow
import com.almanac.portrait.ui.components.ClassicalButton
import com.almanac.portrait.ui.components.ClassicalSwitch
import com.almanac.portrait.ui.components.Footnote
import com.almanac.portrait.ui.components.Hairline
import com.almanac.portrait.ui.components.Kicker
import com.almanac.portrait.ui.components.LabelValueRow
import com.almanac.portrait.ui.components.Lucide
import com.almanac.portrait.ui.theme.Ink
import com.almanac.portrait.ui.theme.Space
import com.almanac.portrait.ui.theme.Type

@Composable
fun ArchiveScreen(
    state: AlmanacUiState,
    onExport: () -> Unit,
    onImport: () -> Unit,
    onToggleLock: (Boolean) -> Unit,
    onCycleGuide: () -> Unit,
    onBackup: () -> Unit,
) {
    ScreenColumn(gap = 12.dp) {
        Kicker("Almanac")
        Text("Your archive", style = Type.title27, color = Ink.text)
        Hairline()

        LabelValueRow("Portraits", Fmt.grouped(state.entries.size))
        LabelValueRow("On disk", Fmt.bytes(state.onDiskBytes))
        LabelValueRow("Location", "app-private storage", Type.record(11.5f))

        if (state.missingOriginals > 0) {
            Text(
                "${Fmt.days(state.missingOriginals)} here have a record but no " +
                    "photograph on this phone. Importing an archive that contains them will restore the images.",
                style = Type.body125,
                color = Ink.accent700,
            )
        }

        Box(Modifier.height(Space.s2))
        Kicker("Keep a copy")
        Box(Modifier.height(Space.s1))
        ActionRow(
            title = "Export archive",
            subtitle = "ZIP of originals + index.json, to a folder you choose",
            onClick = onExport,
            icon = Lucide.ArrowDownToLine,
            enabled = !state.busy,
        )
        ActionRow(
            title = "Import an archive",
            subtitle = "Validated, duplicate-safe, with a report",
            onClick = onImport,
            icon = Lucide.ArrowUpToLine,
            enabled = !state.busy,
        )

        Box(Modifier.height(Space.s2))
        Kicker("Privacy")
        Box(Modifier.height(Space.s1))

        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text("Unlock with fingerprint", style = Type.body14, color = Ink.text)
                Text(
                    "Asked once, when the app is opened cold",
                    style = Type.body115,
                    color = Ink.textMuted,
                )
            }
            ClassicalSwitch(
                checked = state.settings.biometricLock,
                onCheckedChange = onToggleLock,
                label = "Unlock with fingerprint",
            )
        }
        Hairline()

        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text("Framing guide", style = Type.body14, color = Ink.text)
                Text(state.settings.guide.label, style = Type.body115, color = Ink.textMuted)
            }
            ClassicalButton(
                label = "Change",
                onClick = onCycleGuide,
                height = 44.dp,
                contentDescription = "Change the framing guide, currently ${state.settings.guide.label}",
            )
        }
        Hairline()

        Box(Modifier.height(Space.s2))
        Kicker("Permissions this app declares")
        Box(Modifier.height(Space.s1))
        Text("android.permission.CAMERA", style = Type.record(11.5f), color = Ink.text)
        Text("to take the portrait", style = Type.body115, color = Ink.textMuted)
        Box(Modifier.height(Space.s1))
        Text("android.permission.USE_BIOMETRIC", style = Type.record(11.5f), color = Ink.text)
        Text(
            "to ask the system whether it is you, for the optional lock",
            style = Type.body115,
            color = Ink.textMuted,
        )
        Box(Modifier.height(Space.s1))
        Text(
            "…DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION",
            style = Type.record(11.5f),
            color = Ink.textMuted,
        )
        Text(
            "defined by this app for its own use, granted to nothing else",
            style = Type.body115,
            color = Ink.textMuted,
        )
        Box(Modifier.height(Space.s2))
        Text(
            "— and nothing else. No INTERNET, no location, no storage. " +
                "Check it yourself: aapt dump permissions on the installed APK.",
            style = Type.body115,
            color = Ink.textMuted,
        )

        Box(Modifier.height(Space.s3))
        Kicker("Backup")
        Box(Modifier.height(Space.s1))
        ActionRow(
            title = "Home server",
            subtitle = "Not paired — the app works fully without it",
            onClick = onBackup,
        )
        Footnote(
            "A home-server copy would be paired once with a one-time code, hold a " +
                "credential in this phone's Keystore that you can revoke, and never delete " +
                "an original from this phone because a copy exists elsewhere.",
        )

        Box(Modifier.height(Space.s6))
        Hairline()
        Box(Modifier.height(Space.s4))
        Text(
            "Made by Allen, with love, for you.",
            style = Type.italicColophon,
            color = Ink.textMuted,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
