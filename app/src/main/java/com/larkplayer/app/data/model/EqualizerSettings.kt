package com.larkplayer.app.data.model

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "equalizer_settings")
data class EqualizerSettings(
    @PrimaryKey
    val id: Int = 0,

    val preset: EqualizerPreset = EqualizerPreset.NORMAL,
    val bands: List<Float> = listOf(0f, 0f, 0f, 0f, 0f), // 5 bands: 60Hz, 230Hz, 910Hz, 3.6kHz, 14kHz
    val bassBoostEnabled: Boolean = false,
    val bassBoostStrength: Float = 0f, // 0-1
    val virtualizerEnabled: Boolean = false,
    val virtualizerStrength: Float = 0f, // 0-1
    val volumeBoostEnabled: Boolean = false,
    val volumeBoostLevel: Float = 1.0f, // 1.0-3.0 (100%-300%)
    val updatedAt: Long = System.currentTimeMillis()
) : Parcelable {

    constructor(parcel: Parcel) : this(
        id = parcel.readInt(),
        preset = EqualizerPreset.valueOf(parcel.readString()!!),
        bands = parcel.createFloatArray()?.toList() ?: listOf(0f, 0f, 0f, 0f, 0f),
        bassBoostEnabled = parcel.readInt() != 0,
        bassBoostStrength = parcel.readFloat(),
        virtualizerEnabled = parcel.readInt() != 0,
        virtualizerStrength = parcel.readFloat(),
        volumeBoostEnabled = parcel.readInt() != 0,
        volumeBoostLevel = parcel.readFloat(),
        updatedAt = parcel.readLong()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(preset.name)
        parcel.writeFloatArray(bands.toFloatArray())
        parcel.writeInt(if (bassBoostEnabled) 1 else 0)
        parcel.writeFloat(bassBoostStrength)
        parcel.writeInt(if (virtualizerEnabled) 1 else 0)
        parcel.writeFloat(virtualizerStrength)
        parcel.writeInt(if (volumeBoostEnabled) 1 else 0)
        parcel.writeFloat(volumeBoostLevel)
        parcel.writeLong(updatedAt)
    }

    override fun describeContents(): Int = 0

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<EqualizerSettings> = object : Parcelable.Creator<EqualizerSettings> {
            override fun createFromParcel(parcel: Parcel): EqualizerSettings = EqualizerSettings(parcel)
            override fun newArray(size: Int): Array<EqualizerSettings?> = arrayOfNulls(size)
        }

        val DEFAULT = EqualizerSettings()

        // Preset band values (5 bands: 60Hz, 230Hz, 910Hz, 3.6kHz, 14kHz)
        val PRESET_BANDS = mapOf(
            EqualizerPreset.NORMAL to floatArrayOf(0f, 0f, 0f, 0f, 0f),
            EqualizerPreset.POP to floatArrayOf(2f, 1f, 0f, -1f, -2f),
            EqualizerPreset.ROCK to floatArrayOf(4f, 2f, -1f, -2f, 3f),
            EqualizerPreset.JAZZ to floatArrayOf(3f, 1f, 0f, 2f, 3f),
            EqualizerPreset.CLASSICAL to floatArrayOf(2f, 1f, 0f, 1f, 2f),
            EqualizerPreset.DANCE to floatArrayOf(4f, 2f, 0f, -1f, 2f),
            EqualizerPreset.HIP_HOP to floatArrayOf(5f, 2f, -1f, -2f, 1f),
            EqualizerPreset.CUSTOM to floatArrayOf(0f, 0f, 0f, 0f, 0f)
        )
    }

    fun getBandsForPreset(): List<Float> {
        return if (preset == EqualizerPreset.CUSTOM) {
            bands
        } else {
            PRESET_BANDS[preset]?.toList() ?: listOf(0f, 0f, 0f, 0f, 0f)
        }
    }

    fun copyWithUpdatedBands(newBands: List<Float>): EqualizerSettings {
        return copy(
            bands = newBands,
            preset = EqualizerPreset.CUSTOM,
            updatedAt = System.currentTimeMillis()
        )
    }
}

enum class EqualizerPreset {
    NORMAL,
    POP,
    ROCK,
    JAZZ,
    CLASSICAL,
    DANCE,
    HIP_HOP,
    CUSTOM
}

@Serializable
@Entity(tableName = "equalizer_presets")
data class EqualizerPresetCustom(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,
    val bands: List<Float>,
    val createdAt: Long = System.currentTimeMillis()
)