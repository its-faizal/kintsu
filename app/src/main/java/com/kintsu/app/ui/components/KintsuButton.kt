package com.kintsu.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.kintsu.app.ui.theme.KintsuTheme

enum class KintsuButtonVariant {
    PRIMARY,
    SECONDARY,
    ACCENT_SOFT,
    TEXT
}

@Composable
fun KintsuButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: KintsuButtonVariant = KintsuButtonVariant.PRIMARY,
    enabled: Boolean = true,
    leadingIcon: (@Composable (Color) -> Unit)? = null,
    shape: Shape = KintsuTheme.shapes.medium
) {
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography
    val motion = KintsuTheme.motion

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val targetScale = if (motion.fastMs == 0) 1.0f else if (isPressed) 0.97f else 1.0f
    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 500f),
        label = "btnScale"
    )

    val (bg, textColor) = when (variant) {
        KintsuButtonVariant.PRIMARY -> {
            if (enabled) colors.accent to colors.background else colors.surfaceElevated to colors.textTertiary
        }
        KintsuButtonVariant.SECONDARY -> {
            if (enabled) colors.surfaceElevated to colors.textPrimary else colors.surface to colors.textTertiary
        }
        KintsuButtonVariant.ACCENT_SOFT -> {
            if (enabled) colors.accentSoft to colors.accent else colors.surface to colors.textTertiary
        }
        KintsuButtonVariant.TEXT -> {
            Color.Transparent to if (enabled) colors.accent else colors.textTertiary
        }
    }

    Box(
        modifier = modifier
            .scale(scale)
            .defaultMinSize(minHeight = 48.dp)
            .clip(shape)
            .background(bg)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .semantics {
                role = Role.Button
                contentDescription = text
            }
            .padding(horizontal = spacing.l, vertical = spacing.s),
        contentAlignment = Alignment.Center
    ) {
        val cleanText = if (leadingIcon != null && text.startsWith("+ ")) {
            text.removePrefix("+ ").trimStart()
        } else {
            text
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (leadingIcon != null) {
                leadingIcon(textColor)
                Spacer(modifier = Modifier.width(spacing.xs))
            }
            Text(
                text = cleanText,
                style = typography.button,
                color = textColor
            )
        }
    }
}

@Composable
fun KintsuIconButton(
    icon: @Composable (Color) -> Unit,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = KintsuTheme.colors.textPrimary,
    backgroundColor: Color = Color.Transparent
) {
    val motion = KintsuTheme.motion
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val targetScale = if (motion.fastMs == 0) 1.0f else if (isPressed) 0.92f else 1.0f
    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 600f),
        label = "iconBtnScale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .size(48.dp)
            .clip(KintsuTheme.shapes.full)
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .semantics {
                role = Role.Button
                this.contentDescription = contentDescription
            },
        contentAlignment = Alignment.Center
    ) {
        icon(tint)
    }
}
