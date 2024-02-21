package com.skyd.anivu.model.repository

import com.skyd.anivu.base.BaseRepository
import com.skyd.anivu.config.Const
import com.skyd.anivu.model.bean.DownloadInfoBean
import com.skyd.anivu.model.db.dao.DownloadInfoDao
import com.skyd.anivu.model.db.dao.SessionParamsDao
import com.skyd.anivu.model.worker.download.DownloadTorrentWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import javax.inject.Inject

class DownloadRepository @Inject constructor(
    private val downloadInfoDao: DownloadInfoDao,
    private val sessionParamsDao: SessionParamsDao,
) : BaseRepository() {
    fun requestDownloadingVideos(): Flow<List<DownloadInfoBean>> {
        return combine(
            downloadInfoDao.getAllDownloadListFlow().distinctUntilChanged(),
            DownloadTorrentWorker.peerInfoMapFlow,
            DownloadTorrentWorker.torrentStatusMapFlow,
        ) { list, peerInfoMap, uploadPayloadRateMap ->
            list.map { downloadInfoBean ->
                downloadInfoBean.copy().apply {
                    peerInfoList = peerInfoMap.getOrDefault(downloadRequestId, emptyList()).toList()
                    val torrentStatus = uploadPayloadRateMap[downloadRequestId]
                    if (torrentStatus != null) {
                        uploadPayloadRate = torrentStatus.uploadPayloadRate()
                        downloadPayloadRate = torrentStatus.downloadPayloadRate()
                    }
                }
            }
        }.flowOn(Dispatchers.IO)
    }

    suspend fun deleteDownloadTaskInfo(
        link: String,
        downloadingDirName: String,
    ): Flow<Unit> {
        return flow {
            if (downloadingDirName.isNotBlank()) {
                File(Const.DOWNLOADING_VIDEO_DIR, downloadingDirName).deleteRecursively()
            }
            val requestUuid = downloadInfoDao.getDownloadInfo(link)?.downloadRequestId
            if (!requestUuid.isNullOrBlank()) {
                File(Const.TORRENT_RESUME_DATA_DIR, requestUuid).deleteRecursively()
            }
            // 这些最后删除，防止上面会使用
            downloadInfoDao.deleteDownloadInfo(link)
            sessionParamsDao.deleteSessionParams(link)
            downloadInfoDao.removeDownloadLinkByLink(link)
            emit(Unit)
        }.flowOn(Dispatchers.IO)
    }
}