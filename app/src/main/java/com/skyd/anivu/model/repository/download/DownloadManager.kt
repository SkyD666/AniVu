package com.skyd.anivu.model.repository.download

import android.app.Application
import android.app.NotificationManager
import android.content.Context
import com.skyd.anivu.R
import com.skyd.anivu.model.bean.download.DownloadInfoBean
import com.skyd.downloader.Downloader
import com.skyd.downloader.NotificationConfig
import com.skyd.downloader.Status
import com.skyd.downloader.db.DownloadEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DownloadManager private constructor(context: Context) {
    private val downloader = Downloader.init(
        context.applicationContext as Application,
        NotificationConfig(
            channelName = context.getString(R.string.download_channel_name),
            channelDescription = context.getString(R.string.download_channel_description),
            smallIcon = R.drawable.ic_icon_2_24,
            importance = NotificationManager.IMPORTANCE_LOW,
            pauseText = R.string.download_pause,
            resumeText = R.string.download_resume,
            cancelText = R.string.download_cancel,
            retryText = R.string.download_retry,
        )
    )
    val downloadInfoListFlow: Flow<List<DownloadInfoBean>> = downloader.observeDownloads()
        .map { list -> list.map { it.toDownloadInfoBean() } }

    fun download(
        url: String,
        path: String,
        fileName: String? = null,
    ): Any {
        return if (fileName == null) {
            downloader.download(url = url, path = path)
        } else {
            downloader.download(url = url, fileName = fileName, path = path)
        }
    }

    fun pause(id: Int) = downloader.pause(id)
    fun resume(id: Int) = downloader.resume(id)
    fun retry(id: Int) = downloader.retry(id)
    fun delete(id: Int) {
        downloader.find(id) { entity ->
            downloader.clearDb(
                id,
                deleteFile = entity != null && Status.valueOf(entity.status) != Status.Success,
            )
        }
    }

    private fun DownloadEntity.toDownloadInfoBean() = DownloadInfoBean(
        id = id,
        url = url,
        path = path,
        fileName = fileName,
        status = Status.valueOf(status),
        totalBytes = totalBytes,
        downloadedBytes = downloadedBytes,
        speedInBytePerMs = speedInBytePerMs,
        createTime = createTime,
        failureReason = failureReason,
    )

    companion object {
        @Volatile
        private var instance: DownloadManager? = null

        fun getInstance(context: Context): DownloadManager {
            if (instance == null) {
                synchronized(DownloadManager) {
                    if (instance == null) {
                        instance = DownloadManager(context)
                    }
                }
            }
            return instance!!
        }
    }
}