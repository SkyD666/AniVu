package com.skyd.anivu.model.bean.settings

import android.graphics.drawable.Drawable
import com.skyd.anivu.base.BaseBean

data class SettingsSwitchBean(
    val title: String,
    val description: String,
    val icon: Drawable,
    val isChecked: () -> Boolean = { false },
    val onCheckedChanged: ((Boolean) -> Unit)? = null,
) : BaseBean
