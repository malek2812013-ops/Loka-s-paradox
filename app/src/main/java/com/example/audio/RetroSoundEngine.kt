package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

enum class BgmTrack {
    NONE,
    INTRO_STORY,
    RUINS,
    ASHEN,
    WORLD_NORMAL,
    WORLD_OUTLANDS,
    WORLD_SPLIT,
    GLITCHED_VOID,
    BATTLE,
    BATTLE_DUMMY,
    BATTLE_OUTLANDS,
    BATTLE_SPLIT,
    BATTLE_GLITCH,
    BATTLE_FUTURE,
    FINALE
}

/**
 * Zero-asset PCM retro synthesizer providing:
 * - Distinct 8-bit chiptune background music per zone & per boss tier
 * - Breathy whispering synthesis when Loka's past fragments flash in Glitched Void
 * - Retro SFX (typewriter blips, save/spare/chest/heal chimes, glitch noise, soul switches, strikes, hits)
 */
object RetroSoundEngine {
    private const val SAMPLE_RATE = 22050
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile
    var isMuted: Boolean = false
        set(value) {
            field = value
            if (value) stopBgmInternal() else startBgmIfNeeded()
        }

    @Volatile
    private var currentTrack: BgmTrack = BgmTrack.NONE

    private var bgmJob: Job? = null

    fun setMusicTrack(track: BgmTrack) {
        if (currentTrack == track) return
        currentTrack = track
        if (!isMuted) {
            restartBgm()
        }
    }

    private fun startBgmIfNeeded() {
        if (currentTrack != BgmTrack.NONE && bgmJob == null) {
            restartBgm()
        }
    }

    private fun stopBgmInternal() {
        bgmJob?.cancel()
        bgmJob = null
    }

    private fun restartBgm() {
        bgmJob?.cancel()
        val target = currentTrack
        if (target == BgmTrack.NONE || isMuted) {
            bgmJob = null
            return
        }
        bgmJob = scope.launch {
            runBgmLoop(target)
        }
    }

    private fun isBattleTrack(t: BgmTrack): Boolean = t == BgmTrack.BATTLE ||
        t == BgmTrack.BATTLE_DUMMY ||
        t == BgmTrack.BATTLE_OUTLANDS ||
        t == BgmTrack.BATTLE_SPLIT ||
        t == BgmTrack.BATTLE_GLITCH ||
        t == BgmTrack.BATTLE_FUTURE

