package com.skyd.anivu.util.favicon.interceptor

import kotlinx.coroutines.runBlocking
import retrofit2.Retrofit
import javax.inject.Inject

class BaseUrlIconTagInterceptor @Inject constructor(
    retrofit: Retrofit,
) : IconTagInterceptor(retrofit) {
    override fun intercept(url: String): List<Interceptor.IconData> = runBlocking {
        baseUrl(url)
            ?.let { base -> super.intercept(base) }
            .orEmpty()
    }
}