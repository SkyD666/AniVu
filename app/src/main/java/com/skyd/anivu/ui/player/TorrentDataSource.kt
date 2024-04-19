package com.skyd.anivu.ui.player

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import androidx.media3.datasource.BaseDataSource
import androidx.media3.datasource.DataSpec
import com.skyd.anivu.BuildConfig
import com.skyd.anivu.appContext
import com.skyd.anivu.config.Const
import com.skyd.anivu.ext.getAppName
import com.skyd.anivu.ext.getAppVersionName
import com.skyd.anivu.model.worker.download.initProxySettings
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import org.libtorrent4j.AlertListener
import org.libtorrent4j.SessionManager
import org.libtorrent4j.SessionParams
import org.libtorrent4j.TorrentFlags
import org.libtorrent4j.TorrentHandle
import org.libtorrent4j.alerts.AddTorrentAlert
import org.libtorrent4j.alerts.Alert
import org.libtorrent4j.alerts.AlertType
import org.libtorrent4j.alerts.FileErrorAlert
import org.libtorrent4j.alerts.LogAlert
import org.libtorrent4j.alerts.MetadataReceivedAlert
import org.libtorrent4j.alerts.TorrentCheckedAlert
import org.libtorrent4j.alerts.TorrentErrorAlert
import org.libtorrent4j.swig.settings_pack
import java.io.File
import java.io.FileInputStream
import java.util.concurrent.CountDownLatch
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@SuppressLint("UnsafeOptInUsageError")
class TorrentDataSource(
    private val fileIndex: Int = 0,
) : BaseDataSource(true) {
    private lateinit var dataSpec: DataSpec
    private lateinit var sessionManager: SessionManager
    private var torrentHandle: TorrentHandle? = null
    private var file: File? = null
    private var inputStream: TorrentInputStream? = null
    private var fileSize = 0L

    override fun open(dataSpec: DataSpec): Long {
        this.dataSpec = dataSpec
        sessionManager = SessionManager(BuildConfig.DEBUG)
        val sessionParams = SessionParams()

        sessionParams.settings = initProxySettings(
            context = appContext,
            settings = sessionParams.settings,
        ).setString(
            settings_pack.string_types.user_agent.swigValue(),
            "${appContext.getAppName() ?: "AniVu"}/${appContext.getAppVersionName()}"
        )

        sessionManager.start(sessionParams)
        sessionManager.startDht()

        val countDownLatch = CountDownLatch(3)
        runBlocking {
            suspendCancellableCoroutine {
                sessionManager.addListener(object : AlertListener {
                    override fun types() = intArrayOf(
                        AlertType.LOG.swig(),
                        AlertType.ADD_TORRENT.swig(),
                        AlertType.TORRENT_CHECKED.swig(),
                        AlertType.ADD_TORRENT.swig(),
                        AlertType.METADATA_RECEIVED.swig(),
                        AlertType.TORRENT_ERROR.swig(),
                        AlertType.FILE_ERROR.swig(),
                    )

                    private fun countDownAndTryResume() {
                        countDownLatch.countDown()
                        if (countDownLatch.count == 0L) {
                            it.resume(Unit)
                        }
                    }

                    override fun alert(alert: Alert<*>?) {
                        when (alert) {
                            is LogAlert -> {
                                Log.e("TAG", "alert: ${alert.logMessage()}")
                            }

                            is AddTorrentAlert -> {
                                if (torrentHandle == null) {
                                    torrentHandle = sessionManager.find(alert.handle().infoHash())
                                    countDownAndTryResume()
                                }
                            }

                            is TorrentCheckedAlert -> {
                                if (file == null) {
                                    file = File(
                                        alert.handle().torrentFile().files()
                                            .filePath(0, Const.DOWNLOADING_VIDEO_DIR.path)
                                    ).apply {
                                        if (!exists()) createNewFile()
                                    }
                                    countDownAndTryResume()
                                }
                            }

                            is MetadataReceivedAlert -> {
                                if (fileSize == 0L) {
                                    val files = alert.handle().torrentFile().files()
                                    fileSize = files.fileSize(fileIndex)
                                    countDownAndTryResume()
                                }
                            }

                            is TorrentErrorAlert -> {
                                it.resumeWithException(RuntimeException(alert.message()))
                            }
                            // If the storage fails to read or write files that it needs access to,
                            // this alert is generated and the torrent is paused.
                            is FileErrorAlert -> {
                                it.resumeWithException(RuntimeException(alert.message()))
                            }
                        }
                    }
                })

                sessionManager.download(
                    dataSpec.uri.toString(),
                    Const.DOWNLOADING_VIDEO_DIR,
                    TorrentFlags.SEQUENTIAL_DOWNLOAD,
                )
            }
        }

        inputStream = TorrentInputStream(
            Torrent(torrentHandle!!).apply { sessionManager.addListener(this) },
            FileInputStream(file!!)
        ).apply {
            sessionManager.addListener(this)
        }
        return fileSize
    }

    override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        val inputStream = inputStream
        if (length == 0 || inputStream == null) {
            return 0
        }
        return inputStream.read(buffer, offset, length)
    }

    override fun close() {
        inputStream?.let { sessionManager.removeListener(it) }
        sessionManager.pause()
        sessionManager.stopDht()
        sessionManager.stop()
        inputStream?.close()
        if (torrentHandle?.isValid == true) {
            torrentHandle?.pause()
        }
    }

    override fun getUri(): Uri = dataSpec.uri
}