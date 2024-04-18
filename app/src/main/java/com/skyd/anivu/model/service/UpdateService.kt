package com.skyd.anivu.model.service

import com.skyd.anivu.config.Const
import com.skyd.anivu.model.bean.UpdateBean
import retrofit2.http.GET

interface UpdateService {
    @GET(Const.GITHUB_LATEST_RELEASE)
    suspend fun checkUpdate(): UpdateBean
}