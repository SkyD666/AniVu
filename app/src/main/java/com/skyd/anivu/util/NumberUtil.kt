package com.skyd.anivu.util

import android.os.SystemClock

fun uniqueInt(): Int = (SystemClock.uptimeMillis() % 99999999).toInt()

fun floatToPercentage(floatValue: Float): String = "%.2f%%".format(floatValue * 100)