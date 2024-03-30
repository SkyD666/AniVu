package com.skyd.anivu.util

import android.content.Context
import android.util.Log
import androidx.core.app.ShareCompat
import com.skyd.anivu.R

object ShareUtil {
    fun shareText(
        context: Context,
        text: CharSequence,
        packageName: String? = null,
        className: String? = null,
    ) {
        Log.i("shareText", "$packageName $className")
        val shareIntent = ShareCompat.IntentBuilder(context)
            .setType("text/plain")
            .setText(text)
            .setChooserTitle(R.string.share)
            .createChooserIntent()

        if (!packageName.isNullOrBlank() && !className.isNullOrBlank()) {
            shareIntent.setClassName(packageName, className)
        } else if (!packageName.isNullOrBlank()) {
            shareIntent.setPackage(packageName)
        }

        context.startActivity(shareIntent)
    }
}
