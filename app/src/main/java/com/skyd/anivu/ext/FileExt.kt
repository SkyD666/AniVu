package com.skyd.anivu.ext

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.skyd.anivu.R
import com.skyd.anivu.ui.component.showToast
import java.io.File


fun File.toUri(context: Context): Uri {
    return try {
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", this)
    } catch (e: IllegalArgumentException) {
        toUri()
    }
}

fun File.deleteRecursivelyExclude(hook: (File) -> Boolean = { true }): Boolean =
    walkBottomUp().fold(true) { res, it ->
        (it != this && hook(it) && (it.delete() || !it.exists())) && res
    }

fun File.getMimeType(): String? {
    if (isDirectory) return DocumentsContract.Document.MIME_TYPE_DIR
    var type: String? = null
    val extension = path.substringAfterLast(".", "")
    if (extension.isNotBlank()) {
        type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
    }
    return type
}

fun File.savePictureToMediaStore(context: Context, autoDelete: Boolean = true) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val contentValues = ContentValues()
        contentValues.put(
            MediaStore.Images.Media.RELATIVE_PATH,
            "${Environment.DIRECTORY_PICTURES}/AniVu",
        )
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, name)
        val uri = context.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )!!
        context.contentResolver.openOutputStream(uri)?.use { output ->
            inputStream().use { input ->
                input.copyTo(output)
            }
        }
    } else {
        val dir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            "AniVu"
        )
        if (!dir.exists()) dir.mkdirs()
        this.copyTo(File(dir, name))
    }
    context.getString(R.string.save_picture_to_media_store_saved).showToast()
    if (autoDelete) delete()
}