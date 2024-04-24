package com.skyd.anivu.di

import android.content.Context
import com.skyd.anivu.model.db.AppDatabase
import com.skyd.anivu.model.db.SearchDomainDatabase
import com.skyd.anivu.model.db.dao.ArticleDao
import com.skyd.anivu.model.db.dao.DownloadInfoDao
import com.skyd.anivu.model.db.dao.EnclosureDao
import com.skyd.anivu.model.db.dao.FeedDao
import com.skyd.anivu.model.db.dao.GroupDao
import com.skyd.anivu.model.db.dao.SearchDomainDao
import com.skyd.anivu.model.db.dao.SessionParamsDao
import com.skyd.anivu.model.db.dao.TorrentFileDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        AppDatabase.getInstance(context)

    @Provides
    @Singleton
    fun provideGroupDao(database: AppDatabase): GroupDao = database.groupDao()

    @Provides
    @Singleton
    fun provideFeedDao(database: AppDatabase): FeedDao = database.feedDao()

    @Provides
    @Singleton
    fun provideArticleDao(database: AppDatabase): ArticleDao = database.articleDao()

    @Provides
    @Singleton
    fun provideEnclosureDao(database: AppDatabase): EnclosureDao = database.enclosureDao()

    @Provides
    @Singleton
    fun provideDownloadInfoDao(database: AppDatabase): DownloadInfoDao = database.downloadInfoDao()

    @Provides
    @Singleton
    fun provideTorrentFileDao(database: AppDatabase): TorrentFileDao = database.torrentFileDao()

    @Provides
    @Singleton
    fun provideSessionParamsDao(database: AppDatabase): SessionParamsDao =
        database.sessionParamsDao()


    @Provides
    @Singleton
    fun provideSearchDomainDatabase(@ApplicationContext context: Context): SearchDomainDatabase =
        SearchDomainDatabase.getInstance(context)

    @Provides
    @Singleton
    fun provideSearchDomain(database: SearchDomainDatabase): SearchDomainDao =
        database.searchDomainDao()

}
