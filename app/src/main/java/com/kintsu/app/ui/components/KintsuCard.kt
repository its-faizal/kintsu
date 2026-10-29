package com.kintsu.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.kintsu.app.ui.theme.KintsuTheme

@Composable
fun KintsuCard(
    modifier: Modifier = Modifier,
    shape: Shape = KintsuTheme.shapes.large,
    backgroundColor: Color = KintsuTheme.colors.surface,
    borderColor: Color? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = KintsuTheme.colors
    val elevation = KintsuTheme.elevation

    val borderMod = if (borderColor != null) {
        Modifier.border(width = 0.8.dp, color = borderColor, shape = shape)
    } else {
        Modifier.border(width = 0.5.dp, color = colors.border.copy(alpha = 0.5f), shape = shape)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = elevation.low, shape = shape, clip = false)
            .clip(shape)
            .background(backgroundColor)
            .then(borderMod),
        content = content
    )
}

@Composable
fun KintsuGroupedSection(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    KintsuCard(
        modifier = modifier,
        shape = KintsuTheme.shapes.large,
        backgroundColor = KintsuTheme.colors.surface,
        content = content
    )
}

@Composable
fun KintsuDivider(
    modifier: Modifier = Modifier,
    indentStart: Boolean = true
) {
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = if (indentStart) spacing.l else 0.dp)
            .height(0.6.dp)
            .background(colors.border.copy(alpha = 0.45f))
    )
}
