package com.skyd.anivu.di

import com.skyd.anivu.model.db.dao.DownloadInfoDao
import com.skyd.anivu.model.db.dao.SessionParamsDao
import com.skyd.anivu.model.db.dao.TorrentFileDao
import com.skyd.anivu.model.repository.download.DownloadManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DownloadModule {
    @Provides
    @Singleton
    fun provideDownloadManager(
        downloadInfoDao: DownloadInfoDao,
        sessionParamsDao: SessionParamsDao,
        torrentFileDao: TorrentFileDao,
    ): DownloadManager = DownloadManager(downloadInfoDao, sessionParamsDao, torrentFileDao)
}