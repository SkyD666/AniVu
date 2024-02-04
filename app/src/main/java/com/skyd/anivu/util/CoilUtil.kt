package com.skyd.anivu.util

import android.util.Log
import android.widget.ImageView
import androidx.annotation.DrawableRes
import coil.Coil
import coil.ImageLoader
import coil.imageLoader
import coil.load
import coil.request.ImageRequest
import coil.util.DebugLogger
import com.skyd.anivu.R
import com.skyd.anivu.appContext
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import java.net.URL


object CoilUtil {
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface CoilUtilEntryPoint {
        val okHttpClient: OkHttpClient
    }

    private val hiltEntryPoint =
        EntryPointAccessors.fromApplication(appContext, CoilUtilEntryPoint::class.java)

    private val imageLoader: ImageLoader = ImageLoader.Builder(appContext)
        .okHttpClient(hiltEntryPoint.okHttpClient)
        .crossfade(400)
        .apply { debug { logger(DebugLogger()) } }
        .build()

    init {
        Coil.setImageLoader(imageLoader)
    }

    fun ImageView.loadImage(
        url: String?,
        builder: ImageRequest.Builder.() -> Unit = {},
    ) {
        if (url.isNullOrBlank()) {
            Log.e("loadImage", "Image url must not be null or empty")
            return
        }

        this.load(url, builder = builder)
    }

    fun ImageView.loadImage(res: Int) = loadImage(res.toString())

    fun ImageView.loadImage(
        url: String?,
        @DrawableRes placeholder: Int = 0,
        @DrawableRes error: Int = R.drawable.ic_error_24,
    ) {
        if (url.isNullOrBlank()) {
            Log.e("loadImage", "Image url must not be null or empty")
            return
        }

        // 是本地drawable
        url.toIntOrNull()?.let { drawableResId ->
            load(drawableResId) {
                placeholder(placeholder)
                error(error)
            }
            return
        }

        runCatching {
            loadImage(url) {
                placeholder(placeholder)
                error(error)
                addHeader("Host", URL(url).host)
                addHeader("Accept", "*/*")
                addHeader("Accept-Encoding", "gzip, deflate")
                addHeader("Connection", "keep-alive")
            }
        }.onFailure {
            it.printStackTrace()
        }
    }

    fun clearMemoryDiskCache() {
        appContext.imageLoader.memoryCache?.clear()
        Coil.imageLoader(appContext).diskCache?.clear()
    }
}