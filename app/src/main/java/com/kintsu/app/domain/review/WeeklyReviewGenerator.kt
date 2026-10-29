package com.kintsu.app.domain.review

import com.kintsu.app.data.model.FocusSessionEntity
import com.kintsu.app.data.model.GoalEntity
import com.kintsu.app.data.model.GoalProgressType
import com.kintsu.app.data.model.HabitEntity
import com.kintsu.app.data.model.HabitLogEntity
import com.kintsu.app.data.model.TaskEntity
import com.kintsu.app.data.model.WeeklyReviewEntity
import com.kintsu.app.domain.GoldRepairManager
import com.kintsu.app.domain.StreakResult
import java.time.DayOfWeek
import java.time.LocalDate

object WeeklyReviewGenerator {

    const val PROMPT_1 = "What brought calm or steady energy to your rhythm this week?"
    const val PROMPT_2 = "Where did cracks appear, and how did you care for them?"
    const val PROMPT_3 = "What intention or ritual do you carry into the coming week?"

    fun getWeekRange(todayEpochDay: Long, weekStartMonday: Boolean): Pair<Long, Long> {
        val today = LocalDate.ofEpochDay(todayEpochDay)
        val targetStartDay = if (weekStartMonday) DayOfWeek.MONDAY else DayOfWeek.SUNDAY

        var current = today
        while (current.dayOfWeek != targetStartDay) {
            current = current.minusDays(1)
        }
        val startEpoch = current.toEpochDay()
        val endEpoch = startEpoch + 6
        return Pair(startEpoch, endEpoch)
    }

    fun isEndOfWeek(todayEpochDay: Long, weekStartMonday: Boolean): Boolean {
        val today = LocalDate.ofEpochDay(todayEpochDay)
        val endDay = if (weekStartMonday) DayOfWeek.SUNDAY else DayOfWeek.SATURDAY
        return today.dayOfWeek == endDay
    }

    fun generateReviewDraft(
        habits: List<HabitEntity>,
        logs: List<HabitLogEntity>,
        goals: List<GoalEntity>,
        tasks: List<TaskEntity>,
        focusSessions: List<FocusSessionEntity>,
        todayEpochDay: Long,
        weekStartMonday: Boolean,
        streakResult: StreakResult
    ): WeeklyReviewEntity {
        val (weekStart, weekEnd) = getWeekRange(todayEpochDay, weekStartMonday)
        val weekRange = (weekStart..weekEnd)

        // Filter logs in this week
        val weekLogs = logs.filter { weekRange.contains(it.epochDay) }

        // Scheduled habit days in week
        var totalScheduledDays = 0
        var totalCompletedDays = 0

        val habitConsistencyMap = mutableMapOf<String, Pair<Int, Int>>() // name -> (completed, scheduled)

        habits.filter { !it.isArchived }.forEach { h ->
            var hScheduled = 0
            var hCompleted = 0
            for (day in weekRange) {
                if (GoldRepairManager.isHabitScheduledOn(h, day)) {
                    hScheduled++
                    val log = weekLogs.find { it.habitId == h.id && it.epochDay == day }
                    if (log?.isCompleted == true) {
                        hCompleted++
                    }
                }
            }
            if (hScheduled > 0) {
                totalScheduledDays += hScheduled
                totalCompletedDays += hCompleted
                habitConsistencyMap[h.name] = Pair(hCompleted, hScheduled)
            }
        }

        val completionRate = if (totalScheduledDays > 0) {
            (totalCompletedDays.toFloat() / totalScheduledDays.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }

        // Cracks and repairs in week
        val totalCracks = weekLogs.count { it.isCrack }
        val totalRepairs = weekLogs.count { it.isRepairedGold }

        // Focus minutes in week
        val focusMinutes = focusSessions
            .filter { it.isCompleted && weekRange.contains(it.dateEpochDay) }
            .sumOf { it.durationMinutes }

        // Most and least consistent habit
        var mostConsistent: String? = null
        var leastConsistent: String? = null
        var maxRate = -1f
        var minRate = 2f

        habitConsistencyMap.forEach { (name, counts) ->
            val rate = counts.first.toFloat() / counts.second.toFloat()
            if (rate > maxRate) {
                maxRate = rate
                mostConsistent = name
            }
            if (rate < minRate) {
                minRate = rate
                leastConsistent = name
            }
        }

        if (habitConsistencyMap.size <= 1 || maxRate == minRate) {
            leastConsistent = null
        }

        // Goals progress summary
        val activeGoals = goals
        val goalsSummary = if (activeGoals.isEmpty()) {
            "No active horizon goals"
        } else {
            val completedGoals = activeGoals.count { g ->
                if (g.progressType == GoalProgressType.MANUAL) {
                    g.manualProgressPercent >= 100
                } else {
                    val linked = tasks.filter { it.linkedGoalId == g.id }
                    linked.isNotEmpty() && linked.all { it.isCompleted }
                }
            }
            "$completedGoals of ${activeGoals.size} goals fulfilled"
        }

        return WeeklyReviewEntity(
            weekStartEpochDay = weekStart,
            weekEndEpochDay = weekEnd,
            completionRate = completionRate,
            bestStreak = streakResult.bestStreak,
            totalCracks = totalCracks,
            totalRepairs = totalRepairs,
            focusMinutes = focusMinutes,
            mostConsistentHabitName = mostConsistent,
            leastConsistentHabitName = leastConsistent,
            goalsProgressSummary = goalsSummary,
            reflection1 = "",
            reflection2 = "",
            reflection3 = ""
        )
    }
}
