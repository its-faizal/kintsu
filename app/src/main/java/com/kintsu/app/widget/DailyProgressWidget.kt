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
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.kintsu.app.data.KintsuDatabase
import com.kintsu.app.domain.GoldRepairManager
import kotlinx.coroutines.flow.first
import java.time.LocalDate

class DailyProgressWidget : GlanceAppWidget() {

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

        val totalItems = habitsTotal + tasksTotal
        val completedItems = habitsDone + tasksDone
        val percent = if (totalItems > 0) ((completedItems.toFloat() / totalItems) * 100).toInt() else 100

        provideContent {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(colors.surfaceProvider)
                    .cornerRadius(22.dp)
                    .padding(12.dp)
                    .clickable(actionStartActivity(WidgetIntents.createIntent(context, WidgetIntents.ACTION_TAB_PROGRESS))),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Daily Progress",
                    style = TextStyle(
                        color = colors.textSecondaryProvider,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = GlanceModifier.height(4.dp))

                // Central Percentage Badge
                Box(
                    modifier = GlanceModifier
                        .background(colors.surfaceElevatedProvider)
                        .cornerRadius(32.dp)
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$percent%",
                        style = TextStyle(
                            color = colors.accentProvider,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(modifier = GlanceModifier.height(6.dp))

                // Bottom Miniature Category Row
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = GlanceModifier.defaultWeight(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$habitsDone/$habitsTotal",
                            style = TextStyle(
                                color = colors.textPrimaryProvider,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "Habits",
                            style = TextStyle(
                                color = colors.textSecondaryProvider,
                                fontSize = 9.sp
                            )
                        )
                    }

                    Column(
                        modifier = GlanceModifier.defaultWeight(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$tasksDone/$tasksTotal",
                            style = TextStyle(
                                color = colors.textPrimaryProvider,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "Tasks",
                            style = TextStyle(
                                color = colors.textSecondaryProvider,
                                fontSize = 9.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

class DailyProgressWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = DailyProgressWidget()
}
