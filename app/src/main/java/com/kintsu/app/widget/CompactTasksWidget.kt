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
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.kintsu.app.data.KintsuDatabase
import kotlinx.coroutines.flow.first
import java.time.LocalDate

class CompactTasksWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val database = KintsuDatabase.getDatabase(context)
        val colors = KintsuWidgetTheme.getColors(context)

        val todayEpochDay = LocalDate.now().toEpochDay()
        val allTasks = database.taskDao().getAllTasks().first()
        val todayTasks = allTasks.filter {
            it.plannedDateEpochDay == todayEpochDay || it.dueDateEpochDay == todayEpochDay
        }.sortedBy { it.isCompleted }

        val completedCount = todayTasks.count { it.isCompleted }
        val totalCount = todayTasks.size
        val displayTasks = todayTasks.take(3)

        provideContent {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(colors.surfaceProvider)
                    .cornerRadius(22.dp)
                    .padding(12.dp)
            ) {
                // Header Row
                Row(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .clickable(actionStartActivity(WidgetIntents.createIntent(context, WidgetIntents.ACTION_TAB_TASKS))),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tasks",
                        style = TextStyle(
                            color = colors.textPrimaryProvider,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = GlanceModifier.defaultWeight()
                    )
                    Text(
                        text = "$completedCount/$totalCount",
                        style = TextStyle(
                            color = colors.accentProvider,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(modifier = GlanceModifier.height(6.dp))

                if (displayTasks.isEmpty()) {
                    Box(
                        modifier = GlanceModifier
                            .fillMaxSize()
                            .clickable(actionStartActivity(WidgetIntents.createIntent(context, WidgetIntents.ACTION_ADD_TASK))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No tasks today\n+ Tap to add",
                            style = TextStyle(
                                color = colors.textSecondaryProvider,
                                fontSize = 11.sp
                            )
                        )
                    }
                } else {
                    displayTasks.forEach { task ->
                        Row(
                            modifier = GlanceModifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = GlanceModifier
                                    .size(18.dp)
                                    .background(if (task.isCompleted) colors.accentProvider else colors.surfaceElevatedProvider)
                                    .cornerRadius(5.dp)
                                    .clickable(
                                        actionRunCallback<ToggleTaskWidgetCallback>(
                                            actionParametersOf(WidgetTaskIdKey to task.id)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (task.isCompleted) {
                                    Text(
                                        text = "✓",
                                        style = TextStyle(
                                            color = colors.backgroundProvider,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = GlanceModifier.width(6.dp))

                            Text(
                                text = task.title,
                                style = TextStyle(
                                    color = if (task.isCompleted) colors.textTertiaryProvider else colors.textPrimaryProvider,
                                    fontSize = 11.sp,
                                    fontWeight = if (task.isCompleted) FontWeight.Normal else FontWeight.Medium
                                ),
                                maxLines = 1,
                                modifier = GlanceModifier
                                    .defaultWeight()
                                    .clickable(actionStartActivity(WidgetIntents.createIntent(context, WidgetIntents.ACTION_TAB_TASKS)))
                            )
                        }
                    }
                }
            }
        }
    }
}

class CompactTasksWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CompactTasksWidget()
}
