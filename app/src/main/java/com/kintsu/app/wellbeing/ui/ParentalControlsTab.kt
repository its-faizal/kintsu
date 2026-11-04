package com.kintsu.app.wellbeing.ui

import android.app.TimePickerDialog
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.kintsu.app.ui.components.IconLock
import com.kintsu.app.ui.components.IconMoon
import com.kintsu.app.ui.components.IconShield
import com.kintsu.app.ui.theme.KintsuTheme
import java.util.Locale

@Composable
fun ParentalControlsScreen(
    settings: KintsuSettings,
    onBack: () -> Unit,
    onSaveInvincibleMode: (enabled: Boolean, windowHour: Int, windowMin: Int, includeShorts: Boolean, includeBedtime: Boolean) -> Unit,
    onSaveProtection: (protectedAccess: Boolean, tamperProtection: Boolean, uninstallHour: Int, uninstallMin: Int) -> Unit,
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

    fun isWithin10MinuteWindow(windowHour: Int, windowMinute: Int): Boolean {
        val cal = java.util.Calendar.getInstance()
        val currentHour = cal.get(java.util.Calendar.HOUR_OF_DAY)
        val currentMin = cal.get(java.util.Calendar.MINUTE)
        val currentTotalMin = currentHour * 60 + currentMin
        val windowStart = windowHour * 60 + windowMinute
        val windowEnd = windowStart + 10
        return currentTotalMin in windowStart until windowEnd
    }

    fun formatHourMinute(h: Int, m: Int): String {
        val amPm = if (h < 12) "AM" else "PM"
        val displayH = when {
            h == 0 -> 12
            h > 12 -> h - 12
            else -> h
        }
        return String.format(Locale.US, "%d:%02d %s", displayH, m, amPm)
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(spacing.m),
        modifier = Modifier.fillMaxWidth()
    ) {
        // --- TOP BAR WITH BACK ARROW & TITLE ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(colors.surfaceElevated)
                    .clickable { onBack() }
                    .semantics { role = Role.Button },
                contentAlignment = Alignment.Center
            ) {
                IconBack(size = 18.dp, color = colors.textPrimary)
            }
            Spacer(modifier = Modifier.width(spacing.m))
            Text(
                text = "Parental controls",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
        }

        Text(
            text = "When Invincible Mode is on, you won't be able to adjust selected limits after reaching your daily quota. However, you can make changes within a selected 10-minute invincible window.",
            style = typography.caption.copy(lineHeight = 18.sp),
            color = colors.textSecondary
        )

        // 1. Activate Invincible Mode Card
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
                    Text(
                        text = "Activate invincible mode",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                }

                Switch(
                    checked = isInvincibleEnabled,
                    onCheckedChange = {
                        if (!it && settings.invincibleModeEnabled && !isWithin10MinuteWindow(settings.invincibleWindowHour, settings.invincibleWindowMinute)) {
                            android.widget.Toast.makeText(
                                context,
                                "Invincible Mode is active. Changes can only be made during your daily 10-minute window (${formatHourMinute(settings.invincibleWindowHour, settings.invincibleWindowMinute)} - ${formatHourMinute(settings.invincibleWindowHour, settings.invincibleWindowMinute + 10)}).",
                                android.widget.Toast.LENGTH_LONG
                            ).show()
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

        // 2. Invincible Window Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.surfaceElevated)
                .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .clickable {
                    if (settings.invincibleModeEnabled && !isWithin10MinuteWindow(settings.invincibleWindowHour, settings.invincibleWindowMinute)) {
                        android.widget.Toast.makeText(
                            context,
                            "Window changes locked by Invincible Mode until ${formatHourMinute(settings.invincibleWindowHour, settings.invincibleWindowMinute)}.",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        TimePickerDialog(
                            context,
                            { _, h, m ->
                                invincibleHour = h
                                invincibleMinute = m
                                commitInvincible(h = h, m = m)
                            },
                            invincibleHour,
                            invincibleMinute,
                            false
                        ).show()
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
                        text = "Invincible window",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Selected limits can be modified within 10 minutes from the selected time.",
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

        // 3. App Restrictions Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.surfaceElevated)
                .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .clickable { onOpenAppRestrictions() }
                .padding(spacing.m)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "App restrictions",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Prevent changes to the app's selected restrictions once the daily limits are exceeded.",
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                }

                IconChevronRight(color = colors.textSecondary, size = 18.dp)
            }
        }

        // 4. Group Restrictions Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.surfaceElevated)
                .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .clickable { onOpenGroupRestrictions() }
                .padding(spacing.m)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Group restrictions",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Prevent changes to the group's selected restrictions once the daily limits are exceeded.",
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                }

                IconChevronRight(color = colors.textSecondary, size = 18.dp)
            }
        }

        // 5. Include Shorts Timer Checkbox Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.surfaceElevated)
                .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .clickable {
                    includeShorts = !includeShorts
                    commitInvincible(shorts = includeShorts)
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
                            text = "Include shorts timer",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Prevents changes after reaching your daily shorts limit.",
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                    }
                }

                Checkbox(
                    checked = includeShorts,
                    onCheckedChange = {
                        includeShorts = it
                        commitInvincible(shorts = it)
                    },
                    colors = CheckboxDefaults.colors(
                        checkedColor = colors.accent,
                        checkmarkColor = colors.background
                    )
                )
            }
        }

        // 6. Include Bedtime Checkbox Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.surfaceElevated)
                .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .clickable {
                    includeBedtime = !includeBedtime
                    commitInvincible(bedtime = includeBedtime)
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
                            text = "Include bedtime",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Prevents changes during the active bedtime schedule.",
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                    }
                }

                Checkbox(
                    checked = includeBedtime,
                    onCheckedChange = {
                        includeBedtime = it
                        commitInvincible(bedtime = it)
                    },
                    colors = CheckboxDefaults.colors(
                        checkedColor = colors.accent,
                        checkmarkColor = colors.background
                    )
                )
            }
        }

        // --- SECTION: PARENTAL CONTROLS ---
        Text(
            text = "Parental controls",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = colors.accent
        )

        // 7. Protected Access Card
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
                            text = "Protected access",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Protect Mindful with your device lock.",
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                    }
                }

                Switch(
                    checked = isProtectedAccess,
                    onCheckedChange = {
                        isProtectedAccess = it
                        commitProtection(protected = it)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = colors.accent,
                        checkedTrackColor = colors.accentSoft
                    )
                )
            }
        }

        // 8. Tamper Protection Card
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
                            text = "Tamper protection",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Prevent uninstalling and force stopping the app.",
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                    }
                }

                Switch(
                    checked = isTamperProtection,
                    onCheckedChange = {
                        if (!it && settings.tamperProtectionEnabled && !isWithin10MinuteWindow(settings.uninstallWindowHour, settings.uninstallWindowMinute)) {
                            android.widget.Toast.makeText(
                                context,
                                "Tamper protection is active. Changes can only be made during your daily 10-minute window (${formatHourMinute(settings.uninstallWindowHour, settings.uninstallWindowMinute)} - ${formatHourMinute(settings.uninstallWindowHour, settings.uninstallWindowMinute + 10)}).",
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                            return@Switch
                        }
                        isTamperProtection = it
                        commitProtection(tamper = it)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = colors.accent,
                        checkedTrackColor = colors.accentSoft
                    )
                )
            }
        }

        // 9. Uninstall Window Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.surfaceElevated)
                .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .clickable {
                    if (settings.tamperProtectionEnabled && !isWithin10MinuteWindow(settings.uninstallWindowHour, settings.uninstallWindowMinute)) {
                        android.widget.Toast.makeText(
                            context,
                            "Uninstall window changes locked until ${formatHourMinute(settings.uninstallWindowHour, settings.uninstallWindowMinute)}.",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        TimePickerDialog(
                            context,
                            { _, h, m ->
                                uninstallHour = h
                                uninstallMinute = m
                                commitProtection(h = h, m = m)
                            },
                            uninstallHour,
                            uninstallMinute,
                            false
                        ).show()
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
                        text = "Uninstall window",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Tamper protection can be disabled within 10 minutes from the selected time.",
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

        Spacer(modifier = Modifier.height(spacing.xl))
    }
}
