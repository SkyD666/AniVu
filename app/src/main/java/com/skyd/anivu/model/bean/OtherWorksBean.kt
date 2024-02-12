package com.skyd.anivu.model.bean

import android.graphics.drawable.Drawable
import com.skyd.anivu.base.BaseBean

data class OtherWorksBean(
    val name: String,
    val icon: Drawable,
    val description: String,
    val url: String,
) : BaseBean