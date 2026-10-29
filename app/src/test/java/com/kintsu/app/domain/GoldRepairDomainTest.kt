package com.kintsu.app.domain

import com.kintsu.app.data.model.FrequencyType
import com.kintsu.app.data.model.HabitEntity
import com.kintsu.app.data.model.HabitLogEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class GoldRepairDomainTest {

    private val baseHabit = HabitEntity(
        id = 1L,
        name = "Meditation",
        frequencyType = FrequencyType.DAILY,
        weekdaysMask = 127,
        createdAt = LocalDate.of(2026, 1, 1).toEpochDay() * 86_400_000L
    )

    @Test
    fun testRepairWindowCalculation_active() {
        val today = LocalDate.of(2026, 10, 29)
        val missedDay = today.minusDays(1).toEpochDay()

        // Test at 12:00 noon on today
        val noonTodayMillis = today.atTime(12, 0).atZone(ZoneId.of("UTC")).toInstant().toEpochMilli()
        val remainingHours = GoldRepairManager.calculateRemainingRepairHours(
            missedEpochDay = missedDay,
            nowEpochMillis = noonTodayMillis,
            zoneId = ZoneId.of("UTC")
        )

        // 48 hours window starts at midnight after missedDay (which is start of today).
        // At 12 hours into today, 36 hours remain.
        assertEquals(36L, remainingHours)
    }

    @Test
    fun testRepairWindowCalculation_expired() {
        val today = LocalDate.of(2026, 10, 29)
        val missedDay = today.minusDays(3).toEpochDay() // 3 days ago

        val noonTodayMillis = today.atTime(12, 0).atZone(ZoneId.of("UTC")).toInstant().toEpochMilli()
        val remainingHours = GoldRepairManager.calculateRemainingRepairHours(
            missedEpochDay = missedDay,
            nowEpochMillis = noonTodayMillis,
            zoneId = ZoneId.of("UTC")
        )

        assertEquals(0L, remainingHours)
    }

    @Test
    fun testWeeklyRepairLimit() {
        // Monday Oct 26, 2026 to Sunday Nov 1, 2026 is one ISO week
        val monday = LocalDate.of(2026, 10, 26).toEpochDay()
        val wednesday = LocalDate.of(2026, 10, 28).toEpochDay()

        // 1 repair already used on Monday
        val existingLogs = listOf(
            HabitLogEntity(
                habitId = 1L,
                epochDay = monday,
                isCompleted = true,
                isRepairedGold = true,
                repairedAt = System.currentTimeMillis()
            )
        )

        val repairsUsed = GoldRepairManager.getRepairsUsedInWeek(
            habitId = 1L,
            referenceEpochDay = wednesday,
            logs = existingLogs,
            weekStartMonday = true
        )

        assertEquals(1, repairsUsed)

        // Finding repair on Wednesday for Tuesday should report canRepair = false due to limit
        val tuesday = LocalDate.of(2026, 10, 27).toEpochDay()
        val state = GoldRepairManager.findAvailableRepair(
            habit = baseHabit,
            logs = existingLogs,
            todayEpochDay = wednesday,
            nowEpochMillis = LocalDate.of(2026, 10, 28).atTime(10, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            goldRepairEnabled = true,
            weekStartMonday = true
        )

        assertNotNull(state)
        assertFalse(state!!.canRepair)
        assertEquals("Weekly gold repair limit reached (1/week)", state.reasonDisabled)
    }

    @Test
    fun testStreakCalculator_withGoldRepair() {
        val today = LocalDate.of(2026, 10, 29).toEpochDay()

        // Habit completed today, missed yesterday (repaired with gold), completed 2 days ago
        val logs = listOf(
            HabitLogEntity(habitId = 1L, epochDay = today, isCompleted = true),
            HabitLogEntity(habitId = 1L, epochDay = today - 1, isCompleted = true, isRepairedGold = true),
            HabitLogEntity(habitId = 1L, epochDay = today - 2, isCompleted = true)
        )

        val resultWithGold = StreakCalculator.calculateStreaks(
            habits = listOf(baseHabit),
            logs = logs,
            todayEpochDay = today,
            goldRepairEnabled = true
        )

        // Streak continues unbroken thanks to gold repair!
        assertEquals(3, resultWithGold.currentStreak)
        assertEquals(1, resultWithGold.totalRepairs)

        // When gold repair is disabled in settings, the repaired day does not preserve streak
        val resultWithoutGold = StreakCalculator.calculateStreaks(
            habits = listOf(baseHabit),
            logs = logs,
            todayEpochDay = today,
            goldRepairEnabled = false
        )
        // Only today counts because yesterday was not completed normally
        assertEquals(1, resultWithoutGold.currentStreak)
    }

    @Test
    fun testSpecificDaysSchedule_unScheduledDaysDoNotBreakStreak() {
        // Habit only scheduled on Monday, Wednesday, Friday
        // WeekdaysMask: Mon(1) + Wed(4) + Fri(16) = 21
        val mwfHabit = baseHabit.copy(
            frequencyType = FrequencyType.SPECIFIC_DAYS,
            weekdaysMask = 21
        )

        // Friday Oct 30, 2026
        val friday = LocalDate.of(2026, 10, 30).toEpochDay()
        val wednesday = LocalDate.of(2026, 10, 28).toEpochDay()

        // Completed on Wed and Fri, Thursday was a rest day (not scheduled)
        val logs = listOf(
            HabitLogEntity(habitId = 1L, epochDay = friday, isCompleted = true),
            HabitLogEntity(habitId = 1L, epochDay = wednesday, isCompleted = true)
        )

        val result = StreakCalculator.calculateStreaks(
            habits = listOf(mwfHabit),
            logs = logs,
            todayEpochDay = friday,
            goldRepairEnabled = true
        )

        // Rest days are skipped and don't break streak: 2 completed scheduled days
        assertEquals(2, result.currentStreak)
    }
}
