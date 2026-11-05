package com.kintsu.app.ui.screens

import com.kintsu.app.ui.components.IconSparkle
import com.kintsu.app.ui.components.IconChevronRight

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kintsu.app.data.ProgressStats
import com.kintsu.app.data.model.DailyIntentionEntity
import com.kintsu.app.data.model.FocusSessionEntity
import com.kintsu.app.data.model.GoalEntity
import com.kintsu.app.data.model.GoalProgressType
import com.kintsu.app.data.model.HabitEntity
import com.kintsu.app.data.model.HabitLogEntity
import com.kintsu.app.data.model.JournalEntryEntity
import com.kintsu.app.data.model.TaskEntity
import com.kintsu.app.data.model.TaskPriority
import com.kintsu.app.data.model.WeeklyReviewEntity
import com.kintsu.app.domain.progress.AdvancedProgressInsights
import com.kintsu.app.domain.progress.WeeklyTrendPoint
import com.kintsu.app.ui.components.IconCheck
import com.kintsu.app.ui.components.IconFlame
import com.kintsu.app.ui.components.IconFocus
import com.kintsu.app.ui.components.IconGoal
import com.kintsu.app.ui.components.IconGoldSeam
import com.kintsu.app.ui.components.IconHabit
import com.kintsu.app.ui.components.IconProgress
import com.kintsu.app.ui.components.IconTask
import com.kintsu.app.ui.components.IconTrash
import com.kintsu.app.ui.components.KintsuCard
import com.kintsu.app.ui.components.KintsuDivider
import com.kintsu.app.ui.components.KintsuFilterChip
import com.kintsu.app.ui.components.KintsuIconButton
import com.kintsu.app.ui.components.KintsuProgressRing
import com.kintsu.app.ui.components.KintsuTopBar
import com.kintsu.app.ui.theme.KintsuTheme
import java.time.LocalDate

enum class ConsistencyTimeframe(val displayName: String) {
    SEVEN_DAYS("7 Days"),
    THIRTY_DAYS("30 Days"),
    NINETY_DAYS("90 Days")
}

