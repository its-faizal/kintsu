package com.kintsu.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kintsu.app.ui.theme.KintsuTheme

@Composable
fun QuickCreationFab(
    onNewTask: () -> Unit,
    onNewHabit: () -> Unit,
    onNewGoal: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography

    var isExpanded by remember { mutableStateOf(false) }
    val rotationAngle by animateFloatAsState(targetValue = if (isExpanded) 45f else 0f, label = "fabRotate")

    Column(
        modifier = modifier.padding(bottom = 12.dp, end = 16.dp),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 1. New Goal Option
                SpeedDialPill(
                    label = "New Goal",
                    iconChar = "◎",
                    onClick = {
                        isExpanded = false
                        onNewGoal()
                    }
                )

                // 2. New Habit Option
                SpeedDialPill(
                    label = "New Habit",
                    iconChar = "✦",
                    onClick = {
                        isExpanded = false
                        onNewHabit()
                    }
                )

                // 3. New Task Option
                SpeedDialPill(
                    label = "New Task",
                    iconChar = "✓",
                    onClick = {
                        isExpanded = false
                        onNewTask()
                    }
                )
            }
        }

        // Main FAB Button
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(colors.goldSeam)
                .clickable { isExpanded = !isExpanded }
                .semantics { role = Role.Button },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "+",
                fontSize = 32.sp,
                fontWeight = FontWeight.Light,
                color = colors.background,
                modifier = Modifier
                    .rotate(rotationAngle)
                    .padding(bottom = 2.dp)
            )
        }
    }
}

@Composable
private fun SpeedDialPill(
    label: String,
    iconChar: String,
    onClick: () -> Unit
) {
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(colors.surfaceElevated)
            .border(1.dp, colors.goldSeam.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = typography.caption.copy(fontSize = 13.sp, fontWeight = FontWeight.SemiBold),
            color = colors.textPrimary
        )

        Spacer(modifier = Modifier.width(spacing.s))

        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(colors.goldSeamSoft),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = iconChar,
                fontSize = 13.sp,
                color = colors.goldSeam,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
