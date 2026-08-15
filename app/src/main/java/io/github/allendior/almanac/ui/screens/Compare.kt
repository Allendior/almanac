package io.github.allendior.almanac.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.allendior.almanac.domain.Interval
import io.github.allendior.almanac.domain.PortraitEntry
import io.github.allendior.almanac.ui.AlmanacUiState
import io.github.allendior.almanac.ui.ComparePane
import io.github.allendior.almanac.ui.CompareTab
import io.github.allendior.almanac.ui.Fmt
import io.github.allendior.almanac.ui.components.Footnote
import io.github.allendior.almanac.ui.components.Kicker
import io.github.allendior.almanac.ui.components.Lucide
import io.github.allendior.almanac.ui.components.Plate
import io.github.allendior.almanac.ui.components.PortraitImage
import io.github.allendior.almanac.ui.components.SelectChip
import io.github.allendior.almanac.ui.components.accessibleClick
import io.github.allendior.almanac.ui.theme.Ink
import io.github.allendior.almanac.ui.theme.Space
import io.github.allendior.almanac.ui.theme.Type
import java.io.File

@Composable
fun CompareScreen(
    state: AlmanacUiState,
    originalOf: (PortraitEntry) -> File,
    thumbnailOf: (PortraitEntry) -> File,
    onTab: (CompareTab) -> Unit,
    onOpenPicker: (ComparePane) -> Unit,
    onChooseDate: (ComparePane, String) -> Unit,
    onOpenEntry: (String) -> Unit,
) {
    ScreenColumn(gap = 14.dp) {
        Kicker("Archive")
        Text("Compare", style = Type.title27, color = Ink.text)

        Row(horizontalArrangement = Arrangement.spacedBy(Space.s2)) {
            SelectChip("Two dates", state.compare.tab == CompareTab.TWO_DATES, { onTab(CompareTab.TWO_DATES) }, height = 42.dp)
            SelectChip("By year", state.compare.tab == CompareTab.BY_YEAR, { onTab(CompareTab.BY_YEAR) }, height = 42.dp)
        }

        if (state.loading) {
            return@ScreenColumn
        }

        if (state.entries.isEmpty()) {
            Box(Modifier.height(Space.s6))
            Text(
                "There is nothing to compare yet. Two portraits, taken on days of your " +
                    "choosing, are all this screen ever needs.",
                style = Type.body125.copy(textAlign = TextAlign.Justify),
                color = Ink.textMuted,
            )
            return@ScreenColumn
        }

        when (state.compare.tab) {
            CompareTab.TWO_DATES -> TwoDates(state, originalOf, thumbnailOf, onOpenPicker, onChooseDate)
            CompareTab.BY_YEAR -> ByYear(state, thumbnailOf, onOpenEntry)
        }
    }
}

@Composable
private fun TwoDates(
    state: AlmanacUiState,
    originalOf: (PortraitEntry) -> File,
    thumbnailOf: (PortraitEntry) -> File,
    onOpenPicker: (ComparePane) -> Unit,
    onChooseDate: (ComparePane, String) -> Unit,
) {
    // Defaults tell the story on their own: the first portrait against the latest one.
    val left = state.entry(state.compare.leftDayId) ?: state.oldest
    val right = state.entry(state.compare.rightDayId) ?: state.mostRecent

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Space.s2)) {
        ComparePlate(left, ComparePane.LEFT, originalOf, thumbnailOf, onOpenPicker, Modifier.weight(1f))
        ComparePlate(right, ComparePane.RIGHT, originalOf, thumbnailOf, onOpenPicker, Modifier.weight(1f))
    }

    if (left != null && right != null) {
        Box(Modifier.height(Space.s2))
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(Interval.between(left.date, right.date), style = Type.title20, color = Ink.text)
            Box(Modifier.height(2.dp))
            Kicker("Apart")
        }
    }

    val picking = state.compare.picking
    if (picking != null) {
        Box(Modifier.height(Space.s2))
        Kicker(
            if (picking == ComparePane.LEFT) "Choose a date for the left portrait"
            else "Choose a date for the right portrait",
        )
        Box(Modifier.height(Space.s2))
        YearChips(state.entries) { dayId -> onChooseDate(picking, dayId) }
    }
}

