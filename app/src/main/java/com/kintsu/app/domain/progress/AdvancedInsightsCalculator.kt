package com.kintsu.app.domain.progress

import com.kintsu.app.data.model.FocusSessionEntity
import com.kintsu.app.data.model.GoalEntity
import com.kintsu.app.data.model.GoalProgressType
import com.kintsu.app.data.model.HabitEntity
import com.kintsu.app.data.model.HabitLogEntity
import com.kintsu.app.data.model.TaskCompletionEntity
import com.kintsu.app.data.model.TaskEntity
import com.kintsu.app.data.model.TaskPriority
import com.kintsu.app.domain.GoldRepairManager
import java.time.DayOfWeek
import java.time.LocalDate

data class HabitConsistencyMetric(
    val habitId: Long,
    val habitName: String,
    val colorHex: String,
    val rate7Days: Float,
    val rate30Days: Float,
    val rate90Days: Float,
    val eligible30Days: Int,
    val completed30Days: Int
)

data class WeeklyTrendPoint(
    val weekLabel: String,
    val weekStartEpochDay: Long,
    val completionRate: Float,
    val habitsDone: Int,
    val habitsEligible: Int,
    val tasksDone: Int
)

data class TaskProductivityMetrics(
    val completedThisWeek: Int,
    val completedThisMonth: Int,
    val overdueCount: Int,
    val highPriorityCompleted: Int,
    val mediumPriorityCompleted: Int,
    val lowPriorityCompleted: Int,
    val totalPending: Int
)

data class GoalInsightMetrics(
    val activeGoalsCount: Int,
    val completedGoalsCount: Int,
    val approachingDeadlineCount: Int,
    val averageProgress: Float
)

data class FocusInsightMetrics(
    val totalFocusMinutes: Int,
    val completedSessionsCount: Int,
    val averageSessionDuration: Int,
    val weeklyFocusMinutes: Int
)

data class PersonalHighlight(
    val title: String,
    val description: String,
    val category: String
)

data class AdvancedProgressInsights(
    val habitConsistencies: List<HabitConsistencyMetric>,
    val overallRate7Days: Float,
    val overallRate30Days: Float,
    val overallRate90Days: Float,
    val weeklyTrends: List<WeeklyTrendPoint>,
    val taskProductivity: TaskProductivityMetrics,
    val goalInsights: GoalInsightMetrics,
    val focusInsights: FocusInsightMetrics,
    val highlights: List<PersonalHighlight>
)

object AdvancedInsightsCalculator {

