package com.skyd.anivu.model.repository

import com.skyd.anivu.base.BaseRepository
import com.skyd.anivu.model.bean.DownloadInfoBean
import com.skyd.anivu.model.db.dao.SessionParamsDao
import com.skyd.anivu.model.db.dao.DownloadInfoDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import javax.inject.Inject

class DownloadRepository @Inject constructor(
    private val downloadInfoDao: DownloadInfoDao,
    private val sessionParamsDao: SessionParamsDao,
) : BaseRepository() {
    fun requestDownloadingVideos(): Flow<List<DownloadInfoBean>> {
        return downloadInfoDao.getDownloadingList()
            .flowOn(Dispatchers.IO)
    }

    suspend fun deleteDownloadTaskInfo(
        articleId: String,
        link: String,
        file: String?,
    ): Flow<Unit> {
        return flow {
            downloadInfoDao.deleteDownloadInfo(articleId, link)
            sessionParamsDao.deleteSessionParams(articleId, link)
            if (!file.isNullOrBlank()) {
                File(file).deleteRecursively()
            }
            emit(Unit)
        }.flowOn(Dispatchers.IO)
    }
}