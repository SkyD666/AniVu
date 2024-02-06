package com.skyd.anivu.ext

import android.content.Context
import android.text.format.Formatter

fun Long.fileSize(context: Context) = Formatter.formatShortFileSize(context, this)