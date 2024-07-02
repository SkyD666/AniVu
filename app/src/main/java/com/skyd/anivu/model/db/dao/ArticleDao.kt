package com.skyd.anivu.model.db.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Transaction
import androidx.sqlite.db.SupportSQLiteQuery
import com.skyd.anivu.appContext
import com.skyd.anivu.model.bean.ARTICLE_TABLE_NAME
import com.skyd.anivu.model.bean.ArticleBean
import com.skyd.anivu.model.bean.ArticleWithEnclosureBean
import com.skyd.anivu.model.bean.ArticleWithFeed
import com.skyd.anivu.model.bean.EnclosureBean
import com.skyd.anivu.model.bean.FEED_TABLE_NAME
import com.skyd.anivu.model.bean.FeedBean
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleDao {
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface ArticleDaoEntryPoint {
        val enclosureDao: EnclosureDao
    }

    // null always compares false in '='
    @Query(
        """
        SELECT * from $ARTICLE_TABLE_NAME 
        WHERE ${ArticleBean.GUID_COLUMN} = :guid AND 
        ${ArticleBean.FEED_URL_COLUMN} = :feedUrl
        """
    )
    suspend fun queryArticleByGuid(
        guid: String?,
        feedUrl: String,
    ): ArticleBean?

    // null always compares false in '='
    @Query(
        """
        SELECT * from $ARTICLE_TABLE_NAME 
        WHERE ${ArticleBean.LINK_COLUMN} = :link AND 
        ${ArticleBean.FEED_URL_COLUMN} = :feedUrl
        """
    )
    suspend fun queryArticleByLink(
        link: String?,
        feedUrl: String,
    ): ArticleBean?

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun innerUpdateArticle(articleBean: ArticleBean)

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun innerUpdateArticle(articleBeanList: List<ArticleBean>)

    @Transaction
    suspend fun insertListIfNotExist(articleWithEnclosureList: List<ArticleWithEnclosureBean>) {
        val hiltEntryPoint =
            EntryPointAccessors.fromApplication(appContext, ArticleDaoEntryPoint::class.java)
        articleWithEnclosureList.forEach {
            // Duplicate article by guid or link
            val guid = it.article.guid
            val link = it.article.link
            var newArticle: ArticleBean? = null
            if (guid != null) {
                newArticle = queryArticleByGuid(
                    guid = guid,
                    feedUrl = it.article.feedUrl,
                )
            } else if (link != null) {
                newArticle = queryArticleByLink(
                    link = link,
                    feedUrl = it.article.feedUrl,
                )
            }
            if (newArticle == null) {
                innerUpdateArticle(it.article)
                newArticle = it.article
            } else {
                // Update all fields except articleId
                newArticle = it.article.copy(articleId = newArticle.articleId)
                innerUpdateArticle(newArticle)
            }

            hiltEntryPoint.enclosureDao.insertListIfNotExist(
                it.enclosures.map { enclosure -> enclosure.copy(articleId = newArticle.articleId) }
            )
        }
    }

    @Transaction
    @Delete
    suspend fun deleteArticle(articleBean: ArticleBean): Int

    @Transaction
    @Query("DELETE FROM $ARTICLE_TABLE_NAME WHERE ${ArticleBean.FEED_URL_COLUMN} LIKE :feedUrl")
    suspend fun deleteArticle(feedUrl: String): Int

    @Transaction
    @Query(
        """
        DELETE FROM $ARTICLE_TABLE_NAME
        WHERE ${ArticleBean.UPDATE_AT_COLUMN} IS NULL
        OR ${ArticleBean.UPDATE_AT_COLUMN} <= :timestamp
        """
    )
    suspend fun deleteArticleBefore(timestamp: Long): Int

    @Transaction
    @RawQuery(observedEntities = [FeedBean::class, ArticleBean::class, EnclosureBean::class])
    fun getArticlePagingSource(sql: SupportSQLiteQuery): PagingSource<Int, ArticleWithFeed>


    @Transaction
    @RawQuery(observedEntities = [ArticleBean::class])
    fun getArticleList(sql: SupportSQLiteQuery): List<ArticleWithFeed>

    @Transaction
    @Query(
        """
        SELECT * FROM $ARTICLE_TABLE_NAME 
        WHERE ${ArticleBean.ARTICLE_ID_COLUMN} LIKE :articleId
        """
    )
    fun getArticleWithEnclosures(articleId: String): Flow<ArticleWithEnclosureBean?>

    @RewriteQueriesToDropUnusedColumns
    @Query(
        """
        SELECT a.*
        FROM $ARTICLE_TABLE_NAME AS a LEFT JOIN $FEED_TABLE_NAME AS f 
        ON a.${ArticleBean.FEED_URL_COLUMN} = f.${FeedBean.URL_COLUMN}
        WHERE a.${ArticleBean.FEED_URL_COLUMN} = :feedUrl 
        ORDER BY date DESC LIMIT 1
        """
    )
    suspend fun queryLatestByFeedUrl(feedUrl: String): ArticleBean?

    @Transaction
    @Query(
        """
        UPDATE $ARTICLE_TABLE_NAME SET ${ArticleBean.IS_FAVORITE_COLUMN} = :favorite
        WHERE ${ArticleBean.ARTICLE_ID_COLUMN} = :articleId
        """
    )
    fun favoriteArticle(articleId: String, favorite: Boolean)

    @Transaction
    @Query(
        """
        UPDATE $ARTICLE_TABLE_NAME SET ${ArticleBean.IS_READ_COLUMN} = :read
        WHERE ${ArticleBean.ARTICLE_ID_COLUMN} = :articleId
        """
    )
    fun readArticle(articleId: String, read: Boolean)

    @Transaction
    @Query(
        """
        UPDATE $ARTICLE_TABLE_NAME SET ${ArticleBean.IS_READ_COLUMN} = 1
        WHERE ${ArticleBean.IS_READ_COLUMN} = 0 AND ${ArticleBean.FEED_URL_COLUMN} = :feedUrl
        """
    )
    fun readAllInFeed(feedUrl: String): Int

    @Transaction
    @Query(
        """
        UPDATE $ARTICLE_TABLE_NAME SET ${ArticleBean.IS_READ_COLUMN} = 1
        WHERE ${ArticleBean.IS_READ_COLUMN} = 0 AND ${ArticleBean.FEED_URL_COLUMN} IN (
            SELECT DISTINCT ${FeedBean.URL_COLUMN} FROM $FEED_TABLE_NAME
            WHERE ${FeedBean.GROUP_ID_COLUMN} = :groupId OR
            :groupId IS NULL AND ${FeedBean.GROUP_ID_COLUMN} IS NULL
        )
        """
    )
    fun readAllInGroup(groupId: String?): Int
}