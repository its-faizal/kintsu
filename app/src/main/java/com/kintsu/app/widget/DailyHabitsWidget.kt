package com.kintsu.app.widget

import android.content.Context
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionParametersOf
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.kintsu.app.data.KintsuDatabase
import com.kintsu.app.domain.GoldRepairManager
import kotlinx.coroutines.flow.first
import java.time.LocalDate

class DailyHabitsWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val database = KintsuDatabase.getDatabase(context)
        val colors = KintsuWidgetTheme.getColors(context)

        val todayEpochDay = LocalDate.now().toEpochDay()
        val habits = database.habitDao().getActiveHabits().first()
        val scheduledToday = habits.filter { GoldRepairManager.isHabitScheduledOn(it, todayEpochDay) }
        val displayHabitsList = if (scheduledToday.isNotEmpty()) scheduledToday else habits
        val todayLogs = database.habitDao().getLogsForDay(todayEpochDay).first().associateBy { it.habitId }

        val completedCount = displayHabitsList.count { todayLogs[it.id]?.isCompleted == true }
        val totalCount = displayHabitsList.size
        val displayHabits = displayHabitsList.take(5)

        provideContent {
            Row(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(colors.surfaceProvider)
                    .cornerRadius(18.dp)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Title & Counter
                Column(
                    modifier = GlanceModifier
                        .clickable(actionStartActivity(WidgetIntents.createIntent(context, WidgetIntents.ACTION_TAB_HABITS)))
                ) {
                    Text(
                        text = "Habits",
                        style = TextStyle(
                            color = colors.textPrimaryProvider,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "$completedCount/$totalCount done",
                        style = TextStyle(
                            color = colors.textSecondaryProvider,
                            fontSize = 10.sp
                        )
                    )
                }

                Spacer(modifier = GlanceModifier.defaultWeight())

                // Habit circular badges
                if (displayHabits.isEmpty()) {
                    Text(
                        text = "No rituals today",
                        style = TextStyle(
                            color = colors.textSecondaryProvider,
                            fontSize = 11.sp
                        )
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        displayHabits.forEach { habit ->
                            val isDone = todayLogs.containsKey(habit.id)
                            Box(
                                modifier = GlanceModifier
                                    .padding(horizontal = 4.dp)
                                    .size(34.dp)
                                    .background(if (isDone) colors.accentProvider else colors.surfaceElevatedProvider)
                                    .cornerRadius(17.dp)
                                    .clickable(
                                        actionRunCallback<ToggleHabitWidgetCallback>(
                                            actionParametersOf(WidgetHabitIdKey to habit.id)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isDone) "✓" else (habit.name.firstOrNull()?.uppercaseChar()?.toString() ?: "•"),
                                    style = TextStyle(
                                        color = if (isDone) colors.backgroundProvider else colors.textPrimaryProvider,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

class DailyHabitsWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = DailyHabitsWidget()
}
