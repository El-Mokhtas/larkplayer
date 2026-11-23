package com.larkplayer.app.domain.usecase

import com.larkplayer.app.data.model.MediaItem
import com.larkplayer.app.data.model.MediaType
import com.larkplayer.app.domain.repository.MediaRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllAudioUseCase @Inject constructor(
    private val repository: MediaRepository
) {
    operator fun invoke(): Flow<List<MediaItem>> = repository.getAllAudio()
}

class GetAllVideoUseCase @Inject constructor(
    private val repository: MediaRepository
) {
    operator fun invoke(): Flow<List<MediaItem>> = repository.getAllVideo()
}

class GetAudioPagingUseCase @Inject constructor(
    private val repository: MediaRepository
) {
    operator fun invoke() = repository.getAudioPaging()
}

class GetVideoPagingUseCase @Inject constructor(
    private val repository: MediaRepository
) {
    operator fun invoke() = repository.getVideoPaging()
}

class GetFavoritesAudioUseCase @Inject constructor(
    private val repository: MediaRepository
) {
    operator fun invoke(): Flow<List<MediaItem>> = repository.getFavoritesAudio()
}

class GetFavoritesVideoUseCase @Inject constructor(
    private val repository: MediaRepository
) {
    operator fun invoke(): Flow<List<MediaItem>> = repository.getFavoritesVideo()
}

class GetRecentlyPlayedAudioUseCase @Inject constructor(
    private val repository: MediaRepository
) {
    operator fun invoke(limit: Int = 20): Flow<List<MediaItem>> = repository.getRecentlyPlayedAudio(limit)
}

class GetRecentlyPlayedVideoUseCase @Inject constructor(
    private val repository: MediaRepository
) {
    operator fun invoke(limit: Int = 20): Flow<List<MediaItem>> = repository.getRecentlyPlayedVideo(limit)
}

class GetRecentlyAddedAudioUseCase @Inject constructor(
    private val repository: MediaRepository
) {
    operator fun invoke(limit: Int = 20): Flow<List<MediaItem>> = repository.getRecentlyAddedAudio(limit)
}

class GetRecentlyAddedVideoUseCase @Inject constructor(
    private val repository: MediaRepository
) {
    operator fun invoke(limit: Int = 20): Flow<List<MediaItem>> = repository.getRecentlyAddedVideo(limit)
}

class SearchAudioUseCase @Inject constructor(
    private val repository: MediaRepository
) {
    operator fun invoke(query: String): Flow<List<MediaItem>> = repository.searchAudio(query)
}

class SearchVideoUseCase @Inject constructor(
    private val repository: MediaRepository
) {
    operator fun invoke(query: String): Flow<List<MediaItem>> = repository.searchVideo(query)
}

class SearchAllUseCase @Inject constructor(
    private val repository: MediaRepository
) {
    operator fun invoke(query: String): Flow<List<MediaItem>> = repository.searchAll(query)
}

class GetByFolderUseCase @Inject constructor(
    private val repository: MediaRepository
) {
    operator fun invoke(folderPath: String, type: MediaType): Flow<List<MediaItem>> = repository.getByFolder(folderPath, type)
}

class GetByAlbumUseCase @Inject constructor(
    private val repository: MediaRepository
) {
    operator fun invoke(albumId: Long, type: MediaType): Flow<List<MediaItem>> = repository.getByAlbum(albumId, type)
}

class GetByArtistUseCase @Inject constructor(
    private val repository: MediaRepository
) {
    operator fun invoke(artistId: Long, type: MediaType): Flow<List<MediaItem>> = repository.getByArtist(artistId, type)
}

class GetMediaByIdUseCase @Inject constructor(
    private val repository: MediaRepository
) {
    operator fun invoke(id: Long) = repository.getById(id)
}

class GetMediaByPathUseCase @Inject constructor(
    private val repository: MediaRepository
) {
    operator fun invoke(path: String) = repository.getByPath(path)
}

class SetFavoriteUseCase @Inject constructor(
    private val repository: MediaRepository
) {
    operator fun invoke(id: Long, favorite: Boolean) = repository.setFavorite(id, favorite)
}

class IncrementPlayCountUseCase @Inject constructor(
    private val repository: MediaRepository
) {
    operator fun invoke(id: Long) = repository.incrementPlayCount(id)
}

class UpdateBookmarkUseCase @Inject constructor(
    private val repository: MediaRepository
) {
    operator fun invoke(id: Long, position: Long) = repository.updateBookmark(id, position)
}

class UpdateLyricsPathUseCase @Inject constructor(
    private val repository: MediaRepository
) {
    operator fun invoke(id: Long, path: String?) = repository.updateLyricsPath(id, path)
}

class ScanMediaUseCase @Inject constructor(
    private val repository: MediaRepository
) {
    operator fun invoke() = repository.scanMedia()
}

class CountAudioUseCase @Inject constructor(
    private val repository: MediaRepository
) {
    operator fun invoke() = repository.countAudio()
}

class CountVideoUseCase @Inject constructor(
    private val repository: MediaRepository
) {
    operator fun invoke() = repository.countVideo()
}