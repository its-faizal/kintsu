package com.kintsu.app.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.updateAll
import com.kintsu.app.MainActivity
import com.kintsu.app.data.KintsuDatabase
import com.kintsu.app.data.model.HabitLogEntity
import com.kintsu.app.data.model.TaskCompletionEntity
import com.kintsu.app.focus.FocusForegroundService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.LocalDate

val WidgetHabitIdKey = ActionParameters.Key<Long>("widget_habit_id")
val WidgetTaskIdKey = ActionParameters.Key<Long>("widget_task_id")
val WidgetFocusActionKey = ActionParameters.Key<String>("widget_focus_action")
val WidgetFocusPresetKey = ActionParameters.Key<Int>("widget_focus_preset")

object WidgetIntents {
    const val ACTION_TAB_HOME = "com.kintsu.app.widget.TAB_HOME"
    const val ACTION_TAB_HABITS = "com.kintsu.app.widget.TAB_HABITS"
    const val ACTION_TAB_TASKS = "com.kintsu.app.widget.TAB_TASKS"
    const val ACTION_TAB_GOALS = "com.kintsu.app.widget.TAB_GOALS"
    const val ACTION_TAB_PROGRESS = "com.kintsu.app.widget.TAB_PROGRESS"
    const val ACTION_ADD_TASK = "com.kintsu.app.widget.ADD_TASK"
    const val ACTION_ADD_HABIT = "com.kintsu.app.widget.ADD_HABIT"
    const val ACTION_OPEN_FOCUS = "com.kintsu.app.widget.OPEN_FOCUS"
    const val ACTION_OPEN_JOURNAL = "com.kintsu.app.widget.OPEN_JOURNAL"
    const val ACTION_OPEN_INTENTION = "com.kintsu.app.widget.OPEN_INTENTION"

    fun createIntent(context: Context, actionStr: String): Intent {
        return Intent(context, MainActivity::class.java).apply {
            action = actionStr
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
    }
}

class ToggleHabitWidgetCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val habitId = parameters[WidgetHabitIdKey] ?: return
        val database = KintsuDatabase.getDatabase(context)
        val todayEpochDay = LocalDate.now().toEpochDay()
        val existingLog = database.habitDao().getLog(habitId, todayEpochDay)
        if (existingLog != null) {
            database.habitDao().deleteLog(existingLog)
        } else {
            database.habitDao().upsertLog(
                HabitLogEntity(
                    habitId = habitId,
                    epochDay = todayEpochDay,
                    isCompleted = true,
                    completedAt = System.currentTimeMillis()
                )
            )
        }
        KintsuWidgetUpdater.updateAllWidgets(context)
    }
}

class ToggleTaskWidgetCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val taskId = parameters[WidgetTaskIdKey] ?: return
        val database = KintsuDatabase.getDatabase(context)
        val task = database.taskDao().getTaskById(taskId) ?: return
        val todayEpochDay = LocalDate.now().toEpochDay()
        if (task.isCompleted) {
            database.taskDao().updateTask(task.copy(isCompleted = false, completedAt = null))
            database.taskCompletionDao().deleteCompletionForTaskDay(taskId, todayEpochDay)
        } else {
            database.taskDao().updateTask(task.copy(isCompleted = true, completedAt = System.currentTimeMillis()))
            database.taskCompletionDao().insertCompletion(
                TaskCompletionEntity(
                    taskId = taskId,
                    epochDay = todayEpochDay,
                    completedAt = System.currentTimeMillis()
                )
            )
        }
        KintsuWidgetUpdater.updateAllWidgets(context)
    }
}

class FocusControlWidgetCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val focusAction = parameters[WidgetFocusActionKey] ?: "START"
        val preset = parameters[WidgetFocusPresetKey] ?: 25
        when (focusAction) {
            "START" -> FocusForegroundService.startFocus(context, preset)
            "PAUSE" -> {
                val intent = Intent(context, FocusForegroundService::class.java).apply {
                    action = FocusForegroundService.ACTION_PAUSE
                }
                context.startService(intent)
            }
            "RESUME" -> {
                val intent = Intent(context, FocusForegroundService::class.java).apply {
                    action = FocusForegroundService.ACTION_RESUME
                }
                context.startService(intent)
            }
            "STOP" -> {
                val intent = Intent(context, FocusForegroundService::class.java).apply {
                    action = FocusForegroundService.ACTION_STOP
                }
                context.startService(intent)
            }
        }
        KintsuWidgetUpdater.updateAllWidgets(context)
    }
}

object KintsuWidgetUpdater {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun triggerUpdate(context: Context) {
        scope.launch {
            updateAllWidgets(context)
        }
    }

    suspend fun updateAllWidgets(context: Context) {
        try { TodayOverviewWidget().updateAll(context) } catch (_: Exception) {}
        try { FocusTimerWidget().updateAll(context) } catch (_: Exception) {}
        try { TodayTasksWidget().updateAll(context) } catch (_: Exception) {}
        try { DailyHabitsWidget().updateAll(context) } catch (_: Exception) {}
        try { GoalsProgressWidget().updateAll(context) } catch (_: Exception) {}
        try { DailyQuoteWidget().updateAll(context) } catch (_: Exception) {}
        try { DailyProgressWidget().updateAll(context) } catch (_: Exception) {}
        try { QuickAddWidget().updateAll(context) } catch (_: Exception) {}
        try { StackedSummaryWidget().updateAll(context) } catch (_: Exception) {}
        try { AestheticQuoteWidget().updateAll(context) } catch (_: Exception) {}
        try { CompactTasksWidget().updateAll(context) } catch (_: Exception) {}
        try { HabitsGlanceWidget().updateAll(context) } catch (_: Exception) {}
        try { StreakGlanceWidget().updateAll(context) } catch (_: Exception) {}

        // Broadcast to ensure launchers refresh
        try {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val receivers = listOf(
                TodayOverviewWidgetReceiver::class.java,
                FocusTimerWidgetReceiver::class.java,
                TodayTasksWidgetReceiver::class.java,
                DailyHabitsWidgetReceiver::class.java,
                GoalsProgressWidgetReceiver::class.java,
                DailyQuoteWidgetReceiver::class.java,
                DailyProgressWidgetReceiver::class.java,
                QuickAddWidgetReceiver::class.java,
                StackedSummaryWidgetReceiver::class.java,
                AestheticQuoteWidgetReceiver::class.java,
                CompactTasksWidgetReceiver::class.java,
                HabitsGlanceWidgetReceiver::class.java,
                StreakGlanceWidgetReceiver::class.java
            )
            for (receiver in receivers) {
                val componentName = ComponentName(context, receiver)
                val ids = appWidgetManager.getAppWidgetIds(componentName)
                if (ids != null && ids.isNotEmpty()) {
                    val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE).apply {
                        component = componentName
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                    }
                    context.sendBroadcast(intent)
                }
            }
        } catch (_: Exception) {}
    }
}
