package com.skyd.anivu.ui.component.html

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.DrawableWrapper
import android.text.Html
import androidx.appcompat.content.res.AppCompatResources
import coil.Coil
import coil.ImageLoader
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.skyd.anivu.R

class ImageGetter(
    private val context: Context,
    private val imageLoader: ImageLoader = Coil.imageLoader(context),
    private val maxWidth: () -> Int,
    private val onSuccess: (ImageRequest, SuccessResult) -> Unit,
    private val onError: (ImageRequest, ErrorResult) -> Unit = { _, _ -> },
) : Html.ImageGetter {
    override fun getDrawable(source: String): Drawable {
        val drawable = ImageGetterDrawable(
            AppCompatResources.getDrawable(context, R.drawable.ic_hourglass_24)!!
        )
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)

        fun setAndResizeDrawable(newDrawable: Drawable) {
            val drawableWidth = newDrawable.intrinsicWidth
            val maxWidth = maxWidth()
            check(maxWidth > 0) { "maxWidth must be greater than 0" }
            drawable.drawable = newDrawable
            if (drawableWidth > maxWidth) {
                val scale = maxWidth.toFloat() / drawableWidth
                drawable.setBounds(
                    0,
                    0,
                    maxWidth,
                    (newDrawable.intrinsicHeight * scale).toInt()
                )
            } else {
                drawable.setBounds(
                    0,
                    0,
                    drawable.intrinsicWidth,
                    drawable.intrinsicHeight
                )
            }
        }

        // 使用 Coil 加载图片
        val request = ImageRequest.Builder(context)
            .data(source)
            .placeholder(R.drawable.ic_hourglass_24)
            .error(R.drawable.ic_error_24)
            .listener(
                onSuccess = { request, result ->
                    setAndResizeDrawable(result.drawable)
                    onSuccess(request, result)
                },
                onError = { request, result ->
                    if (result.drawable != null) {
                        setAndResizeDrawable(result.drawable!!)
                    }
                    onError(request, result)
                },
            )
            .build()

        // 返回占位符，直到图片加载完成
        imageLoader.enqueue(request)

        return drawable
    }

    inner class ImageGetterDrawable(
        drawable: Drawable,
    ) : DrawableWrapper(drawable)
}