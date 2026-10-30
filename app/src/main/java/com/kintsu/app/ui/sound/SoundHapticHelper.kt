package com.kintsu.app.ui.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

object SoundHapticHelper {

    enum class SoundEffect(val id: String, val title: String, val category: String) {
        SOFT_CHIME("sound_soft_chime", "Soft Chime", "Habits"),
        GENTLE_BELL("sound_gentle_bell", "Gentle Bell", "Habits"),
        WOODEN_TAP("sound_wooden_tap", "Wooden Tap", "Habits"),
        QUIET_SPARKLE("sound_quiet_sparkle", "Quiet Sparkle", "Habits"),
        WARM_RESONANCE("sound_warm_resonance", "Warm Resonance", "Focus"),
        SOFT_PIANO("sound_soft_piano", "Soft Piano Tone", "Focus"),
        CALM_BELL("sound_calm_bell", "Calm Bell", "Focus"),
        MINIMAL_TONE("sound_minimal_tone", "Minimal Digital Tone", "Focus")
    }

    enum class HapticPreset(val id: String, val label: String) {
        OFF("OFF", "Off"),
        SUBTLE("SUBTLE", "Subtle"),
        STANDARD("STANDARD", "Standard")
    }

    private val audioScope = CoroutineScope(Dispatchers.Default)

