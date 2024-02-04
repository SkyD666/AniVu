package com.skyd.anivu.model.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.skyd.anivu.appContext
import com.skyd.anivu.model.bean.FEED_TABLE_NAME
import com.skyd.anivu.model.bean.FeedBean
import com.skyd.anivu.model.bean.FeedWithArticleBean
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow

@Dao
interface FeedDao {
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface FeedDaoEntryPoint {
        val articleDao: ArticleDao
    }

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setFeed(feedBean: FeedBean)

    @Transaction
    suspend fun setFeedWithArticle(feedWithArticleBean: FeedWithArticleBean) {
        setFeed(feedWithArticleBean.feed)
        val hiltEntryPoint =
            EntryPointAccessors.fromApplication(appContext, FeedDaoEntryPoint::class.java)
        val feedUrl = feedWithArticleBean.feed.url
        hiltEntryPoint.articleDao.insertListIfNotExist(feedWithArticleBean.articles.map {
            if (it.feedUrl != feedUrl) it.copy(feedUrl = feedUrl)
            else it
        })
    }

    @Transaction
    @Delete
    suspend fun removeFeed(feedBean: FeedBean): Int

    @Transaction
    @Query("DELETE FROM $FEED_TABLE_NAME WHERE ${FeedBean.URL_COLUMN} LIKE :url")
    suspend fun removeFeed(url: String): Int

    @Transaction
    @Query("SELECT * FROM $FEED_TABLE_NAME")
    fun getFeedList(): Flow<List<FeedBean>>

    @Transaction
    @Query("SELECT * FROM $FEED_TABLE_NAME WHERE ${FeedBean.URL_COLUMN} LIKE :feedUrl")
    suspend fun getFeed(feedUrl: String): FeedBean
}