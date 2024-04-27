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
open class GroupBean(
    @PrimaryKey
    @ColumnInfo(name = GROUP_ID_COLUMN)
    val groupId: String,
    @ColumnInfo(name = NAME_COLUMN)
    open val name: String,
) : BaseBean, Parcelable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GroupBean) return false
        return groupId == other.groupId
    }

    override fun hashCode(): Int {
        return groupId.hashCode()
    }

    object DefaultGroup :
        GroupBean(DEFAULT_GROUP_ID, appContext.getString(R.string.default_feed_group)) {
        private fun readResolve(): Any = DefaultGroup
        override val name: String
            get() = appContext.getString(R.string.default_feed_group)
    }

    companion object {
        const val DEFAULT_GROUP_ID = "default"

        const val NAME_COLUMN = "name"
        const val GROUP_ID_COLUMN = "groupId"

        fun GroupBean.isDefaultGroup(): Boolean = this.groupId == DEFAULT_GROUP_ID
    }
}