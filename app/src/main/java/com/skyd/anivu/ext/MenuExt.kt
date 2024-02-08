package com.skyd.anivu.ext

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.InsetDrawable
import android.util.TypedValue
import android.view.Menu
import androidx.appcompat.view.menu.MenuBuilder

@SuppressLint("RestrictedApi")
fun Menu.tryAddIcon(context: Context) {
    if (this is MenuBuilder) {
        setOptionalIconsVisible(true)
        for (item in visibleItems) {
            val iconMarginPx = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                6f,
                context.resources.displayMetrics
            ).toInt()
            if (item.icon != null) {
                item.icon = InsetDrawable(item.icon, iconMarginPx, 0, iconMarginPx, 0)
            }
        }
    }
}