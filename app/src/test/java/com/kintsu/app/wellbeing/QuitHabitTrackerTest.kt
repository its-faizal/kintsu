package com.kintsu.app.wellbeing

import com.kintsu.app.data.model.QuitHabitEntity
import com.kintsu.app.wellbeing.domain.QuitHabitTracker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QuitHabitTrackerTest {

    @Test
    fun testCleanStreakCalculation() {
        val now = 10_000_000_000L
        val threeDaysAgo = now - (3 * 24 * 60 * 60 * 1000L) - (4 * 60 * 60 * 1000L)

        val habit = QuitHabitEntity(
            id = 1,
            name = "Doomscrolling",
            cleanStreakStartMillis = threeDaysAgo,
            bestStreakDays = 5,
            totalSlips = 0
        )

        val stats = QuitHabitTracker.calculateStats(habit, now)
        assertEquals(3L, stats.cleanDays)
        assertEquals(4L, stats.cleanHours)
        assertFalse(stats.isEligibleForGoldRepair)
    }

    @Test
    fun testSlipAndGoldRepairEligibility() {
        val now = 20_000_000_000L
        val habit = QuitHabitEntity(
            id = 1,
            name = "Late Night Snacking",
            cleanStreakStartMillis = now - (10 * 24 * 60 * 60 * 1000L),
            bestStreakDays = 7,
            totalSlips = 0
        )

        // Record a slip today
        val slipped = QuitHabitTracker.recordSlip(habit, now)
        assertEquals(1, slipped.totalSlips)
        assertEquals(10, slipped.bestStreakDays) // updated best streak!

        // Within 48 hours of slip -> eligible for Gold Repair
        val statsRecent = QuitHabitTracker.calculateStats(slipped, now + (12 * 60 * 60 * 1000L))
        assertTrue(statsRecent.isEligibleForGoldRepair)
        assertEquals(36L, statsRecent.repairWindowRemainingHours)

        // After 48 hours -> repair window expired
        val statsExpired = QuitHabitTracker.calculateStats(slipped, now + (50 * 60 * 60 * 1000L))
        assertFalse(statsExpired.isEligibleForGoldRepair)

        // Mend with Gold
        val repaired = QuitHabitTracker.repairWithGold(slipped, habit.cleanStreakStartMillis)
        assertEquals(1, repaired.totalRepairs)
        assertEquals(habit.cleanStreakStartMillis, repaired.cleanStreakStartMillis)
    }
}
