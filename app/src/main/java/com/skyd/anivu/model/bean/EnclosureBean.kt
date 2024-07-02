package com.skyd.anivu.model.bean

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import com.skyd.anivu.base.BaseBean
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

const val ENCLOSURE_TABLE_NAME = "enclosure"

@Parcelize
@Serializable
@Entity(
    tableName = ENCLOSURE_TABLE_NAME,
    primaryKeys = [EnclosureBean.ARTICLE_ID_COLUMN, EnclosureBean.URL_COLUMN],
    foreignKeys = [
        ForeignKey(
            entity = ArticleBean::class,
            parentColumns = [ArticleBean.ARTICLE_ID_COLUMN],
            childColumns = [EnclosureBean.ARTICLE_ID_COLUMN],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class EnclosureBean(
    @ColumnInfo(name = ARTICLE_ID_COLUMN)
    val articleId: String,
    @ColumnInfo(name = URL_COLUMN)
    val url: String,
    @ColumnInfo(name = LENGTH_COLUMN)
    val length: Long,
    @ColumnInfo(name = TYPE_COLUMN)
    val type: String?,
) : BaseBean, Parcelable {
    companion object {
        const val ARTICLE_ID_COLUMN = "articleId"
        const val URL_COLUMN = "url"
        const val LENGTH_COLUMN = "length"
        const val TYPE_COLUMN = "type"

        val mediaExtensions = listOf(
            ".m3u8", ".ogg", ".mp3", ".flac", ".m4v", ".mov", ".avi", ".webm",
            ".mp4", ".mkv", ".m4a",
        )
    }

    val isMedia: Boolean
        get() = type?.startsWith("audio/") == true ||
                type?.startsWith("video/") == true ||
                type == "application/vnd.apple.mpegurl" ||
                mediaExtensions.any { url.endsWith(it) }
}