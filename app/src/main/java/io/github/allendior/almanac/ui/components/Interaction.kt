package io.github.allendior.almanac.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role

/**
 * Every interactive element goes through here so the accessibility contract is one
 * decision, not a per-call-site habit: a button role, a spoken label, and a disabled
 * state that is actually disabled rather than merely dimmed.
 */
fun Modifier.accessibleClick(
    onClick: () -> Unit,
    enabled: Boolean = true,
    label: String? = null,
    role: Role = Role.Button,
): Modifier = this.clickable(
    enabled = enabled,
    onClickLabel = label,
    role = role,
    onClick = onClick,
)
