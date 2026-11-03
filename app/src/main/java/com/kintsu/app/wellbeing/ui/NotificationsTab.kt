package com.kintsu.app.wellbeing.ui

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.kintsu.app.ui.components.IconDevice
import com.kintsu.app.ui.components.IconMoon
import com.kintsu.app.ui.components.IconNotifications
import com.kintsu.app.ui.components.IconSun
import com.kintsu.app.ui.theme.KintsuTheme
import com.kintsu.app.wellbeing.domain.PermissionStateHelper
import com.kintsu.app.wellbeing.service.KintsuNotificationListenerService

@Composable
fun NotificationsTab(
    settings: KintsuSettings,
    onSaveNotificationBatching: (
        enabled: Boolean,
        storeAll: Boolean,
        historyDays: Int,
        recapType: String,
        morning: Boolean,
        afternoon: Boolean,
        evening: Boolean
    ) -> Unit
) {
    val context = LocalContext.current
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography

    val totalNotifications by KintsuNotificationListenerService.totalNotificationsToday.collectAsState()
    val batchedApps by KintsuNotificationListenerService.batchedAppsCount.collectAsState()
    val isAccessGranted = remember { PermissionStateHelper.isNotificationListenerGranted(context) }

    var showHistoryDialog by remember { mutableStateOf(false) }
    var showRecapDialog by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(spacing.m)) {
        // --- 1. HEADER (Title "Notifications" & "Work in progress" badge) ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Notifications",
                style = typography.screenTitle.copy(fontSize = 22.sp, fontWeight = FontWeight.Bold),
                color = colors.textPrimary
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF5A2A2A))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "Work in progress",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF8A80)
                )
            }
        }

        Text(
            text = "Batch notification from apps and set schedules like morning, noon, evening and night. Stay updated without constant interruptions.",
            style = typography.caption,
            color = colors.textSecondary
        )

        // --- 2. TOP STAT CARDS (2 COLUMNS: Notifications count, Batched apps count) ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.surfaceElevated)
                    .border(1.dp, colors.border.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconNotifications(color = colors.textSecondary, size = 18.dp)
                    Text("Notifications", fontSize = 11.sp, color = colors.textSecondary)
                    Text("$totalNotifications", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.surfaceElevated)
                    .border(1.dp, colors.border.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconDevice(color = colors.textSecondary, size = 18.dp)
                    Text("Batched apps", fontSize = 11.sp, color = colors.textSecondary)
                    Text("$batchedApps", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                }
            }
        }

        // --- 3. NOTIFICATION ACCESS PERMISSION CARD (If not granted) ---
        if (!isAccessGranted) {
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
                        IconNotifications(color = colors.goldSeam, size = 20.dp)
                        Text(
                            text = "Notification Access",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                    }
                    Text(
                        text = "Kintsu requires notification access to batch and schedule notifications.",
                        style = typography.caption,
                        color = colors.textSecondary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(colors.accent)
                                .clickable {
                                    context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
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

        // --- 4. NOTIFICATION SETTINGS (Image 3) ---
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Store all notifications toggle
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
                        Text("Store all notifications", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                        Text("Save non-batched notifications too.", fontSize = 11.sp, color = colors.textSecondary)
                    }
                    Switch(
                        checked = settings.storeAllNotifications,
                        onCheckedChange = {
                            onSaveNotificationBatching(
                                settings.notificationBatchingEnabled,
                                it,
                                settings.notificationHistoryDays,
                                settings.notificationRecapType,
                                settings.notificationScheduleMorning,
                                settings.notificationScheduleAfternoon,
                                settings.notificationScheduleEvening
                            )
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = colors.accent,
                            checkedTrackColor = colors.accentSoft
                        )
                    )
                }
            }

            // Notification history dropdown
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.surfaceElevated)
                    .border(1.dp, colors.border.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .clickable { showHistoryDialog = true }
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Notification history", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                        Text("${settings.notificationHistoryDays} days", fontSize = 11.sp, color = colors.textSecondary)
                    }
                    IconChevronRight(color = colors.textSecondary, size = 16.dp)
                }
            }

            // Batch recap type dropdown
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.surfaceElevated)
                    .border(1.dp, colors.border.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .clickable { showRecapDialog = true }
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Batch recap type", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                        Text(
                            text = if (settings.notificationRecapType == "SUMMARY_ONLY") "Summary only" else "Full digest",
                            fontSize = 11.sp,
                            color = colors.textSecondary
                        )
                    }
                    IconChevronRight(color = colors.textSecondary, size = 16.dp)
                }
            }
        }

        // --- 5. SCHEDULES (Image 3) ---
        Text(
            text = "Schedules",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.accent
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Morning 8:00 AM
            ScheduleRow(
                icon = { IconSun(color = colors.goldSeam, size = 20.dp) },
                period = "Morning",
                time = "8:00 AM",
                isChecked = settings.notificationScheduleMorning,
                onCheckedChange = {
                    onSaveNotificationBatching(
                        settings.notificationBatchingEnabled,
                        settings.storeAllNotifications,
                        settings.notificationHistoryDays,
                        settings.notificationRecapType,
                        it,
                        settings.notificationScheduleAfternoon,
                        settings.notificationScheduleEvening
                    )
                }
            )

            // Afternoon 12:00 PM
            ScheduleRow(
                icon = { IconSun(color = colors.goldSeam, size = 20.dp) },
                period = "Afternoon",
                time = "12:00 PM",
                isChecked = settings.notificationScheduleAfternoon,
                onCheckedChange = {
                    onSaveNotificationBatching(
                        settings.notificationBatchingEnabled,
                        settings.storeAllNotifications,
                        settings.notificationHistoryDays,
                        settings.notificationRecapType,
                        settings.notificationScheduleMorning,
                        it,
                        settings.notificationScheduleEvening
                    )
                }
            )

            // Evening 8:00 PM
            ScheduleRow(
                icon = { IconMoon(color = colors.accent, size = 20.dp) },
                period = "Evening",
                time = "8:00 PM",
                isChecked = settings.notificationScheduleEvening,
                onCheckedChange = {
                    onSaveNotificationBatching(
                        settings.notificationBatchingEnabled,
                        settings.storeAllNotifications,
                        settings.notificationHistoryDays,
                        settings.notificationRecapType,
                        settings.notificationScheduleMorning,
                        settings.notificationScheduleAfternoon,
                        it
                    )
                }
            )
        }
    }

    // History Dialog
    if (showHistoryDialog) {
        val daysList = listOf(7, 15, 30)
        AlertDialog(
            onDismissRequest = { showHistoryDialog = false },
            title = { Text("Notification History Retention", style = typography.itemTitle, color = colors.textPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    daysList.forEach { d ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (settings.notificationHistoryDays == d) colors.accentSoft else colors.surface)
                                .clickable {
                                    onSaveNotificationBatching(
                                        settings.notificationBatchingEnabled,
                                        settings.storeAllNotifications,
                                        d,
                                        settings.notificationRecapType,
                                        settings.notificationScheduleMorning,
                                        settings.notificationScheduleAfternoon,
                                        settings.notificationScheduleEvening
                                    )
                                    showHistoryDialog = false
                                }
                                .padding(14.dp)
                        ) {
                            Text(
                                text = "$d days",
                                fontSize = 14.sp,
                                fontWeight = if (settings.notificationHistoryDays == d) FontWeight.Bold else FontWeight.Normal,
                                color = if (settings.notificationHistoryDays == d) colors.accent else colors.textPrimary
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showHistoryDialog = false }) {
                    Text("Close", color = colors.textSecondary)
                }
            },
            containerColor = colors.surfaceElevated,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Recap Dialog
    if (showRecapDialog) {
        val types = listOf("SUMMARY_ONLY" to "Summary only", "FULL_DIGEST" to "Full digest")
        AlertDialog(
            onDismissRequest = { showRecapDialog = false },
            title = { Text("Batch Recap Type", style = typography.itemTitle, color = colors.textPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    types.forEach { (typeKey, label) ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (settings.notificationRecapType == typeKey) colors.accentSoft else colors.surface)
                                .clickable {
                                    onSaveNotificationBatching(
                                        settings.notificationBatchingEnabled,
                                        settings.storeAllNotifications,
                                        settings.notificationHistoryDays,
                                        typeKey,
                                        settings.notificationScheduleMorning,
                                        settings.notificationScheduleAfternoon,
                                        settings.notificationScheduleEvening
                                    )
                                    showRecapDialog = false
                                }
                                .padding(14.dp)
                        ) {
                            Text(
                                text = label,
                                fontSize = 14.sp,
                                fontWeight = if (settings.notificationRecapType == typeKey) FontWeight.Bold else FontWeight.Normal,
                                color = if (settings.notificationRecapType == typeKey) colors.accent else colors.textPrimary
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showRecapDialog = false }) {
                    Text("Close", color = colors.textSecondary)
                }
            },
            containerColor = colors.surfaceElevated,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun ScheduleRow(
    icon: @Composable () -> Unit,
    period: String,
    time: String,
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
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                icon()
                Column {
                    Text(period, fontSize = 11.sp, color = colors.textSecondary)
                    Text(time, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                }
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
