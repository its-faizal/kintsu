package com.kintsu.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kintsu.app.data.model.FocusSessionEntity
import com.kintsu.app.data.model.HabitEntity
import com.kintsu.app.data.model.HabitLogEntity
import com.kintsu.app.data.model.JournalEntryEntity
import com.kintsu.app.data.model.TaskEntity
import com.kintsu.app.ui.components.IconFocus
import com.kintsu.app.ui.components.IconGoldSeam
import com.kintsu.app.ui.components.IconHabit
import com.kintsu.app.ui.components.IconSparkle
import com.kintsu.app.ui.components.IconTask
import com.kintsu.app.ui.components.KintsuCard
import com.kintsu.app.ui.components.KintsuFilterChip
import com.kintsu.app.ui.theme.KintsuTheme
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

enum class ChartMetric(val label: String) {
    HABITS("Habits"),
    TASKS("Tasks"),
    FOCUS("Focus"),
    GOLD_SEAMS("Gold Seams"),
    MOOD("Mood")
}

enum class ChartTimeframe(val label: String, val days: Int) {
    DAYS_7("7D", 7),
    DAYS_30("30D", 30),
    DAYS_90("90D", 90)
}

data class DynamicBarData(
    val label: String,
    val sublabel: String,
    val value: Float,
    val formattedValue: String,
    val epochDayStart: Long,
    val epochDayEnd: Long,
    val details: String
)

