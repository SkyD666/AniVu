package com.skyd.anivu.ext

import android.view.View
import android.widget.PopupWindow

fun PopupWindow.showAsDropDownImmersively(anchor: View, xoff: Int, yoff: Int) {
    val window = contentView.tryActivity?.window

    if (window != null) {
        val isFocusable: Boolean = isFocusable
        setFocusable(false)
        /* This not work, why?
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, contentView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
         */
        @Suppress("DEPRECATION")
        contentView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_FULLSCREEN
        showAsDropDown(anchor, xoff, yoff)
        setFocusable(isFocusable)
    } else {
        showAsDropDown(anchor, xoff, yoff)
    }
}