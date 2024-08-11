package com.skyd.anivu.ext

import android.app.Activity
import android.view.View
import android.view.Window
import android.view.animation.AlphaAnimation
import android.view.inputmethod.InputMethodManager
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible


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

val View.activity: Activity
    get() = context.activity

val View.tryActivity: Activity?
    get() = context.tryActivity

val View.tryWindow: Window?
    get() = (parent as? DialogWindowProvider)?.window ?: context.tryWindow

fun View.showSoftKeyboard() {
    if (requestFocus()) {
        val imm = context.getSystemService(InputMethodManager::class.java)
        imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    }
}

fun View.showSoftKeyboard(window: Window) {
    if (requestFocus()) {
        WindowCompat.getInsetsController(window, this).show(WindowInsetsCompat.Type.ime())
    }
}

fun View.hideSoftKeyboard(window: Window) {
    if (requestFocus()) {
        WindowCompat.getInsetsController(window, this).hide(WindowInsetsCompat.Type.ime())
    }
}