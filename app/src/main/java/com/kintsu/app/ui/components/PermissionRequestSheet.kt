package com.kintsu.app.ui.components

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.kintsu.app.ui.theme.KintsuTheme
import com.kintsu.app.util.PermissionHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionRequestSheet(
    onDismissRequest: () -> Unit,
    onContinue: () -> Unit
) {
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasNotifications by remember {
        mutableStateOf(PermissionHelper.isNotificationPermissionGranted(context))
    }
    var hasExactAlarms by remember {
        mutableStateOf(PermissionHelper.isExactAlarmPermissionGranted(context))
    }
    var isBatteryUnrestricted by remember {
        mutableStateOf(PermissionHelper.isBatteryOptimizationIgnored(context))
    }

    // Refresh permission statuses on resume when returning from system settings
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasNotifications = PermissionHelper.isNotificationPermissionGranted(context)
                hasExactAlarms = PermissionHelper.isExactAlarmPermissionGranted(context)
                isBatteryUnrestricted = PermissionHelper.isBatteryOptimizationIgnored(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasNotifications = granted
    }

    KintsuBottomSheet(
        onDismissRequest = onDismissRequest,
        title = "Permissions & System Access"
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = spacing.s),
            verticalArrangement = Arrangement.spacedBy(spacing.m)
        ) {
            Text(
                text = "Kintsu is an intentional, privacy-first companion. To help you maintain consistency and flow without unexpected interruptions, Kintsu benefits from three core Android system permissions.",
                style = typography.body,
                color = colors.textSecondary
            )

            // 1. Mindful Notifications
            PermissionCard(
                title = "Mindful Notifications",
                description = "Gentle alerts for scheduled habits, daily intentions, and timer intervals. Never noisy, never spammy.",
                isGranted = hasNotifications,
                statusText = if (hasNotifications) "Allowed" else "Not Allowed",
                actionText = if (hasNotifications) "Settings" else "Allow Notifications",
                onAction = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotifications) {
                        notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        PermissionHelper.openAppNotificationSettings(context)
                    }
                }
            )

            // 2. Precise Timing & Alarms
            PermissionCard(
                title = "Exact Alarms & Timing",
                description = "Ensures scheduled rituals and reviews trigger at your exact chosen minute without Android battery delays.",
                isGranted = hasExactAlarms,
                statusText = if (hasExactAlarms) "Granted" else "Delayed by System",
                actionText = if (hasExactAlarms) "Configured" else "Enable Exact Alarms",
                onAction = {
                    PermissionHelper.openExactAlarmSettings(context)
                }
            )

            // 3. Unrestricted Focus & Sync
            PermissionCard(
                title = "Unrestricted Background Work",
                description = "Prevents aggressive battery savers from interrupting active focus timers and cloud sync when your screen is off.",
                isGranted = isBatteryUnrestricted,
                statusText = if (isBatteryUnrestricted) "Unrestricted" else "Optimized",
                actionText = if (isBatteryUnrestricted) "Allowed" else "Allow Background",
                onAction = {
                    PermissionHelper.openBatteryOptimizationSettings(context)
                }
            )

            Spacer(modifier = Modifier.height(spacing.s))

            KintsuButton(
                text = "Continue to Sanctuary",
                onClick = onContinue,
                variant = KintsuButtonVariant.PRIMARY,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun PermissionCard(
    title: String,
    description: String,
    isGranted: Boolean,
    statusText: String,
    actionText: String,
    onAction: () -> Unit
) {
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography

    KintsuCard(modifier = Modifier.fillMaxWidth()) {
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
                Text(
                    text = title,
                    style = typography.itemTitle,
                    color = colors.textPrimary,
                    modifier = Modifier.weight(1f)
                )

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (isGranted) colors.accentSoft else colors.surfaceElevated)
                        .padding(horizontal = spacing.s, vertical = 4.dp)
                ) {
                    Text(
                        text = statusText,
                        style = typography.caption,
                        fontWeight = FontWeight.Medium,
                        color = if (isGranted) colors.accent else colors.textTertiary
                    )
                }
            }

            Text(
                text = description,
                style = typography.caption,
                color = colors.textSecondary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                KintsuButton(
                    text = actionText,
                    onClick = onAction,
                    variant = if (isGranted) KintsuButtonVariant.SECONDARY else KintsuButtonVariant.ACCENT_SOFT
                )
            }
        }
    }
}
