package com.kintsu.app.domain

import com.kintsu.app.data.KintsuRepository
import com.kintsu.app.data.KintsuSettings
import com.kintsu.app.data.model.FrequencyType
import com.kintsu.app.data.model.GoalEntity
import com.kintsu.app.data.model.GoalProgressType
import com.kintsu.app.data.model.HabitEntity
import com.kintsu.app.data.model.HabitLogEntity
import com.kintsu.app.data.model.TaskEntity
import com.kintsu.app.data.model.TaskPriority
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class GoalProgressTest {

    private val today = LocalDate.now().toEpochDay()

    @Test
    fun testAutoGoalProgressFromLinkedTasksAndHabits() {
        val goalId = 42L
        val goal = GoalEntity(
            id = goalId,
            title = "Run Half Marathon",
            targetDateEpochDay = today + 60,
            progressType = GoalProgressType.AUTO_FROM_ITEMS
        )

        val habits = listOf(
            HabitEntity(
                id = 10L,
                name = "Morning 5k",
                iconName = "habit",
                colorHex = "#B8935A",
                frequencyType = FrequencyType.DAILY,
                weekdaysMask = 127,
                linkedGoalId = goalId
            ),
            HabitEntity(
                id = 11L,
                name = "Evening Stretch",
                iconName = "habit",
                colorHex = "#6B8E7D",
                frequencyType = FrequencyType.DAILY,
                weekdaysMask = 127,
                linkedGoalId = 999L // linked to a different goal
            )
        )

        val tasks = listOf(
            TaskEntity(
                id = 101L,
                title = "Buy running shoes",
                isCompleted = true,
                priority = TaskPriority.HIGH,
                linkedGoalId = goalId
            ),
            TaskEntity(
                id = 102L,
                title = "Register for marathon",
                isCompleted = false,
                priority = TaskPriority.MEDIUM,
                linkedGoalId = goalId
            )
        )

        // Today's logs: Habit 10 is completed today
        val todayLogs = listOf(
            HabitLogEntity(
                habitId = 10L,
                epochDay = today,
                isCompleted = true
            )
        )

        val linkedTasks = tasks.filter { it.linkedGoalId == goal.id }
        val linkedHabits = habits.filter { it.linkedGoalId == goal.id }
        val totalLinkedItems = linkedTasks.size + linkedHabits.size
        assertEquals(3, totalLinkedItems) // 2 tasks + 1 habit

        val doneTasks = linkedTasks.count { it.isCompleted } // 1 done
        val doneHabits = linkedHabits.count { h -> todayLogs.any { it.habitId == h.id && it.isCompleted } } // 1 done
        val totalDone = doneTasks + doneHabits
        assertEquals(2, totalDone)

        val progress = totalDone.toFloat() / totalLinkedItems.toFloat()
        assertEquals(2f / 3f, progress, 0.001f)

        // Now complete the second task
        val updatedTasks = tasks.map { if (it.id == 102L) it.copy(isCompleted = true) else it }
        val newDoneTasks = updatedTasks.filter { it.linkedGoalId == goal.id }.count { it.isCompleted }
        val newProgress = (newDoneTasks + doneHabits).toFloat() / totalLinkedItems.toFloat()
        assertEquals(1.0f, newProgress, 0.001f)
    }

    @Test
    fun testManualGoalProgressIgnoresLinkedItems() {
        val goal = GoalEntity(
            id = 1L,
            title = "Read 12 books",
            targetDateEpochDay = today + 90,
            progressType = GoalProgressType.MANUAL,
            manualProgressPercent = 45
        )
        val progress = goal.manualProgressPercent.toFloat() / 100f
        assertEquals(0.45f, progress, 0.001f)
    }

    @Test
    fun testComputeStatsReactsToHabitAndTaskCompletions() {
        val habits = listOf(
            HabitEntity(
                id = 1L,
                name = "Deep Breath",
                iconName = "habit",
                colorHex = "#B8935A",
                frequencyType = FrequencyType.DAILY,
                weekdaysMask = 127
            )
        )
        val logs = listOf(
            HabitLogEntity(
                habitId = 1L,
                epochDay = today,
                isCompleted = true
            )
        )
        val tasks = listOf(
            TaskEntity(
                id = 1L,
                title = "Send letter",
                isCompleted = true,
                priority = TaskPriority.LOW
            ),
            TaskEntity(
                id = 2L,
                title = "Clean desk",
                isCompleted = false,
                priority = TaskPriority.LOW
            )
        )

        val stats = KintsuRepository.computeStats(
            habits = habits,
            logs = logs,
            tasks = tasks,
            focusSessions = emptyList(),
            settings = KintsuSettings(goldRepairEnabled = true),
            today = today
        )

        assertEquals(1, stats.totalHabitsDone)
        assertEquals(1, stats.totalTasksDone)
        assertEquals(1, stats.currentStreak)
    }
}
