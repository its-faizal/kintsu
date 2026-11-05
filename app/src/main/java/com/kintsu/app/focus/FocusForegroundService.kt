package com.kintsu.app.focus

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import androidx.glance.appwidget.updateAll
import com.kintsu.app.MainActivity
import com.kintsu.app.data.KintsuDatabase
import com.kintsu.app.data.SettingsDataStore
import com.kintsu.app.data.model.FocusSessionEntity
import com.kintsu.app.data.model.HabitLogEntity
import com.kintsu.app.domain.GoldRepairConfig
import com.kintsu.app.domain.GoldRepairManager
import com.kintsu.app.domain.focus.FocusState
import com.kintsu.app.domain.focus.FocusTimerManager
import com.kintsu.app.widget.HabitsGlanceWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate

class FocusForegroundService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var tickerJob: Job? = null

    companion object {
        const val CHANNEL_ID = "kintsu_focus_channel"
        const val NOTIFICATION_ID = 2001
        const val COMPLETE_NOTIFICATION_ID = 2002

        const val ACTION_START = "com.kintsu.app.focus.START"
        const val ACTION_PAUSE = "com.kintsu.app.focus.PAUSE"
        const val ACTION_RESUME = "com.kintsu.app.focus.RESUME"
        const val ACTION_STOP = "com.kintsu.app.focus.STOP"

        const val EXTRA_DURATION = "extra_duration_minutes"
        const val EXTRA_HABIT_ID = "extra_habit_id"
        const val EXTRA_TASK_ID = "extra_task_id"
        const val EXTRA_TASK_TITLE = "extra_task_title"
        const val EXTRA_IS_BREAK = "extra_is_break"

        private val _focusState = MutableStateFlow(FocusState())
        val focusState: StateFlow<FocusState> = _focusState.asStateFlow()

        fun startFocus(
            context: Context,
            durationMinutes: Int,
            habitId: Long? = null,
            taskId: Long? = null,
            taskTitle: String? = null,
            isBreak: Boolean = false
        ) {
            val intent = Intent(context, FocusForegroundService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_DURATION, durationMinutes)
                putExtra(EXTRA_HABIT_ID, habitId ?: -1L)
                putExtra(EXTRA_TASK_ID, taskId ?: -1L)
                putExtra(EXTRA_TASK_TITLE, taskTitle)
                putExtra(EXTRA_IS_BREAK, isBreak)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun pauseFocus(context: Context) {
            val intent = Intent(context, FocusForegroundService::class.java).apply {
                action = ACTION_PAUSE
            }
            context.startService(intent)
        }

        fun resumeFocus(context: Context) {
            val intent = Intent(context, FocusForegroundService::class.java).apply {
                action = ACTION_RESUME
            }
            context.startService(intent)
        }

        fun stopFocus(context: Context) {
            val intent = Intent(context, FocusForegroundService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val duration = intent.getIntExtra(EXTRA_DURATION, 25)
                val rawHabitId = intent.getLongExtra(EXTRA_HABIT_ID, -1L)
                val habitId = if (rawHabitId > 0) rawHabitId else null
                val rawTaskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)
                val taskId = if (rawTaskId > 0) rawTaskId else null
                val taskTitle = intent.getStringExtra(EXTRA_TASK_TITLE)
                val isBreak = intent.getBooleanExtra(EXTRA_IS_BREAK, false)
                startTimer(duration, habitId, taskId, taskTitle, isBreak)
            }
            ACTION_PAUSE -> {
                pauseTimer()
            }
            ACTION_RESUME -> {
                resumeTimer()
            }
            ACTION_STOP -> {
                stopTimer()
            }
        }
        return START_NOT_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Focus Sessions",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Ongoing timer for mindful focus and rest intervals"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun startTimer(
        durationMinutes: Int,
        habitId: Long?,
        taskId: Long?,
        taskTitle: String?,
        isBreak: Boolean
    ) {
        val newState = FocusTimerManager.start(durationMinutes, habitId, taskId, taskTitle, isBreak)
        _focusState.value = newState

        startForeground(NOTIFICATION_ID, buildNotification(newState))
        startTicker()
        com.kintsu.app.widget.KintsuWidgetUpdater.triggerUpdate(applicationContext)

        FocusSoundEngine.playZenBell()
        if (FocusSoundEngine.currentMode != SoundscapeMode.OFF) {
            FocusSoundEngine.start()
        }
    }

    private fun pauseTimer() {
        FocusSoundEngine.stop()
        val paused = FocusTimerManager.pause(_focusState.value)
        _focusState.value = paused
        updateNotification(paused)
        com.kintsu.app.widget.KintsuWidgetUpdater.triggerUpdate(applicationContext)
    }

    private fun resumeTimer() {
        val resumed = FocusTimerManager.resume(_focusState.value)
        _focusState.value = resumed
        updateNotification(resumed)
        com.kintsu.app.widget.KintsuWidgetUpdater.triggerUpdate(applicationContext)
        if (FocusSoundEngine.currentMode != SoundscapeMode.OFF) {
            FocusSoundEngine.start()
        }
    }

    private fun stopTimer() {
        tickerJob?.cancel()
        FocusSoundEngine.stop()
        val currentState = _focusState.value
        if (currentState.isRunning && !currentState.isBreak) {
            // Record cancelled session in history
            val now = System.currentTimeMillis()
            val elapsedMinutes = ((now - currentState.sessionStartMillis) / 60000L).toInt().coerceAtLeast(1)
            serviceScope.launch(Dispatchers.IO) {
                val db = KintsuDatabase.getDatabase(applicationContext)
                db.focusDao().insertSession(
                    FocusSessionEntity(
                        dateEpochDay = LocalDate.now().toEpochDay(),
                        durationMinutes = elapsedMinutes,
                        linkedHabitId = currentState.linkedHabitId,
                        linkedTaskId = currentState.linkedTaskId,
                        isCompleted = false,
                        startTimeMillis = currentState.sessionStartMillis,
                        endTimeMillis = now
                    )
                )
            }
        }
        _focusState.value = FocusTimerManager.cancel()
        com.kintsu.app.widget.KintsuWidgetUpdater.triggerUpdate(applicationContext)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = serviceScope.launch {
            while (true) {
                delay(1000L)
                val state = _focusState.value
                if (!state.isRunning) break

                if (FocusTimerManager.isCompleted(state)) {
                    onSessionCompleted(state)
                    break
                } else if (!state.isPaused) {
                    updateNotification(state)
                    val rem = FocusTimerManager.getRemainingSeconds(state)
                    if (rem % 60L == 0L) {
                        com.kintsu.app.widget.KintsuWidgetUpdater.triggerUpdate(applicationContext)
                    }
                }
            }
        }
    }

    private fun onSessionCompleted(state: FocusState) {
        tickerJob?.cancel()
        FocusSoundEngine.stop()
        FocusSoundEngine.playZenBell()
        val now = System.currentTimeMillis()
        val todayEpochDay = LocalDate.now().toEpochDay()

        serviceScope.launch(Dispatchers.IO) {
            val db = KintsuDatabase.getDatabase(applicationContext)
            val settings = SettingsDataStore(applicationContext).settingsFlow.first()

            // 1. Record completed focus session in DB
            if (!state.isBreak) {
                db.focusDao().insertSession(
                    FocusSessionEntity(
                        dateEpochDay = todayEpochDay,
                        durationMinutes = state.durationMinutes,
                        linkedHabitId = state.linkedHabitId,
                        linkedTaskId = state.linkedTaskId,
                        isCompleted = true,
                        startTimeMillis = state.sessionStartMillis,
                        endTimeMillis = now
                    )
                )
            }

            // 2. Habit linking & Gold Repair awareness
            if (state.linkedHabitId != null && !state.isBreak) {
                val habitId = state.linkedHabitId
                val habit = db.habitDao().getAllHabits().first().find { it.id == habitId }
                if (habit != null) {
                    val logs = db.habitDao().getAllLogs().first()
                    val availableRepair = GoldRepairManager.findAvailableRepair(
                        habit = habit,
                        logs = logs,
                        todayEpochDay = todayEpochDay,
                        goldRepairEnabled = settings.goldRepairEnabled,
                        weekStartMonday = settings.weekStartMonday
                    )

                    if (availableRepair != null && availableRepair.canRepair) {
                        // Complete by mending crack with gold seam!
                        val currentLog = logs.find { it.habitId == habitId && it.epochDay == availableRepair.missedEpochDay }
                        if (currentLog == null) {
                            db.habitDao().upsertLog(
                                HabitLogEntity(
                                    habitId = habitId,
                                    epochDay = availableRepair.missedEpochDay,
                                    isCompleted = true,
                                    isRepairedGold = true,
                                    repairedAt = now
                                )
                            )
                        } else {
                            db.habitDao().upsertLog(
                                currentLog.copy(
                                    isCompleted = true,
                                    isRepairedGold = true,
                                    repairedAt = now
                                )
                            )
                        }
                    } else {
                        // Complete standard ritual for today
                        val existingTodayLog = logs.find { it.habitId == habitId && it.epochDay == todayEpochDay }
                        if (existingTodayLog == null || !existingTodayLog.isCompleted) {
                            db.habitDao().upsertLog(
                                HabitLogEntity(
                                    id = existingTodayLog?.id ?: 0,
                                    habitId = habitId,
                                    epochDay = todayEpochDay,
                                    isCompleted = true,
                                    isRepairedGold = false,
                                    completedAt = now
                                )
                            )
                        }
                    }
                    try {
                        com.kintsu.app.widget.KintsuWidgetUpdater.triggerUpdate(applicationContext)
                    } catch (e: Exception) {
                        // ignore glance widget update failures in tests/background
                    }
                }
            }

            // 3. Vibration & Audio feedback per Settings
            if (settings.focusVibrationEnabled) {
                triggerVibration()
            }

            // 4. Completion notification
            showCompletionNotification(state)
        }

        _focusState.value = FocusTimerManager.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun triggerVibration() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(VibratorManager::class.java)
                val vibrator = vibratorManager.defaultVibrator
                vibrator.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = getSystemService(VIBRATOR_SERVICE) as? Vibrator
                vibrator?.vibrate(300)
            }
        } catch (e: Exception) {
            // Ignore vibration failure if hardware unavailable
        }
    }

    private fun showCompletionNotification(state: FocusState) {
        val title = if (state.isBreak) "Break Complete" else "Mindful Focus Complete"
        val message = if (state.isBreak) {
            "Rest interval finished. Return to stillness or begin your next intention."
        } else {
            "Completed ${state.durationMinutes} minutes of undisturbed presence."
        }

        val openAppIntent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            COMPLETE_NOTIFICATION_ID,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(COMPLETE_NOTIFICATION_ID, notification)
    }

    private fun buildNotification(state: FocusState): Notification {
        val remainingSeconds = FocusTimerManager.getRemainingSeconds(state)
        val minutes = remainingSeconds / 60
        val seconds = remainingSeconds % 60
        val timeString = String.format("%02d:%02d", minutes, seconds)

        val title = when {
            state.isBreak -> "Rest Interval"
            state.linkedTaskTitle != null -> "Focusing: ${state.linkedTaskTitle}"
            else -> "Mindful Focus"
        }
        val content = if (state.isPaused) "Paused • $timeString remaining" else "$timeString remaining"

        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Pause/Resume action
        val toggleActionIntent = Intent(this, FocusForegroundService::class.java).apply {
            action = if (state.isPaused) ACTION_RESUME else ACTION_PAUSE
        }
        val togglePendingIntent = PendingIntent.getService(
            this,
            1,
            toggleActionIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val toggleTitle = if (state.isPaused) "Resume" else "Pause"

        // Stop action
        val stopIntent = Intent(this, FocusForegroundService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            2,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle(title)
            .setContentText(content)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(contentPendingIntent)
            .addAction(android.R.drawable.ic_media_pause, toggleTitle, togglePendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification(state: FocusState) {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, buildNotification(state))
    }

    override fun onDestroy() {
        tickerJob?.cancel()
        FocusSoundEngine.stop()
        super.onDestroy()
    }
}
