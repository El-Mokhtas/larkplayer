package com.larkplayer.app.domain.repository

import com.larkplayer.app.data.model.EqualizerPresetCustom
import com.larkplayer.app.data.model.EqualizerSettings
import kotlinx.coroutines.flow.Flow

interface EqualizerRepository {

    fun getSettings(): Flow<EqualizerSettings>

    suspend fun getSettingsSuspend(): EqualizerSettings

    suspend fun saveSettings(settings: EqualizerSettings)

    fun getCustomPresets(): Flow<List<EqualizerPresetCustom>>

    suspend fun savePreset(preset: EqualizerPresetCustom)

    suspend fun deletePreset(id: Long)
}