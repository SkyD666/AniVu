package com.skyd.anivu.model.service

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url

interface HttpService {
    @GET
    fun requestGetResponseBody(@Url url: String): Call<ResponseBody>
}