package com.larkplayer.app.di

import com.larkplayer.app.data.repository.EqualizerRepositoryImpl
import com.larkplayer.app.data.repository.FolderRepositoryImpl
import com.larkplayer.app.data.repository.MediaRepositoryImpl
import com.larkplayer.app.data.repository.PlaylistRepositoryImpl
import com.larkplayer.app.domain.repository.EqualizerRepository
import com.larkplayer.app.domain.repository.FolderRepository
import com.larkplayer.app.domain.repository.MediaRepository
import com.larkplayer.app.domain.repository.PlaylistRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Provides
    fun provideMediaRepository(impl: MediaRepositoryImpl): MediaRepository = impl

    @Provides
    fun providePlaylistRepository(impl: PlaylistRepositoryImpl): PlaylistRepository = impl

    @Provides
    fun provideFolderRepository(impl: FolderRepositoryImpl): FolderRepository = impl

    @Provides
    fun provideEqualizerRepository(impl: EqualizerRepositoryImpl): EqualizerRepository = impl
}