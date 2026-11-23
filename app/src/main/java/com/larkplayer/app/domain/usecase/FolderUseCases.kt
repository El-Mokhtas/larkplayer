package com.larkplayer.app.domain.usecase

import com.larkplayer.app.data.model.Folder
import com.larkplayer.app.domain.repository.FolderRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ScanFoldersUseCase @Inject constructor(
    private val repository: FolderRepository
) {
    operator fun invoke() = repository.scanFolders()
}

class GetAllFoldersUseCase @Inject constructor(
    private val repository: FolderRepository
) {
    operator fun invoke(): Flow<List<Folder>> = repository.getAllFolders()
}

class GetSubFoldersUseCase @Inject constructor(
    private val repository: FolderRepository
) {
    operator fun invoke(parentPath: String): Flow<List<Folder>> = repository.getSubFolders(parentPath)
}

class GetFolderByPathUseCase @Inject constructor(
    private val repository: FolderRepository
) {
    operator fun invoke(path: String) = repository.getByPath(path)
}