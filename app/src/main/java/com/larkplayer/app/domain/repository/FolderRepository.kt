package com.larkplayer.app.domain.repository

import com.larkplayer.app.data.model.Folder
import kotlinx.coroutines.flow.Flow

interface FolderRepository {

    suspend fun scanFolders()

    fun getAllFolders(): Flow<List<Folder>>

    fun getSubFolders(parentPath: String): Flow<List<Folder>>

    suspend fun getByPath(path: String): Folder?
}