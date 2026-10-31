package com.kintsu.app.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Apple Design translucent frosted liquid glass materials:
 * - Subdued, elevated surface with subtle hairline depth border
 * - Optical hierarchy that preserves legibility across light and dark themes
 */
@Composable
fun KintsuGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    backgroundColor: Color = LocalKintsuColors.current.surface.copy(alpha = 0.78f),
    borderColor: Color = LocalKintsuColors.current.border.copy(alpha = 0.45f),
    borderWidth: Dp = 1.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        backgroundColor,
                        backgroundColor.copy(alpha = (backgroundColor.alpha * 0.92f))
                    )
                )
            )
            .border(
                BorderStroke(
                    borderWidth,
                    Brush.verticalGradient(
                        colors = listOf(
                            borderColor.copy(alpha = 0.6f),
                            borderColor.copy(alpha = 0.25f)
                        )
                    )
                ),
                shape
            ),
        content = content
    )
}
