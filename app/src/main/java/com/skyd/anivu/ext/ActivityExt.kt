package com.skyd.anivu.ext

import android.app.Activity
import android.provider.Settings

/**
 * 获取系统屏幕亮度
 */
fun Activity.getScreenBrightness(): Int? = try {
    Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS)
} catch (e: Settings.SettingNotFoundException) {
    e.printStackTrace()
    null
}