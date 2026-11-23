package com.larkplayer.app.domain.usecase

import androidx.paging.Pager
import com.larkplayer.app.data.model.Playlist
import com.larkplayer.app.data.model.PlaylistItem
import com.larkplayer.app.domain.repository.PlaylistRepository
import javax.inject.Inject

class CreatePlaylistUseCase @Inject constructor(
    private val repository: PlaylistRepository
) {
    operator fun invoke(name: String, description: String = "") = repository.createPlaylist(name, description)
}

class UpdatePlaylistUseCase @Inject constructor(
    private val repository: PlaylistRepository
) {
    operator fun invoke(playlist: Playlist) = repository.updatePlaylist(playlist)
}

class DeletePlaylistUseCase @Inject constructor(
    private val repository: PlaylistRepository
) {
    operator fun invoke(id: Long) = repository.deletePlaylist(id)
}

class GetAllUserPlaylistsUseCase @Inject constructor(
    private val repository: PlaylistRepository
) {
    operator fun invoke(): Flow<List<Playlist>> = repository.getAllUserPlaylists()
}

class GetSystemPlaylistsUseCase @Inject constructor(
    private val repository: PlaylistRepository
) {
    operator fun invoke(): Flow<List<Playlist>> = repository.getSystemPlaylists()
}

class GetPlaylistByIdUseCase @Inject constructor(
    private val repository: PlaylistRepository
) {
    operator fun invoke(id: Long) = repository.getById(id)
}

class AddToPlaylistUseCase @Inject constructor(
    private val repository: PlaylistRepository
) {
    operator fun invoke(playlistId: Long, mediaItemId: Long) = repository.addItem(playlistId, mediaItemId)
}

class AddMultipleToPlaylistUseCase @Inject constructor(
    private val repository: PlaylistRepository
) {
    operator fun invoke(playlistId: Long, mediaItemIds: List<Long>) = repository.addItems(playlistId, mediaItemIds)
}

class RemoveFromPlaylistUseCase @Inject constructor(
    private val repository: PlaylistRepository
) {
    operator fun invoke(playlistId: Long, mediaItemId: Long) = repository.removeItem(playlistId, mediaItemId)
}

class ClearPlaylistUseCase @Inject constructor(
    private val repository: PlaylistRepository
) {
    operator fun invoke(playlistId: Long) = repository.clearPlaylist(playlistId)
}

class GetPlaylistItemsUseCase @Inject constructor(
    private val repository: PlaylistRepository
) {
    operator fun invoke(playlistId: Long): Flow<List<PlaylistItem>> = repository.getItems(playlistId)
}

class GetPlaylistItemsPagingUseCase @Inject constructor(
    private val repository: PlaylistRepository
) {
    operator fun invoke(playlistId: Long) = repository.getItemsPaging(playlistId)
}

class GetPlaylistItemCountUseCase @Inject constructor(
    private val repository: PlaylistRepository
) {
    operator fun invoke(playlistId: Long) = repository.getItemCount(playlistId)
}

class ReorderPlaylistItemUseCase @Inject constructor(
    private val repository: PlaylistRepository
) {
    operator fun invoke(playlistId: Long, mediaItemId: Long, newPosition: Int) = repository.reorderItem(playlistId, mediaItemId, newPosition)
}

class ContainsInPlaylistUseCase @Inject constructor(
    private val repository: PlaylistRepository
) {
    operator fun invoke(playlistId: Long, mediaItemId: Long) = repository.containsItem(playlistId, mediaItemId)
}