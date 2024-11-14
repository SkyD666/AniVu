package com.skyd.anivu.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil3.ComponentRegistry
import coil3.EventListener
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.lifecycle
import coil3.util.DebugLogger
import com.skyd.anivu.ext.imageLoaderBuilder


@Composable
fun AniVuImage(
    model: Any?,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    imageLoader: ImageLoader = rememberAniVuImageLoader(),
    contentScale: ContentScale = ContentScale.FillWidth,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
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
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .memoryCachePolicy(CachePolicy.ENABLED)
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
        colorFilter = colorFilter,
    )
}

@Composable
fun rememberAniVuImageLoader(
    listener: EventListener? = null,
    components: ComponentRegistry.Builder.() -> Unit = {},
): ImageLoader {
    val context = LocalContext.current
    return remember(context) {
        context.imageLoaderBuilder()
            .components(components)
            .run { if (listener != null) eventListener(listener) else this }
            .logger(DebugLogger())
            .build()
    }
}