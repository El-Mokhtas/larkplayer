package com.larkplayer.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.larkplayer.app.data.model.Folder
import com.larkplayer.app.data.model.MediaItem
import com.larkplayer.app.data.model.MediaType
import com.larkplayer.app.domain.usecase.GetAllFoldersUseCase
import com.larkplayer.app.domain.usecase.GetByFolderUseCase
import com.larkplayer.app.domain.usecase.GetFolderByPathUseCase
import com.larkplayer.app.domain.usecase.ScanFoldersUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel

@HiltViewModel
class FolderViewModel @Inject constructor(
    private val getAllFoldersUseCase: GetAllFoldersUseCase,
    private val getSubFoldersUseCase: GetSubFoldersUseCase,
    private val getFolderByPathUseCase: GetFolderByPathUseCase,
    private val scanFoldersUseCase: ScanFoldersUseCase,
    private val getByFolderUseCase: GetByFolderUseCase
) : ViewModel() {

    private val _folders = MutableStateFlow<List<Folder>>(emptyList())
    val folders: StateFlow<List<Folder>> = _folders

    private val _currentFolder = MutableStateFlow<Folder?>(null)
    val currentFolder: StateFlow<Folder?> = _currentFolder

    private val _folderContents = MutableStateFlow<List<MediaItem>>(emptyList())
    val folderContents: StateFlow<List<MediaItem>> = _folderContents

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _mediaType = MutableStateFlow(MediaType.AUDIO)
    val mediaType: StateFlow<MediaType> = _mediaType

    init {
        loadFolders()
    }

    private fun loadFolders() {
        viewModelScope.launch {
            _isLoading.value = true
            // Collect from flow in real implementation
            _isLoading.value = false
        }
    }

    fun onFolderClick(folder: Folder) {
        _currentFolder.value = folder
        loadFolderContents(folder.path)
    }

    fun onBackClick() {
        _currentFolder.value?.parentPath?.let { parentPath ->
            getFolderByPathUseCase(parentPath)?.let { parentFolder ->
                _currentFolder.value = parentFolder
                loadFolderContents(parentPath)
            }
        } ?: run {
            _currentFolder.value = null
            loadFolders()
        }
    }

    fun setMediaType(type: MediaType) {
        _mediaType.value = type
        _currentFolder.value?.let { loadFolderContents(it.path) }
    }

    fun scanFolders() {
        viewModelScope.launch {
            _isLoading.value = true
            scanFoldersUseCase()
            _isLoading.value = false
        }
    }

    private fun loadFolderContents(folderPath: String) {
        viewModelScope.launch {
            _isLoading.value = true
            // Collect from getByFolderUseCase(folderPath, _mediaType.value)
            _isLoading.value = false
        }
    }

    val canGoBack: Boolean
        get() = _currentFolder.value?.parentPath != null
}