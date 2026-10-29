package com.kintsu.app.wellbeing.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.kintsu.app.data.KintsuDatabase
import com.kintsu.app.data.SettingsDataStore
import com.kintsu.app.data.model.BlockRuleEntity
import com.kintsu.app.data.model.DigitalDetoxEntity
import com.kintsu.app.wellbeing.domain.BlockMode
import com.kintsu.app.wellbeing.domain.DailyLimitTracker
import com.kintsu.app.wellbeing.domain.ProtectedApps
import com.kintsu.app.wellbeing.domain.ScheduleEvaluator
import com.kintsu.app.wellbeing.ui.BlockActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.util.concurrent.ConcurrentHashMap

class KintsuAccessibilityBlockerService : AccessibilityService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val limitTracker = DailyLimitTracker()
    private val temporarilyUnlockedUntil = ConcurrentHashMap<String, Long>()

    private var failSafeErrorsIn10Min = 0
    private var lastErrorTimeMillis = 0L

    companion object {
        var isServiceRunning = false
            private set

        fun unlockPackageTemporarily(packageName: String, durationMinutes: Int = 15) {
            instance?.temporarilyUnlockedUntil?.put(
                packageName,
                System.currentTimeMillis() + (durationMinutes * 60 * 1000L)
            )
        }

        private var instance: KintsuAccessibilityBlockerService? = null
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        isServiceRunning = true

        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS or
                    AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
            notificationTimeout = 100
        }
        serviceInfo = info

        serviceScope.launch {
            try {
                SettingsDataStore(applicationContext).setProtectionActive(true)
            } catch (_: Exception) {}
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val pkgName = event.packageName?.toString() ?: return
        if (ProtectedApps.isProtected(pkgName)) return

        // Check if currently temporarily unlocked
        val unlockExpiry = temporarilyUnlockedUntil[pkgName]
        if (unlockExpiry != null && System.currentTimeMillis() < unlockExpiry) {
            return
        } else if (unlockExpiry != null) {
            temporarilyUnlockedUntil.remove(pkgName)
        }

        serviceScope.launch {
            try {
                handlePackageInspection(pkgName)
            } catch (e: Exception) {
                recordFailSafeError()
            }
        }
    }

    private suspend fun handlePackageInspection(packageName: String) {
        val settings = SettingsDataStore(applicationContext).settingsFlow.first()
        if (!settings.wellbeingEnabled) return
        if (settings.protectionFailSafeTripped) return

        val db = KintsuDatabase.getDatabase(applicationContext)
        val nowMillis = System.currentTimeMillis()

        // 1. Check Active Digital Detox
        val activeDetox = db.wellbeingDao().getActiveDetox(nowMillis)
        if (activeDetox != null) {
            val allowedList = activeDetox.allowedPackages.split(",").map { it.trim() }.toSet()
            if (!allowedList.contains(packageName)) {
                triggerBlockScreen(
                    targetPackage = packageName,
                    ruleMode = "DIGITAL_DETOX",
                    strictness = settings.wellbeingStrictness,
                    customPrompt = activeDetox.intention ?: "Digital Detox is active."
                )
                return
            }
        }

        // 2. Check App Groups & Rules
        val allGroups = db.wellbeingDao().getAllAppGroups().first()
        val matchingGroups = allGroups.filter { group ->
            group.packageNames.split(",").map { it.trim() }.contains(packageName)
        }

        if (matchingGroups.isEmpty()) return

        val todayEpoch = LocalDate.now().toEpochDay()
        val localTime = LocalTime.now()
        val dayOfWeek = LocalDate.now().dayOfWeek

        limitTracker.onAppOpened(packageName, todayEpoch)

        for (group in matchingGroups) {
            val rules = db.wellbeingDao().getRulesForGroup(group.id)
            for (rule in rules) {
                if (!rule.isEnabled) continue

                val isBlocked = evaluateRule(db, rule, packageName, localTime, dayOfWeek)
                if (isBlocked) {
                    triggerBlockScreen(
                        targetPackage = packageName,
                        ruleMode = rule.mode,
                        strictness = rule.strictnessLevel,
                        customPrompt = rule.pauseQuestion,
                        pauseDurationSeconds = rule.pauseBreathingSeconds
                    )
                    return
                }
            }
        }
    }

    private suspend fun evaluateRule(
        db: KintsuDatabase,
        rule: BlockRuleEntity,
        packageName: String,
        localTime: LocalTime,
        dayOfWeek: java.time.DayOfWeek
    ): Boolean {
        return when (rule.mode) {
            BlockMode.DAILY_LIMIT.name -> {
                val usageSec = limitTracker.getUsageSecondsToday(packageName)
                usageSec >= (rule.dailyLimitMinutes * 60L)
            }
            BlockMode.SCHEDULE.name, BlockMode.BEDTIME.name -> {
                val schedules = db.wellbeingDao().getSchedulesForRule(rule.id)
                ScheduleEvaluator.isAnyScheduleActive(schedules, localTime, dayOfWeek)
            }
            BlockMode.OPEN_LIMIT.name -> {
                val opens = limitTracker.getOpenCountToday(packageName)
                rule.maxOpensPerDay > 0 && opens >= rule.maxOpensPerDay
            }
            BlockMode.MINDFUL_PAUSE.name -> {
                // Mindful pause intercepts on every open unless bypassed
                true
            }
            else -> false
        }
    }

    private fun triggerBlockScreen(
        targetPackage: String,
        ruleMode: String,
        strictness: String,
        customPrompt: String,
        pauseDurationSeconds: Int = 10
    ) {
        val intent = Intent(this, BlockActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(BlockActivity.EXTRA_PACKAGE_NAME, targetPackage)
            putExtra(BlockActivity.EXTRA_RULE_MODE, ruleMode)
            putExtra(BlockActivity.EXTRA_STRICTNESS, strictness)
            putExtra(BlockActivity.EXTRA_PROMPT, customPrompt)
            putExtra(BlockActivity.EXTRA_PAUSE_SECONDS, pauseDurationSeconds)
        }
        startActivity(intent)
    }

    private fun recordFailSafeError() {
        val now = System.currentTimeMillis()
        if (now - lastErrorTimeMillis > 10 * 60 * 1000L) {
            failSafeErrorsIn10Min = 1
        } else {
            failSafeErrorsIn10Min++
        }
        lastErrorTimeMillis = now

        if (failSafeErrorsIn10Min >= 3) {
            // Tripped circuit breaker
            serviceScope.launch {
                try {
                    val settings = SettingsDataStore(applicationContext)
                    settings.setProtectionFailSafeTripped(true)
                    settings.setProtectionActive(false)
                } catch (_: Exception) {}
            }
        }
    }

    override fun onInterrupt() {
        // Accessibility service interrupted
    }

    override fun onDestroy() {
        instance = null
        isServiceRunning = false
        serviceScope.cancel()
        super.onDestroy()
    }
}
