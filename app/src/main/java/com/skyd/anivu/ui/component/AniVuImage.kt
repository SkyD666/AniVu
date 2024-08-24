package com.skyd.anivu.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.EventListener
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.skyd.anivu.ext.imageLoaderBuilder


@Composable
fun AniVuImage(
    model: Any?,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    imageLoader: ImageLoader = rememberAniVuImageLoader(),
    contentScale: ContentScale = ContentScale.FillWidth,
    alpha: Float = DefaultAlpha,
) {
    AsyncImage(
        model = if (model is ImageRequest) {
            model
        } else {
            val context = LocalContext.current
            val lifecycleOwner = LocalLifecycleOwner.current
            remember(model) {
                ImageRequest.Builder(context)
                    .lifecycle(lifecycleOwner)
                    .data(model)
                    .crossfade(true)
                    .build()
            }
        },
        modifier = modifier,
        contentDescription = contentDescription,
        contentScale = contentScale,
        imageLoader = imageLoader,
        alpha = alpha,
    )
}

@Composable
fun rememberAniVuImageLoader(listener: EventListener? = null): ImageLoader {
    val context = LocalContext.current
    return remember(context) {
        context.imageLoaderBuilder()
            .run { if (listener != null) eventListener(listener) else this }
            .build()
    }
}