package com.larkplayer.app.domain.repository

import androidx.paging.Pager
import com.larkplayer.app.data.model.Playlist
import com.larkplayer.app.data.model.PlaylistItem
import kotlinx.coroutines.flow.Flow

interface PlaylistRepository {

    suspend fun createPlaylist(name: String, description: String): Long

    suspend fun updatePlaylist(playlist: Playlist)

    suspend fun deletePlaylist(id: Long): Boolean

    fun getAllUserPlaylists(): Flow<List<Playlist>>

    fun getSystemPlaylists(): Flow<List<Playlist>>

    suspend fun getById(id: Long): Playlist?

    // Playlist Items
    suspend fun addItem(playlistId: Long, mediaItemId: Long): Boolean

    suspend fun addItems(playlistId: Long, mediaItemIds: List<Long>): Int

    suspend fun removeItem(playlistId: Long, mediaItemId: Long): Boolean

    suspend fun clearPlaylist(playlistId: Long): Int

    fun getItems(playlistId: Long): Flow<List<PlaylistItem>>

    fun getItemsPaging(playlistId: Long): Pager<Int, PlaylistItem>

    suspend fun getItemCount(playlistId: Long): Int

    suspend fun reorderItem(playlistId: Long, mediaItemId: Long, newPosition: Int)

    suspend fun containsItem(playlistId: Long, mediaItemId: Long): Boolean
}