@Composable
fun DynamicProgressChartSection(
    todayEpochDay: Long,
    habits: List<HabitEntity>,
    logs: List<HabitLogEntity>,
    tasks: List<TaskEntity>,
    focusSessions: List<FocusSessionEntity>,
    journalEntries: List<JournalEntryEntity>,
    modifier: Modifier = Modifier
) {
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography

    var selectedMetric by remember { mutableStateOf(ChartMetric.HABITS) }
    var selectedTimeframe by remember { mutableStateOf(ChartTimeframe.DAYS_7) }
    var selectedBarIndex by remember { mutableStateOf<Int?>(null) }

    // Calculate chart bars based on selected metric and timeframe
    val barItems = remember(selectedMetric, selectedTimeframe, logs, tasks, focusSessions, journalEntries, habits) {
        selectedBarIndex = null // reset selection on tab change

        when (selectedTimeframe) {
            ChartTimeframe.DAYS_7 -> {
                // 7 individual days (from today - 6 to today)
                (6 downTo 0).map { offset ->
                    val day = todayEpochDay - offset
                    val localDate = LocalDate.ofEpochDay(day)
                    val dayOfWeek = localDate.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())

                    when (selectedMetric) {
                        ChartMetric.HABITS -> {
                            val doneCount = logs.count { it.epochDay == day && it.isCompleted }
                            DynamicBarData(
                                label = dayOfWeek,
                                sublabel = "${localDate.dayOfMonth}",
                                value = doneCount.toFloat(),
                                formattedValue = "$doneCount rituals",
                                epochDayStart = day,
                                epochDayEnd = day,
                                details = "$dayOfWeek, ${localDate.dayOfMonth}: $doneCount habits completed"
                            )
                        }
                        ChartMetric.TASKS -> {
                            val doneCount = tasks.count {
                                it.isCompleted && ((it.completedAt?.let { ts -> ts / 86400000L } ?: it.dueDateEpochDay ?: 0L) == day)
                            }
                            DynamicBarData(
                                label = dayOfWeek,
                                sublabel = "${localDate.dayOfMonth}",
                                value = doneCount.toFloat(),
                                formattedValue = "$doneCount tasks",
                                epochDayStart = day,
                                epochDayEnd = day,
                                details = "$dayOfWeek: $doneCount tasks completed"
                            )
                        }
                        ChartMetric.FOCUS -> {
                            val focusMins = focusSessions
                                .filter { it.dateEpochDay == day }
                                .sumOf { it.durationMinutes }
                            DynamicBarData(
                                label = dayOfWeek,
                                sublabel = "${localDate.dayOfMonth}",
                                value = focusMins.toFloat(),
                                formattedValue = "$focusMins m",
                                epochDayStart = day,
                                epochDayEnd = day,
                                details = "$dayOfWeek: $focusMins minutes focused"
                            )
                        }
                        ChartMetric.GOLD_SEAMS -> {
                            val repairCount = logs.count { it.epochDay == day && it.isRepairedGold }
                            DynamicBarData(
                                label = dayOfWeek,
                                sublabel = "${localDate.dayOfMonth}",
                                value = repairCount.toFloat(),
                                formattedValue = "$repairCount mended",
                                epochDayStart = day,
                                epochDayEnd = day,
                                details = "$dayOfWeek: $repairCount gold seams mended"
                            )
                        }
                        ChartMetric.MOOD -> {
                            val entries = journalEntries.filter { it.dateEpochDay == day }
                            val moodScore = if (entries.isEmpty()) 0f else {
                                entries.map { entry ->
                                    when (entry.mood?.uppercase()) {
                                        "GREAT", "SERENE", "INSPIRED" -> 5f
                                        "GOOD", "FOCUSED", "CALM" -> 4f
                                        "NEUTRAL", "CONTEMPLATIVE" -> 3f
                                        "TIRED", "RESTLESS" -> 2f
                                        "LOW", "STRESSED" -> 1f
                                        else -> 3f
                                    }
                                }.average().toFloat()
                            }
                            DynamicBarData(
                                label = dayOfWeek,
                                sublabel = "${localDate.dayOfMonth}",
                                value = moodScore,
                                formattedValue = if (moodScore > 0) String.format(Locale.US, "%.1f/5", moodScore) else "—",
                                epochDayStart = day,
                                epochDayEnd = day,
                                details = if (entries.isNotEmpty()) "$dayOfWeek: Mood ${String.format(Locale.US, "%.1f", moodScore)}/5 (${entries.first().mood})" else "$dayOfWeek: No reflection logged"
                            )
                        }
                    }
                }
            }
            ChartTimeframe.DAYS_30 -> {
                // 4 weekly blocks spanning the last 28-30 days
                (3 downTo 0).map { weekOffset ->
                    val weekStart = todayEpochDay - ((weekOffset + 1) * 7 - 1)
                    val weekEnd = todayEpochDay - (weekOffset * 7)
                    val startDate = LocalDate.ofEpochDay(weekStart)
                    val endDate = LocalDate.ofEpochDay(weekEnd)
                    val label = "W${4 - weekOffset}"

                    when (selectedMetric) {
                        ChartMetric.HABITS -> {
                            val doneCount = logs.count { it.epochDay in weekStart..weekEnd && it.isCompleted }
                            DynamicBarData(
                                label = label,
                                sublabel = "${startDate.dayOfMonth}-${endDate.dayOfMonth}",
                                value = doneCount.toFloat(),
                                formattedValue = "$doneCount",
                                epochDayStart = weekStart,
                                epochDayEnd = weekEnd,
                                details = "Week ${4 - weekOffset} (${startDate.dayOfMonth} - ${endDate.dayOfMonth}): $doneCount habits done"
                            )
                        }
                        ChartMetric.TASKS -> {
                            val doneCount = tasks.count {
                                it.isCompleted && ((it.completedAt?.let { ts -> ts / 86400000L } ?: it.dueDateEpochDay ?: 0L) in weekStart..weekEnd)
                            }
                            DynamicBarData(
                                label = label,
                                sublabel = "${startDate.dayOfMonth}-${endDate.dayOfMonth}",
                                value = doneCount.toFloat(),
                                formattedValue = "$doneCount",
                                epochDayStart = weekStart,
                                epochDayEnd = weekEnd,
                                details = "Week ${4 - weekOffset}: $doneCount tasks completed"
                            )
                        }
                        ChartMetric.FOCUS -> {
                            val focusMins = focusSessions
                                .filter { it.dateEpochDay in weekStart..weekEnd }
                                .sumOf { it.durationMinutes }
                            DynamicBarData(
                                label = label,
                                sublabel = "${focusMins}m",
                                value = focusMins.toFloat(),
                                formattedValue = "$focusMins m",
                                epochDayStart = weekStart,
                                epochDayEnd = weekEnd,
                                details = "Week ${4 - weekOffset}: $focusMins focus minutes"
                            )
                        }
                        ChartMetric.GOLD_SEAMS -> {
                            val repairCount = logs.count { it.epochDay in weekStart..weekEnd && it.isRepairedGold }
                            DynamicBarData(
                                label = label,
                                sublabel = "$repairCount",
                                value = repairCount.toFloat(),
                                formattedValue = "$repairCount",
                                epochDayStart = weekStart,
                                epochDayEnd = weekEnd,
                                details = "Week ${4 - weekOffset}: $repairCount gold seams mended"
                            )
                        }
                        ChartMetric.MOOD -> {
                            val entries = journalEntries.filter { it.dateEpochDay in weekStart..weekEnd }
                            val moodScore = if (entries.isEmpty()) 0f else {
                                entries.map { entry ->
                                    when (entry.mood?.uppercase()) {
                                        "GREAT", "SERENE" -> 5f
                                        "GOOD", "CALM" -> 4f
                                        "NEUTRAL" -> 3f
                                        "TIRED" -> 2f
                                        else -> 1f
                                    }
                                }.average().toFloat()
                            }
                            DynamicBarData(
                                label = label,
                                sublabel = if (moodScore > 0) String.format(Locale.US, "%.1f", moodScore) else "—",
                                value = moodScore,
                                formattedValue = if (moodScore > 0) String.format(Locale.US, "%.1f/5", moodScore) else "—",
                                epochDayStart = weekStart,
                                epochDayEnd = weekEnd,
                                details = "Week ${4 - weekOffset}: Avg mood ${if (moodScore > 0) String.format(Locale.US, "%.1f/5", moodScore) else "No entries"}"
                            )
                        }
                    }
                }
            }
            ChartTimeframe.DAYS_90 -> {
                // 6 bi-weekly chunks (spanning ~84 days)
                (5 downTo 0).map { chunkOffset ->
                    val chunkStart = todayEpochDay - ((chunkOffset + 1) * 14 - 1)
                    val chunkEnd = todayEpochDay - (chunkOffset * 14)
                    val startDate = LocalDate.ofEpochDay(chunkStart)
                    val endDate = LocalDate.ofEpochDay(chunkEnd)
                    val label = "${startDate.dayOfMonth}/${startDate.monthValue}"

                    when (selectedMetric) {
                        ChartMetric.HABITS -> {
                            val doneCount = logs.count { it.epochDay in chunkStart..chunkEnd && it.isCompleted }
                            DynamicBarData(
                                label = label,
                                sublabel = "$doneCount",
                                value = doneCount.toFloat(),
                                formattedValue = "$doneCount",
                                epochDayStart = chunkStart,
                                epochDayEnd = chunkEnd,
                                details = "${startDate.dayOfMonth}/${startDate.monthValue} - ${endDate.dayOfMonth}/${endDate.monthValue}: $doneCount habits finished"
                            )
                        }
                        ChartMetric.TASKS -> {
                            val doneCount = tasks.count {
                                it.isCompleted && ((it.completedAt?.let { ts -> ts / 86400000L } ?: it.dueDateEpochDay ?: 0L) in chunkStart..chunkEnd)
                            }
                            DynamicBarData(
                                label = label,
                                sublabel = "$doneCount",
                                value = doneCount.toFloat(),
                                formattedValue = "$doneCount",
                                epochDayStart = chunkStart,
                                epochDayEnd = chunkEnd,
                                details = "Period: $doneCount tasks completed"
                            )
                        }
                        ChartMetric.FOCUS -> {
                            val focusMins = focusSessions
                                .filter { it.dateEpochDay in chunkStart..chunkEnd }
                                .sumOf { it.durationMinutes }
                            DynamicBarData(
                                label = label,
                                sublabel = "${focusMins}m",
                                value = focusMins.toFloat(),
                                formattedValue = "$focusMins m",
                                epochDayStart = chunkStart,
                                epochDayEnd = chunkEnd,
                                details = "Period: $focusMins focus minutes"
                            )
                        }
                        ChartMetric.GOLD_SEAMS -> {
                            val repairCount = logs.count { it.epochDay in chunkStart..chunkEnd && it.isRepairedGold }
                            DynamicBarData(
                                label = label,
                                sublabel = "$repairCount",
                                value = repairCount.toFloat(),
                                formattedValue = "$repairCount",
                                epochDayStart = chunkStart,
                                epochDayEnd = chunkEnd,
                                details = "Period: $repairCount gold seams"
                            )
                        }
                        ChartMetric.MOOD -> {
                            val entries = journalEntries.filter { it.dateEpochDay in chunkStart..chunkEnd }
                            val moodScore = if (entries.isEmpty()) 0f else {
                                entries.map { entry ->
                                    when (entry.mood?.uppercase()) {
                                        "GREAT", "SERENE" -> 5f
                                        "GOOD", "CALM" -> 4f
                                        "NEUTRAL" -> 3f
                                        else -> 2f
                                    }
                                }.average().toFloat()
                            }
                            DynamicBarData(
                                label = label,
                                sublabel = if (moodScore > 0) String.format(Locale.US, "%.1f", moodScore) else "—",
                                value = moodScore,
                                formattedValue = if (moodScore > 0) String.format(Locale.US, "%.1f/5", moodScore) else "—",
                                epochDayStart = chunkStart,
                                epochDayEnd = chunkEnd,
                                details = "Period: Avg mood ${if (moodScore > 0) String.format(Locale.US, "%.1f/5", moodScore) else "No entries"}"
                            )
                        }
                    }
                }
            }
        }
    }

    val maxValue = remember(barItems) {
        val max = barItems.maxOfOrNull { it.value } ?: 1f
        if (max <= 0f) 5f else max
    }

    val metricColor = when (selectedMetric) {
        ChartMetric.HABITS -> colors.accent
        ChartMetric.TASKS -> Color(0xFF60A5FA)
        ChartMetric.FOCUS -> Color(0xFFA78BFA)
        ChartMetric.GOLD_SEAMS -> colors.goldSeam
        ChartMetric.MOOD -> Color(0xFFFBBF24)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(spacing.s)
    ) {
        // Section Header with Timeframe filter
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Activity Trend",
                style = typography.sectionHeader,
                color = colors.textPrimary
            )

            Row(horizontalArrangement = Arrangement.spacedBy(spacing.xxs)) {
                ChartTimeframe.values().forEach { tf ->
                    KintsuFilterChip(
                        label = tf.label,
                        selected = selectedTimeframe == tf,
                        onClick = { selectedTimeframe = tf }
                    )
                }
            }
        }

        // Changeable Metric Filter Bar (Horizontal scrollable pills)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(spacing.xs)
        ) {
            ChartMetric.values().forEach { metric ->
                val isSelected = selectedMetric == metric
                val chipColor = when (metric) {
                    ChartMetric.HABITS -> colors.accent
                    ChartMetric.TASKS -> Color(0xFF60A5FA)
                    ChartMetric.FOCUS -> Color(0xFFA78BFA)
                    ChartMetric.GOLD_SEAMS -> colors.goldSeam
                    ChartMetric.MOOD -> Color(0xFFFBBF24)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) chipColor.copy(alpha = 0.2f) else colors.surfaceElevated)
                        .border(
                            1.dp,
                            if (isSelected) chipColor else colors.border.copy(alpha = 0.4f),
                            RoundedCornerShape(16.dp)
                        )
                        .clickable { selectedMetric = metric }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(spacing.xxs)
                    ) {
                        when (metric) {
                            ChartMetric.HABITS -> IconHabit(color = if (isSelected) chipColor else colors.textSecondary, size = 13.dp)
                            ChartMetric.TASKS -> IconTask(color = if (isSelected) chipColor else colors.textSecondary, size = 13.dp)
                            ChartMetric.FOCUS -> IconFocus(color = if (isSelected) chipColor else colors.textSecondary, size = 13.dp)
                            ChartMetric.GOLD_SEAMS -> IconGoldSeam(color = if (isSelected) chipColor else colors.textSecondary, size = 13.dp)
                            ChartMetric.MOOD -> IconSparkle(color = if (isSelected) chipColor else colors.textSecondary, size = 13.dp)
                        }
                        Text(
                            text = metric.label,
                            style = typography.caption,
                            color = if (isSelected) chipColor else colors.textSecondary,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        // Dynamic Chart Card
        KintsuCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(spacing.l),
                verticalArrangement = Arrangement.spacedBy(spacing.m)
            ) {
                val totalValue = barItems.sumOf { it.value.toDouble() }
                val summaryText = when (selectedMetric) {
                    ChartMetric.HABITS -> "Total Completed: ${totalValue.toInt()} rituals"
                    ChartMetric.TASKS -> "Total Finished: ${totalValue.toInt()} tasks"
                    ChartMetric.FOCUS -> "Total Focused: ${totalValue.toInt()} minutes"
                    ChartMetric.GOLD_SEAMS -> "Mended with Gold: ${totalValue.toInt()} cracks"
                    ChartMetric.MOOD -> {
                        val validEntries = barItems.filter { it.value > 0f }
                        val avg = if (validEntries.isEmpty()) 0.0 else validEntries.map { it.value.toDouble() }.average()
                        "Average Mood: ${if (avg > 0) String.format(Locale.US, "%.1f/5", avg) else "No entries"}"
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = selectedMetric.label,
                            style = typography.itemTitle,
                            color = metricColor
                        )
                        Text(
                            text = summaryText,
                            style = typography.secondary,
                            color = colors.textSecondary
                        )
                    }

                    val peakItem = barItems.maxByOrNull { it.value }
                    if (peakItem != null && peakItem.value > 0f) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(metricColor.copy(alpha = 0.12f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Peak: ${peakItem.formattedValue}",
                                style = typography.caption,
                                color = metricColor,
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Interactive Dynamic Bars
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    barItems.forEachIndexed { index, item ->
                        val isSelected = selectedBarIndex == index
                        val fraction = if (maxValue > 0f) (item.value / maxValue).coerceIn(0f, 1f) else 0f
                        val hasValue = item.value > 0.001f

                        val barHeight by animateDpAsState(
                            targetValue = if (hasValue) (80 * fraction.coerceAtLeast(0.12f)).dp else 3.dp,
                            animationSpec = tween(durationMillis = 300),
                            label = "barHeight"
                        )

                        val barColor = when {
                            isSelected -> metricColor
                            hasValue -> metricColor.copy(alpha = 0.6f)
                            else -> colors.border.copy(alpha = 0.35f)
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    selectedBarIndex = if (isSelected) null else index
                                }
                                .padding(horizontal = 3.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(barHeight)
                                    .clip(RoundedCornerShape(if (hasValue) 6.dp else 1.dp))
                                    .background(barColor)
                                    .then(
                                        if (isSelected) Modifier.border(
                                            1.5.dp,
                                            colors.textPrimary,
                                            RoundedCornerShape(6.dp)
                                        ) else Modifier
                                    )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = item.label,
                                style = typography.caption,
                                color = if (isSelected) metricColor else colors.textTertiary,
                                fontSize = 10.sp,
                                maxLines = 1,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                AnimatedVisibility(visible = selectedBarIndex != null) {
                    selectedBarIndex?.let { idx ->
                        val item = barItems.getOrNull(idx)
                        if (item != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(KintsuTheme.shapes.small)
                                    .background(colors.surfaceElevated)
                                    .border(1.dp, metricColor.copy(alpha = 0.3f), KintsuTheme.shapes.small)
                                    .padding(spacing.s),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = item.details,
                                    style = typography.caption,
                                    color = metricColor,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
