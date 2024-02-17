package com.skyd.anivu.model.worker.download

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
import android.database.sqlite.SQLiteConstraintException
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.navigation.NavDeepLinkBuilder
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.skyd.anivu.BuildConfig
import com.skyd.anivu.R
import com.skyd.anivu.appContext
import com.skyd.anivu.config.Const
import com.skyd.anivu.ext.ifNullOfBlank
import com.skyd.anivu.ext.saveTo
import com.skyd.anivu.ext.toDecodedUrl
import com.skyd.anivu.ext.validateFileName
import com.skyd.anivu.model.bean.DownloadInfoBean
import com.skyd.anivu.model.bean.DownloadLinkUuidMapBean
import com.skyd.anivu.model.bean.SessionParamsBean
import com.skyd.anivu.model.db.dao.DownloadInfoDao
import com.skyd.anivu.model.db.dao.SessionParamsDao
import com.skyd.anivu.model.repository.DownloadRepository
import com.skyd.anivu.model.service.HttpService
import com.skyd.anivu.util.floatToPercentage
import com.skyd.anivu.util.uniqueInt
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.libtorrent4j.AlertListener
import org.libtorrent4j.SessionManager
import org.libtorrent4j.SessionParams
import org.libtorrent4j.TorrentHandle
import org.libtorrent4j.TorrentInfo
import org.libtorrent4j.alerts.Alert
import org.libtorrent4j.alerts.FileErrorAlert
import org.libtorrent4j.alerts.MetadataReceivedAlert
import org.libtorrent4j.alerts.StateChangedAlert
import org.libtorrent4j.alerts.TorrentAlert
import org.libtorrent4j.alerts.TorrentErrorAlert
import org.libtorrent4j.alerts.TorrentFinishedAlert
import org.libtorrent4j.swig.torrent_flags_t
import retrofit2.Retrofit
import java.io.File
import java.util.UUID
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resumeWithException
import kotlin.random.Random


