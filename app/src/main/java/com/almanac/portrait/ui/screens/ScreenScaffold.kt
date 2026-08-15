package com.almanac.portrait.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.almanac.portrait.ui.theme.Ink

/** The single-column page every destination sits in. No dashboard, no cards grid. */
@Composable
fun ScreenColumn(
    modifier: Modifier = Modifier,
    horizontal: Dp = 24.dp,
    top: Dp = 26.dp,
    bottom: Dp = 32.dp,
    gap: Dp = 16.dp,
    scrollable: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    val scroll = rememberScrollState()
    Column(
        modifier
            .fillMaxSize()
            .background(Ink.bg)
            .then(if (scrollable) Modifier.verticalScroll(scroll) else Modifier)
            .padding(start = horizontal, end = horizontal, top = top, bottom = bottom),
        verticalArrangement = Arrangement.spacedBy(gap),
        content = content,
    )
}
