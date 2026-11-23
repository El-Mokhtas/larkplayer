package com.larkplayer.app.util

import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Environment
import androidx.core.content.FileProvider
import com.google.gson.Gson
import com.larkplayer.app.data.model.MediaItem
import java.io.File
import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.Enumeration

object NetworkUtils {

    fun getLocalIpAddress(): String? {
        try {
            val interfaces: Enumeration<NetworkInterface> = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                val addresses: Enumeration<Inet4Address> = networkInterface.inetAddresses as Enumeration<Inet4Address>
                while (addresses.hasMoreElements()) {
                    val address = addresses.nextElement()
                    if (!address.isLoopbackAddress && address is Inet4Address) {
                        return address.hostAddress
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun getWifiIpAddress(context: Context): String? {
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiInfo: WifiInfo = wifiManager.connectionInfo
        val ip = wifiInfo.ipAddress
        return String.format("%d.%d.%d.%d", ip and 0xFF, ip shr 8 and 0xFF, ip shr 16 and 0xFF, ip shr 24 and 0xFF)
    }

    fun getMediaFiles(context: Context): List<MediaFileInfo> {
        val files = mutableListOf<MediaFileInfo>()
        val projection = arrayOf(
            android.provider.MediaStore.MediaColumns._ID,
            android.provider.MediaStore.MediaColumns.TITLE,
            android.provider.MediaStore.MediaColumns.DISPLAY_NAME,
            android.provider.MediaStore.MediaColumns.MIME_TYPE,
            android.provider.MediaStore.MediaColumns.SIZE,
            android.provider.MediaStore.MediaColumns.DATA,
            android.provider.MediaStore.MediaColumns.DATE_ADDED
        )

        // Audio
        context.contentResolver.query(
            android.provider.MediaStore.Audio.Media.getContentUri(android.provider.MediaStore.VOLUME_EXTERNAL_PRIMARY),
            projection,
            "${android.provider.MediaStore.Audio.Media.IS_MUSIC} != 0",
            null,
            null
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                files.add(cursorToFileInfo(cursor))
            }
        }

        // Video
        context.contentResolver.query(
            android.provider.MediaStore.Video.Media.getContentUri(android.provider.MediaStore.VOLUME_EXTERNAL_PRIMARY),
            projection,
            null,
            null,
            null
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                files.add(cursorToFileInfo(cursor))
            }
        }

        return files
    }

    private fun cursorToFileInfo(cursor: android.database.Cursor): MediaFileInfo {
        val getString = { col: String -> cursor.getString(cursor.getColumnIndexOrThrow(col)) }
        val getLong = { col: String -> cursor.getLong(cursor.getColumnIndexOrThrow(col)) }

        return MediaFileInfo(
            id = getLong(android.provider.MediaStore.MediaColumns._ID),
            title = getString(android.provider.MediaStore.MediaColumns.TITLE),
            displayName = getString(android.provider.MediaStore.MediaColumns.DISPLAY_NAME),
            mimeType = getString(android.provider.MediaStore.MediaColumns.MIME_TYPE),
            size = getLong(android.provider.MediaStore.MediaColumns.SIZE),
            path = getString(android.provider.MediaStore.MediaColumns.DATA) ?: "",
            dateAdded = getLong(android.provider.MediaStore.MediaColumns.DATE_ADDED)
        )
    }

    fun filesToJson(files: List<MediaFileInfo>): String {
        return Gson().toJson(files)
    }

    fun scanMediaFile(context: Context, filePath: String) {
        MediaScannerConnection.scanFile(context, arrayOf(filePath), null) { path, uri ->
            // Scanned
        }
    }

    fun shareFile(context: Context, file: File): Uri? {
        return try {
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    data class MediaFileInfo(
        val id: Long,
        val title: String,
        val displayName: String,
        val mimeType: String,
        val size: Long,
        val path: String,
        val dateAdded: Long
    )
}