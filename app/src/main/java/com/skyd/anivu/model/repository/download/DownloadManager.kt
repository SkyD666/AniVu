package com.skyd.anivu.model.repository.download

import androidx.work.WorkManager
import com.skyd.anivu.appContext
import com.skyd.anivu.ext.debounceWithoutFirst
import com.skyd.anivu.model.bean.download.DownloadInfoBean
import com.skyd.anivu.model.bean.download.DownloadInfoBean.DownloadState
import com.skyd.anivu.model.bean.download.DownloadLinkUuidMapBean
import com.skyd.anivu.model.bean.download.SessionParamsBean
import com.skyd.anivu.model.bean.download.TorrentFileBean
import com.skyd.anivu.model.db.dao.DownloadInfoDao
import com.skyd.anivu.model.db.dao.SessionParamsDao
import com.skyd.anivu.model.db.dao.TorrentFileDao
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

object DownloadManager {
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface DownloadManagerPoint {
        val downloadInfoDao: DownloadInfoDao
        val sessionParamsDao: SessionParamsDao
        val torrentFileDao: TorrentFileDao
    }

    private val hiltEntryPoint = EntryPointAccessors.fromApplication(
        appContext, DownloadManagerPoint::class.java
    )

    private val downloadInfoDao = hiltEntryPoint.downloadInfoDao
    private val sessionParamsDao = hiltEntryPoint.sessionParamsDao
    private val torrentFileDao = hiltEntryPoint.torrentFileDao
    private val scope = CoroutineScope(Dispatchers.IO)
    private val intentFlow = MutableSharedFlow<DownloadManagerIntent>()
    private lateinit var downloadInfoMap: LinkedHashMap<String, DownloadInfoBean>
    private lateinit var downloadInfoListFlow: MutableStateFlow<List<DownloadInfoBean>>

    init {
        intentFlow
            .onEachIntent()
            .launchIn(scope)
    }

    private fun Flow<DownloadManagerIntent>.onEachIntent(): Flow<DownloadManagerIntent> {
        return merge(
            filterIsInstance<DownloadManagerIntent.UpdateDownloadInfo>()
                .debounceWithoutFirst(100)
                .onEach { intent ->
                    downloadInfoDao.updateDownloadInfo(intent.downloadInfoBean)
                    putDownloadInfoToMap(
                        link = intent.downloadInfoBean.link,
                        newInfo = intent.downloadInfoBean,
                    )
                }.catch { it.printStackTrace() },

            filterIsInstance<DownloadManagerIntent.UpdateSessionParams>()
                .onEach { intent ->
                    sessionParamsDao.updateSessionParams(
                        SessionParamsBean(
                            link = intent.link,
                            data = intent.sessionStateData,
                        )
                    )
                }.catch { it.printStackTrace() },

            filterIsInstance<DownloadManagerIntent.UpdateDownloadProgress>()
                .debounceWithoutFirst(1000)
                .onEach { intent ->
                    val result = downloadInfoDao.updateDownloadProgress(
                        link = intent.link,
                        progress = intent.progress,
                    )
                    if (result > 0) {
                        updateDownloadInfoMap(intent.link) { copy(progress = intent.progress) }
                    }
                }.catch { it.printStackTrace() },

            filterIsInstance<DownloadManagerIntent.UpdateDownloadState>()
                .onEach { intent ->
                    val result = downloadInfoDao.updateDownloadState(
                        link = intent.link,
                        downloadState = intent.downloadState,
                    )
                    if (result > 0) {
                        updateDownloadInfoMap(intent.link) { copy(downloadState = intent.downloadState) }
                    }
                }.catch { it.printStackTrace() },

            filterIsInstance<DownloadManagerIntent.UpdateDownloadSize>()
                .debounceWithoutFirst(1000)
                .onEach { intent ->
                    val result = downloadInfoDao.updateDownloadSize(
                        link = intent.link, size = intent.size,
                    )
                    if (result > 0) {
                        updateDownloadInfoMap(intent.link) { copy(size = intent.size) }
                    }
                }.catch { it.printStackTrace() },

            filterIsInstance<DownloadManagerIntent.UpdateDownloadName>()
                .debounceWithoutFirst(200)
                .onEach { intent ->
                    if (intent.name.isNullOrBlank()) return@onEach
                    val result = downloadInfoDao.updateDownloadName(
                        link = intent.link,
                        name = intent.name,
                    )
                    if (result > 0) {
                        updateDownloadInfoMap(intent.link) { copy(name = intent.name) }
                    }
                }.catch { it.printStackTrace() },

            filterIsInstance<DownloadManagerIntent.UpdateDownloadInfoRequestId>()
                .onEach { intent ->
                    val result = downloadInfoDao.updateDownloadInfoRequestId(
                        link = intent.link,
                        downloadRequestId = intent.downloadRequestId,
                    )
                    if (result > 0) {
                        updateDownloadInfoMap(intent.link) {
                            copy(downloadRequestId = intent.downloadRequestId)
                        }
                    }
                }.catch { it.printStackTrace() },

            filterIsInstance<DownloadManagerIntent.UpdateTorrentFiles>()
                .onEach { intent -> torrentFileDao.updateTorrentFiles(intent.files) }
                .catch { it.printStackTrace() },

            filterIsInstance<DownloadManagerIntent.UpdateDownloadDescription>()
                .debounceWithoutFirst(500)
                .onEach { intent ->
                    val result = downloadInfoDao.updateDownloadDescription(
                        link = intent.link,
                        description = intent.description,
                    )
                    if (result > 0) {
                        updateDownloadInfoMap(intent.link) { copy(description = intent.description) }
                    }
                }
                .catch { it.printStackTrace() },
        )
    }

    fun sendIntent(intent: DownloadManagerIntent) = scope.launch {
        intentFlow.emit(intent)
    }

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
}