    private suspend fun runBgmLoop(trackType: BgmTrack) {
        var streamTrack: AudioTrack? = null
        try {
            val minBuf = AudioTrack.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(2048)

            streamTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setTransferMode(AudioTrack.MODE_STREAM)
                .setBufferSizeInBytes(minBuf * 2)
                .build()

            streamTrack.play()

            val melody = notesForTrack(trackType)
            val stepMs = stepDurationMs(trackType)
            val samplesPerStep = (SAMPLE_RATE * stepMs) / 1000
            val chunk = ShortArray(samplesPerStep)
            var noteIdx = 0
            var phase = 0.0
            var harmonyPhase = 0.0
            var bassPhase = 0.0
            val battleMode = isBattleTrack(trackType)

            while (kotlinx.coroutines.currentCoroutineContext().isActive && !isMuted && currentTrack == trackType) {
                val freq = melody[noteIdx % melody.size]
                val bassFreq = when {
                    trackType == BgmTrack.GLITCHED_VOID -> 65.4f
                    trackType == BgmTrack.BATTLE_FUTURE && noteIdx % 2 == 1 -> freq * 0.25f
                    else -> freq * 0.5f
                }
                val harmFreq = if (battleMode && freq > 0f) freq * 1.498f else 0f // crisp chiptune 5th harmony
                val hasDrumKick = battleMode && (noteIdx % 4 == 0)
                val hasSnare = battleMode && (noteIdx % 4 == 2)

                for (i in 0 until samplesPerStep) {
                    val t = i.toFloat() / samplesPerStep
                    val env = if (trackType == BgmTrack.GLITCHED_VOID) {
                        (1f - t * 0.65f) * 0.55f
                    } else {
                        (1f - t * 0.68f)
                    }

                    val mainWave = if (freq > 0f) {
                        phase += 2.0 * PI * freq / SAMPLE_RATE
                        when (trackType) {
                            BgmTrack.GLITCHED_VOID -> sin(phase).toFloat() * 0.75f + sin(phase * 1.015).toFloat() * 0.25f
                            BgmTrack.INTRO_STORY, BgmTrack.RUINS -> {
                                (2.0 / PI * kotlin.math.asin(sin(phase))).toFloat()
                            }
                            else -> {
                                if (sin(phase) >= 0.0) 0.72f else -0.72f
                            }
                        }
                    } else 0f

                    val harmWave = if (harmFreq > 0f) {
                        harmonyPhase += 2.0 * PI * harmFreq / SAMPLE_RATE
                        (2.0 / PI * kotlin.math.asin(sin(harmonyPhase))).toFloat() * 0.28f
                    } else 0f

                    bassPhase += 2.0 * PI * bassFreq / SAMPLE_RATE
                    val bassWave = sin(bassPhase).toFloat() * (if (trackType == BgmTrack.GLITCHED_VOID) 0.45f else 0.28f)

                    val drumWave = when {
                        hasDrumKick && t < 0.28f -> sin(2.0 * PI * (120f - t * 240f) * i / SAMPLE_RATE).toFloat() * (1f - t * 3.2f) * 0.32f
                        hasSnare && t < 0.22f -> (Random.nextFloat() * 2f - 1f) * (1f - t * 4.2f) * 0.16f
                        else -> 0f
                    }

                    val vol = when {
                        trackType == BgmTrack.GLITCHED_VOID -> 0.065f
                        trackType == BgmTrack.BATTLE_FUTURE -> 0.090f
                        battleMode -> 0.082f
                        else -> 0.070f
                    }
                    val mixed = (mainWave * env + harmWave * env + bassWave + drumWave) * vol
                    chunk[i] = (mixed * Short.MAX_VALUE).toInt()
                        .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                        .toShort()
                }
                streamTrack.write(chunk, 0, chunk.size)
                noteIdx++
            }
        } catch (_: Throwable) {
            delay(200)
        } finally {
            try {
                streamTrack?.stop()
                streamTrack?.release()
            } catch (_: Throwable) {
            }
        }
    }

    private fun stepDurationMs(track: BgmTrack): Int = when (track) {
        BgmTrack.INTRO_STORY -> 380
        BgmTrack.RUINS -> 300
        BgmTrack.ASHEN -> 340
        BgmTrack.GLITCHED_VOID -> 520
        BgmTrack.BATTLE_DUMMY -> 220
        BgmTrack.BATTLE_OUTLANDS -> 165
        BgmTrack.BATTLE_SPLIT -> 150
        BgmTrack.BATTLE_GLITCH -> 145
        BgmTrack.BATTLE_FUTURE -> 125
        BgmTrack.BATTLE -> 165
        BgmTrack.FINALE -> 250
        else -> 250
    }

