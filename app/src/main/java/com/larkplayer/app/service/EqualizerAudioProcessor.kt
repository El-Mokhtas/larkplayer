package com.larkplayer.app.service

import android.media.audiofx.Equalizer
import android.media.audiofx.Virtualizer
import android.media.audiofx.BassBoost
import androidx.media3.common.audio.AudioProcessor
import com.larkplayer.app.data.model.EqualizerSettings
import com.larkplayer.app.data.model.EqualizerPreset

class EqualizerAudioProcessor : AudioProcessor {

    private var equalizer: Equalizer? = null
    private var virtualizer: Virtualizer? = null
    private var bassBoost: BassBoost? = null
    private var sessionId = 0
    private var currentSettings = EqualizerSettings.DEFAULT

    override fun configure(inputAudioFormat: AudioProcessor.AudioFormat): AudioProcessor.AudioFormat {
        return inputAudioFormat
    }

    override fun isActive(): Boolean = true

    override fun queueInput(inputBuffer: java.nio.ByteBuffer): Int {
        return AudioProcessor.PROCESSING_RESULT_OK
    }

    override fun getOutput(outputBuffer: java.nio.ByteBuffer): Int {
        return AudioProcessor.PROCESSING_RESULT_OK
    }

    override fun flush() {}

    override fun reset() {
        release()
    }

    fun setSessionId(audioSessionId: Int) {
        if (sessionId == audioSessionId) return
        sessionId = audioSessionId
        initializeEffects()
        applySettings(currentSettings)
    }

    private fun initializeEffects() {
        release()

        try {
            equalizer = Equalizer(0, sessionId)
            equalizer?.enabled = true

            virtualizer = Virtualizer(0, sessionId)
            virtualizer?.enabled = true

            bassBoost = BassBoost(0, sessionId)
            bassBoost?.enabled = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun applySettings(settings: EqualizerSettings) {
        currentSettings = settings

        // Apply equalizer bands
        equalizer?.let { eq ->
            val bands = settings.getBandsForPreset()
            val numBands = eq.numberOfBands
            val minLevel = eq.bandLevelRange[0]
            val maxLevel = eq.bandLevelRange[1]

            for (i in 0 until numBands) {
                val gain = if (i < bands.size) bands[i] else 0f
                // Convert from dB to millibel (100 * dB)
                val level = (gain * 100).coerceIn(minLevel, maxLevel).toShort()
                try {
                    eq.setBandLevel((i + 1).toShort(), level)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        // Apply bass boost
        bassBoost?.let { bb ->
            if (settings.bassBoostEnabled) {
                val strength = (settings.bassBoostStrength * 1000).toShort() // 0-1000
                try {
                    bb.strength = strength
                    bb.enabled = true
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                bb.enabled = false
            }
        }

        // Apply virtualizer
        virtualizer?.let { vz ->
            if (settings.virtualizerEnabled) {
                val strength = (settings.virtualizerStrength * 1000).toShort() // 0-1000
                try {
                    vz.strength = strength
                    vz.enabled = true
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                vz.enabled = false
            }
        }
    }

    fun getBandLevels(): IntArray {
        return equalizer?.let { eq ->
            val numBands = eq.numberOfBands
            val levels = IntArray(numBands)
            for (i in 0 until numBands) {
                levels[i] = eq.getBandLevel((i + 1).toShort())
            }
            levels
        } ?: intArrayOf()
    }

    private fun release() {
        equalizer?.release()
        equalizer = null
        virtualizer?.release()
        virtualizer = null
        bassBoost?.release()
        bassBoost = null
    }

    fun releaseEffects() {
        release()
    }
}