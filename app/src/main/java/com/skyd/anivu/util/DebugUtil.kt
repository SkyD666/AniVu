package com.skyd.anivu.util

import com.skyd.anivu.BuildConfig

/**
 * 只有debug包才会执行表达式
 */
inline fun debug(lambda: () -> Unit) {
    if (BuildConfig.DEBUG) {
        lambda.invoke()
    }
}

/**
 * 只有release包才会执行表达式
 */
inline fun release(lambda: () -> Unit) {
    if (!BuildConfig.DEBUG) {
        lambda.invoke()
    }
}