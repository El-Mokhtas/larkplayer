package com.larkplayer.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.media3.session.MediaButtonReceiver

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_MY_PACKAGE_REPLACED -> {
                // Restore playback state if needed
                // This would typically start the MediaPlaybackService
                // and restore the last playlist/position
            }
        }
    }
}