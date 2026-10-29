package com.kintsu.app.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.kintsu.app.MainActivity
import com.kintsu.app.data.KintsuDatabase
import com.kintsu.app.data.SettingsDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate

class ReminderAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val customTitle = intent.getStringExtra(EXTRA_CUSTOM_TITLE)
        val customText = intent.getStringExtra(EXTRA_CUSTOM_TEXT)

        if (customTitle != null && customText != null) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    createNotificationChannel(context)
                    val openIntent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                    val openPendingIntent = PendingIntent.getActivity(
                        context,
                        customTitle.hashCode(),
                        openIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(android.R.drawable.ic_dialog_info)
                        .setContentTitle(customTitle)
                        .setContentText(customText)
                        .setContentIntent(openPendingIntent)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .build()
                    val manager = NotificationManagerCompat.from(context)
                    manager.notify(customTitle.hashCode(), notification)
                } finally {
                    pendingResult.finish()
                }
            }
            return
        }

        val habitId = intent.getLongExtra(ReminderScheduler.EXTRA_HABIT_ID, -1L)
        val habitName = intent.getStringExtra(ReminderScheduler.EXTRA_HABIT_NAME) ?: "Daily Ritual"
        val isRepair = intent.getBooleanExtra(ReminderScheduler.EXTRA_IS_REPAIR, false)
        val missedEpochDay = intent.getLongExtra(ReminderScheduler.EXTRA_MISSED_EPOCH_DAY, -1L)

        if (habitId == -1L) return

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val settingsStore = SettingsDataStore(context)
                val settings = settingsStore.settingsFlow.first()

                if (!settings.notificationsEnabled) return@launch

                val database = KintsuDatabase.getDatabase(context)
                val todayEpochDay = LocalDate.now().toEpochDay()

                // "Only if not done yet" mode
                if (!isRepair && settings.notifyOnlyIfNotDone) {
                    val log = database.habitDao().getLog(habitId, todayEpochDay)
                    if (log?.isCompleted == true) {
                        // Already completed, skip notification!
                        return@launch
                    }
                }

                createNotificationChannel(context)

                val notificationId = (habitId * 10 + if (isRepair) 1 else 0).toInt()

                // Content intent: open MainActivity
                val openIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val openPendingIntent = PendingIntent.getActivity(
                    context,
                    notificationId,
                    openIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                // Action: Done
                val doneIntent = Intent(context, NotificationActionReceiver::class.java).apply {
                    action = NotificationActionReceiver.ACTION_DONE
                    putExtra(NotificationActionReceiver.EXTRA_HABIT_ID, habitId)
                    putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
                    putExtra(NotificationActionReceiver.EXTRA_IS_REPAIR, isRepair)
                    putExtra(NotificationActionReceiver.EXTRA_MISSED_EPOCH_DAY, missedEpochDay)
                }
                val donePendingIntent = PendingIntent.getBroadcast(
                    context,
                    notificationId * 10 + 1,
                    doneIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                // Action: Snooze 10m
                val snooze10Intent = Intent(context, NotificationActionReceiver::class.java).apply {
                    action = NotificationActionReceiver.ACTION_SNOOZE
                    putExtra(NotificationActionReceiver.EXTRA_HABIT_ID, habitId)
                    putExtra(NotificationActionReceiver.EXTRA_HABIT_NAME, habitName)
                    putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
                    putExtra(NotificationActionReceiver.EXTRA_SNOOZE_MINUTES, 10)
                }
                val snooze10PendingIntent = PendingIntent.getBroadcast(
                    context,
                    notificationId * 10 + 2,
                    snooze10Intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                // Action: Snooze 30m
                val snooze30Intent = Intent(context, NotificationActionReceiver::class.java).apply {
                    action = NotificationActionReceiver.ACTION_SNOOZE
                    putExtra(NotificationActionReceiver.EXTRA_HABIT_ID, habitId)
                    putExtra(NotificationActionReceiver.EXTRA_HABIT_NAME, habitName)
                    putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
                    putExtra(NotificationActionReceiver.EXTRA_SNOOZE_MINUTES, 30)
                }
                val snooze30PendingIntent = PendingIntent.getBroadcast(
                    context,
                    notificationId * 10 + 3,
                    snooze30Intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val title = if (isRepair) "Mending Opportunity · $habitName" else habitName
                val bodyText = if (isRepair) {
                    "A crack is waiting to be mended with gold. Take a mindful moment."
                } else {
                    "A quiet moment for your ritual."
                }

                val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle(title)
                    .setContentText(bodyText)
                    .setContentIntent(openPendingIntent)
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .addAction(
                        android.R.drawable.checkbox_on_background,
                        if (isRepair) "Mend with Gold" else "Done",
                        donePendingIntent
                    )
                    .addAction(
                        android.R.drawable.ic_popup_reminder,
                        "Snooze 10m",
                        snooze10PendingIntent
                    )
                    .addAction(
                        android.R.drawable.ic_popup_reminder,
                        "Snooze 30m",
                        snooze30PendingIntent
                    )
                    .build()

                val manager = NotificationManagerCompat.from(context)
                manager.notify(notificationId, notification)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Daily Rituals"
            val descriptionText = "Gentle reminders for daily habits and kintsugi gold mending"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "kintsu_rituals_channel"
        const val EXTRA_CUSTOM_TITLE = "extra_custom_title"
        const val EXTRA_CUSTOM_TEXT = "extra_custom_text"
    }
}
