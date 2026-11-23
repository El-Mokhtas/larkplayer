package com.larkplayer.app.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.MediaMetadata
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata as Media3MediaMetadata
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.MediaMetadataRetriever
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.exoplayer.upstream.DefaultDataSource
import androidx.media3.exoplayer.upstream.DefaultHttpDataSource
import androidx.media3.session.MediaController
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionCommandGroup
import androidx.media3.session.SessionPositionInfo
import androidx.media3.session.SessionResult
import com.google.common.util.concurrent.ListenableFuture
import com.larkplayer.app.LarkPlayerApplication
import com.larkplayer.app.R
import com.larkplayer.app.data.model.MediaItem
import com.larkplayer.app.data.model.MediaType
import com.larkplayer.app.domain.repository.EqualizerRepository
import com.larkplayer.app.domain.usecase.IncrementPlayCountUseCase
import com.larkplayer.app.domain.usecase.UpdateBookmarkUseCase
import com.larkplayer.app.presentation.ui.MainActivity
import com.larkplayer.app.util.LyricsParser
import com.larkplayer.app.util.MediaNotificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@UnstableApi
@Singleton
class MediaPlaybackService : MediaSessionService() {

    private lateinit var exoPlayer: ExoPlayer
    private lateinit var mediaSession: MediaSession
    private lateinit var mediaNotificationManager: MediaNotificationManager
    private var currentMediaItem: MediaItem? = null
    private var currentPlaylist: List<MediaItem> = emptyList()
    private var currentIndex = 0
    private var repeatMode = Player.REPEAT_MODE_OFF
    private var shuffleMode = Player.SHUFFLE_MODE_OFF
    private var sleepTimerJob: Job? = null
    private var playbackSpeed = 1.0f

    private val scope = CoroutineScope(Dispatchers.Main)
    private val handler = Handler(Looper.getMainLooper())

    // Use cases (will be injected via Hilt)
    @Inject
    lateinit var incrementPlayCountUseCase: IncrementPlayCountUseCase
    @Inject
    lateinit var updateBookmarkUseCase: UpdateBookmarkUseCase
    @Inject
    lateinit var equalizerRepository: EqualizerRepository

    override fun onCreate() {
        super.onCreate()
        // Inject dependencies
        (applicationContext as LarkPlayerApplication).applicationContext
            .let { (it as dagger.hilt.android.HiltAndroidApp).hiltEntryPoint }.let {
                // Note: Actual injection happens via Hilt's ServiceEntryPoint
            }

        initializePlayer()
        initializeMediaSession()
        mediaNotificationManager = MediaNotificationManager(this)
    }

