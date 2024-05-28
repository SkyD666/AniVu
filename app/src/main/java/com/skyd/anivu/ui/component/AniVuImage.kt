package com.skyd.anivu.ui.component

import android.os.Build.VERSION.SDK_INT
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.EventListener
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.decode.SvgDecoder
import coil.request.ImageRequest


@Composable
fun AniVuImage(
    model: Any?,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    imageLoader: ImageLoader = rememberAniVuImageLoader(),
    contentScale: ContentScale = ContentScale.FillWidth,
    alpha: Float = DefaultAlpha,
) {
    val context = LocalContext.current
    AsyncImage(
        model = remember(model) {
            ImageRequest.Builder(context)
                .data(model)
                .crossfade(true)
                .build()
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
        ImageLoader.Builder(context)
            .components {
                if (SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
                add(SvgDecoder.Factory())
            }
            .run { if (listener != null) eventListener(listener) else this }
            .build()
    }
}