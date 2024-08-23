package com.skyd.anivu.ext

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Environment
import android.provider.MediaStore
import kotlin.random.Random

fun BitmapDrawable.saveToGallery(
    context: Context,
    filename: String = System.currentTimeMillis().toString() + "_" + Random.nextInt(),
): Boolean {
    val contentResolver = context.contentResolver

    val values = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, "$filename.png")
        put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        put(
            MediaStore.Images.Media.RELATIVE_PATH,
            Environment.DIRECTORY_PICTURES + "/" + context.getAppName()
        )
    }

    val uri = contentResolver
        .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return false
    contentResolver.openOutputStream(uri)?.use { outputStream ->
        val out = bitmap?.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return out == true
    } ?: return false
}