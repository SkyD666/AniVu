package com.skyd.anivu.ext

import android.content.Context
import android.text.format.DateUtils
import com.skyd.anivu.model.preference.appearance.DateStylePreference
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Long.toDateTimeString(
    context: Context,
): String = Date(this).toDateTimeString(context)

fun Date.toDateTimeString(
    context: Context,
): String {
    return if (context.dataStore.getOrDefault(DateStylePreference) == DateStylePreference.RELATIVE) {
        toRelativeDateTimeString()
    } else {
        toAbsoluteDateTimeString()
    }
}

fun Long.toAbsoluteDateTimeString(
    dateStyle: Int = SimpleDateFormat.MEDIUM,
    timeStyle: Int = SimpleDateFormat.MEDIUM,
    locale: Locale = Locale.getDefault()
): String = Date(this).toAbsoluteDateTimeString(dateStyle, timeStyle, locale)

fun Date.toAbsoluteDateTimeString(
    dateStyle: Int = SimpleDateFormat.MEDIUM,
    timeStyle: Int = SimpleDateFormat.MEDIUM,
    locale: Locale = Locale.getDefault()
): String = SimpleDateFormat
    .getDateTimeInstance(dateStyle, timeStyle, locale)
    .format(this)

fun Long.toRelativeDateTimeString(): String = Date(this).toRelativeDateTimeString()

fun Date.toRelativeDateTimeString(): String {
    val current = System.currentTimeMillis()
    val delta = current - this.time
    return DateUtils.getRelativeTimeSpanString(
        this.time,
        current,
        // "DateUtils.WEEK_IN_MILLIS <= .. <= DateUtils.WEEK_IN_MILLIS * 4" is 1~3 weeks ago
        if (delta in DateUtils.WEEK_IN_MILLIS..DateUtils.WEEK_IN_MILLIS * 4) {
            DateUtils.WEEK_IN_MILLIS
        } else 0
    ).toString()
}