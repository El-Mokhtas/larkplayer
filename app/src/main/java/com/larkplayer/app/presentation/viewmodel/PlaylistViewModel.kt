package com.larkplayer.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.flow.cachedIn
import com.larkplayer.app.data.model.Playlist
import com.larkplayer.app.data.model.PlaylistItem
import com.larkplayer.app.domain.usecase.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    private val getAllUserPlaylistsUseCase: GetAllUserPlaylistsUseCase,
    private val getSystemPlaylistsUseCase: GetSystemPlaylistsUseCase,
    private val createPlaylistUseCase: CreatePlaylistUseCase,
    private val updatePlaylistUseCase: UpdatePlaylistUseCase,
    private val deletePlaylistUseCase: DeletePlaylistUseCase,
    private val getPlaylistByIdUseCase: GetPlaylistByIdUseCase,
    private val addToPlaylistUseCase: AddToPlaylistUseCase,
    private val addMultipleToPlaylistUseCase: AddMultipleToPlaylistUseCase,
    private val removeFromPlaylistUseCase: RemoveFromPlaylistUseCase,
    private val clearPlaylistUseCase: ClearPlaylistUseCase,
    private val getPlaylistItemsUseCase: GetPlaylistItemsUseCase,
    private val getPlaylistItemsPagingUseCase: GetPlaylistItemsPagingUseCase,
    private val getPlaylistItemCountUseCase: GetPlaylistItemCountUseCase,
    private val reorderPlaylistItemUseCase: ReorderPlaylistItemUseCase,
    private val containsInPlaylistUseCase: ContainsInPlaylistUseCase
) : ViewModel() {

    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: StateFlow<List<Playlist>> = _playlists

    private val _systemPlaylists = MutableStateFlow<List<Playlist>>(emptyList())
    val systemPlaylists: StateFlow<List<Playlist>> = _systemPlaylists

    private val _selectedPlaylist = MutableStateFlow<Playlist?>(null)
    val selectedPlaylist: StateFlow<Playlist?> = _selectedPlaylist

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadPlaylists()
    }

    private fun loadPlaylists() {
        viewModelScope.launch {
            _isLoading.value = true
            // In real implementation, you'd collect from the flows
            _isLoading.value = false
        }
    }

    fun onPlaylistClick(playlist: Playlist) {
        _selectedPlaylist.value = playlist
    }

    fun createPlaylist(name: String, description: String = "") {
        viewModelScope.launch {
            val id = createPlaylistUseCase(name, description)
            // Refresh list
        }
    }

    fun updatePlaylist(playlist: Playlist) {
        viewModelScope.launch {
            updatePlaylistUseCase(playlist)
        }
    }

    fun deletePlaylist(id: Long) {
        viewModelScope.launch {
            deletePlaylistUseCase(id)
        }
    }

    fun addToPlaylist(playlistId: Long, mediaItemId: Long) {
        viewModelScope.launch {
            addToPlaylistUseCase(playlistId, mediaItemId)
        }
    }

    fun addMultipleToPlaylist(playlistId: Long, mediaItemIds: List<Long>) {
        viewModelScope.launch {
            addMultipleToPlaylistUseCase(playlistId, mediaItemIds)
        }
    }

    fun removeFromPlaylist(playlistId: Long, mediaItemId: Long) {
        viewModelScope.launch {
            removeFromPlaylistUseCase(playlistId, mediaItemId)
        }
    }

    fun clearPlaylist(playlistId: Long) {
        viewModelScope.launch {
            clearPlaylistUseCase(playlistId)
        }
    }

    fun getPlaylistItems(playlistId: Long) = getPlaylistItemsUseCase(playlistId)

    fun getPlaylistItemsPaging(playlistId: Long) =
        getPlaylistItemsPagingUseCase(playlistId).flow.cachedIn(viewModelScope)

    fun getPlaylistItemCount(playlistId: Long) {
        viewModelScope.launch {
            getPlaylistItemCountUseCase(playlistId)
        }
    }

    fun reorderItem(playlistId: Long, mediaItemId: Long, newPosition: Int) {
        viewModelScope.launch {
            reorderPlaylistItemUseCase(playlistId, mediaItemId, newPosition)
        }
    }

    fun containsInPlaylist(playlistId: Long, mediaItemId: Long) {
        viewModelScope.launch {
            containsInPlaylistUseCase(playlistId, mediaItemId)
        }
    }
}