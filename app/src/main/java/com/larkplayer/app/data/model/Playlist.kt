package com.larkplayer.app.data.model

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "playlists",
    indices = [Index(value = ["name"], unique = true)],
    foreignKeys = [
        ForeignKey(
            entity = MediaItem::class,
            parentColumns = ["id"],
            childColumns = ["mediaItemId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Playlist(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,
    val description: String = "",
    val artworkUri: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isSystemPlaylist: Boolean = false,
    val sortOrder: Int = 0
) : Parcelable {

    constructor(parcel: Parcel) : this(
        id = parcel.readLong(),
        name = parcel.readString()!!,
        description = parcel.readString()!!,
        artworkUri = parcel.readString(),
        createdAt = parcel.readLong(),
        updatedAt = parcel.readLong(),
        isSystemPlaylist = parcel.readInt() != 0,
        sortOrder = parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(name)
        parcel.writeString(description)
        parcel.writeString(artworkUri)
        parcel.writeLong(createdAt)
        parcel.writeLong(updatedAt)
        parcel.writeInt(if (isSystemPlaylist) 1 else 0)
        parcel.writeInt(sortOrder)
    }

    override fun describeContents(): Int = 0

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Playlist> = object : Parcelable.Creator<Playlist> {
            override fun createFromParcel(parcel: Parcel): Playlist = Playlist(parcel)
            override fun newArray(size: Int): Array<Playlist?> = arrayOfNulls(size)
        }

        // System playlists
        val LIKED_SONGS = Playlist(
            id = -1,
            name = "Liked Songs",
            description = "Your favorite tracks",
            isSystemPlaylist = true,
            sortOrder = -100
        )

        val RECENTLY_PLAYED = Playlist(
            id = -2,
            name = "Recently Played",
            description = "Recently played tracks",
            isSystemPlaylist = true,
            sortOrder = -99
        )

        val RECENTLY_ADDED = Playlist(
            id = -3,
            name = "Recently Added",
            description = "Recently added to library",
            isSystemPlaylist = true,
            sortOrder = -98
        )
    }

    val artworkUrl: android.net.Uri?
        get() = artworkUri?.let { android.net.Uri.parse(it) }
}

@Serializable
@Entity(
    tableName = "playlist_items",
    primaryKeys = ["playlistId", "mediaItemId"],
    foreignKeys = [
        ForeignKey(
            entity = Playlist::class,
            parentColumns = ["id"],
            childColumns = ["playlistId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = MediaItem::class,
            parentColumns = ["id"],
            childColumns = ["mediaItemId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PlaylistItem(
    val playlistId: Long,
    val mediaItemId: Long,
    val position: Int,
    val addedAt: Long = System.currentTimeMillis()
)