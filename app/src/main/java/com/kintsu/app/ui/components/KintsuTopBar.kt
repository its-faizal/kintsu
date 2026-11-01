package com.kintsu.app.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
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
    onCustomizationClick: (() -> Unit)? = null,
    showBackButton: Boolean = false,
    onBackClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography

    // Apple-style spring animations for smooth entrance
    val translateY by animateFloatAsState(0f, animationSpec = spring(dampingRatio = 0.9f, stiffness = 200f))
    val alpha by animateFloatAsState(1f, animationSpec = spring(dampingRatio = 0.9f, stiffness = 200f))

    // Interaction sources for press/ripple states
    val profileInteraction = remember { MutableInteractionSource() }
    val shopInteraction = remember { MutableInteractionSource() }
    val customizationInteraction = remember { MutableInteractionSource() }
    val settingsInteraction = remember { MutableInteractionSource() }
    val backInteraction = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.surfaceTranslucent)
            .statusBarsPadding()
            .padding(horizontal = spacing.xl, vertical = spacing.m)
            .graphicsLayer {
                translationY = translateY
                this.alpha = alpha
            }
    ) {
        if (onProfileClick != null) {
            // HOME SCREEN: Profile left, actions right, greeting below
            Column(modifier = Modifier.fillMaxWidth()) {
                // Top Row: Profile | Coins/Shop/Customization
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Profile Avatar (LEFT)
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(colors.surfaceElevated)
                            .border(1.dp, colors.border, CircleShape)
                            .clickable(
                                interactionSource = profileInteraction,
                                indication = null,
                                onClick = onProfileClick
                            )
                            .semantics {
                                role = Role.Button
                                contentDescription = "Profile and Settings"
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        IconUser(color = colors.textPrimary, size = 20.dp)
                    }

                    // Right Action Cluster: Coins + Shop + Customization
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(spacing.xs)
                    ) {
                        // Coins Balance Pill
                        if (coinsBalance > 0) {
                            Box(
                                modifier = Modifier
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(19.dp))
                                    .background(colors.goldSeam.copy(alpha = 0.12f))
                                    .border(1.dp, colors.goldSeam.copy(alpha = 0.4f), RoundedCornerShape(19.dp))
                                    .padding(horizontal = spacing.m)
                                    .semantics { contentDescription = "$coinsBalance Kintsugi Gold" }
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
                                            fontSize = 13.sp,
                                            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                                        )
                                    )
                                }
                            }
                        }

                        // Sanctuary Shop
                        if (onShopClick != null) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(colors.goldSeam.copy(alpha = 0.12f))
                                    .border(1.dp, colors.goldSeam.copy(alpha = 0.4f), CircleShape)
                                    .clickable(
                                        interactionSource = shopInteraction,
                                        indication = null,
                                        onClick = onShopClick
                                    )
                                    .semantics {
                                        role = Role.Button
                                        contentDescription = "Sanctuary Shop"
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                IconShop(color = colors.goldSeam, size = 16.dp)
                            }
                        }

                        // Customization
                        if (onCustomizationClick != null) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(colors.accentSoft.copy(alpha = 0.15f))
                                    .border(1.dp, colors.accent.copy(alpha = 0.4f), CircleShape)
                                    .clickable(
                                        interactionSource = customizationInteraction,
                                        indication = null,
                                        onClick = onCustomizationClick
                                    )
                                    .semantics {
                                        role = Role.Button
                                        contentDescription = "Customization - Themes, Icons, Widgets"
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                IconCustomizer(color = colors.accent, size = 18.dp)
                            }
                        }
                    }
                }

                // Gap before greeting
                Spacer(modifier = Modifier.height(spacing.m))

                // Greeting & Title Stack
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    if (!subtitle.isNullOrBlank()) {
                        Text(
                            text = subtitle,
                            style = typography.caption.copy(
                                color = colors.textSecondary,
                                letterSpacing = 0.2.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(spacing.xxs))
                    }
                    Text(
                        text = title,
                        style = typography.screenTitle.copy(
                            color = colors.textPrimary,
                            letterSpacing = -0.5.sp
                        )
                    )
                }
            }
        } else {
            // SUB SCREENS: Back button left, title centered, settings right (when no back)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                // Optional Back Button
                if (showBackButton && onBackClick != null) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(colors.surfaceElevated.copy(alpha = 0.7f))
                            .clickable(
                                interactionSource = backInteraction,
                                indication = null,
                                onClick = onBackClick
                            )
                            .semantics {
                                role = Role.Button
                                contentDescription = "Back"
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        IconChevronLeft(color = colors.textPrimary, size = 20.dp)
                    }
                    Spacer(modifier = Modifier.width(spacing.xs))
                }

                // Title Column - centered with back, left without
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = if (showBackButton) Alignment.CenterHorizontally else Alignment.Start
                ) {
                    if (!subtitle.isNullOrBlank()) {
                        Text(
                            text = subtitle,
                            style = typography.caption.copy(
                                color = colors.textSecondary,
                                letterSpacing = 0.2.sp
                            )
                        )
                    }
                    Text(
                        text = title,
                        style = typography.screenTitle.copy(
                            color = colors.textPrimary,
                            letterSpacing = -0.5.sp
                        ),
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }

                // Settings Button (only when no back button)
                if (onSettingsClick != null && !showBackButton) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(colors.surfaceElevated.copy(alpha = 0.7f))
                            .clickable(
                                interactionSource = settingsInteraction,
                                indication = null,
                                onClick = onSettingsClick
                            )
                            .semantics {
                                role = Role.Button
                                contentDescription = "Settings"
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        IconSettings(color = colors.textPrimary, size = 20.dp)
                    }
                } else if (!showBackButton) {
                    // Spacer for alignment when no settings/back
                    Spacer(modifier = Modifier.width(44.dp))
                }
            }
        }
    }
}

// Chevron Left Icon for back navigation
@Composable
private fun IconChevronLeft(
    color: Color,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 24.dp
) {
    Icon(
        imageVector = Icons.Default.ArrowBack,
        contentDescription = "Back",
        modifier = modifier.size(size),
        tint = color
    )
}