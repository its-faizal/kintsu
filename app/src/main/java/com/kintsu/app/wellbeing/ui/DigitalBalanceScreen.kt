package com.kintsu.app.wellbeing.ui

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
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
import com.kintsu.app.data.model.QuitHabitEntity
import com.kintsu.app.ui.components.IconBack
import com.kintsu.app.ui.components.IconChevronRight
import com.kintsu.app.ui.components.IconHome
import com.kintsu.app.ui.components.IconMoon
import com.kintsu.app.ui.components.IconNotifications
import com.kintsu.app.ui.components.IconProgress
import com.kintsu.app.ui.components.IconShield
import com.kintsu.app.ui.theme.KintsuTheme

enum class AddictionBottomTab(val title: String) {
    DASHBOARD("Dashboard"),
    STATISTICS("Statistics"),
    NOTIFICATIONS("Notifications"),
    BEDTIME("Bedtime")
}

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
    activeQuitHabits: List<QuitHabitEntity> = emptyList(),
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
    onToggleDns: (Boolean) -> Unit,
    onToggleMasterShield: ((Boolean) -> Unit)? = null,
    onSaveShortFormEnabled: ((Boolean) -> Unit)? = null,
    onSaveShortFormPlatforms: ((yt: Boolean, ig: Boolean, tt: Boolean, fb: Boolean, snap: Boolean, web: Boolean) -> Unit)? = null,
    onSaveContinuousSession: ((maxSession: Int, cooldown: Int) -> Unit)? = null,
    onSaveLaunchLimit: ((enabled: Boolean, maxLaunches: Int) -> Unit)? = null,
    onSaveBedtimeLock: ((enabled: Boolean, startHour: Int, endHour: Int) -> Unit)? = null,
    onSaveMindfulPause: ((Boolean) -> Unit)? = null,
    onStartInstantDetox: ((minutes: Int) -> Unit)? = null,
    onSaveBedtimeSchedule: ((enabled: Boolean, startHour: Int, endHour: Int, daysMask: Int, dnd: Boolean) -> Unit)? = null,
    onSaveNotificationBatching: ((enabled: Boolean, storeAll: Boolean, historyDays: Int, recapType: String, morning: Boolean, afternoon: Boolean, evening: Boolean) -> Unit)? = null,
    onSaveInvincibleMode: ((enabled: Boolean, windowHour: Int, windowMin: Int, includeShorts: Boolean, includeBedtime: Boolean) -> Unit)? = null,
    onSaveParentalControlsProtection: ((protectedAccess: Boolean, tamperProtection: Boolean, uninstallHour: Int, uninstallMin: Int) -> Unit)? = null,
    onAddQuitHabit: ((name: String, ifThen: String, alt: String, cName: String?, cPhone: String?) -> Unit)? = null,
    onRecordSlip: ((QuitHabitEntity) -> Unit)? = null,
    onRepairSlip: ((QuitHabitEntity) -> Unit)? = null,
    onLogUrge: ((id: Long, intensity: Int, tags: String, note: String) -> Unit)? = null
) {
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography

    var currentTab by remember { mutableStateOf(AddictionBottomTab.DASHBOARD) }
    var showParentalControls by remember { mutableStateOf(false) }

    // App/group restriction sheets
    var showAppPicker by remember { mutableStateOf(false) }
    var showGroupPicker by remember { mutableStateOf(false) }

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
        ) {
            // --- TOP HEADER BAR ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.l, vertical = spacing.s),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(colors.surfaceElevated)
                            .clickable {
                                if (showParentalControls) {
                                    showParentalControls = false
                                } else {
                                    onBack()
                                }
                            }
                            .semantics { role = Role.Button },
                        contentAlignment = Alignment.Center
                    ) {
                        IconBack(size = 18.dp, color = colors.textPrimary)
                    }

                    Text(
                        text = if (showParentalControls) "Parental controls" else currentTab.title,
                        style = typography.screenTitle.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                        color = colors.textPrimary
                    )
                }

                // Top right: Parental controls shortcut
                if (!showParentalControls) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (settings.invincibleModeEnabled) colors.accentSoft else colors.surfaceElevated)
                            .border(1.dp, if (settings.invincibleModeEnabled) colors.accent else colors.border.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                            .clickable { showParentalControls = true }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .semantics { role = Role.Button },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            IconShield(
                                size = 16.dp,
                                color = if (settings.invincibleModeEnabled) colors.accent else colors.textSecondary
                            )
                            Text(
                                text = "Invincible",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (settings.invincibleModeEnabled) colors.accent else colors.textSecondary
                            )
                        }
                    }
                }
            }

            // --- TAB CONTENT (Scrollable) ---
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = spacing.l, vertical = spacing.s)
                ) {
                    if (showParentalControls) {
                        ParentalControlsScreen(
                            settings = settings,
                            onBack = { showParentalControls = false },
                            onSaveInvincibleMode = { en, wH, wM, sh, bt ->
                                onSaveInvincibleMode?.invoke(en, wH, wM, sh, bt)
                            },
                            onSaveProtection = { prot, tamp, unH, unM ->
                                onSaveParentalControlsProtection?.invoke(prot, tamp, unH, unM)
                            },
                            onOpenAppRestrictions = { showAppPicker = true },
                            onOpenGroupRestrictions = { showGroupPicker = true }
                        )
                    } else {
                        when (currentTab) {
                            AddictionBottomTab.DASHBOARD -> {
                                AddictionDashboardTab(
                                    settings = settings,
                                    activeQuitHabits = activeQuitHabits,
                                    blockedWebsites = blockedWebsites,
                                    appGroups = appGroups,
                                    blockRules = blockRules,
                                    interventionsTodayCount = interventionsTodayCount,
                                    onAddQuitHabit = onAddQuitHabit,
                                    onRecordSlip = onRecordSlip,
                                    onRepairSlip = onRepairSlip,
                                    onLogUrge = onLogUrge,
                                    onStartInstantDetox = onStartInstantDetox,
                                    onToggleShortFormMaster = { onSaveShortFormEnabled?.invoke(it) },
                                    onSaveShortFormPlatforms = { yt, ig, tt, fb, snap, web ->
                                        onSaveShortFormPlatforms?.invoke(yt, ig, tt, fb, snap, web)
                                    },
                                    onAddWebsite = onAddWebsite,
                                    onToggleWebsite = onToggleWebsite,
                                    onDeleteWebsite = onDeleteWebsite,
                                    onOpenParentalControls = { showParentalControls = true },
                                    onOpenAppLimits = { showAppPicker = true }
                                )
                            }

                            AddictionBottomTab.STATISTICS -> {
                                StatisticsTab()
                            }

                            AddictionBottomTab.NOTIFICATIONS -> {
                                NotificationsTab(
                                    settings = settings,
                                    onSaveNotificationBatching = { en, storeAll, historyDays, recapType, morning, afternoon, evening ->
                                        onSaveNotificationBatching?.invoke(en, storeAll, historyDays, recapType, morning, afternoon, evening)
                                    }
                                )
                            }

                            AddictionBottomTab.BEDTIME -> {
                                BedtimeTab(
                                    settings = settings,
                                    onSaveBedtimeSchedule = { en, st, enH, days, dnd ->
                                        onSaveBedtimeSchedule?.invoke(en, st, enH, days, dnd)
                                    },
                                    onOpenDistractingApps = { showAppPicker = true }
                                )
                            }
                        }
                    }
                }
            }

            // --- BOTTOM NAVIGATION BAR (Images 3, 4, 5) ---
            if (!showParentalControls) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.surface)
                        .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                        .padding(horizontal = spacing.m, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AddictionBottomTab.values().forEach { tab ->
                        val isSelected = currentTab == tab
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { currentTab = tab }
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                                .semantics { role = Role.Tab }
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isSelected) colors.accentSoft else Color.Transparent)
                                    .padding(horizontal = 18.dp, vertical = 5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                when (tab) {
                                    AddictionBottomTab.DASHBOARD -> IconHome(
                                        size = 20.dp,
                                        color = if (isSelected) colors.accent else colors.textSecondary
                                    )
                                    AddictionBottomTab.STATISTICS -> IconProgress(
                                        size = 20.dp,
                                        color = if (isSelected) colors.accent else colors.textSecondary
                                    )
                                    AddictionBottomTab.NOTIFICATIONS -> IconNotifications(
                                        size = 20.dp,
                                        color = if (isSelected) colors.accent else colors.textSecondary
                                    )
                                    AddictionBottomTab.BEDTIME -> IconMoon(
                                        size = 20.dp,
                                        color = if (isSelected) colors.accent else colors.textSecondary
                                    )
                                }
                            }

                            Text(
                                text = tab.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) colors.accent else colors.textSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}
