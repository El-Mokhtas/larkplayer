package com.larkplayer.app.data.model

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "media_items")
@TypeConverters(MediaTypeConverter::class)
data class MediaItem(
    @PrimaryKey
    @SerializedName("id")
    val id: Long,

    @SerializedName("title")
    val title: String,

    @SerializedName("artist")
    val artist: String?,

    @SerializedName("album")
    val album: String?,

    @SerializedName("albumArtist")
    val albumArtist: String?,

    @SerializedName("duration")
    val duration: Long,

    @SerializedName("data")
    val data: String,

    @SerializedName("displayName")
    val displayName: String,

    @SerializedName("mimeType")
    val mimeType: String,

    @SerializedName("size")
    val size: Long,

    @SerializedName("dateAdded")
    val dateAdded: Long,

    @SerializedName("dateModified")
    val dateModified: Long,

    @SerializedName("mediaType")
    val mediaType: MediaType,

    @SerializedName("albumId")
    val albumId: Long?,

    @SerializedName("artistId")
    val artistId: Long?,

    @SerializedName("trackNumber")
    val trackNumber: Int?,

    @SerializedName("year")
    val year: Int?,

    @SerializedName("genre")
    val genre: String?,

    @SerializedName("composer")
    val composer: String?,

    @SerializedName("writer")
    val writer: String?,

    @SerializedName("isFavorite")
    val isFavorite: Boolean = false,

    @SerializedName("playCount")
    val playCount: Int = 0,

    @SerializedName("lastPlayed")
    val lastPlayed: Long? = null,

    @SerializedName("bookmarkPosition")
    val bookmarkPosition: Long = 0,

    @SerializedName("lyricsPath")
    val lyricsPath: String? = null,

    @SerializedName("artworkUri")
    val artworkUri: String? = null,

    @SerializedName("folderPath")
    val folderPath: String? = null
) : Parcelable {

    constructor(parcel: Parcel) : this(
        id = parcel.readLong(),
        title = parcel.readString()!!,
        artist = parcel.readString(),
        album = parcel.readString(),
        albumArtist = parcel.readString(),
        duration = parcel.readLong(),
        data = parcel.readString()!!,
        displayName = parcel.readString()!!,
        mimeType = parcel.readString()!!,
        size = parcel.readLong(),
        dateAdded = parcel.readLong(),
        dateModified = parcel.readLong(),
        mediaType = MediaType.valueOf(parcel.readString()!!),
        albumId = parcel.readLong().takeIf { it != -1L },
        artistId = parcel.readLong().takeIf { it != -1L },
        trackNumber = parcel.readInt().takeIf { it != -1 },
        year = parcel.readInt().takeIf { it != -1 },
        genre = parcel.readString(),
        composer = parcel.readString(),
        writer = parcel.readString(),
        isFavorite = parcel.readInt() != 0,
        playCount = parcel.readInt(),
        lastPlayed = parcel.readLong().takeIf { it != -1L },
        bookmarkPosition = parcel.readLong(),
        lyricsPath = parcel.readString(),
        artworkUri = parcel.readString(),
        folderPath = parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(title)
        parcel.writeString(artist)
        parcel.writeString(album)
        parcel.writeString(albumArtist)
        parcel.writeLong(duration)
        parcel.writeString(data)
        parcel.writeString(displayName)
        parcel.writeString(mimeType)
        parcel.writeLong(size)
        parcel.writeLong(dateAdded)
        parcel.writeLong(dateModified)
        parcel.writeString(mediaType.name)
        parcel.writeLong(albumId ?: -1L)
        parcel.writeLong(artistId ?: -1L)
        parcel.writeInt(trackNumber ?: -1)
        parcel.writeInt(year ?: -1)
        parcel.writeString(genre)
        parcel.writeString(composer)
        parcel.writeString(writer)
        parcel.writeInt(if (isFavorite) 1 else 0)
        parcel.writeInt(playCount)
        parcel.writeLong(lastPlayed ?: -1L)
        parcel.writeLong(bookmarkPosition)
        parcel.writeString(lyricsPath)
        parcel.writeString(artworkUri)
        parcel.writeString(folderPath)
    }

    override fun describeContents(): Int = 0

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<MediaItem> = object : Parcelable.Creator<MediaItem> {
            override fun createFromParcel(parcel: Parcel): MediaItem = MediaItem(parcel)
            override fun newArray(size: Int): Array<MediaItem?> = arrayOfNulls(size)
        }
    }

    val artworkUrl: Uri?
        get() = artworkUri?.let { Uri.parse(it) }

    val fileUri: Uri
        get() = Uri.parse(data)

    val isAudio: Boolean
        get() = mediaType == MediaType.AUDIO

    val isVideo: Boolean
        get() = mediaType == MediaType.VIDEO

    val formattedDuration: String
        get() = formatDuration(duration)

    companion object {
        private fun formatDuration(durationMs: Long): String {
            val totalSeconds = durationMs / 1000
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60
            return if (hours > 0) {
                String.format("%d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format("%02d:%02d", minutes, seconds)
            }
        }
    }
}

enum class MediaType {
    AUDIO,
    VIDEO,
    UNKNOWN
}

class MediaTypeConverter {
    @androidx.room.TypeConverter
    fun fromMediaType(type: MediaType): String = type.name

    @androidx.room.TypeConverter
    fun toMediaType(type: String): MediaType = MediaType.valueOf(type)
}