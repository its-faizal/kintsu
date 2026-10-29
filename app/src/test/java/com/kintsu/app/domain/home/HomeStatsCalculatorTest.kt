package com.kintsu.app.domain.home

import com.kintsu.app.data.model.FocusSessionEntity
import com.kintsu.app.data.model.FrequencyType
import com.kintsu.app.data.model.HabitEntity
import com.kintsu.app.data.model.HabitLogEntity
import com.kintsu.app.data.model.TaskEntity
import com.kintsu.app.data.model.TaskPriority
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class HomeStatsCalculatorTest {

    private val baseHabit = HabitEntity(
        id = 1L,
        name = "Morning Tea",
        iconName = "cup",
        colorHex = "#B8935A",
        frequencyType = FrequencyType.DAILY,
        weekdaysMask = 127,
        createdAt = 10000L * 86_400_000L
    )

    @Test
    fun `empty state returns zeroes`() {
        val stats = HomeStatsCalculator.calculate(
            habits = emptyList(),
            logs = emptyList(),
            tasks = emptyList(),
            focusSessions = emptyList(),
            todayEpochDay = 20000L
        )

        assertEquals(0, stats.currentStreak)
        assertEquals(0, stats.tasksDueToday)
        assertEquals(0, stats.focusMinutesToday)
    }

    @Test
    fun `calculates streak, due tasks, and focus minutes correctly`() {
        val today = 20000L
        val logs = listOf(
            HabitLogEntity(habitId = 1L, epochDay = today, isCompleted = true),
            HabitLogEntity(habitId = 1L, epochDay = today - 1, isCompleted = true),
            HabitLogEntity(habitId = 1L, epochDay = today - 2, isCompleted = true)
        )

        val tasks = listOf(
            TaskEntity(id = 1, title = "Task Due Today", dueDateEpochDay = today, isCompleted = false, priority = TaskPriority.HIGH),
            TaskEntity(id = 2, title = "Overdue Task", dueDateEpochDay = today - 2, isCompleted = false, priority = TaskPriority.MEDIUM),
            TaskEntity(id = 3, title = "Completed Task", dueDateEpochDay = today, isCompleted = true, priority = TaskPriority.LOW),
            TaskEntity(id = 4, title = "Future Task", dueDateEpochDay = today + 2, isCompleted = false, priority = TaskPriority.LOW),
            TaskEntity(id = 5, title = "No Due Date Task", dueDateEpochDay = null, isCompleted = false, priority = TaskPriority.LOW)
        )

        val focusSessions = listOf(
            FocusSessionEntity(id = 1, dateEpochDay = today, durationMinutes = 25, isCompleted = true, startTimeMillis = 1000L, endTimeMillis = 25000L),
            FocusSessionEntity(id = 2, dateEpochDay = today, durationMinutes = 15, isCompleted = true, startTimeMillis = 30000L, endTimeMillis = 45000L),
            FocusSessionEntity(id = 3, dateEpochDay = today, durationMinutes = 30, isCompleted = false, startTimeMillis = 50000L, endTimeMillis = 55000L),
            FocusSessionEntity(id = 4, dateEpochDay = today - 1, durationMinutes = 50, isCompleted = true, startTimeMillis = 0L, endTimeMillis = 100L)
        )

        val stats = HomeStatsCalculator.calculate(
            habits = listOf(baseHabit),
            logs = logs,
            tasks = tasks,
            focusSessions = focusSessions,
            todayEpochDay = today
        )

        // 3 consecutive completed days = streak 3
        assertEquals(3, stats.currentStreak)
        // Task 1 (due today) + Task 2 (overdue) = 2
        assertEquals(2, stats.tasksDueToday)
        // Session 1 (25) + Session 2 (15) = 40 mins (Session 3 incomplete, Session 4 yesterday)
        assertEquals(40, stats.focusMinutesToday)
    }

    @Test
    fun `midnight rollover correctly transitions tasks and focus sessions`() {
        val day1 = 20000L
        val day2 = 20001L

        val tasks = listOf(
            TaskEntity(id = 1, title = "Due on Day 2", dueDateEpochDay = day2, isCompleted = false, priority = TaskPriority.HIGH)
        )

        val focusSessions = listOf(
            FocusSessionEntity(id = 1, dateEpochDay = day1, durationMinutes = 45, isCompleted = true, startTimeMillis = 100L, endTimeMillis = 200L),
            FocusSessionEntity(id = 2, dateEpochDay = day2, durationMinutes = 20, isCompleted = true, startTimeMillis = 300L, endTimeMillis = 400L)
        )

        // On day 1: Task 1 is future, day 1 focus is 45 min
        val statsDay1 = HomeStatsCalculator.calculate(
            habits = listOf(baseHabit),
            logs = emptyList(),
            tasks = tasks,
            focusSessions = focusSessions,
            todayEpochDay = day1
        )
        assertEquals(0, statsDay1.tasksDueToday)
        assertEquals(45, statsDay1.focusMinutesToday)

        // On day 2 (midnight rollover): Task 1 is now due today, day 2 focus is 20 min
        val statsDay2 = HomeStatsCalculator.calculate(
            habits = listOf(baseHabit),
            logs = emptyList(),
            tasks = tasks,
            focusSessions = focusSessions,
            todayEpochDay = day2
        )
        assertEquals(1, statsDay2.tasksDueToday)
        assertEquals(20, statsDay2.focusMinutesToday)
    }
}
