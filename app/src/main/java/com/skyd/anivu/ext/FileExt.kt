package com.skyd.anivu.ext

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

fun File.toUri(context: Context): Uri = FileProvider.getUriForFile(
    context, "${context.packageName}.fileprovider", this
)