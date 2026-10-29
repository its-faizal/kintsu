package com.kintsu.app.domain

import com.kintsu.app.data.model.HabitEntity
import com.kintsu.app.data.model.HabitLogEntity
import java.time.LocalDate

object StreakCalculator {

    fun calculateStreaks(
        habits: List<HabitEntity>,
        logs: List<HabitLogEntity>,
        todayEpochDay: Long = LocalDate.now().toEpochDay(),
        nowEpochMillis: Long = System.currentTimeMillis(),
        goldRepairEnabled: Boolean = true
    ): StreakResult {
        val activeHabits = habits.filter { !it.isArchived }
        if (activeHabits.isEmpty()) {
            return StreakResult(
                currentStreak = 0,
                bestStreak = 0,
                totalRepairs = 0,
                totalCracks = 0,
                activeRepairAvailable = false
            )
        }

        val logsByDayAndHabit = logs.groupBy { it.epochDay }
            .mapValues { entry -> entry.value.associateBy { it.habitId } }

        fun isDaySatisfied(day: Long): Boolean {
            val scheduled = activeHabits.filter { GoldRepairManager.isHabitScheduledOn(it, day) }
            if (scheduled.isEmpty()) return true // Rest day counts as satisfied
            val dayMap = logsByDayAndHabit[day] ?: return false
            return scheduled.all { h ->
                val l = dayMap[h.id] ?: return@all false
                if (l.isRepairedGold) {
                    goldRepairEnabled && l.isCompleted
                } else {
                    l.isCompleted
                }
            }
        }

        fun isDayRepairable(day: Long): Boolean {
            if (!goldRepairEnabled) return false
            val remaining = GoldRepairManager.calculateRemainingRepairHours(day, nowEpochMillis)
            return remaining > 0
        }

        // Current Streak
        var currentStreak = 0
        val todaySatisfied = isDaySatisfied(todayEpochDay)

        var pointerDay = if (todaySatisfied) {
            currentStreak++
            todayEpochDay - 1
        } else {
            todayEpochDay - 1
        }

        var hasActiveRepair = false

        while (true) {
            val scheduled = activeHabits.filter { GoldRepairManager.isHabitScheduledOn(it, pointerDay) }
            if (scheduled.isEmpty()) {
                // Not scheduled on this day, skip back
                pointerDay--
                if (todayEpochDay - pointerDay > 365) break
                continue
            }

            if (isDaySatisfied(pointerDay)) {
                currentStreak++
                pointerDay--
            } else if (isDayRepairable(pointerDay)) {
                // Streak is preserved pending repair window!
                hasActiveRepair = true
                currentStreak++
                pointerDay--
            } else {
                break
            }
        }

        // Best Streak in History (look back up to 365 days or earliest log)
        val earliestLogDay = logs.minOfOrNull { it.epochDay } ?: todayEpochDay
        val startDay = minOf(earliestLogDay, todayEpochDay - 60)

        var bestStreak = 0
        var runningStreak = 0

        for (day in startDay..todayEpochDay) {
            val scheduled = activeHabits.filter { GoldRepairManager.isHabitScheduledOn(it, day) }
            if (scheduled.isEmpty()) continue

            if (isDaySatisfied(day)) {
                runningStreak++
                if (runningStreak > bestStreak) {
                    bestStreak = runningStreak
                }
            } else {
                runningStreak = 0
            }
        }

        bestStreak = maxOf(bestStreak, currentStreak)

        val totalRepairs = logs.count { it.isRepairedGold }

        // Total Cracks: past scheduled days that were uncompleted and not repaired
        var totalCracks = 0
        for (day in startDay until todayEpochDay) {
            val scheduled = activeHabits.filter { GoldRepairManager.isHabitScheduledOn(it, day) }
            if (scheduled.isNotEmpty() && !isDaySatisfied(day)) {
                totalCracks++
            }
        }

        return StreakResult(
            currentStreak = currentStreak,
            bestStreak = bestStreak,
            totalRepairs = totalRepairs,
            totalCracks = totalCracks,
            activeRepairAvailable = hasActiveRepair
        )
    }
}
