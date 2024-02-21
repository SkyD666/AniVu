package com.skyd.anivu.util

import android.os.SystemClock

fun uniqueInt(): Int = (SystemClock.uptimeMillis() % 99999999).toInt()
