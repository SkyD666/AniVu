package com.skyd.anivu.model.bean

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.skyd.anivu.R
import com.skyd.anivu.appContext
import com.skyd.anivu.base.BaseBean
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

const val GROUP_TABLE_NAME = "Group"

@Parcelize
@Serializable
@Entity(tableName = GROUP_TABLE_NAME)
data class GroupBean(
    @PrimaryKey
    @ColumnInfo(name = GROUP_ID_COLUMN)
    val groupId: String,
    @ColumnInfo(name = NAME_COLUMN)
    val name: String,
) : BaseBean, Parcelable {
    companion object {
        const val DEFAULT_GROUP_ID = "default"

        const val NAME_COLUMN = "name"
        const val GROUP_ID_COLUMN = "groupId"

        val defaultGroup = GroupBean(
            groupId = DEFAULT_GROUP_ID,
            name = appContext.getString(R.string.default_feed_group)
        )
    }
}