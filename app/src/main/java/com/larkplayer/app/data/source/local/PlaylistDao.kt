package com.larkplayer.app.data.source.local

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import androidx.room.*
import com.larkplayer.app.data.model.Playlist
import com.larkplayer.app.data.model.PlaylistItem
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: Playlist): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylists(playlists: List<Playlist>)

    @Update
    suspend fun updatePlaylist(playlist: Playlist)

    @Delete
    suspend fun deletePlaylist(playlist: Playlist)

    @Query("DELETE FROM playlists WHERE id = :id AND isSystemPlaylist = 0")
    suspend fun deleteById(id: Long): Int

    @Query("SELECT * FROM playlists WHERE isSystemPlaylist = 0 ORDER BY sortOrder ASC, name COLLATE NOCASE ASC")
    fun getAllUserPlaylists(): LiveData<List<Playlist>>

    @Query("SELECT * FROM playlists WHERE isSystemPlaylist = 0 ORDER BY sortOrder ASC, name COLLATE NOCASE ASC")
    fun getAllUserPlaylistsFlow(): Flow<List<Playlist>>

    @Query("SELECT * FROM playlists WHERE isSystemPlaylist = 1")
    fun getSystemPlaylists(): LiveData<List<Playlist>>

    @Query("SELECT * FROM playlists WHERE id = :id")
    fun getById(id: Long): LiveData<Playlist?>

    @Query("SELECT * FROM playlists WHERE id = :id")
    suspend fun getByIdSuspend(id: Long): Playlist?

    // Playlist Items
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<PlaylistItem>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: PlaylistItem)

    @Delete
    suspend fun deleteItem(item: PlaylistItem)

    @Query("DELETE FROM playlist_items WHERE playlistId = :playlistId AND mediaItemId = :mediaItemId")
    suspend fun removeItem(playlistId: Long, mediaItemId: Long): Int

    @Query("DELETE FROM playlist_items WHERE playlistId = :playlistId")
    suspend fun clearPlaylist(playlistId: Long): Int

    @Query("SELECT * FROM playlist_items WHERE playlistId = :playlistId ORDER BY position ASC")
    fun getItems(playlistId: Long): LiveData<List<PlaylistItem>>

    @Query("SELECT * FROM playlist_items WHERE playlistId = :playlistId ORDER BY position ASC")
    fun getItemsFlow(playlistId: Long): Flow<List<PlaylistItem>>

    @Query("SELECT * FROM playlist_items WHERE playlistId = :playlistId ORDER BY position ASC")
    fun getItemsPaging(playlistId: Long): PagingSource<Int, PlaylistItem>

    @Query("SELECT COUNT(*) FROM playlist_items WHERE playlistId = :playlistId")
    suspend fun getItemCount(playlistId: Long): Int

    @Query("SELECT MAX(position) FROM playlist_items WHERE playlistId = :playlistId")
    suspend fun getMaxPosition(playlistId: Long): Int?

    @Query("SELECT mediaItemId FROM playlist_items WHERE playlistId = :playlistId ORDER BY position ASC")
    suspend fun getMediaItemIds(playlistId: Long): List<Long>

    // Reorder
    @Query("UPDATE playlist_items SET position = :newPosition WHERE playlistId = :playlistId AND mediaItemId = :mediaItemId")
    suspend fun updatePosition(playlistId: Long, mediaItemId: Long, newPosition: Int)

    // Check if item exists in playlist
    @Query("SELECT 1 FROM playlist_items WHERE playlistId = :playlistId AND mediaItemId = :mediaItemId LIMIT 1")
    suspend fun containsItem(playlistId: Long, mediaItemId: Long): Boolean?
}