package com.kintsu.app.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

object ZenAudioPlayer {

    private const val TAG = "ZenAudioPlayer"
    private var mediaPlayer: MediaPlayer? = null
    private var audioManager: AudioManager? = null
    private var focusRequest: AudioFocusRequest? = null

    private val playerScope = CoroutineScope(Dispatchers.Main + Job())
    private var fadeJob: Job? = null

    private val _currentTrack = MutableStateFlow<ZenSoundTrack?>(null)
    val currentTrack: StateFlow<ZenSoundTrack?> = _currentTrack.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _volume = MutableStateFlow(0.75f)
    val volume: StateFlow<Float> = _volume.asStateFlow()

    private val afChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> {
                Log.d(TAG, "Audio focus lost permanently, stopping track")
                stop()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                Log.d(TAG, "Audio focus lost transiently, pausing")
                pause()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                Log.d(TAG, "Ducking audio volume")
                mediaPlayer?.setVolume(_volume.value * 0.3f, _volume.value * 0.3f)
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                Log.d(TAG, "Audio focus gained, restoring volume")
                val v = _volume.value
                mediaPlayer?.setVolume(v, v)
                if (!_isPlaying.value && _currentTrack.value != null) {
                    resume()
                }
            }
        }
    }

    fun init(context: Context) {
        if (audioManager == null) {
            audioManager = context.applicationContext.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        }
    }

    fun play(context: Context, track: ZenSoundTrack) {
        init(context)

        // If the same track is already playing, do nothing
        if (_currentTrack.value == track && _isPlaying.value) {
            return
        }

        playerScope.launch {
            // Fade out existing player if active
            if (mediaPlayer != null) {
                fadeVolume(from = _volume.value, to = 0f, durationMs = 300)
                releasePlayer()
            }

            requestAudioFocus()

            try {
                val mp = MediaPlayer.create(context.applicationContext, track.rawResId).apply {
                    isLooping = true
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    setVolume(0f, 0f)
                    start()
                }

                mediaPlayer = mp
                _currentTrack.value = track
                _isPlaying.value = true

                // Smooth fade-in
                fadeVolume(from = 0f, to = _volume.value, durationMs = 600)
            } catch (e: Exception) {
                Log.e(TAG, "Error playing track ${track.title}", e)
                _isPlaying.value = false
            }
        }
    }

    fun pause() {
        playerScope.launch {
            fadeVolume(from = _volume.value, to = 0f, durationMs = 300)
            mediaPlayer?.pause()
            _isPlaying.value = false
        }
    }

    fun resume() {
        if (mediaPlayer != null && _currentTrack.value != null) {
            requestAudioFocus()
            mediaPlayer?.start()
            _isPlaying.value = true
            fadeVolume(from = 0f, to = _volume.value, durationMs = 400)
        }
    }

    fun stop() {
        playerScope.launch {
            if (mediaPlayer != null) {
                fadeVolume(from = _volume.value, to = 0f, durationMs = 400)
                releasePlayer()
            }
            abandonAudioFocus()
            _currentTrack.value = null
            _isPlaying.value = false
        }
    }

    fun togglePlayPause(context: Context, track: ZenSoundTrack) {
        if (_currentTrack.value == track) {
            if (_isPlaying.value) {
                pause()
            } else {
                resume()
            }
        } else {
            play(context, track)
        }
    }

    fun setVolume(vol: Float) {
        val clamped = vol.coerceIn(0f, 1f)
        _volume.value = clamped
        if (_isPlaying.value) {
            mediaPlayer?.setVolume(clamped, clamped)
        }
    }

    private fun fadeVolume(from: Float, to: Float, durationMs: Long) {
        fadeJob?.cancel()
        fadeJob = playerScope.launch {
            val steps = 15
            val stepDelay = durationMs / steps
            for (i in 1..steps) {
                val current = from + (to - from) * (i.toFloat() / steps)
                mediaPlayer?.setVolume(current, current)
                delay(stepDelay)
            }
            mediaPlayer?.setVolume(to, to)
        }
    }

    private fun requestAudioFocus(): Boolean {
        val am = audioManager ?: return false
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val req = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setOnAudioFocusChangeListener(afChangeListener)
                .build()
            focusRequest = req
            am.requestAudioFocus(req) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        } else {
            @Suppress("DEPRECATION")
            am.requestAudioFocus(
                afChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            ) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
    }

    private fun abandonAudioFocus() {
        val am = audioManager ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusRequest?.let { am.abandonAudioFocusRequest(it) }
            focusRequest = null
        } else {
            @Suppress("DEPRECATION")
            am.abandonAudioFocus(afChangeListener)
        }
    }

    private fun releasePlayer() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing player", e)
        } finally {
            mediaPlayer = null
        }
    }
}
