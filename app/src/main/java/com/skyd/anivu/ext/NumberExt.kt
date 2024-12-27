package com.skyd.anivu.ext

import android.content.Context
import android.content.res.Resources
import android.text.format.Formatter
import android.util.TypedValue

fun Long.fileSize(context: Context): String =
    Formatter.formatFileSize(context, this)
        // On some systems, zero-width characters are inserted into the string
        // for unknown reasons 🤔, and they need to be removed to avoid display issues.
        .replace(Regex("[\\u200B\\u200C\\u200E\\u200F\\u202A-\\u202E\\u2060]"), "")

val Float.dp: Float
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this,
        Resources.getSystem().displayMetrics
    )

val Int.dp: Int
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        Resources.getSystem().displayMetrics
    ).toInt()

val Float.sp: Float
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP,
        this,
        Resources.getSystem().displayMetrics
    )

val Int.sp: Int
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP,
        this.toFloat(),
        Resources.getSystem().displayMetrics
    ).toInt()

fun Float.toPercentage(format: String = "%.2f%%"): String = format.format(this * 100)

fun Float.toDegrees(): Float = (this * 180 / Math.PI).toFloat()