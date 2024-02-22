package com.skyd.anivu.model.bean.settings

import android.graphics.drawable.Drawable
import com.skyd.anivu.base.BaseBean

data class SettingsBaseBean(
    val title: String,
    val description: String,
    val icon: Drawable,
    val action: ((SettingsBaseBean) -> Unit)? = null,
) : BaseBean
