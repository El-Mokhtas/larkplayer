package com.larkplayer.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.larkplayer.app.data.model.MediaItem
import com.larkplayer.app.domain.usecase.GetMediaByIdUseCase
import com.larkplayer.app.domain.usecase.IncrementPlayCountUseCase
import com.larkplayer.app.domain.usecase.SetFavoriteUseCase
import com.larkplayer.app.domain.usecase.UpdateBookmarkUseCase
import com.larkplayer.app.domain.usecase.UpdateLyricsPathUseCase
import com.larkplayer.app.service.MediaPlaybackService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val getMediaByIdUseCase: GetMediaByIdUseCase,
    private val incrementPlayCountUseCase: IncrementPlayCountUseCase,
    private val setFavoriteUseCase: SetFavoriteUseCase,
    private val updateBookmarkUseCase: UpdateBookmarkUseCase,
    private val updateLyricsPathUseCase: UpdateLyricsPathUseCase
) : ViewModel() {

    // Playback state
    private val _currentMedia = MutableStateFlow<MediaItem?>(null)
    val currentMedia: StateFlow<MediaItem?> = _currentMedia

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed

    private val _repeatMode = MutableStateFlow(MediaPlaybackService.REPEAT_MODE_OFF)
    val repeatMode: StateFlow<Int> = _repeatMode

    private val _shuffleMode = MutableStateFlow(MediaPlaybackService.SHUFFLE_MODE_OFF)
    val shuffleMode: StateFlow<Int> = _shuffleMode

    private val _sleepTimerRemaining = MutableStateFlow<Long?>(null)
    val sleepTimerRemaining: StateFlow<Long?> = _sleepTimerRemaining

    private val _lyrics = MutableStateFlow<List<com.larkplayer.app.util.LyricLine>>(emptyList())
    val lyrics: StateFlow<List<com.larkplayer.app.util.LyricLine>> = _lyrics

    private val _currentLyricIndex = MutableStateFlow(-1)
    val currentLyricIndex: StateFlow<Int> = _currentLyricIndex

    // Queue
    private val _queue = MutableStateFlow<List<MediaItem>>(emptyList())
    val queue: StateFlow<List<MediaItem>> = _queue

    private val _queueIndex = MutableStateFlow(0)
    val queueIndex: StateFlow<Int> = _queueIndex

    fun loadMedia(mediaId: Long) {
        viewModelScope.launch {
            val media = getMediaByIdUseCase(mediaId)
            media?.let { _currentMedia.value = it }
        }
    }

    fun setCurrentMedia(media: MediaItem, queue: List<MediaItem> = emptyList(), index: Int = 0) {
        _currentMedia.value = media
        _queue.value = if (queue.isNotEmpty()) queue else listOf(media)
        _queueIndex.value = index.coerceIn(0, _queue.value.lastIndex)
    }

    fun updatePlaybackState(isPlaying: Boolean) {
        _isPlaying.value = isPlaying
    }

    fun updatePosition(position: Long, duration: Long) {
        _currentPosition.value = position
        _duration.value = duration
        updateLyricPosition(position)
    }

    fun setPlaybackSpeed(speed: Float) {
        _playbackSpeed.value = speed.coerceIn(0.5f, 2.0f)
    }

    fun setRepeatMode(mode: Int) {
        _repeatMode.value = mode
    }

    fun setShuffleMode(mode: Int) {
        _shuffleMode.value = mode
    }

    fun toggleFavorite() {
        _currentMedia.value?.let { media ->
            val newFavorite = !media.isFavorite
            _currentMedia.value = media.copy(isFavorite = newFavorite)
            viewModelScope.launch {
                setFavoriteUseCase(media.id, newFavorite)
            }
        }
    }

    fun setSleepTimer(minutes: Long) {
        if (minutes > 0) {
            _sleepTimerRemaining.value = minutes * 60 * 1000
            startSleepTimerCountdown()
        } else {
            _sleepTimerRemaining.value = null
        }
    }

    private fun startSleepTimerCountdown() {
        viewModelScope.launch {
            while (_sleepTimerRemaining.value != null && _sleepTimerRemaining.value!! > 0) {
                kotlinx.coroutines.delay(1000)
                _sleepTimerRemaining.value = _sleepTimerRemaining.value!! - 1000
            }
            _sleepTimerRemaining.value = null
        }
    }

    fun updateLyrics(lyrics: List<com.larkplayer.app.util.LyricLine>) {
        _lyrics.value = lyrics
    }

    private fun updateLyricPosition(position: Long) {
        val lyricsList = _lyrics.value
        if (lyricsList.isNotEmpty()) {
            var newIndex = -1
            for (i in lyricsList.indices) {
                if (lyricsList[i].timestamp <= position) {
                    newIndex = i
                } else {
                    break
                }
            }
            if (newIndex != _currentLyricIndex.value) {
                _currentLyricIndex.value = newIndex
            }
        }
    }

    fun nextTrack() {
        if (_queueIndex.value < _queue.value.lastIndex) {
            _queueIndex.value++
            val nextMedia = _queue.value[_queueIndex.value]
            _currentMedia.value = nextMedia
        }
    }

    fun previousTrack() {
        if (_queueIndex.value > 0) {
            _queueIndex.value--
            val prevMedia = _queue.value[_queueIndex.value]
            _currentMedia.value = prevMedia
        }
    }

    fun removeFromQueue(index: Int) {
        val currentQueue = _queue.value.toMutableList()
        if (index in currentQueue.indices) {
            currentQueue.removeAt(index)
            _queue.value = currentQueue
            if (_queueIndex.value >= currentQueue.size) {
                _queueIndex.value = currentQueue.lastIndex
            }
            if (_queueIndex.value >= 0) {
                _currentMedia.value = currentQueue[_queueIndex.value]
            }
        }
    }

    fun clearQueue() {
        _queue.value = emptyList()
        _queueIndex.value = 0
    }

    override fun onCleared() {
        super.onCleared()
        // Save bookmark for current media
        _currentMedia.value?.let { media ->
            viewModelScope.launch {
                updateBookmarkUseCase(media.id, _currentPosition.value)
            }
        }
    }
}