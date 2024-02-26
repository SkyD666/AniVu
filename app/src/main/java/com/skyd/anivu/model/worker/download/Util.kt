package com.skyd.anivu.model.worker.download

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import com.skyd.anivu.R
import com.skyd.anivu.config.Const
import com.skyd.anivu.ext.ifNullOfBlank
import com.skyd.anivu.ext.toDecodedUrl
import com.skyd.anivu.ext.validateFileName
import com.skyd.anivu.model.bean.download.DownloadInfoBean
import com.skyd.anivu.model.bean.download.SessionParamsBean
import com.skyd.anivu.model.bean.download.TorrentFileBean
import org.libtorrent4j.FileStorage
import org.libtorrent4j.TorrentStatus
import org.libtorrent4j.Vectors
import org.libtorrent4j.alerts.SaveResumeDataAlert
import org.libtorrent4j.swig.add_torrent_params
import org.libtorrent4j.swig.error_code
import org.libtorrent4j.swig.libtorrent
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


fun TorrentStatus.State.toDisplayString(context: Context): String {
    return when (this) {
        TorrentStatus.State.CHECKING_FILES -> context.getString(R.string.torrent_status_checking_files)
        TorrentStatus.State.DOWNLOADING_METADATA -> context.getString(R.string.torrent_status_downloading_metadata)
        TorrentStatus.State.DOWNLOADING -> context.getString(R.string.torrent_status_downloading)
        TorrentStatus.State.FINISHED -> context.getString(R.string.torrent_status_finished)
        TorrentStatus.State.SEEDING -> context.getString(R.string.torrent_status_seeding)
        TorrentStatus.State.CHECKING_RESUME_DATA -> context.getString(R.string.torrent_status_checking_resume_data)
        TorrentStatus.State.UNKNOWN -> ""
    }
}

internal fun getWhatPausedState(oldState: DownloadInfoBean.DownloadState?) =
    when (oldState) {
        DownloadInfoBean.DownloadState.Seeding,
        DownloadInfoBean.DownloadState.Completed,
        DownloadInfoBean.DownloadState.SeedingPaused -> {
            DownloadInfoBean.DownloadState.SeedingPaused
        }

        else -> {
            DownloadInfoBean.DownloadState.Paused
        }
    }

internal fun updateDownloadState(
    link: String,
    downloadState: DownloadInfoBean.DownloadState,
): Boolean {
    try {
        val result = DownloadTorrentWorker.hiltEntryPoint.downloadInfoDao.updateDownloadState(
            link = link,
            downloadState = downloadState,
        )
        if (result == 0) {
            Log.w(
                DownloadTorrentWorker.TAG,
                "updateDownloadState return 0. downloadState: $downloadState"
            )
        }
        return result != 0
    } catch (e: SQLiteConstraintException) {
        // 捕获link外键约束异常
        e.printStackTrace()
    }
    return false
}

internal fun updateDownloadStateAndSessionParams(
    link: String,
    sessionStateData: ByteArray,
    downloadState: DownloadInfoBean.DownloadState,
) {
    updateDownloadState(link, downloadState)
    try {
        DownloadTorrentWorker.hiltEntryPoint.sessionParamsDao.updateSessionParams(
            SessionParamsBean(
                link = link,
                data = sessionStateData,
            )
        )
    } catch (e: SQLiteConstraintException) {
        // 捕获link外键约束异常
        e.printStackTrace()
    }
}

internal fun updateDescriptionInfoToDb(link: String, description: String): Boolean {
    DownloadTorrentWorker.hiltEntryPoint.downloadInfoDao.apply {
        val result = updateDownloadDescription(
            link = link,
            description = description,
        )
        if (result == 0) {
            Log.w(
                DownloadTorrentWorker.TAG,
                "updateDownloadDescription return 0. description: $description"
            )
        }
        return result != 0
    }
}

internal fun updateTorrentFilesToDb(link: String, files: FileStorage): Boolean {
    DownloadTorrentWorker.hiltEntryPoint.torrentFileDao.apply {
        val list = mutableListOf<TorrentFileBean>()
        runCatching {
            for (i in 0..<files.numFiles()) {
                list.add(
                    TorrentFileBean(
                        link = link,
                        path = files.filePath(i),
                        size = files.fileSize(i),
                    )
                )
            }
        }.onFailure {
            return false
        }
        updateTorrentFiles(list)
        return true
    }
}

