package com.kintsu.app.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import androidx.glance.appwidget.updateAll
import com.kintsu.app.data.KintsuDatabase
import com.kintsu.app.data.model.HabitLogEntity
import com.kintsu.app.widget.HabitsGlanceWidget
import com.kintsu.app.widget.StreakGlanceWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

class NotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val habitId = intent.getLongExtra(EXTRA_HABIT_ID, -1L)
        val habitName = intent.getStringExtra(EXTRA_HABIT_NAME) ?: "Daily Ritual"
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)
        val isRepair = intent.getBooleanExtra(EXTRA_IS_REPAIR, false)
        val missedEpochDay = intent.getLongExtra(EXTRA_MISSED_EPOCH_DAY, -1L)
        val snoozeMinutes = intent.getIntExtra(EXTRA_SNOOZE_MINUTES, 10)

        if (notificationId != -1) {
            NotificationManagerCompat.from(context).cancel(notificationId)
        }

        if (habitId == -1L) return

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (action) {
                    ACTION_DONE -> {
                        val database = KintsuDatabase.getDatabase(context)
                        val targetDay = if (isRepair && missedEpochDay != -1L) missedEpochDay else LocalDate.now().toEpochDay()
                        val currentLog = database.habitDao().getLog(habitId, targetDay)
                        val now = System.currentTimeMillis()

                        if (currentLog == null) {
                            database.habitDao().upsertLog(
                                HabitLogEntity(
                                    habitId = habitId,
                                    epochDay = targetDay,
                                    isCompleted = true,
                                    isRepairedGold = isRepair,
                                    repairedAt = if (isRepair) now else null
                                )
                            )
                        } else {
                            database.habitDao().upsertLog(
                                currentLog.copy(
                                    isCompleted = true,
                                    isRepairedGold = if (isRepair) true else currentLog.isRepairedGold,
                                    repairedAt = if (isRepair) now else currentLog.repairedAt
                                )
                            )
                        }

                        // Update Glance widgets
                        HabitsGlanceWidget().updateAll(context)
                        StreakGlanceWidget().updateAll(context)
                    }

                    ACTION_SNOOZE -> {
                        ReminderScheduler.scheduleSnooze(context, habitId, habitName, snoozeMinutes)
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_DONE = "com.kintsu.app.action.NOTIFICATION_DONE"
        const val ACTION_SNOOZE = "com.kintsu.app.action.NOTIFICATION_SNOOZE"

        const val EXTRA_HABIT_ID = "extra_habit_id"
        const val EXTRA_HABIT_NAME = "extra_habit_name"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
        const val EXTRA_IS_REPAIR = "extra_is_repair"
        const val EXTRA_MISSED_EPOCH_DAY = "extra_missed_epoch_day"
        const val EXTRA_SNOOZE_MINUTES = "extra_snooze_minutes"
    }
}
