package com.example.game.engine

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

class AudioEngine {

    var isMuted: Boolean = false
    var bgmVolume: Float = 0.6f
    var sfxVolume: Float = 0.8f

    private val sampleRate = 22050
    private val scope = CoroutineScope(Dispatchers.Default)
    private var bgmJob: Job? = null
    private var currentBgmTheme: String = ""

    fun startBgm(theme: String) {
        if (currentBgmTheme == theme && bgmJob?.isActive == true) return
        currentBgmTheme = theme
        bgmJob?.cancel()

        bgmJob = scope.launch {
            while (isActive && !isMuted && bgmVolume > 0.01f) {
                when (theme) {
                    "village" -> playVillageChords()
                    "forest" -> playForestMelody()
                    "dungeon" -> playDungeonDrone()
                    "boss" -> playBossTheme()
                    else -> playVillageChords()
                }
            }
        }
    }

    fun stopBgm() {
        bgmJob?.cancel()
        currentBgmTheme = ""
    }

    // SFX Methods
    fun playSlash() {
        if (isMuted || sfxVolume <= 0.01f) return
        scope.launch {
            // White noise sweep with downward frequency
            val numSamples = (sampleRate * 0.12f).toInt()
            val buffer = ShortArray(numSamples)
            for (i in 0 until numSamples) {
                val t = i.toFloat() / numSamples
                val freq = 800f - (t * 500f)
                val amp = (1f - t) * sfxVolume * 22000f
                val noise = (Math.random() * 2.0 - 1.0).toFloat() * 0.3f
                val wave = sin(2 * PI * freq * i / sampleRate).toFloat() * 0.7f
                buffer[i] = ((wave + noise) * amp).toInt().coerceIn(-32767, 32767).toShort()
            }
            playPcmBuffer(buffer)
        }
    }

    fun playHeavyHit() {
        if (isMuted || sfxVolume <= 0.01f) return
        scope.launch {
            val numSamples = (sampleRate * 0.25f).toInt()
            val buffer = ShortArray(numSamples)
            for (i in 0 until numSamples) {
                val t = i.toFloat() / numSamples
                val freq = 160f * (1f - (t * 0.7f))
                val amp = (1f - t * t) * sfxVolume * 28000f
                val noise = (Math.random() * 2.0 - 1.0).toFloat() * 0.5f * (1f - t)
                val wave = sin(2 * PI * freq * i / sampleRate).toFloat() * 0.8f
                buffer[i] = ((wave + noise) * amp).toInt().coerceIn(-32767, 32767).toShort()
            }
            playPcmBuffer(buffer)
        }
    }

    fun playParry() {
        if (isMuted || sfxVolume <= 0.01f) return
        scope.launch {
            val numSamples = (sampleRate * 0.35f).toInt()
            val buffer = ShortArray(numSamples)
            val baseFreq = 1250f
            for (i in 0 until numSamples) {
                val t = i.toFloat() / numSamples
                val amp = kotlin.math.exp(-t * 8f) * sfxVolume * 25000f
                val wave = sin(2 * PI * baseFreq * i / sampleRate).toFloat() + 0.5f * sin(2 * PI * (baseFreq * 1.5f) * i / sampleRate).toFloat()
                buffer[i] = (wave * amp).toInt().coerceIn(-32767, 32767).toShort()
            }
            playPcmBuffer(buffer)
        }
    }

    fun playBlock() {
        if (isMuted || sfxVolume <= 0.01f) return
        scope.launch {
            val numSamples = (sampleRate * 0.15f).toInt()
            val buffer = ShortArray(numSamples)
            for (i in 0 until numSamples) {
                val t = i.toFloat() / numSamples
                val amp = (1f - t) * sfxVolume * 20000f
                val wave = sin(2 * PI * 220f * i / sampleRate).toFloat()
                buffer[i] = (wave * amp).toInt().coerceIn(-32767, 32767).toShort()
            }
            playPcmBuffer(buffer)
        }
    }

