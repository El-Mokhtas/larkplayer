package com.larkplayer.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.larkplayer.app.data.model.MediaItem
import com.larkplayer.app.data.model.MediaType
import com.larkplayer.app.domain.usecase.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val getAllAudioUseCase: GetAllAudioUseCase,
    private val getAllVideoUseCase: GetAllVideoUseCase,
    private val getFavoritesAudioUseCase: GetFavoritesAudioUseCase,
    private val getFavoritesVideoUseCase: GetFavoritesVideoUseCase,
    private val getRecentlyPlayedAudioUseCase: GetRecentlyPlayedAudioUseCase,
    private val getRecentlyPlayedVideoUseCase: GetRecentlyPlayedVideoUseCase,
    private val getRecentlyAddedAudioUseCase: GetRecentlyAddedAudioUseCase,
    private val getRecentlyAddedVideoUseCase: GetRecentlyAddedVideoUseCase,
    private val searchAudioUseCase: SearchAudioUseCase,
    private val searchVideoUseCase: SearchVideoUseCase,
    private val scanMediaUseCase: ScanMediaUseCase,
    private val countAudioUseCase: CountAudioUseCase,
    private val countVideoUseCase: CountVideoUseCase
) : ViewModel() {

    // UI State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching

    private val _selectedTab = MutableStateFlow(LibraryTab.SONGS)
    val selectedTab: StateFlow<LibraryTab> = _selectedTab

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning

    // Data flows
    val allAudio = getAllAudioUseCase()
    val allVideo = getAllVideoUseCase()
    val favoritesAudio = getFavoritesAudioUseCase()
    val favoritesVideo = getFavoritesVideoUseCase()
    val recentlyPlayedAudio = getRecentlyPlayedAudioUseCase(20)
    val recentlyPlayedVideo = getRecentlyPlayedVideoUseCase(20)
    val recentlyAddedAudio = getRecentlyAddedAudioUseCase(20)
    val recentlyAddedVideo = getRecentlyAddedVideoUseCase(20)

    // Search results
    val searchResultsAudio = combine(_searchQuery.distinctUntilChanged(), _isSearching) { query, searching ->
        if (searching && query.isNotBlank()) {
            searchAudioUseCase(query)
        } else {
            emptyFlow<List<MediaItem>>()
        }
    }.flattenMerge()

    val searchResultsVideo = combine(_searchQuery.distinctUntilChanged(), _isSearching) { query, searching ->
        if (searching && query.isNotBlank()) {
            searchVideoUseCase(query)
        } else {
            emptyFlow<List<MediaItem>>()
        }
    }.flattenMerge()

    // Counts
    val audioCount = countAudioUseCase()
    val videoCount = countVideoUseCase()

    fun onTabSelected(tab: LibraryTab) {
        _selectedTab.value = tab
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        _isSearching.value = query.isNotBlank()
    }

    fun clearSearch() {
        _searchQuery.value = ""
        _isSearching.value = false
    }

    fun scanMedia() {
        _isScanning.value = true
        viewModelScope.launch {
            scanMediaUseCase()
            _isScanning.value = false
        }
    }

    // Helper to get appropriate data based on tab
    fun getDataForTab(tab: LibraryTab, type: MediaType): StateFlow<List<MediaItem>> {
        return when (tab) {
            LibraryTab.SONGS -> if (type == MediaType.AUDIO) allAudio else allVideo
            LibraryTab.FAVORITES -> if (type == MediaType.AUDIO) favoritesAudio else favoritesVideo
            LibraryTab.RECENTLY_PLAYED -> if (type == MediaType.AUDIO) recentlyPlayedAudio else recentlyPlayedVideo
            LibraryTab.RECENTLY_ADDED -> if (type == MediaType.AUDIO) recentlyAddedAudio else recentlyAddedVideo
        } as StateFlow<List<MediaItem>>
    }

    companion object {
        fun emptyFlow<T>(): kotlinx.coroutines.flow.Flow<T> = kotlinx.coroutines.flow.flowOf()
    }
}

enum class LibraryTab {
    SONGS,
    FAVORITES,
    RECENTLY_PLAYED,
    RECENTLY_ADDED
}