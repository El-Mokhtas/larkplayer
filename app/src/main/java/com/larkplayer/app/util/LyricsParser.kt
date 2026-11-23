package com.larkplayer.app.util

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.regex.Pattern

data class LyricLine(
    val timestamp: Long, // in milliseconds
    val text: String
)

object LyricsParser {

    // LRC format: [mm:ss.xx]text or [mm:ss:xx]text
    private val TIMESTAMP_PATTERN = Pattern.compile("\\[(\\d{2}):(\\d{2})[.:](\\d{2,3})\\](.*)")
    // Also handle [mm:ss] format
    private val SIMPLE_TIMESTAMP_PATTERN = Pattern.compile("\\[(\\d{2}):(\\d{2})\\](.*)")

    @Suppress("UNUSED_PARAMETER")
    fun parseLyricsFile(context: Context, path: String): List<LyricLine> = withContext(Dispatchers.IO) {
        val lines = mutableListOf<LyricLine>()

        try {
            val uri = Uri.parse(path)
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: context.assets.open(path)
                ?: java.io.FileInputStream(path)

            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                reader.forEachLine { line ->
                    parseLine(line)?.let { lines.add(it) }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        lines.sortedBy { it.timestamp }
    }

    private fun parseLine(line: String): LyricLine? {
        var matcher = TIMESTAMP_PATTERN.matcher(line)
        if (matcher.matches()) {
            val minutes = matcher.group(1).toLong()
            val seconds = matcher.group(2).toLong()
            val centiseconds = matcher.group(3).toLong()
            val text = matcher.group(4).trim()

            val timestamp = (minutes * 60 + seconds) * 1000 + centiseconds * 10
            return LyricLine(timestamp, text)
        }

        matcher = SIMPLE_TIMESTAMP_PATTERN.matcher(line)
        if (matcher.matches()) {
            val minutes = matcher.group(1).toLong()
            val seconds = matcher.group(2).toLong()
            val text = matcher.group(3).trim()

            val timestamp = (minutes * 60 + seconds) * 1000
            return LyricLine(timestamp, text)
        }

        return null
    }

    fun findCurrentLyric(lyrics: List<LyricLine>, position: Long): LyricLine? {
        if (lyrics.isEmpty()) return null

        var current: LyricLine? = null
        for (lyric in lyrics) {
            if (lyric.timestamp <= position) {
                current = lyric
            } else {
                break
            }
        }
        return current
    }

    fun findNextLyric(lyrics: List<LyricLine>, position: Long): LyricLine? {
        for (lyric in lyrics) {
            if (lyric.timestamp > position) {
                return lyric
            }
        }
        return null
    }

    fun getProgressToNextLyric(lyrics: List<LyricLine>, position: Long): Float {
        val current = findCurrentLyric(lyrics, position)
        val next = findNextLyric(lyrics, position)

        if (current == null || next == null) return 0f

        val currentTime = current.timestamp
        val nextTime = next.timestamp
        val elapsed = position - currentTime
        val total = nextTime - currentTime

        return if (total > 0) {
            (elapsed.toFloat() / total).coerceIn(0f, 1f)
        } else 0f
    }
}