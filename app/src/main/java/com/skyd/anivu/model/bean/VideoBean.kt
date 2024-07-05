package com.skyd.anivu.model.bean

import com.skyd.anivu.base.BaseBean
import com.skyd.anivu.ext.getMimeType
import java.io.File

data class VideoBean(
    val displayName: String? = null,
    val file: File,
) : BaseBean {
    var name: String = file.name
    var mimetype: String = file.getMimeType() ?: "*/*"
    var size: Long = file.length()
    var date: Long = file.lastModified()
    val isMedia: Boolean = mimetype.startsWith("video/") || mimetype.startsWith("audio/")
    val isDir: Boolean = file.isDirectory
    val isFile: Boolean = file.isFile
}