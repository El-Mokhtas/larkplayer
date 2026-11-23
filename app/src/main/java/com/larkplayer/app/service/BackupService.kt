package com.larkplayer.app.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import com.larkplayer.app.data.model.MediaItem
import com.larkplayer.app.data.model.Playlist
import com.larkplayer.app.data.source.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File as JavaFile
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.concurrent.Executors

class BackupService : Service() {

    private var driveService: Drive? = null
    private val scope = CoroutineScope(Dispatchers.IO)
    private val executor = Executors.newSingleThreadExecutor()

    companion object {
        const val ACTION_BACKUP = "com.larkplayer.app.BACKUP"
        const val ACTION_RESTORE = "com.larkplayer.app.RESTORE"
        const val EXTRA_ACCOUNT_NAME = "account_name"
        const val BACKUP_FOLDER_NAME = "LarkPlayer_Backup"
        const val PLAYLISTS_FILE = "playlists.json"
        const val SETTINGS_FILE = "settings.json"
        const val FAVORITES_FILE = "favorites.json"
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.action?.let { action ->
            val accountName = intent.getStringExtra(EXTRA_ACCOUNT_NAME)
            when (action) {
                ACTION_BACKUP -> backupToDrive(accountName)
                ACTION_RESTORE -> restoreFromDrive(accountName)
            }
        }
        return START_STICKY
    }

    private fun backupToDrive(accountName: String?) {
        scope.launch(Dispatchers.IO) {
            try {
                initializeDriveService(accountName)
                val folderId = getOrCreateBackupFolder()

                // Backup playlists
                val playlistsJson = exportPlaylists()
                uploadFile(folderId, PLAYLISTS_FILE, playlistsJson.toByteArray(), "application/json")

                // Backup favorites
                val favoritesJson = exportFavorites()
                uploadFile(folderId, FAVORITES_FILE, favoritesJson.toByteArray(), "application/json")

                // Backup settings
                val settingsJson = exportSettings()
                uploadFile(folderId, SETTINGS_FILE, settingsJson.toByteArray(), "application/json")

                Log.d("BackupService", "Backup completed successfully")
            } catch (e: Exception) {
                Log.e("BackupService", "Backup failed", e)
            }
        }
    }

    private fun restoreFromDrive(accountName: String?) {
        scope.launch(Dispatchers.IO) {
            try {
                initializeDriveService(accountName)
                val folderId = findBackupFolder()
                folderId?.let {
                    // Restore playlists
                    val playlistsFile = findFileInFolder(it, PLAYLISTS_FILE)
                    playlistsFile?.let { downloadAndImportPlaylists(it.id) }

                    // Restore favorites
                    val favoritesFile = findFileInFolder(it, FAVORITES_FILE)
                    favoritesFile?.let { downloadAndImportFavorites(it.id) }

                    // Restore settings
                    val settingsFile = findFileInFolder(it, SETTINGS_FILE)
                    settingsFile?.let { downloadAndImportSettings(it.id) }

                    Log.d("BackupService", "Restore completed successfully")
                }
            } catch (e: Exception) {
                Log.e("BackupService", "Restore failed", e)
            }
        }
    }

    private fun initializeDriveService(accountName: String?) {
        // Use GoogleAccountCredential for authentication
        val credential = GoogleAccountCredential.usingOAuth2(
            this,
            listOf(DriveScopes.DRIVE_FILE)
        )
        accountName?.let { credential.selectedAccountName = it }

        driveService = Drive.Builder(
            GoogleNetHttpTransport.newTrustedTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        ).setApplicationName("Lark Player").build()
    }

    private fun getOrCreateBackupFolder(): String {
        val folder = findBackupFolder() ?: createBackupFolder()
        return folder!!.id
    }

    private fun findBackupFolder(): File? {
        val query = "name='$BACKUP_FOLDER_NAME' and mimeType='application/vnd.google-apps.folder' and trashed=false"
        val result = driveService?.files()?.list()
            ?.setQ(query)
            ?.setSpaces("drive")
            ?.setFields("files(id, name)")
            ?.execute()
        return result?.files?.firstOrNull()
    }

    private fun createBackupFolder(): File? {
        val folderMetadata = File().apply {
            name = BACKUP_FOLDER_NAME
            mimeType = "application/vnd.google-apps.folder"
        }
        return driveService?.files()?.create(folderMetadata)?.setFields("id")?.execute()
    }

    private fun uploadFile(folderId: String, fileName: String, content: ByteArray, mimeType: String) {
        val fileMetadata = File().apply {
            name = fileName
            parents = listOf(folderId)
        }
        val mediaContent = com.google.api.client.http.ByteArrayContent(mimeType, content)
        driveService?.files()?.create(fileMetadata, mediaContent)?.setFields("id")?.execute()
    }

    private fun findFileInFolder(folderId: String, fileName: String): File? {
        val query = "name='$fileName' and '$folderId' in parents and trashed=false"
        val result = driveService?.files()?.list()
            ?.setQ(query)
            ?.setSpaces("drive")
            ?.setFields("files(id, name)")
            ?.execute()
        return result?.files?.firstOrNull()
    }

    private fun downloadFile(fileId: String): ByteArray? {
        return try {
            val outputStream = ByteArrayOutputStream()
            driveService?.files()?.get(fileId)?.executeMediaAndDownloadTo(outputStream)
            outputStream.toByteArray()
        } catch (e: Exception) {
            Log.e("BackupService", "Download failed", e)
            null
        }
    }

    private fun exportPlaylists(): String {
        // Query playlists from database and convert to JSON
        return "[]"
    }

    private fun exportFavorites(): String {
        // Query favorites from database and convert to JSON
        return "[]"
    }

    private fun exportSettings(): String {
        // Export settings to JSON
        return "{}"
    }

    private fun downloadAndImportPlaylists(fileId: String) {
        downloadFile(fileId)?.let { data ->
            val json = String(data)
            // Parse and import playlists
        }
    }

    private fun downloadAndImportFavorites(fileId: String) {
        downloadFile(fileId)?.let { data ->
            val json = String(data)
            // Parse and import favorites
        }
    }

    private fun downloadAndImportSettings(fileId: String) {
        downloadFile(fileId)?.let { data ->
            val json = String(data)
            // Parse and import settings
        }
    }

    override fun onDestroy() {
        executor.shutdown()
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}