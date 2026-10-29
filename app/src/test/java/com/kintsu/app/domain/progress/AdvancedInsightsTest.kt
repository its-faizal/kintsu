package com.kintsu.app.domain.progress

import com.kintsu.app.data.model.FocusSessionEntity
import com.kintsu.app.data.model.FrequencyType
import com.kintsu.app.data.model.GoalEntity
import com.kintsu.app.data.model.GoalProgressType
import com.kintsu.app.data.model.HabitEntity
import com.kintsu.app.data.model.HabitLogEntity
import com.kintsu.app.data.model.TaskCompletionEntity
import com.kintsu.app.data.model.TaskEntity
import com.kintsu.app.data.model.TaskPriority
import com.kintsu.app.data.model.TaskRecurrenceType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class AdvancedInsightsTest {

    private val today = LocalDate.of(2026, 10, 29).toEpochDay() // A Thursday

    @Test
    fun testHabitConsistencyAccountsForScheduledFrequency() {
        // Habit scheduled ONLY on Mon, Wed, Fri (bit 0, bit 2, bit 4 -> 1 + 4 + 16 = 21)
        val habit = HabitEntity(
            id = 1L,
            name = "Gym Workout",
            frequencyType = FrequencyType.SPECIFIC_DAYS,
            weekdaysMask = 21,
            createdAt = (today - 30) * 86_400_000L
        )

        // Range of 7 days: today-6 to today (Friday Oct 23 to Thursday Oct 29)
        // Days in this 7-day window:
        // Oct 23 (Fri) - scheduled
        // Oct 24 (Sat) - NOT scheduled
        // Oct 25 (Sun) - NOT scheduled
        // Oct 26 (Mon) - scheduled
        // Oct 27 (Tue) - NOT scheduled
        // Oct 28 (Wed) - scheduled
        // Oct 29 (Thu) - NOT scheduled
        // Total eligible days = 3 (Fri, Mon, Wed)

        val logs = listOf(
            HabitLogEntity(habitId = 1L, epochDay = today - 6, isCompleted = true), // Oct 23 (Fri)
            HabitLogEntity(habitId = 1L, epochDay = today - 3, isCompleted = true), // Oct 26 (Mon)
            HabitLogEntity(habitId = 1L, epochDay = today - 1, isCompleted = true)  // Oct 28 (Wed)
        )

        val insights = AdvancedInsightsCalculator.calculate(
            habits = listOf(habit),
            logs = logs,
            tasks = emptyList(),
            taskCompletions = emptyList(),
            goals = emptyList(),
            focusSessions = emptyList(),
            todayEpochDay = today
        )

        val metric = insights.habitConsistencies.find { it.habitId == 1L }!!
        // 3 completed out of 3 eligible days = 100% completion rate (NOT 3/7 = 42%!)
        assertEquals(1.0f, metric.rate7Days, 0.001f)
    }

    @Test
    fun testTaskProductivityCountsRecurringOccurrences() {
        val nonRecurringTask = TaskEntity(
            id = 10L,
            title = "File taxes",
            priority = TaskPriority.HIGH,
            isCompleted = true,
            completedAt = today * 86_400_000L
        )
        val recurringTask = TaskEntity(
            id = 20L,
            title = "Water plants",
            priority = TaskPriority.LOW,
            recurrenceType = TaskRecurrenceType.DAILY,
            isCompleted = false // Template remains active
        )
        val recurringCompletions = listOf(
            TaskCompletionEntity(id = 1L, taskId = 20L, epochDay = today, completedAt = today * 86_400_000L),
            TaskCompletionEntity(id = 2L, taskId = 20L, epochDay = today - 2, completedAt = (today - 2) * 86_400_000L)
        )

        val insights = AdvancedInsightsCalculator.calculate(
            habits = emptyList(),
            logs = emptyList(),
            tasks = listOf(nonRecurringTask, recurringTask),
            taskCompletions = recurringCompletions,
            goals = emptyList(),
            focusSessions = emptyList(),
            todayEpochDay = today
        )

        // Total completed this week = 1 non-recurring + 2 recurring occurrences = 3
        assertEquals(3, insights.taskProductivity.completedThisWeek)
        assertEquals(1, insights.taskProductivity.highPriorityCompleted)
        assertEquals(2, insights.taskProductivity.lowPriorityCompleted)
    }

    @Test
    fun testGoalInsightsApproachingDeadline() {
        val goal1 = GoalEntity(
            id = 100L,
            title = "Finish marathon training",
            targetDateEpochDay = today + 5, // In 5 days: approaching deadline!
            progressType = GoalProgressType.MANUAL,
            manualProgressPercent = 80
        )
        val goal2 = GoalEntity(
            id = 101L,
            title = "Read 50 books",
            targetDateEpochDay = today + 100, // In 100 days
            progressType = GoalProgressType.MANUAL,
            manualProgressPercent = 100 // Achieved!
        )

        val insights = AdvancedInsightsCalculator.calculate(
            habits = emptyList(),
            logs = emptyList(),
            tasks = emptyList(),
            taskCompletions = emptyList(),
            goals = listOf(goal1, goal2),
            focusSessions = emptyList(),
            todayEpochDay = today
        )

        assertEquals(1, insights.goalInsights.activeGoalsCount)
        assertEquals(1, insights.goalInsights.completedGoalsCount)
        assertEquals(1, insights.goalInsights.approachingDeadlineCount)
        assertEquals(0.9f, insights.goalInsights.averageProgress, 0.001f)
    }

    @Test
    fun testFocusInsightsAggregation() {
        val sessions = listOf(
            FocusSessionEntity(id = 1L, dateEpochDay = today, durationMinutes = 25, isCompleted = true),
            FocusSessionEntity(id = 2L, dateEpochDay = today - 1, durationMinutes = 45, isCompleted = true),
            FocusSessionEntity(id = 3L, dateEpochDay = today - 2, durationMinutes = 15, isCompleted = false) // abandoned session
        )

        val insights = AdvancedInsightsCalculator.calculate(
            habits = emptyList(),
            logs = emptyList(),
            tasks = emptyList(),
            taskCompletions = emptyList(),
            goals = emptyList(),
            focusSessions = sessions,
            todayEpochDay = today
        )

        // Only completed sessions (25 + 45 = 70 min, 2 sessions, avg = 35)
        assertEquals(70, insights.focusInsights.totalFocusMinutes)
        assertEquals(2, insights.focusInsights.completedSessionsCount)
        assertEquals(35, insights.focusInsights.averageSessionDuration)
    }

    @Test
    fun testPersonalHighlightsGeneration() {
        val habit = HabitEntity(
            id = 1L,
            name = "Morning Journal",
            frequencyType = FrequencyType.DAILY,
            createdAt = (today - 40) * 86_400_000L
        )
        // 25 completed logs out of 30 days = 83%
        val logs = (0 until 25).map { offset ->
            HabitLogEntity(habitId = 1L, epochDay = today - offset, isCompleted = true)
        }

        val insights = AdvancedInsightsCalculator.calculate(
            habits = listOf(habit),
            logs = logs,
            tasks = emptyList(),
            taskCompletions = emptyList(),
            goals = emptyList(),
            focusSessions = emptyList(),
            todayEpochDay = today
        )

        assertTrue(insights.highlights.any { it.category == "RITUAL" })
        assertTrue(insights.highlights.any { it.title.contains("Consistent") })
    }
}
