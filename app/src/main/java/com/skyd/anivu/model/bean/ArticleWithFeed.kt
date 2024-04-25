package com.skyd.anivu.model.bean

import androidx.room.Embedded
import androidx.room.Relation

/**
 * An [articleWithEnclosure] contains a [feed].
 */
data class ArticleWithFeed(
    @Embedded
    var articleWithEnclosure: ArticleWithEnclosureBean,
    @Relation(parentColumn = ArticleBean.FEED_URL_COLUMN, entityColumn = FeedBean.URL_COLUMN)
    var feed: FeedBean,
)