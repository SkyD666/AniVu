package com.skyd.anivu.util.favicon.interceptor

import com.skyd.anivu.model.service.HttpService
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import retrofit2.Retrofit
import javax.inject.Inject

class HardCodedInterceptor @Inject constructor(
    private val retrofit: Retrofit,
) : Interceptor {
    private val hardCodedFavicons = arrayOf(
        "/favicon.ico",
        "/apple-touch-icon.png",
        "/apple-touch-icon-precomposed.png",
    )

    override fun intercept(url: String): List<Interceptor.IconData> = runBlocking {
        try {
            val baseUrl = baseUrl(url) ?: return@runBlocking emptyList()
            val request = mutableListOf<Deferred<Interceptor.IconData?>>()
            hardCodedFavicons.forEach {
                val faviconUrl = baseUrl + it
                request += async {
                    try {
                        retrofit.create(HttpService::class.java).head(faviconUrl).run {
                            if (isSuccessful && isImage()) {
                                Interceptor.IconData(
                                    url = faviconUrl,
                                    size = if (isSvg() ||
                                        faviconUrl.endsWith(".svg", ignoreCase = true)
                                    ) {
                                        Interceptor.IconSize.MAX_SIZE
                                    } else {
                                        Interceptor.IconSize.EMPTY
                                    },
                                )
                            } else null
                        }
                    } catch (e: Exception) {
                        null
                    }
                }
            }
            request.mapNotNull { it.await() }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}