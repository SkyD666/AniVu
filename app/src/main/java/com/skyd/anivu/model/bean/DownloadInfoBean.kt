package com.skyd.anivu.model.bean

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.skyd.anivu.base.BaseBean
import com.skyd.anivu.ui.adapter.variety.Diff
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

const val DOWNLOAD_INFO_TABLE_NAME = "DownloadInfo"

@Parcelize
@Serializable
@Entity(
    tableName = DOWNLOAD_INFO_TABLE_NAME,
    primaryKeys = [
        DownloadInfoBean.ARTICLE_ID_COLUMN,
        DownloadInfoBean.LINK_COLUMN
    ],
    foreignKeys = [
        ForeignKey(
            entity = ArticleBean::class,
            parentColumns = [ArticleBean.ARTICLE_ID_COLUMN],
            childColumns = [DownloadInfoBean.ARTICLE_ID_COLUMN],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(DownloadInfoBean.ARTICLE_ID_COLUMN, unique = true),
        Index(DownloadInfoBean.LINK_COLUMN, unique = true),
    ]
)
data class DownloadInfoBean(
    @ColumnInfo(name = ARTICLE_ID_COLUMN)
    val articleId: String,
    @ColumnInfo(name = LINK_COLUMN)
    val link: String,
    @ColumnInfo(name = NAME_COLUMN)
    val name: String,
    @ColumnInfo(name = FILE_COLUMN)
    var file: String?,
    @ColumnInfo(name = DOWNLOAD_DATE_COLUMN)
    var downloadDate: Long,
    @ColumnInfo(name = SIZE_COLUMN)
    val size: Long,
    @ColumnInfo(name = PROGRESS_COLUMN)
    val progress: Float,
    @ColumnInfo(name = DOWNLOAD_STATE_COLUMN)
    val downloadState: DownloadState = DownloadState.Init,
    @ColumnInfo(name = DOWNLOAD_REQUEST_ID_COLUMN)
    val downloadRequestId: String,
) : BaseBean, Parcelable, Diff {
    enum class DownloadState {
        Init, Downloading, Paused, Completed
    }


    companion object {
        const val ARTICLE_ID_COLUMN = "articleId"
        const val LINK_COLUMN = "link"
        const val NAME_COLUMN = "name"
        const val FILE_COLUMN = "file"
        const val DOWNLOAD_DATE_COLUMN = "downloadDate"
        const val SIZE_COLUMN = "size"
        const val PROGRESS_COLUMN = "progress"
        const val DOWNLOAD_STATE_COLUMN = "downloadState"
        const val DOWNLOAD_REQUEST_ID_COLUMN = "downloadRequestId"

        const val PAYLOAD_PROGRESS = "progress"
        const val PAYLOAD_DOWNLOAD_STATE = "downloadState"
        const val PAYLOAD_NAME = "name"
        const val PAYLOAD_FILE = "file"
        const val PAYLOAD_SIZE = "size"
    }

    override fun sameAs(o: Any?): Boolean {
        return o is DownloadInfoBean && articleId == o.articleId && link == o.link
    }

    override fun contentSameAs(o: Any?): Boolean {
        return this == o
    }

    override fun diff(o: Any?): Any? {
        if (o !is DownloadInfoBean) return null

        val list: MutableList<Any> = mutableListOf()
        if (progress != o.progress) list += PAYLOAD_PROGRESS
        if (downloadState != o.downloadState) list += PAYLOAD_DOWNLOAD_STATE
        if (name != o.name) list += PAYLOAD_NAME
        if (file != o.file) list += PAYLOAD_FILE
        if (size != o.size) list += PAYLOAD_SIZE
        return list.ifEmpty { null }
    }
}