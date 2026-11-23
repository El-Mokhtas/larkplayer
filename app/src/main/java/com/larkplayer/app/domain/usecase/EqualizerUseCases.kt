package com.larkplayer.app.domain.usecase

import com.larkplayer.app.data.model.EqualizerPresetCustom
import com.larkplayer.app.data.model.EqualizerSettings
import com.larkplayer.app.domain.repository.EqualizerRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetEqualizerSettingsUseCase @Inject constructor(
    private val repository: EqualizerRepository
) {
    operator fun invoke(): Flow<EqualizerSettings> = repository.getSettings()
}

class GetEqualizerSettingsSuspendUseCase @Inject constructor(
    private val repository: EqualizerRepository
) {
    operator fun invoke() = repository.getSettingsSuspend()
}

class SaveEqualizerSettingsUseCase @Inject constructor(
    private val repository: EqualizerRepository
) {
    operator fun invoke(settings: EqualizerSettings) = repository.saveSettings(settings)
}

class GetCustomPresetsUseCase @Inject constructor(
    private val repository: EqualizerRepository
) {
    operator fun invoke(): Flow<List<EqualizerPresetCustom>> = repository.getCustomPresets()
}

class SaveCustomPresetUseCase @Inject constructor(
    private val repository: EqualizerRepository
) {
    operator fun invoke(preset: EqualizerPresetCustom) = repository.savePreset(preset)
}

class DeleteCustomPresetUseCase @Inject constructor(
    private val repository: EqualizerRepository
) {
    operator fun invoke(id: Long) = repository.deletePreset(id)
}