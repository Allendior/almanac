package com.almanac.portrait.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.almanac.portrait.domain.PortraitEntry
import com.almanac.portrait.ui.AlmanacUiState
import com.almanac.portrait.ui.Fmt
import com.almanac.portrait.ui.components.ButtonTone
import com.almanac.portrait.ui.components.ClassicalButton
import com.almanac.portrait.ui.components.Hairline
import com.almanac.portrait.ui.components.Kicker
import com.almanac.portrait.ui.theme.Ink
import com.almanac.portrait.ui.theme.Space
import com.almanac.portrait.ui.theme.Type
import java.io.File
import java.time.LocalDate

private sealed interface TimelineRow {
    data class MonthHeading(val key: String, val label: String, val count: Int) : TimelineRow
    data class Portraits(val key: String, val entries: List<PortraitEntry>) : TimelineRow
}

/**
 * The whole archive, newest month first, in a lazily-composed list — ten years of
 * daily portraits is several thousand rows, so nothing off screen is built or decoded.
 */
@Composable
fun TimelineScreen(
    state: AlmanacUiState,
    thumbnailOf: (PortraitEntry) -> File,
    onOpenEntry: (String) -> Unit,
    onGoToToday: () -> Unit,
) {
    if (state.loading) {
        Box(Modifier.fillMaxSize().background(Ink.bg))
        return
    }
    if (state.entries.isEmpty()) {
        EmptyTimeline(onGoToToday)
        return
    }

    val rows = remember(state.entries) { buildRows(state.entries) }
    val span = remember(state.entries) {
        val oldest = state.entries.last().date
        "${Fmt.month(oldest)} ${oldest.year} to today"
    }

    LazyColumn(
        Modifier
            .fillMaxSize()
            .background(Ink.bg),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 24.dp, end = 24.dp, top = 26.dp, bottom = 32.dp,
        ),
    ) {
        item(key = "header") {
            Column {
                Kicker("Archive")
                Box(Modifier.height(Space.s2))
                Text("Timeline", style = Type.title27, color = Ink.text)
                Box(Modifier.height(Space.s1))
                Text(
                    "${Fmt.portraits(state.entries.size)} · $span",
                    style = Type.body125,
                    color = Ink.textMuted,
                )
                Box(Modifier.height(Space.s3))
                Hairline()
            }
        }

        items(rows, key = { it.key() }) { row ->
            when (row) {
                is TimelineRow.MonthHeading -> {
                    Column(Modifier.padding(top = 26.dp)) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom,
                        ) {
                            Text(row.label, style = Type.title19, color = Ink.text)
                            Text(Fmt.grouped(row.count), style = Type.figures(11f), color = Ink.textMuted)
                        }
                        Box(Modifier.height(Space.s1))
                        Hairline()
                        Box(Modifier.height(Space.s2))
                    }
                }

                is TimelineRow.Portraits -> {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(bottom = 9.dp),
                        horizontalArrangement = Arrangement.spacedBy(9.dp),
                    ) {
                        row.entries.forEach { entry ->
                            Box(Modifier.weight(1f)) {
                                DayThumbnail(entry, thumbnailOf(entry), onOpenEntry)
                            }
                        }
                        repeat(3 - row.entries.size) { Box(Modifier.weight(1f)) }
                    }
                }
            }
        }
    }
}

private fun TimelineRow.key(): String = when (this) {
    is TimelineRow.MonthHeading -> "h-$key"
    is TimelineRow.Portraits -> "p-$key"
}

private fun buildRows(entries: List<PortraitEntry>): List<TimelineRow> {
    val rows = mutableListOf<TimelineRow>()
    entries
        .groupBy { it.dayId.substring(0, 7) }
        .toSortedMap(compareByDescending { it })
        .forEach { (month, monthEntries) ->
            val first = LocalDate.parse("$month-01")
            rows += TimelineRow.MonthHeading(
                key = month,
                label = "${Fmt.month(first)} ${first.year}",
                count = monthEntries.size,
            )
            monthEntries.chunked(3).forEachIndexed { index, chunk ->
                rows += TimelineRow.Portraits(key = "$month-$index", entries = chunk)
            }
        }
    return rows
}

@Composable
private fun EmptyTimeline(onGoToToday: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Ink.bg)
            .padding(horizontal = 40.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Nothing recorded yet", style = Type.title20, color = Ink.textMuted)
        Box(Modifier.height(Space.s2))
        Text(
            "The timeline fills itself one day at a time.",
            style = Type.body13.copy(textAlign = TextAlign.Center),
            color = Ink.textMuted,
        )
        Box(Modifier.height(Space.s6))
        ClassicalButton(
            label = "Take today's portrait",
            onClick = onGoToToday,
            tone = ButtonTone.Primary,
            height = 52.dp,
            fontSize = 15.sp,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
