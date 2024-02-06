package com.skyd.anivu.ext

import android.content.Context
import androidx.core.content.FileProvider
import java.io.File

fun File.toUri(context: Context) = FileProvider.getUriForFile(
    context, "${context.packageName}.fileprovider", this
)