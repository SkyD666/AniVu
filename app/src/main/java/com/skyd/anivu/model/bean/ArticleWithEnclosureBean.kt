package com.skyd.anivu.model.bean

import androidx.room.Embedded
import androidx.room.Relation

data class ArticleWithEnclosureBean(
    @Embedded
    var article: ArticleBean,
    @Relation(
        parentColumn = ArticleBean.ARTICLE_ID_COLUMN,
        entityColumn = EnclosureBean.ARTICLE_ID_COLUMN,
    )
    var enclosures: List<EnclosureBean>,
)
