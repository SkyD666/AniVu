package com.skyd.anivu.ext

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.graphics.Point
import android.view.WindowManager

val Context.activity: Activity
    get() {
        return tryActivity ?: error("can't find activity: $this")
    }

val Context.tryActivity: Activity?
    get() {
        var ctx = this
        while (ctx is ContextWrapper) {
            if (ctx is Activity) {
                return ctx
            }
            ctx = ctx.baseContext
        }
        return null
    }

val Context.screenIsLand: Boolean
    get() = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

fun Context.screenHeight(includeVirtualKey: Boolean): Int {
    val display =
        (getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
    val outPoint = Point()
    // 可能有虚拟按键的情况
    if (includeVirtualKey) display.getRealSize(outPoint)
    else display.getSize(outPoint)
    return outPoint.y
}

fun Context.screenWidth(includeVirtualKey: Boolean): Int {
    val display =
        (getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
    val outPoint = Point()
    // 可能有虚拟按键的情况
    if (includeVirtualKey) display.getRealSize(outPoint)
    else display.getSize(outPoint)
    return outPoint.x
}