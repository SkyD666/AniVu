package com.skyd.anivu.model.bean

import androidx.annotation.DrawableRes
import com.skyd.anivu.base.BaseBean

data class OtherWorksBean(
    val name: String,
    @DrawableRes val icon: Int,
    val description: String,
    val url: String,
) : BaseBean