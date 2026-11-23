package com.larkplayer.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.larkplayer.app.data.model.EqualizerPreset
import com.larkplayer.app.data.model.EqualizerPresetCustom
import com.larkplayer.app.data.model.EqualizerSettings
import com.larkplayer.app.domain.usecase.DeleteCustomPresetUseCase
import com.larkplayer.app.domain.usecase.GetCustomPresetsUseCase
import com.larkplayer.app.domain.usecase.GetEqualizerSettingsSuspendUseCase
import com.larkplayer.app.domain.usecase.GetEqualizerSettingsUseCase
import com.larkplayer.app.domain.usecase.SaveCustomPresetUseCase
import com.larkplayer.app.domain.usecase.SaveEqualizerSettingsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel

@HiltViewModel
class EqualizerViewModel @Inject constructor(
    private val getEqualizerSettingsUseCase: GetEqualizerSettingsUseCase,
    private val getEqualizerSettingsSuspendUseCase: GetEqualizerSettingsSuspendUseCase,
    private val saveEqualizerSettingsUseCase: SaveEqualizerSettingsUseCase,
    private val getCustomPresetsUseCase: GetCustomPresetsUseCase,
    private val saveCustomPresetUseCase: SaveCustomPresetUseCase,
    private val deleteCustomPresetUseCase: DeleteCustomPresetUseCase
) : ViewModel() {

    private val _settings = MutableStateFlow<EqualizerSettings>(EqualizerSettings.DEFAULT)
    val settings: StateFlow<EqualizerSettings> = _settings

    private val _customPresets = MutableStateFlow<List<EqualizerPresetCustom>>(emptyList())
    val customPresets: StateFlow<List<EqualizerPresetCustom>> = _customPresets

    private val _selectedPreset = MutableStateFlow<EqualizerPreset>(EqualizerPreset.NORMAL)
    val selectedPreset: StateFlow<EqualizerPreset> = _selectedPreset

    private val _isEditingCustom = MutableStateFlow(false)
    val isEditingCustom: StateFlow<Boolean> = _isEditingCustom

    init {
        loadSettings()
        loadCustomPresets()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val currentSettings = getEqualizerSettingsSuspendUseCase()
            _settings.value = currentSettings
            _selectedPreset.value = currentSettings.preset
        }
    }

    private fun loadCustomPresets() {
        viewModelScope.launch {
            // In real implementation, collect from flow
            // getCustomPresetsUseCase().collect { _customPresets.value = it }
        }
    }

    fun onPresetSelected(preset: EqualizerPreset) {
        _selectedPreset.value = preset
        val newSettings = _settings.value.copy(preset = preset)
        if (preset != EqualizerPreset.CUSTOM) {
            _settings.value = newSettings
        }
        saveSettings(newSettings)
    }

    fun onBandChanged(bandIndex: Int, gain: Float) {
        val currentBands = _settings.value.getBandsForPreset().toMutableList()
        if (bandIndex < currentBands.size) {
            currentBands[bandIndex] = gain.coerceIn(-12f, 12f)
            val newSettings = _settings.value.copyWithUpdatedBands(currentBands)
            _settings.value = newSettings
            _selectedPreset.value = EqualizerPreset.CUSTOM
            _isEditingCustom.value = true
            saveSettings(newSettings)
        }
    }

    fun onBassBoostChanged(enabled: Boolean, strength: Float = 0f) {
        val newSettings = _settings.value.copy(
            bassBoostEnabled = enabled,
            bassBoostStrength = strength.coerceIn(0f, 1f),
            preset = EqualizerPreset.CUSTOM
        )
        _settings.value = newSettings
        _selectedPreset.value = EqualizerPreset.CUSTOM
        _isEditingCustom.value = true
        saveSettings(newSettings)
    }

    fun onVirtualizerChanged(enabled: Boolean, strength: Float = 0f) {
        val newSettings = _settings.value.copy(
            virtualizerEnabled = enabled,
            virtualizerStrength = strength.coerceIn(0f, 1f),
            preset = EqualizerPreset.CUSTOM
        )
        _settings.value = newSettings
        _selectedPreset.value = EqualizerPreset.CUSTOM
        _isEditingCustom.value = true
        saveSettings(newSettings)
    }

    fun onVolumeBoostChanged(enabled: Boolean, level: Float = 1.0f) {
        val newSettings = _settings.value.copy(
            volumeBoostEnabled = enabled,
            volumeBoostLevel = level.coerceIn(1.0f, 3.0f),
            preset = EqualizerPreset.CUSTOM
        )
        _settings.value = newSettings
        _selectedPreset.value = EqualizerPreset.CUSTOM
        _isEditingCustom.value = true
        saveSettings(newSettings)
    }

    fun saveCustomPreset(name: String) {
        val preset = EqualizerPresetCustom(
            name = name,
            bands = _settings.value.bands
        )
        viewModelScope.launch {
            saveCustomPresetUseCase(preset)
            loadCustomPresets()
        }
    }

    fun deleteCustomPreset(id: Long) {
        viewModelScope.launch {
            deleteCustomPresetUseCase(id)
            loadCustomPresets()
        }
    }

    fun applyCustomPreset(preset: EqualizerPresetCustom) {
        val newSettings = _settings.value.copy(
            bands = preset.bands,
            preset = EqualizerPreset.CUSTOM
        )
        _settings.value = newSettings
        _selectedPreset.value = EqualizerPreset.CUSTOM
        _isEditingCustom.value = true
        saveSettings(newSettings)
    }

    fun resetToDefault() {
        val defaultSettings = EqualizerSettings.DEFAULT
        _settings.value = defaultSettings
        _selectedPreset.value = EqualizerPreset.NORMAL
        _isEditingCustom.value = false
        saveSettings(defaultSettings)
    }

    private fun saveSettings(settings: EqualizerSettings) {
        viewModelScope.launch {
            saveEqualizerSettingsUseCase(settings)
        }
    }

    // Band frequencies for display
    val bandFrequencies = listOf(60, 230, 910, 3600, 14000) // Hz

    fun getBandFrequencyLabel(index: Int): String {
        val freq = bandFrequencies.getOrNull(index) ?: 0
        return if (freq >= 1000) "${freq / 1000}kHz" else "${freq}Hz"
    }
}