package com.kintsu.app.wellbeing.ui

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kintsu.app.data.KintsuSettings
import com.kintsu.app.data.model.BlockedWebsiteEntity
import com.kintsu.app.ui.components.IconChevronRight
import com.kintsu.app.ui.components.IconDelete
import com.kintsu.app.ui.components.IconEdit
import com.kintsu.app.ui.components.IconShield
import com.kintsu.app.ui.components.IconUmbrella
import com.kintsu.app.ui.theme.KintsuTheme
import com.kintsu.app.wellbeing.domain.PermissionStateHelper

enum class DashboardSubTab {
    SHORTS,
    WEBSITES
}

@Composable
fun ShortsAndWebsitesTab(
    settings: KintsuSettings,
    blockedWebsites: List<BlockedWebsiteEntity>,
    onSaveShortFormBudget: (Int) -> Unit,
    onToggleShortFormMaster: (Boolean) -> Unit,
    onSaveShortFormPlatforms: (yt: Boolean, ig: Boolean, tt: Boolean, fb: Boolean, snap: Boolean, web: Boolean) -> Unit,
    onToggleNsfw: (Boolean) -> Unit,
    onAddWebsite: (domain: String, group: String, mode: String, matchSubdomains: Boolean) -> Unit,
    onToggleWebsite: (BlockedWebsiteEntity) -> Unit,
    onDeleteWebsite: (BlockedWebsiteEntity) -> Unit
) {
    val context = LocalContext.current
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography

    var activeSubTab by remember { mutableStateOf(DashboardSubTab.SHORTS) }
    var isAccessibilityGranted by remember { mutableStateOf(PermissionStateHelper.isAccessibilityEnabled(context)) }

    // Budget Editor Dialog
    var showBudgetDialog by remember { mutableStateOf(false) }

    // Add Website Dialog
    var showAddWebsiteDialog by remember { mutableStateOf(false) }
    var newWebsiteInput by remember { mutableStateOf("") }

    // Expandable platform details
    var instagramExpanded by remember { mutableStateOf(false) }
    var snapchatExpanded by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(spacing.m)) {
        // --- SUB-TAB SEGMENTED SWITCHER (Shorts Blocking vs Websites Blocking) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(colors.surfaceElevated)
                .border(1.dp, colors.border.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (activeSubTab == DashboardSubTab.SHORTS) colors.accentSoft else Color.Transparent)
                    .clickable { activeSubTab = DashboardSubTab.SHORTS }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Shorts Blocking",
                    fontSize = 13.sp,
                    fontWeight = if (activeSubTab == DashboardSubTab.SHORTS) FontWeight.Bold else FontWeight.Normal,
                    color = if (activeSubTab == DashboardSubTab.SHORTS) colors.accent else colors.textSecondary
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (activeSubTab == DashboardSubTab.WEBSITES) colors.accentSoft else Color.Transparent)
                    .clickable { activeSubTab = DashboardSubTab.WEBSITES }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Websites Blocking",
                    fontSize = 13.sp,
                    fontWeight = if (activeSubTab == DashboardSubTab.WEBSITES) FontWeight.Bold else FontWeight.Normal,
                    color = if (activeSubTab == DashboardSubTab.WEBSITES) colors.accent else colors.textSecondary
                )
            }
        }

        if (activeSubTab == DashboardSubTab.SHORTS) {
            // ==================== SCREEN 1: SHORTS BLOCKING (IMAGE 1) ====================
            Text(
                text = "Shorts blocking",
                style = typography.screenTitle.copy(fontSize = 22.sp, fontWeight = FontWeight.Bold),
                color = colors.textPrimary
            )
            Text(
                text = "Control how much time you spend on short content across platforms like Instagram, YouTube, Snapchat, and Facebook, including their websites.",
                style = typography.caption,
                color = colors.textSecondary
            )

            // Section label
            Text(
                text = "Short content",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.accent
            )

            // CIRCULAR DIAL WITH UMBRELLA ICON (Exact Image 1 layout)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = spacing.m),
                contentAlignment = Alignment.Center
            ) {
                val budget = settings.maxContinuousSessionMinutes.coerceAtLeast(15)
                val strokeColor = colors.accent
                val trackColor = colors.surfaceElevated

                Canvas(modifier = Modifier.size(190.dp)) {
                    val w = this.size.width
                    val strokeWidth = 14.dp.toPx()
                    // Track circle
                    drawCircle(
                        color = trackColor,
                        radius = (w - strokeWidth) / 2f,
                        style = Stroke(strokeWidth)
                    )
                    // Active arc (e.g. 75% fill)
                    drawArc(
                        color = strokeColor,
                        startAngle = -90f,
                        sweepAngle = 270f,
                        useCenter = false,
                        style = Stroke(strokeWidth, cap = StrokeCap.Round)
                    )
                }

                // Inner content: Umbrella, 30M, Left from 30m, Pencil edit button
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconUmbrella(color = colors.goldSeam, size = 28.dp)

                    Text(
                        text = "${budget}M",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Left from ${budget}m",
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(colors.surfaceElevated)
                            .clickable { showBudgetDialog = true }
                            .semantics { role = Role.Button },
                        contentAlignment = Alignment.Center
                    ) {
                        IconEdit(color = colors.textSecondary, size = 15.dp)
                    }
                }
            }

            // ACCESSIBILITY PERMISSION CARD (If not granted)
            if (!isAccessibilityGranted) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.surfaceElevated)
                        .border(1.dp, colors.goldSeam.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            IconShield(color = colors.goldSeam, size = 20.dp)
                            Text(
                                text = "Accessibility",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                        }
                        Text(
                            text = "Kintsu requires accessibility permission to block short content and websites effectively.",
                            style = typography.caption,
                            color = colors.textSecondary
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Help?",
                                fontSize = 12.sp,
                                color = colors.textTertiary
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(colors.accent)
                                    .clickable {
                                        context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                        })
                                    }
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                                    .semantics { role = Role.Button }
                            ) {
                                Text(
                                    text = "Grant Permission",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }
            }

            // PLATFORM LIST (Exact Image 1 Cards)
            Column(verticalArrangement = Arrangement.spacedBy(spacing.s)) {
                // 1. Instagram
                PlatformCard(
                    title = "Instagram",
                    subtitle = "Restrict features on instagram.",
                    isExpandable = true,
                    isExpanded = instagramExpanded,
                    onToggleExpand = { instagramExpanded = !instagramExpanded }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp, start = 8.dp, end = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Restrict Instagram Reels & Explore feed",
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                        Switch(
                            checked = settings.blockInstagramReels,
                            onCheckedChange = {
                                onSaveShortFormPlatforms(
                                    settings.blockYouTubeShorts,
                                    it,
                                    settings.blockTikTok,
                                    settings.blockFacebookReels,
                                    settings.blockSnapchatSpotlight,
                                    settings.blockWebShorts
                                )
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = colors.accent,
                                checkedTrackColor = colors.accentSoft
                            )
                        )
                    }
                }

                // 2. Snapchat
                PlatformCard(
                    title = "Snapchat",
                    subtitle = "Restrict features on snapchat.",
                    isExpandable = true,
                    isExpanded = snapchatExpanded,
                    onToggleExpand = { snapchatExpanded = !snapchatExpanded }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp, start = 8.dp, end = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Restrict Snapchat Spotlight feed",
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                        Switch(
                            checked = settings.blockSnapchatSpotlight,
                            onCheckedChange = {
                                onSaveShortFormPlatforms(
                                    settings.blockYouTubeShorts,
                                    settings.blockInstagramReels,
                                    settings.blockTikTok,
                                    settings.blockFacebookReels,
                                    it,
                                    settings.blockWebShorts
                                )
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = colors.accent,
                                checkedTrackColor = colors.accentSoft
                            )
                        )
                    }
                }

                // 3. YouTube
                PlatformCardWithSwitch(
                    title = "Youtube",
                    subtitle = "Restrict shorts on youtube.",
                    isChecked = settings.blockYouTubeShorts,
                    onCheckedChange = {
                        onSaveShortFormPlatforms(
                            it,
                            settings.blockInstagramReels,
                            settings.blockTikTok,
                            settings.blockFacebookReels,
                            settings.blockSnapchatSpotlight,
                            settings.blockWebShorts
                        )
                    }
                )

                // 4. Facebook
                PlatformCardWithSwitch(
                    title = "Facebook",
                    subtitle = "Restrict reels on facebook.",
                    isChecked = settings.blockFacebookReels,
                    onCheckedChange = {
                        onSaveShortFormPlatforms(
                            settings.blockYouTubeShorts,
                            settings.blockInstagramReels,
                            settings.blockTikTok,
                            it,
                            settings.blockSnapchatSpotlight,
                            settings.blockWebShorts
                        )
                    }
                )

                // 5. Reddit
                PlatformCardWithSwitch(
                    title = "Reddit",
                    subtitle = "Restrict shorts on reddit.",
                    isChecked = settings.blockTikTok, // Uses secondary short-form slot
                    onCheckedChange = {
                        onSaveShortFormPlatforms(
                            settings.blockYouTubeShorts,
                            settings.blockInstagramReels,
                            it,
                            settings.blockFacebookReels,
                            settings.blockSnapchatSpotlight,
                            settings.blockWebShorts
                        )
                    }
                )
            }

        } else {
            // ==================== SCREEN 2: WEBSITES BLOCKING (IMAGE 2) ====================
            Text(
                text = "Websites blocking",
                style = typography.screenTitle.copy(fontSize = 22.sp, fontWeight = FontWeight.Bold),
                color = colors.textPrimary
            )
            Text(
                text = "Block adult websites and any custom sites you choose to create a safer and more focused online experience. Take charge of your browsing and stay distraction-free.",
                style = typography.caption,
                color = colors.textSecondary
            )

            // Section: Adult content
            Text(
                text = "Adult content",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.accent
            )

            // Block Nsfw Card (Exact Image 2)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.surfaceElevated)
                    .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Block Nsfw",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Restrict browsers from opening adult and porn websites.",
                            fontSize = 11.sp,
                            color = colors.textSecondary
                        )
                    }
                    Switch(
                        checked = settings.blockNsfw,
                        onCheckedChange = { onToggleNsfw(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = colors.accent,
                            checkedTrackColor = colors.accentSoft
                        )
                    )
                }
            }

            // Section: Blocked websites
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Blocked websites",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.accent
                )
                Text(
                    text = "${blockedWebsites.count { it.isEnabled }} active",
                    fontSize = 11.sp,
                    color = colors.textTertiary
                )
            }

            if (blockedWebsites.isEmpty()) {
                // Empty state with clean sad face icon (Exact Image 2 layout)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = spacing.xl),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(spacing.m)
                    ) {
                        // Sad face vector
                        Canvas(modifier = Modifier.size(68.dp)) {
                            val stroke = 2.dp.toPx()
                            val w = this.size.width
                            val h = this.size.height
                            drawCircle(colors.textTertiary.copy(alpha = 0.35f), w / 2f)
                            // Eyes
                            drawCircle(colors.textSecondary, 3.dp.toPx(), Offset(w * 0.35f, h * 0.40f))
                            drawCircle(colors.textSecondary, 3.dp.toPx(), Offset(w * 0.65f, h * 0.40f))
                            // Sad mouth arc
                            val arc = androidx.compose.ui.graphics.Path().apply {
                                moveTo(w * 0.32f, h * 0.72f)
                                quadraticBezierTo(w * 0.50f, h * 0.58f, w * 0.68f, h * 0.72f)
                            }
                            drawPath(arc, colors.textSecondary, style = Stroke(stroke, cap = StrokeCap.Round))
                        }

                        Text(
                            text = "Click on '+ Add Website' button to add distracting websites which you wish to block.",
                            fontSize = 12.sp,
                            color = colors.textSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = spacing.l)
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    blockedWebsites.forEach { site ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(colors.surfaceElevated)
                                .border(1.dp, colors.border.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = site.domain,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = "Subdomains: ${if (site.matchSubdomains) "Included" else "Exact"}",
                                        fontSize = 10.sp,
                                        color = colors.textTertiary
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Switch(
                                        checked = site.isEnabled,
                                        onCheckedChange = { onToggleWebsite(site.copy(isEnabled = it)) },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = colors.accent,
                                            checkedTrackColor = colors.accentSoft
                                        )
                                    )

                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .clickable { onDeleteWebsite(site) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        IconDelete(color = colors.textTertiary, size = 16.dp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // + Add Website Button (Full width, prominent, not tiny pill!)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.surfaceElevated)
                    .border(1.dp, colors.accent.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                    .clickable {
                        newWebsiteInput = ""
                        showAddWebsiteDialog = true
                    }
                    .padding(vertical = 14.dp)
                    .semantics { role = Role.Button },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+ Add Website",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.accent
                )
            }
        }
    }

    // Budget Selection Dialog
    if (showBudgetDialog) {
        val options = listOf(15, 30, 45, 60, 90, 120)
        AlertDialog(
            onDismissRequest = { showBudgetDialog = false },
            title = { Text("Daily Shorts Time Budget", style = typography.itemTitle, color = colors.textPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    options.forEach { mins ->
                        val label = if (mins >= 60) "${mins / 60}h" else "${mins}m"
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (settings.maxContinuousSessionMinutes == mins) colors.accentSoft else colors.surface)
                                .clickable {
                                    onSaveShortFormBudget(mins)
                                    showBudgetDialog = false
                                }
                                .padding(14.dp)
                        ) {
                            Text(
                                text = "$label per day",
                                fontSize = 14.sp,
                                fontWeight = if (settings.maxContinuousSessionMinutes == mins) FontWeight.Bold else FontWeight.Normal,
                                color = if (settings.maxContinuousSessionMinutes == mins) colors.accent else colors.textPrimary
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showBudgetDialog = false }) {
                    Text("Close", color = colors.textSecondary)
                }
            },
            containerColor = colors.surfaceElevated,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Add Website Dialog
    if (showAddWebsiteDialog) {
        AlertDialog(
            onDismissRequest = { showAddWebsiteDialog = false },
            title = { Text("Block Distracting Website", style = typography.itemTitle, color = colors.textPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter website domain name (e.g. twitter.com, reddit.com):", fontSize = 12.sp, color = colors.textSecondary)
                    OutlinedTextField(
                        value = newWebsiteInput,
                        onValueChange = { newWebsiteInput = it },
                        placeholder = { Text("domain.com", color = colors.textTertiary) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val clean = newWebsiteInput.trim().lowercase().removePrefix("https://").removePrefix("http://").removePrefix("www.")
                        if (clean.isNotBlank()) {
                            onAddWebsite(clean, "CUSTOM", "BLOCK", true)
                            showAddWebsiteDialog = false
                        }
                    }
                ) {
                    Text("Add", color = colors.accent, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddWebsiteDialog = false }) {
                    Text("Cancel", color = colors.textSecondary)
                }
            },
            containerColor = colors.surfaceElevated,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun PlatformCard(
    title: String,
    subtitle: String,
    isExpandable: Boolean,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    expandedContent: @Composable () -> Unit
) {
    val colors = KintsuTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surfaceElevated)
            .border(1.dp, colors.border.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleExpand),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                    Text(subtitle, fontSize = 11.sp, color = colors.textSecondary)
                }
                IconChevronRight(color = colors.textSecondary, size = 16.dp)
            }
            AnimatedVisibility(visible = isExpanded) {
                expandedContent()
            }
        }
    }
}

@Composable
private fun PlatformCardWithSwitch(
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val colors = KintsuTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surfaceElevated)
            .border(1.dp, colors.border.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                Text(subtitle, fontSize = 11.sp, color = colors.textSecondary)
            }
            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = colors.accent,
                    checkedTrackColor = colors.accentSoft
                )
            )
        }
    }
}
