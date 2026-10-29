package com.kintsu.app.domain

import com.kintsu.app.data.model.FocusSessionEntity
import com.kintsu.app.data.model.FrequencyType
import com.kintsu.app.data.model.HabitEntity
import com.kintsu.app.data.model.HabitLogEntity
import com.kintsu.app.domain.review.WeeklyReviewGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class WeeklyReviewGeneratorTest {

    @Test
    fun `week range computes correctly for monday and sunday starts`() {
        // 2026-10-29 is a Thursday
        val thursday = LocalDate.of(2026, 10, 29).toEpochDay()

        // Monday start: 2026-10-26 (Monday) to 2026-11-01 (Sunday)
        val (monStart, monEnd) = WeeklyReviewGenerator.getWeekRange(thursday, weekStartMonday = true)
        assertEquals(LocalDate.of(2026, 10, 26).toEpochDay(), monStart)
        assertEquals(LocalDate.of(2026, 11, 1).toEpochDay(), monEnd)

        // Sunday start: 2026-10-25 (Sunday) to 2026-10-31 (Saturday)
        val (sunStart, sunEnd) = WeeklyReviewGenerator.getWeekRange(thursday, weekStartMonday = false)
        assertEquals(LocalDate.of(2026, 10, 25).toEpochDay(), sunStart)
        assertEquals(LocalDate.of(2026, 10, 31).toEpochDay(), sunEnd)
    }

    @Test
    fun `generates review from real stored data`() {
        val today = LocalDate.of(2026, 10, 29).toEpochDay()
        val (weekStart, _) = WeeklyReviewGenerator.getWeekRange(today, weekStartMonday = true)

        val habit1 = HabitEntity(id = 1, name = "Tea Ceremony", frequencyType = FrequencyType.DAILY, weekdaysMask = 127)
        val habit2 = HabitEntity(id = 2, name = "Running", frequencyType = FrequencyType.DAILY, weekdaysMask = 127)

        val logs = listOf(
            HabitLogEntity(habitId = 1, epochDay = weekStart, isCompleted = true),
            HabitLogEntity(habitId = 1, epochDay = weekStart + 1, isCompleted = true),
            HabitLogEntity(habitId = 1, epochDay = weekStart + 2, isCompleted = true, isRepairedGold = true),
            HabitLogEntity(habitId = 2, epochDay = weekStart, isCompleted = false, isCrack = true)
        )

        val focusSessions = listOf(
            FocusSessionEntity(
                id = 1,
                dateEpochDay = weekStart + 1,
                durationMinutes = 45,
                isCompleted = true,
                startTimeMillis = 1000L,
                endTimeMillis = 5000L
            )
        )

        val streakResult = StreakResult(
            currentStreak = 3,
            bestStreak = 5,
            totalRepairs = 1,
            totalCracks = 1,
            activeRepairAvailable = false
        )

        val review = WeeklyReviewGenerator.generateReviewDraft(
            habits = listOf(habit1, habit2),
            logs = logs,
            goals = emptyList(),
            tasks = emptyList(),
            focusSessions = focusSessions,
            todayEpochDay = today,
            weekStartMonday = true,
            streakResult = streakResult
        )

        assertNotNull(review)
        assertEquals(45, review.focusMinutes)
        assertEquals(1, review.totalCracks)
        assertEquals(1, review.totalRepairs)
        assertEquals("Tea Ceremony", review.mostConsistentHabitName)
        assertEquals(5, review.bestStreak)
    }
}
