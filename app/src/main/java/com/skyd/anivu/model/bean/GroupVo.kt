package com.skyd.anivu.model.bean

import android.os.Parcelable
import com.skyd.anivu.R
import com.skyd.anivu.appContext
import com.skyd.anivu.base.BaseBean
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
open class GroupVo(
    val groupId: String,
    open val name: String,
) : BaseBean, Parcelable {
    fun toPo(): GroupBean {
        return GroupBean(groupId, name, null, null)
    }

    object DefaultGroup :
        GroupVo(DEFAULT_GROUP_ID, appContext.getString(R.string.default_feed_group)) {
        private fun readResolve(): Any = DefaultGroup
        override val name: String
            get() = appContext.getString(R.string.default_feed_group)
    }

    companion object {
        const val DEFAULT_GROUP_ID = "default"
        fun GroupVo.isDefaultGroup(): Boolean = this.groupId == DEFAULT_GROUP_ID
    }
}