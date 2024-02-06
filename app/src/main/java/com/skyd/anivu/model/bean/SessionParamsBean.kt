package com.skyd.anivu.model.bean

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.skyd.anivu.base.BaseBean
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

const val SESSION_PARAMS_TABLE_NAME = "SessionParams"

@Parcelize
@Serializable
@Entity(
    tableName = SESSION_PARAMS_TABLE_NAME,
    primaryKeys = [
        DownloadInfoBean.ARTICLE_ID_COLUMN,
        DownloadInfoBean.LINK_COLUMN
    ],
    foreignKeys = [
        ForeignKey(
            entity = DownloadInfoBean::class,
            parentColumns = [DownloadInfoBean.ARTICLE_ID_COLUMN],
            childColumns = [SessionParamsBean.ARTICLE_ID_COLUMN],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = DownloadInfoBean::class,
            parentColumns = [DownloadInfoBean.LINK_COLUMN],
            childColumns = [SessionParamsBean.LINK_COLUMN],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(SessionParamsBean.ARTICLE_ID_COLUMN),
        Index(SessionParamsBean.LINK_COLUMN),
    ]
)
data class SessionParamsBean(
    @ColumnInfo(name = ARTICLE_ID_COLUMN)
    val articleId: String,
    @ColumnInfo(name = LINK_COLUMN)
    val link: String,
    @ColumnInfo(name = DATA_COLUMN, typeAffinity = ColumnInfo.BLOB)
    val data: ByteArray,
) : BaseBean, Parcelable {
    companion object {
        const val ARTICLE_ID_COLUMN = "articleId"
        const val LINK_COLUMN = "link"
        const val DATA_COLUMN = "data"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SessionParamsBean

        if (articleId != other.articleId) return false
        if (link != other.link) return false
        return data.contentEquals(other.data)
    }

    override fun hashCode(): Int {
        var result = articleId.hashCode()
        result = 31 * result + link.hashCode()
        result = 31 * result + data.contentHashCode()
        return result
    }
}