    fun playDash() {
        if (isMuted || sfxVolume <= 0.01f) return
        scope.launch {
            val numSamples = (sampleRate * 0.14f).toInt()
            val buffer = ShortArray(numSamples)
            for (i in 0 until numSamples) {
                val t = i.toFloat() / numSamples
                val amp = (sin(t * PI).toFloat()) * sfxVolume * 16000f
                val noise = (Math.random() * 2.0 - 1.0).toFloat()
                buffer[i] = (noise * amp).toInt().coerceIn(-32767, 32767).toShort()
            }
            playPcmBuffer(buffer)
        }
    }

    fun playMagicSpell() {
        if (isMuted || sfxVolume <= 0.01f) return
        scope.launch {
            val numSamples = (sampleRate * 0.28f).toInt()
            val buffer = ShortArray(numSamples)
            for (i in 0 until numSamples) {
                val t = i.toFloat() / numSamples
                val freq = 400f + (sin(t * 12 * PI).toFloat() * 120f) + (t * 300f)
                val amp = (1f - t) * sfxVolume * 22000f
                val wave = sin(2 * PI * freq * i / sampleRate).toFloat()
                buffer[i] = (wave * amp).toInt().coerceIn(-32767, 32767).toShort()
            }
            playPcmBuffer(buffer)
        }
    }

    fun playPotion() {
        if (isMuted || sfxVolume <= 0.01f) return
        scope.launch {
            val numSamples = (sampleRate * 0.22f).toInt()
            val buffer = ShortArray(numSamples)
            for (i in 0 until numSamples) {
                val t = i.toFloat() / numSamples
                val freq = 520f + (sin(t * 20 * PI).toFloat() * 180f)
                val amp = (1f - t) * sfxVolume * 18000f
                val wave = sin(2 * PI * freq * i / sampleRate).toFloat()
                buffer[i] = (wave * amp).toInt().coerceIn(-32767, 32767).toShort()
            }
            playPcmBuffer(buffer)
        }
    }

    fun playCoin() {
        if (isMuted || sfxVolume <= 0.01f) return
        scope.launch {
            val numSamples = (sampleRate * 0.16f).toInt()
            val buffer = ShortArray(numSamples)
            for (i in 0 until numSamples) {
                val t = i.toFloat() / numSamples
                val freq = if (t < 0.5f) 987.77f else 1318.51f // B5 to E6 chime
                val amp = (1f - t) * sfxVolume * 20000f
                val wave = sin(2 * PI * freq * i / sampleRate).toFloat()
                buffer[i] = (wave * amp).toInt().coerceIn(-32767, 32767).toShort()
            }
            playPcmBuffer(buffer)
        }
    }

    fun playLevelUp() {
        if (isMuted || sfxVolume <= 0.01f) return
        scope.launch {
            val notes = floatArrayOf(440f, 554.37f, 659.25f, 880f) // A major fanfare
            val noteDuration = (sampleRate * 0.12f).toInt()
            val buffer = ShortArray(noteDuration * notes.size)
            for (n in notes.indices) {
                val freq = notes[n]
                val offset = n * noteDuration
                for (i in 0 until noteDuration) {
                    val t = i.toFloat() / noteDuration
                    val amp = (1f - (t * 0.4f)) * sfxVolume * 22000f
                    val wave = sin(2 * PI * freq * i / sampleRate).toFloat()
                    buffer[offset + i] = (wave * amp).toInt().coerceIn(-32767, 32767).toShort()
                }
            }
            playPcmBuffer(buffer)
        }
    }

