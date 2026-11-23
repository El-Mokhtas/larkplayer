package com.larkplayer.app.data.source.local

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import androidx.room.*
import com.larkplayer.app.data.model.MediaItem
import com.larkplayer.app.data.model.MediaType
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<MediaItem>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: MediaItem)

    @Update
    suspend fun update(item: MediaItem)

    @Delete
    suspend fun delete(item: MediaItem)

    @Query("DELETE FROM media_items")
    suspend fun clearAll()

    // Audio queries
    @Query("SELECT * FROM media_items WHERE mediaType = :type ORDER BY title COLLATE NOCASE ASC")
    fun getAllByType(type: MediaType): LiveData<List<MediaItem>>

    @Query("SELECT * FROM media_items WHERE mediaType = :type ORDER BY title COLLATE NOCASE ASC")
    fun getAllByTypeFlow(type: MediaType): Flow<List<MediaItem>>

    @Query("SELECT * FROM media_items WHERE mediaType = :type ORDER BY title COLLATE NOCASE ASC")
    fun getAllByTypePaging(type: MediaType): PagingSource<Int, MediaItem>

    // Favorites
    @Query("SELECT * FROM media_items WHERE isFavorite = 1 AND mediaType = :type ORDER BY title COLLATE NOCASE ASC")
    fun getFavoritesByType(type: MediaType): LiveData<List<MediaItem>>

    @Query("SELECT * FROM media_items WHERE isFavorite = 1 AND mediaType = :type ORDER BY title COLLATE NOCASE ASC")
    fun getFavoritesByTypeFlow(type: MediaType): Flow<List<MediaItem>>

    // Recently played
    @Query("SELECT * FROM media_items WHERE lastPlayed IS NOT NULL AND mediaType = :type ORDER BY lastPlayed DESC LIMIT :limit")
    fun getRecentlyPlayed(type: MediaType, limit: Int): LiveData<List<MediaItem>>

    @Query("SELECT * FROM media_items WHERE lastPlayed IS NOT NULL AND mediaType = :type ORDER BY lastPlayed DESC LIMIT :limit")
    fun getRecentlyPlayedFlow(type: MediaType, limit: Int): Flow<List<MediaItem>>

    // Recently added
    @Query("SELECT * FROM media_items WHERE mediaType = :type ORDER BY dateAdded DESC LIMIT :limit")
    fun getRecentlyAdded(type: MediaType, limit: Int): LiveData<List<MediaItem>>

    @Query("SELECT * FROM media_items WHERE mediaType = :type ORDER BY dateAdded DESC LIMIT :limit")
    fun getRecentlyAddedFlow(type: MediaType, limit: Int): Flow<List<MediaItem>>

    // Search
    @Query("SELECT * FROM media_items WHERE (title LIKE :query OR artist LIKE :query OR album LIKE :query) AND mediaType = :type ORDER BY title COLLATE NOCASE ASC")
    fun search(query: String, type: MediaType): LiveData<List<MediaItem>>

    @Query("SELECT * FROM media_items WHERE (title LIKE :query OR artist LIKE :query OR album LIKE :query) AND mediaType = :type ORDER BY title COLLATE NOCASE ASC")
    fun searchFlow(query: String, type: MediaType): Flow<List<MediaItem>>

    @Query("SELECT * FROM media_items WHERE (title LIKE :query OR artist LIKE :query OR album LIKE :query) AND mediaType = :type ORDER BY title COLLATE NOCASE ASC")
    fun searchPaging(query: String, type: MediaType): PagingSource<Int, MediaItem>

    // By folder
    @Query("SELECT * FROM media_items WHERE folderPath = :folderPath AND mediaType = :type ORDER BY title COLLATE NOCASE ASC")
    fun getByFolder(folderPath: String, type: MediaType): LiveData<List<MediaItem>>

    @Query("SELECT * FROM media_items WHERE folderPath = :folderPath AND mediaType = :type ORDER BY title COLLATE NOCASE ASC")
    fun getByFolderFlow(folderPath: String, type: MediaType): Flow<List<MediaItem>>

    // By album
    @Query("SELECT * FROM media_items WHERE albumId = :albumId AND mediaType = :type ORDER BY trackNumber ASC")
    fun getByAlbum(albumId: Long, type: MediaType): LiveData<List<MediaItem>>

    @Query("SELECT * FROM media_items WHERE albumId = :albumId AND mediaType = :type ORDER BY trackNumber ASC")
    fun getByAlbumFlow(albumId: Long, type: MediaType): Flow<List<MediaItem>>

    // By artist
    @Query("SELECT * FROM media_items WHERE artistId = :artistId AND mediaType = :type ORDER BY album, trackNumber ASC")
    fun getByArtist(artistId: Long, type: MediaType): LiveData<List<MediaItem>>

    @Query("SELECT * FROM media_items WHERE artistId = :artistId AND mediaType = :type ORDER BY album, trackNumber ASC")
    fun getByArtistFlow(artistId: Long, type: MediaType): Flow<List<MediaItem>>

    // Single item
    @Query("SELECT * FROM media_items WHERE id = :id")
    fun getById(id: Long): LiveData<MediaItem?>

    @Query("SELECT * FROM media_items WHERE id = :id")
    suspend fun getByIdSuspend(id: Long): MediaItem?

    @Query("SELECT * FROM media_items WHERE data = :path")
    suspend fun getByPath(path: String): MediaItem?

    // Count
    @Query("SELECT COUNT(*) FROM media_items WHERE mediaType = :type")
    suspend fun countByType(type: MediaType): Int

    @Query("SELECT COUNT(*) FROM media_items WHERE isFavorite = 1 AND mediaType = :type")
    suspend fun countFavoritesByType(type: MediaType): Int

    // Update specific fields
    @Query("UPDATE media_items SET isFavorite = :favorite WHERE id = :id")
    suspend fun setFavorite(id: Long, favorite: Boolean)

    @Query("UPDATE media_items SET playCount = playCount + 1, lastPlayed = :lastPlayed WHERE id = :id")
    suspend fun incrementPlayCount(id: Long, lastPlayed: Long)

    @Query("UPDATE media_items SET bookmarkPosition = :position WHERE id = :id")
    suspend fun updateBookmark(id: Long, position: Long)

    @Query("UPDATE media_items SET lyricsPath = :path WHERE id = :id")
    suspend fun updateLyricsPath(id: Long, path: String?)
}