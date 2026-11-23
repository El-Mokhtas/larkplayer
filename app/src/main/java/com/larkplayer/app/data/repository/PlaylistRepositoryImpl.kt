package com.larkplayer.app.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.larkplayer.app.data.model.Playlist
import com.larkplayer.app.data.model.PlaylistItem
import com.larkplayer.app.data.source.local.AppDatabase
import com.larkplayer.app.data.source.local.PlaylistDao
import com.larkplayer.app.domain.repository.PlaylistRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PlaylistRepositoryImpl @Inject constructor(
    private val database: AppDatabase
) : PlaylistRepository {

    private val playlistDao: PlaylistDao = database.playlistDao()

    override suspend fun createPlaylist(name: String, description: String): Long {
        return withContext(Dispatchers.IO) {
            val playlist = Playlist(name = name, description = description)
            playlistDao.insertPlaylist(playlist)
        }
    }

    override suspend fun updatePlaylist(playlist: Playlist) = withContext(Dispatchers.IO) {
        playlistDao.updatePlaylist(playlist.copy(updatedAt = System.currentTimeMillis()))
    }

    override suspend fun deletePlaylist(id: Long): Boolean = withContext(Dispatchers.IO) {
        playlistDao.deleteById(id) > 0
    }

    override fun getAllUserPlaylists(): Flow<List<Playlist>> = playlistDao.getAllUserPlaylistsFlow()

    override fun getSystemPlaylists(): Flow<List<Playlist>> = playlistDao.getSystemPlaylists()

    override suspend fun getById(id: Long): Playlist? = playlistDao.getByIdSuspend(id)

    override suspend fun addItem(playlistId: Long, mediaItemId: Long): Boolean = withContext(Dispatchers.IO) {
        val maxPos = playlistDao.getMaxPosition(playlistId) ?: -1
        val item = PlaylistItem(playlistId, mediaItemId, maxPos + 1)
        playlistDao.insertItem(item)
        true
    }

    override suspend fun addItems(playlistId: Long, mediaItemIds: List<Long>): Int = withContext(Dispatchers.IO) {
        var maxPos = playlistDao.getMaxPosition(playlistId) ?: -1
        val items = mediaItemIds.mapIndexed { index, mediaId ->
            maxPos++
            PlaylistItem(playlistId, mediaId, maxPos)
        }
        playlistDao.insertItems(items)
        items.size
    }

    override suspend fun removeItem(playlistId: Long, mediaItemId: Long): Boolean = withContext(Dispatchers.IO) {
        playlistDao.removeItem(playlistId, mediaItemId) > 0
    }

    override suspend fun clearPlaylist(playlistId: Long): Int = withContext(Dispatchers.IO) {
        playlistDao.clearPlaylist(playlistId)
    }

    override fun getItems(playlistId: Long): Flow<List<PlaylistItem>> = playlistDao.getItemsFlow(playlistId)

    override fun getItemsPaging(playlistId: Long): Pager<Int, PlaylistItem> = Pager(
        PagingConfig(pageSize = 50, enablePlaceholders = false)
    ) { playlistDao.getItemsPaging(playlistId) }

    override suspend fun getItemCount(playlistId: Long): Int = playlistDao.getItemCount(playlistId)

    override suspend fun reorderItem(playlistId: Long, mediaItemId: Long, newPosition: Int) = withContext(Dispatchers.IO) {
        playlistDao.updatePosition(playlistId, mediaItemId, newPosition)
    }

    override suspend fun containsItem(playlistId: Long, mediaItemId: Long): Boolean = withContext(Dispatchers.IO) {
        playlistDao.containsItem(playlistId, mediaItemId) == true
    }
}