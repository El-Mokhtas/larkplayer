package com.larkplayer.app.data.source.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.larkplayer.app.data.model.EqualizerPresetCustom
import com.larkplayer.app.data.model.EqualizerSettings
import com.larkplayer.app.data.model.Folder
import com.larkplayer.app.data.model.MediaItem
import com.larkplayer.app.data.model.MediaTypeConverter
import com.larkplayer.app.data.model.Playlist
import com.larkplayer.app.data.model.PlaylistItem

@Database(
    entities = [
        MediaItem::class,
        Playlist::class,
        PlaylistItem::class,
        Folder::class,
        EqualizerSettings::class,
        EqualizerPresetCustom::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(MediaTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun mediaDao(): MediaDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun folderDao(): FolderDao
    abstract fun equalizerDao(): EqualizerDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "lark_player.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}