    fun playChest() {
        if (isMuted || sfxVolume <= 0.01f) return
        scope.launch {
            val notes = floatArrayOf(330f, 392f, 523.25f, 659.25f)
            val noteDur = (sampleRate * 0.09f).toInt()
            val buffer = ShortArray(noteDur * notes.size)
            for (n in notes.indices) {
                val freq = notes[n]
                val offset = n * noteDur
                for (i in 0 until noteDur) {
                    val t = i.toFloat() / noteDur
                    val amp = (1f - t) * sfxVolume * 18000f
                    val wave = sin(2 * PI * freq * i / sampleRate).toFloat()
                    buffer[offset + i] = (wave * amp).toInt().coerceIn(-32767, 32767).toShort()
                }
            }
            playPcmBuffer(buffer)
        }
    }

    fun playUiClick() {
        if (isMuted || sfxVolume <= 0.01f) return
        scope.launch {
            val numSamples = (sampleRate * 0.04f).toInt()
            val buffer = ShortArray(numSamples)
            for (i in 0 until numSamples) {
                val t = i.toFloat() / numSamples
                val amp = (1f - t) * sfxVolume * 15000f
                val wave = sin(2 * PI * 1200f * i / sampleRate).toFloat()
                buffer[i] = (wave * amp).toInt().coerceIn(-32767, 32767).toShort()
            }
            playPcmBuffer(buffer)
        }
    }

    // Procedural Music Loops
    private suspend fun playVillageChords() {
        // Warm medieval folk arpeggios in D minor: D, F, A, G, E, C
        val melody = listOf(
            293.66f to 300L, 349.23f to 300L, 440.00f to 300L, 392.00f to 300L,
            329.63f to 300L, 261.63f to 300L, 293.66f to 600L
        )
        playNoteSequence(melody, bgmVolume * 0.4f)
    }

    private suspend fun playForestMelody() {
        // Mysterious woodland theme in E minor
        val melody = listOf(
            329.63f to 350L, 392.00f to 350L, 493.88f to 350L, 440.00f to 350L,
            349.23f to 350L, 329.63f to 700L
        )
        playNoteSequence(melody, bgmVolume * 0.4f)
    }

    private suspend fun playDungeonDrone() {
        // Ominous low bass pulses with dark harmonics
        val melody = listOf(
            110.00f to 500L, 116.54f to 500L, 110.00f to 500L, 98.00f to 500L,
            130.81f to 500L, 123.47f to 500L
        )
        playNoteSequence(melody, bgmVolume * 0.45f)
    }

    private suspend fun playBossTheme() {
        // Rapid epic battle sequence in C minor
        val melody = listOf(
            130.81f to 150L, 261.63f to 150L, 311.13f to 150L, 261.63f to 150L,
            349.23f to 150L, 311.13f to 150L, 392.00f to 150L, 349.23f to 150L,
            415.30f to 150L, 392.00f to 150L, 311.13f to 150L, 261.63f to 150L
        )
        playNoteSequence(melody, bgmVolume * 0.55f)
    }

    private suspend fun playNoteSequence(notes: List<Pair<Float, Long>>, volume: Float) {
        for ((freq, durationMs) in notes) {
            if (!scope.isActive || isMuted || volume <= 0.001f) break
            val numSamples = (sampleRate * (durationMs / 1000f)).toInt()
            val buffer = ShortArray(numSamples)
            for (i in 0 until numSamples) {
                val t = i.toFloat() / numSamples
                // gentle envelope (attack & decay)
                val env = (sin(t * PI)).toFloat()
                val wave = sin(2 * PI * freq * i / sampleRate).toFloat() + 0.3f * sin(4 * PI * freq * i / sampleRate).toFloat()
                buffer[i] = (wave * env * volume * 20000f).toInt().coerceIn(-32767, 32767).toShort()
            }
            playPcmBuffer(buffer)
            delay(durationMs)
        }
    }

    private fun playPcmBuffer(buffer: ShortArray) {
        try {
            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
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
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(buffer, 0, buffer.size)
            audioTrack.play()
            scope.launch {
                delay((buffer.size * 1000L / sampleRate) + 50L)
                try {
                    audioTrack.stop()
                    audioTrack.release()
                } catch (_: Exception) {}
            }
        } catch (_: Exception) {}
    }
}