@Composable
fun ProgressScreen(
    stats: ProgressStats,
    insights: AdvancedProgressInsights,
    logs: List<HabitLogEntity>,
    goals: List<GoalEntity>,
    tasks: List<TaskEntity>,
    habits: List<HabitEntity> = emptyList(),
    focusSessions: List<FocusSessionEntity> = emptyList(),
    allIntentions: List<DailyIntentionEntity> = emptyList(),
    allJournalEntries: List<JournalEntryEntity> = emptyList(),
    allWeeklyReviews: List<WeeklyReviewEntity> = emptyList(),
    onDeleteJournalEntry: (JournalEntryEntity) -> Unit = {},
    onDeleteWeeklyReview: (WeeklyReviewEntity) -> Unit = {},
    onOpenSettings: () -> Unit,
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography

    val todayEpochDay = remember { LocalDate.now().toEpochDay() }
    var selectedTimeframe by remember { mutableStateOf(ConsistencyTimeframe.THIRTY_DAYS) }
    var selectedTrendPoint by remember { mutableStateOf<WeeklyTrendPoint?>(null) }
    var showIndividualHabits by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        KintsuTopBar(
            title = "Progress & Insights",
            subtitle = "Your patterns, progress and growth",
                                )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = spacing.xl),
            verticalArrangement = Arrangement.spacedBy(spacing.l)
        ) {
            // 1. Core Streaks & Repairs
            item {
                Spacer(modifier = Modifier.height(spacing.xxs))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing.s)
                ) {
                    MetricStatCard(
                        title = "Current",
                        value = "${stats.currentStreak}",
                        icon = { IconFlame(color = colors.accent, size = 16.dp) },
                        modifier = Modifier.weight(1f)
                    )
                    MetricStatCard(
                        title = "Best",
                        value = "${stats.bestStreak}",
                        icon = { IconSparkle(color = colors.textTertiary, size = 16.dp) },
                        modifier = Modifier.weight(1f)
                    )
                    MetricStatCard(
                        title = "Consistency",
                        value = "${(insights.overallRate30Days * 100).toInt()}%",
                        icon = { IconProgress(color = colors.accent, size = 16.dp) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(spacing.m))

                // Kintsugi Gold Repairs Banner
                KintsuCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = colors.surface, borderColor = colors.goldSeam.copy(alpha = 0.35f)
                ) {
                    Row(
                        modifier = Modifier.padding(spacing.l),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(colors.goldSeam.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            IconGoldSeam(color = colors.goldSeam, size = 22.dp)
                        }

                        Spacer(modifier = Modifier.size(spacing.m))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${stats.totalRepairsGold} Gold Seams Mended",
                                style = typography.itemTitle,
                                color = colors.goldSeam
                            )
                            Text(
                                text = "Every repaired crack makes you stronger.",
                                style = typography.caption,
                                color = colors.textSecondary
                            )
                        }
                        IconChevronRight(color = colors.goldSeam, size = 16.dp)
                    }
                }
            }

            // 2. Personal Highlights (Only shown when supported by actual recorded data)
            if (insights.highlights.isNotEmpty()) {
                item {
                    Text(
                        text = "Personal Highlights",
                        style = typography.sectionHeader,
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(spacing.xs))

                    Column(verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
                        insights.highlights.forEach { hl ->
                            KintsuCard(
                                modifier = Modifier.fillMaxWidth(),
                                backgroundColor = colors.surface
                            ) {
                                Row(
                                    modifier = Modifier.padding(spacing.m),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(spacing.m)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(colors.accentSoft),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        when (hl.category) {
                                            "RITUAL" -> IconHabit(color = colors.accent, size = 18.dp)
                                            "FOCUS" -> IconFocus(color = colors.accent, size = 18.dp)
                                            "PRODUCTIVITY" -> IconTask(color = colors.accent, size = 18.dp)
                                            else -> IconFlame(color = colors.accent, size = 18.dp)
                                        }
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = hl.title,
                                            style = typography.itemTitle,
                                            color = colors.textPrimary
                                        )
                                        Text(
                                            text = hl.description,
                                            style = typography.secondary,
                                            color = colors.textSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. Habit Consistency & Schedule-Aware Rates (B1 & B2)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Habit Consistency",
                        style = typography.sectionHeader,
                        color = colors.textPrimary
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(spacing.xxs)) {
                        ConsistencyTimeframe.values().forEach { tf ->
                            KintsuFilterChip(
                                label = tf.displayName,
                                selected = selectedTimeframe == tf,
                                onClick = { selectedTimeframe = tf }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(spacing.xs))

                val activeRate = when (selectedTimeframe) {
                    ConsistencyTimeframe.SEVEN_DAYS -> insights.overallRate7Days
                    ConsistencyTimeframe.THIRTY_DAYS -> insights.overallRate30Days
                    ConsistencyTimeframe.NINETY_DAYS -> insights.overallRate90Days
                }

                KintsuCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(spacing.l),
                        verticalArrangement = Arrangement.spacedBy(spacing.m)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${(activeRate * 100).toInt()}% Overall Consistency",
                                    style = typography.sectionHeader,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "Calculated strictly against scheduled days, never penalizing rest days.",
                                    style = typography.secondary,
                                    color = colors.textSecondary
                                )
                            }
                            KintsuProgressRing(
                                progress = activeRate,
                                size = 64.dp,
                                strokeWidth = 6.dp,
                                progressColor = colors.accent
                            )
                        }

                        KintsuDivider()

                        // Interactive Weekly Trend Bars (B2)
                        Text(
                            text = "Weekly Completion Trend",
                            style = typography.caption,
                            color = colors.textSecondary
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            insights.weeklyTrends.forEach { pt ->
                                val isSelected = selectedTrendPoint?.weekStartEpochDay == pt.weekStartEpochDay
                                val hasData = pt.completionRate > 0.001f
                                val barHeight = if (hasData) (70 * pt.completionRate.coerceIn(0.08f, 1f)).dp else 2.dp
                                val barColor = when {
                                    isSelected -> colors.accent
                                    hasData -> colors.accentSoft.copy(alpha = 0.65f)
                                    else -> colors.border.copy(alpha = 0.5f)
                                }

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            selectedTrendPoint = if (isSelected) null else pt
                                        }
                                        .padding(horizontal = spacing.xxs)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(barHeight)
                                            .clip(RoundedCornerShape(if (hasData) 6.dp else 1.dp))
                                            .background(barColor)
                                            .then(
                                                if (hasData) Modifier.border(
                                                    1.dp,
                                                    if (isSelected) colors.accent else colors.border,
                                                    RoundedCornerShape(6.dp)
                                                ) else Modifier
                                            )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = pt.weekLabel,
                                        style = typography.caption,
                                        color = if (isSelected) colors.accent else colors.textTertiary,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        // Selected trend point summary pill
                        AnimatedVisibility(visible = selectedTrendPoint != null) {
                            selectedTrendPoint?.let { pt ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(KintsuTheme.shapes.small)
                                        .background(colors.surfaceElevated)
                                        .padding(spacing.s),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${pt.weekLabel}: ${(pt.completionRate * 100).toInt()}% • ${pt.habitsDone}/${pt.habitsEligible} rituals • ${pt.tasksDone} tasks done",
                                        style = typography.caption,
                                        color = colors.accent,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        // Toggle for individual habit consistency breakdown
                        if (insights.habitConsistencies.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showIndividualHabits = !showIndividualHabits },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (showIndividualHabits) "Hide individual rituals" else "Show individual ritual performance",
                                    style = typography.caption,
                                    color = colors.accent
                                )
                                Text(
                                    text = if (showIndividualHabits) "▲" else "▼",
                                    style = typography.caption,
                                    color = colors.accent
                                )
                            }

                            AnimatedVisibility(visible = showIndividualHabits) {
                                Column(verticalArrangement = Arrangement.spacedBy(spacing.s)) {
                                    insights.habitConsistencies.forEach { hm ->
                                        val rate = when (selectedTimeframe) {
                                            ConsistencyTimeframe.SEVEN_DAYS -> hm.rate7Days
                                            ConsistencyTimeframe.THIRTY_DAYS -> hm.rate30Days
                                            ConsistencyTimeframe.NINETY_DAYS -> hm.rate90Days
                                        }
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(spacing.xs)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .clip(CircleShape)
                                                        .background(try { Color(android.graphics.Color.parseColor(hm.colorHex)) } catch (e: Exception) { colors.accent })
                                                )
                                                Text(
                                                    text = hm.habitName,
                                                    style = typography.body,
                                                    color = colors.textPrimary
                                                )
                                            }

                                            Text(
                                                text = "${(rate * 100).toInt()}%",
                                                style = typography.caption,
                                                color = colors.textSecondary,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Dynamic Changeable Activity Graph (Habits, Tasks, Focus, Gold Seams, Mood)
            item {
                DynamicProgressChartSection(
                    todayEpochDay = todayEpochDay,
                    habits = habits,
                    logs = logs,
                    tasks = tasks,
                    focusSessions = focusSessions,
                    journalEntries = allJournalEntries
                )
            }

            // 4. Task Productivity Insights (B3)
            item {
                Text(
                    text = "Task Productivity",
                    style = typography.sectionHeader,
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.height(spacing.xs))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing.m)
                ) {
                    MetricStatCard(
                        title = "Completed This Wk",
                        value = "${insights.taskProductivity.completedThisWeek}",
                        icon = { IconTask(color = colors.accent, size = 18.dp) },
                        modifier = Modifier.weight(1f)
                    )
                    MetricStatCard(
                        title = "Completed This Mo",
                        value = "${insights.taskProductivity.completedThisMonth}",
                        icon = { IconTask(color = colors.textSecondary, size = 18.dp) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(spacing.xs))

                KintsuCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(spacing.m),
                        verticalArrangement = Arrangement.spacedBy(spacing.xs)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Completion by Priority",
                                style = typography.caption,
                                color = colors.textSecondary
                            )
                            if (insights.taskProductivity.overdueCount > 0) {
                                Text(
                                    text = "${insights.taskProductivity.overdueCount} overdue",
                                    style = typography.caption,
                                    color = colors.accent,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(spacing.s)
                        ) {
                            Text(
                                text = "High: ${insights.taskProductivity.highPriorityCompleted}",
                                style = typography.caption,
                                color = colors.accent
                            )
                            Text(
                                text = "• Medium: ${insights.taskProductivity.mediumPriorityCompleted}",
                                style = typography.caption,
                                color = colors.textSecondary
                            )
                            Text(
                                text = "• Low: ${insights.taskProductivity.lowPriorityCompleted}",
                                style = typography.caption,
                                color = colors.textTertiary
                            )
                        }
                    }
                }
            }

            // 5. Mindful Focus Insights (B5)
            item {
                Text(
                    text = "Focus Practice",
                    style = typography.sectionHeader,
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.height(spacing.xs))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing.m)
                ) {
                    MetricStatCard(
                        title = "Total Focus Time",
                        value = "${insights.focusInsights.totalFocusMinutes} min",
                        icon = { IconFocus(color = colors.accent, size = 18.dp) },
                        modifier = Modifier.weight(1f)
                    )
                    MetricStatCard(
                        title = "Avg Session Length",
                        value = "${insights.focusInsights.averageSessionDuration} min",
                        icon = { IconFocus(color = colors.textSecondary, size = 18.dp) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 6. Horizon Goals Insights (B4)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Horizon Goals",
                        style = typography.sectionHeader,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "${insights.goalInsights.activeGoalsCount} active • ${insights.goalInsights.completedGoalsCount} achieved",
                        style = typography.caption,
                        color = colors.textSecondary
                    )
                }
            }

            if (goals.isEmpty()) {
                item {
                    KintsuCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "No goals configured yet. Add a horizon goal to track long-term aspirations.",
                            style = typography.secondary,
                            color = colors.textTertiary,
                            modifier = Modifier.padding(spacing.m)
                        )
                    }
                }
            } else {
                items(goals, key = { "goal_${it.id}" }) { goal ->
                    val linkedTasks = tasks.filter { it.linkedGoalId == goal.id }
                    val linkedHabits = habits.filter { it.linkedGoalId == goal.id }
                    val totalLinkedItems = linkedTasks.size + linkedHabits.size
                    val doneTasks = linkedTasks.count { it.isCompleted }
                    val doneHabits = linkedHabits.count { h -> logs.any { it.habitId == h.id && it.epochDay == todayEpochDay && it.isCompleted } }
                    val totalDoneItems = doneTasks + doneHabits

                    val progressFloat = if (goal.progressType == GoalProgressType.AUTO_FROM_ITEMS) {
                        if (totalLinkedItems == 0) 0f else totalDoneItems.toFloat() / totalLinkedItems.toFloat()
                    } else {
                        goal.manualProgressPercent.toFloat() / 100f
                    }

                    val daysLeft = goal.targetDateEpochDay - todayEpochDay
                    val deadlineBadge = when {
                        daysLeft < 0 -> "Target passed"
                        daysLeft == 0L -> "Target today"
                        daysLeft <= 14L -> "$daysLeft days left"
                        else -> null
                    }

                    KintsuCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(spacing.m),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = goal.title,
                                    style = typography.itemTitle,
                                    color = colors.textPrimary
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(spacing.xs)
                                ) {
                                    Text(
                                        text = if (goal.progressType == GoalProgressType.AUTO_FROM_ITEMS) {
                                            "$totalDoneItems/$totalLinkedItems linked items"
                                        } else {
                                            "Manual: ${goal.manualProgressPercent}%"
                                        },
                                        style = typography.secondary,
                                        color = colors.textSecondary
                                    )
                                    if (deadlineBadge != null) {
                                        Text(
                                            text = "• $deadlineBadge",
                                            style = typography.caption,
                                            color = if (daysLeft <= 7) colors.accent else colors.textTertiary
                                        )
                                    }
                                }
                            }

                            KintsuProgressRing(
                                progress = progressFloat,
                                size = 46.dp,
                                strokeWidth = 4.dp
                            )
                        }
                    }
                }
            }

            // 7. Journey Heatmap
            item {
                Text(
                    text = "Journey Heatmap",
                    style = typography.sectionHeader,
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.height(spacing.xs))

                KintsuCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(spacing.l)) {
                        HeatmapGrid(
                            todayEpochDay = todayEpochDay,
                            logs = logs
                        )

                        Spacer(modifier = Modifier.height(spacing.m))

                        // Accessible Legend using distinct shapes and icons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HeatmapLegendItem(
                                shapeDescription = "Circle",
                                label = "Intact",
                                indicator = {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(colors.accent)
                                    )
                                }
                            )
                            HeatmapLegendItem(
                                shapeDescription = "Gold Seam Squircle",
                                label = "Gold Seam",
                                indicator = {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .border(1.dp, colors.goldSeam, RoundedCornerShape(3.dp))
                                            .background(colors.goldSeam.copy(alpha = 0.3f))
                                    )
                                }
                            )
                            HeatmapLegendItem(
                                shapeDescription = "Fractured Square",
                                label = "Crack",
                                indicator = {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .border(1.dp, colors.textTertiary, RoundedCornerShape(2.dp))
                                            .background(colors.surfaceElevated)
                                    )
                                }
                            )
                            HeatmapLegendItem(
                                shapeDescription = "Quiet Dot",
                                label = "Rest",
                                indicator = {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(colors.surfaceElevated)
                                    )
                                }
                            )
                        }
                    }
                }
            }

            // 8. Weekly Reviews Archive (B7)
            if (allWeeklyReviews.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(spacing.s))
                    Text(
                        text = "Weekly Reviews Summary",
                        style = typography.sectionHeader,
                        color = colors.textPrimary
                    )
                }

                items(allWeeklyReviews, key = { "review_${it.id}" }) { review ->
                    val startStr = remember(review.weekStartEpochDay) {
                        LocalDate.ofEpochDay(review.weekStartEpochDay).toString()
                    }
                    val endStr = remember(review.weekEndEpochDay) {
                        LocalDate.ofEpochDay(review.weekEndEpochDay).toString()
                    }
                    KintsuCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(spacing.m),
                            verticalArrangement = Arrangement.spacedBy(spacing.xs)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "$startStr — $endStr",
                                    style = typography.itemTitle,
                                    color = colors.textPrimary
                                )
                                KintsuIconButton(
                                    icon = { IconTrash(color = it, size = 14.dp) },
                                    contentDescription = "Delete weekly review",
                                    onClick = { onDeleteWeeklyReview(review) },
                                    tint = colors.textTertiary
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(spacing.m)) {
                                Text(
                                    text = "Rhythm: ${(review.completionRate * 100).toInt()}%",
                                    style = typography.caption,
                                    color = colors.accent
                                )
                                Text(
                                    text = "Focus: ${review.focusMinutes}m",
                                    style = typography.caption,
                                    color = colors.textSecondary
                                )
                                Text(
                                    text = "Repairs: ${review.totalRepairs}",
                                    style = typography.caption,
                                    color = colors.goldSeam
                                )
                            }

                            if (review.reflection1.isNotBlank()) {
                                Text(
                                    text = "• ${review.reflection1}",
                                    style = typography.secondary,
                                    color = colors.textSecondary
                                )
                            }
                        }
                    }
                }
            }

            // 9. Daily Intentions History
            if (allIntentions.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(spacing.s))
                    Text(
                        text = "Intentions History",
                        style = typography.sectionHeader,
                        color = colors.textPrimary
                    )
                }

                items(allIntentions.take(5), key = { "intention_${it.id}" }) { intention ->
                    val dateFormatted = remember(intention.dateEpochDay) {
                        LocalDate.ofEpochDay(intention.dateEpochDay).toString()
                    }
                    KintsuCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(spacing.m),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = dateFormatted,
                                    style = typography.caption,
                                    color = colors.textTertiary
                                )
                                Text(
                                    text = intention.text,
                                    style = typography.body,
                                    color = colors.textPrimary
                                )
                            }
                            if (intention.isCompleted) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(colors.accentSoft),
                                    contentAlignment = Alignment.Center
                                ) {
                                    IconCheck(color = colors.accent, size = 14.dp)
                                }
                            }
                        }
                    }
                }
            }

            // 10. Quiet Reflections (Journal) Archive
            if (allJournalEntries.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(spacing.s))
                    Text(
                        text = "Quiet Reflections",
                        style = typography.sectionHeader,
                        color = colors.textPrimary
                    )
                }

                items(allJournalEntries.take(5), key = { "journal_${it.id}" }) { entry ->
                    val dateFormatted = remember(entry.dateEpochDay) {
                        LocalDate.ofEpochDay(entry.dateEpochDay).toString()
                    }
                    KintsuCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(spacing.m)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(spacing.xs)
                                ) {
                                    if (entry.mood != null) {
                                        Text(text = entry.mood, style = typography.caption)
                                    }
                                    Text(
                                        text = dateFormatted,
                                        style = typography.caption,
                                        color = colors.textTertiary
                                    )
                                }
                                KintsuIconButton(
                                    icon = { IconTrash(color = it, size = 14.dp) },
                                    contentDescription = "Delete reflection",
                                    onClick = { onDeleteJournalEntry(entry) },
                                    tint = colors.textTertiary
                                )
                            }
                            Spacer(modifier = Modifier.height(spacing.xxs))
                            Text(
                                text = entry.content,
                                style = typography.body,
                                color = colors.textPrimary
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(spacing.xxxl))
            }
        }
    }
}