class DownloadTorrentWorker(context: Context, parameters: WorkerParameters) :
    CoroutineWorker(context, parameters) {
    private lateinit var torrentLinkUuid: String
    private lateinit var torrentLink: String
    private var progress: Float = 0f
        set(value) {
            field = value
            notificationContentText = floatToPercentage(value)
        }
    private var notificationContentText: String = "Starting Download"
    private var name: String? = null
    private lateinit var tempDownloadingDirName: String
    private var description: String? = null

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val notificationId = uniqueInt()

    private val sessionManager = SessionManager(BuildConfig.DEBUG)

    override suspend fun doWork(): Result {
        withContext(Dispatchers.IO) {
            coroutineContext.job.invokeOnCompletion {
                if (it is CancellationException) {
                    this@DownloadTorrentWorker.pause(handle = null)
                }
            }
            torrentLinkUuid =
                inputData.getString(TORRENT_LINK_UUID) ?: return@withContext Result.failure()
            torrentLink = hiltEntryPoint.downloadInfoDao.getDownloadLinkByUuid(torrentLinkUuid)
                ?: return@withContext Result.failure()
            name = hiltEntryPoint.downloadInfoDao.getDownloadName(link = torrentLink)
            tempDownloadingDirName = hiltEntryPoint.downloadInfoDao
                .getDownloadingDirName(link = torrentLink)
                .ifNullOfBlank { "${System.currentTimeMillis()}_${Random.nextLong()}" }
            updateNotification()
            updateAllDownloadVideoInfoToDb()
            workerDownload()
        }
        hiltEntryPoint.downloadInfoDao.removeDownloadLinkByUuid(torrentLinkUuid)
        return Result.success(
            workDataOf(
                STATE to (hiltEntryPoint.downloadInfoDao
                    .getDownloadState(link = torrentLink)?.ordinal ?: 0),
                TORRENT_LINK_UUID to torrentLinkUuid,
            )
        )
    }

    private var sessionIsStopping: Boolean = false
    private suspend fun workerDownload(
        saveDir: File = File(Const.DOWNLOADING_VIDEO_DIR, tempDownloadingDirName)
    ) = suspendCancellableCoroutine { continuation ->
        if (!saveDir.exists() && !saveDir.mkdirs()) {
            continuation.resumeWithException(RuntimeException("Mkdirs failed: $saveDir"))
        }
        sessionManager.apply {
            addListener(object : AlertListener {
                override fun types(): IntArray? = null         // 监听所有类型的警报
                override fun alert(alert: Alert<*>?) {
                    if (alert == null) return

                    onAlert(continuation, alert)

                    if (isStopped && !sessionIsStopping) {
                        val handle = (alert as? TorrentAlert)?.handle() ?: return
                        this@DownloadTorrentWorker.pause(handle = handle)
                        continuation.resume(Unit, null)
                    }
                }
            })

            // 这里不是挂起函数，因此外面的job.invokeOnCompletion不能捕获到异常，需要手动runCatching
            runCatching {
                howToDownload(continuation = continuation, saveDir = saveDir)
            }.onFailure {
                this@DownloadTorrentWorker.pause(handle = null)
                continuation.resumeWithException(it)
            }
        }
    }

    private fun howToDownload(continuation: CancellableContinuation<Unit>, saveDir: File) {
        sessionManager.apply {
            val lastSessionParams = hiltEntryPoint.sessionParamsDao
                .getSessionParams(link = torrentLink)
            val sessionParams = if (lastSessionParams == null) SessionParams()
            else SessionParams(lastSessionParams.data)

            start(sessionParams)

            if (hiltEntryPoint.downloadInfoDao.containsDownloadInfo(link = torrentLink) > 0) {
                hiltEntryPoint.downloadInfoDao.updateDownloadInfoRequestId(
                    link = torrentLink,
                    downloadRequestId = id.toString(),
                )
            }
            when (hiltEntryPoint.downloadInfoDao.getDownloadState(link = torrentLink)) {
                null,
                    // 重新下载
                DownloadInfoBean.DownloadState.Completed,/* -> {
                    stop()
                    continuation.resume(Unit, null)
                }*/
                DownloadInfoBean.DownloadState.Init -> {
                    downloadByMagnetOrTorrent(torrentLink, saveDir)
                    updateDownloadStateAndSessionParams(DownloadInfoBean.DownloadState.Downloading)
                }

                DownloadInfoBean.DownloadState.Downloading,
                DownloadInfoBean.DownloadState.ErrorPaused,
                DownloadInfoBean.DownloadState.Paused -> {
                    downloadByMagnetOrTorrent(torrentLink, saveDir)
                    updateDownloadStateAndSessionParams(DownloadInfoBean.DownloadState.Downloading)
                }
            }
        }
    }

    private fun downloadByMagnetOrTorrent(
        link: String,
        saveDir: File,
    ) {
        if (link.startsWith("magnet:")) {
            sessionManager.download(link, saveDir, torrent_flags_t())
        } else if (
            (link.startsWith("http:") || link.startsWith("https:")) &&
            link.endsWith(".torrent")
        ) {
            val tempTorrentFile = File(
                Const.TEMP_TORRENT_DIR,
                link.substringAfterLast('/').toDecodedUrl().validateFileName()
            )
            hiltEntryPoint.retrofit.create(HttpService::class.java)
                .requestGetResponseBody(link).execute().body()!!.byteStream()
                .use { it.saveTo(tempTorrentFile) }
            sessionManager.download(TorrentInfo(tempTorrentFile), saveDir)
        } else {
            error("Unsupported link: $link")
        }
    }

    override suspend fun getForegroundInfo() = createForegroundInfo()

    private suspend fun updateNotification() {
        runCatching {
            setForeground(createForegroundInfo())
        }.onFailure { it.printStackTrace() }
    }

    private fun updateNotificationAsync() {
        runCatching {
            setForegroundAsync(createForegroundInfo())
        }.onFailure { it.printStackTrace() }
    }

    // Creates an instance of ForegroundInfo which can be used to update the ongoing notification.
    private fun createForegroundInfo(): ForegroundInfo {
        val title = name.ifNullOfBlank {
            applicationContext.getString(R.string.downloading)
        }
        // This PendingIntent can be used to cancel the worker
        val cancelIntent = WorkManager.getInstance(applicationContext)
            .createCancelPendingIntent(id)
        val contentIntent = NavDeepLinkBuilder(applicationContext)
            .setGraph(R.navigation.nav_graph)
            .setDestination(R.id.download_fragment)
            .createPendingIntent()

        // Create a Notification channel if necessary
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel()
        }

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle(title)
            .setTicker(title)
            .setContentText(notificationContentText)
            .setSmallIcon(R.drawable.ic_icon_24)
            .setOngoing(true)
            .setProgress(100, (progress * 100).toInt(), false)
            // Add the cancel action to the notification which can be used to cancel the worker
            .addAction(
                R.drawable.ic_pause_24,
                applicationContext.getString(R.string.download_pause),
                cancelIntent,
            )
            .setContentIntent(contentIntent)
            .build()

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ForegroundInfo(
                notificationId,
                notification,
                FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            ForegroundInfo(notificationId, notification)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }

    private fun onAlert(continuation: CancellableContinuation<Unit>, alert: Alert<*>) {
        when (alert) {
            is TorrentErrorAlert -> {
                // 下载错误更新
                this@DownloadTorrentWorker.pause(
                    handle = alert.handle(),
                    state = DownloadInfoBean.DownloadState.ErrorPaused,
                )
                continuation.resumeWithException(RuntimeException(alert.message()))
            }
            // If the storage fails to read or write files that it needs access to,
            // this alert is generated and the torrent is paused.
            is FileErrorAlert -> {
                // 文件错误，例如存储空间已满
                this@DownloadTorrentWorker.pause(
                    handle = alert.handle(),
                    state = DownloadInfoBean.DownloadState.ErrorPaused,
                )
                continuation.resumeWithException(RuntimeException(alert.message()))
            }

            is TorrentFinishedAlert -> {
                // 下载完成更新
                val handle = alert.handle()
                progress = 1f
                name = handle.name
                updateNotificationAsync()     // 更新Notification
                updateDownloadStateAndSessionParams(DownloadInfoBean.DownloadState.Completed)
                moveFromDownloadingDirToVideoDir()
                continuation.resume(Unit, null)
            }

            is MetadataReceivedAlert -> {
                // 元数据更新
                val handle = alert.handle()
                name = handle.name
                updateNotificationAsync()     // 更新Notification
                updateNameInfoToDb()
            }

            is StateChangedAlert -> {
                // 下载状态更新
                description = alert.state.toDisplayString(context = applicationContext)
                updateDescriptionInfoToDb()
                val handle = alert.handle()
                if (handle.isValid) {
                    progress = handle.status().progress()
                    updateNotificationAsync()     // 更新Notification
                    updateProgressInfoToDb()
                }
            }

            is TorrentAlert<*> -> {
//                Log.e("TAG", "onAlert: ${alert}")
                // 下载进度更新
                val handle = alert.handle()
                if (handle.isValid) {
                    if (progress != handle.status().progress()) {
                        progress = handle.status().progress()
                        updateNotificationAsync()     // 更新Notification
                        updateProgressInfoToDb()
                        updateSizeInfoToDb()
                    }
                }
            }
        }
    }

    private fun pause(
        handle: TorrentHandle?,
        state: DownloadInfoBean.DownloadState = DownloadInfoBean.DownloadState.Paused
    ) {
        if (!sessionManager.isRunning || sessionIsStopping) {
            return
        }
        sessionIsStopping = true
        updateDownloadStateAndSessionParams(state)
        if (handle != null) {
            handle.saveResumeData()
            sessionManager.remove(handle)
        }

        sessionManager.pause()
        sessionManager.stopDht()
        sessionManager.stop()
    }

    private fun moveFromDownloadingDirToVideoDir() {
        val downloadingDir = File(Const.DOWNLOADING_VIDEO_DIR, tempDownloadingDirName)
        downloadingDir.listFiles()?.forEach {
            it.copyRecursively(File(Const.VIDEO_DIR, it.name), true)
            it.deleteRecursively()
        }
        downloadingDir.deleteRecursively()
    }

    private fun updateDownloadStateAndSessionParams(downloadState: DownloadInfoBean.DownloadState) {
        try {
            hiltEntryPoint.sessionParamsDao.updateSessionParams(
                SessionParamsBean(
                    link = torrentLink,
                    data = sessionManager.saveState() ?: byteArrayOf()
                )
            )
            hiltEntryPoint.downloadInfoDao.updateDownloadState(
                link = torrentLink,
                downloadState = downloadState,
            )
        } catch (e: SQLiteConstraintException) {
            // 捕获link外键约束异常
            e.printStackTrace()
        }
    }

    private fun updateNameInfoToDb() {
        hiltEntryPoint.downloadInfoDao.apply {
            val lastName = getDownloadName(link = torrentLink)
            if (lastName == null) {
                updateAllDownloadVideoInfoToDb()
            } else {
                if (lastName != name) {
                    updateDownloadName(
                        link = torrentLink,
                        name = name.ifNullOfBlank { lastName },
                    ).apply { setProgressAsync(workDataOf("data" to progress)) }
                }
            }
        }
    }

    private fun updateSizeInfoToDb() {
        hiltEntryPoint.downloadInfoDao.apply {
            val lastSize = getDownloadSize(link = torrentLink)
            if (lastSize == null) {
                updateAllDownloadVideoInfoToDb()
            } else {
                val size = sessionManager.stats().totalDownload()
                if (size != lastSize) {
                    updateDownloadSize(
                        link = torrentLink,
                        size = size,
                    )
                }
            }
        }
    }

    private fun updateProgressInfoToDb() {
        hiltEntryPoint.downloadInfoDao.apply {
            val lastProgress = getDownloadProgress(link = torrentLink)
            if (lastProgress == null) {
                updateAllDownloadVideoInfoToDb()
            } else {
                if (lastProgress != progress) {
                    updateDownloadProgress(
                        link = torrentLink,
                        progress = progress,
                    ).apply { setProgressAsync(workDataOf("data" to progress)) }
                }
            }
        }
    }

    private fun updateDescriptionInfoToDb() {
        hiltEntryPoint.downloadInfoDao.apply {
            val lastDescription = getDownloadDescription(link = torrentLink)
            if (lastDescription == null &&
                getDownloadInfo(link = torrentLink) == null
            ) {
                updateAllDownloadVideoInfoToDb()
            } else {
                if (lastDescription != description) {
                    updateDownloadDescription(
                        link = torrentLink,
                        description = description,
                    )
                }
            }
        }
    }

    private fun updateAllDownloadVideoInfoToDb() {
        hiltEntryPoint.downloadInfoDao.apply {
            val video = getDownloadInfo(link = torrentLink)
            if (video != null) {
                updateDownloadInfo(
                    link = torrentLink,
                    name = name.ifNullOfBlank {
                        torrentLink.substringAfterLast('/')
                            .toDecodedUrl()
                            .validateFileName()
                    },
                    size = sessionManager.stats().totalDownload(),
                    progress = progress,
                ).apply { setProgressAsync(workDataOf("data" to progress)) }
            } else {
                updateDownloadInfo(
                    DownloadInfoBean(
                        link = torrentLink,
                        name = name.ifNullOfBlank {
                            torrentLink.substringAfterLast('/')
                                .toDecodedUrl()
                                .validateFileName()
                        },
                        downloadingDirName = tempDownloadingDirName,
                        downloadDate = System.currentTimeMillis(),
                        size = sessionManager.stats().totalDownload(),
                        progress = progress,
                        downloadRequestId = id.toString(),
                    ).apply { setProgressAsync(workDataOf("data" to progress)) }
                )
            }
        }
    }

    companion object {
        const val STATE = "state"
        const val TORRENT_LINK_UUID = "torrentLinkUuid"
        const val CHANNEL_ID = "downloadTorrent"
        const val CHANNEL_NAME = "downloadMessage"

        private val coroutineScope = CoroutineScope(Dispatchers.IO)

        @EntryPoint
        @InstallIn(SingletonComponent::class)
        interface WorkerEntryPoint {
            val retrofit: Retrofit
            val downloadInfoDao: DownloadInfoDao
            val sessionParamsDao: SessionParamsDao
            val downloadRepository: DownloadRepository
        }

        private val hiltEntryPoint = EntryPointAccessors.fromApplication(
            appContext, WorkerEntryPoint::class.java
        )

        fun startWorker(context: Context, torrentLink: String) {
            val torrentLinkUuid = UUID.randomUUID().toString()
            coroutineScope.launch {
                hiltEntryPoint.downloadInfoDao.setDownloadLinkUuidMap(
                    DownloadLinkUuidMapBean(
                        link = torrentLink,
                        uuid = torrentLinkUuid,
                    )
                )
                val sendLogsWorkRequest = OneTimeWorkRequestBuilder<DownloadTorrentWorker>()
                    .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                    .setInputData(workDataOf(TORRENT_LINK_UUID to torrentLinkUuid))
                    .build()
                WorkManager.getInstance(context).enqueueUniqueWork(
                    torrentLinkUuid,
                    ExistingWorkPolicy.KEEP,
                    sendLogsWorkRequest
                )
            }
        }

        fun pause(context: Context, requestId: String) {
            WorkManager.getInstance(context)
                .cancelWorkById(UUID.fromString(requestId))
        }

        fun cancel(
            context: Context,
            requestId: String,
            link: String,
            downloadingDirName: String
        ) {
            val flow = WorkManager.getInstance(context)
                .getWorkInfoByIdFlow(UUID.fromString(requestId))
            // 在worker结束后删除数据库中的下载任务信息
            coroutineScope.launch {
                flow.filter { it == null || it.state.isFinished }
                    .flatMapConcat {
                        delay(2000)
                        hiltEntryPoint.downloadRepository.deleteDownloadTaskInfo(
                            link = link,
                            downloadingDirName = downloadingDirName,
                        )
                    }.collect {
                        cancel()
                    }
            }
            pause(context, requestId)
        }
    }
}