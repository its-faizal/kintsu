package com.kintsu.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.sp
import com.kintsu.app.ui.theme.KintsuTheme

@Composable
fun KintsuTopBar(
    title: String,
    subtitle: String? = null,
    onSettingsClick: (() -> Unit)? = null,
    onProfileClick: (() -> Unit)? = null,
    profileInitial: String = "S",
    coinsBalance: Int = 0,
    onShopClick: (() -> Unit)? = null,
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
        if (onProfileClick != null) {
            // Home screen top bar layout: Profile avatar and Shop & Coins at TOP-LEFT
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(spacing.s)
                    ) {
                        // Profile Avatar Button (48dp touch target)
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(colors.surfaceElevated)
                                .border(1.dp, colors.border, CircleShape)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = onProfileClick
                                )
                                .semantics {
                                    role = Role.Button
                                    contentDescription = "Profile and Settings"
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            IconUser(
                                color = colors.textPrimary,
                                size = 20.dp
                            )
                        }

                        // Kintsugi Coins & Sanctuary Shop Pill (48dp accessible height)
                        if (onShopClick != null) {
                            Box(
                                modifier = Modifier
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(19.dp))
                                    .background(colors.goldSeam.copy(alpha = 0.12f))
                                    .border(1.dp, colors.goldSeam.copy(alpha = 0.45f), RoundedCornerShape(19.dp))
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = onShopClick
                                    )
                                    .padding(horizontal = spacing.m)
                                    .semantics {
                                        role = Role.Button
                                        contentDescription = "Sanctuary Shop, Balance $coinsBalance gold coins"
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(spacing.xs)
                                ) {
                                    IconGoldSeam(color = colors.goldSeam, size = 15.dp)
                                    Text(
                                        text = "$coinsBalance",
                                        style = typography.caption.copy(
                                            color = colors.goldSeam,
                                            fontSize = 13.sp
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    IconShop(color = colors.goldSeam, size = 13.dp)
                                }
                            }
                        }
                    }

                    // Optional right side status (can be empty for true minimalism)
                }

                Spacer(modifier = Modifier.height(spacing.s))

                // Date & Greeting beneath profile
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
        } else {
            // General Screen Top Bar (Tasks, Habits, Goals, Progress)
            // Settings button removed from other menus per instruction
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

                if (onSettingsClick != null) {
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
                                contentDescription = "Settings"
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
    }
}
