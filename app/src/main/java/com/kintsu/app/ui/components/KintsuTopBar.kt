package com.kintsu.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.kintsu.app.ui.theme.KintsuTheme

@Composable
fun KintsuTopBar(
    title: String,
    subtitle: String? = null,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.surfaceTranslucent)
            .statusBarsPadding()
            .padding(horizontal = spacing.xl, vertical = spacing.s)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        style = typography.caption,
                        color = colors.textSecondary
                    )
                }
                Text(
                    text = title,
                    style = typography.screenTitle,
                    color = colors.textPrimary
                )
            }

            // 48dp minimum accessible touch target
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(colors.surfaceElevated.copy(alpha = 0.7f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onSettingsClick
                    )
                    .semantics {
                        role = Role.Button
                        contentDescription = "Profile and Settings"
                    },
                contentAlignment = Alignment.Center
            ) {
                IconSettings(
                    color = colors.textPrimary,
                    size = 20.dp
                )
            }
        }
    }
}
