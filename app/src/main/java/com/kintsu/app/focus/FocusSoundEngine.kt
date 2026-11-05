package com.kintsu.app.focus

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Random
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

enum class SoundscapeMode(val displayName: String) {
    OFF("Silent"),
    BROWN_NOISE("Deep Rain & Waterfall"),
    BINAURAL_40HZ("40Hz Gamma Flow")
}

/**
 * Zero-asset real-time audio synthesizer for deep mindful focus.
 * Synthesizes acoustic brown noise and binaural beats directly via AudioTrack.
 */
object FocusSoundEngine {

    private const val SAMPLE_RATE = 44100
    private var audioTrack: AudioTrack? = null
    private var synthesizerJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    var currentMode: SoundscapeMode = SoundscapeMode.OFF
        private set

    fun setMode(mode: SoundscapeMode) {
        if (currentMode == mode) return
        stop()
        currentMode = mode
        if (mode != SoundscapeMode.OFF) {
            start(mode)
        }
    }

    fun start(mode: SoundscapeMode = currentMode) {
        if (mode == SoundscapeMode.OFF) return
        stop()
        currentMode = mode

        synthesizerJob = scope.launch {
            val minBufSize = AudioTrack.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val bufferSize = (minBufSize * 2).coerceAtLeast(SAMPLE_RATE / 4)

            val track = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()
            } else {
                @Suppress("DEPRECATION")
                AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_STEREO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize,
                    AudioTrack.MODE_STREAM
                )
            }

            audioTrack = track
            track.play()

            val pcmBuffer = ShortArray(bufferSize / 2) // Stereo pairs

            when (mode) {
                SoundscapeMode.BROWN_NOISE -> streamBrownNoise(track, pcmBuffer)
                SoundscapeMode.BINAURAL_40HZ -> streamBinauralBeats(track, pcmBuffer, carrierHz = 200.0, beatHz = 40.0)
                SoundscapeMode.OFF -> {}
            }
        }
    }

    private fun CoroutineScope.streamBrownNoise(track: AudioTrack, buffer: ShortArray) {
        var lastOutL = 0.0
        var lastOutR = 0.0
        val random = Random()

        while (isActive) {
            for (i in 0 until buffer.size step 2) {
                // Brown noise: integration of white noise with soft dampening
                val whiteL = (random.nextDouble() * 2.0 - 1.0)
                val whiteR = (random.nextDouble() * 2.0 - 1.0)

                lastOutL = (lastOutL + (0.025 * whiteL)) / 1.025
                lastOutR = (lastOutR + (0.025 * whiteR)) / 1.025

                // Clamp and scale to 16-bit PCM amplitude
                val ampL = (lastOutL * 3.5 * 14000.0).coerceIn(-30000.0, 30000.0).toInt().toShort()
                val ampR = (lastOutR * 3.5 * 14000.0).coerceIn(-30000.0, 30000.0).toInt().toShort()

                buffer[i] = ampL
                buffer[i + 1] = ampR
            }
            track.write(buffer, 0, buffer.size)
        }
    }

    private fun CoroutineScope.streamBinauralBeats(
        track: AudioTrack,
        buffer: ShortArray,
        carrierHz: Double,
        beatHz: Double
    ) {
        var phaseL = 0.0
        var phaseR = 0.0
        val stepL = (2.0 * PI * carrierHz) / SAMPLE_RATE
        val stepR = (2.0 * PI * (carrierHz + beatHz)) / SAMPLE_RATE
        val random = Random()
        var brownL = 0.0
        var brownR = 0.0

        while (isActive) {
            for (i in 0 until buffer.size step 2) {
                // Subtle gentle brown noise bed + pure sine carrier tone
                val whiteL = (random.nextDouble() * 2.0 - 1.0)
                val whiteR = (random.nextDouble() * 2.0 - 1.0)
                brownL = (brownL + (0.015 * whiteL)) / 1.015
                brownR = (brownR + (0.015 * whiteR)) / 1.015

                val sineL = sin(phaseL) * 0.45
                val sineR = sin(phaseR) * 0.45

                val combinedL = (sineL + brownL * 0.55) * 16000.0
                val combinedR = (sineR + brownR * 0.55) * 16000.0

                buffer[i] = combinedL.coerceIn(-30000.0, 30000.0).toInt().toShort()
                buffer[i + 1] = combinedR.coerceIn(-30000.0, 30000.0).toInt().toShort()

                phaseL += stepL
                if (phaseL > 2.0 * PI) phaseL -= 2.0 * PI

                phaseR += stepR
                if (phaseR > 2.0 * PI) phaseR -= 2.0 * PI
            }
            track.write(buffer, 0, buffer.size)
        }
    }

    fun playZenBell() {
        scope.launch {
            try {
                val durationSec = 3.0
                val sampleCount = (SAMPLE_RATE * durationSec).toInt()
                val buffer = ShortArray(sampleCount * 2)

                val fundamental = 432.0 // A = 432Hz Zen tuning
                val harmonic2 = 864.0
                val harmonic3 = 1296.0

                for (i in 0 until sampleCount) {
                    val t = i.toDouble() / SAMPLE_RATE
                    val decay = exp(-t * 1.5)

                    val s1 = sin(2.0 * PI * fundamental * t) * 0.6
                    val s2 = sin(2.0 * PI * harmonic2 * t) * 0.25
                    val s3 = sin(2.0 * PI * harmonic3 * t) * 0.15

                    val sample = ((s1 + s2 + s3) * decay * 22000.0).toInt().toShort()
                    buffer[i * 2] = sample
                    buffer[i * 2 + 1] = sample
                }

                val bellTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                            .build()
                    )
                    .setBufferSizeInBytes(buffer.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                bellTrack.write(buffer, 0, buffer.size)
                bellTrack.play()
            } catch (_: Exception) {}
        }
    }

    fun stop() {
        synthesizerJob?.cancel()
        synthesizerJob = null
        try {
            audioTrack?.pause()
            audioTrack?.flush()
            audioTrack?.stop()
            audioTrack?.release()
        } catch (_: Exception) {}
        audioTrack = null
    }
}
