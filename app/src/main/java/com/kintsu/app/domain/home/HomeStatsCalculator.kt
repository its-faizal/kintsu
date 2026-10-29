package com.kintsu.app.domain.home

import com.kintsu.app.data.model.FocusSessionEntity
import com.kintsu.app.data.model.HabitEntity
import com.kintsu.app.data.model.HabitLogEntity
import com.kintsu.app.data.model.TaskEntity
import com.kintsu.app.domain.StreakCalculator
import java.time.LocalDate

data class HomeKeyNumbers(
    val currentStreak: Int,
    val tasksDueToday: Int,
    val focusMinutesToday: Int
)

object HomeStatsCalculator {

    fun calculate(
        habits: List<HabitEntity>,
        logs: List<HabitLogEntity>,
        tasks: List<TaskEntity>,
        focusSessions: List<FocusSessionEntity>,
        todayEpochDay: Long = LocalDate.now().toEpochDay(),
        nowEpochMillis: Long = System.currentTimeMillis(),
        goldRepairEnabled: Boolean = true
    ): HomeKeyNumbers {
        val streakResult = StreakCalculator.calculateStreaks(
            habits = habits,
            logs = logs,
            todayEpochDay = todayEpochDay,
            nowEpochMillis = nowEpochMillis,
            goldRepairEnabled = goldRepairEnabled
        )

        val dueCount = tasks.count { task ->
            !task.isCompleted && (
                (task.dueDateEpochDay != null && task.dueDateEpochDay <= todayEpochDay) ||
                task.plannedDateEpochDay == todayEpochDay
            )
        }

        val focusMins = focusSessions
            .filter { it.dateEpochDay == todayEpochDay && it.isCompleted }
            .sumOf { it.durationMinutes }

        return HomeKeyNumbers(
            currentStreak = streakResult.currentStreak,
            tasksDueToday = dueCount,
            focusMinutesToday = focusMins
        )
    }
}
