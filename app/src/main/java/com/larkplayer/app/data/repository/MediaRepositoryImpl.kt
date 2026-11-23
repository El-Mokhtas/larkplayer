package com.larkplayer.app.data.repository

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.LiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import com.larkplayer.app.data.model.MediaItem
import com.larkplayer.app.data.model.MediaType
import com.larkplayer.app.data.source.local.AppDatabase
import com.larkplayer.app.data.source.local.MediaDao
import com.larkplayer.app.domain.repository.MediaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class MediaRepositoryImpl @Inject constructor(
    private val context: Context,
    private val database: AppDatabase
) : MediaRepository {

    private val mediaDao: MediaDao = database.mediaDao()

    override suspend fun scanMedia(): Int {
        return withContext(Dispatchers.IO) {
            val audioCount = scanAudioFiles()
            val videoCount = scanVideoFiles()
            audioCount + videoCount
        }
    }

    private fun scanAudioFiles(): Int {
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DATE_MODIFIED,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ARTIST_ID,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.GENRE,
            MediaStore.Audio.Media.COMPOSER,
            MediaStore.Audio.Media.WRITER,
            MediaStore.Audio.Media.IS_MUSIC
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val contentUri = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)

        var count = 0
        context.contentResolver.query(contentUri, projection, selection, null, "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC")?.use { cursor ->
            val items = mutableListOf<MediaItem>()
            while (cursor.moveToNext()) {
                val item = cursorToMediaItem(cursor, MediaType.AUDIO)
                items.add(item)
            }
            if (items.isNotEmpty()) {
                mediaDao.insertAll(items)
                count = items.size
            }
        }
        return count
    }

    private fun scanVideoFiles(): Int {
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.ARTIST,
            MediaStore.Video.Media.ALBUM,
            MediaStore.Video.Media.ALBUM_ARTIST,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.MIME_TYPE,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.DATE_MODIFIED,
            MediaStore.Video.Media.ALBUM_ID,
            MediaStore.Video.Media.ARTIST_ID,
            MediaStore.Video.Media.TRACK,
            MediaStore.Video.Media.YEAR
        )

        val contentUri = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)

        var count = 0
        context.contentResolver.query(contentUri, projection, null, null, "${MediaStore.Video.Media.TITLE} COLLATE NOCASE ASC")?.use { cursor ->
            val items = mutableListOf<MediaItem>()
            while (cursor.moveToNext()) {
                val item = cursorToMediaItem(cursor, MediaType.VIDEO)
                items.add(item)
            }
            if (items.isNotEmpty()) {
                mediaDao.insertAll(items)
                count = items.size
            }
        }
        return count
    }

    private fun cursorToMediaItem(cursor: Cursor, type: MediaType): MediaItem {
        val getString = { column: String -> cursor.getString(cursor.getColumnIndexOrThrow(column)) }
        val getLong = { column: String -> cursor.getLong(cursor.getColumnIndexOrThrow(column)) }
        val getInt = { column: String -> cursor.getInt(cursor.getColumnIndexOrThrow(column)) }

        val id = getLong(MediaStore.MediaColumns._ID)
        val title = getString(MediaStore.MediaColumns.TITLE) ?: ""
        val artist = getString(MediaStore.Audio.Media.ARTIST)
        val album = getString(MediaStore.Audio.Media.ALBUM)
        val albumArtist = getString(MediaStore.Audio.Media.ALBUM_ARTIST)
        val duration = getLong(MediaStore.Audio.Media.DURATION)
        val data = getString(MediaStore.MediaColumns.DATA) ?: ""
        val displayName = getString(MediaStore.MediaColumns.DISPLAY_NAME) ?: title
        val mimeType = getString(MediaStore.MediaColumns.MIME_TYPE) ?: ""
        val size = getLong(MediaStore.MediaColumns.SIZE)
        val dateAdded = getLong(MediaStore.MediaColumns.DATE_ADDED) * 1000L // Convert to milliseconds
        val dateModified = getLong(MediaStore.MediaColumns.DATE_MODIFIED) * 1000L
        val albumId = if (type == MediaType.AUDIO) getLong(MediaStore.Audio.Media.ALBUM_ID).takeIf { it > 0 } else null
        val artistId = if (type == MediaType.AUDIO) getLong(MediaStore.Audio.Media.ARTIST_ID).takeIf { it > 0 } else null
        val trackNumber = getInt(MediaStore.Audio.Media.TRACK).takeIf { it > 0 }
        val year = getInt(MediaStore.Audio.Media.YEAR).takeIf { it > 0 }
        val genre = getString(MediaStore.Audio.Media.GENRE)
        val composer = getString(MediaStore.Audio.Media.COMPOSER)
        val writer = getString(MediaStore.Audio.Media.WRITER)

        // Get folder path
        val folderPath = data.substringBeforeLast("/")

        // Try to get artwork
        var artworkUri: String? = null
        if (type == MediaType.AUDIO && albumId != null) {
            artworkUri = Uri.withAppendedPath(
                Uri.parse("content://media/external/audio/albumart"),
                albumId.toString()
            ).toString()
        }

        return MediaItem(
            id = id,
            title = title,
            artist = artist,
            album = album,
            albumArtist = albumArtist,
            duration = duration,
            data = data,
            displayName = displayName,
            mimeType = mimeType,
            size = size,
            dateAdded = dateAdded,
            dateModified = dateModified,
            mediaType = type,
            albumId = albumId,
            artistId = artistId,
            trackNumber = trackNumber,
            year = year,
            genre = genre,
            composer = composer,
            writer = writer,
            folderPath = folderPath,
            artworkUri = artworkUri
        )
    }

    override fun getAllAudio(): Flow<List<MediaItem>> = mediaDao.getAllByTypeFlow(MediaType.AUDIO)

    override fun getAllVideo(): Flow<List<MediaItem>> = mediaDao.getAllByTypeFlow(MediaType.VIDEO)

    override fun getAudioPaging(): Pager<Int, MediaItem> = Pager(
        PagingConfig(pageSize = 50, enablePlaceholders = false)
    ) { mediaDao.getAllByTypePaging(MediaType.AUDIO) }

    override fun getVideoPaging(): Pager<Int, MediaItem> = Pager(
        PagingConfig(pageSize = 50, enablePlaceholders = false)
    ) { mediaDao.getAllByTypePaging(MediaType.VIDEO) }

    override fun getFavoritesAudio(): Flow<List<MediaItem>> = mediaDao.getFavoritesByTypeFlow(MediaType.AUDIO)

    override fun getFavoritesVideo(): Flow<List<MediaItem>> = mediaDao.getFavoritesByTypeFlow(MediaType.VIDEO)

    override fun getRecentlyPlayedAudio(limit: Int): Flow<List<MediaItem>> = mediaDao.getRecentlyPlayedFlow(MediaType.AUDIO, limit)

    override fun getRecentlyPlayedVideo(limit: Int): Flow<List<MediaItem>> = mediaDao.getRecentlyPlayedFlow(MediaType.VIDEO, limit)

    override fun getRecentlyAddedAudio(limit: Int): Flow<List<MediaItem>> = mediaDao.getRecentlyAddedFlow(MediaType.AUDIO, limit)

    override fun getRecentlyAddedVideo(limit: Int): Flow<List<MediaItem>> = mediaDao.getRecentlyAddedFlow(MediaType.VIDEO, limit)

    override fun searchAudio(query: String): Flow<List<MediaItem>> = mediaDao.searchFlow("%$query%", MediaType.AUDIO)

    override fun searchVideo(query: String): Flow<List<MediaItem>> = mediaDao.searchFlow("%$query%", MediaType.VIDEO)

    override fun searchAll(query: String): Flow<List<MediaItem>> = mediaDao.searchFlow("%$query%", MediaType.AUDIO)
        .map { audio ->
            // Note: This is a simplified version. In practice, you'd want to merge both audio and video
            audio
        }

    override fun getByFolder(folderPath: String, type: MediaType): Flow<List<MediaItem>> = mediaDao.getByFolderFlow(folderPath, type)

    override fun getByAlbum(albumId: Long, type: MediaType): Flow<List<MediaItem>> = mediaDao.getByAlbumFlow(albumId, type)

    override fun getByArtist(artistId: Long, type: MediaType): Flow<List<MediaItem>> = mediaDao.getByArtistFlow(artistId, type)

    override suspend fun getById(id: Long): MediaItem? = mediaDao.getByIdSuspend(id)

    override suspend fun getByPath(path: String): MediaItem? = mediaDao.getByPath(path)

    override suspend fun setFavorite(id: Long, favorite: Boolean) = mediaDao.setFavorite(id, favorite)

    override suspend fun incrementPlayCount(id: Long) = mediaDao.incrementPlayCount(id, System.currentTimeMillis())

    override suspend fun updateBookmark(id: Long, position: Long) = mediaDao.updateBookmark(id, position)

    override suspend fun updateLyricsPath(id: Long, path: String?) = mediaDao.updateLyricsPath(id, path)

    override suspend fun countAudio(): Int = mediaDao.countByType(MediaType.AUDIO)

    override suspend fun countVideo(): Int = mediaDao.countByType(MediaType.VIDEO)
}