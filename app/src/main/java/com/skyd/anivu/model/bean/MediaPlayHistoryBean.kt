package com.skyd.anivu.model.bean

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.skyd.anivu.base.BaseBean
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

const val MEDIA_PLAY_HISTORY_TABLE_NAME = "MediaPlayHistory"

@Parcelize
@Serializable
@Entity(tableName = MEDIA_PLAY_HISTORY_TABLE_NAME)
data class MediaPlayHistoryBean(
    @PrimaryKey
    @ColumnInfo(name = PATH_COLUMN)
    val path: String,
    @ColumnInfo(name = LAST_PLAY_POSITION_COLUMN)
    val lastPlayPosition: Long,
) : BaseBean, Parcelable {
    companion object {
        const val PATH_COLUMN = "path"
        const val LAST_PLAY_POSITION_COLUMN = "lastPlayPosition"
    }
}