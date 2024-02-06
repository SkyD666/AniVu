package com.skyd.anivu.config

import com.skyd.anivu.appContext
import java.io.File

object Const {
    const val BASE_URL = "https://github.com/SkyD666/"

    val VIDEO_DIR = File(appContext.filesDir.path, "Video").apply {
        if (!exists()) mkdirs()
    }
}