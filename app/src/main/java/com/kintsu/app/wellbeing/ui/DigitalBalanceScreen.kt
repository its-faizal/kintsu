package com.kintsu.app.wellbeing.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kintsu.app.data.KintsuSettings
import com.kintsu.app.data.model.AppGroupEntity
import com.kintsu.app.data.model.BlockedWebsiteEntity
import com.kintsu.app.data.model.BlockRuleEntity
import com.kintsu.app.data.model.BlockScheduleEntity
import com.kintsu.app.data.model.DistractionInterventionLogEntity
import com.kintsu.app.data.model.FocusLockSessionEntity
import com.kintsu.app.data.model.IntentionalBreakLogEntity
import com.kintsu.app.ui.components.IconBack
import com.kintsu.app.ui.components.IconCheck
import com.kintsu.app.ui.components.IconClock
import com.kintsu.app.ui.components.IconGlobe
import com.kintsu.app.ui.components.IconLock
import com.kintsu.app.ui.components.IconShield
import com.kintsu.app.ui.components.IconSparkle
import com.kintsu.app.ui.components.KintsuButton
import com.kintsu.app.ui.components.KintsuButtonVariant
import com.kintsu.app.ui.components.KintsuCard
import com.kintsu.app.ui.theme.KintsuTheme
import com.kintsu.app.wellbeing.domain.PermissionStateHelper
import com.kintsu.app.wellbeing.domain.ProtectionState
import com.kintsu.app.wellbeing.domain.ScreenTimeHelper
import com.kintsu.app.wellbeing.service.KintsuAccessibilityBlockerService
import com.kintsu.app.wellbeing.service.KintsuVpnDnsFilterService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun DigitalBalanceScreen(
    settings: KintsuSettings,
    appGroups: List<AppGroupEntity>,
    blockRules: List<BlockRuleEntity>,
    blockSchedules: List<BlockScheduleEntity>,
    blockedWebsites: List<BlockedWebsiteEntity>,
    activeFocusLock: FocusLockSessionEntity?,
    allFocusLockSessions: List<FocusLockSessionEntity>,
    interventions: List<DistractionInterventionLogEntity>,
    breaks: List<IntentionalBreakLogEntity>,
    interventionsTodayCount: Int,
    onBack: () -> Unit,
    onSaveAppGroup: (name: String, category: String, packages: String) -> Unit,
    onSaveRule: (groupId: Long, mode: String, dailyLimitMinutes: Int) -> Unit,
    onAddWebsite: (domain: String, group: String, mode: String, matchSubdomains: Boolean) -> Unit,
    onToggleWebsite: (BlockedWebsiteEntity) -> Unit,
    onDeleteWebsite: (BlockedWebsiteEntity) -> Unit,
    onStartFocusLock: (title: String, durationMinutes: Int, restrictedGroupIds: String, websites: Boolean, strictness: String, breaks: Boolean) -> Unit,
    onEndFocusLock: (session: FocusLockSessionEntity, wasEarly: Boolean) -> Unit,
    onTakeBreak: (session: FocusLockSessionEntity, minutes: Int) -> Unit,
    onSaveSchedule: (ruleId: Long, startMin: Int, endMin: Int, daysMask: Int) -> Unit,
    onToggleSchedule: (BlockScheduleEntity) -> Unit,
    onSaveDailyBudget: (Int) -> Unit,
    onSaveReintervention: (Int) -> Unit,
    onSaveBreathingPreferences: (style: String, durationSec: Int, reinterventionMin: Int, haptics: Boolean) -> Unit,
    onToggleDns: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography

    // Sub-sheet navigation states
    var openSheet by remember { mutableStateOf<String?>(null) } // APPS, WEBSITES, FOCUS_LOCK, LIMITS, BREATHING, SCHEDULES, INSIGHTS, DIAGNOSTICS, DNS

    // Real screen time calculation
    var screenTimeTodayMinutes by remember { mutableLongStateOf(0L) }
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val mins = ScreenTimeHelper.getScreenTimeTodayMinutes(context)
            withContext(Dispatchers.Main) {
                screenTimeTodayMinutes = mins
            }
        }
    }

    // Determine current protection state
    val isAccessibilityGranted = remember { PermissionStateHelper.isAccessibilityEnabled(context) }
    val isUsageGranted = remember { ScreenTimeHelper.hasUsageStatsPermission(context) }

    val protectionState = remember(settings.wellbeingEnabled, settings.protectionFailSafeTripped, isAccessibilityGranted, isUsageGranted, appGroups, blockedWebsites) {
        if (!isAccessibilityGranted || !isUsageGranted) {
            ProtectionState.SETUP_REQUIRED
        } else if (settings.protectionFailSafeTripped) {
            ProtectionState.PAUSED
        } else if (!settings.wellbeingEnabled) {
            ProtectionState.NO_RESTRICTIONS
        } else if (appGroups.isNotEmpty() || blockedWebsites.isNotEmpty()) {
            ProtectionState.ACTIVE
        } else {
            ProtectionState.NO_RESTRICTIONS
        }
    }

    // Remaining daily budget
    val remainingBudgetMinutes = remember(screenTimeTodayMinutes, settings.dailyScreenTimeBudgetMinutes) {
        if (settings.dailyScreenTimeBudgetMinutes > 0) {
            (settings.dailyScreenTimeBudgetMinutes - screenTimeTodayMinutes).coerceAtLeast(0L)
        } else null
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(spacing.m)
        ) {
            // Top Navigation & Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(colors.surfaceElevated)
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center
                ) {
                    IconBack(size = 20.dp, color = colors.textPrimary)
                }
                Spacer(modifier = Modifier.width(spacing.m))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Digital Balance",
                        style = typography.screenTitle.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold),
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Make your screen time intentional.",
                        style = typography.caption,
                        color = colors.textSecondary
                    )
                }

                // Protection Status Badge
                StatusPill(state = protectionState)
            }

            Spacer(modifier = Modifier.height(spacing.l))

            // 4 Concise Live Metrics (Section 1 requirement)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.s)
            ) {
                OverviewMetricCard(
                    title = "Screen Time",
                    value = "${screenTimeTodayMinutes / 60}h ${screenTimeTodayMinutes % 60}m",
                    subtitle = "Measured today",
                    modifier = Modifier.weight(1f)
                )
                OverviewMetricCard(
                    title = "Time Remaining",
                    value = if (remainingBudgetMinutes != null) "${remainingBudgetMinutes}m" else "Unlimited",
                    subtitle = if (remainingBudgetMinutes != null) "In daily budget" else "No budget set",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(spacing.s))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.s)
            ) {
                OverviewMetricCard(
                    title = "Interrupted",
                    value = "$interventionsTodayCount",
                    subtitle = "Mindful pauses",
                    modifier = Modifier.weight(1f)
                )
                OverviewMetricCard(
                    title = "Focus Lock",
                    value = if (activeFocusLock != null) {
                        val remMin = ((activeFocusLock.endTimeMillis - System.currentTimeMillis()) / 60000L).coerceAtLeast(0L)
                        "${remMin}m left"
                    } else "Ready",
                    subtitle = if (activeFocusLock != null) activeFocusLock.title else "No active lock",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(spacing.xl))

            // PRIMARY ACTIONS (Section 1 requirement)
            Text(
                text = "PRIMARY CONTROLS",
                style = typography.caption.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp),
                color = colors.textTertiary
            )
            Spacer(modifier = Modifier.height(spacing.s))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.s)
            ) {
                PrimaryActionCard(
                    title = if (activeFocusLock != null) "Active Focus HUD" else "Start Focus Lock",
                    subtitle = if (activeFocusLock != null) "Tap to view timer" else "Strict session blocking",
                    icon = { IconLock(size = 22.dp, color = colors.goldSeam) },
                    onClick = { openSheet = "FOCUS_LOCK" },
                    modifier = Modifier.weight(1f)
                )

                PrimaryActionCard(
                    title = "Blocked Apps",
                    subtitle = "${appGroups.sumOf { it.packageNames.split(",").filter { s -> s.isNotBlank() }.size }} configured",
                    icon = { IconShield(size = 22.dp, color = colors.goldSeam) },
                    onClick = { openSheet = "APPS" },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(spacing.s))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.s)
            ) {
                PrimaryActionCard(
                    title = "Websites Shield",
                    subtitle = "${blockedWebsites.count { it.isEnabled }} restricted",
                    icon = { IconGlobe(size = 22.dp, color = colors.goldSeam) },
                    onClick = { openSheet = "WEBSITES" },
                    modifier = Modifier.weight(1f)
                )

                PrimaryActionCard(
                    title = "Time Limits",
                    subtitle = "${settings.dailyScreenTimeBudgetMinutes / 60}h budget",
                    icon = { IconClock(size = 22.dp, color = colors.goldSeam) },
                    onClick = { openSheet = "LIMITS" },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(spacing.xl))

            // SECONDARY SECTIONS (Section 1 requirement)
            Text(
                text = "INTELLIGENT DISTRACTION TOOLS",
                style = typography.caption.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp),
                color = colors.textTertiary
            )
            Spacer(modifier = Modifier.height(spacing.s))

            SecondarySectionRow(
                title = "Breathing Interventions",
                subtitle = "Style: ${settings.interventionStyle.replace('_', ' ')} (${settings.breathingDurationSeconds}s)",
                onClick = { openSheet = "BREATHING" }
            )

            Spacer(modifier = Modifier.height(spacing.xs))

            SecondarySectionRow(
                title = "Blocking Schedules",
                subtitle = "${blockSchedules.count { it.isEnabled }} recurring window rules active",
                onClick = { openSheet = "SCHEDULES" }
            )

            Spacer(modifier = Modifier.height(spacing.xs))

            SecondarySectionRow(
                title = "DNS Protection & Local Filter",
                subtitle = if (KintsuVpnDnsFilterService.isVpnRunning) "VPN Local Loopback Active" else "Domain loopback offline",
                onClick = { openSheet = "DNS" }
            )

            Spacer(modifier = Modifier.height(spacing.xs))

            SecondarySectionRow(
                title = "Distraction Insights",
                subtitle = "Weekly trends, top apps & intervention outcomes",
                onClick = { openSheet = "INSIGHTS" }
            )

            Spacer(modifier = Modifier.height(spacing.xs))

            SecondarySectionRow(
                title = "Protection Settings & Diagnostics",
                subtitle = "Granular capabilities, guided permissions & safe simulator",
                onClick = { openSheet = "DIAGNOSTICS" }
            )
        }
    }

    // Modal Sheets for sub-features
    when (openSheet) {
        "APPS" -> {
            AppBlockingSheet(
                appGroups = appGroups,
                blockRules = blockRules,
                onSaveAppGroup = onSaveAppGroup,
                onSaveRule = onSaveRule,
                onDismiss = { openSheet = null }
            )
        }
        "WEBSITES" -> {
            WebsiteBlockingSheet(
                blockedWebsites = blockedWebsites,
                onAddWebsite = onAddWebsite,
                onToggleWebsite = onToggleWebsite,
                onDeleteWebsite = onDeleteWebsite,
                onDismiss = { openSheet = null }
            )
        }
        "FOCUS_LOCK" -> {
            FocusLockSheet(
                activeSession = activeFocusLock,
                appGroups = appGroups,
                onStartSession = { title, dur, groups, webs, strict, breaksAllowed ->
                    onStartFocusLock(title, dur, groups, webs, strict, breaksAllowed)
                },
                onEndSession = onEndFocusLock,
                onTakeBreak = onTakeBreak,
                onDismiss = { openSheet = null }
            )
        }
        "LIMITS" -> {
            TimeLimitsSheet(
                dailyBudgetMinutes = settings.dailyScreenTimeBudgetMinutes,
                reinterventionMinutes = settings.reinterventionIntervalMinutes,
                appGroups = appGroups,
                blockRules = blockRules,
                onSaveDailyBudget = onSaveDailyBudget,
                onSaveReintervention = onSaveReintervention,
                onDismiss = { openSheet = null }
            )
        }
        "BREATHING" -> {
            BreathingSettingsSheet(
                currentStyle = settings.interventionStyle,
                currentDurationSeconds = settings.breathingDurationSeconds,
                currentReinterventionMinutes = settings.reinterventionIntervalMinutes,
                currentHaptics = settings.wellbeingHapticsEnabled,
                onSavePreferences = onSaveBreathingPreferences,
                onDismiss = { openSheet = null }
            )
        }
        "SCHEDULES" -> {
            BlockingSchedulesSheet(
                schedules = blockSchedules,
                appGroups = appGroups,
                onAddSchedule = onSaveSchedule,
                onToggleSchedule = onToggleSchedule,
                onDismiss = { openSheet = null }
            )
        }
        "DNS" -> {
            DnsProtectionSheet(
                blockedWebsites = blockedWebsites,
                isDnsEnabled = settings.dnsProtectionEnabled,
                onToggleDns = onToggleDns,
                onDismiss = { openSheet = null }
            )
        }
        "INSIGHTS" -> {
            DistractionInsightsSheet(
                interventions = interventions,
                focusLockSessions = allFocusLockSessions,
                breaks = breaks,
                onDismiss = { openSheet = null }
            )
        }
        "DIAGNOSTICS" -> {
            PermissionsAndDiagnosticsSheet(
                onDismiss = { openSheet = null }
            )
        }
    }
}

