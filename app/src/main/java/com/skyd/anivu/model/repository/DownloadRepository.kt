package com.skyd.anivu.model.repository

import com.skyd.anivu.base.BaseRepository
import com.skyd.anivu.config.Const
import com.skyd.anivu.model.bean.DownloadInfoBean
import com.skyd.anivu.model.db.dao.DownloadInfoDao
import com.skyd.anivu.model.db.dao.SessionParamsDao
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
        return downloadInfoDao.getDownloadingListFlow()
            .flowOn(Dispatchers.IO)
    }

    suspend fun deleteDownloadTaskInfo(
        link: String,
        downloadingDirName: String,
    ): Flow<Unit> {
        return flow {
            downloadInfoDao.deleteDownloadInfo(link)
            sessionParamsDao.deleteSessionParams(link)
            File(Const.DOWNLOADING_VIDEO_DIR, downloadingDirName).deleteRecursively()
            emit(Unit)
        }.flowOn(Dispatchers.IO)
    }
}