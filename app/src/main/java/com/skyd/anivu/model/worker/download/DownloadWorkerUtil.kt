package com.skyd.anivu.model.worker.download

import android.content.Context
import com.skyd.anivu.R
import org.libtorrent4j.TorrentStatus
import org.libtorrent4j.TorrentStatus.State.CHECKING_FILES
import org.libtorrent4j.TorrentStatus.State.CHECKING_RESUME_DATA
import org.libtorrent4j.TorrentStatus.State.DOWNLOADING
import org.libtorrent4j.TorrentStatus.State.DOWNLOADING_METADATA
import org.libtorrent4j.TorrentStatus.State.FINISHED
import org.libtorrent4j.TorrentStatus.State.SEEDING
import org.libtorrent4j.TorrentStatus.State.UNKNOWN

fun TorrentStatus.State.toDisplayString(context: Context): String {
    return when (this) {
        CHECKING_FILES -> context.getString(R.string.torrent_status_checking_files)
        DOWNLOADING_METADATA -> context.getString(R.string.torrent_status_downloading_metadata)
        DOWNLOADING -> context.getString(R.string.torrent_status_downloading)
        FINISHED -> context.getString(R.string.torrent_status_finished)
        SEEDING -> context.getString(R.string.torrent_status_seeding)
        CHECKING_RESUME_DATA -> context.getString(R.string.torrent_status_checking_resume_data)
        UNKNOWN -> ""
    }
}