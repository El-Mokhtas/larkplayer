package com.larkplayer.app.data.source.local

import androidx.lifecycle.LiveData
import androidx.room.*
import com.larkplayer.app.data.model.Folder
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(folders: List<Folder>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(folder: Folder)

    @Update
    suspend fun update(folder: Folder)

    @Delete
    suspend fun delete(folder: Folder)

    @Query("DELETE FROM folders")
    suspend fun clearAll()

    @Query("SELECT * FROM folders WHERE isHidden = 0 ORDER BY name COLLATE NOCASE ASC")
    fun getAll(): LiveData<List<Folder>>

    @Query("SELECT * FROM folders WHERE isHidden = 0 ORDER BY name COLLATE NOCASE ASC")
    fun getAllFlow(): Flow<List<Folder>>

    @Query("SELECT * FROM folders WHERE parentPath = :parentPath AND isHidden = 0 ORDER BY name COLLATE NOCASE ASC")
    fun getByParent(parentPath: String): LiveData<List<Folder>>

    @Query("SELECT * FROM folders WHERE parentPath = :parentPath AND isHidden = 0 ORDER BY name COLLATE NOCASE ASC")
    fun getByParentFlow(parentPath: String): Flow<List<Folder>>

    @Query("SELECT * FROM folders WHERE path = :path")
    fun getByPath(path: String): LiveData<Folder?>

    @Query("SELECT * FROM folders WHERE path = :path")
    suspend fun getByPathSuspend(path: String): Folder?

    @Query("SELECT * FROM folders WHERE path LIKE :path || '/%' AND isHidden = 0 ORDER BY name COLLATE NOCASE ASC")
    fun getSubFolders(path: String): LiveData<List<Folder>>

    @Query("SELECT * FROM folders WHERE path LIKE :path || '/%' AND isHidden = 0 ORDER BY name COLLATE NOCASE ASC")
    fun getSubFoldersFlow(path: String): Flow<List<Folder>>

    @Query("SELECT COUNT(*) FROM folders WHERE isHidden = 0")
    suspend fun count(): Int
}