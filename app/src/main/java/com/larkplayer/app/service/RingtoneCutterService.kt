package com.larkplayer.app.service

import android.app.Service
import android.content.ContentValues
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Environment
import android.os.IBinder
import android.provider.MediaStore
import android.util.Log
import com.larkplayer.app.data.model.MediaItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class RingtoneCutterService : Service() {

    private val scope = CoroutineScope(Dispatchers.IO)

    companion object {
        const val ACTION_CREATE_RINGTONE = "com.larkplayer.app.CREATE_RINGTONE"
        const val EXTRA_SOURCE_PATH = "source_path"
        const val EXTRA_START_MS = "start_ms"
        const val EXTRA_END_MS = "end_ms"
        const val EXTRA_TITLE = "title"
        const val EXTRA_TYPE = "type" // ringtone, notification, alarm
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.action?.let { action ->
            when (action) {
                ACTION_CREATE_RINGTONE -> {
                    val sourcePath = intent.getStringExtra(EXTRA_SOURCE_PATH) ?: ""
                    val startMs = intent.getLongExtra(EXTRA_START_MS, 0)
                    val endMs = intent.getLongExtra(EXTRA_END_MS, 0)
                    val title = intent.getStringExtra(EXTRA_TITLE) ?: "Ringtone"
                    val type = intent.getStringExtra(EXTRA_TYPE) ?: "ringtone"

                    createRingtone(sourcePath, startMs, endMs, title, type)
                }
            }
        }
        return START_STICKY
    }

    private fun createRingtone(sourcePath: String, startMs: Long, endMs: Long, title: String, type: String) {
        scope.launch {
            try {
                val sourceFile = File(sourcePath)
                if (!sourceFile.exists()) {
                    Log.e("RingtoneCutter", "Source file not found: $sourcePath")
                    return@launch
                }

                // Create output file
                val outputDir = getExternalFilesDir(Environment.DIRECTORY_RINGTONES)
                val outputFile = File(outputDir, "${title}_${System.currentTimeMillis()}.mp3")

                // Use MediaMetadataRetriever to extract audio segment
                extractAudioSegment(sourceFile, outputFile, startMs, endMs)

                // Save to MediaStore
                saveToMediaStore(outputFile, title, type)

                Log.d("RingtoneCutter", "Ringtone created: ${outputFile.absolutePath}")
            } catch (e: Exception) {
                Log.e("RingtoneCutter", "Failed to create ringtone", e)
            }
        }
    }

    private fun extractAudioSegment(source: File, output: File, startMs: Long, endMs: Long) {
        // This is a simplified implementation
        // In production, use MediaExtractor + MediaMuxer for precise cutting
        // Or use FFmpeg for more reliable audio processing

        // For now, copy the file (placeholder)
        source.copyTo(output)
    }

    private fun saveToMediaStore(file: File, title: String, type: String) {
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.TITLE, title)
            put(MediaStore.MediaColumns.DISPLAY_NAME, file.name)
            put(MediaStore.MediaColumns.MIME_TYPE, "audio/mpeg")
            put(MediaStore.MediaColumns.SIZE, file.length())
            put(MediaStore.MediaColumns.DATA, file.absolutePath)
            put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis() / 1000)
            put(MediaStore.MediaColumns.DATE_MODIFIED, System.currentTimeMillis() / 1000)

            when (type) {
                "ringtone" -> {
                    put(MediaStore.Audio.Media.IS_RINGTONE, true)
                    put(MediaStore.Audio.Media.IS_NOTIFICATION, false)
                    put(MediaStore.Audio.Media.IS_ALARM, false)
                    put(MediaStore.Audio.Media.IS_MUSIC, false)
                }
                "notification" -> {
                    put(MediaStore.Audio.Media.IS_RINGTONE, false)
                    put(MediaStore.Audio.Media.IS_NOTIFICATION, true)
                    put(MediaStore.Audio.Media.IS_ALARM, false)
                    put(MediaStore.Audio.Media.IS_MUSIC, false)
                }
                "alarm" -> {
                    put(MediaStore.Audio.Media.IS_RINGTONE, false)
                    put(MediaStore.Audio.Media.IS_NOTIFICATION, false)
                    put(MediaStore.Audio.Media.IS_ALARM, true)
                    put(MediaStore.Audio.Media.IS_MUSIC, false)
                }
            }
        }

        contentResolver.insert(MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY), values)
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}