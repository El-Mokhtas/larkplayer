package com.larkplayer.app.data.model

import androidx.room.TypeConverter

class EqualizerConverters {

    @TypeConverter
    fun fromPreset(preset: EqualizerPreset): String = preset.name

    @TypeConverter
    fun toPreset(value: String): EqualizerPreset =
        runCatching { EqualizerPreset.valueOf(value) }.getOrDefault(EqualizerPreset.NORMAL)

    @TypeConverter
    fun fromFloatList(values: List<Float>): String = values.joinToString(",")

    @TypeConverter
    fun toFloatList(value: String): List<Float> =
        if (value.isBlank()) emptyList()
        else value.split(",").mapNotNull { it.toFloatOrNull() }

    @TypeConverter
    fun fromFloatArray(values: FloatArray): String = values.joinToString(",")

    @TypeConverter
    fun toFloatArray(value: String): FloatArray =
        if (value.isBlank()) FloatArray(0)
        else value.split(",").mapNotNull { it.toFloatOrNull() }.toFloatArray()
}
