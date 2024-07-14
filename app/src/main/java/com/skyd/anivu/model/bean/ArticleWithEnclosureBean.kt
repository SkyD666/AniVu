package com.skyd.anivu.model.bean

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.parcelize.Parcelize
import java.io.Serializable

@Parcelize
@kotlinx.serialization.Serializable
data class ArticleWithEnclosureBean(
    @Embedded
    var article: ArticleBean,
    @Relation(
        parentColumn = ArticleBean.ARTICLE_ID_COLUMN,
        entityColumn = EnclosureBean.ARTICLE_ID_COLUMN,
    )
    var enclosures: List<EnclosureBean>,
) : Serializable, Parcelable