    private fun notesForTrack(track: BgmTrack): FloatArray = when (track) {
        BgmTrack.INTRO_STORY -> floatArrayOf(
            261.6f, 392.0f, 329.6f, 293.7f, 261.6f, 246.9f, 261.6f, 0f,
            293.7f, 440.0f, 392.0f, 329.6f, 293.7f, 261.6f, 246.9f, 0f
        )
        BgmTrack.RUINS -> floatArrayOf(
            329.6f, 392.0f, 493.9f, 392.0f, 329.6f, 440.0f, 392.0f, 329.6f,
            293.7f, 349.2f, 440.0f, 349.2f, 293.7f, 392.0f, 329.6f, 0f
        )
        BgmTrack.ASHEN -> floatArrayOf(
            220.0f, 207.7f, 196.0f, 0f, 233.1f, 220.0f, 185.0f, 0f
        )
        BgmTrack.WORLD_NORMAL -> floatArrayOf(
            392.0f, 440.0f, 493.9f, 587.3f, 523.3f, 493.9f, 440.0f, 392.0f
        )
        BgmTrack.WORLD_OUTLANDS -> floatArrayOf(
            293.7f, 349.2f, 415.3f, 440.0f, 415.3f, 349.2f, 329.6f, 293.7f
        )
        BgmTrack.WORLD_SPLIT -> floatArrayOf(
            329.6f, 392.0f, 493.9f, 659.3f, 466.2f, 392.0f, 311.1f, 293.7f
        )
        BgmTrack.GLITCHED_VOID -> floatArrayOf(
            196.0f, 0f, 207.7f, 0f, 174.6f, 0f, 0f, 155.6f, 0f, 164.8f, 0f
        )
        BgmTrack.BATTLE_DUMMY -> floatArrayOf(
            392.0f, 493.9f, 523.3f, 587.3f, 523.3f, 493.9f, 440.0f, 392.0f
        )
        BgmTrack.BATTLE_OUTLANDS, BgmTrack.BATTLE -> floatArrayOf(
            440.0f, 523.3f, 587.3f, 659.3f, 587.3f, 523.3f, 493.9f, 440.0f,
            392.0f, 493.9f, 587.3f, 659.3f, 784.0f, 659.3f, 587.3f, 523.3f
        )
        BgmTrack.BATTLE_SPLIT -> floatArrayOf(
            329.6f, 493.9f, 659.3f, 622.3f, 587.3f, 523.3f, 493.9f, 392.0f,
            349.2f, 523.3f, 698.5f, 659.3f, 587.3f, 523.3f, 493.9f, 440.0f
        )
        BgmTrack.BATTLE_GLITCH -> floatArrayOf(
            311.1f, 370.0f, 293.7f, 466.2f, 277.2f, 415.3f, 261.6f, 392.0f
        )
        BgmTrack.BATTLE_FUTURE -> floatArrayOf(
            293.7f, 293.7f, 587.3f, 440.0f, 0f, 415.3f, 392.0f, 349.2f,
            293.7f, 349.2f, 392.0f, 587.3f, 523.3f, 493.9f, 440.0f, 392.0f
        )
        BgmTrack.FINALE -> floatArrayOf(
            349.2f, 440.0f, 523.3f, 698.5f, 659.3f, 587.3f, 523.3f, 440.0f
        )
        BgmTrack.NONE -> floatArrayOf(0f)
    }

    /**
     * Synthesizes creepy breathy whispering bursts ("shhh... loka...") when fragments of Loka's past flash.
     */
    fun playWhisper() {
        if (isMuted) return
        scope.launch {
            val durationMs = 760
            val count = (SAMPLE_RATE * durationMs) / 1000
            val samples = ShortArray(count)
            var lp = 0f
            var prev = 0f
            for (i in 0 until count) {
                val t = i.toFloat() / count
                val breath1 = sin((t * 2.2 * PI).coerceAtMost(PI)).toFloat().coerceAtLeast(0f)
                val breath2 = if (t > 0.42f) sin(((t - 0.42f) / 0.58f) * PI).toFloat().coerceAtLeast(0f) else 0f
                val env = (breath1 * 0.65f + breath2 * 0.85f)
                val white = Random.nextFloat() * 2f - 1f
                lp += 0.28f * (white - lp)
                val hp = lp - prev * 0.72f
                prev = lp
                val eerieTone = sin(2.0 * PI * 310.0 * i / SAMPLE_RATE).toFloat() * 0.08f
                val mixed = (hp * 0.85f + eerieTone) * env * 0.24f
                samples[i] = (mixed * Short.MAX_VALUE).toInt()
                    .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                    .toShort()
            }
            writeAudio(samples)
        }
    }

    fun playTypeBlip(pitchHz: Float = 360f) {
        if (isMuted) return
        playTone(freqStart = pitchHz, freqEnd = pitchHz * 0.94f, durationMs = 26, volume = 0.13f, squareWave = true)
    }

