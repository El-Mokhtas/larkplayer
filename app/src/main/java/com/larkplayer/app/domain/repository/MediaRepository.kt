package com.larkplayer.app.domain.repository

import androidx.paging.Pager
import com.larkplayer.app.data.model.MediaItem
import com.larkplayer.app.data.model.MediaType
import kotlinx.coroutines.flow.Flow

interface MediaRepository {

    suspend fun scanMedia(): Int

    fun getAllAudio(): Flow<List<MediaItem>>

    fun getAllVideo(): Flow<List<MediaItem>>

    fun getAudioPaging(): Pager<Int, MediaItem>

    fun getVideoPaging(): Pager<Int, MediaItem>

    fun getFavoritesAudio(): Flow<List<MediaItem>>

    fun getFavoritesVideo(): Flow<List<MediaItem>>

    fun getRecentlyPlayedAudio(limit: Int): Flow<List<MediaItem>>

    fun getRecentlyPlayedVideo(limit: Int): Flow<List<MediaItem>>

    fun getRecentlyAddedAudio(limit: Int): Flow<List<MediaItem>>

    fun getRecentlyAddedVideo(limit: Int): Flow<List<MediaItem>>

    fun searchAudio(query: String): Flow<List<MediaItem>>

    fun searchVideo(query: String): Flow<List<MediaItem>>

    fun searchAll(query: String): Flow<List<MediaItem>>

    fun getByFolder(folderPath: String, type: MediaType): Flow<List<MediaItem>>

    fun getByAlbum(albumId: Long, type: MediaType): Flow<List<MediaItem>>

    fun getByArtist(artistId: Long, type: MediaType): Flow<List<MediaItem>>

    suspend fun getById(id: Long): MediaItem?

    suspend fun getByPath(path: String): MediaItem?

    suspend fun setFavorite(id: Long, favorite: Boolean)

    suspend fun incrementPlayCount(id: Long)

    suspend fun updateBookmark(id: Long, position: Long)

    suspend fun updateLyricsPath(id: Long, path: String?)

    suspend fun countAudio(): Int

    suspend fun countVideo(): Int
}