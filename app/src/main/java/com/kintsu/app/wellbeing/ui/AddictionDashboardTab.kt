package com.kintsu.app.wellbeing.ui

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import com.kintsu.app.data.SettingsDataStore
import kotlinx.coroutines.launch
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kintsu.app.data.KintsuSettings
import com.kintsu.app.data.model.AppGroupEntity
import com.kintsu.app.data.model.BlockedWebsiteEntity
import com.kintsu.app.data.model.BlockRuleEntity
import com.kintsu.app.data.model.QuitHabitEntity
import com.kintsu.app.ui.components.IconChevronRight
import com.kintsu.app.ui.components.IconGoldSeam
import com.kintsu.app.ui.components.IconMoon
import com.kintsu.app.ui.components.IconShield
import com.kintsu.app.ui.components.KintsuButton
import com.kintsu.app.ui.components.KintsuButtonVariant
import com.kintsu.app.ui.components.KintsuCard
import com.kintsu.app.ui.theme.KintsuTheme
import com.kintsu.app.wellbeing.domain.QuitHabitTracker
import kotlinx.coroutines.delay

@Composable
fun AddictionDashboardTab(
    settings: KintsuSettings,
    activeQuitHabits: List<QuitHabitEntity>,
    blockedWebsites: List<BlockedWebsiteEntity>,
    appGroups: List<AppGroupEntity>,
    blockRules: List<BlockRuleEntity>,
    interventionsTodayCount: Int,
    onAddQuitHabit: ((name: String, ifThen: String, alt: String, cName: String?, cPhone: String?) -> Unit)? = null,
    onRecordSlip: ((QuitHabitEntity) -> Unit)? = null,
    onRepairSlip: ((QuitHabitEntity) -> Unit)? = null,
    onLogUrge: ((id: Long, intensity: Int, tags: String, note: String) -> Unit)? = null,
    onStartInstantDetox: ((minutes: Int) -> Unit)? = null,
    onToggleShortFormMaster: ((Boolean) -> Unit)? = null,
    onSaveShortFormPlatforms: ((yt: Boolean, ig: Boolean, tt: Boolean, fb: Boolean, snap: Boolean, web: Boolean) -> Unit)? = null,
    onAddWebsite: (domain: String, group: String, mode: String, matchSubdomains: Boolean) -> Unit,
    onToggleWebsite: (BlockedWebsiteEntity) -> Unit,
    onDeleteWebsite: (BlockedWebsiteEntity) -> Unit,
    onOpenParentalControls: () -> Unit,
    onOpenAppLimits: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val settingsStore = remember { SettingsDataStore(context) }
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography

    // Dialog & overlay states
    var isAddingHabit by remember { mutableStateOf(false) }
    var activeSosHabit by remember { mutableStateOf<QuitHabitEntity?>(null) }
    var loggingUrgeHabit by remember { mutableStateOf<QuitHabitEntity?>(null) }
    var isManagingWebsites by remember { mutableStateOf(false) }
    var newWebsiteDomain by remember { mutableStateOf("") }

    // Urge logging state
    var urgeIntensity by remember { mutableFloatStateOf(5f) }
    var urgeTags by remember { mutableStateOf("Boredom, Evening") }
    var urgeNote by remember { mutableStateOf("") }

    // New habit creation state
    var newHabitName by remember { mutableStateOf("") }
    var newHabitIfThen by remember { mutableStateOf("") }
    var newHabitAlts by remember { mutableStateOf("Drink warm tea\n5-minute walk outside\n10 deep mindful breaths") }
    var newContactName by remember { mutableStateOf("") }
    var newContactPhone by remember { mutableStateOf("") }

    // SOS Timer state
    var sosSecondsRemaining by remember { mutableIntStateOf(600) }
    LaunchedEffect(activeSosHabit) {
        if (activeSosHabit != null) {
            sosSecondsRemaining = 600
            while (sosSecondsRemaining > 0) {
                delay(1000)
                sosSecondsRemaining--
            }
        }
    }

    // Platforms state
    var blockYouTube by remember(settings.blockYouTubeShorts) { mutableStateOf(settings.blockYouTubeShorts) }
    var blockInstagram by remember(settings.blockInstagramReels) { mutableStateOf(settings.blockInstagramReels) }
    var blockTikTok by remember(settings.blockTikTok) { mutableStateOf(settings.blockTikTok) }
    var blockFacebook by remember(settings.blockFacebookReels) { mutableStateOf(settings.blockFacebookReels) }
    var blockSnapchat by remember(settings.blockSnapchatSpotlight) { mutableStateOf(settings.blockSnapchatSpotlight) }
    var blockWeb by remember(settings.blockWebShorts) { mutableStateOf(settings.blockWebShorts) }

    fun commitPlatformChange() {
        onSaveShortFormPlatforms?.invoke(blockYouTube, blockInstagram, blockTikTok, blockFacebook, blockSnapchat, blockWeb)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(spacing.m)
    ) {
        // --- 1. HERO: RECOVERY FREEDOM & ATTENTION RECLAIMED ---
        KintsuCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = colors.surfaceElevated,
            borderColor = colors.goldSeam.copy(alpha = 0.5f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(spacing.m)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Addiction & Impulse Recovery",
                            style = typography.itemTitle.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Rewire unconscious reflexes with mindful mastery",
                            style = typography.caption.copy(fontSize = 12.sp),
                            color = colors.textSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(colors.goldSeam.copy(alpha = 0.15f))
                            .padding(8.dp)
                    ) {
                        IconShield(size = 20.dp, color = colors.goldSeam)
                    }
                }

                Spacer(modifier = Modifier.height(spacing.m))

                // Stats Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing.s)
                ) {
                    val maxCleanDays = remember(activeQuitHabits) {
                        activeQuitHabits.maxOfOrNull { QuitHabitTracker.calculateStats(it).cleanDays } ?: 0L
                    }

                    // Stat 1: Max Clean Streak
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.surface)
                            .border(1.dp, colors.border.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(spacing.s),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${maxCleanDays}d",
                                style = typography.itemTitle.copy(
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.goldSeam
                                )
                            )
                            Text(
                                text = "Clean Streak",
                                style = typography.caption.copy(fontSize = 10.sp),
                                color = colors.textSecondary
                            )
                        }
                    }

                    // Stat 2: Interventions Today
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.surface)
                            .border(1.dp, colors.border.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(spacing.s),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$interventionsTodayCount",
                                style = typography.itemTitle.copy(
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.accent
                                )
                            )
                            Text(
                                text = "Pauses Honored",
                                style = typography.caption.copy(fontSize = 10.sp),
                                color = colors.textSecondary
                            )
                        }
                    }

                    // Stat 3: Reclaimed Attention Time
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.surface)
                            .border(1.dp, colors.border.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(spacing.s),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val reclaimedMins = interventionsTodayCount * 5
                            Text(
                                text = "${reclaimedMins}m",
                                style = typography.itemTitle.copy(
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary
                                )
                            )
                            Text(
                                text = "Time Saved",
                                style = typography.caption.copy(fontSize = 10.sp),
                                color = colors.textSecondary
                            )
                        }
                    }
                }

                // Emergency Urge Surfing SOS Trigger
                if (activeQuitHabits.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(spacing.m))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.warning.copy(alpha = 0.15f))
                            .border(1.dp, colors.warning.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .clickable { activeSosHabit = activeQuitHabits.firstOrNull() }
                            .padding(horizontal = spacing.m, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(text = "SOS", fontWeight = FontWeight.Bold, color = colors.warning, fontSize = 12.sp)
                                Text(
                                    text = "Urge Surfing • Craving Emergency Mode",
                                    style = typography.caption.copy(fontWeight = FontWeight.SemiBold),
                                    color = colors.warning
                                )
                            }
                            Text(text = "10m Timer >", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.warning)
                        }
                    }
                }
            }
        }

        // --- 2. QUIT HABITS & IMPULSE BREAKING (CORE SECTION) ---
        Column(verticalArrangement = Arrangement.spacedBy(spacing.s)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Impulse Breaking & Clean Streaks",
                    style = typography.itemTitle.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                    color = colors.textPrimary
                )

                Text(
                    text = "+ New Habit",
                    style = typography.caption.copy(color = colors.accent, fontWeight = FontWeight.Bold),
                    modifier = Modifier
                        .clickable { isAddingHabit = true }
                        .padding(4.dp)
                )
            }

            if (activeQuitHabits.isEmpty()) {
                KintsuCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = colors.surfaceElevated
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(spacing.m),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No active quit habits tracked yet.",
                            style = typography.caption,
                            color = colors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(spacing.s))
                        KintsuButton(
                            text = "Track an Impulse to Break",
                            onClick = { isAddingHabit = true },
                            variant = KintsuButtonVariant.SECONDARY
                        )
                    }
                }
            } else {
                activeQuitHabits.forEach { habit ->
                    val stats = QuitHabitTracker.calculateStats(habit)

                    KintsuCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = colors.surfaceElevated,
                        borderColor = colors.border
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(spacing.m),
                            verticalArrangement = Arrangement.spacedBy(spacing.s)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(colors.goldSeam.copy(alpha = 0.15f))
                                            .border(1.dp, colors.goldSeam, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        IconGoldSeam(color = colors.goldSeam, size = 18.dp)
                                    }

                                    Column {
                                        Text(
                                            text = habit.name,
                                            style = typography.itemTitle.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                                            color = colors.textPrimary
                                        )
                                        Text(
                                            text = "Best: ${habit.bestStreakDays}d • Slips: ${habit.totalSlips} • Mends: ${habit.totalRepairs}",
                                            style = typography.caption.copy(fontSize = 11.sp),
                                            color = colors.textSecondary
                                        )
                                    }
                                }

                                // Days Clean Badge
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(colors.accentSoft)
                                        .border(1.dp, colors.accent.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "${stats.cleanDays}d ${stats.cleanHours}h Clean",
                                        style = typography.caption.copy(color = colors.accent, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    )
                                }
                            }

                            // Kintsugi Gold Repair Banner (if in grace window)
                            if (stats.isEligibleForGoldRepair) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(colors.goldSeam.copy(alpha = 0.12f))
                                        .padding(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Kintsugi Grace Window: ${stats.repairWindowRemainingHours}h remaining",
                                            style = typography.caption.copy(color = colors.goldSeam, fontSize = 11.sp)
                                        )
                                        Text(
                                            text = "Mend (+2 Gold)",
                                            style = typography.caption.copy(color = colors.goldSeam, fontWeight = FontWeight.Bold),
                                            modifier = Modifier.clickable { onRepairSlip?.invoke(habit) }
                                        )
                                    }
                                }
                            }

                            // Action buttons: Log Urge & Record Slip
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(spacing.s)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(colors.surface)
                                        .border(1.dp, colors.border, RoundedCornerShape(8.dp))
                                        .clickable { loggingUrgeHabit = habit }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "Log Urge", style = typography.caption.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium), color = colors.textPrimary)
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(colors.surface)
                                        .border(1.dp, colors.border, RoundedCornerShape(8.dp))
                                        .clickable { onRecordSlip?.invoke(habit) }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "Record Slip", style = typography.caption.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium), color = colors.textSecondary)
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(colors.warning.copy(alpha = 0.12f))
                                        .border(1.dp, colors.warning.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                        .clickable { activeSosHabit = habit }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "Surfing SOS", style = typography.caption.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold), color = colors.warning)
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 3. INSTANT DIGITAL SANCTUARY (ONE-TAP DETOX) ---
        KintsuCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = colors.surfaceElevated
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(spacing.m),
                verticalArrangement = Arrangement.spacedBy(spacing.s)
            ) {
                Text(
                    text = "Instant Digital Detox Sanctuary",
                    style = typography.itemTitle.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
                    color = colors.textPrimary
                )
                Text(
                    text = "One-tap emergency screen lockout to break compulsive loops.",
                    style = typography.caption.copy(fontSize = 11.sp),
                    color = colors.textSecondary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing.xs)
                ) {
                    listOf(15 to "15m Breath", 30 to "30m Reset", 60 to "1h Detox", 120 to "2h Fast").forEach { (mins, label) ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(colors.surface)
                                .border(1.dp, colors.accent.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                                .clickable { onStartInstantDetox?.invoke(mins) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = typography.caption.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colors.accent)
                            )
                        }
                    }
                }
            }
        }

        // --- 4. SHORT-FORM FEED & WEBSITE GUARD (COMPACT & CLEAN) ---
        KintsuCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = colors.surfaceElevated,
            borderColor = if (settings.shortFormBlockingEnabled) colors.accent.copy(alpha = 0.4f) else colors.border
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(spacing.m),
                verticalArrangement = Arrangement.spacedBy(spacing.s)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Short-Form Feed & Website Guard",
                            style = typography.itemTitle.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Intercept compulsive video reels & distracting sites",
                            style = typography.caption.copy(fontSize = 11.sp),
                            color = colors.textSecondary
                        )
                    }

                    Switch(
                        checked = settings.shortFormBlockingEnabled,
                        onCheckedChange = { onToggleShortFormMaster?.invoke(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = colors.accent,
                            checkedTrackColor = colors.accentSoft
                        )
                    )
                }

                if (settings.shortFormBlockingEnabled) {
                    // Quick platform chips (tap to toggle)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        PlatformPill("YT Shorts", blockYouTube) { blockYouTube = !blockYouTube; commitPlatformChange() }
                        PlatformPill("Reels", blockInstagram) { blockInstagram = !blockInstagram; commitPlatformChange() }
                        PlatformPill("TikTok", blockTikTok) { blockTikTok = !blockTikTok; commitPlatformChange() }
                        PlatformPill("Facebook", blockFacebook) { blockFacebook = !blockFacebook; commitPlatformChange() }
                        PlatformPill("Web", blockWeb) { blockWeb = !blockWeb; commitPlatformChange() }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Blocked websites summary row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.surface)
                        .clickable { isManagingWebsites = !isManagingWebsites }
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${blockedWebsites.size} Blocked Websites Guarded",
                        style = typography.caption.copy(fontSize = 12.sp, fontWeight = FontWeight.Medium),
                        color = colors.textPrimary
                    )
                    Text(
                        text = if (isManagingWebsites) "Done ▲" else "Manage Domains ▼",
                        style = typography.caption.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.accent)
                    )
                }

                // Expandable Domain Manager
                if (isManagingWebsites) {
                    Column(verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(spacing.xs)
                        ) {
                            OutlinedTextField(
                                value = newWebsiteDomain,
                                onValueChange = { newWebsiteDomain = it },
                                placeholder = { Text("e.g. twitter.com or reddit.com", fontSize = 11.sp) },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = colors.accent,
                                    unfocusedBorderColor = colors.border
                                )
                            )
                            KintsuButton(
                                text = "Add",
                                onClick = {
                                    if (newWebsiteDomain.isNotBlank()) {
                                        onAddWebsite(newWebsiteDomain.trim(), "Distraction", "ALWAYS_BLOCKED", true)
                                        newWebsiteDomain = ""
                                    }
                                },
                                variant = KintsuButtonVariant.PRIMARY
                            )
                        }

                        blockedWebsites.forEach { site ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = site.domain, fontSize = 12.sp, color = colors.textPrimary)
                                Text(
                                    text = "Remove",
                                    fontSize = 11.sp,
                                    color = colors.error,
                                    modifier = Modifier.clickable { onDeleteWebsite(site) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- 18+ ADULT, PORN & INTIMACY WEB SHIELD CARD ---
        KintsuCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = colors.surfaceElevated
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(spacing.m),
                verticalArrangement = Arrangement.spacedBy(spacing.s)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(colors.warning.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            IconShield(size = 18.dp, color = colors.warning)
                        }

                        Column {
                            Text(
                                text = "18+ Adult & Intimacy Web Shield",
                                style = typography.itemTitle.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                                color = colors.textPrimary
                            )
                            Text(
                                text = "DNS sinkhole & browser filter for adult, sex & seductive web",
                                style = typography.caption.copy(fontSize = 11.sp),
                                color = colors.textSecondary
                            )
                        }
                    }

                    val isInvincibleLocked = settings.invincibleModeEnabled
                    Switch(
                        checked = settings.blockNsfw,
                        enabled = !isInvincibleLocked,
                        onCheckedChange = { isChecked ->
                            scope.launch {
                                settingsStore.setBlockNsfw(isChecked)
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = colors.warning,
                            checkedTrackColor = colors.warning.copy(alpha = 0.35f)
                        )
                    )
                }

                if (settings.invincibleModeEnabled) {
                    Text(
                        text = "Locked by Invincible Mode. Protection cannot be modified.",
                        style = typography.caption.copy(fontSize = 10.sp),
                        color = colors.warning
                    )
                } else {
                    Text(
                        text = "Auto-blocks 500+ adult domains via Cloudflare 1.1.1.3 Family DNS & keyword trie.",
                        style = typography.caption.copy(fontSize = 10.sp),
                        color = colors.textTertiary
                    )
                }
            }
        }

        // --- 5. CONTINUOUS SESSION & APP LIMITS ---
        KintsuCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = colors.surfaceElevated
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenAppLimits() }
                    .padding(spacing.m),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(colors.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        IconShield(size = 18.dp, color = colors.accent)
                    }

                    Column {
                        Text(
                            text = "App Groups & Session Limits",
                            style = typography.itemTitle.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                            color = colors.textPrimary
                        )
                        Text(
                            text = "${appGroups.size} groups • ${blockRules.size} rules • Cooldown protections",
                            style = typography.caption.copy(fontSize = 11.sp),
                            color = colors.textSecondary
                        )
                    }
                }

                IconChevronRight(size = 16.dp, color = colors.textSecondary)
            }
        }

        // --- 6. BEDTIME SANCTUARY CURFEW CARD ---
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
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (settings.bedtimeLockEnabled) colors.accentSoft else colors.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        IconMoon(size = 18.dp, color = if (settings.bedtimeLockEnabled) colors.accent else colors.textSecondary)
                    }

                    Column {
                        Text(
                            text = "Bedtime Sanctuary Curfew",
                            style = typography.itemTitle.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                            color = colors.textPrimary
                        )
                        Text(
                            text = "${settings.bedtimeStartHour}:00 to ${settings.bedtimeEndHour}:00 • -5 Gold tax on breach",
                            style = typography.caption.copy(fontSize = 11.sp),
                            color = colors.textSecondary
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (settings.bedtimeLockEnabled) colors.accentSoft else colors.surface)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (settings.bedtimeLockEnabled) "Active" else "Scheduled",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (settings.bedtimeLockEnabled) colors.accent else colors.textSecondary
                    )
                }
            }
        }

        // --- 7. PARENTAL CONTROLS & INVINCIBLE MODE ENTRY ---
        KintsuCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = colors.surfaceElevated
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenParentalControls() }
                    .padding(spacing.m),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (settings.invincibleModeEnabled) colors.accentSoft else colors.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        IconShield(size = 18.dp, color = if (settings.invincibleModeEnabled) colors.accent else colors.textSecondary)
                    }

                    Column {
                        Text(
                            text = "Parental Controls & Invincible Mode",
                            style = typography.itemTitle.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                            color = colors.textPrimary
                        )
                        Text(
                            text = if (settings.invincibleModeEnabled) "Active • Bypass attempts locked" else "Lock limits with tamper protection",
                            style = typography.caption.copy(fontSize = 11.sp),
                            color = colors.textSecondary
                        )
                    }
                }

                IconChevronRight(size = 16.dp, color = colors.textSecondary)
            }
        }

        Spacer(modifier = Modifier.height(spacing.xl))
    }

    // Modal Add Quit Habit Dialog
    if (isAddingHabit) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { isAddingHabit = false },
            title = { Text("Track Impulse to Break", style = typography.itemTitle) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(spacing.s)) {
                    OutlinedTextField(
                        value = newHabitName,
                        onValueChange = { newHabitName = it },
                        label = { Text("Impulse / Habit Name") },
                        placeholder = { Text("e.g. Social Media Binge, Night Doomscroll") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newHabitIfThen,
                        onValueChange = { newHabitIfThen = it },
                        label = { Text("If-Then Implementation Plan") },
                        placeholder = { Text("If I feel the urge to scroll, then I will drink tea.") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newContactName,
                        onValueChange = { newContactName = it },
                        label = { Text("Accountability Contact Name (Optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newContactPhone,
                        onValueChange = { newContactPhone = it },
                        label = { Text("Contact Phone (Optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                KintsuButton(
                    text = "Commit",
                    onClick = {
                        if (newHabitName.isNotBlank()) {
                            onAddQuitHabit?.invoke(
                                newHabitName.trim(),
                                newHabitIfThen.trim(),
                                newHabitAlts.trim(),
                                newContactName.ifBlank { null },
                                newContactPhone.ifBlank { null }
                            )
                            newHabitName = ""
                            newHabitIfThen = ""
                            newContactName = ""
                            newContactPhone = ""
                            isAddingHabit = false
                        }
                    },
                    variant = KintsuButtonVariant.PRIMARY
                )
            },
            dismissButton = {
                KintsuButton(
                    text = "Cancel",
                    onClick = { isAddingHabit = false },
                    variant = KintsuButtonVariant.SECONDARY
                )
            }
        )
    }

    // Modal Urge Log Dialog
    loggingUrgeHabit?.let { habit ->
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { loggingUrgeHabit = null },
            title = { Text("Log Urge • ${habit.name}", style = typography.itemTitle) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(spacing.s)) {
                    Text(text = "Intensity: ${urgeIntensity.toInt()}/10", fontSize = 12.sp, color = colors.goldSeam)
                    Slider(
                        value = urgeIntensity,
                        onValueChange = { urgeIntensity = it },
                        valueRange = 1f..10f,
                        steps = 8
                    )
                    OutlinedTextField(
                        value = urgeTags,
                        onValueChange = { urgeTags = it },
                        label = { Text("Triggers (e.g. Boredom, Late Night)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = urgeNote,
                        onValueChange = { urgeNote = it },
                        label = { Text("Optional Reflection") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                KintsuButton(
                    text = "Save Entry",
                    onClick = {
                        onLogUrge?.invoke(habit.id, urgeIntensity.toInt(), urgeTags, urgeNote)
                        loggingUrgeHabit = null
                    },
                    variant = KintsuButtonVariant.PRIMARY
                )
            },
            dismissButton = {
                KintsuButton(
                    text = "Cancel",
                    onClick = { loggingUrgeHabit = null },
                    variant = KintsuButtonVariant.SECONDARY
                )
            }
        )
    }

    // Modal SOS Urge Surfing Dialog
    activeSosHabit?.let { habit ->
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { activeSosHabit = null },
            title = { Text("Urge Surfing • ${habit.name}", style = typography.itemTitle) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(spacing.m)
                ) {
                    val mins = sosSecondsRemaining / 60
                    val secs = sosSecondsRemaining % 60
                    Text(
                        text = String.format("%02d:%02d", mins, secs),
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.goldSeam
                    )
                    Text(
                        text = "Cravings are like ocean waves: they peak and naturally subside within 10 minutes without giving in.",
                        style = typography.caption,
                        color = colors.textSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    if (!habit.emergencyContactPhone.isNullOrBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(spacing.s)
                        ) {
                            KintsuButton(
                                text = "Call Contact",
                                onClick = {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${habit.emergencyContactPhone}"))
                                    context.startActivity(intent)
                                },
                                variant = KintsuButtonVariant.PRIMARY,
                                modifier = Modifier.weight(1f)
                            )
                            KintsuButton(
                                text = "Text Contact",
                                onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:${habit.emergencyContactPhone}")).apply {
                                        putExtra("sms_body", "Hi, I am surfing a craving right now and could use support.")
                                    }
                                    context.startActivity(intent)
                                },
                                variant = KintsuButtonVariant.SECONDARY,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                KintsuButton(
                    text = "Wave Passed • I am Calm",
                    onClick = { activeSosHabit = null },
                    variant = KintsuButtonVariant.PRIMARY
                )
            }
        )
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.PlatformPill(
    name: String,
    checked: Boolean,
    onToggle: () -> Unit
) {
    val colors = KintsuTheme.colors
    Box(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(if (checked) colors.accentSoft else colors.surface)
            .border(1.dp, if (checked) colors.accent else colors.border, RoundedCornerShape(8.dp))
            .clickable(onClick = onToggle)
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (checked) "$name ✓" else name,
            fontSize = 9.sp,
            fontWeight = if (checked) FontWeight.Bold else FontWeight.Normal,
            color = if (checked) colors.accent else colors.textSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
