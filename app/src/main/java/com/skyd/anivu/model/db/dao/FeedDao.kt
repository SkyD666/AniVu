package com.skyd.anivu.model.db.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Transaction
import androidx.sqlite.db.SupportSQLiteQuery
import com.skyd.anivu.appContext
import com.skyd.anivu.model.bean.ArticleBean
import com.skyd.anivu.model.bean.FEED_TABLE_NAME
import com.skyd.anivu.model.bean.FEED_VIEW_NAME
import com.skyd.anivu.model.bean.FeedBean
import com.skyd.anivu.model.bean.FeedViewBean
import com.skyd.anivu.model.bean.FeedWithArticleBean
import com.skyd.anivu.model.bean.GROUP_TABLE_NAME
import com.skyd.anivu.model.bean.GroupBean
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

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
        hiltEntryPoint.articleDao.insertListIfNotExist(
            feedWithArticleBean.articles.map { articleWithEnclosure ->
                // Enclosure
                val articleId = articleWithEnclosure.article.articleId

                // Add ArticleWithEnclosure
                if (articleWithEnclosure.article.feedUrl != feedUrl) {
                    articleWithEnclosure.copy(
                        article = articleWithEnclosure.article.copy(feedUrl = feedUrl),
                        enclosures = articleWithEnclosure.enclosures.map {
                            if (it.articleId != articleId) it.copy(articleId = articleId)
                            else it
                        }
                    )
                } else articleWithEnclosure
            }
        )
    }

    @Transaction
    @Delete
    suspend fun removeFeed(feedBean: FeedBean): Int

    @Transaction
    @Query("DELETE FROM $FEED_TABLE_NAME WHERE ${FeedBean.URL_COLUMN} = :url")
    suspend fun removeFeed(url: String): Int

    @Transaction
    @Query("DELETE FROM $FEED_TABLE_NAME WHERE ${FeedBean.GROUP_ID_COLUMN} = :groupId")
    suspend fun removeFeedByGroupId(groupId: String): Int

    @Transaction
    @Query(
        """
        UPDATE $FEED_TABLE_NAME
        SET ${FeedBean.NICKNAME_COLUMN} = :nickname, ${FeedBean.GROUP_ID_COLUMN} = :groupId
        WHERE ${FeedBean.URL_COLUMN} = :feedUrl
        """
    )
    suspend fun updateFeedGroupId(feedUrl: String, nickname: String?, groupId: String?): Int

    @Transaction
    @Query(
        """
        UPDATE $FEED_TABLE_NAME
        SET ${FeedBean.GROUP_ID_COLUMN} = :toGroupId
        WHERE :fromGroupId IS NULL AND ${FeedBean.GROUP_ID_COLUMN} IS NULL OR
        ${FeedBean.GROUP_ID_COLUMN} = :fromGroupId OR
        :fromGroupId IS NULL AND ${FeedBean.GROUP_ID_COLUMN} NOT IN (
            SELECT DISTINCT ${GroupBean.GROUP_ID_COLUMN} FROM `$GROUP_TABLE_NAME`
        )
        """
    )
    suspend fun moveFeedToGroup(fromGroupId: String?, toGroupId: String?): Int

    @Transaction
    @Query(
        """
        UPDATE $FEED_TABLE_NAME
        SET ${FeedBean.ICON_COLUMN} = :icon
        WHERE ${FeedBean.URL_COLUMN} = :feedUrl
        """
    )
    suspend fun updateFeedIcon(feedUrl: String, icon: String?): Int

    @Transaction
    @Query("SELECT * FROM $FEED_TABLE_NAME")
    fun getFeedPagingSource(): PagingSource<Int, FeedBean>

    @Transaction
    @Query("SELECT * FROM $FEED_TABLE_NAME WHERE ${FeedBean.URL_COLUMN} = :feedUrl")
    suspend fun getFeed(feedUrl: String): FeedBean

    @Transaction
    @Query(
        """
            SELECT * FROM $FEED_VIEW_NAME
            WHERE ${FeedBean.GROUP_ID_COLUMN} IS NULL OR 
            ${FeedBean.GROUP_ID_COLUMN} NOT IN (:groupIds)
        """
    )
    suspend fun getFeedsNotIn(groupIds: List<String>): List<FeedViewBean>

    @Transaction
    @RawQuery(observedEntities = [FeedBean::class, ArticleBean::class])
    fun getFeedPagingSource(sql: SupportSQLiteQuery): PagingSource<Int, FeedViewBean>

    @Transaction
    @RawQuery(observedEntities = [FeedBean::class, ArticleBean::class])
    fun getFeedList(sql: SupportSQLiteQuery): List<FeedViewBean>

    @Transaction
    @Query("SELECT ${FeedBean.URL_COLUMN} FROM $FEED_TABLE_NAME")
    fun getAllFeedUrl(): List<String>
}