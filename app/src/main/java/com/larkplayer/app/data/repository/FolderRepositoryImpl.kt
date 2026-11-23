package com.larkplayer.app.data.repository

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import com.larkplayer.app.data.model.Folder
import com.larkplayer.app.data.source.local.AppDatabase
import com.larkplayer.app.data.source.local.FolderDao
import com.larkplayer.app.domain.repository.FolderRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FolderRepositoryImpl @Inject constructor(
    private val context: Context,
    private val database: AppDatabase
) : FolderRepository {

    private val folderDao: FolderDao = database.folderDao()

    override suspend fun scanFolders() = withContext(Dispatchers.IO) {
        // Scan audio folders
        val audioFolders = mutableMapOf<String, Folder>()
        val audioProjection = arrayOf(MediaStore.Audio.Media.DATA)
        val audioUri = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        context.contentResolver.query(audioUri, audioProjection, "${MediaStore.Audio.Media.IS_MUSIC} != 0", null, null)?.use { cursor ->
            while (cursor.moveToNext()) {
                val path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
                val folderPath = path.substringBeforeLast("/")
                updateFolderMap(audioFolders, folderPath, true)
            }
        }

        // Scan video folders
        val videoProjection = arrayOf(MediaStore.Video.Media.DATA)
        val videoUri = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        context.contentResolver.query(videoUri, videoProjection, null, null, null)?.use { cursor ->
            while (cursor.moveToNext()) {
                val path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA))
                val folderPath = path.substringBeforeLast("/")
                updateFolderMap(audioFolders, folderPath, false)
            }
        }

        // Insert/update folders
        folderDao.insertAll(audioFolders.values.toList())
    }

    private fun updateFolderMap(map: MutableMap<String, Folder>, folderPath: String, isAudio: Boolean) {
        val existing = map[folderPath]
        val name = folderPath.split("/").lastOrNull() ?: folderPath
        if (existing != null) {
            map[folderPath] = existing.copy(mediaCount = existing.mediaCount + 1)
        } else {
            map[folderPath] = Folder(
                path = folderPath,
                name = name,
                mediaCount = 1,
                parentPath = folderPath.substringBeforeLast("/").takeIf { it.isNotBlank() }
            )
        }
    }

    override fun getAllFolders(): Flow<List<Folder>> = folderDao.getAllFlow()

    override fun getSubFolders(parentPath: String): Flow<List<Folder>> = folderDao.getSubFoldersFlow(parentPath)

    override suspend fun getByPath(path: String): Folder? = folderDao.getByPathSuspend(path)
}