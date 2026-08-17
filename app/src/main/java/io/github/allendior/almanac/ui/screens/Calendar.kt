package io.github.allendior.almanac.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.allendior.almanac.domain.PortraitEntry
import io.github.allendior.almanac.ui.AlmanacUiState
import io.github.allendior.almanac.ui.Fmt
import io.github.allendior.almanac.ui.components.Footnote
import io.github.allendior.almanac.ui.components.Hairline
import io.github.allendior.almanac.ui.components.Kicker
import io.github.allendior.almanac.ui.components.Lucide
import io.github.allendior.almanac.ui.components.PortraitImage
import io.github.allendior.almanac.ui.components.accessibleClick
import io.github.allendior.almanac.ui.theme.Ink
import io.github.allendior.almanac.ui.theme.Space
import io.github.allendior.almanac.ui.theme.Type
import java.io.File
import java.time.LocalDate
import java.time.YearMonth

/**
 * The primary archive view.
 *
 * An empty square is empty. It carries no colour, no marker and no tooltip, because
 * the app keeps no record of what the owner intended to do on a day they did not
 * record. Only what happened is shown. A day holding more than one portrait carries a
 * small count instead — still just a fact, not a judgement.
 */
@Composable
fun CalendarScreen(
    state: AlmanacUiState,
    thumbnailOf: (PortraitEntry) -> File,
    onMonthChange: (YearMonth) -> Unit,
    onOpenDay: (String) -> Unit,
) {
    val month = state.calendarMonth
    val byDay = remember(state.entries) { state.entries.groupBy { it.dayId } }
    val recordedThisMonth = byDay.keys.count { it.startsWith(monthPrefix(month)) }

    ScreenColumn(gap = 14.dp) {
        Kicker("Archive")
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MonthButton(Lucide.ChevronLeft, "Previous month") { onMonthChange(month.minusMonths(1)) }
            Text(
                Fmt.monthYear(month.year, month.monthValue),
                style = Type.title25,
                color = Ink.text,
            )
            MonthButton(Lucide.ChevronRight, "Next month") { onMonthChange(month.plusMonths(1)) }
        }
        Text(
            "${Fmt.days(recordedThisMonth)} recorded",
            style = Type.figures(12.5f),
            color = Ink.textMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Hairline()

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { head ->
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Kicker(head, letterSpacing = 0.08f)
                }
            }
        }

        val weeks = remember(month) { weeksOf(month) }
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            weeks.forEach { week ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    week.forEach { date ->
                        Box(Modifier.weight(1f)) {
                            if (date == null) {
                                Box(Modifier.fillMaxWidth().aspectRatio(1f))
                            } else {
                                DayCell(
                                    date = date,
                                    dayEntries = byDay[date.toString()].orEmpty(),
                                    isToday = date == state.today,
                                    thumbnailOf = thumbnailOf,
                                    onOpenDay = onOpenDay,
                                )
                            }
                        }
                    }
                }
            }
        }

        Box(Modifier.height(Space.s1))
        Footnote(
            "Filled squares are days with a portrait. Empty squares are just empty — " +
                "the app keeps no record of what you meant to do.",
        )
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    dayEntries: List<PortraitEntry>,
    isToday: Boolean,
    thumbnailOf: (PortraitEntry) -> File,
    onOpenDay: (String) -> Unit,
) {
    // Entries arrive newest-first, so the first is the one shown as this day's face.
    val entry = dayEntries.firstOrNull()
    val label = when {
        dayEntries.size > 1 -> "${Fmt.short(date)}, ${dayEntries.size} portraits"
        entry != null -> "${Fmt.short(date)}, recorded"
        else -> "${Fmt.short(date)}, no portrait"
    }
    val border = when {
        entry != null -> Ink.divider
        isToday -> Ink.accent
        else -> Color.Transparent
    }
    Box(
        Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .border(1.dp, border)
            .then(
                if (entry != null) {
                    Modifier.accessibleClick(onClick = { onOpenDay(date.toString()) }, label = label)
                } else {
                    Modifier.clearAndSetSemantics { contentDescription = label }
                },
            ),
    ) {
        if (entry != null) {
            PortraitImage(
                file = thumbnailOf(entry),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                placeholderMark = 16.dp,
            )
        }
        Text(
            date.dayOfMonth.toString(),
            style = Type.record(11f).copy(
                shadow = if (entry != null) {
                    androidx.compose.ui.graphics.Shadow(
                        color = Color.Black.copy(alpha = 0.5f),
                        offset = androidx.compose.ui.geometry.Offset(0f, 1f),
                        blurRadius = 3f,
                    )
                } else {
                    null
                },
            ),
            color = when {
                entry != null -> Color.White.copy(alpha = 0.92f)
                isToday -> Ink.accent
                else -> Ink.textMuted
            },
            modifier = Modifier.align(Alignment.Center),
        )
        if (dayEntries.size > 1) {
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(2.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color.Black.copy(alpha = 0.45f))
                    .padding(horizontal = 3.dp, vertical = 1.dp),
            ) {
                Text(
                    dayEntries.size.toString(),
                    style = Type.record(8f),
                    color = Color.White.copy(alpha = 0.95f),
                )
            }
        }
    }
}

@Composable
private fun MonthButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    Box(
        Modifier
            .size(48.dp)
            .accessibleClick(onClick = onClick, label = label),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = Ink.text.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
    }
}

private fun monthPrefix(month: YearMonth): String =
    "%04d-%02d".format(month.year, month.monthValue)

/** Weeks running Monday to Sunday, with nulls for the days either side of the month. */
private fun weeksOf(month: YearMonth): List<List<LocalDate?>> {
    val first = month.atDay(1)
    val lead = (first.dayOfWeek.value - 1)
    val length = month.lengthOfMonth()
    val cells = buildList<LocalDate?> {
        repeat(lead) { add(null) }
        for (day in 1..length) add(month.atDay(day))
        while (size % 7 != 0) add(null)
    }
    return cells.chunked(7)
}
