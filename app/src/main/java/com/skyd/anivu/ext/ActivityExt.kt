package com.skyd.anivu.ext

import android.app.Activity
import android.provider.Settings
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.skyd.anivu.R

/**
 * 获取系统屏幕亮度
 */
fun Activity.getScreenBrightness(): Int? = try {
    Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS)
} catch (e: Settings.SettingNotFoundException) {
    e.printStackTrace()
    null
}

fun Activity.findMainNavController(): NavController {
    return Navigation.findNavController(this, R.id.nav_host_fragment_main)
}