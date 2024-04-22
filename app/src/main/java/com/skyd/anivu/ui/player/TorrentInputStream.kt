package com.skyd.anivu.ui.player

import android.util.Log
import org.libtorrent4j.AlertListener
import org.libtorrent4j.alerts.Alert
import org.libtorrent4j.alerts.AlertType
import org.libtorrent4j.alerts.PieceFinishedAlert
import java.io.FilterInputStream
import java.io.IOException
import java.io.InputStream

internal class TorrentInputStream(val torrent: Torrent, inputStream: InputStream) :
    FilterInputStream(inputStream), AlertListener {
    private var stopped = false
    private var location: Long = 0

    @Synchronized
    private fun waitForPiece(offset: Long): Boolean {
        while (!Thread.currentThread().isInterrupted && !stopped) {
            try {
                if (torrent.hasBytes(offset)) {
                    return true
                }
                torrent.setInterestedBytes(offset)
                (this as Object).wait()
            } catch (ex: InterruptedException) {
                Thread.currentThread().interrupt()
            }
        }
        return false
    }

    @Synchronized
    @Throws(IOException::class)
    override fun read(): Int {
        if (!waitForPiece(location)) {
            return -1
        }
        location++
        return super.read()
    }

    @Synchronized
    @Throws(IOException::class)
    override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        val pieceLength: Int = torrent.torrentHandle.torrentFile().pieceLength()
        var i = 0
        Log.e("TAG", "wantread: $location")
        while (i < length) {
            if (!waitForPiece(location + i)) {
                return -1
            }
            i += pieceLength
        }
        location += length.toLong()
        Log.e("TAG", "read: $location")
        return super.read(buffer, offset, length)
    }

    @Throws(IOException::class)
    override fun close() {
        synchronized(this) {
            stopped = true
            (this as Object).notifyAll()
        }
        super.close()
    }

    @Synchronized
    @Throws(IOException::class)
    override fun skip(n: Long): Long {
        location += n
        return super.skip(n)
    }

    override fun markSupported() = false

    @Synchronized
    private fun pieceFinished() {
        (this as Object).notifyAll()
    }

    override fun types() = intArrayOf(
        AlertType.PIECE_FINISHED.swig()
    )

    override fun alert(alert: Alert<*>) {
        when (alert) {
            is PieceFinishedAlert -> pieceFinished()
            else -> {}
        }
    }
}