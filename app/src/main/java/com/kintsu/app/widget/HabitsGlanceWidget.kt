package com.kintsu.app.widget

import android.content.Context
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
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
import com.kintsu.app.MainActivity
import com.kintsu.app.data.KintsuDatabase
import com.kintsu.app.data.model.HabitLogEntity
import com.kintsu.app.domain.GoldRepairManager
import kotlinx.coroutines.flow.first
import java.time.LocalDate

val HabitIdKey = ActionParameters.Key<Long>("habit_id")

class HabitsGlanceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val database = KintsuDatabase.getDatabase(context)
        val habits = database.habitDao().getActiveHabits().first()
        val todayEpochDay = LocalDate.now().toEpochDay()
        val todayLogs = database.habitDao().getLogsForDay(todayEpochDay).first().associateBy { it.habitId }

        val scheduledToday = habits.filter { GoldRepairManager.isHabitScheduledOn(it, todayEpochDay) }

        provideContent {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(KintsuGlanceTheme.backgroundProvider)
                    .cornerRadius(20.dp)
                    .padding(14.dp)
            ) {
                // Header
                Row(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .clickable(actionStartActivity<MainActivity>()),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Kintsu",
                        style = TextStyle(
                            color = KintsuGlanceTheme.goldAccentProvider,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = GlanceModifier.width(6.dp))
                    Text(
                        text = "· Today\'s Rituals",
                        style = TextStyle(
                            color = KintsuGlanceTheme.textSecondaryProvider,
                            fontSize = 13.sp
                        )
                    )
                }

                Spacer(modifier = GlanceModifier.height(10.dp))

                if (scheduledToday.isEmpty()) {
                    Box(
                        modifier = GlanceModifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No rituals scheduled today · Rest in stillness",
                            style = TextStyle(
                                color = KintsuGlanceTheme.textSecondaryProvider,
                                fontSize = 12.sp
                            )
                        )
                    }
                } else {
                    scheduledToday.take(4).forEach { habit ->
                        val log = todayLogs[habit.id]
                        val isDone = log?.isCompleted == true
                        val isRepaired = log?.isRepairedGold == true

                        Row(
                            modifier = GlanceModifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable(
                                    actionRunCallback<ToggleHabitWidgetCallback>(
                                        actionParametersOf(HabitIdKey to habit.id)
                                    )
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Checkbox circle
                            Box(
                                modifier = GlanceModifier
                                    .size(20.dp)
                                    .cornerRadius(10.dp)
                                    .background(
                                        if (isRepaired) KintsuGlanceTheme.goldAccentProvider
                                        else if (isDone) KintsuGlanceTheme.goldAccentSoftProvider
                                        else KintsuGlanceTheme.surfaceProvider
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isDone || isRepaired) {
                                    Text(
                                        text = if (isRepaired) "✦" else "✓",
                                        style = TextStyle(
                                            color = if (isRepaired) KintsuGlanceTheme.backgroundProvider else KintsuGlanceTheme.goldAccentProvider,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = GlanceModifier.width(10.dp))

                            Text(
                                text = habit.name,
                                style = TextStyle(
                                    color = if (isDone || isRepaired) KintsuGlanceTheme.textSecondaryProvider else KintsuGlanceTheme.textPrimaryProvider,
                                    fontSize = 13.sp
                                ),
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

class ToggleHabitWidgetCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val habitId = parameters[HabitIdKey] ?: return
        val database = KintsuDatabase.getDatabase(context)
        val todayEpochDay = LocalDate.now().toEpochDay()
        val currentLog = database.habitDao().getLog(habitId, todayEpochDay)

        if (currentLog == null) {
            database.habitDao().upsertLog(
                HabitLogEntity(
                    habitId = habitId,
                    epochDay = todayEpochDay,
                    isCompleted = true,
                    isRepairedGold = false
                )
            )
        } else {
            database.habitDao().upsertLog(
                currentLog.copy(
                    isCompleted = !currentLog.isCompleted,
                    isRepairedGold = false
                )
            )
        }

        HabitsGlanceWidget().updateAll(context)
        StreakGlanceWidget().updateAll(context)
    }
}

class HabitsGlanceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = HabitsGlanceWidget()
}
