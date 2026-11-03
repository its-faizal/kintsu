package com.kintsu.app.wellbeing.ui

import android.app.TimePickerDialog
import android.content.Intent
import android.provider.Settings
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
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kintsu.app.data.KintsuSettings
import com.kintsu.app.ui.components.IconChevronRight
import com.kintsu.app.ui.components.IconMoon
import com.kintsu.app.ui.theme.KintsuTheme
import java.util.Locale

@Composable
fun BedtimeTab(
    settings: KintsuSettings,
    onSaveBedtimeSchedule: (enabled: Boolean, startHour: Int, endHour: Int, daysMask: Int, dnd: Boolean) -> Unit,
    onOpenDistractingApps: () -> Unit
) {
    val context = LocalContext.current
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography

    var startHour by remember(settings.bedtimeStartHour) { mutableIntStateOf(settings.bedtimeStartHour) }
    var startMinute by remember { mutableIntStateOf(0) }
    var endHour by remember(settings.bedtimeEndHour) { mutableIntStateOf(settings.bedtimeEndHour) }
    var endMinute by remember { mutableIntStateOf(0) }
    var daysMask by remember(settings.bedtimeDaysMask) { mutableIntStateOf(settings.bedtimeDaysMask) }
    var isScheduleEnabled by remember(settings.bedtimeLockEnabled) { mutableStateOf(settings.bedtimeLockEnabled) }
    var isDndEnabled by remember(settings.bedtimeDndEnabled) { mutableStateOf(settings.bedtimeDndEnabled) }

    fun commitSchedule(
        newEnabled: Boolean = isScheduleEnabled,
        newStart: Int = startHour,
        newEnd: Int = endHour,
        newMask: Int = daysMask,
        newDnd: Boolean = isDndEnabled
    ) {
        onSaveBedtimeSchedule(newEnabled, newStart, newEnd, newMask, newDnd)
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

    // Calculate duration in hours/minutes
    val durationText = remember(startHour, startMinute, endHour, endMinute) {
        val startTotalMin = startHour * 60 + startMinute
        val endTotalMin = endHour * 60 + endMinute
        val diffMin = if (endTotalMin >= startTotalMin) {
            endTotalMin - startTotalMin
        } else {
            (24 * 60 - startTotalMin) + endTotalMin
        }
        val hrs = diffMin / 60
        val mins = diffMin % 60
        when {
            hrs == 0 && mins == 0 -> "0 second"
            mins == 0 -> "$hrs hours"
            hrs == 0 -> "$mins minutes"
            else -> "$hrs hours, $mins minutes"
        }
    }

    val daysList = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    Column(
        verticalArrangement = Arrangement.spacedBy(spacing.m),
        modifier = Modifier.fillMaxWidth()
    ) {
        // --- SCREEN TITLE & SUBTITLE ---
        Text(
            text = "Bedtime",
            style = typography.screenTitle.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold),
            color = colors.textPrimary
        )
        Text(
            text = "Set your bedtime schedule by selecting a time period and days of the week. Choose distracting apps to block and enable Do Not Disturb (DND) mode for a peaceful night.",
            style = typography.caption.copy(lineHeight = 18.sp),
            color = colors.textSecondary
        )

        // --- SECTION: SCHEDULE ---
        Text(
            text = "Schedule",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = colors.accent
        )

        // Main Schedule Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.surfaceElevated)
                .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .padding(spacing.l)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Start & End Times
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Start Time
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                TimePickerDialog(
                                    context,
                                    { _, pickedH, pickedM ->
                                        startHour = pickedH
                                        startMinute = pickedM
                                        commitSchedule(newStart = pickedH)
                                    },
                                    startHour,
                                    startMinute,
                                    false
                                ).show()
                            }
                            .padding(4.dp)
                    ) {
                        Text(
                            text = "Start",
                            fontSize = 11.sp,
                            color = colors.textTertiary,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = formatHourMinute(startHour, startMinute),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                    }

                    // End Time
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                TimePickerDialog(
                                    context,
                                    { _, pickedH, pickedM ->
                                        endHour = pickedH
                                        endMinute = pickedM
                                        commitSchedule(newEnd = pickedH)
                                    },
                                    endHour,
                                    endMinute,
                                    false
                                ).show()
                            }
                            .padding(4.dp)
                    ) {
                        Text(
                            text = "End",
                            fontSize = 11.sp,
                            color = colors.textTertiary,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = formatHourMinute(endHour, endMinute),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                    }
                }

                // Duration Indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = colors.border.copy(alpha = 0.4f))
                    Text(
                        text = durationText,
                        fontSize = 11.sp,
                        color = colors.textSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = colors.border.copy(alpha = 0.4f))
                }

                // Circular Day Selector Pills (Mon - Sun)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    daysList.forEachIndexed { index, dayName ->
                        val isDaySelected = (daysMask and (1 shl index)) != 0
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(if (isDaySelected) colors.accentSoft else Color.Transparent)
                                .border(
                                    width = 1.dp,
                                    color = if (isDaySelected) colors.accent else colors.border.copy(alpha = 0.5f),
                                    shape = CircleShape
                                )
                                .clickable {
                                    val newMask = daysMask xor (1 shl index)
                                    daysMask = newMask
                                    commitSchedule(newMask = newMask)
                                }
                                .semantics { role = Role.Checkbox },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dayName,
                                fontSize = 11.sp,
                                fontWeight = if (isDaySelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isDaySelected) colors.accent else colors.textSecondary
                            )
                        }
                    }
                }
            }
        }

        // Schedule Enable Switch Card
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
                        IconMoon(size = 20.dp, color = colors.accent)
                    }

                    Column {
                        Text(
                            text = "Schedule",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Enable or disable daily schedule.",
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                    }
                }

                Switch(
                    checked = isScheduleEnabled,
                    onCheckedChange = {
                        isScheduleEnabled = it
                        commitSchedule(newEnabled = it)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = colors.accent,
                        checkedTrackColor = colors.accentSoft
                    )
                )
            }
        }

        // --- SECTION: QUICK ACTIONS ---
        Text(
            text = "Quick actions",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = colors.accent
        )

        // Start DND Card
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Start DND",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Also enable Do Not Disturb mode.",
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                }

                Switch(
                    checked = isDndEnabled,
                    onCheckedChange = {
                        isDndEnabled = it
                        commitSchedule(newDnd = it)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = colors.accent,
                        checkedTrackColor = colors.accentSoft
                    )
                )
            }
        }

        // Do Not Disturb Settings Link Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.surfaceElevated)
                .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .clickable {
                    try {
                        context.startActivity(Intent(Settings.ACTION_ZEN_MODE_PRIORITY_SETTINGS))
                    } catch (_: Exception) {
                        try {
                            context.startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
                        } catch (_: Exception) {}
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
                        IconMoon(size = 18.dp, color = colors.textSecondary)
                    }

                    Column {
                        Text(
                            text = "Do not disturb settings",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Manage which apps and notifications can reach you in DND.",
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                    }
                }

                IconChevronRight(color = colors.textSecondary, size = 18.dp)
            }
        }

        // Distracting Apps Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.surfaceElevated)
                .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .clickable { onOpenDistractingApps() }
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
                        IconMoon(size = 18.dp, color = colors.textSecondary)
                    }

                    Column {
                        Text(
                            text = "Distracting apps",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Select which apps are distracting you from your bedtime routine.",
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                    }
                }

                // Diagonal arrow icon (rendered cleanly)
                Text(
                    text = "↗",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(spacing.xl))
    }
}
