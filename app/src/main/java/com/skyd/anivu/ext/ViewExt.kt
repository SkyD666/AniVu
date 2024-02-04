package com.skyd.anivu.ext

import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.inputmethod.InputMethodManager
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import com.skyd.anivu.appContext


fun View.enable() {
    if (isEnabled) return
    isEnabled = true
}

fun View.disable() {
    if (!isEnabled) return
    isEnabled = false
}

fun View.gone(animate: Boolean = false, dur: Long = 500L) {
    if (isGone) return
    if (animate) startAnimation(AlphaAnimation(1f, 0f).apply { duration = dur })
    isGone = true
}

fun View.visible(animate: Boolean = false, dur: Long = 500L) {
    if (isVisible) return
    isVisible = true
    if (animate) startAnimation(AlphaAnimation(0f, 1f).apply { duration = dur })
}

fun View.invisible(animate: Boolean = false, dur: Long = 500L) {
    if (isInvisible) return
    isInvisible = true
    if (animate) startAnimation(AlphaAnimation(0f, 1f).apply { duration = dur })
}

fun View.clickScale(scale: Float = 0.75f, duration: Long = 100) {
    animate().scaleX(scale).scaleY(scale).setDuration(duration)
        .withEndAction {
            animate().scaleX(1f).scaleY(1f).setDuration(duration).start()
        }.start()
}

val View.activity: Activity
    get() = context.activity

fun View.showKeyboard() {
    isFocusable = true
    isFocusableInTouchMode = true
    requestFocus()
    val inputManager =
        appContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputManager.showSoftInput(this, 0)
}

fun View.hideKeyboard() {
    val inputManager =
        appContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputManager.hideSoftInputFromWindow(this.windowToken, 0)
}

/**
 * 判断View和给定的Rect是否重叠（边和点不计入）
 * @return true if overlap
 */
fun View.overlap(rect: Rect): Boolean {
    val location = IntArray(2)
    getLocationOnScreen(location)
    val left = location[0]
    val right = location[0] + width
    val top = location[1]
    val bottom = location[1] + height
    return !(left > rect.right || right < rect.left || top > rect.bottom || bottom < rect.top)
}

fun View.addFitsSystemWindows(
    top: Boolean = false,
    bottom: Boolean = false,
    left: Boolean = false,
    right: Boolean = false
) {
    ViewCompat.setOnApplyWindowInsetsListener(this) { v, ins ->
        var newPaddingTop = v.paddingTop
        var newPaddingBottom = v.paddingBottom
        var newPaddingLeft = v.paddingLeft
        var newPaddingRight = v.paddingRight
        if (top) newPaddingTop = ins.getInsets(WindowInsetsCompat.Type.statusBars()).top
        if (bottom) newPaddingBottom =
            ins.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
        if (left) newPaddingLeft = ins.getInsets(WindowInsetsCompat.Type.displayCutout()).left
        if (right) newPaddingRight = ins.getInsets(WindowInsetsCompat.Type.navigationBars()).right

        v.updatePadding(
            top = newPaddingTop,
            bottom = newPaddingBottom,
            left = newPaddingLeft,
            right = newPaddingRight
        )
        ins
    }
}