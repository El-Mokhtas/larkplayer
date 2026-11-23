package com.larkplayer.app.util

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.MediaSession
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.larkplayer.app.LarkPlayerApplication
import com.larkplayer.app.R
import com.larkplayer.app.presentation.ui.MainActivity
import com.larkplayer.app.service.MediaPlaybackService

class MediaNotificationManager(private val context: Context) {

    private var notificationManager: PlayerNotificationManager? = null
    private val channelId = MediaPlaybackService.NOTIFICATION_CHANNEL_ID
    private val notificationId = MediaPlaybackService.NOTIFICATION_ID

    init {
        createNotificationChannel()
        initializeNotificationManager()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                channelId,
                "Media Playback",
                android.app.NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifications for media playback"
                setSound(null, null)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun initializeNotificationManager() {
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val mediaDescriptionAdapter = PlayerNotificationManager.MediaDescriptionAdapter { player ->
            val metadata = player.currentMediaItem?.mediaMetadata
            val title = metadata?.title ?: "Unknown"
            val artist = metadata?.artist ?: "Unknown Artist"
            val artworkUri = metadata?.artworkUri

            PlayerNotificationManager.MediaDescription(
                title = title,
                subtitle = artist,
                description = "",
                icon = artworkUri,
                contentIntent = pendingIntent
            )
        }

        val notificationListener = object : PlayerNotificationManager.NotificationListener {
            override fun onNotificationStarted(notificationId: Int, notification: Notification) {
                // Start foreground service
                if (context is MediaPlaybackService) {
                    (context as MediaPlaybackService).startForeground(notificationId, notification)
                }
            }

            override fun onNotificationCancelled(notificationId: Int) {
                // Stop foreground service
                if (context is MediaPlaybackService) {
                    (context as MediaPlaybackService).stopForeground(true)
                }
            }
        }

        notificationManager = PlayerNotificationManager.createWithNotificationChannel(
            context,
            channelId,
            R.string.app_name,
            notificationId,
            mediaDescriptionAdapter,
            notificationListener
        ).apply {
            setSmallIcon(R.drawable.ic_notification_music)
            setColor(ContextCompat.getColor(context, R.color.primary))
            setColorized(true)
            setUseChronometer(true)
            setUseNextAction(true)
            setUsePreviousAction(true)
            setUsePlayPauseActions(true)
            setUseStopAction(false)
            setFastForwardIncrementMs(10000)
            setRewindIncrementMs(10000)
            setShowDismissAffordance(true)
        }
    }

    fun createNotification(controller: MediaController): Notification {
        return notificationManager?.createNotification(controller) ?: NotificationCompat.Builder(context, channelId).build()
    }

    fun updateNotification(notification: Notification, controller: MediaController) {
        notificationManager?.updateNotification(notification, controller)
    }

    fun setPlayer(player: Player) {
        notificationManager?.setPlayer(player)
    }

    fun setSmallIcon(iconRes: Int) {
        notificationManager?.setSmallIcon(iconRes)
    }

    fun setUseNextAction(use: Boolean) {
        notificationManager?.setUseNextAction(use)
    }

    fun setUsePreviousAction(use: Boolean) {
        notificationManager?.setUsePreviousAction(use)
    }
}