package com.skyd.anivu.ext

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.TypedArray
import android.graphics.Point
import android.net.Uri
import android.os.Build
import android.util.TypedValue
import android.view.WindowManager
import android.widget.Toast
import com.skyd.anivu.R
import com.skyd.anivu.ui.component.showToast

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

fun Context.getAttrColor(attr: Int): Int {
    val typedValue = TypedValue()
    val typedArray: TypedArray = obtainStyledAttributes(typedValue.data, intArrayOf(attr))
    val color = typedArray.getColor(0, 0)
    typedArray.recycle()
    return color
}

fun Context.getAppVersionName(): String {
    var appVersionName = ""
    try {
        val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0L))
        } else {
            packageManager.getPackageInfo(packageName, 0)
        }
        appVersionName = packageInfo.versionName
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
    }
    return appVersionName
}

fun Context.openBrowser(url: String) {
    try {
        val uri: Uri = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        e.printStackTrace()
        getString(R.string.no_browser_found, url).showToast(Toast.LENGTH_LONG)
    }
}