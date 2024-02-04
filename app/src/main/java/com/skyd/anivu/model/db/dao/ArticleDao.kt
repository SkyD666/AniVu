package com.skyd.anivu.model.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Transaction
import com.skyd.anivu.model.bean.ARTICLE_TABLE_NAME
import com.skyd.anivu.model.bean.ArticleBean
import com.skyd.anivu.model.bean.FEED_TABLE_NAME
import com.skyd.anivu.model.bean.FeedBean
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleDao {
    @Query(
        """
        SELECT * from article 
        WHERE ${ArticleBean.LINK_COLUMN} = :link
        AND ${ArticleBean.FEED_URL_COLUMN} = :feedUrl
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
    suspend fun insertListIfNotExist(articleBeanList: List<ArticleBean>) {
        articleBeanList.mapNotNull {
            if (queryArticleByLink(
                    link = it.link,
                    feedUrl = it.feedUrl,
                ) == null
            ) it else null
        }.also {
            innerUpdateArticle(it)
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
        SELECT * FROM $ARTICLE_TABLE_NAME 
        WHERE ${ArticleBean.FEED_URL_COLUMN} LIKE :feedUrl
        ORDER BY ${ArticleBean.DATE_COLUMN} DESC
        """
    )
    fun getArticleList(feedUrl: String): Flow<List<ArticleBean>>

    @RewriteQueriesToDropUnusedColumns
    @Query(
        """
        SELECT *
        FROM $ARTICLE_TABLE_NAME AS a LEFT JOIN $FEED_TABLE_NAME AS f 
        ON a.${ArticleBean.FEED_URL_COLUMN} = f.${FeedBean.URL_COLUMN}
        WHERE a.${ArticleBean.FEED_URL_COLUMN} = :feedUrl 
        ORDER BY date DESC LIMIT 1
        """
    )
    suspend fun queryLatestByFeedUrl(feedUrl: String): ArticleBean?
}