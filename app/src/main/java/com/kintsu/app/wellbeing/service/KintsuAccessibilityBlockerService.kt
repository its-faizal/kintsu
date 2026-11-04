package com.kintsu.app.wellbeing.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import com.kintsu.app.data.KintsuDatabase
import com.kintsu.app.data.SettingsDataStore
import com.kintsu.app.data.model.BlockRuleEntity
import com.kintsu.app.data.model.DistractionInterventionLogEntity
import com.kintsu.app.wellbeing.domain.BlockMode
import com.kintsu.app.wellbeing.domain.DailyLimitTracker
import com.kintsu.app.wellbeing.domain.ProtectedApps
import com.kintsu.app.wellbeing.domain.ScheduleEvaluator
import com.kintsu.app.wellbeing.domain.ShortFormContentDetector
import com.kintsu.app.wellbeing.ui.BlockActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime
import java.util.Calendar
import java.util.concurrent.ConcurrentHashMap

class KintsuAccessibilityBlockerService : AccessibilityService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val limitTracker = DailyLimitTracker()
    private val temporarilyUnlockedUntil = ConcurrentHashMap<String, Long>()
    private val lastTriggeredTimeMillis = ConcurrentHashMap<String, Long>()
    private val lastShortFormCheckMillis = ConcurrentHashMap<String, Long>()

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
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                    AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or
                    AccessibilityEvent.TYPE_WINDOWS_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS or
                    AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                    AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
            notificationTimeout = 80
        }
        serviceInfo = info

        serviceScope.launch {
            try {
                SettingsDataStore(applicationContext).setProtectionActive(true)
            } catch (_: Exception) {}
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val eventType = event.eventType
        if (eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED &&
            eventType != AccessibilityEvent.TYPE_WINDOWS_CHANGED
        ) return

        val pkgName = event.packageName?.toString() ?: ""

        // 0. Tamper Protection & Invincible Mode Interception (Prioritized before protected app check)
        if (pkgName.isNotBlank()) {
            serviceScope.launch {
                try {
                    checkTamperAndInvincible(event, pkgName)
                } catch (_: Exception) {}
            }
        }

        // Multi-Window, Split-Screen & Pop-up Window Inspection
        serviceScope.launch {
            try {
                inspectMultiWindowAndPopups(event)
            } catch (_: Exception) {}
        }

        if (pkgName.isBlank() || ProtectedApps.isProtected(pkgName)) return

        val now = System.currentTimeMillis()

        // 1. High-Priority Short-Form Video / Reels Interception
        val lastShortCheck = lastShortFormCheckMillis[pkgName] ?: 0L
        if (now - lastShortCheck > 600L) {
            lastShortFormCheckMillis[pkgName] = now
            serviceScope.launch {
                inspectShortFormAndWeb(event, pkgName, now)
            }
        }

        // If not a window state change, we only inspect short form and website URLs above
        if (eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        // Anti-flicker throttling for app level inspection
        val lastTrigger = lastTriggeredTimeMillis[pkgName] ?: 0L
        if (now - lastTrigger < 2500L) {
            return
        }

        // Check if currently temporarily unlocked
        val unlockExpiry = temporarilyUnlockedUntil[pkgName]
        if (unlockExpiry != null && now < unlockExpiry) {
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

    private suspend fun inspectShortFormAndWeb(
        event: AccessibilityEvent,
        pkgName: String,
        now: Long
    ) {
        val settings = SettingsDataStore(applicationContext).settingsFlow.first()
        if (!settings.wellbeingEnabled || settings.protectionFailSafeTripped) return

        val db = KintsuDatabase.getDatabase(applicationContext)

        // A. Short-form video detection (YouTube Shorts, IG Reels, TikTok, FB Reels, Snapchat)
        if (settings.shortFormBlockingEnabled) {
            val rootNode = try { rootInActiveWindow } catch (_: Exception) { null }
            val isShortForm = ShortFormContentDetector.isShortFormContent(
                event = event,
                rootNode = rootNode,
                packageName = pkgName,
                blockYouTube = settings.blockYouTubeShorts,
                blockInstagram = settings.blockInstagramReels,
                blockTikTok = settings.blockTikTok,
                blockFacebook = settings.blockFacebookReels,
                blockSnapchat = settings.blockSnapchatSpotlight,
                blockWeb = settings.blockWebShorts
            )

            if (isShortForm) {
                if (com.kintsu.app.wellbeing.ui.ReelInterventionOverlayActivity.isTemporarilyUnlocked(pkgName)) {
                    return
                }

                if (com.kintsu.app.wellbeing.ui.ReelInterventionOverlayActivity.hasPassExpiredRecently(pkgName)) {
                    com.kintsu.app.coin.NegativeEconomyEngine.recordDeduction(
                        context = applicationContext,
                        type = com.kintsu.app.coin.DeductionType.REEL_BINGE_OVERSTAY,
                        targetKey = pkgName
                    )
                }

                lastTriggeredTimeMillis[pkgName] = now

                // Log intervention
                db.wellbeingDao().insertIntervention(
                    DistractionInterventionLogEntity(
                        timestampMillis = now,
                        packageName = pkgName,
                        interventionStyle = "REEL_OVERLAY",
                        userAction = "PAUSED_FOR_CHOICE"
                    )
                )

                // Launch interface overlay with conscious choices directly over reels
                val platformName = when {
                    pkgName.contains("instagram") -> "Instagram"
                    pkgName.contains("youtube") -> "YouTube"
                    pkgName.contains("tiktok") -> "TikTok"
                    pkgName.contains("facebook") || pkgName.contains("katana") -> "Facebook"
                    pkgName.contains("snapchat") -> "Snapchat"
                    pkgName.contains("reddit") -> "Reddit"
                    else -> "Video Feed"
                }

                val overlayIntent = android.content.Intent(applicationContext, com.kintsu.app.wellbeing.ui.ReelInterventionOverlayActivity::class.java).apply {
                    flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra(com.kintsu.app.wellbeing.ui.ReelInterventionOverlayActivity.EXTRA_PACKAGE, pkgName)
                    putExtra(com.kintsu.app.wellbeing.ui.ReelInterventionOverlayActivity.EXTRA_PLATFORM, platformName)
                }
                startActivity(overlayIntent)
                return
            }
        }

        // B. Browser URL inspection against blocked website list
        if (ShortFormContentDetector.BROWSER_PACKAGES.contains(pkgName)) {
            val rootNode = try { rootInActiveWindow } catch (_: Exception) { null }
            val url = ShortFormContentDetector.extractBrowserUrl(rootNode)
            if (!url.isNullOrBlank()) {
                val blockedWebsites = db.wellbeingDao().getAllBlockedWebsites().first()
                val isBlockedSite = blockedWebsites.any { it.isEnabled && url.contains(it.domain, ignoreCase = true) }
                val isWebShort = settings.shortFormBlockingEnabled && settings.blockWebShorts && ShortFormContentDetector.isShortFormUrl(url)

                if (isBlockedSite || isWebShort) {
                    performGlobalAction(GLOBAL_ACTION_BACK)
                    lastTriggeredTimeMillis[pkgName] = now

                    val prompt = if (isWebShort) {
                        "Short-form feed is restricted."
                    } else {
                        "Access to this website ($url) is restricted by Kintsu Shield."
                    }

                    db.wellbeingDao().insertIntervention(
                        DistractionInterventionLogEntity(
                            timestampMillis = now,
                            packageName = pkgName,
                            interventionStyle = settings.interventionStyle,
                            userAction = "BLOCKED"
                        )
                    )

                    triggerBlockScreen(
                        targetPackage = pkgName,
                        ruleMode = "WEBSITE_BLOCKED",
                        strictness = "STRICT",
                        customPrompt = prompt,
                        pauseDurationSeconds = 10,
                        interventionStyle = settings.interventionStyle
                    )
                }
            }
        }
    }

    private suspend fun handlePackageInspection(packageName: String) {
        val settings = SettingsDataStore(applicationContext).settingsFlow.first()
        if (!settings.wellbeingEnabled) return
        if (settings.protectionFailSafeTripped) return

        val db = KintsuDatabase.getDatabase(applicationContext)
        val nowMillis = System.currentTimeMillis()

        // 1. Check if App is in Active Cooldown from an excessive session
        if (limitTracker.isPackageInCooldown(packageName)) {
            val remainingSec = limitTracker.getRemainingCooldownSeconds(packageName)
            lastTriggeredTimeMillis[packageName] = nowMillis
            triggerBlockScreen(
                targetPackage = packageName,
                ruleMode = "COOLDOWN",
                strictness = "STRICT",
                customPrompt = "Continuous session cooldown active (${remainingSec / 60}m ${remainingSec % 60}s remaining). Rest your eyes.",
                pauseDurationSeconds = 10,
                interventionStyle = settings.interventionStyle
            )
            return
        }

        // 2. Check Active Focus Lock Session
        val activeFocusLock = db.wellbeingDao().getActiveFocusLockSession(nowMillis)
        if (activeFocusLock != null) {
            val allGroups = db.wellbeingDao().getAllAppGroups().first()
            val matchingGroups = allGroups.filter { group ->
                group.packageNames.split(",").map { it.trim() }.contains(packageName)
            }
            val restrictedIds = activeFocusLock.restrictedAppGroupIds.split(",").map { it.trim() }.toSet()
            val isRestricted = activeFocusLock.restrictedAppGroupIds == "ALL" ||
                    matchingGroups.any { restrictedIds.contains(it.id.toString()) }

            if (isRestricted) {
                lastTriggeredTimeMillis[packageName] = nowMillis
                triggerBlockScreen(
                    targetPackage = packageName,
                    ruleMode = "FOCUS_LOCK",
                    strictness = activeFocusLock.strictnessMode,
                    customPrompt = "Focus Lock is active: ${activeFocusLock.title}",
                    pauseDurationSeconds = settings.breathingDurationSeconds,
                    interventionStyle = settings.interventionStyle
                )
                return
            }
        }

        // 3. Check Active Digital Detox
        val activeDetox = db.wellbeingDao().getActiveDetox(nowMillis)
        if (activeDetox != null) {
            val allowedList = activeDetox.allowedPackages.split(",").map { it.trim() }.toSet()
            if (!allowedList.contains(packageName)) {
                lastTriggeredTimeMillis[packageName] = nowMillis
                triggerBlockScreen(
                    targetPackage = packageName,
                    ruleMode = "DIGITAL_DETOX",
                    strictness = settings.wellbeingStrictness,
                    customPrompt = activeDetox.intention ?: "Digital Detox Sanctuary is active.",
                    pauseDurationSeconds = settings.breathingDurationSeconds,
                    interventionStyle = settings.interventionStyle
                )
                return
            }
        }

        // 4. Check App Groups & Rules
        val allGroups = db.wellbeingDao().getAllAppGroups().first()
        val matchingGroups = allGroups.filter { group ->
            group.packageNames.split(",").map { it.trim() }.contains(packageName)
        }

        if (matchingGroups.isEmpty()) return

        val todayEpoch = LocalDate.now().toEpochDay()
        val localTime = LocalTime.now()
        val dayOfWeek = LocalDate.now().dayOfWeek

        limitTracker.onAppOpened(packageName, todayEpoch)

        // 5. Check Active Continuous Session Limit
        val activeSessionSec = limitTracker.getActiveSessionSeconds()
        if (settings.maxContinuousSessionMinutes > 0 && activeSessionSec >= (settings.maxContinuousSessionMinutes * 60L)) {
            limitTracker.triggerCooldown(packageName, settings.sessionCooldownMinutes)
            lastTriggeredTimeMillis[packageName] = nowMillis
            triggerBlockScreen(
                targetPackage = packageName,
                ruleMode = "SESSION_LIMIT",
                strictness = "STRICT",
                customPrompt = "You've been in this app continuously for over ${settings.maxContinuousSessionMinutes} minutes. Taking a ${settings.sessionCooldownMinutes}-minute pause.",
                pauseDurationSeconds = settings.breathingDurationSeconds,
                interventionStyle = settings.interventionStyle
            )
            return
        }

        // 6. Check Daily Launch Limit
        if (settings.appLaunchLimitEnabled) {
            val opensToday = limitTracker.getOpenCountToday(packageName)
            if (opensToday > settings.defaultAppMaxLaunches) {
                lastTriggeredTimeMillis[packageName] = nowMillis
                triggerBlockScreen(
                    targetPackage = packageName,
                    ruleMode = "OPEN_LIMIT",
                    strictness = "STRICT",
                    customPrompt = "Daily launch limit reached ($opensToday / ${settings.defaultAppMaxLaunches} launches today).",
                    pauseDurationSeconds = settings.breathingDurationSeconds,
                    interventionStyle = settings.interventionStyle
                )
                return
            }
        }

        // 7. Check Bedtime & Sleep Sanctuary Lock
        if (settings.bedtimeLockEnabled) {
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            val isBedtime = if (settings.bedtimeStartHour > settings.bedtimeEndHour) {
                hour >= settings.bedtimeStartHour || hour < settings.bedtimeEndHour
            } else {
                hour in settings.bedtimeStartHour until settings.bedtimeEndHour
            }
            if (isBedtime) {
                lastTriggeredTimeMillis[packageName] = nowMillis
                com.kintsu.app.coin.NegativeEconomyEngine.recordDeduction(
                    context = applicationContext,
                    type = com.kintsu.app.coin.DeductionType.BEDTIME_CURFEW_VIOLATION,
                    targetKey = packageName
                )
                triggerBlockScreen(
                    targetPackage = packageName,
                    ruleMode = "BEDTIME",
                    strictness = "STRICT",
                    customPrompt = "Bedtime Sanctuary Curfew is active. Wind down for restful sleep.",
                    pauseDurationSeconds = settings.breathingDurationSeconds,
                    interventionStyle = settings.interventionStyle
                )
                return
            }
        }

        // 8. Evaluate configured group rules (Daily Limit, Schedule, Mindful Pause)
        for (group in matchingGroups) {
            val rules = db.wellbeingDao().getRulesForGroup(group.id)
            for (rule in rules) {
                if (!rule.isEnabled) continue

                val isBlocked = evaluateRule(db, rule, packageName, localTime, dayOfWeek)
                if (isBlocked) {
                    lastTriggeredTimeMillis[packageName] = nowMillis
                    if (rule.mode == "DAILY_LIMIT" || rule.mode == "SESSION_LIMIT") {
                        com.kintsu.app.coin.NegativeEconomyEngine.recordDeduction(
                            context = applicationContext,
                            type = com.kintsu.app.coin.DeductionType.MARATHON_OVERSTAY,
                            targetKey = packageName
                        )
                    }
                    triggerBlockScreen(
                        targetPackage = packageName,
                        ruleMode = rule.mode,
                        strictness = rule.strictnessLevel,
                        customPrompt = rule.pauseQuestion,
                        pauseDurationSeconds = if (rule.pauseBreathingSeconds > 0) rule.pauseBreathingSeconds else settings.breathingDurationSeconds,
                        interventionStyle = settings.interventionStyle
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
        pauseDurationSeconds: Int = 10,
        interventionStyle: String = "BREATHING"
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
            putExtra(BlockActivity.EXTRA_INTERVENTION_STYLE, interventionStyle)
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
            serviceScope.launch {
                try {
                    val settings = SettingsDataStore(applicationContext)
                    settings.setProtectionFailSafeTripped(true)
                    settings.setProtectionActive(false)
                } catch (_: Exception) {}
            }
        }
    }


    private fun isWithin10MinuteWindow(windowHour: Int, windowMinute: Int): Boolean {
        val cal = Calendar.getInstance()
        val currentHour = cal.get(Calendar.HOUR_OF_DAY)
        val currentMin = cal.get(Calendar.MINUTE)
        val currentTotalMin = currentHour * 60 + currentMin
        val windowStart = windowHour * 60 + windowMinute
        val windowEnd = windowStart + 10 // strictly 10 minutes
        return currentTotalMin in windowStart until windowEnd
    }

    private suspend fun checkTamperAndInvincible(event: AccessibilityEvent, pkgName: String): Boolean {
        val settings = SettingsDataStore(applicationContext).settingsFlow.first()
        if (!settings.wellbeingEnabled) return false

        val isInvincibleActive = settings.invincibleModeEnabled
        val isTamperActive = settings.tamperProtectionEnabled
        if (!isInvincibleActive && !isTamperActive) return false

        // Check if inside the 10-minute allowed modification window
        val inInvincibleWindow = if (isInvincibleActive) isWithin10MinuteWindow(settings.invincibleWindowHour, settings.invincibleWindowMinute) else true
        val inTamperWindow = if (isTamperActive) isWithin10MinuteWindow(settings.uninstallWindowHour, settings.uninstallWindowMinute) else true

        if (inInvincibleWindow && inTamperWindow) {
            // Legitimate 10-minute modification window
            return false
        }

        val isSettingsOrInstaller = pkgName == "com.android.settings" ||
                pkgName.contains("packageinstaller") ||
                pkgName.contains("permissioncontroller") ||
                pkgName == "com.android.vending"

        if (!isSettingsOrInstaller) return false

        val rootNode = try { rootInActiveWindow } catch (_: Exception) { null } ?: event.source ?: return false
        val screenText = collectAllText(rootNode).lowercase()

        val isTargetingKintsu = screenText.contains("kintsu")
        val isUninstallAction = screenText.contains("uninstall") || screenText.contains("force stop") || screenText.contains("clear data") || screenText.contains("clear storage")
        val isDisablingService = screenText.contains("accessibility") && (screenText.contains("turn off") || screenText.contains("stop") || screenText.contains("disable"))
        val isDeviceAdminAction = screenText.contains("device admin") && (screenText.contains("deactivate") || screenText.contains("remove"))
        val isUserSwitchAction = screenText.contains("switch user") || screenText.contains("guest") || screenText.contains("multiple users") || screenText.contains("add user")

        if ((isTargetingKintsu && (isUninstallAction || isDisablingService || isDeviceAdminAction)) ||
            (isUninstallAction && isTargetingKintsu) ||
            isUserSwitchAction
        ) {
            performGlobalAction(GLOBAL_ACTION_HOME)
            performGlobalAction(GLOBAL_ACTION_BACK)

            withContext(Dispatchers.Main) {
                Toast.makeText(
                    applicationContext,
                    "Invincible Mode: Settings & uninstalling are locked until your daily 10-minute window.",
                    Toast.LENGTH_LONG
                ).show()
            }
            return true
        }

        return false
    }

    private suspend fun inspectMultiWindowAndPopups(event: AccessibilityEvent) {
        val settings = SettingsDataStore(applicationContext).settingsFlow.first()
        if (!settings.wellbeingEnabled || settings.protectionFailSafeTripped) return

        val currentWindows = try { windows } catch (_: Exception) { emptyList() }
        for (w in currentWindows) {
            val wRoot = try { w.root } catch (_: Exception) { null } ?: continue
            val wPkg = wRoot.packageName?.toString() ?: continue
            if (ProtectedApps.isProtected(wPkg)) continue

            // If short-form content in a floating / pop-up / split window
            if (settings.shortFormBlockingEnabled && ShortFormContentDetector.isShortFormContent(
                    event = event,
                    rootNode = wRoot,
                    packageName = wPkg,
                    blockYouTube = settings.blockYouTubeShorts,
                    blockInstagram = settings.blockInstagramReels,
                    blockTikTok = settings.blockTikTok,
                    blockFacebook = settings.blockFacebookReels,
                    blockSnapchat = settings.blockSnapchatSpotlight,
                    blockWeb = settings.blockWebShorts
                )
            ) {
                if (!com.kintsu.app.wellbeing.ui.ReelInterventionOverlayActivity.isTemporarilyUnlocked(wPkg)) {
                    // Close the popup/split window
                    performGlobalAction(GLOBAL_ACTION_HOME)
                    performGlobalAction(GLOBAL_ACTION_BACK)

                    val overlayIntent = Intent(applicationContext, com.kintsu.app.wellbeing.ui.ReelInterventionOverlayActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        putExtra(com.kintsu.app.wellbeing.ui.ReelInterventionOverlayActivity.EXTRA_PACKAGE, wPkg)
                        putExtra(com.kintsu.app.wellbeing.ui.ReelInterventionOverlayActivity.EXTRA_PLATFORM, "Video Feed")
                    }
                    startActivity(overlayIntent)
                    return
                }
            }
        }
    }

    private fun collectAllText(node: android.view.accessibility.AccessibilityNodeInfo?): String {
        if (node == null) return ""
        val sb = StringBuilder()
        node.text?.let { sb.append(it).append(" ") }
        node.contentDescription?.let { sb.append(it).append(" ") }
        for (i in 0 until node.childCount) {
            try {
                val child = node.getChild(i)
                if (child != null) {
                    sb.append(collectAllText(child)).append(" ")
                    child.recycle()
                }
            } catch (_: Exception) {}
        }
        return sb.toString()
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        instance = null
        isServiceRunning = false
        serviceScope.cancel()
        super.onDestroy()
    }
}
