package com.kintsu.app.wellbeing.domain

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.net.VpnService
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.core.app.NotificationManagerCompat
import com.kintsu.app.wellbeing.service.KintsuAccessibilityBlockerService

data class PermissionDetail(
    val id: String,
    val title: String,
    val subtitle: String,
    val isGranted: Boolean,
    val isOptional: Boolean,
    val whyNeeded: String,
    val featuresDependent: String,
    val informationAccessed: String,
    val openSettingsIntent: (Context) -> Intent?
)

object PermissionStateHelper {

    fun isAccessibilityEnabled(context: Context): Boolean {
        if (KintsuAccessibilityBlockerService.isServiceRunning) return true
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager ?: return false
        val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        return enabledServices.any {
            it.resolveInfo?.serviceInfo?.packageName == context.packageName
        }
    }

    fun isOverlayGranted(context: Context): Boolean {
        return Settings.canDrawOverlays(context)
    }

    fun isVpnPrepared(context: Context): Boolean {
        return VpnService.prepare(context) == null
    }

    fun isNotificationsGranted(context: Context): Boolean {
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    fun isBatteryOptimizationIgnored(context: Context): Boolean {
        val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager ?: return false
        return pm.isIgnoringBatteryOptimizations(context.packageName)
    }

    fun isNotificationListenerGranted(context: Context): Boolean {
        val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
        return flat != null && flat.contains(context.packageName)
    }

    fun isDndAccessGranted(context: Context): Boolean {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? android.app.NotificationManager ?: return false
        return nm.isNotificationPolicyAccessGranted
    }

    fun getAllPermissionDetails(context: Context): List<PermissionDetail> {
        val usageGranted = ScreenTimeHelper.hasUsageStatsPermission(context)
        val accessibilityGranted = isAccessibilityEnabled(context)
        val overlayGranted = isOverlayGranted(context)
        val vpnGranted = isVpnPrepared(context)
        val notifGranted = isNotificationsGranted(context)
        val batteryGranted = isBatteryOptimizationIgnored(context)

        return listOf(
            PermissionDetail(
                id = "USAGE_ACCESS",
                title = "App Usage Access",
                subtitle = "Measure screen time and monitor app limit allowances",
                isGranted = usageGranted,
                isOptional = false,
                whyNeeded = "Required to accurately track daily screen time, foreground app minutes, and enforce time budgets.",
                featuresDependent = "Screen Time Dashboard, Daily App Limits, Distraction Insights.",
                informationAccessed = "Only foreground duration of installed applications. Never private content, messages, or keystrokes.",
                openSettingsIntent = {
                    Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                }
            ),
            PermissionDetail(
                id = "ACCESSIBILITY",
                title = "App Intervention Service",
                subtitle = "Detect launches of distracting apps to display mindful breathing",
                isGranted = accessibilityGranted,
                isOptional = false,
                whyNeeded = "Allows Kintsu to recognize when you open a blocked or restricted app and offer a mindful pause.",
                featuresDependent = "Mindful Breathing Interventions, Focus Lock sessions, Scheduled App Blocking.",
                informationAccessed = "Only window state changes (package names) of foreground apps. Excludes emergency and system tools.",
                openSettingsIntent = {
                    Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                }
            ),
            PermissionDetail(
                id = "OVERLAY",
                title = "Display Over Other Apps",
                subtitle = "Present peaceful full-screen breathing and pause screens",
                isGranted = overlayGranted,
                isOptional = true,
                whyNeeded = "Allows the calm breathing shield to appear immediately when a restricted application is opened.",
                featuresDependent = "Breathing Intervention Overlay, Strict Limit Screen.",
                informationAccessed = "None. Only authorizes rendering a visual overlay over foreground apps.",
                openSettingsIntent = { ctx ->
                    Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${ctx.packageName}")
                    ).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                }
            ),
            PermissionDetail(
                id = "VPN_DNS",
                title = "Local DNS Filtering (VPN)",
                subtitle = "Block distracting websites locally without routing traffic off-device",
                isGranted = vpnGranted,
                isOptional = true,
                whyNeeded = "Filters domain lookups on-device using a local loopback so specified distracting websites cannot connect.",
                featuresDependent = "Website Blocking, Domain Filter, Focus Lock Web Restriction.",
                informationAccessed = "Only requested domain names for DNS lookup. 100% processed locally on device; no proxy servers used.",
                openSettingsIntent = { ctx ->
                    VpnService.prepare(ctx)?.apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                }
            ),
            PermissionDetail(
                id = "NOTIFICATIONS",
                title = "Notifications & Reminders",
                subtitle = "Usage limit alerts, break countdowns, and Focus updates",
                isGranted = notifGranted,
                isOptional = true,
                whyNeeded = "Delivers gentle heads-up notifications when limits are approaching and Focus Lock completes.",
                featuresDependent = "Focus HUD, Limit Warnings, Session Completion.",
                informationAccessed = "None.",
                openSettingsIntent = { ctx ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                            putExtra(Settings.EXTRA_APP_PACKAGE, ctx.packageName)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                    } else {
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.parse("package:${ctx.packageName}")
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                    }
                }
            ),
            PermissionDetail(
                id = "BATTERY",
                title = "Background Protection",
                subtitle = "Ensure schedule transitions occur smoothly when the screen is off",
                isGranted = batteryGranted,
                isOptional = true,
                whyNeeded = "Prevents Android from putting Kintsu's scheduled focus alarms to sleep during long focus periods.",
                featuresDependent = "Automatic Blocking Schedules, Overnight Sleep Routines.",
                informationAccessed = "None.",
                openSettingsIntent = { ctx ->
                    Intent(
                        Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                        Uri.parse("package:${ctx.packageName}")
                    ).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                }
            )
        )
    }
}
