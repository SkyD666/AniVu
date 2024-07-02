package com.skyd.anivu.model.bean

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.DatabaseView
import androidx.room.Embedded
import com.skyd.anivu.base.BaseBean
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

const val FEED_VIEW_NAME = "FeedView"

@Parcelize
@Serializable
@DatabaseView(
    value = "SELECT $FEED_TABLE_NAME.*, IFNULL(ArticleCount.`count`, 0) AS ${FeedViewBean.ARTICLE_COUNT_COLUMN} " +
            "FROM $FEED_TABLE_NAME LEFT JOIN (SELECT ${ArticleBean.FEED_URL_COLUMN}, COUNT(1) AS `count` " +
            "FROM $ARTICLE_TABLE_NAME GROUP BY ${ArticleBean.FEED_URL_COLUMN}) AS ArticleCount " +
            "ON $FEED_TABLE_NAME.${FeedBean.URL_COLUMN} = ArticleCount.${ArticleBean.FEED_URL_COLUMN}",
    viewName = FEED_VIEW_NAME
)
data class FeedViewBean(
    @Embedded
    val feed: FeedBean,
    @ColumnInfo(name = ARTICLE_COUNT_COLUMN)
    var articleCount: Int = 0
) : BaseBean, Parcelable {
    companion object {
        const val ARTICLE_COUNT_COLUMN = "articleCount"
    }
}