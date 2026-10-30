package com.kintsu.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.kintsu.app.ui.theme.KintsuTheme

@Composable
fun KintsuFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: (@Composable (Color) -> Unit)? = null
) {
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography
    val motion = KintsuTheme.motion

    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val bg by animateColorAsState(
        targetValue = if (selected) colors.accentSoft else colors.surfaceElevated,
        animationSpec = tween(durationMillis = motion.fastMs),
        label = "chipBg"
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) colors.accentText else colors.textSecondary,
        animationSpec = tween(durationMillis = motion.fastMs),
        label = "chipTextColor"
    )

    Box(
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
            .clip(KintsuTheme.shapes.full)
            .background(bg)
            .border(
                width = if (isFocused) 1.5.dp else 0.8.dp,
                color = if (isFocused) colors.accentText else if (selected) colors.accent.copy(alpha = 0.6f) else colors.border.copy(alpha = 0.7f),
                shape = KintsuTheme.shapes.full
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .semantics {
                role = Role.Tab
                this.selected = selected
                contentDescription = "$label filter"
            }
            .padding(horizontal = spacing.m, vertical = spacing.xs),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                icon(textColor)
                Spacer(modifier = Modifier.width(spacing.xs))
            }
            Text(
                text = label,
                style = typography.caption,
                color = textColor,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}
