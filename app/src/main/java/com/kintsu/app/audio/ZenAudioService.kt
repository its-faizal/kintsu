package com.kintsu.app.audio

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.kintsu.app.MainActivity
import com.kintsu.app.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ZenAudioService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())

    companion object {
        const val CHANNEL_ID = "kintsu_zen_audio_channel"
        const val NOTIFICATION_ID = 3001

        const val ACTION_PLAY = "com.kintsu.app.audio.PLAY"
        const val ACTION_PAUSE = "com.kintsu.app.audio.PAUSE"
        const val ACTION_STOP = "com.kintsu.app.audio.STOP"
        const val EXTRA_TRACK_ID = "extra_track_id"

        fun startTrack(context: Context, track: ZenSoundTrack) {
            val intent = Intent(context, ZenAudioService::class.java).apply {
                action = ACTION_PLAY
                putExtra(EXTRA_TRACK_ID, track.id)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopAudio(context: Context) {
            val intent = Intent(context, ZenAudioService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        serviceScope.launch {
            ZenAudioPlayer.isPlaying.collectLatest { playing ->
                val track = ZenAudioPlayer.currentTrack.value
                if (track != null) {
                    updateNotification(track, playing)
                } else if (!playing) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> {
                val trackId = intent.getStringExtra(EXTRA_TRACK_ID)
                val track = trackId?.let { ZenSoundTrack.fromId(it) } ?: ZenSoundTrack.BAMBOO_RAIN
                ZenAudioPlayer.play(this, track)
                startForeground(NOTIFICATION_ID, buildNotification(track, isPlaying = true))
            }
            ACTION_PAUSE -> {
                ZenAudioPlayer.pause()
            }
            ACTION_STOP -> {
                ZenAudioPlayer.stop()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Zen Soundscapes",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Background playback for Kintsu meditation and focus soundscapes"
                setShowBadge(false)
            }
            val nm = getSystemService(NotificationManager::class.java)
            nm?.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(track: ZenSoundTrack, isPlaying: Boolean): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val toggleActionIntent = Intent(this, ZenAudioService::class.java).apply {
            action = if (isPlaying) ACTION_PAUSE else ACTION_PLAY
            putExtra(EXTRA_TRACK_ID, track.id)
        }
        val togglePendingIntent = PendingIntent.getService(
            this,
            1,
            toggleActionIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopActionIntent = Intent(this, ZenAudioService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            2,
            stopActionIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.app_logo_kintsu)
            .setContentTitle(track.title)
            .setContentText(track.description)
            .setContentIntent(openAppPendingIntent)
            .addAction(
                0,
                if (isPlaying) "Pause" else "Resume",
                togglePendingIntent
            )
            .addAction(0, "End Soundscape", stopPendingIntent)
            .setOngoing(isPlaying)
            .setSilent(true)
            .build()
    }

    private fun updateNotification(track: ZenSoundTrack, isPlaying: Boolean) {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        nm?.notify(NOTIFICATION_ID, buildNotification(track, isPlaying))
    }

    override fun onDestroy() {
        serviceScope.launch {
            ZenAudioPlayer.stop()
        }
        super.onDestroy()
    }
}
