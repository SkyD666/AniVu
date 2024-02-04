package com.skyd.anivu.ext

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Long.toDateTimeString(
    dateStyle: Int = SimpleDateFormat.MEDIUM,
    timeStyle: Int = SimpleDateFormat.MEDIUM,
    locale: Locale = Locale.getDefault()
): String = Date(this).toDateTimeString(dateStyle, timeStyle, locale)

fun Date.toDateTimeString(
    dateStyle: Int = SimpleDateFormat.MEDIUM,
    timeStyle: Int = SimpleDateFormat.MEDIUM,
    locale: Locale = Locale.getDefault()
): String = SimpleDateFormat
    .getDateTimeInstance(dateStyle, timeStyle, locale)
    .format(this)
