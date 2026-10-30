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
import com.kintsu.app.domain.GoldRepairManager
import kotlinx.coroutines.flow.first
import java.time.LocalDate

class StackedSummaryWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val database = KintsuDatabase.getDatabase(context)
        val colors = KintsuWidgetTheme.getColors(context)

        val todayEpochDay = LocalDate.now().toEpochDay()
        val habits = database.habitDao().getActiveHabits().first()
        val scheduledHabits = habits.filter { GoldRepairManager.isHabitScheduledOn(it, todayEpochDay) }
        val habitLogs = database.habitDao().getLogsForDay(todayEpochDay).first()
        val habitsDone = habitLogs.size
        val habitsTotal = scheduledHabits.size

        val tasks = database.taskDao().getAllTasks().first()
        val todayTasks = tasks.filter { it.plannedDateEpochDay == todayEpochDay || it.dueDateEpochDay == todayEpochDay }
        val tasksDone = todayTasks.count { it.isCompleted }
        val tasksTotal = todayTasks.size

        val goals = database.goalDao().getAllGoals().first()
        val activeGoalsCount = goals.size

        val focusSessions = database.focusDao().getSessionsForDay(todayEpochDay).first()
        val focusMinutes = focusSessions.sumOf { it.durationMinutes }

        provideContent {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(colors.surfaceProvider)
                    .cornerRadius(22.dp)
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Row 1: Habits
                Row(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .background(colors.surfaceElevatedProvider)
                        .cornerRadius(8.dp)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                        .clickable(actionStartActivity(WidgetIntents.createIntent(context, WidgetIntents.ACTION_TAB_HABITS))),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "✦",
                        style = TextStyle(color = colors.accentProvider, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = GlanceModifier.width(6.dp))
                    Text(
                        text = "Habits",
                        style = TextStyle(color = colors.textPrimaryProvider, fontSize = 11.sp, fontWeight = FontWeight.Medium),
                        modifier = GlanceModifier.defaultWeight()
                    )
                    Text(
                        text = "$habitsDone/$habitsTotal",
                        style = TextStyle(color = colors.accentProvider, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = GlanceModifier.height(4.dp))

                // Row 2: Tasks
                Row(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .background(colors.surfaceElevatedProvider)
                        .cornerRadius(8.dp)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                        .clickable(actionStartActivity(WidgetIntents.createIntent(context, WidgetIntents.ACTION_TAB_TASKS))),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "✓",
                        style = TextStyle(color = colors.goldSeamProvider, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = GlanceModifier.width(6.dp))
                    Text(
                        text = "Tasks",
                        style = TextStyle(color = colors.textPrimaryProvider, fontSize = 11.sp, fontWeight = FontWeight.Medium),
                        modifier = GlanceModifier.defaultWeight()
                    )
                    Text(
                        text = "$tasksDone/$tasksTotal",
                        style = TextStyle(color = colors.textPrimaryProvider, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = GlanceModifier.height(4.dp))

                // Row 3: Goals
                Row(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .background(colors.surfaceElevatedProvider)
                        .cornerRadius(8.dp)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                        .clickable(actionStartActivity(WidgetIntents.createIntent(context, WidgetIntents.ACTION_TAB_GOALS))),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "◎",
                        style = TextStyle(color = colors.goldSeamProvider, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = GlanceModifier.width(6.dp))
                    Text(
                        text = "Goals",
                        style = TextStyle(color = colors.textPrimaryProvider, fontSize = 11.sp, fontWeight = FontWeight.Medium),
                        modifier = GlanceModifier.defaultWeight()
                    )
                    Text(
                        text = "$activeGoalsCount Active",
                        style = TextStyle(color = colors.goldSeamProvider, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = GlanceModifier.height(4.dp))

                // Row 4: Focus
                Row(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .background(colors.surfaceElevatedProvider)
                        .cornerRadius(8.dp)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                        .clickable(actionStartActivity(WidgetIntents.createIntent(context, WidgetIntents.ACTION_OPEN_FOCUS))),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "▶",
                        style = TextStyle(color = colors.accentProvider, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = GlanceModifier.width(6.dp))
                    Text(
                        text = "Focus",
                        style = TextStyle(color = colors.textPrimaryProvider, fontSize = 11.sp, fontWeight = FontWeight.Medium),
                        modifier = GlanceModifier.defaultWeight()
                    )
                    Text(
                        text = "${focusMinutes}m",
                        style = TextStyle(color = colors.textPrimaryProvider, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

class StackedSummaryWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = StackedSummaryWidget()
}