    private fun initializePlayer() {
        exoPlayer = ExoPlayer.Builder(this)
            .setHandleAudioBecomingNoisy(true)
            .build()

        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(@Player.State state: Int) {
                updateNotification()
                when (state) {
                    Player.STATE_ENDED -> handlePlaybackEnded()
                    Player.STATE_READY -> {
                        if (exoPlayer.playWhenReady) {
                            startSleepTimerIfNeeded()
                        }
                    }
                }
            }

            override fun onPositionDiscontinuity(reason: Int) {
                updateNotification()
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updateNotification()
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                currentMediaItem = mediaItem
                updateNotification()
                fetchLyricsIfAvailable()
            }
        })
    }

    private fun initializeMediaSession() {
        val sessionBuilder = MediaSession.Builder(this, exoPlayer)
            .setId("LarkPlayerSession")
            .setSessionCommandGroup(SessionCommandGroup.DEFAULT)

        mediaSession = sessionBuilder.build()
        mediaSession.setMediaNotificationProvider(MediaNotificationProvider())
    }

    inner class MediaNotificationProvider : MediaSession.MediaNotificationProvider {
        override fun createNotification(
            mediaSession: MediaSession,
            controller: MediaController
        ): Notification {
            return mediaNotificationManager.createNotification(controller)
        }

        override fun updateNotification(
            notification: Notification,
            mediaSession: MediaSession,
            controller: MediaController
        ) {
            mediaNotificationManager.updateNotification(notification, controller)
        }
    }

    override fun onGetSession(controllerInfo: MediaController.ControllerInfo): MediaSession {
        return mediaSession
    }

    // Playback control methods
    fun playMedia(mediaItem: MediaItem, playlist: List<MediaItem> = emptyList(), startIndex: Int = 0) {
        currentMediaItem = mediaItem
        currentPlaylist = if (playlist.isNotEmpty()) playlist else listOf(mediaItem)
        currentIndex = startIndex.coerceIn(0, currentPlaylist.lastIndex)

        val media3Item = toMedia3Item(mediaItem)
        exoPlayer.setMediaItem(media3Item)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true

        // Update play count
        scope.launch { incrementPlayCountUseCase(mediaItem.id) }
    }

    fun playMediaAtIndex(index: Int) {
        if (index in currentPlaylist.indices) {
            currentIndex = index
            val item = currentPlaylist[index]
            playMedia(item, currentPlaylist, index)
        }
    }

    fun playNext() {
        if (currentPlaylist.isNotEmpty()) {
            var nextIndex = currentIndex + 1
            if (nextIndex >= currentPlaylist.size) {
                if (repeatMode == Player.REPEAT_MODE_ALL) {
                    nextIndex = 0
                } else {
                    return
                }
            }
            playMediaAtIndex(nextIndex)
        }
    }

    fun playPrevious() {
        if (currentPlaylist.isNotEmpty()) {
            var prevIndex = currentIndex - 1
            if (prevIndex < 0) {
                if (repeatMode == Player.REPEAT_MODE_ALL) {
                    prevIndex = currentPlaylist.lastIndex
                } else {
                    return
                }
            }
            playMediaAtIndex(prevIndex)
        }
    }

    fun seekTo(position: Long) {
        exoPlayer.seekTo(position)
        saveBookmark()
    }

    fun setPlaybackSpeed(speed: Float) {
        playbackSpeed = speed.coerceIn(0.5f, 2.0f)
        exoPlayer.playbackParameters = PlaybackParameters(playbackSpeed)
    }

    fun setRepeatMode(mode: Int) {
        repeatMode = mode
        exoPlayer.repeatMode = mode
    }

    fun setShuffleMode(mode: Int) {
        shuffleMode = mode
        exoPlayer.shuffleMode = mode
    }

    fun togglePlayPause() {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        } else {
            exoPlayer.playWhenReady = true
        }
    }

    fun setSleepTimer(minutes: Long) {
        sleepTimerJob?.cancel()
        if (minutes > 0) {
            sleepTimerJob = scope.launch {
                kotlinx.coroutines.delay(minutes * 60 * 1000)
                handler.post { pause() }
            }
        }
    }

    private fun startSleepTimerIfNeeded() {
        // Sleep timer logic would be restored from preferences
    }

    private fun handlePlaybackEnded() {
        when (repeatMode) {
            Player.REPEAT_MODE_ONE -> exoPlayer.seekTo(0)
            Player.REPEAT_MODE_ALL -> playNext()
            else -> {
                if (currentIndex < currentPlaylist.lastIndex) {
                    playNext()
                } else {
                    pause()
                }
            }
        }
    }

    private fun saveBookmark() {
        currentMediaItem?.let { item ->
            scope.launch { updateBookmarkUseCase(item.id, exoPlayer.currentPosition) }
        }
    }

    private fun fetchLyricsIfAvailable() {
        currentMediaItem?.lyricsPath?.let { path =>
            scope.launch(Dispatchers.IO) {
                val lyrics = LyricsParser.parseLyricsFile(this@MediaPlaybackService, path)
                // Notify UI via callback or LiveData
            }
        }
    }

    private fun updateNotification() {
        mediaSession.setMediaNotificationProvider(MediaNotificationProvider())
    }

    private fun toMedia3Item(item: MediaItem): MediaItem {
        val mediaItem = MediaItem.fromUri(item.data)
            .setMediaId(item.id.toString())
            .setMediaMetadata(
                Media3MediaMetadata.Builder()
                    .setTitle(item.title)
                    .setArtist(item.artist ?: "")
                    .setAlbumTitle(item.album ?: "")
                    .setArtworkUri(item.artworkUri?.let { Uri.parse(it) })
                    .setDuration(item.duration)
                    .build()
            )
        return mediaItem
    }

    override fun onDestroy() {
        sleepTimerJob?.cancel()
        exoPlayer.release()
        mediaSession.release()
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "media_playback"
        const val NOTIFICATION_ID = 1
    }
}