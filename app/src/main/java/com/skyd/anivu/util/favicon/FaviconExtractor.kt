package com.skyd.anivu.util.favicon

import androidx.compose.ui.util.fastMaxBy
import com.skyd.anivu.util.favicon.interceptor.BaseUrlIconTagInterceptor
import com.skyd.anivu.util.favicon.interceptor.HardCodedInterceptor
import com.skyd.anivu.util.favicon.interceptor.IconTagInterceptor
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import retrofit2.Retrofit
import javax.inject.Inject

class FaviconExtractor @Inject constructor(
    retrofit: Retrofit,
) {
    private val interceptors = listOf(
        HardCodedInterceptor(retrofit),
        IconTagInterceptor(retrofit),
        BaseUrlIconTagInterceptor(retrofit),
    )

    fun extractFavicon(url: String): String? = runBlocking {
        interceptors
            .map { async { it.intercept(url) } }
            .map { it.await() }
            .flatten()
            .fastMaxBy {
                (it.size.height * it.size.width).coerceIn(Int.MIN_VALUE..Int.MAX_VALUE)
            }?.url
    }
}