@Composable
private fun MetricStatCard(
    title: String,
    value: String,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography

    KintsuCard(modifier = modifier) {
        Column(modifier = Modifier.padding(spacing.m)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = typography.caption,
                    color = colors.textSecondary
                )
                icon()
            }
            Spacer(modifier = Modifier.height(spacing.xs))
            Text(
                text = value,
                style = typography.sectionHeader,
                color = colors.textPrimary
            )
        }
    }
}

@Composable
private fun HeatmapGrid(
    todayEpochDay: Long,
    logs: List<HabitLogEntity>
) {
    val colors = KintsuTheme.colors
    val days = (34 downTo 0).map { offset -> todayEpochDay - offset }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        days.chunked(7).forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                week.forEach { epochDay ->
                    val logsForDay = logs.filter { it.epochDay == epochDay }
                    val hasGold = logsForDay.any { it.isRepairedGold }
                    val hasCompleted = logsForDay.any { it.isCompleted }
                    val isPast = epochDay < todayEpochDay

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .then(
                                when {
                                    hasGold -> {
                                        Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .border(1.5.dp, colors.goldSeam, RoundedCornerShape(8.dp))
                                            .background(colors.goldSeam.copy(alpha = 0.2f))
                                    }
                                    hasCompleted -> {
                                        Modifier
                                            .clip(CircleShape)
                                            .background(colors.accent)
                                    }
                                    isPast -> {
                                        Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .border(1.dp, colors.border, RoundedCornerShape(4.dp))
                                            .background(colors.surfaceElevated.copy(alpha = 0.5f))
                                    }
                                    else -> {
                                        Modifier
                                            .clip(CircleShape)
                                            .background(colors.surfaceElevated)
                                    }
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (hasGold) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                // Elegant corner gold seam that frames the date without occluding the numeral
                                drawLine(
                                    color = colors.goldSeam,
                                    start = Offset(size.width * 0.65f, 3f),
                                    end = Offset(size.width - 3f, size.height * 0.35f),
                                    strokeWidth = 2.5.dp.toPx()
                                )
                            }
                        }

                        Text(
                            text = (LocalDate.ofEpochDay(epochDay).dayOfMonth).toString(),
                            style = KintsuTheme.typography.caption,
                            color = when {
                                hasGold -> colors.goldSeam
                                hasCompleted -> colors.onAccent
                                else -> colors.textTertiary
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeatmapLegendItem(
    shapeDescription: String,
    label: String,
    indicator: @Composable () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        indicator()
        Text(
            text = label,
            style = KintsuTheme.typography.caption,
            color = KintsuTheme.colors.textSecondary
        )
    }
}
