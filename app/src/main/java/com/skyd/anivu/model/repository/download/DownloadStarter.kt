package com.skyd.anivu.model.repository.download

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.skyd.anivu.R
import com.skyd.anivu.ext.dataStore
import com.skyd.anivu.ext.getOrDefault
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
                    Regex("^(http|https)://.*\\.torrent$").matches(url)
        if (isMagnetOrTorrent) {
            BtDownloadManager.download(context, url, requestId = null)
        } else {
            DownloadManager.getInstance(context).download(
                url = url,
                path = File(context.dataStore.getOrDefault(MediaLibLocationPreference)).path,
            )
        }
    }
}