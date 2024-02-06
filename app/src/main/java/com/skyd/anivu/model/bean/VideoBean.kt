package com.skyd.anivu.model.bean

import android.content.Context
import android.os.Parcelable
import com.skyd.anivu.base.BaseBean
import com.skyd.anivu.ext.toUri
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.io.File

@Parcelize
data class VideoBean(
    val file: File,
) : BaseBean, Parcelable {
    fun isMedia(context: Context): Boolean = context.contentResolver
        .getType(file.toUri(context))?.startsWith("video/") == true

    @IgnoredOnParcel
    val name: String = file.name.orEmpty()

    @IgnoredOnParcel
    val size: Long = file.length()

    @IgnoredOnParcel
    val date: Long = file.lastModified()

    @IgnoredOnParcel
    val isDir: Boolean = file.isDirectory
}