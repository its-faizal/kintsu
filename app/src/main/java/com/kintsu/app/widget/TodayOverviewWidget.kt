package com.kintsu.app.widget

import android.content.Context
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
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
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.kintsu.app.data.KintsuDatabase
import com.kintsu.app.data.SettingsDataStore
import com.kintsu.app.domain.GoldRepairManager
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class TodayOverviewWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val database = KintsuDatabase.getDatabase(context)
        val settingsStore = SettingsDataStore(context)
        val settings = settingsStore.settingsFlow.first()
        val colors = KintsuWidgetTheme.getColors(context)

        val todayEpochDay = LocalDate.now().toEpochDay()
        val habits = database.habitDao().getActiveHabits().first()
        val scheduledHabits = habits.filter { GoldRepairManager.isHabitScheduledOn(it, todayEpochDay) }
        val habitLogs = database.habitDao().getLogsForDay(todayEpochDay).first()
        val scheduledHabitIds = scheduledHabits.map { it.id }.toSet()
        val habitsDone = habitLogs.count { it.isCompleted && scheduledHabitIds.contains(it.habitId) }
        val habitsTotal = scheduledHabits.size

        val tasks = database.taskDao().getAllTasks().first()
        val dueOrPlannedToday = tasks.filter {
            (it.dueDateEpochDay != null && it.dueDateEpochDay <= todayEpochDay) ||
            it.plannedDateEpochDay == todayEpochDay
        }
        val todayTasks = if (dueOrPlannedToday.isNotEmpty()) dueOrPlannedToday else tasks.filter { !it.isCompleted }
        val tasksDone = todayTasks.count { it.isCompleted }
        val tasksTotal = todayTasks.size

        val goals = database.goalDao().getAllGoals().first()
        val avgGoalProgress = if (goals.isNotEmpty()) {
            goals.map { it.manualProgressPercent.toDouble() }.average().toInt()
        } else 0

        val focusSessions = database.focusDao().getSessionsForDay(todayEpochDay).first()
        val focusMinutes = focusSessions.sumOf { it.durationMinutes }

        val greeting = when (LocalTime.now().hour) {
            in 5..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            else -> "Good evening"
        }
        val name = settings.displayName.ifBlank { "Seeker" }
        val dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMM d"))

        provideContent {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(colors.surfaceProvider)
                    .cornerRadius(22.dp)
                    .padding(14.dp)
            ) {
                // Header (Greeting + Date)
                Row(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .clickable(actionStartActivity(WidgetIntents.createIntent(context, WidgetIntents.ACTION_TAB_HOME))),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = GlanceModifier.defaultWeight()) {
                        Text(
                            text = "$greeting, $name",
                            style = TextStyle(
                                color = colors.textPrimaryProvider,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = dateStr,
                            style = TextStyle(
                                color = colors.textSecondaryProvider,
                                fontSize = 11.sp
                            )
                        )
                    }

                    Box(
                        modifier = GlanceModifier
                            .background(colors.accentSoftProvider)
                            .cornerRadius(10.dp)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Kintsu",
                            style = TextStyle(
                                color = colors.accentProvider,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                Spacer(modifier = GlanceModifier.height(10.dp))

                // 4-Metrics Cards Grid
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Metric 1: Habits
                    Box(
                        modifier = GlanceModifier
                            .defaultWeight()
                            .background(colors.surfaceElevatedProvider)
                            .cornerRadius(14.dp)
                            .padding(8.dp)
                            .clickable(actionStartActivity(WidgetIntents.createIntent(context, WidgetIntents.ACTION_TAB_HABITS))),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$habitsDone/$habitsTotal",
                                style = TextStyle(
                                    color = colors.accentProvider,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(modifier = GlanceModifier.height(2.dp))
                            Text(
                                text = "Rituals",
                                style = TextStyle(
                                    color = colors.textSecondaryProvider,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = GlanceModifier.width(6.dp))

                    // Metric 2: Tasks
                    Box(
                        modifier = GlanceModifier
                            .defaultWeight()
                            .background(colors.surfaceElevatedProvider)
                            .cornerRadius(14.dp)
                            .padding(8.dp)
                            .clickable(actionStartActivity(WidgetIntents.createIntent(context, WidgetIntents.ACTION_TAB_TASKS))),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (tasksTotal > 0) "$tasksDone/$tasksTotal" else "0",
                                style = TextStyle(
                                    color = colors.textPrimaryProvider,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(modifier = GlanceModifier.height(2.dp))
                            Text(
                                text = "Tasks",
                                style = TextStyle(
                                    color = colors.textSecondaryProvider,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = GlanceModifier.width(6.dp))

                    // Metric 3: Goals
                    Box(
                        modifier = GlanceModifier
                            .defaultWeight()
                            .background(colors.surfaceElevatedProvider)
                            .cornerRadius(14.dp)
                            .padding(8.dp)
                            .clickable(actionStartActivity(WidgetIntents.createIntent(context, WidgetIntents.ACTION_TAB_GOALS))),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$avgGoalProgress%",
                                style = TextStyle(
                                    color = colors.goldSeamProvider,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(modifier = GlanceModifier.height(2.dp))
                            Text(
                                text = "Goals",
                                style = TextStyle(
                                    color = colors.textSecondaryProvider,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = GlanceModifier.width(6.dp))

                    // Metric 4: Focus
                    Box(
                        modifier = GlanceModifier
                            .defaultWeight()
                            .background(colors.surfaceElevatedProvider)
                            .cornerRadius(14.dp)
                            .padding(8.dp)
                            .clickable(actionStartActivity(WidgetIntents.createIntent(context, WidgetIntents.ACTION_OPEN_FOCUS))),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${focusMinutes}m",
                                style = TextStyle(
                                    color = colors.textPrimaryProvider,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(modifier = GlanceModifier.height(2.dp))
                            Text(
                                text = "Focus",
                                style = TextStyle(
                                    color = colors.textSecondaryProvider,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

class TodayOverviewWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TodayOverviewWidget()
}
