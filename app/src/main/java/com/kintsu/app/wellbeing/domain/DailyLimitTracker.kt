package com.kintsu.app.wellbeing.domain

import android.os.SystemClock

data class AppUsageSession(
    val packageName: String,
    val startMonotonicMillis: Long,
    val startWallClockMillis: Long
)

class DailyLimitTracker(
    private val resetHourOfDay: Int = 0 // Midnight by default
) {
    // PackageName -> accumulated seconds today
    private val accumulatedUsageSeconds = mutableMapOf<String, Long>()
    // PackageName -> number of times opened today
    private val openCountsToday = mutableMapOf<String, Int>()

    private var activeSession: AppUsageSession? = null
    private var lastRecordedEpochDay: Long = -1L
    private var lastMonotonicTime: Long = SystemClock.elapsedRealtime()
    private var lastWallClockTime: Long = System.currentTimeMillis()

    /**
     * Checks if the day has turned over relative to [currentEpochDay] and reset hour.
     */
    fun checkAndPerformDailyReset(currentEpochDay: Long) {
        if (lastRecordedEpochDay != currentEpochDay) {
            accumulatedUsageSeconds.clear()
            openCountsToday.clear()
            lastRecordedEpochDay = currentEpochDay
        }
    }

    /**
     * Records the user opening an application.
     */
    fun onAppOpened(
        packageName: String,
        currentEpochDay: Long,
        monotonicNow: Long = SystemClock.elapsedRealtime(),
        wallClockNow: Long = System.currentTimeMillis()
    ): Boolean {
        checkAndPerformDailyReset(currentEpochDay)

        // Close any previously open session
        onAppClosed(monotonicNow, wallClockNow)

        // Detect tampering
        val tamperDetected = detectClockTamper(monotonicNow, wallClockNow)

        // Track open count
        val count = openCountsToday.getOrDefault(packageName, 0) + 1
        openCountsToday[packageName] = count

        activeSession = AppUsageSession(
            packageName = packageName,
            startMonotonicMillis = monotonicNow,
            startWallClockMillis = wallClockNow
        )

        return tamperDetected
    }

    /**
     * Records the user navigating away from the current application.
     */
    fun onAppClosed(
        monotonicNow: Long = SystemClock.elapsedRealtime(),
        wallClockNow: Long = System.currentTimeMillis()
    ) {
        val session = activeSession ?: return
        val elapsedMillis = (monotonicNow - session.startMonotonicMillis).coerceAtLeast(0L)
        val elapsedSeconds = elapsedMillis / 1000L

        val existing = accumulatedUsageSeconds.getOrDefault(session.packageName, 0L)
        accumulatedUsageSeconds[session.packageName] = existing + elapsedSeconds

        activeSession = null
        lastMonotonicTime = monotonicNow
        lastWallClockTime = wallClockNow
    }

    /**
     * Returns total seconds used today for this package (including currently open session).
     */
    fun getUsageSecondsToday(
        packageName: String,
        monotonicNow: Long = SystemClock.elapsedRealtime()
    ): Long {
        var total = accumulatedUsageSeconds.getOrDefault(packageName, 0L)
        val session = activeSession
        if (session != null && session.packageName == packageName) {
            val liveMillis = (monotonicNow - session.startMonotonicMillis).coerceAtLeast(0L)
            total += (liveMillis / 1000L)
        }
        return total
    }

    /**
     * Returns total open count today for this package.
     */
    fun getOpenCountToday(packageName: String): Int {
        return openCountsToday.getOrDefault(packageName, 0)
    }

    /**
     * Clock-tamper detection:
     * Detects if the wall-clock jumped forward or backward by more than 5 minutes
     * while monotonic time advanced normally.
     */
    fun detectClockTamper(
        monotonicNow: Long = SystemClock.elapsedRealtime(),
        wallClockNow: Long = System.currentTimeMillis()
    ): Boolean {
        val monotonicDelta = (monotonicNow - lastMonotonicTime).coerceAtLeast(0L)
        val wallClockDelta = wallClockNow - lastWallClockTime

        val drift = kotlin.math.abs(wallClockDelta - monotonicDelta)
        // If drift exceeds 5 minutes (300,000 ms), user or network adjusted system clock
        val isTampered = drift > 300_000L

        lastMonotonicTime = monotonicNow
        lastWallClockTime = wallClockNow
        return isTampered
    }

    fun resetAllForTesting() {
        accumulatedUsageSeconds.clear()
        openCountsToday.clear()
        activeSession = null
        lastRecordedEpochDay = -1L
    }
}
