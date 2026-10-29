package com.kintsu.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kintsu.app.ui.theme.KintsuTheme

@Composable
fun KintsuEmptyState(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    icon: (@Composable (Color) -> Unit)? = null,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = spacing.xxl, horizontal = spacing.l),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (icon != null) {
            icon(colors.textTertiary)
            Spacer(modifier = Modifier.height(spacing.m))
        }

        Text(
            text = title,
            style = typography.sectionHeader,
            color = colors.textPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(spacing.xs))

        Text(
            text = description,
            style = typography.secondary,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = spacing.m)
        )

        if (!actionText.isNullOrBlank() && onActionClick != null) {
            Spacer(modifier = Modifier.height(spacing.l))
            KintsuButton(
                text = actionText,
                onClick = onActionClick,
                variant = KintsuButtonVariant.ACCENT_SOFT
            )
        }
    }
}
