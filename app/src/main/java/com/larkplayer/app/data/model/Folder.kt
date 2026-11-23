package com.larkplayer.app.data.model

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "folders")
data class Folder(
    @PrimaryKey
    val path: String,

    val name: String,
    val mediaCount: Int = 0,
    val totalDuration: Long = 0,
    val totalSize: Long = 0,
    val lastScanned: Long = System.currentTimeMillis(),
    val isHidden: Boolean = false,
    val parentPath: String? = null
) : Parcelable {

    constructor(parcel: Parcel) : this(
        path = parcel.readString()!!,
        name = parcel.readString()!!,
        mediaCount = parcel.readInt(),
        totalDuration = parcel.readLong(),
        totalSize = parcel.readLong(),
        lastScanned = parcel.readLong(),
        isHidden = parcel.readInt() != 0,
        parentPath = parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(path)
        parcel.writeString(name)
        parcel.writeInt(mediaCount)
        parcel.writeLong(totalDuration)
        parcel.writeLong(totalSize)
        parcel.writeLong(lastScanned)
        parcel.writeInt(if (isHidden) 1 else 0)
        parcel.writeString(parentPath)
    }

    override fun describeContents(): Int = 0

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Folder> = object : Parcelable.Creator<Folder> {
            override fun createFromParcel(parcel: Parcel): Folder = Folder(parcel)
            override fun newArray(size: Int): Array<Folder?> = arrayOfNulls(size)
        }
    }

    val displayName: String
        get() = if (name.isNotBlank()) name else path.split("/").lastOrNull() ?: path

    val depth: Int
        get() = path.count { it == '/' }
}