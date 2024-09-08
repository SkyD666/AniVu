package com.skyd.anivu.model.repository.download

import com.skyd.anivu.base.mvi.MviIntent
import com.skyd.anivu.model.bean.download.DownloadInfoBean
import com.skyd.anivu.model.bean.download.DownloadInfoBean.DownloadState
import com.skyd.anivu.model.bean.download.TorrentFileBean

sealed interface DownloadManagerIntent : MviIntent {
    data class UpdateDownloadInfo(val downloadInfoBean: DownloadInfoBean) : DownloadManagerIntent
    data class UpdateSessionParams(
        val link: String,
        val sessionStateData: ByteArray,
    ) : DownloadManagerIntent {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as UpdateSessionParams

            if (link != other.link) return false
            if (!sessionStateData.contentEquals(other.sessionStateData)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = link.hashCode()
            result = 31 * result + sessionStateData.contentHashCode()
            return result
        }
    }

    data class UpdateDownloadProgress(val link: String, val progress: Float) : DownloadManagerIntent
    data class UpdateDownloadState(
        val link: String,
        val downloadState: DownloadState,
    ) : DownloadManagerIntent

    data class UpdateDownloadSize(val link: String, val size: Long) : DownloadManagerIntent
    data class UpdateDownloadName(val link: String, val name: String?) : DownloadManagerIntent
    data class UpdateDownloadInfoRequestId(val link: String, val downloadRequestId: String) :
        DownloadManagerIntent

    data class UpdateTorrentFiles(val files: List<TorrentFileBean>) : DownloadManagerIntent
    data class UpdateDownloadDescription(val link: String, val description: String) :
        DownloadManagerIntent
}