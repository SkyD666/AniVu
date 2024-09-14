package com.skyd.anivu.model.repository.download

import com.skyd.anivu.base.BaseRepository
import com.skyd.anivu.config.Const
import com.skyd.anivu.ext.sampleWithoutFirst
import com.skyd.anivu.model.bean.download.DownloadInfoBean
import com.skyd.anivu.model.worker.download.DownloadTorrentWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import javax.inject.Inject

class DownloadRepository @Inject constructor() : BaseRepository() {
    suspend fun requestDownloadingVideos(): Flow<List<DownloadInfoBean>> {
        return combine(
            DownloadManager.getDownloadInfoList().distinctUntilChanged(),
            DownloadTorrentWorker.peerInfoMapFlow.sampleWithoutFirst(1000),
            DownloadTorrentWorker.torrentStatusMapFlow.sampleWithoutFirst(1000),
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
    ): Flow<Unit> {
        return flow {
            if (DownloadManager.getDownloadState(link)?.downloadComplete() != true) {
                DownloadManager.getTorrentFilesByLink(link).forEach {
                    File(it.path).deleteRecursively()
                }
            }
            val requestUuid = DownloadManager.getDownloadInfo(link)?.downloadRequestId
            if (!requestUuid.isNullOrBlank()) {
                File(Const.TORRENT_RESUME_DATA_DIR, requestUuid).deleteRecursively()
            }
            // 这些最后删除，防止上面会使用
            DownloadManager.deleteDownloadInfo(link)
            DownloadManager.deleteSessionParams(link)
            DownloadManager.removeDownloadLinkUuidMap(link)
            emit(Unit)
        }.flowOn(Dispatchers.IO)
    }
}