    /**
     * Plays a pleasant synthesized acoustic tone asynchronously.
     */
    fun playSound(context: Context, effect: SoundEffect, enabled: Boolean = true) {
        if (!enabled) return

        audioScope.launch {
            try {
                when (effect) {
                    SoundEffect.SOFT_CHIME -> playTones(listOf(523.25f, 659.25f, 783.99f), 0.35f, 0.4f) // C5-E5-G5 triad chime
                    SoundEffect.GENTLE_BELL -> playBell(880.0f, 0.6f, 0.5f) // A5 pure bell
                    SoundEffect.WOODEN_TAP -> playClick(220.0f, 0.08f, 0.35f) // Woodblock tap
                    SoundEffect.QUIET_SPARKLE -> playSparkle(listOf(659.25f, 880.0f, 1046.5f, 1318.5f), 0.3f) // E5-A5-C6-E6 arpeggio
                    SoundEffect.WARM_RESONANCE -> playResonance(216.0f, 1.2f, 0.5f) // Tibetan singing bowl fundamental
                    SoundEffect.SOFT_PIANO -> playPianoTone(440.0f, 0.8f, 0.5f) // Warm A4
                    SoundEffect.CALM_BELL -> playBell(587.33f, 0.9f, 0.5f) // D5 calming chime
                    SoundEffect.MINIMAL_TONE -> playTones(listOf(880.0f), 0.12f, 0.25f) // Crisp minimal pip
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun playSoundById(context: Context, soundId: String, enabled: Boolean = true) {
        val effect = SoundEffect.values().find { it.id == soundId } ?: SoundEffect.SOFT_CHIME
        playSound(context, effect, enabled)
    }

    /**
     * Triggers tactile feedback conforming to the user's selected preset.
     */
    fun performHaptic(context: Context, preset: HapticPreset) {
        if (preset == HapticPreset.OFF) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                val vibrator = vm?.defaultVibrator
                if (vibrator != null && vibrator.hasVibrator()) {
                    val effect = if (preset == HapticPreset.SUBTLE) {
                        VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
                    } else {
                        VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                    }
                    vibrator.vibrate(effect)
                }
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (vibrator != null && vibrator.hasVibrator()) {
                    val ms = if (preset == HapticPreset.SUBTLE) 10L else 30L
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val amplitude = if (preset == HapticPreset.SUBTLE) 60 else 180
                        vibrator.vibrate(VibrationEffect.createOneShot(ms, amplitude))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(ms)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun playTones(frequencies: List<Float>, durationSec: Float, maxVolume: Float) {
        val sampleRate = 44100
        val numSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            // Exponential decay envelope
            val envelope = exp(-4.0 * (t / durationSec).toDouble()).toFloat()
            var sample = 0f
            for (freq in frequencies) {
                sample += sin(2.0 * PI * freq * t).toFloat()
            }
            sample = (sample / frequencies.size) * envelope * maxVolume
            buffer[i] = (sample * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }

        writeAudioTrack(buffer, sampleRate)
    }

    private fun playBell(frequency: Float, durationSec: Float, maxVolume: Float) {
        val sampleRate = 44100
        val numSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val envelope = exp(-3.0 * (t / durationSec).toDouble()).toFloat()
            val fundamental = sin(2.0 * PI * frequency * t).toFloat()
            val overtone = 0.3f * sin(2.0 * PI * (frequency * 2.76f) * t).toFloat()
            val overtone2 = 0.15f * sin(2.0 * PI * (frequency * 5.4f) * t).toFloat()
            val sample = (fundamental + overtone + overtone2) * envelope * maxVolume
            buffer[i] = (sample * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }

        writeAudioTrack(buffer, sampleRate)
    }

    private fun playClick(frequency: Float, durationSec: Float, maxVolume: Float) {
        val sampleRate = 44100
        val numSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val envelope = exp(-25.0 * (t / durationSec).toDouble()).toFloat()
            val sample = sin(2.0 * PI * frequency * t).toFloat() * envelope * maxVolume
            buffer[i] = (sample * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }

        writeAudioTrack(buffer, sampleRate)
    }

    private fun playSparkle(frequencies: List<Float>, totalSec: Float) {
        val sampleRate = 44100
        val numSamples = (sampleRate * totalSec).toInt()
        val buffer = ShortArray(numSamples)
        val stepSamples = numSamples / frequencies.size

        for (i in 0 until numSamples) {
            val noteIndex = (i / stepSamples).coerceIn(0, frequencies.size - 1)
            val freq = frequencies[noteIndex]
            val localT = (i % stepSamples).toFloat() / sampleRate
            val localDuration = stepSamples.toFloat() / sampleRate
            val envelope = exp(-5.0 * (localT / localDuration).toDouble()).toFloat()
            val sample = sin(2.0 * PI * freq * localT).toFloat() * envelope * 0.4f
            buffer[i] = (sample * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }

        writeAudioTrack(buffer, sampleRate)
    }

    private fun playResonance(frequency: Float, durationSec: Float, maxVolume: Float) {
        val sampleRate = 44100
        val numSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val envelope = exp(-1.5 * (t / durationSec).toDouble()).toFloat()
            val beat = 0.5f * (1f + sin(2.0 * PI * 2.5 * t).toFloat()) // 2.5 Hz calming acoustic beating
            val sample = sin(2.0 * PI * frequency * t).toFloat() * envelope * beat * maxVolume
            buffer[i] = (sample * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }

        writeAudioTrack(buffer, sampleRate)
    }

    private fun playPianoTone(frequency: Float, durationSec: Float, maxVolume: Float) {
        val sampleRate = 44100
        val numSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val envelope = exp(-2.5 * (t / durationSec).toDouble()).toFloat()
            val fund = sin(2.0 * PI * frequency * t).toFloat()
            val h2 = 0.4f * sin(2.0 * PI * (frequency * 2.0f) * t).toFloat()
            val h3 = 0.2f * sin(2.0 * PI * (frequency * 3.0f) * t).toFloat()
            val sample = (fund + h2 + h3) * envelope * maxVolume
            buffer[i] = (sample * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }

        writeAudioTrack(buffer, sampleRate)
    }

    private fun writeAudioTrack(buffer: ShortArray, sampleRate: Int) {
        var track: AudioTrack? = null
        try {
            val minBufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val bufferSize = maxOf(minBufferSize, buffer.size * 2)

            track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(buffer, 0, buffer.size)
            track.play()
            Thread.sleep((buffer.size * 1000L / sampleRate) + 50L)
        } catch (e: Exception) {
            // Ignore audio interruption
        } finally {
            try {
                track?.stop()
                track?.release()
            } catch (e: Exception) {
                // Ignore cleanup
            }
        }
    }
}
