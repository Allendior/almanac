package com.almanac.portrait.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.almanac.portrait.ui.theme.Ink
import com.almanac.portrait.ui.theme.Space
import com.almanac.portrait.ui.theme.Type

data class NavItem(val label: String, val icon: ImageVector)

/**
 * Five destinations on a hairline. Hidden entirely on Capture, Review and Lock —
 * those screens are one task each and offer no wandering.
 *
 * The active item gets a small accent underline that grows in and a colour crossfade,
 * both on the system's one animation budget (180ms, no bounce) — the same restraint
 * the Archive switch already uses. Nothing here parallaxes, springs, or overshoots.
 */
@Composable
fun BottomNav(
    items: List<NavItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
) {
    Column(Modifier.fillMaxWidth().background(Ink.bg)) {
        Hairline()
        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            items.forEachIndexed { index, item ->
                val active = index == selectedIndex
                val tint by animateColorAsState(
                    targetValue = if (active) Ink.accent else Ink.textMuted,
                    animationSpec = tween(180),
                    label = "navTint",
                )
                val indicatorWidth by animateDpAsState(
                    targetValue = if (active) 20.dp else 0.dp,
                    animationSpec = tween(180),
                    label = "navIndicator",
                )
                Column(
                    Modifier
                        .weight(1f)
                        .heightIn(min = 56.dp)
                        .accessibleClick(
                            onClick = { onSelect(index) },
                            label = item.label,
                            role = Role.Tab,
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(item.icon, contentDescription = null, tint = tint, modifier = Modifier.size(19.dp))
                    Box(Modifier.height(Space.s1))
                    Text(item.label.uppercase(), style = Type.kickerNav, color = tint)
                    Box(Modifier.height(4.dp))
                    Box(
                        Modifier
                            .height(2.dp)
                            .width(indicatorWidth)
                            .background(Ink.accent),
                    )
                }
            }
        }
    }
}
