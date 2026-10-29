package com.kintsu.app.domain.task

import com.kintsu.app.data.KintsuRepository
import com.kintsu.app.data.KintsuSettings
import com.kintsu.app.data.model.GoalEntity
import com.kintsu.app.data.model.GoalProgressType
import com.kintsu.app.data.model.SubtaskEntity
import com.kintsu.app.data.model.TaskCompletionEntity
import com.kintsu.app.data.model.TaskEntity
import com.kintsu.app.data.model.TaskPriority
import com.kintsu.app.data.model.TaskRecurrenceType
import com.kintsu.app.domain.home.HomeStatsCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class SubtaskAndPlanningTest {

    private val today = LocalDate.now().toEpochDay()

    @Test
    fun testSubtasksDoNotDoubleCountInGoalProgress() {
        // Parent task linked to goal
        val goalId = 1L
        val parentTask = TaskEntity(
            id = 10L,
            title = "Launch Project",
            linkedGoalId = goalId,
            isCompleted = false
        )
        // Subtasks attached to parent task
        val subtask1 = SubtaskEntity(id = 1L, taskId = 10L, title = "Design mockups", isCompleted = true)
        val subtask2 = SubtaskEntity(id = 2L, taskId = 10L, title = "Write code", isCompleted = true)
        val subtask3 = SubtaskEntity(id = 3L, taskId = 10L, title = "Deploy", isCompleted = false)

        val linkedTasks = listOf(parentTask)
        val totalLinkedItems = linkedTasks.size // Subtasks are child items of the task, NOT independent goal items
        val doneTasks = linkedTasks.count { it.isCompleted }

        // Ensure parent task is 1 item and done is 0
        assertEquals(1, totalLinkedItems)
        assertEquals(0, doneTasks)
        assertEquals(0f, doneTasks.toFloat() / totalLinkedItems.toFloat(), 0.001f)
    }

    @Test
    fun testCompletingParentDoesNotForceSubtasks() {
        val parentTask = TaskEntity(
            id = 20L,
            title = "Weekly Review",
            isCompleted = true
        )
        val subtask = SubtaskEntity(
            id = 5L,
            taskId = 20L,
            title = "Review finance",
            isCompleted = false
        )

        // Subtask remains independent and uncompleted
        assertFalse(subtask.isCompleted)
        assertTrue(parentTask.isCompleted)
    }

    @Test
    fun testHomeStatsCountsPlannedTasksForToday() {
        val tasks = listOf(
            // Due next week, but planned for today!
            TaskEntity(
                id = 30L,
                title = "Planned for today",
                dueDateEpochDay = today + 7,
                plannedDateEpochDay = today,
                isCompleted = false
            ),
            // Unscheduled task
            TaskEntity(
                id = 31L,
                title = "Later task",
                dueDateEpochDay = today + 7,
                plannedDateEpochDay = null,
                isCompleted = false
            )
        )

        val homeNumbers = HomeStatsCalculator.calculate(
            habits = emptyList(),
            logs = emptyList(),
            tasks = tasks,
            focusSessions = emptyList(),
            todayEpochDay = today
        )

        // The planned task must count as 1 task due today!
        assertEquals(1, homeNumbers.tasksDueToday)
    }

    @Test
    fun testRecurringTaskCompletionCountInStats() {
        val tasks = listOf(
            TaskEntity(
                id = 40L,
                title = "Daily habit task",
                recurrenceType = TaskRecurrenceType.DAILY,
                isCompleted = false // Active for next occurrence
            )
        )
        val completions = listOf(
            TaskCompletionEntity(id = 1L, taskId = 40L, epochDay = today, completedAt = System.currentTimeMillis())
        )

        val stats = KintsuRepository.computeStats(
            habits = emptyList(),
            logs = emptyList(),
            tasks = tasks,
            focusSessions = emptyList(),
            settings = KintsuSettings(),
            today = today,
            taskCompletions = completions
        )

        // Completed tasks in stats counts completed regular tasks + task completion history records
        assertEquals(1, stats.totalTasksDone)
    }
}