/** One portrait per year — the first recorded in it — as a way in to that year. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun YearChips(entries: List<PortraitEntry>, onChoose: (String) -> Unit) {
    val firstOfYear = remember(entries) { firstEntryPerYear(entries) }
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(Space.s2),
        verticalArrangement = Arrangement.spacedBy(Space.s2),
    ) {
        firstOfYear.forEach { entry ->
            SelectChip(
                label = "${entry.date.year} · ${Fmt.short(entry.date)}",
                selected = false,
                onClick = { onChoose(entry.dayId) },
                height = 44.dp,
            )
        }
    }
}

@Composable
private fun ComparePlate(
    entry: PortraitEntry?,
    pane: ComparePane,
    originalOf: (PortraitEntry) -> File,
    thumbnailOf: (PortraitEntry) -> File,
    onOpenPicker: (ComparePane) -> Unit,
    modifier: Modifier,
) {
    Column(modifier) {
        Plate(matWidth = 5.dp, modifier = Modifier.fillMaxWidth()) {
            PortraitImage(
                file = entry?.let(originalOf),
                contentDescription = entry?.let { "Portrait of ${Fmt.long(it.date)}" } ?: "No portrait chosen",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(3f / 4f),
                placeholderMark = 28.dp,
                thumbnailFile = entry?.let(thumbnailOf),
            )
        }
        Box(Modifier.height(Space.s1))
        Row(
            Modifier
                .fillMaxWidth()
                .height(44.dp)
                .accessibleClick(
                    onClick = { onOpenPicker(pane) },
                    label = "Choose the ${if (pane == ComparePane.LEFT) "left" else "right"} date",
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                entry?.let { Fmt.longNoWeekday(it.date) } ?: "Choose a date",
                style = Type.body125,
                color = Ink.textMuted,
            )
            Box(Modifier.size(4.dp))
            Icon(Lucide.ChevronDown, contentDescription = null, tint = Ink.textMuted, modifier = Modifier.size(14.dp))
        }
    }
}

@Composable
private fun ByYear(
    state: AlmanacUiState,
    thumbnailOf: (PortraitEntry) -> File,
    onOpenEntry: (String) -> Unit,
) {
    val years = remember(state.entries) { firstEntryPerYear(state.entries) }
    val counts = remember(state.entries) { state.entries.groupingBy { it.date.year }.eachCount() }

    years.chunked(2).forEach { pair ->
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Space.s2)) {
            pair.forEach { entry ->
                Column(Modifier.weight(1f)) {
                    Plate(
                        matWidth = 5.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .accessibleClick(
                                onClick = { onOpenEntry(entry.dayId) },
                                label = "${entry.date.year}, open ${Fmt.longNoWeekday(entry.date)}",
                            ),
                    ) {
                        PortraitImage(
                            file = thumbnailOf(entry),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(3f / 4f),
                            placeholderMark = 28.dp,
                        )
                    }
                    Box(Modifier.height(Space.s1))
                    Text(entry.date.year.toString(), style = Type.title17, color = Ink.text)
                    Text(
                        Fmt.days(counts[entry.date.year] ?: 0),
                        style = Type.figures(11.5f),
                        color = Ink.textMuted,
                    )
                }
            }
            repeat(2 - pair.size) { Box(Modifier.weight(1f)) }
        }
        Box(Modifier.height(Space.s3))
    }

    Footnote(
        "One portrait stands for each year — the first you recorded in it. Tap to open that day.",
    )
}

private fun firstEntryPerYear(entries: List<PortraitEntry>): List<PortraitEntry> =
    entries
        .groupBy { it.date.year }
        .toSortedMap()
        .map { (_, yearEntries) -> yearEntries.minBy { it.dayId } }
