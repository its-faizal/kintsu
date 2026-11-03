package com.kintsu.app.wellbeing.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kintsu.app.data.KintsuSettings
import com.kintsu.app.ui.components.IconCheck
import com.kintsu.app.ui.components.IconClose
import com.kintsu.app.ui.components.IconShield
import com.kintsu.app.ui.components.KintsuBottomSheet
import com.kintsu.app.ui.components.KintsuCard
import com.kintsu.app.ui.components.KintsuDivider
import com.kintsu.app.ui.theme.KintsuTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShortFormBlockingSheet(
    settings: KintsuSettings,
    onToggleMaster: (Boolean) -> Unit,
    onUpdatePlatforms: (yt: Boolean, ig: Boolean, tt: Boolean, fb: Boolean, snap: Boolean, web: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography

    KintsuBottomSheet(
        onDismissRequest = onDismiss,
        title = "Anti-Doomscroll Shield",
        scrollable = true
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = spacing.l),
            verticalArrangement = Arrangement.spacedBy(spacing.m)
        ) {
            // Hero Status Card
            KintsuCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = colors.surfaceElevated
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(spacing.m),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(spacing.m),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (settings.shortFormBlockingEnabled) colors.accentSoft else colors.surface)
                                .border(1.dp, if (settings.shortFormBlockingEnabled) colors.accent else colors.border, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            IconShield(
                                size = 22.dp,
                                color = if (settings.shortFormBlockingEnabled) colors.accent else colors.textTertiary
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = if (settings.shortFormBlockingEnabled) "Anti-Doomscroll Active" else "Shield Offline",
                                style = typography.itemTitle,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "Instantly exit vertical algorithmic feeds.",
                                style = typography.caption,
                                color = colors.textSecondary
                            )
                        }
                    }

                    Switch(
                        checked = settings.shortFormBlockingEnabled,
                        onCheckedChange = { onToggleMaster(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = colors.accent,
                            checkedTrackColor = colors.accent.copy(alpha = 0.3f),
                            uncheckedThumbColor = colors.textTertiary,
                            uncheckedTrackColor = colors.surface
                        )
                    )
                }
            }

            Text(
                text = "SUPPORTED PLATFORMS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = colors.textTertiary,
                modifier = Modifier.padding(start = spacing.xs, top = spacing.xs)
            )

            // Platform Toggles
            KintsuCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = colors.surface
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    PlatformToggleRow(
                        name = "YouTube Shorts",
                        description = "Blocks player, shelf and bottom tab in YouTube app",
                        enabled = settings.blockYouTubeShorts,
                        masterEnabled = settings.shortFormBlockingEnabled,
                        onToggle = {
                            onUpdatePlatforms(!settings.blockYouTubeShorts, settings.blockInstagramReels, settings.blockTikTok, settings.blockFacebookReels, settings.blockSnapchatSpotlight, settings.blockWebShorts)
                        }
                    )

                    KintsuDivider()

                    PlatformToggleRow(
                        name = "Instagram Reels",
                        description = "Blocks clips viewer, reels tray & explore reels",
                        enabled = settings.blockInstagramReels,
                        masterEnabled = settings.shortFormBlockingEnabled,
                        onToggle = {
                            onUpdatePlatforms(settings.blockYouTubeShorts, !settings.blockInstagramReels, settings.blockTikTok, settings.blockFacebookReels, settings.blockSnapchatSpotlight, settings.blockWebShorts)
                        }
                    )

                    KintsuDivider()

                    PlatformToggleRow(
                        name = "TikTok",
                        description = "Blocks access to pure short-form app feeds",
                        enabled = settings.blockTikTok,
                        masterEnabled = settings.shortFormBlockingEnabled,
                        onToggle = {
                            onUpdatePlatforms(settings.blockYouTubeShorts, settings.blockInstagramReels, !settings.blockTikTok, settings.blockFacebookReels, settings.blockSnapchatSpotlight, settings.blockWebShorts)
                        }
                    )

                    KintsuDivider()

                    PlatformToggleRow(
                        name = "Facebook Reels",
                        description = "Blocks short-form video feed and reels player",
                        enabled = settings.blockFacebookReels,
                        masterEnabled = settings.shortFormBlockingEnabled,
                        onToggle = {
                            onUpdatePlatforms(settings.blockYouTubeShorts, settings.blockInstagramReels, settings.blockTikTok, !settings.blockFacebookReels, settings.blockSnapchatSpotlight, settings.blockWebShorts)
                        }
                    )

                    KintsuDivider()

                    PlatformToggleRow(
                        name = "Snapchat Spotlight",
                        description = "Blocks algorithmic spotlight video tab",
                        enabled = settings.blockSnapchatSpotlight,
                        masterEnabled = settings.shortFormBlockingEnabled,
                        onToggle = {
                            onUpdatePlatforms(settings.blockYouTubeShorts, settings.blockInstagramReels, settings.blockTikTok, settings.blockFacebookReels, !settings.blockSnapchatSpotlight, settings.blockWebShorts)
                        }
                    )

                    KintsuDivider()

                    PlatformToggleRow(
                        name = "Web Browsers & Shorts URLs",
                        description = "Inspects Chrome, Firefox, Edge for /shorts & /reels URLs",
                        enabled = settings.blockWebShorts,
                        masterEnabled = settings.shortFormBlockingEnabled,
                        onToggle = {
                            onUpdatePlatforms(settings.blockYouTubeShorts, settings.blockInstagramReels, settings.blockTikTok, settings.blockFacebookReels, settings.blockSnapchatSpotlight, !settings.blockWebShorts)
                        }
                    )
                }
            }

            // Explanatory note
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.surfaceElevated)
                    .border(1.dp, colors.border.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(spacing.m)
            ) {
                Text(
                    text = "How it protects you: Kintsu's Accessibility Blocker detects when you enter an algorithmic short-form loop, automatically presses back to exit the trance, and prompts you to return to conscious presence.",
                    style = typography.caption,
                    color = colors.textSecondary,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun PlatformToggleRow(
    name: String,
    description: String,
    enabled: Boolean,
    masterEnabled: Boolean,
    onToggle: () -> Unit
) {
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = masterEnabled, onClick = onToggle)
            .padding(spacing.m),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f).padding(end = spacing.m),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = name,
                style = typography.itemTitle.copy(fontSize = 14.sp),
                color = if (masterEnabled) colors.textPrimary else colors.textTertiary
            )
            Text(
                text = description,
                style = typography.caption.copy(fontSize = 11.sp),
                color = colors.textSecondary
            )
        }

        Switch(
            checked = enabled && masterEnabled,
            onCheckedChange = { if (masterEnabled) onToggle() },
            enabled = masterEnabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = colors.accent,
                checkedTrackColor = colors.accent.copy(alpha = 0.3f),
                uncheckedThumbColor = colors.textTertiary,
                uncheckedTrackColor = colors.surfaceElevated
            )
        )
    }
}
