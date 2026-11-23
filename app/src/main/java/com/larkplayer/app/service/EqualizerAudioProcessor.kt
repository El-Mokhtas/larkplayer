package com.larkplayer.app.service

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.Virtualizer
import androidx.media3.common.audio.AudioProcessor
import com.larkplayer.app.data.model.EqualizerSettings
import java.nio.ByteBuffer
import java.nio.ByteOrder

class EqualizerAudioProcessor : AudioProcessor {

    private var equalizer: Equalizer? = null
    private var virtualizer: Virtualizer? = null
    private var bassBoost: BassBoost? = null
    private var sessionId = 0
    private var currentSettings = EqualizerSettings.DEFAULT
    private var inputEnded = false
    private var outputBuffer: ByteBuffer = AudioProcessor.EMPTY_BUFFER

    override fun configure(inputAudioFormat: AudioProcessor.AudioFormat): AudioProcessor.AudioFormat {
        return inputAudioFormat
    }

    override fun isActive(): Boolean = false

    override fun queueInput(inputBuffer: ByteBuffer) {
        // No audio transformation; effects are applied via Android audiofx on sessionId.
    }

    override fun getOutput(): ByteBuffer {
        val output = outputBuffer
        outputBuffer = AudioProcessor.EMPTY_BUFFER
        return output
    }

    override fun isEnded(): Boolean = inputEnded

    override fun flush() {
        outputBuffer = AudioProcessor.EMPTY_BUFFER
        inputEnded = false
    }

    override fun queueEndOfStream() {
        inputEnded = true
    }

    override fun reset() {
        flush()
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
            equalizer = Equalizer(0, sessionId).apply { enabled = true }
            virtualizer = Virtualizer(0, sessionId).apply { enabled = true }
            bassBoost = BassBoost(0, sessionId).apply { enabled = true }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun applySettings(settings: EqualizerSettings) {
        currentSettings = settings
        equalizer?.let { eq ->
            val bands = settings.getBandsForPreset()
            val numBands = eq.numberOfBands.toInt()
            val minLevel = eq.bandLevelRange[0].toInt()
            val maxLevel = eq.bandLevelRange[1].toInt()
            for (i in 0 until numBands) {
                val gain = if (i < bands.size) bands[i] else 0f
                val level = (gain * 100).toInt().coerceIn(minLevel, maxLevel).toShort()
                try {
                    eq.setBandLevel(i.toShort(), level)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        bassBoost?.let { bb ->
            if (settings.bassBoostEnabled) {
                try {
                    bb.strength = (settings.bassBoostStrength * 1000).toInt().coerceIn(0, 1000).toShort()
                    bb.enabled = true
                } catch (e: Exception) { e.printStackTrace() }
            } else bb.enabled = false
        }
        virtualizer?.let { vz ->
            if (settings.virtualizerEnabled) {
                try {
                    vz.strength = (settings.virtualizerStrength * 1000).toInt().coerceIn(0, 1000).toShort()
                    vz.enabled = true
                } catch (e: Exception) { e.printStackTrace() }
            } else vz.enabled = false
        }
    }

    fun getBandLevels(): IntArray {
        return equalizer?.let { eq ->
            val numBands = eq.numberOfBands.toInt()
            IntArray(numBands) { i -> eq.getBandLevel(i.toShort()).toInt() }
        } ?: intArrayOf()
    }

    private fun release() {
        equalizer?.release(); equalizer = null
        virtualizer?.release(); virtualizer = null
        bassBoost?.release(); bassBoost = null
    }

    fun releaseEffects() { release() }
}