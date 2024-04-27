package com.skyd.anivu.model.service

import com.skyd.anivu.model.bean.FaviconBean
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface HttpService {
    @GET
    fun requestGetResponseBody(@Url url: String): Call<ResponseBody>

    @GET("https://besticon-demo.herokuapp.com/allicons.json")
    suspend fun requestFavicon(@Query("url") url: String): FaviconBean
}