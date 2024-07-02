package com.skyd.anivu.model.bean

import com.skyd.anivu.base.BaseBean

data class LinkEnclosureBean(
    val link: String,
) : BaseBean {
    val isMedia: Boolean
        get() = EnclosureBean.mediaExtensions.any { link.endsWith(it) }
}
