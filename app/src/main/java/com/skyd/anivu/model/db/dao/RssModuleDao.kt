package com.skyd.anivu.model.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.skyd.anivu.model.bean.article.RSS_MEDIA_TABLE_NAME
import com.skyd.anivu.model.bean.article.RssMediaBean

@Dao
interface RssModuleDao {
    @Transaction
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertIfNotExistITunesRssBean(rssMediaBean: RssMediaBean)

    @Transaction
    @Query(
        """
        SELECT * FROM $RSS_MEDIA_TABLE_NAME
        WHERE ${RssMediaBean.ARTICLE_ID_COLUMN} = :articleId
        """
    )
    fun getRssMediaBean(articleId: String): RssMediaBean
}