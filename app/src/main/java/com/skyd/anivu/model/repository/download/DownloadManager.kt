package com.skyd.anivu.model.repository.download

import androidx.work.WorkManager
import com.skyd.anivu.appContext
import com.skyd.anivu.model.bean.download.DownloadInfoBean
import com.skyd.anivu.model.bean.download.DownloadInfoBean.DownloadState
import com.skyd.anivu.model.bean.download.DownloadLinkUuidMapBean
import com.skyd.anivu.model.bean.download.SessionParamsBean
import com.skyd.anivu.model.bean.download.TorrentFileBean
import com.skyd.anivu.model.db.dao.DownloadInfoDao
import com.skyd.anivu.model.db.dao.SessionParamsDao
import com.skyd.anivu.model.db.dao.TorrentFileDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

class DownloadManager @Inject constructor(
    private val downloadInfoDao: DownloadInfoDao,
    private val sessionParamsDao: SessionParamsDao,
    private val torrentFileDao: TorrentFileDao,
) {
    private lateinit var downloadInfoMap: LinkedHashMap<String, DownloadInfoBean>
    private lateinit var downloadInfoListFlow: MutableStateFlow<List<DownloadInfoBean>>

    suspend fun getDownloadInfoList(): Flow<List<DownloadInfoBean>> {
        checkDownloadInfoMapInitialized()
        return downloadInfoListFlow
    }

    suspend fun getDownloadInfo(link: String): DownloadInfoBean? {
        checkDownloadInfoMapInitialized()
        return downloadInfoMap[link]
    }

    fun getDownloadLinkByUuid(uuid: String): String? {
        return downloadInfoDao.getDownloadLinkByUuid(uuid)
    }

    fun getDownloadUuidByLink(link: String): String? {
        return downloadInfoDao.getDownloadUuidByLink(link)
    }

    fun setDownloadLinkUuidMap(bean: DownloadLinkUuidMapBean) {
        return downloadInfoDao.setDownloadLinkUuidMap(bean)
    }

    suspend fun getDownloadName(link: String): String? {
        checkDownloadInfoMapInitialized()
        return downloadInfoMap[link]?.name
    }

    suspend fun getDownloadProgress(link: String): Float? {
        checkDownloadInfoMapInitialized()
        return downloadInfoMap[link]?.progress
    }

    suspend fun getDownloadState(link: String): DownloadState? {
        checkDownloadInfoMapInitialized()
        return downloadInfoMap[link]?.downloadState
    }

    suspend fun containsDownloadInfo(link: String): Boolean {
        checkDownloadInfoMapInitialized()
        return downloadInfoMap.containsKey(link)
    }

    fun getSessionParams(link: String): SessionParamsBean? {
        return sessionParamsDao.getSessionParams(link)
    }

    fun getTorrentFilesByLink(link: String): List<TorrentFileBean> {
        return torrentFileDao.getTorrentFilesByLink(link = link)
    }

    suspend fun deleteDownloadInfo(link: String): Int {
        checkDownloadInfoMapInitialized()
        removeDownloadInfoFromMap(link)
        return downloadInfoDao.deleteDownloadInfo(link)
    }

    fun deleteSessionParams(link: String): Int {
        return sessionParamsDao.deleteSessionParams(link)
    }

    fun removeDownloadLinkUuidMap(link: String): Int {
        return downloadInfoDao.removeDownloadLinkUuidMap(link)
    }

    // update ---------------------------

    private suspend fun checkDownloadInfoMapInitialized() {
        withContext(Dispatchers.IO) {
            if (!this@DownloadManager::downloadInfoMap.isInitialized) {
                initDownloadInfoList()
            }
        }
    }

    private suspend fun initDownloadInfoList() {
        val workManager = WorkManager.getInstance(appContext)
        downloadInfoMap = downloadInfoDao.getAllDownloadListFlow().first().let { list ->
            val newList = list.map {
                when (it.downloadState) {
                    DownloadState.Downloading -> {
                        if (workManager.getWorkInfoById(UUID.fromString(it.downloadRequestId))
                                .get()?.state?.isFinished == true
                        ) return@map it
                        val result = downloadInfoDao.updateDownloadState(
                            link = it.link,
                            downloadState = DownloadState.Paused,
                        )
                        if (result > 0) {
                            it.copy(downloadState = DownloadState.Paused)
                        } else it
                    }

                    DownloadState.Seeding -> {
                        if (workManager.getWorkInfoById(UUID.fromString(it.downloadRequestId))
                                .get()?.state?.isFinished == true
                        ) return@map it
                        val result = downloadInfoDao.updateDownloadState(
                            link = it.link,
                            downloadState = DownloadState.SeedingPaused,
                        )
                        if (result > 0) {
                            it.copy(downloadState = DownloadState.SeedingPaused)
                        } else it
                    }

                    else -> it
                }
            }

            linkedMapOf(*(newList.map { it.link to it }.toTypedArray()))
        }
        downloadInfoListFlow = MutableStateFlow(downloadInfoMap.values.toList())
    }

    private suspend fun updateFlow() {
        checkDownloadInfoMapInitialized()
        downloadInfoListFlow.emit(downloadInfoMap.values.toList())
    }

    private suspend fun putDownloadInfoToMap(
        link: String,
        newInfo: DownloadInfoBean,
    ) {
        checkDownloadInfoMapInitialized()
        downloadInfoMap[link] = newInfo
        updateFlow()
    }

    private suspend fun updateDownloadInfoMap(
        link: String,
        newInfo: DownloadInfoBean.() -> DownloadInfoBean,
    ) {
        checkDownloadInfoMapInitialized()
        val downloadInfo = downloadInfoMap[link] ?: return
        downloadInfoMap[link] = downloadInfo.newInfo()
        updateFlow()
    }

    private suspend fun removeDownloadInfoFromMap(link: String) {
        checkDownloadInfoMapInitialized()
        downloadInfoMap.remove(link)
        updateFlow()
    }

    suspend fun updateDownloadInfo(downloadInfoBean: DownloadInfoBean) {
        downloadInfoDao.updateDownloadInfo(downloadInfoBean)
        putDownloadInfoToMap(link = downloadInfoBean.link, newInfo = downloadInfoBean)
    }

    suspend fun updateDownloadState(
        link: String,
        downloadState: DownloadState,
    ): Int {
        val result = downloadInfoDao.updateDownloadState(
            link = link,
            downloadState = downloadState,
        )
        if (result > 0) {
            updateDownloadInfoMap(link) { copy(downloadState = downloadState) }
        }
        return result
    }

    suspend fun updateDownloadStateAndSessionParams(
        link: String,
        sessionStateData: ByteArray,
        downloadState: DownloadState,
    ) {
        updateDownloadState(link, downloadState)
        sessionParamsDao.updateSessionParams(
            SessionParamsBean(
                link = link,
                data = sessionStateData,
            )
        )
    }

    suspend fun updateDownloadDescription(link: String, description: String): Int {
        val result = downloadInfoDao.updateDownloadDescription(
            link = link,
            description = description,
        )
        if (result > 0) {
            updateDownloadInfoMap(link) { copy(description = description) }
        }
        return result
    }

    fun updateTorrentFiles(files: List<TorrentFileBean>) {
        torrentFileDao.updateTorrentFiles(files)
    }

    suspend fun updateDownloadName(link: String, name: String?): Int {
        if (name.isNullOrBlank()) return 0
        val result = downloadInfoDao.updateDownloadName(
            link = link,
            name = name,
        )
        if (result > 0) {
            updateDownloadInfoMap(link) { copy(name = name) }
        }
        return result
    }

    suspend fun updateDownloadProgress(link: String, progress: Float): Int {
        val result = downloadInfoDao.updateDownloadProgress(
            link = link,
            progress = progress,
        )
        if (result > 0) {
            updateDownloadInfoMap(link) { copy(progress = progress) }
        }
        return result
    }

    suspend fun updateDownloadSize(link: String, size: Long): Int {
        val result = downloadInfoDao.updateDownloadSize(
            link = link,
            size = size,
        )
        if (result > 0) {
            updateDownloadInfoMap(link) { copy(size = size) }
        }
        return result
    }

    suspend fun updateDownloadInfoRequestId(link: String, downloadRequestId: String): Int {
        val result = downloadInfoDao.updateDownloadInfoRequestId(
            link = link,
            downloadRequestId = downloadRequestId,
        )
        if (result > 0) {
            updateDownloadInfoMap(link) { copy(downloadRequestId = downloadRequestId) }
        }
        return result
    }
}