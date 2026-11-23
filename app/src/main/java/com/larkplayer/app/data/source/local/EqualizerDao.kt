package com.larkplayer.app.data.source.local

import androidx.lifecycle.LiveData
import androidx.room.*
import com.larkplayer.app.data.model.EqualizerPresetCustom
import com.larkplayer.app.data.model.EqualizerSettings
import kotlinx.coroutines.flow.Flow

@Dao
interface EqualizerDao {

    @Query("SELECT * FROM equalizer_settings WHERE id = 0")
    fun getSettings(): LiveData<EqualizerSettings?>

    @Query("SELECT * FROM equalizer_settings WHERE id = 0")
    suspend fun getSettingsSuspend(): EqualizerSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: EqualizerSettings)

    @Update
    suspend fun updateSettings(settings: EqualizerSettings)

    // Custom presets
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreset(preset: EqualizerPresetCustom): Long

    @Update
    suspend fun updatePreset(preset: EqualizerPresetCustom)

    @Delete
    suspend fun deletePreset(preset: EqualizerPresetCustom)

    @Query("DELETE FROM equalizer_presets WHERE id = :id")
    suspend fun deletePresetById(id: Long): Int

    @Query("SELECT * FROM equalizer_presets ORDER BY createdAt DESC")
    fun getCustomPresets(): LiveData<List<EqualizerPresetCustom>>

    @Query("SELECT * FROM equalizer_presets ORDER BY createdAt DESC")
    fun getCustomPresetsFlow(): Flow<List<EqualizerPresetCustom>>

    @Query("SELECT * FROM equalizer_presets WHERE id = :id")
    suspend fun getPresetById(id: Long): EqualizerPresetCustom?
}