    fun playGlitchNoise(durationMs: Int = 140) {
        if (isMuted) return
        scope.launch {
            val count = (SAMPLE_RATE * durationMs) / 1000
            val samples = ShortArray(count)
            for (i in 0 until count) {
                val env = 1f - (i.toFloat() / count)
                val noise = (Random.nextFloat() * 2f - 1f)
                val buzz = sin(2.0 * PI * (120 + (i % 7) * 65) * i / SAMPLE_RATE).toFloat()
                val mixed = (noise * 0.65f + buzz * 0.35f) * env * 0.22f
                samples[i] = (mixed * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            writeAudio(samples)
        }
    }

    fun playSoulSwitch(index: Int) {
        if (isMuted) return
        val base = 420f + index * 90f
        playTone(freqStart = base, freqEnd = base * 1.45f, durationMs = 85, volume = 0.18f, squareWave = false)
    }

    fun playAbility() {
        if (isMuted) return
        playTone(freqStart = 540f, freqEnd = 920f, durationMs = 110, volume = 0.18f, squareWave = false)
    }

    fun playSaveChime() {
        if (isMuted) return
        scope.launch {
            playToneSync(523.3f, 523.3f, 70, 0.16f, false)
            playToneSync(659.3f, 659.3f, 70, 0.16f, false)
            playToneSync(784.0f, 1046.5f, 130, 0.18f, false)
        }
    }

    fun playChestOpen() {
        if (isMuted) return
        scope.launch {
            playToneSync(440f, 554.4f, 65, 0.17f, true)
            playToneSync(659.3f, 880f, 130, 0.19f, false)
        }
    }

    fun playHealChime() {
        if (isMuted) return
        scope.launch {
            playToneSync(493.9f, 587.3f, 65, 0.15f, false)
            playToneSync(659.3f, 784.0f, 110, 0.17f, false)
        }
    }

    fun playSpareChime() {
        if (isMuted) return
        scope.launch {
            playToneSync(523.3f, 659.3f, 80, 0.17f, false)
            playToneSync(784.0f, 1046.5f, 160, 0.19f, false)
        }
    }

    fun playStrike(critical: Boolean) {
        if (isMuted) return
        if (critical) {
            playTone(freqStart = 680f, freqEnd = 1180f, durationMs = 120, volume = 0.22f, squareWave = true)
        } else {
            playTone(freqStart = 420f, freqEnd = 210f, durationMs = 90, volume = 0.18f, squareWave = true)
        }
    }

    fun playHurt() {
        if (isMuted) return
        playTone(freqStart = 210f, freqEnd = 75f, durationMs = 140, volume = 0.25f, squareWave = true)
    }

    private fun playToneSync(
        freqStart: Float,
        freqEnd: Float,
        durationMs: Int,
        volume: Float,
        squareWave: Boolean
    ) {
        val count = (SAMPLE_RATE * durationMs) / 1000
        if (count <= 0) return
        val samples = ShortArray(count)
        var phase = 0.0
        for (i in 0 until count) {
            val t = i.toFloat() / count
            val freq = freqStart + (freqEnd - freqStart) * t
            val env = (1f - t)
            phase += 2.0 * PI * freq / SAMPLE_RATE
            val raw = if (squareWave) {
                if (sin(phase) >= 0.0) 1f else -1f
            } else {
                sin(phase).toFloat()
            }
            val valFloat = raw * env * volume
            samples[i] = (valFloat * Short.MAX_VALUE).toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                .toShort()
        }
        writeAudio(samples)
    }

    private fun playTone(
        freqStart: Float,
        freqEnd: Float,
        durationMs: Int,
        volume: Float,
        squareWave: Boolean
    ) {
        scope.launch {
            val count = (SAMPLE_RATE * durationMs) / 1000
            if (count <= 0) return@launch
            val samples = ShortArray(count)
            var phase = 0.0
            for (i in 0 until count) {
                val t = i.toFloat() / count
                val freq = freqStart + (freqEnd - freqStart) * t
                val env = (1f - t)
                phase += 2.0 * PI * freq / SAMPLE_RATE
                val raw = if (squareWave) {
                    if (sin(phase) >= 0.0) 1f else -1f
                } else {
                    sin(phase).toFloat()
                }
                val valFloat = raw * env * volume
                samples[i] = (valFloat * Short.MAX_VALUE).toInt()
                    .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                    .toShort()
            }
            writeAudio(samples)
        }
    }

    private fun writeAudio(samples: ShortArray) {
        try {
            val minBuffer = AudioTrack.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val bufferBytes = maxOf(minBuffer, samples.size * 2)
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setTransferMode(AudioTrack.MODE_STATIC)
                .setBufferSizeInBytes(bufferBytes)
                .build()

            track.write(samples, 0, samples.size)
            track.play()
            Thread.sleep((samples.size * 1000L / SAMPLE_RATE) + 30L)
            track.stop()
            track.release()
        } catch (_: Throwable) {
            // Audio unavailable on headless environment or busy device; ignore safely.
        }
    }
}
