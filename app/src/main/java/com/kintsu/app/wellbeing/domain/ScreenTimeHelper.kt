package com.kintsu.app.wellbeing.domain

import android.app.AppOpsManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.app.usage.UsageEvents
import android.os.Build
import android.content.Context
import android.os.Process
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar

object ScreenTimeHelper {

    fun hasUsageStatsPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager ?: return false
        val mode = appOps.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    /**
     * Returns total foreground screen time in minutes for today across all non-protected apps.
     */
    fun getScreenTimeTodayMinutes(context: Context): Long {
        if (!hasUsageStatsPermission(context)) return 0L
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager ?: return 0L

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = calendar.timeInMillis
        val now = System.currentTimeMillis()

        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startOfDay,
            now
        ) ?: return 0L

        var totalMillis = 0L
        for (usage in stats) {
            val pkg = usage.packageName ?: continue
            if (!ProtectedApps.isProtected(pkg)) {
                totalMillis += usage.totalTimeInForeground
            }
        }
        return totalMillis / (1000 * 60)
    }

    /**
     * Returns map of packageName to foreground minutes used today.
     */
    fun getPerAppUsageTodayMinutes(context: Context): Map<String, Long> {
        if (!hasUsageStatsPermission(context)) return emptyMap()
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager ?: return emptyMap()

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = calendar.timeInMillis
        val now = System.currentTimeMillis()

        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startOfDay,
            now
        ) ?: return emptyMap()

        val result = mutableMapOf<String, Long>()
        for (usage in stats) {
            val pkg = usage.packageName ?: continue
            val mins = usage.totalTimeInForeground / (1000 * 60)
            if (mins > 0) {
                result[pkg] = (result[pkg] ?: 0L) + mins
            }
        }
        return result
    }

    fun getPackageUsageTodayMinutes(context: Context, packageName: String): Long {
        return getPerAppUsageTodayMinutes(context)[packageName] ?: 0L
    }

    /**
     * Returns a list of daily screen time in minutes for each of the last 7 days (including today).
     */
    fun getWeeklyScreenTimeTrends(context: Context): List<Pair<String, Long>> {
        val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        if (!hasUsageStatsPermission(context)) {
            return days.map { it to 0L }
        }

        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return days.map { it to 0L }

        val today = LocalDate.now()
        val result = mutableListOf<Pair<String, Long>>()

        for (i in 6 downTo 0) {
            val date = today.minusDays(i.toLong())
            val dayName = date.dayOfWeek.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
            val startMillis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val endMillis = if (i == 0) System.currentTimeMillis() else date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

            val stats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                startMillis,
                endMillis
            )
            var totalMin = 0L
            if (stats != null) {
                for (u in stats) {
                    if (!ProtectedApps.isProtected(u.packageName ?: "")) {
                        totalMin += (u.totalTimeInForeground / (1000 * 60))
                    }
                }
            }
            result.add(dayName to totalMin)
        }
        return result
    }

    /**
     * Measures app launch frequency today using actual Android UsageEvents.
     */
    fun getAppLaunchCountsToday(context: Context): Map<String, Int> {
        if (!hasUsageStatsPermission(context)) return emptyMap()
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager ?: return emptyMap()

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = calendar.timeInMillis
        val now = System.currentTimeMillis()

        val events = usageStatsManager.queryEvents(startOfDay, now) ?: return emptyMap()
        val event = UsageEvents.Event()
        val counts = mutableMapOf<String, Int>()

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                val pkg = event.packageName ?: continue
                if (!ProtectedApps.isProtected(pkg)) {
                    counts[pkg] = (counts[pkg] ?: 0) + 1
                }
            }
        }
        return counts
    }

    /**
     * Calculates late-night screen time minutes (between 11:00 PM and 5:00 AM).
     */
    fun getLateNightScreenTimeMinutes(context: Context): Long {
        if (!hasUsageStatsPermission(context)) return 0L
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager ?: return 0L

        val cal = Calendar.getInstance()
        val currentHour = cal.get(Calendar.HOUR_OF_DAY)

        // If currently between 0 and 5 AM, query from 11 PM yesterday to now
        // If past 11 PM, query from 11 PM today to now
        val calStart = Calendar.getInstance().apply {
            if (currentHour < 5) {
                add(Calendar.DAY_OF_YEAR, -1)
            }
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startMillis = calStart.timeInMillis
        val now = System.currentTimeMillis()
        if (now <= startMillis) return 0L

        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startMillis,
            now
        ) ?: return 0L

        var lateNightMin = 0L
        for (u in stats) {
            if (!ProtectedApps.isProtected(u.packageName ?: "")) {
                lateNightMin += (u.totalTimeInForeground / (1000 * 60))
            }
        }
        return lateNightMin
    }

    /**
     * Detects unusually long continuous usage sessions (e.g. > 45 minutes).
     */
    fun getLongestSessionMinutes(context: Context): Int {
        if (!hasUsageStatsPermission(context)) return 0
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager ?: return 0

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val events = usageStatsManager.queryEvents(calendar.timeInMillis, System.currentTimeMillis()) ?: return 0
        val event = UsageEvents.Event()

        var maxDurationMillis = 0L
        val activeSessions = mutableMapOf<String, Long>()

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            val pkg = event.packageName ?: continue
            if (ProtectedApps.isProtected(pkg)) continue

            when (event.eventType) {
                UsageEvents.Event.ACTIVITY_RESUMED -> {
                    activeSessions[pkg] = event.timeStamp
                }
                UsageEvents.Event.ACTIVITY_PAUSED, UsageEvents.Event.ACTIVITY_STOPPED -> {
                    val start = activeSessions.remove(pkg)
                    if (start != null && event.timeStamp > start) {
                        val duration = event.timeStamp - start
                        if (duration > maxDurationMillis) {
                            maxDurationMillis = duration
                        }
                    }
                }
            }
        }
        return (maxDurationMillis / (1000 * 60)).toInt()
    }

    /**
     * Calculates comparison: today vs yesterday screen time minutes.
     */
    fun getYesterdayScreenTimeMinutes(context: Context): Long {
        if (!hasUsageStatsPermission(context)) return 0L
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager ?: return 0L

        val calYesterdayStart = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val calYesterdayEnd = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }

        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            calYesterdayStart.timeInMillis,
            calYesterdayEnd.timeInMillis
        ) ?: return 0L

        var totalMillis = 0L
        for (u in stats) {
            if (!ProtectedApps.isProtected(u.packageName ?: "")) {
                totalMillis += u.totalTimeInForeground
            }
        }
        return totalMillis / (1000 * 60)
    }
}
