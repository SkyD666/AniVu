package com.skyd.anivu.model.repository

import com.skyd.anivu.base.BaseRepository
import com.skyd.anivu.model.bean.VideoBean
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import javax.inject.Inject

class VideoRepository @Inject constructor() : BaseRepository() {
    fun requestVideos(path: String): Flow<List<VideoBean>> {
        return flow {
            val fileList = File(path).listFiles()
                .orEmpty()
                .filterNotNull()
                .map { VideoBean(file = it) }
            emit(fileList)
        }.flowOn(Dispatchers.IO)
    }

    fun requestDelete(file: File): Flow<Boolean> {
        return flow {
            emit(file.deleteRecursively())
        }.flowOn(Dispatchers.IO)
    }
}