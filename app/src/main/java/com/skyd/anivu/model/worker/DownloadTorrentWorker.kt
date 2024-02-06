package com.skyd.anivu.model.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.skyd.anivu.BuildConfig
import com.skyd.anivu.R
import com.skyd.anivu.appContext
import com.skyd.anivu.config.Const
import com.skyd.anivu.model.bean.SessionParamsBean
import com.skyd.anivu.model.bean.DownloadInfoBean
import com.skyd.anivu.model.db.dao.SessionParamsDao
import com.skyd.anivu.model.db.dao.DownloadInfoDao
import com.skyd.anivu.util.floatToPercentage
import com.skyd.anivu.util.uniqueInt
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.libtorrent4j.AlertListener
import org.libtorrent4j.SessionManager
import org.libtorrent4j.SessionParams
import org.libtorrent4j.TorrentHandle
import org.libtorrent4j.alerts.Alert
import org.libtorrent4j.alerts.TorrentAlert
import org.libtorrent4j.alerts.TorrentErrorAlert
import org.libtorrent4j.alerts.TorrentFinishedAlert
import org.libtorrent4j.swig.torrent_flags_t
import java.io.File
import java.util.UUID
import kotlin.coroutines.resumeWithException


class DownloadTorrentWorker(context: Context, parameters: WorkerParameters) :
    CoroutineWorker(context, parameters) {
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WorkerEntryPoint {
        val downloadInfoDao: DownloadInfoDao
        val sessionParamsDao: SessionParamsDao
    }

    private val hiltEntryPoint = EntryPointAccessors.fromApplication(
        appContext, WorkerEntryPoint::class.java
    )

    private lateinit var torrentLink: String
    private lateinit var articleId: String
    private var progress: Float = 0f
    private var fileName: String? = null
    private var path: String? = null

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val notificationId = uniqueInt()

    private val sessionManager = SessionManager(BuildConfig.DEBUG)

    override suspend fun doWork(): Result {
        withContext(Dispatchers.IO) {
            torrentLink = inputData.getString(TORRENT_LINK) ?: return@withContext Result.failure()
            articleId = inputData.getString(ARTICLE_ID) ?: return@withContext Result.failure()
            updateForeground("Starting Download")
            download(torrentLink)
        }
        return Result.success(
            workDataOf(
                STATE to (hiltEntryPoint.downloadInfoDao.getDownloadState(
                    articleId = articleId,
                    link = torrentLink
                )?.ordinal ?: 0),
                ARTICLE_ID to articleId,
                TORRENT_LINK to torrentLink,
            )
        )
    }

    private var sessionIsStopping: Boolean = false
    private suspend fun download(
        torrentLink: String,
        saveDir: File = Const.VIDEO_DIR
    ) = suspendCancellableCoroutine { continuation ->
        Log.e("TAG", "download: $torrentLink")
        if (!saveDir.exists() && !saveDir.mkdirs()) {
            continuation.resumeWithException(RuntimeException("Mkdirs failed: $saveDir"))
        }
        // Calls setForeground() periodically when it needs to update
        // the ongoing Notification8
        sessionManager.apply {
            addListener(object : AlertListener {
                override fun types(): IntArray? = null         // 监听所有类型的警报
                override fun alert(alert: Alert<*>?) {
                    Log.w("TAG", "alert: ${alert?.message()}")
                    // 处理 TorrentAlert 类型的警报，它包含了下载相关的信息
                    if (alert is TorrentErrorAlert) {
                        this@DownloadTorrentWorker.pause(handle = alert.handle())
                        continuation.resumeWithException(RuntimeException(alert.message()))
                    } else if (alert is TorrentFinishedAlert) {
                        val handle = alert.handle()
                        progress = 1f
                        fileName = handle.name
                        path = handle.savePath() + "/" + fileName
                        updateDownloadStateAndSessionParams(DownloadInfoBean.DownloadState.Completed)
                        continuation.resume(Unit, null)
                    } else if (alert is TorrentAlert<*>) {
                        // 检查是否是下载进度更新警报
                        val handle = alert.handle() ?: return
                        Log.w("TAG", "handle: ${handle.status().progress()}")
                        if (handle.isValid) {
                            val status = handle.status()
                            progress = status.progress()
                            fileName = handle.name
                            path = handle.savePath() + "/" + fileName
                            // 更新下载进度
                            updateForegroundAsync()
                        }
                    }
                    if (isStopped && !sessionIsStopping) {
                        val handle = (alert as? TorrentAlert)?.handle() ?: return
                        sessionIsStopping = true
                        this@DownloadTorrentWorker.pause(handle = handle)
                        continuation.resume(Unit, null)
                    }
                }
            })
            howToDownload(continuation = continuation, saveDir = saveDir)
        }
    }

    private fun howToDownload(continuation: CancellableContinuation<Unit>, saveDir: File) {
        sessionManager.apply {
            val lastSessionParams = hiltEntryPoint.sessionParamsDao.getSessionParams(
                articleId = articleId,
                link = torrentLink,
            )
            val sessionParams = if (lastSessionParams == null) SessionParams()
            else SessionParams(lastSessionParams.data)

            start(sessionParams)

            if (hiltEntryPoint.downloadInfoDao.containsDownloadInfo(
                    articleId = articleId,
                    link = torrentLink
                ) > 0
            ) {
                hiltEntryPoint.downloadInfoDao.updateDownloadInfoRequestId(
                    articleId = articleId,
                    link = torrentLink,
                    downloadRequestId = id.toString(),
                )
            }
            val state = hiltEntryPoint.downloadInfoDao.getDownloadState(
                articleId = articleId,
                link = torrentLink,
            )
            when (state) {
                null,
                DownloadInfoBean.DownloadState.Init -> {
                    download(torrentLink, saveDir, torrent_flags_t())
                    updateDownloadStateAndSessionParams(DownloadInfoBean.DownloadState.Downloading)
                }

                DownloadInfoBean.DownloadState.Downloading,
                DownloadInfoBean.DownloadState.Paused -> {
                    download(torrentLink, saveDir, torrent_flags_t())
                    updateDownloadStateAndSessionParams(DownloadInfoBean.DownloadState.Downloading)
                }

                DownloadInfoBean.DownloadState.Completed -> {
                    stop()
                    continuation.resume(Unit, null)
                }
            }
        }
    }

    private suspend fun updateForeground(text: String) {
        setForeground(createForegroundInfo(text))
        updateDownloadVideoInfoToDb()
    }

    private fun updateForegroundAsync() {
        updateForegroundAsync(floatToPercentage(progress))
    }

    private fun updateForegroundAsync(text: String) {
        setForegroundAsync(createForegroundInfo(text))
        updateDownloadVideoInfoToDb()
    }

    // Creates an instance of ForegroundInfo which can be used to update the
    // ongoing notification.
    private fun createForegroundInfo(progress: String): ForegroundInfo {
        val title = applicationContext.getString(R.string.download_torrent_title)
        val cancel = applicationContext.getString(R.string.cancel)
        // This PendingIntent can be used to cancel the worker
        val intent = WorkManager.getInstance(applicationContext)
            .createCancelPendingIntent(id)

        // Create a Notification channel if necessary
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel()
        }

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle(title)
            .setTicker(title)
            .setContentText(progress)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setOngoing(true)
            // Add the cancel action to the notification which can
            // be used to cancel the worker
            .addAction(android.R.drawable.ic_delete, cancel, intent)
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

    fun pause(handle: TorrentHandle) {
        updateDownloadStateAndSessionParams(DownloadInfoBean.DownloadState.Paused)
        handle.saveResumeData()

        sessionManager.remove(handle)
        sessionManager.pause()
        sessionManager.stop()
//        hiltEntryPoint.sessionParamsDao.updateSessionParams(
//            SessionParamsBean(
//                articleId = articleId,
//                link = torrentLink,
//                data = (sessionManager.saveState() ?: byteArrayOf())
//            )
//        )
    }

    private fun updateDownloadStateAndSessionParams(downloadState: DownloadInfoBean.DownloadState) {
        hiltEntryPoint.sessionParamsDao.updateSessionParams(
            SessionParamsBean(
                articleId = articleId,
                link = torrentLink,
                data = sessionManager.saveState() ?: byteArrayOf()
            )
        )
        hiltEntryPoint.downloadInfoDao.updateDownloadState(
            articleId = articleId,
            link = torrentLink,
            downloadState = downloadState,
        )
    }

    private fun updateDownloadVideoInfoToDb() {
        hiltEntryPoint.downloadInfoDao.apply {
            val video = getDownloadInfo(articleId = articleId, link = torrentLink)
            if (video != null) {
                updateDownloadInfo(
                    articleId = articleId,
                    link = torrentLink,
                    name = fileName ?: torrentLink.substringAfterLast('/'),
                    file = path,
                    size = sessionManager.stats().totalDownload(),
                    progress = progress,
                )
            } else {
                updateDownloadInfo(
                    DownloadInfoBean(
                        articleId = articleId,
                        link = torrentLink,
                        name = fileName ?: torrentLink.substringAfterLast('/'),
                        file = null,
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
        const val TORRENT_LINK = "torrentLink"
        const val ARTICLE_ID = "articleId"
        const val CHANNEL_ID = "downloadTorrent"
        const val CHANNEL_NAME = "downloadMessage"

        fun startWorker(context: Context, torrentLink: String, articleId: String) {
            val sendLogsWorkRequest = OneTimeWorkRequestBuilder<DownloadTorrentWorker>()
                .setInputData(
                    workDataOf(
                        TORRENT_LINK to /*"magnet:?xt=urn:btih:XF5KF6TRZBS3D6UTUXC3S26TO3SRCAXB&dn=&tr=http%3A%2F%2F104.143.10.186%3A8000%2Fannounce&tr=udp%3A%2F%2F104.143.10.186%3A8000%2Fannounce&tr=http%3A%2F%2Ftracker.openbittorrent.com%3A80%2Fannounce&tr=http%3A%2F%2Ftracker3.itzmx.com%3A6961%2Fannounce&tr=http%3A%2F%2Ftracker4.itzmx.com%3A2710%2Fannounce&tr=http%3A%2F%2Ftracker.publicbt.com%3A80%2Fannounce&tr=http%3A%2F%2Ftracker.prq.to%2Fannounce&tr=http%3A%2F%2Fopen.acgtracker.com%3A1096%2Fannounce&tr=https%3A%2F%2Ft-115.rhcloud.com%2Fonly_for_ylbud&tr=http%3A%2F%2Ftracker1.itzmx.com%3A8080%2Fannounce&tr=http%3A%2F%2Ftracker2.itzmx.com%3A6961%2Fannounce&tr=udp%3A%2F%2Ftracker1.itzmx.com%3A8080%2Fannounce&tr=udp%3A%2F%2Ftracker2.itzmx.com%3A6961%2Fannounce&tr=udp%3A%2F%2Ftracker3.itzmx.com%3A6961%2Fannounce&tr=udp%3A%2F%2Ftracker4.itzmx.com%3A2710%2Fannounce&tr=http%3A%2F%2Ftracker.opentrackr.org%3A1337%2Fannounce&tr=wss%3A%2F%2Ftracker.openwebtorrent.com%3A443%2Fannounce"
                  */  torrentLink,
                        ARTICLE_ID to articleId
                    )
                )
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                torrentLink,
                ExistingWorkPolicy.KEEP,
                sendLogsWorkRequest
            )
        }

        fun pause(context: Context, requestId: String) {
            WorkManager.getInstance(context)
                .cancelWorkById(UUID.fromString(requestId))
        }
    }
}