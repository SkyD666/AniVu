package com.skyd.anivu.model.repository.download

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.core.net.toFile
import androidx.core.net.toUri
import com.skyd.anivu.R
import com.skyd.anivu.config.Const
import com.skyd.anivu.ext.copyTo
import com.skyd.anivu.ext.dataStore
import com.skyd.anivu.ext.fileName
import com.skyd.anivu.ext.getOrDefault
import com.skyd.anivu.ext.isLocal
import com.skyd.anivu.ext.validateFileName
import com.skyd.anivu.model.preference.data.medialib.MediaLibLocationPreference
import com.skyd.anivu.model.repository.download.bt.BtDownloadManager
import com.skyd.anivu.model.worker.download.isTorrentMimetype
import com.skyd.anivu.ui.component.showToast
import java.io.File

object DownloadStarter {
    fun download(context: Context, url: String, type: String? = null) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted =
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            if (granted == PermissionChecker.PERMISSION_DENIED) {
                context.getString(R.string.download_no_notification_permission_tip)
                    .showToast()
                return
            }
        }
        val isMagnetOrTorrent =
            url.startsWith("magnet:") || isTorrentMimetype(type) ||
                    Regex("^(((http|https|file|content)://)|/).*\\.torrent$").matches(url)
        if (isMagnetOrTorrent) {
            var uri = url.toUri()
            if (url.startsWith("/")) {
                uri = uri.buildUpon().scheme("file").build()
            }
            if (uri.isLocal()) {
                val newUrl = File(Const.TEMP_TORRENT_DIR, uri.fileName() ?: url.validateFileName())
                if (uri.scheme == "content") {
                    if (uri.copyTo(newUrl) > 0) {
                        BtDownloadManager.download(context, newUrl.path, requestId = null)
                    }
                } else {
                    File(url).copyTo(newUrl)
                    BtDownloadManager.download(context, newUrl.path, requestId = null)
                }
            } else {
                BtDownloadManager.download(context, url, requestId = null)
            }
        } else {
            DownloadManager.getInstance(context).download(
                url = url,
                path = File(context.dataStore.getOrDefault(MediaLibLocationPreference)).path,
            )
        }
    }
}