    fun calculate(
        habits: List<HabitEntity>,
        logs: List<HabitLogEntity>,
        tasks: List<TaskEntity>,
        taskCompletions: List<TaskCompletionEntity>,
        goals: List<GoalEntity>,
        focusSessions: List<FocusSessionEntity>,
        todayEpochDay: Long = LocalDate.now().toEpochDay()
    ): AdvancedProgressInsights {
        val activeHabits = habits.filter { !it.isArchived }

        // 1. Habit consistency calculations (7, 30, 90 days)
        val range7 = (todayEpochDay - 6..todayEpochDay)
        val range30 = (todayEpochDay - 29..todayEpochDay)
        val range90 = (todayEpochDay - 89..todayEpochDay)

        val habitMetrics = activeHabits.map { habit ->
            val eligible7 = range7.count { day -> GoldRepairManager.isHabitScheduledOn(habit, day) }
            val completed7 = logs.count { it.habitId == habit.id && (it.isCompleted || it.isRepairedGold) && it.epochDay in range7 }
            val rate7 = if (eligible7 > 0) (completed7.toFloat() / eligible7).coerceIn(0f, 1f) else 0f

            val eligible30 = range30.count { day -> GoldRepairManager.isHabitScheduledOn(habit, day) }
            val completed30 = logs.count { it.habitId == habit.id && (it.isCompleted || it.isRepairedGold) && it.epochDay in range30 }
            val rate30 = if (eligible30 > 0) (completed30.toFloat() / eligible30).coerceIn(0f, 1f) else 0f

            val eligible90 = range90.count { day -> GoldRepairManager.isHabitScheduledOn(habit, day) }
            val completed90 = logs.count { it.habitId == habit.id && (it.isCompleted || it.isRepairedGold) && it.epochDay in range90 }
            val rate90 = if (eligible90 > 0) (completed90.toFloat() / eligible90).coerceIn(0f, 1f) else 0f

            HabitConsistencyMetric(
                habitId = habit.id,
                habitName = habit.name,
                colorHex = habit.colorHex,
                rate7Days = rate7,
                rate30Days = rate30,
                rate90Days = rate90,
                eligible30Days = eligible30,
                completed30Days = completed30
            )
        }

        // Overall completion rates across all scheduled habits
        fun overallRate(range: LongRange): Float {
            var totalEligible = 0
            var totalCompleted = 0
            for (habit in activeHabits) {
                totalEligible += range.count { day -> GoldRepairManager.isHabitScheduledOn(habit, day) }
                totalCompleted += logs.count { it.habitId == habit.id && (it.isCompleted || it.isRepairedGold) && it.epochDay in range }
            }
            return if (totalEligible > 0) (totalCompleted.toFloat() / totalEligible).coerceIn(0f, 1f) else 0f
        }

        val overall7 = overallRate(range7)
        val overall30 = overallRate(range30)
        val overall90 = overallRate(range90)

        // 2. Weekly Trend Points (Last 4 full/partial weeks)
        val weeklyTrends = mutableListOf<WeeklyTrendPoint>()
        for (w in 3 downTo 0) {
            val weekStart = todayEpochDay - (w * 7 + 6)
            val weekEnd = todayEpochDay - (w * 7)
            val weekRange = (weekStart..weekEnd)

            var weekEligible = 0
            var weekDone = 0
            for (habit in activeHabits) {
                weekEligible += weekRange.count { day -> GoldRepairManager.isHabitScheduledOn(habit, day) }
                weekDone += logs.count { it.habitId == habit.id && (it.isCompleted || it.isRepairedGold) && it.epochDay in weekRange }
            }

            // Tasks completed in this week window
            val tasksInWeek = tasks.count {
                it.isCompleted && it.completedAt != null &&
                (it.completedAt / 86_400_000L) in weekRange
            } + taskCompletions.count { it.epochDay in weekRange }

            val rate = if (weekEligible > 0) (weekDone.toFloat() / weekEligible).coerceIn(0f, 1f) else 0f
            val label = if (w == 0) "This Wk" else "${w}w ago"

            weeklyTrends.add(
                WeeklyTrendPoint(
                    weekLabel = label,
                    weekStartEpochDay = weekStart,
                    completionRate = rate,
                    habitsDone = weekDone,
                    habitsEligible = weekEligible,
                    tasksDone = tasksInWeek
                )
            )
        }

        // 3. Task Productivity Insights
        val overdueTasksCount = tasks.count { !it.isCompleted && it.dueDateEpochDay != null && it.dueDateEpochDay < todayEpochDay }
        val pendingCount = tasks.count { !it.isCompleted }

        val completedThisWeekCount = tasks.count {
            it.isCompleted && it.completedAt != null && (it.completedAt / 86_400_000L) in range7
        } + taskCompletions.count { it.epochDay in range7 }

        val completedThisMonthCount = tasks.count {
            it.isCompleted && it.completedAt != null && (it.completedAt / 86_400_000L) in range30
        } + taskCompletions.count { it.epochDay in range30 }

        // Task priority breakdown: regular completed + recurring instances
        val taskMap = tasks.associateBy { it.id }
        var highDone = tasks.count { it.isCompleted && it.priority == TaskPriority.HIGH }
        var medDone = tasks.count { it.isCompleted && it.priority == TaskPriority.MEDIUM }
        var lowDone = tasks.count { it.isCompleted && it.priority == TaskPriority.LOW }

        taskCompletions.forEach { tc ->
            when (taskMap[tc.taskId]?.priority) {
                TaskPriority.HIGH -> highDone++
                TaskPriority.LOW -> lowDone++
                else -> medDone++
            }
        }

        val taskProductivity = TaskProductivityMetrics(
            completedThisWeek = completedThisWeekCount,
            completedThisMonth = completedThisMonthCount,
            overdueCount = overdueTasksCount,
            highPriorityCompleted = highDone,
            mediumPriorityCompleted = medDone,
            lowPriorityCompleted = lowDone,
            totalPending = pendingCount
        )

        // 4. Goal Insights
        var completedGoals = 0
        var approachingDeadline = 0
        var totalProgressSum = 0f

        goals.forEach { g ->
            val linkedTasks = tasks.filter { it.linkedGoalId == g.id }
            val linkedHabits = habits.filter { it.linkedGoalId == g.id }
            val totalItems = linkedTasks.size + linkedHabits.size
            val doneItems = linkedTasks.count { it.isCompleted } + linkedHabits.count { h ->
                logs.any { it.habitId == h.id && it.epochDay == todayEpochDay && it.isCompleted }
            }

            val pFloat = if (g.progressType == GoalProgressType.AUTO_FROM_ITEMS) {
                if (totalItems == 0) 0f else doneItems.toFloat() / totalItems.toFloat()
            } else {
                g.manualProgressPercent.toFloat() / 100f
            }

            totalProgressSum += pFloat
            if (pFloat >= 1f) completedGoals++
            if (pFloat < 1f && g.targetDateEpochDay in todayEpochDay..(todayEpochDay + 14)) {
                approachingDeadline++
            }
        }

        val activeCount = goals.size - completedGoals
        val avgProgress = if (goals.isNotEmpty()) totalProgressSum / goals.size else 0f

        val goalInsights = GoalInsightMetrics(
            activeGoalsCount = activeCount.coerceAtLeast(0),
            completedGoalsCount = completedGoals,
            approachingDeadlineCount = approachingDeadline,
            averageProgress = avgProgress
        )

        // 5. Focus Insights
        val completedFocus = focusSessions.filter { it.isCompleted }
        val totalFocusMins = completedFocus.sumOf { it.durationMinutes }
        val avgDuration = if (completedFocus.isNotEmpty()) totalFocusMins / completedFocus.size else 0
        val weeklyFocus = completedFocus.filter { it.dateEpochDay in range7 }.sumOf { it.durationMinutes }

        val focusInsights = FocusInsightMetrics(
            totalFocusMinutes = totalFocusMins,
            completedSessionsCount = completedFocus.size,
            averageSessionDuration = avgDuration,
            weeklyFocusMinutes = weeklyFocus
        )

        // 6. Personal Highlights (Derived factually only from actual data)
        val highlightsList = mutableListOf<PersonalHighlight>()

        // Highlight 1: Most consistent habit with sufficient occurrences
        val bestHabit = habitMetrics
            .filter { it.eligible30Days >= 3 }
            .maxByOrNull { it.rate30Days }
        if (bestHabit != null && bestHabit.rate30Days >= 0.5f) {
            highlightsList.add(
                PersonalHighlight(
                    title = "Most Consistent Ritual",
                    description = "${bestHabit.habitName} reached ${(bestHabit.rate30Days * 100).toInt()}% consistency over the past 30 days.",
                    category = "RITUAL"
                )
            )
        }

        // Highlight 2: Strongest completion day of week
        val completedLogs = logs.filter { it.isCompleted || it.isRepairedGold }
        if (completedLogs.size >= 5) {
            val dayCounts = completedLogs
                .groupBy { LocalDate.ofEpochDay(it.epochDay).dayOfWeek }
                .mapValues { it.value.size }
            val topDay = dayCounts.maxByOrNull { it.value }
            if (topDay != null) {
                val dayName = topDay.key.name.lowercase().replaceFirstChar { it.uppercase() }
                highlightsList.add(
                    PersonalHighlight(
                        title = "Peak Momentum Day",
                        description = "$dayName tends to be your most productive day with ${topDay.value} completed check-ins.",
                        category = "MOMENTUM"
                    )
                )
            }
        }

        // Highlight 3: Focus discipline
        if (totalFocusMins >= 25) {
            highlightsList.add(
                PersonalHighlight(
                    title = "Deep Focus Practice",
                    description = "You have dedicated $totalFocusMins minutes across ${completedFocus.size} mindful sessions.",
                    category = "FOCUS"
                )
            )
        }

        // Highlight 4: Task flow
        if (completedThisWeekCount >= 3) {
            highlightsList.add(
                PersonalHighlight(
                    title = "Task Velocity",
                    description = "$completedThisWeekCount tasks brought to completion this week.",
                    category = "PRODUCTIVITY"
                )
            )
        }

        return AdvancedProgressInsights(
            habitConsistencies = habitMetrics,
            overallRate7Days = overall7,
            overallRate30Days = overall30,
            overallRate90Days = overall90,
            weeklyTrends = weeklyTrends,
            taskProductivity = taskProductivity,
            goalInsights = goalInsights,
            focusInsights = focusInsights,
            highlights = highlightsList
        )
    }
}
