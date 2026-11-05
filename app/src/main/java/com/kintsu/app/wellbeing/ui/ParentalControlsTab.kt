package com.kintsu.app.wellbeing.ui

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kintsu.app.data.KintsuSettings
import com.kintsu.app.ui.components.IconBack
import com.kintsu.app.ui.components.IconChevronRight
import com.kintsu.app.ui.components.IconClock
import com.kintsu.app.ui.components.IconLock
import com.kintsu.app.ui.components.IconSparkle
import com.kintsu.app.ui.components.IconMoon
import com.kintsu.app.ui.components.IconShield
import com.kintsu.app.ui.components.KintsuButton
import com.kintsu.app.ui.components.KintsuButtonVariant
import com.kintsu.app.ui.components.KintsuTimePickerSheet
import com.kintsu.app.ui.theme.KintsuTheme
import com.kintsu.app.wellbeing.service.KintsuDeviceAdminReceiver
import java.util.Calendar
import java.util.Locale

@Composable
fun ParentalControlsScreen(
    settings: KintsuSettings,
    onBack: () -> Unit,
    onSaveInvincibleMode: (enabled: Boolean, windowHour: Int, windowMin: Int, includeShorts: Boolean, includeBedtime: Boolean) -> Unit,
    onSaveProtection: (protectedAccess: Boolean, tamperProtection: Boolean, uninstallHour: Int, uninstallMin: Int) -> Unit,
    onSaveShortsTimer: (dailyMinutes: Int, sessionMinutes: Int) -> Unit = { _, _ -> },
    onOpenAppRestrictions: () -> Unit,
    onOpenGroupRestrictions: () -> Unit
) {
    val context = LocalContext.current
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography

    var isInvincibleEnabled by remember(settings.invincibleModeEnabled) { mutableStateOf(settings.invincibleModeEnabled) }
    var invincibleHour by remember(settings.invincibleWindowHour) { mutableIntStateOf(settings.invincibleWindowHour) }
    var invincibleMinute by remember(settings.invincibleWindowMinute) { mutableIntStateOf(settings.invincibleWindowMinute) }
    var includeShorts by remember(settings.invincibleIncludeShorts) { mutableStateOf(settings.invincibleIncludeShorts) }
    var includeBedtime by remember(settings.invincibleIncludeBedtime) { mutableStateOf(settings.invincibleIncludeBedtime) }

    var isProtectedAccess by remember(settings.protectedAccessEnabled) { mutableStateOf(settings.protectedAccessEnabled) }
    var isTamperProtection by remember(settings.tamperProtectionEnabled) { mutableStateOf(settings.tamperProtectionEnabled) }
    var uninstallHour by remember(settings.uninstallWindowHour) { mutableIntStateOf(settings.uninstallWindowHour) }
    var uninstallMinute by remember(settings.uninstallWindowMinute) { mutableIntStateOf(settings.uninstallWindowMinute) }

    // Premium Kintsu Time Picker states
    var showInvinciblePicker by remember { mutableStateOf(false) }
    var showUninstallPicker by remember { mutableStateOf(false) }
    var showShortsTimerPicker by remember { mutableStateOf(false) }
    var shortsDailyLimit by remember(settings.shortsDailyLimitMinutes) { mutableIntStateOf(settings.shortsDailyLimitMinutes) }
    var shortsSessionInterruption by remember(settings.shortsSessionInterruptionMinutes) { mutableIntStateOf(settings.shortsSessionInterruptionMinutes) }

    // Device Admin status
    var isDeviceAdminActive by remember { mutableStateOf(false) }
    val dpm = remember { context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager }
    val adminComponent = remember { ComponentName(context, KintsuDeviceAdminReceiver::class.java) }

    LaunchedEffect(Unit) {
        isDeviceAdminActive = dpm?.isAdminActive(adminComponent) == true
    }

    fun isWithin10MinuteWindow(windowHour: Int, windowMinute: Int): Boolean {
        val cal = Calendar.getInstance()
        val currentHour = cal.get(Calendar.HOUR_OF_DAY)
        val currentMin = cal.get(Calendar.MINUTE)
        val currentTotalMin = currentHour * 60 + currentMin
        val windowStart = windowHour * 60 + windowMinute
        val windowEnd = windowStart + 10
        return currentTotalMin in windowStart until windowEnd
    }

    val isLockedByInvincible = settings.invincibleModeEnabled && !isWithin10MinuteWindow(settings.invincibleWindowHour, settings.invincibleWindowMinute)

    fun formatHourMinute(h: Int, m: Int): String {
        val amPm = if (h < 12) "AM" else "PM"
        val displayH = when {
            h == 0 -> 12
            h > 12 -> h - 12
            else -> h
        }
        return String.format(Locale.US, "%d:%02d %s", displayH, m, amPm)
    }

    fun showInvincibleLockedToast() {
        Toast.makeText(
            context,
            "Invincible Mode Active: Changes are locked until your daily 10-minute window (${formatHourMinute(settings.invincibleWindowHour, settings.invincibleWindowMinute)} - ${formatHourMinute(settings.invincibleWindowHour, settings.invincibleWindowMinute + 10)}).",
            Toast.LENGTH_LONG
        ).show()
    }

    fun commitInvincible(
        enabled: Boolean = isInvincibleEnabled,
        h: Int = invincibleHour,
        m: Int = invincibleMinute,
        shorts: Boolean = includeShorts,
        bedtime: Boolean = includeBedtime
    ) {
        onSaveInvincibleMode(enabled, h, m, shorts, bedtime)
    }

    fun commitProtection(
        protected: Boolean = isProtectedAccess,
        tamper: Boolean = isTamperProtection,
        h: Int = uninstallHour,
        m: Int = uninstallMinute
    ) {
        onSaveProtection(protected, tamper, h, m)
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(spacing.m),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Header description
        Text(
            text = "Enforce hard boundaries on screen time, app groups, short-form doomscrolling, and device tamper protections.",
            style = typography.caption.copy(lineHeight = 16.sp),
            color = colors.textSecondary
        )

        if (isLockedByInvincible) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.warning.copy(alpha = 0.15f))
                    .border(1.dp, colors.warning.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(spacing.m)
            ) {
                Text(
                    text = "🔒 Invincible Mode Active: All options and restrictions are locked. Changes can only be made during your daily 10-minute window (${formatHourMinute(settings.invincibleWindowHour, settings.invincibleWindowMinute)} - ${formatHourMinute(settings.invincibleWindowHour, settings.invincibleWindowMinute + 10)}).",
                    style = typography.caption.copy(lineHeight = 16.sp, fontWeight = FontWeight.SemiBold),
                    color = colors.warning
                )
            }
        } else {
            Text(
                text = "When Invincible Mode is enabled, adjustments to restrictions, apps, and uninstall protections are strictly forbidden outside your selected daily 10-minute window.",
                style = typography.caption.copy(lineHeight = 18.sp),
                color = colors.textSecondary
            )
        }

        // --- 1. HARDWARE DEVICE ADMIN REBOOT PROTECTION CARD ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.surfaceElevated)
                .border(1.dp, if (isDeviceAdminActive) colors.goldSeam.copy(alpha = 0.6f) else colors.warning, RoundedCornerShape(16.dp))
                .padding(spacing.m)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(colors.surface),
                            contentAlignment = Alignment.Center
                        ) {
                            IconShield(size = 18.dp, color = if (isDeviceAdminActive) colors.goldSeam else colors.warning)
                        }

                        Column {
                            Text(
                                text = "Device Admin Reboot Protection",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            Text(
                                text = if (isDeviceAdminActive) "Active: Android OS blocks uninstallation on launcher and reboot."
                                else "Inactive: Android can allow deletion right after restart.",
                                fontSize = 11.sp,
                                color = if (isDeviceAdminActive) colors.goldSeam else colors.warning
                            )
                        }
                    }
                }

                if (!isDeviceAdminActive) {
                    KintsuButton(
                        text = "Activate Device Administrator",
                        onClick = {
                            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                                putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
                                putExtra(
                                    DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                                    "Kintsu requires Device Administrator permission to permanently block uninstall bypass and reboot deletion."
                                )
                            }
                            context.startActivity(intent)
                        },
                        variant = KintsuButtonVariant.PRIMARY,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // --- 2. ACTIVATE INVINCIBLE MODE CARD ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.surfaceElevated)
                .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .padding(spacing.m)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(colors.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        IconShield(size = 20.dp, color = colors.accent)
                    }
                    Column {
                        Text(
                            text = "Activate Invincible Mode",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Lock all wellbeing settings outside the 10m window",
                            fontSize = 11.sp,
                            color = colors.textSecondary
                        )
                    }
                }

                Switch(
                    checked = isInvincibleEnabled,
                    enabled = !isLockedByInvincible,
                    onCheckedChange = {
                        if (isLockedByInvincible) {
                            showInvincibleLockedToast()
                            return@Switch
                        }
                        isInvincibleEnabled = it
                        commitInvincible(enabled = it)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = colors.accent,
                        checkedTrackColor = colors.accentSoft
                    )
                )
            }
        }

        // --- 3. INVINCIBLE WINDOW CARD ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.surfaceElevated)
                .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .clickable {
                    if (isLockedByInvincible) {
                        showInvincibleLockedToast()
                    } else {
                        showInvinciblePicker = true
                    }
                }
                .padding(spacing.m)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Invincible Window (10 Minutes)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Settings can only be adjusted within 10 minutes from this time.",
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                }

                Text(
                    text = formatHourMinute(invincibleHour, invincibleMinute),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.accent
                )
            }
        }

        // --- 4. APP RESTRICTIONS CARD ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.surfaceElevated)
                .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .clickable {
                    if (isLockedByInvincible) {
                        showInvincibleLockedToast()
                    } else {
                        onOpenAppRestrictions()
                    }
                }
                .padding(spacing.m)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(colors.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        IconClock(size = 20.dp, color = colors.goldSeam)
                    }
                    Column {
                        Text(
                            text = "App Restrictions & Timers",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Configure daily usage quota and limits for individual apps.",
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                    }
                }

                IconChevronRight(color = colors.textSecondary, size = 18.dp)
            }
        }

        // --- 5. GROUP RESTRICTIONS CARD ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.surfaceElevated)
                .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .clickable {
                    if (isLockedByInvincible) {
                        showInvincibleLockedToast()
                    } else {
                        onOpenGroupRestrictions()
                    }
                }
                .padding(spacing.m)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(colors.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        IconSparkle(size = 20.dp, color = colors.accent)
                    }
                    Column {
                        Text(
                            text = "Category & Group Restrictions",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Set category overall timer across social, video, and game bundles.",
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                    }
                }

                IconChevronRight(color = colors.textSecondary, size = 18.dp)
            }
        }

        // --- 5.5 SHORT-FORM CONTENT TIMER CARD ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.surfaceElevated)
                .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .clickable {
                    if (isLockedByInvincible) {
                        showInvincibleLockedToast()
                    } else {
                        showShortsTimerPicker = true
                    }
                }
                .padding(spacing.m)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(colors.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        IconClock(size = 20.dp, color = colors.goldSeam)
                    }
                    Column {
                        Text(
                            text = "Short-Form Content Timer",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Daily Quota: ${if (shortsDailyLimit == 0) "Block Immediately" else "${shortsDailyLimit}m"} · Interruption: Every ${shortsSessionInterruption}m",
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                    }
                }

                Text(
                    text = if (shortsDailyLimit == 0) "0m" else "${shortsDailyLimit}m",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.accent
                )
            }
        }

        // --- 6. INCLUDE SHORTS TIMER CARD ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.surfaceElevated)
                .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .clickable {
                    if (isLockedByInvincible) {
                        showInvincibleLockedToast()
                    } else {
                        includeShorts = !includeShorts
                        commitInvincible(shorts = includeShorts)
                    }
                }
                .padding(spacing.m)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(colors.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("▶", fontSize = 14.sp, color = colors.accent)
                    }

                    Column {
                        Text(
                            text = "Include Shorts & Reel Interventions",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Prevents changes to shorts blocking. Max 2x 5m passes daily.",
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                    }
                }

                Checkbox(
                    checked = includeShorts,
                    enabled = !isLockedByInvincible,
                    onCheckedChange = {
                        if (isLockedByInvincible) {
                            showInvincibleLockedToast()
                        } else {
                            includeShorts = it
                            commitInvincible(shorts = it)
                        }
                    },
                    colors = CheckboxDefaults.colors(
                        checkedColor = colors.accent,
                        checkmarkColor = colors.background
                    )
                )
            }
        }

        // --- 7. INCLUDE BEDTIME CARD ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.surfaceElevated)
                .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .clickable {
                    if (isLockedByInvincible) {
                        showInvincibleLockedToast()
                    } else {
                        includeBedtime = !includeBedtime
                        commitInvincible(bedtime = includeBedtime)
                    }
                }
                .padding(spacing.m)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(colors.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        IconMoon(size = 18.dp, color = colors.accent)
                    }

                    Column {
                        Text(
                            text = "Include Bedtime Sanctuary",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Prevents changes during the active bedtime curfew.",
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                    }
                }

                Checkbox(
                    checked = includeBedtime,
                    enabled = !isLockedByInvincible,
                    onCheckedChange = {
                        if (isLockedByInvincible) {
                            showInvincibleLockedToast()
                        } else {
                            includeBedtime = it
                            commitInvincible(bedtime = it)
                        }
                    },
                    colors = CheckboxDefaults.colors(
                        checkedColor = colors.accent,
                        checkmarkColor = colors.background
                    )
                )
            }
        }

        // --- 8. PROTECTED ACCESS CARD ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.surfaceElevated)
                .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .padding(spacing.m)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(colors.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        IconLock(size = 18.dp, color = colors.accent)
                    }

                    Column {
                        Text(
                            text = "Protected Access",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Protect Kintsu with your biometric or device lock.",
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                    }
                }

                Switch(
                    checked = isProtectedAccess,
                    enabled = !isLockedByInvincible,
                    onCheckedChange = {
                        if (isLockedByInvincible) {
                            showInvincibleLockedToast()
                        } else {
                            isProtectedAccess = it
                            commitProtection(protected = it)
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = colors.accent,
                        checkedTrackColor = colors.accentSoft
                    )
                )
            }
        }

        // --- 9. TAMPER PROTECTION CARD ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.surfaceElevated)
                .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .padding(spacing.m)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(colors.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        IconShield(size = 20.dp, color = colors.accent)
                    }

                    Column {
                        Text(
                            text = "Tamper Protection",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Prevent uninstalling and force-stopping the app.",
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                    }
                }

                Switch(
                    checked = isTamperProtection,
                    enabled = !isLockedByInvincible,
                    onCheckedChange = {
                        if (isLockedByInvincible) {
                            showInvincibleLockedToast()
                        } else {
                            isTamperProtection = it
                            commitProtection(tamper = it)
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = colors.accent,
                        checkedTrackColor = colors.accentSoft
                    )
                )
            }
        }

        // --- 10. UNINSTALL WINDOW CARD ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.surfaceElevated)
                .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .clickable {
                    if (isLockedByInvincible) {
                        showInvincibleLockedToast()
                    } else {
                        showUninstallPicker = true
                    }
                }
                .padding(spacing.m)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Uninstall Window",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Tamper protection can only be disabled within 10 minutes from this time.",
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                }

                Text(
                    text = formatHourMinute(uninstallHour, uninstallMinute),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.accent
                )
            }
        }

        // --- 11. DEVICE OWNER HARDCORE SHIELD CARD ---
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
        val isDeviceOwner = dpm?.isDeviceOwnerApp(context.packageName) == true
        val adbCmd = "adb shell dpm set-device-owner com.kintsu.app/.wellbeing.receiver.KintsuDeviceAdminReceiver"

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.surfaceElevated)
                .border(
                    1.dp,
                    if (isDeviceOwner) colors.goldSeam else colors.border.copy(alpha = 0.5f),
                    RoundedCornerShape(16.dp)
                )
                .padding(spacing.m)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(spacing.s)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        IconShield(size = 20.dp, color = if (isDeviceOwner) colors.goldSeam else colors.textSecondary)
                        Text(
                            text = "Firmware Hardcore Shield",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isDeviceOwner) colors.goldSeamSoft else colors.surface)
                            .border(1.dp, if (isDeviceOwner) colors.goldSeam else colors.border, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isDeviceOwner) "ACTIVE" else "STANDARD",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDeviceOwner) colors.goldSeam else colors.textSecondary
                        )
                    }
                }

                Text(
                    text = if (isDeviceOwner)
                        "Device Owner status is active. Kintsu cannot be uninstalled, force-stopped, or bypassed even after device reboot."
                    else
                        "Elevate Kintsu to Device Owner via ADB to make it 100% uninstall-proof and unbypassable.",
                    fontSize = 12.sp,
                    color = colors.textSecondary,
                    lineHeight = 16.sp
                )

                if (!isDeviceOwner) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.surface)
                            .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .padding(spacing.s)
                    ) {
                        Text(
                            text = adbCmd,
                            fontSize = 11.sp,
                            color = colors.goldSeam,
                            lineHeight = 15.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.surface)
                            .border(1.dp, colors.border, RoundedCornerShape(10.dp))
                            .clickable {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                clipboard?.setPrimaryClip(ClipData.newPlainText("ADB Command", adbCmd))
                                Toast.makeText(context, "ADB command copied to clipboard", Toast.LENGTH_SHORT).show()
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Copy ADB Shell Command",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textPrimary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(spacing.xl))
    }

    // Premium Time Pickers
    if (showInvinciblePicker) {
        KintsuTimePickerSheet(
            initialHour = invincibleHour,
            initialMinute = invincibleMinute,
            title = "Invincible Unlock Window",
            subtitle = "Daily 10-minute window when settings modifications are permitted.",
            onDismiss = { showInvinciblePicker = false },
            onConfirm = { h, m ->
                invincibleHour = h
                invincibleMinute = m
                commitInvincible(h = h, m = m)
                showInvinciblePicker = false
            }
        )
    }

    if (showUninstallPicker) {
        KintsuTimePickerSheet(
            initialHour = uninstallHour,
            initialMinute = uninstallMinute,
            title = "Uninstall Protection Window",
            subtitle = "Daily 10-minute window when tamper protection may be altered.",
            onDismiss = { showUninstallPicker = false },
            onConfirm = { h, m ->
                uninstallHour = h
                uninstallMinute = m
                commitProtection(h = h, m = m)
                showUninstallPicker = false
            }
        )
    }

    if (showShortsTimerPicker) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showShortsTimerPicker = false },
            title = {
                Text(
                    text = "Short-Form Content Timer",
                    style = typography.itemTitle.copy(fontWeight = FontWeight.Bold),
                    color = colors.textPrimary
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(spacing.m)
                ) {
                    Text(
                        text = "Daily Quota for Reels & Shorts",
                        style = typography.caption.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.goldSeam
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(0 to "0m (Block)", 15 to "15m", 30 to "30m", 45 to "45m").forEach { (mins, label) ->
                            val isSel = shortsDailyLimit == mins
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) colors.goldSeamSoft else colors.surface)
                                    .border(1.dp, if (isSel) colors.goldSeam else colors.border, RoundedCornerShape(8.dp))
                                    .clickable { shortsDailyLimit = mins }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSel) colors.goldSeam else colors.textSecondary
                                )
                            }
                        }
                    }

                    Text(
                        text = "Mindful Interruption Frequency",
                        style = typography.caption.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.goldSeam
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(5 to "Every 5m", 10 to "Every 10m", 15 to "Every 15m").forEach { (mins, label) ->
                            val isSel = shortsSessionInterruption == mins
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) colors.goldSeamSoft else colors.surface)
                                    .border(1.dp, if (isSel) colors.goldSeam else colors.border, RoundedCornerShape(8.dp))
                                    .clickable { shortsSessionInterruption = mins }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSel) colors.goldSeam else colors.textSecondary
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        onSaveShortsTimer(shortsDailyLimit, shortsSessionInterruption)
                        showShortsTimerPicker = false
                    }
                ) {
                    Text("Save Timer", color = colors.goldSeam, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showShortsTimerPicker = false }) {
                    Text("Cancel", color = colors.textSecondary)
                }
            },
            containerColor = colors.surfaceElevated
        )
    }
}
