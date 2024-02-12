package com.skyd.anivu.model.bean

import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.IdRes
import com.skyd.anivu.base.BaseBean

data class MoreBean(
    val icon: Drawable,
    @ColorInt val iconTint: Int,
    val title: String,
    @IdRes val navigateId: Int,
    val background: Drawable,
    @ColorInt val backgroundTint: Int,
) : BaseBean
