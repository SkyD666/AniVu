package com.skyd.anivu.model.bean

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import com.skyd.anivu.base.BaseBean

data class MoreBean(
    val title: String,
    @DrawableRes val icon: Int,
    val iconTint: Color,
    val shape: Shape,
    val shapeColor: Color,
    val action: () -> Unit,
) : BaseBean
