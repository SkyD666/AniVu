package com.skyd.anivu.ext

import android.content.Context
import android.text.format.DateUtils
import com.skyd.anivu.model.preference.appearance.DateStylePreference
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Long.toDateTimeString(
    context: Context,
    dateStyle: Int = SimpleDateFormat.MEDIUM,
    timeStyle: Int = SimpleDateFormat.MEDIUM,
    locale: Locale = Locale.getDefault()
): String = Date(this).toDateTimeString(context, dateStyle, timeStyle, locale)

fun Date.toDateTimeString(
    context: Context,
    dateStyle: Int = SimpleDateFormat.MEDIUM,
    timeStyle: Int = SimpleDateFormat.MEDIUM,
    locale: Locale = Locale.getDefault()
): String {
    return if (context.dataStore.getOrDefault(DateStylePreference) == DateStylePreference.RELATIVE) {
        DateUtils.getRelativeTimeSpanString(this.time, System.currentTimeMillis(), 0).toString()
    } else {
        SimpleDateFormat
            .getDateTimeInstance(dateStyle, timeStyle, locale)
            .format(this)
    }
}
