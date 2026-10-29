package com.kintsu.app.domain

import com.kintsu.app.data.model.FrequencyType
import com.kintsu.app.data.model.HabitEntity
import com.kintsu.app.data.model.HabitLogEntity
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

object GoldRepairManager {

    /**
     * Determines whether a habit is scheduled to be performed on the given epochDay.
     */
    fun isHabitScheduledOn(habit: HabitEntity, epochDay: Long): Boolean {
        if (habit.isArchived) return false
        val habitCreationEpochDay = habit.createdAt / (86_400_000L)
        if (epochDay < habitCreationEpochDay) return false

        return when (habit.frequencyType) {
            FrequencyType.DAILY,
            FrequencyType.TIMES_PER_DAY -> true
            FrequencyType.TIMES_PER_WEEK -> true
            FrequencyType.SPECIFIC_DAYS -> {
                val date = LocalDate.ofEpochDay(epochDay)
                val dayOfWeekValue = date.dayOfWeek.value // 1 (Mon) .. 7 (Sun)
                val bit = 1 shl (dayOfWeekValue - 1)
                (habit.weekdaysMask and bit) != 0
            }
        }
    }

    /**
     * Calculates the repair window remaining hours for a missed day.
     */
    fun calculateRemainingRepairHours(
        missedEpochDay: Long,
        nowEpochMillis: Long = System.currentTimeMillis(),
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Long {
        val missedDate = LocalDate.ofEpochDay(missedEpochDay)
        // Missed day ends at midnight following the day
        val endOfMissedDay = missedDate.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        val windowExpiryMillis = endOfMissedDay + (GoldRepairConfig.REPAIR_WINDOW_HOURS * 3600 * 1000L)
        val remainingMillis = windowExpiryMillis - nowEpochMillis
        return if (remainingMillis > 0) (remainingMillis / (3600 * 1000L)) else 0L
    }

    /**
     * Checks how many repairs this habit has already used in the calendar week of the given day.
     */
    fun getRepairsUsedInWeek(
        habitId: Long,
        referenceEpochDay: Long,
        logs: List<HabitLogEntity>,
        weekStartMonday: Boolean = true
    ): Int {
        val refDate = LocalDate.ofEpochDay(referenceEpochDay)
        val startOfWeek = if (weekStartMonday) {
            refDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        } else {
            refDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
        }
        val endOfWeek = startOfWeek.plusDays(6)
        val startEpoch = startOfWeek.toEpochDay()
        val endEpoch = endOfWeek.toEpochDay()

        return logs.count { log ->
            log.habitId == habitId && log.isRepairedGold && log.epochDay in startEpoch..endEpoch
        }
    }

    /**
     * Finds any active repair opportunities for the given habit.
     */
    fun findAvailableRepair(
        habit: HabitEntity,
        logs: List<HabitLogEntity>,
        todayEpochDay: Long = LocalDate.now().toEpochDay(),
        nowEpochMillis: Long = System.currentTimeMillis(),
        goldRepairEnabled: Boolean = true,
        weekStartMonday: Boolean = true
    ): HabitRepairState? {
        if (!goldRepairEnabled) return null

        val habitLogsMap = logs.filter { it.habitId == habit.id }.associateBy { it.epochDay }
        val repairsUsedThisWeek = getRepairsUsedInWeek(habit.id, todayEpochDay, logs, weekStartMonday)

        // Check recent missed past days within max possible window (up to 3 days back)
        for (offset in 1..3) {
            val pastEpoch = todayEpochDay - offset
            if (!isHabitScheduledOn(habit, pastEpoch)) continue

            val log = habitLogsMap[pastEpoch]
            val isSatisfied = log?.isCompleted == true || log?.isRepairedGold == true

            if (!isSatisfied) {
                val remainingHours = calculateRemainingRepairHours(pastEpoch, nowEpochMillis)
                if (remainingHours > 0) {
                    val canRepair = repairsUsedThisWeek < GoldRepairConfig.MAX_REPAIRS_PER_HABIT_PER_WEEK
                    val reason = if (!canRepair) "Weekly gold repair limit reached (1/week)" else null
                    return HabitRepairState(
                        habit = habit,
                        missedEpochDay = pastEpoch,
                        remainingHours = remainingHours,
                        canRepair = canRepair,
                        reasonDisabled = reason
                    )
                }
            }
        }
        return null
    }

    /**
     * Maps an epochDay to its visual DayStatus for the calendar/heatmap.
     */
    fun getDayTileType(
        epochDay: Long,
        habits: List<HabitEntity>,
        logs: List<HabitLogEntity>,
        todayEpochDay: Long = LocalDate.now().toEpochDay(),
        nowEpochMillis: Long = System.currentTimeMillis(),
        goldRepairEnabled: Boolean = true
    ): DayTileType {
        val scheduledHabits = habits.filter { isHabitScheduledOn(it, epochDay) }
        if (scheduledHabits.isEmpty()) return DayTileType.NOT_SCHEDULED

        val dayLogs = logs.filter { it.epochDay == epochDay }.associateBy { it.habitId }

        val anyRepaired = scheduledHabits.any { h -> dayLogs[h.id]?.isRepairedGold == true }
        if (anyRepaired) {
            return DayTileType.REPAIRED_GOLD
        }

        val allCompleted = scheduledHabits.all { h -> dayLogs[h.id]?.isCompleted == true }
        if (allCompleted) {
            return DayTileType.INTACT_COMPLETED
        }

        if (epochDay >= todayEpochDay) {
            return DayTileType.EMPTY_FUTURE_OR_PAST
        }

        // It is an uncompleted past day: check if it is still repairable
        if (goldRepairEnabled) {
            val remainingHours = calculateRemainingRepairHours(epochDay, nowEpochMillis)
            if (remainingHours > 0) {
                return DayTileType.REPAIRABLE_CRACK
            }
        }

        return DayTileType.PERMANENT_CRACK
    }
}
