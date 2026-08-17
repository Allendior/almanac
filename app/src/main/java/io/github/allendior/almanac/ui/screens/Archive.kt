package io.github.allendior.almanac.ui.screens

import android.app.TimePickerDialog
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.github.allendior.almanac.ui.AlmanacUiState
import io.github.allendior.almanac.ui.Fmt
import io.github.allendior.almanac.ui.components.ActionRow
import io.github.allendior.almanac.ui.components.ClassicalButton
import io.github.allendior.almanac.ui.components.ClassicalSwitch
import io.github.allendior.almanac.ui.components.Footnote
import io.github.allendior.almanac.ui.components.Hairline
import io.github.allendior.almanac.ui.components.Kicker
import io.github.allendior.almanac.ui.components.LabelValueRow
import io.github.allendior.almanac.ui.components.Lucide
import io.github.allendior.almanac.ui.theme.Ink
import io.github.allendior.almanac.ui.theme.Space
import io.github.allendior.almanac.ui.theme.Type

@Composable
fun ArchiveScreen(
    state: AlmanacUiState,
    onExport: () -> Unit,
    onImport: () -> Unit,
    onToggleLock: (Boolean) -> Unit,
    onCycleGuide: () -> Unit,
    onBackup: () -> Unit,
    onOpenTips: () -> Unit,
    onToggleNotifications: (Boolean) -> Unit,
    onSetNotificationMinute: (Int) -> Unit,
    onOpenSponsors: () -> Unit,
) {
    val context = LocalContext.current
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
        Kicker("Reminder")
        Box(Modifier.height(Space.s1))

        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text("Daily reminder", style = Type.body14, color = Ink.text)
                Text(
                    "Once a day, only if today has no portrait yet",
                    style = Type.body115,
                    color = Ink.textMuted,
                )
            }
            ClassicalSwitch(
                checked = state.settings.notificationsEnabled,
                onCheckedChange = onToggleNotifications,
                label = "Daily reminder",
            )
        }

        if (state.settings.notificationsEnabled) {
            Hairline()
            Row(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Around", style = Type.body14, color = Ink.text)
                    Text(
                        Fmt.minuteOfDay(state.settings.notificationMinuteOfDay),
                        style = Type.body115,
                        color = Ink.textMuted,
                    )
                }
                ClassicalButton(
                    label = "Change",
                    onClick = {
                        val minute = state.settings.notificationMinuteOfDay
                        TimePickerDialog(
                            context,
                            { _, hour, minuteOfHour -> onSetNotificationMinute(hour * 60 + minuteOfHour) },
                            minute / 60,
                            minute % 60,
                            true,
                        ).show()
                    },
                    height = 44.dp,
                    contentDescription = "Change the reminder time, currently ${Fmt.minuteOfDay(state.settings.notificationMinuteOfDay)}",
                )
            }
        }
        Hairline()

        Box(Modifier.height(Space.s2))
        Kicker("Taking a portrait")
        Box(Modifier.height(Space.s1))
        ActionRow(
            title = "Tips for a more comparable portrait",
            subtitle = "Same spot, same light, same rough time — entirely optional",
            onClick = onOpenTips,
            icon = Lucide.Zap,
        )

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
        Text("android.permission.POST_NOTIFICATIONS", style = Type.record(11.5f), color = Ink.text)
        Text(
            "for the optional daily reminder, on by default and easy to turn off above",
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

        Box(Modifier.height(Space.s3))
        Kicker("Support")
        Box(Modifier.height(Space.s1))
        ActionRow(
            title = "Support this app",
            subtitle = "GitHub Sponsors, entirely optional — opens your browser, nothing more",
            onClick = onOpenSponsors,
            icon = Lucide.Heart,
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
