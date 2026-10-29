package com.kintsu.app.domain.home

import com.kintsu.app.data.model.FocusSessionEntity
import com.kintsu.app.data.model.FrequencyType
import com.kintsu.app.data.model.HabitEntity
import com.kintsu.app.data.model.HabitLogEntity
import com.kintsu.app.data.model.TaskEntity
import com.kintsu.app.data.model.TaskPriority
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.system.measureTimeMillis

class HomePerformanceTest {

    @Test
    fun `deriving home stats from large database executes well under 50ms`() {
        val today = 20000L
        val habits = (1L..10L).map { id ->
            HabitEntity(
                id = id,
                name = "Habit #$id",
                iconName = "star",
                colorHex = "#B8935A",
                frequencyType = FrequencyType.DAILY,
                weekdaysMask = 127
            )
        }

        // 1,500 logs spanning 150 days
        val logs = mutableListOf<HabitLogEntity>()
        for (day in (today - 150)..today) {
            for (h in habits) {
                logs.add(
                    HabitLogEntity(
                        habitId = h.id,
                        epochDay = day,
                        isCompleted = (day % 3L != 0L)
                    )
                )
            }
        }

        // 400 tasks
        val tasks = (1..400).map { id ->
            TaskEntity(
                id = id.toLong(),
                title = "Task $id",
                dueDateEpochDay = today - (id % 30) + 10,
                isCompleted = (id % 2 == 0),
                priority = TaskPriority.MEDIUM
            )
        }

        // 300 focus sessions
        val focusSessions = (1..300).map { id ->
            FocusSessionEntity(
                id = id.toLong(),
                dateEpochDay = today - (id % 30),
                durationMinutes = 25,
                isCompleted = true,
                startTimeMillis = 1000L,
                endTimeMillis = 26000L
            )
        }

        // Warm up JVM JIT
        repeat(5) {
            HomeStatsCalculator.calculate(habits, logs, tasks, focusSessions, today)
        }

        val elapsedMs = measureTimeMillis {
            val stats = HomeStatsCalculator.calculate(habits, logs, tasks, focusSessions, today)
            assertTrue(stats.tasksDueToday >= 0)
        }

        println("HomeStats derivation time for 1,500 logs + 400 tasks + 300 focus sessions: ${elapsedMs}ms")
        assertTrue("Derivation took ${elapsedMs}ms, which exceeds 50ms limit", elapsedMs < 50)
    }
}
