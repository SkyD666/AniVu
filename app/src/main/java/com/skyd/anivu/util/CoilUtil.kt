package com.skyd.anivu.util

import android.graphics.Bitmap
import android.widget.ImageView
import androidx.annotation.DrawableRes
import coil.Coil
import coil.ImageLoader
import coil.imageLoader
import coil.load
import com.skyd.anivu.R
import com.skyd.anivu.appContext
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.net.URL


object CoilUtil {
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface CoilUtilEntryPoint {
        val imageLoader: ImageLoader
    }

    private val hiltEntryPoint =
        EntryPointAccessors.fromApplication(appContext, CoilUtilEntryPoint::class.java)

    init {
        Coil.setImageLoader(hiltEntryPoint.imageLoader)
    }

    fun ImageView.loadImage(
        res: Int,
        @DrawableRes placeholder: Int = 0,
        @DrawableRes error: Int = R.drawable.ic_image_load_error_24,
    ) = load(res, imageLoader = hiltEntryPoint.imageLoader) {
        placeholder(placeholder)
        error(error)
    }

    fun ImageView.loadImage(
        bitmap: Bitmap,
        @DrawableRes placeholder: Int = 0,
        @DrawableRes error: Int = R.drawable.ic_image_load_error_24,
    ) = load(bitmap, imageLoader = hiltEntryPoint.imageLoader) {
        placeholder(placeholder)
        error(error)
    }

    fun ImageView.loadImage(
        url: String,
        @DrawableRes placeholder: Int = 0,
        @DrawableRes error: Int = R.drawable.ic_image_load_error_24,
    ) {
        load(url) {
            placeholder(placeholder)
            error(error)
            runCatching { addHeader("Host", URL(url).host) }.onFailure { it.printStackTrace() }
        }
    }

    fun clearMemoryDiskCache() {
        appContext.imageLoader.memoryCache?.clear()
        Coil.imageLoader(appContext).diskCache?.clear()
    }
}