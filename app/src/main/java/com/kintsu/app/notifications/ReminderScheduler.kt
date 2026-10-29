package com.kintsu.app.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.kintsu.app.data.KintsuDatabase
import com.kintsu.app.data.SettingsDataStore
import com.kintsu.app.data.model.HabitEntity
import com.kintsu.app.domain.GoldRepairManager
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object ReminderScheduler {

    const val EXTRA_HABIT_ID = "extra_habit_id"
    const val EXTRA_HABIT_NAME = "extra_habit_name"
    const val EXTRA_TIME_STR = "extra_time_str"
    const val EXTRA_IS_REPAIR = "extra_is_repair"
    const val EXTRA_MISSED_EPOCH_DAY = "extra_missed_epoch_day"

    suspend fun scheduleAll(context: Context) {
        val database = KintsuDatabase.getDatabase(context)
        val settingsStore = SettingsDataStore(context)
        val settings = settingsStore.settingsFlow.first()

        if (!settings.notificationsEnabled) {
            cancelAll(context)
            return
        }

        val habits = database.habitDao().getActiveHabits().first()
        val todayEpochDay = LocalDate.now().toEpochDay()
        val allLogs = database.habitDao().getAllLogs().first()

        habits.forEach { habit ->
            // Schedule habit reminders
            val times = parseReminderTimes(habit, settings.defaultReminderTime)
            times.forEachIndexed { index, timeStr ->
                scheduleHabitAlarm(context, habit, timeStr, requestCode = (habit.id * 100 + index).toInt())
            }

            // Check if gold repair reminder should be scheduled
            if (settings.goldRepairEnabled && settings.goldRepairReminderEnabled) {
                val repairState = GoldRepairManager.findAvailableRepair(
                    habit = habit,
                    logs = allLogs,
                    todayEpochDay = todayEpochDay,
                    goldRepairEnabled = true,
                    weekStartMonday = settings.weekStartMonday
                )
                if (repairState != null && repairState.canRepair) {
                    scheduleRepairAlarm(context, habit, repairState.missedEpochDay)
                }
            }
        }

        // Schedule Daily Intention reminder
        if (settings.dailyIntentionEnabled) {
            scheduleNamedAlarm(
                context,
                "What matters most today?",
                "Take a serene moment to declare your core daily intention.",
                settings.dailyIntentionReminderTime,
                requestCode = 9101
            )
        }

        // Schedule Evening Journal reminder
        if (settings.journalEnabled) {
            scheduleNamedAlarm(
                context,
                "Evening Reflection",
                "A quiet pause to capture what was noticed or mended today.",
                settings.journalReminderTime,
                requestCode = 9102
            )
        }

        // Schedule Weekly Review reminder
        if (settings.weeklyReviewEnabled) {
            scheduleNamedAlarm(
                context,
                "Weekly Review Ready",
                "Close this cycle with honest perspective derived from real data.",
                settings.weeklyReviewReminderTime,
                requestCode = 9103
            )
        }
    }

    fun scheduleNamedAlarm(
        context: Context,
        title: String,
        text: String,
        timeStr: String,
        requestCode: Int
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val triggerMillis = computeNextTriggerMillis(timeStr)
        val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            putExtra(ReminderAlarmReceiver.EXTRA_CUSTOM_TITLE, title)
            putExtra(ReminderAlarmReceiver.EXTRA_CUSTOM_TEXT, text)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        scheduleExactOrIdle(alarmManager, triggerMillis, pendingIntent)
    }

    fun parseReminderTimes(habit: HabitEntity, defaultTime: String): List<String> {
        return when {
            habit.reminderTimes.isNotBlank() -> {
                habit.reminderTimes.split(",").map { it.trim() }.filter { it.isNotBlank() }
            }
            !habit.reminderTime.isNullOrBlank() -> {
                listOf(habit.reminderTime.trim())
            }
            else -> {
                listOf(defaultTime)
            }
        }
    }

    private fun scheduleHabitAlarm(
        context: Context,
        habit: HabitEntity,
        timeStr: String,
        requestCode: Int
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val triggerMillis = computeNextTriggerMillis(timeStr)
        val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            putExtra(EXTRA_HABIT_ID, habit.id)
            putExtra(EXTRA_HABIT_NAME, habit.name)
            putExtra(EXTRA_TIME_STR, timeStr)
            putExtra(EXTRA_IS_REPAIR, false)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        scheduleExactOrIdle(alarmManager, triggerMillis, pendingIntent)
    }

    fun scheduleSnooze(
        context: Context,
        habitId: Long,
        habitName: String,
        snoozeMinutes: Int
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val triggerMillis = System.currentTimeMillis() + (snoozeMinutes * 60 * 1000L)

        val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            putExtra(EXTRA_HABIT_ID, habitId)
            putExtra(EXTRA_HABIT_NAME, habitName)
            putExtra(EXTRA_TIME_STR, "snooze")
            putExtra(EXTRA_IS_REPAIR, false)
        }

        val requestCode = (habitId * 1000 + snoozeMinutes).toInt()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        scheduleExactOrIdle(alarmManager, triggerMillis, pendingIntent)
    }

    private fun scheduleRepairAlarm(
        context: Context,
        habit: HabitEntity,
        missedEpochDay: Long
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        // Trigger around 10:00 AM today or tomorrow
        val triggerMillis = computeNextTriggerMillis("10:00")

        val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            putExtra(EXTRA_HABIT_ID, habit.id)
            putExtra(EXTRA_HABIT_NAME, habit.name)
            putExtra(EXTRA_IS_REPAIR, true)
            putExtra(EXTRA_MISSED_EPOCH_DAY, missedEpochDay)
        }

        val requestCode = (habit.id * 10000 + 999).toInt()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        scheduleExactOrIdle(alarmManager, triggerMillis, pendingIntent)
    }

    private fun scheduleExactOrIdle(
        alarmManager: AlarmManager,
        triggerMillis: Long,
        pendingIntent: PendingIntent
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
        }
    }

    private fun computeNextTriggerMillis(timeStr: String): Long {
        return try {
            val formatter = DateTimeFormatter.ofPattern("HH:mm")
            val targetTime = LocalTime.parse(timeStr.trim(), formatter)
            val now = LocalDateTime.now()
            var targetDateTime = now.withHour(targetTime.hour).withMinute(targetTime.minute).withSecond(0).withNano(0)

            if (targetDateTime.isBefore(now)) {
                targetDateTime = targetDateTime.plusDays(1)
            }
            targetDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        } catch (e: Exception) {
            System.currentTimeMillis() + 3600 * 1000L
        }
    }

    fun cancelAll(context: Context) {
        // Can be called when notifications are disabled globally
    }
}