internal fun updateNameInfoToDb(link: String, name: String?): Boolean {
    if (name.isNullOrBlank()) return false
    DownloadTorrentWorker.hiltEntryPoint.downloadInfoDao.apply {
        val result = updateDownloadName(
            link = link,
            name = name,
        )
        if (result == 0) {
            Log.w(DownloadTorrentWorker.TAG, "updateDownloadName return 0. name: $name")
        }
        return result != 0
    }
}

internal fun updateProgressInfoToDb(link: String, progress: Float): Boolean {
    DownloadTorrentWorker.hiltEntryPoint.downloadInfoDao.apply {
        val result = updateDownloadProgress(
            link = link,
            progress = progress,
        )
        if (result == 0) {
            Log.w(DownloadTorrentWorker.TAG, "updateDownloadProgress return 0. progress: $progress")
        }
        return result != 0
    }
}

internal fun updateSizeInfoToDb(link: String, size: Long): Boolean {
    DownloadTorrentWorker.hiltEntryPoint.downloadInfoDao.apply {
        val result = updateDownloadSize(
            link = link,
            size = size,
        )
        if (result == 0) {
            Log.w(DownloadTorrentWorker.TAG, "updateDownloadSize return 0. size: $size")
        }
        return result != 0
    }
}

/**
 * 添加新的下载信息（之前没下载过的）
 */
internal fun addNewDownloadInfoToDbIfNotExists(
    forceAdd: Boolean = false,
    link: String,
    name: String?,
    progress: Float,
    size: Long,
    downloadingDirName: String,
    downloadRequestId: String,
) {
    DownloadTorrentWorker.hiltEntryPoint.downloadInfoDao.apply {
        if (!forceAdd) {
            val video = getDownloadInfo(link = link)
            if (video != null) return
        }
        updateDownloadInfo(
            DownloadInfoBean(
                link = link,
                name = name.ifNullOfBlank {
                    link.substringAfterLast('/')
                        .toDecodedUrl()
                        .validateFileName()
                },
                downloadingDirName = downloadingDirName,
                downloadDate = System.currentTimeMillis(),
                size = size,
                progress = progress,
                downloadRequestId = downloadRequestId,
            )
        )
    }
}

internal fun updateAllDownloadVideoInfoToDb(
    link: String,
    name: String?,
    progress: Float,
    size: Long,
    downloadingDirName: String,
    downloadRequestId: String,
) {
    DownloadTorrentWorker.hiltEntryPoint.downloadInfoDao.apply {
        val video = getDownloadInfo(link = link)
        if (video != null) {
            updateDownloadInfo(
                link = link,
                name = name.ifNullOfBlank {
                    link.substringAfterLast('/')
                        .toDecodedUrl()
                        .validateFileName()
                },
                size = size,
                progress = progress,
            )
        } else {
            addNewDownloadInfoToDbIfNotExists(
                forceAdd = true,
                link = link,
                name = name,
                progress = progress,
                size = size,
                downloadingDirName = downloadingDirName,
                downloadRequestId = downloadRequestId,
            )
        }
    }
}

fun serializeResumeData(name: String, alert: SaveResumeDataAlert) {
    val resume = File(Const.TORRENT_RESUME_DATA_DIR, name)
    if (!resume.exists()) resume.createNewFile()
    val data = libtorrent.write_resume_data(alert.params().swig()).bencode()
    try {
        FileOutputStream(resume).use { it.write(Vectors.byte_vector2bytes(data)) }
    } catch (e: IOException) {
        Log.e("serializeResumeData", "Error saving resume data")
    }
}

fun readResumeData(name: String): add_torrent_params? {
    val resume = File(Const.TORRENT_RESUME_DATA_DIR, name)
    if (!resume.exists()) return null
    try {
        val data = resume.readBytes()
        val ec = error_code()
        val p: add_torrent_params =
            libtorrent.read_resume_data_ex(Vectors.bytes2byte_vector(data), ec)
        require(ec.value() == 0) { "Unable to read the resume data: " + ec.message() }
        return p
    } catch (e: Throwable) {
        Log.w("readResumeData", "Unable to set resume data: $e")
    }
    return null
}