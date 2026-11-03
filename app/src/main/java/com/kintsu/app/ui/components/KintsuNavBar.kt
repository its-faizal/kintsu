package com.kintsu.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
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

enum class KintsuNavTab(val title: String) {
    HABITS("Habits"),
    TASKS("Tasks"),
    HOME("Home"),
    GOALS("Goals"),
    PROGRESS("Progress")
}

@Composable
fun KintsuNavBar(
    selectedTab: KintsuNavTab,
    onTabSelected: (KintsuNavTab) -> Unit,
    onTabReselected: (KintsuNavTab) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val motion = KintsuTheme.motion
    val animDuration = if (motion.fastMs == 0) 0 else 200

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.surfaceTranslucent)
            .navigationBarsPadding()
            .padding(vertical = spacing.xxs, horizontal = spacing.s),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 640.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            KintsuNavTab.values().forEach { tab ->
                val isSelected = tab == selectedTab
                val isHome = tab == KintsuNavTab.HOME
                val interactionSource = remember { MutableInteractionSource() }
                val isFocused by interactionSource.collectIsFocusedAsState()

                val targetIconColor = when {
                    isSelected -> colors.accent
                    isSelected -> colors.accent
                    else -> colors.textTertiary
                }

                val iconColor by animateColorAsState(
                    targetValue = targetIconColor,
                    animationSpec = tween(durationMillis = animDuration),
                    label = "tabIconColor"
                )
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) colors.accentText else colors.textSecondary,
                    animationSpec = tween(durationMillis = animDuration),
                    label = "tabTextColor"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 48.dp)
                        .padding(horizontal = 2.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .then(
                            if (isSelected) {
                                Modifier.background(colors.accentSoft.copy(alpha = 0.5f))
                            } else {
                                Modifier
                            }
                        )
                        .then(
                            if (isFocused) {
                                Modifier.border(1.5.dp, colors.accentText, RoundedCornerShape(12.dp))
                            } else {
                                Modifier
                            }
                        )
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = {
                                if (isSelected) {
                                    onTabReselected(tab)
                                } else {
                                    onTabSelected(tab)
                                }
                            }
                        )
                        .semantics {
                            role = Role.Tab
                            selected = isSelected
                            contentDescription = "${tab.title} Tab"
                        }
                        .padding(vertical = spacing.xs),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier.size(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            when (tab) {
                                KintsuNavTab.HOME -> IconHome(
                                    color = iconColor,
                                    size = if (isSelected) 23.dp else 21.dp
                                )
                                KintsuNavTab.HABITS -> IconHabit(color = iconColor, size = 19.dp)
                                KintsuNavTab.TASKS -> IconTask(color = iconColor, size = 19.dp)
                                KintsuNavTab.GOALS -> IconGoal(color = iconColor, size = 19.dp)
                                KintsuNavTab.PROGRESS -> IconProgress(color = iconColor, size = 19.dp)
                            }
                        }
                        Text(
                            text = tab.title,
                            style = KintsuTheme.typography.caption,
                            color = textColor,
                            modifier = Modifier.padding(top = 2.dp),
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
