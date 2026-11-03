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
import com.kintsu.app.ui.theme.AppTheme

@Composable
fun KintsuTopBar(
    title: String,
    subtitle: String? = null,
    onSettingsClick: (() -> Unit)? = null,
    onProfileClick: (() -> Unit)? = null,
    profileInitial: String = "S",
    profileAvatarId: String = "avatar_botanical",
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
            .background(if (colors.background == Color(0xFF000000) && colors.isDark) Color(0xFF000000) else colors.surfaceTranslucent)
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
                            .background(colors.goldSeam.copy(alpha = 0.12f))
                            .border(1.2.dp, colors.goldSeam.copy(alpha = 0.6f), CircleShape)
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
                        ProfileAvatarVisual(
                            avatarId = profileAvatarId,
                            size = 44.dp,
                            iconSize = 20.dp
                        )
                    }

                    // Right Action Cluster: Combined Shop & Coins Pill + Customize Icon
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(spacing.xs)
                    ) {
                        // Combined Shop & Coins Action Pill
                        if (onShopClick != null) {
                            Box(
                                modifier = Modifier
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(19.dp))
                                    .background(colors.goldSeam.copy(alpha = 0.14f))
                                    .border(1.dp, colors.goldSeam.copy(alpha = 0.45f), RoundedCornerShape(19.dp))
                                    .clickable(
                                        interactionSource = shopInteraction,
                                        indication = null,
                                        onClick = onShopClick
                                    )
                                    .padding(horizontal = spacing.s)
                                    .semantics {
                                        role = Role.Button
                                        contentDescription = "Sanctuary Shop, $coinsBalance Coins"
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    IconShop(color = colors.goldSeam, size = 16.dp)
                                    Text(
                                        text = "$coinsBalance",
                                        style = typography.caption.copy(
                                            color = colors.goldSeam,
                                            fontSize = 13.sp,
                                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }

                        // Customize Icon Button (Positioned at right of Shop)
                        if (onCustomizationClick != null) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(colors.goldSeam.copy(alpha = 0.14f))
                                    .border(1.dp, colors.goldSeam.copy(alpha = 0.45f), CircleShape)
                                    .clickable(
                                        interactionSource = customizationInteraction,
                                        indication = null,
                                        onClick = onCustomizationClick
                                    )
                                    .semantics {
                                        role = Role.Button
                                        contentDescription = "Customize Themes and Styles"
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                IconCustomizer(color = colors.goldSeam, size = 18.dp)
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
            // MAIN SCREENS: Clean, symmetrical, balanced header matching Home's breathing room
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = spacing.s),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
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
                        ),
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }

                if (onSettingsClick != null) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(colors.surfaceElevated)
                            .border(1.dp, colors.border, CircleShape)
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
                        IconSettings(color = colors.textPrimary, size = 18.dp)
                    }
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