@Composable
private fun StatusPill(state: ProtectionState) {
    val colors = KintsuTheme.colors
    val (label, bg, textColor) = when (state) {
        ProtectionState.ACTIVE -> Triple("Protection Active", colors.accentSoft, colors.accent)
        ProtectionState.PAUSED -> Triple("Protection Paused", colors.goldSeamSoft, colors.goldSeam)
        ProtectionState.NO_RESTRICTIONS -> Triple("No Active Restrictions", colors.surfaceElevated, colors.textSecondary)
        ProtectionState.SETUP_REQUIRED -> Triple("Setup Required", colors.goldSeamSoft, colors.goldSeam)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
    }
}

@Composable
private fun OverviewMetricCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography

    KintsuCard(
        modifier = modifier,
        backgroundColor = colors.surfaceElevated
    ) {
        Column(modifier = Modifier.padding(spacing.m)) {
            Text(title, style = typography.caption, color = colors.textSecondary)
            Spacer(modifier = Modifier.height(spacing.xs))
            Text(value, style = typography.itemTitle.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold), color = colors.goldSeam)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, fontSize = 11.sp, color = colors.textTertiary)
        }
    }
}

@Composable
private fun PrimaryActionCard(
    title: String,
    subtitle: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography

    KintsuCard(
        modifier = modifier.clickable(onClick = onClick),
        backgroundColor = colors.surfaceElevated
    ) {
        Column(modifier = Modifier.padding(spacing.m)) {
            icon()
            Spacer(modifier = Modifier.height(spacing.s))
            Text(title, style = typography.itemTitle.copy(fontWeight = FontWeight.SemiBold), color = colors.textPrimary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, fontSize = 11.sp, color = colors.textSecondary)
        }
    }
}

@Composable
private fun SecondarySectionRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography

    KintsuCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        backgroundColor = colors.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.m, vertical = spacing.s),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = typography.itemTitle.copy(fontSize = 15.sp), color = colors.textPrimary)
                Text(subtitle, style = typography.caption, color = colors.textSecondary)
            }
            Text("→", style = typography.itemTitle, color = colors.goldSeam)
        }
    }
}
