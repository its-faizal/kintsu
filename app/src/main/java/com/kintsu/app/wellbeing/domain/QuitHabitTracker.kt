package com.kintsu.app.wellbeing.domain

import com.kintsu.app.data.model.QuitHabitEntity

data class QuitHabitStats(
    val cleanDays: Long,
    val cleanHours: Long,
    val isEligibleForGoldRepair: Boolean,
    val repairWindowRemainingHours: Long
)

object QuitHabitTracker {

    const val GOLD_REPAIR_WINDOW_MILLIS: Long = 48 * 60 * 60 * 1000L // 48-hour kintsugi grace window

    /**
     * Calculates clean streak duration in days and hours.
     */
    fun calculateStats(
        habit: QuitHabitEntity,
        currentTimeMillis: Long = System.currentTimeMillis()
    ): QuitHabitStats {
        val elapsedMillis = (currentTimeMillis - habit.cleanStreakStartMillis).coerceAtLeast(0L)
        val cleanDays = elapsedMillis / (24 * 60 * 60 * 1000L)
        val cleanHours = (elapsedMillis % (24 * 60 * 60 * 1000L)) / (60 * 60 * 1000L)

        // Eligible for Gold Repair if slip occurred within the last 48 hours
        val timeSinceSlip = (currentTimeMillis - habit.cleanStreakStartMillis).coerceAtLeast(0L)
        val isEligible = habit.totalSlips > 0 && timeSinceSlip <= GOLD_REPAIR_WINDOW_MILLIS
        val remRepairHours = ((GOLD_REPAIR_WINDOW_MILLIS - timeSinceSlip) / (60 * 60 * 1000L)).coerceAtLeast(0L)

        return QuitHabitStats(
            cleanDays = cleanDays,
            cleanHours = cleanHours,
            isEligibleForGoldRepair = isEligible,
            repairWindowRemainingHours = remRepairHours
        )
    }

    /**
     * Records a slip:
     * Increments slip count, updates best streak if current was highest, resets streak start.
     */
    fun recordSlip(
        habit: QuitHabitEntity,
        slipTimeMillis: Long = System.currentTimeMillis()
    ): QuitHabitEntity {
        val currentDays = ((slipTimeMillis - habit.cleanStreakStartMillis) / (24 * 60 * 60 * 1000L)).toInt().coerceAtLeast(0)
        val newBest = kotlin.math.max(habit.bestStreakDays, currentDays)

        return habit.copy(
            cleanStreakStartMillis = slipTimeMillis,
            bestStreakDays = newBest,
            totalSlips = habit.totalSlips + 1
        )
    }

    /**
     * Mends a slip using Gold Repair:
     * Recommits within the window without erasing history, adding a golden seam to the streak.
     */
    fun repairWithGold(
        habit: QuitHabitEntity,
        restoredStreakStartMillis: Long
    ): QuitHabitEntity {
        return habit.copy(
            cleanStreakStartMillis = restoredStreakStartMillis,
            totalRepairs = habit.totalRepairs + 1
        )
    }

    /**
     * Gentle self-help disclaimer copy.
     */
    const val DISCLAIMER_TEXT = "Kintsu is a supportive mindful self-reflection tool, not a substitute for professional healthcare or addiction recovery treatment."
}
