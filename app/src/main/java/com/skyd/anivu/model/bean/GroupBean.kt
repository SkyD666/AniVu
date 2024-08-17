package com.skyd.anivu.model.bean

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
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
    @ColumnInfo(name = PREVIOUS_GROUP_ID_COLUMN)
    open val previousGroupId: String? = null,
    @ColumnInfo(name = NEXT_GROUP_ID_COLUMN)
    open val nextGroupId: String? = null,
) : BaseBean, Parcelable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GroupBean) return false

        if (groupId != other.groupId) return false
        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        var result = groupId.hashCode()
        result = 31 * result + name.hashCode()
        return result
    }

    fun toVo(): GroupVo {
        return GroupVo(groupId, name)
    }

    companion object {
        const val NAME_COLUMN = "name"
        const val GROUP_ID_COLUMN = "groupId"
        const val PREVIOUS_GROUP_ID_COLUMN = "previousGroupId"
        const val NEXT_GROUP_ID_COLUMN = "nextGroupId"
    }
}