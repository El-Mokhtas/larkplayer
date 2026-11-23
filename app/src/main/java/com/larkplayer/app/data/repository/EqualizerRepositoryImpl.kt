package com.larkplayer.app.data.repository

import com.larkplayer.app.data.model.EqualizerPresetCustom
import com.larkplayer.app.data.model.EqualizerSettings
import com.larkplayer.app.data.source.local.AppDatabase
import com.larkplayer.app.data.source.local.EqualizerDao
import com.larkplayer.app.domain.repository.EqualizerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class EqualizerRepositoryImpl @Inject constructor(
    private val database: AppDatabase
) : EqualizerRepository {

    private val equalizerDao: EqualizerDao = database.equalizerDao()

    override fun getSettings(): Flow<EqualizerSettings> = equalizerDao.getSettings()
        .map { it ?: EqualizerSettings.DEFAULT }

    override suspend fun getSettingsSuspend(): EqualizerSettings = withContext(Dispatchers.IO) {
        equalizerDao.getSettingsSuspend() ?: EqualizerSettings.DEFAULT
    }

    override suspend fun saveSettings(settings: EqualizerSettings) = withContext(Dispatchers.IO) {
        equalizerDao.insertSettings(settings.copy(updatedAt = System.currentTimeMillis()))
    }

    override fun getCustomPresets(): Flow<List<EqualizerPresetCustom>> = equalizerDao.getCustomPresetsFlow()

    override suspend fun savePreset(preset: EqualizerPresetCustom) = withContext(Dispatchers.IO) {
        equalizerDao.insertPreset(preset)
    }

    override suspend fun deletePreset(id: Long) = withContext(Dispatchers.IO) {
        equalizerDao.deletePresetById(id)
    }
}