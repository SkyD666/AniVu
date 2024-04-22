package com.skyd.anivu.ui.player

import android.util.Log
import org.libtorrent4j.AlertListener
import org.libtorrent4j.Priority
import org.libtorrent4j.TorrentFlags
import org.libtorrent4j.TorrentHandle
import org.libtorrent4j.alerts.Alert
import org.libtorrent4j.alerts.AlertType
import org.libtorrent4j.alerts.PieceFinishedAlert
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.lang.ref.WeakReference

class Torrent(
    val torrentHandle: TorrentHandle,
) : AlertListener {
    enum class State {
        RETRIEVING_META,
        STARTING,
        STREAMING
    }

    private var lastPieceIndex: Int = -1
    private var firstPieceIndex: Int = -1
    private var selectedFileIndex = -1

    /**
     * Get the index of the piece we're currently interested in
     *
     * @return Interested piece index
     */
    private var interestedPieceIndex = 0
    private var hasPieces: BooleanArray? = null
    private val torrentStreamReferences: MutableList<WeakReference<TorrentInputStream>> =
        mutableListOf()

    /**
     * Get current torrent state
     *
     * @return [State]
     */
    var state = State.RETRIEVING_META
        private set

    /**
     * First the largest file in the download is selected as the file for playback
     *
     * After setting this priority, the first and last index of the pieces that make up this file are determined.
     * And last: amount of pieces that are needed for playback are calculated (needed for playback means: make up 10 megabyte of the file)
     */
    init {
        if (selectedFileIndex == -1) {
            setLargestFile()
        }
        startDownload()
    }

    /**
     * Reset piece priorities of selected file to normal
     */
    private fun resetPriorities() {
        val priorities = torrentHandle.piecePriorities()
        for (i in priorities.indices) {
            if (i in firstPieceIndex..lastPieceIndex) {
                torrentHandle.piecePriority(i, Priority.IGNORE)
            } else {
                torrentHandle.piecePriority(i, Priority.IGNORE)
            }
        }
    }

    val videoFile: File
        get() = File(
            torrentHandle.savePath(),
            torrentHandle.torrentFile().files().filePath(selectedFileIndex)
        )

    val videoStream: InputStream
        /**
         * Get an InputStream for the video file.
         * Read is be blocked until the requested piece(s) is downloaded.
         *
         * @return [InputStream]
         */
        get() {
            val inputStream = TorrentInputStream(this, FileInputStream(videoFile))
            torrentStreamReferences.add(WeakReference(inputStream))
            return inputStream
        }
    val saveLocation: File
        /**
         * Get the location of the file that is being downloaded
         *
         * @return [File] The file location
         */
        get() = File(torrentHandle.savePath(), torrentHandle.getName())

    fun resume() {
        torrentHandle.resume()
    }

    fun pause() {
        torrentHandle.pause()
    }

    /**
     * Set the selected file index to the largest file in the torrent
     */
    fun setLargestFile() {
        setSelectedFileIndex(-1)
    }

    /**
     * Set the index of the file that should be downloaded
     * If the given index is -1, then the largest file is chosen
     *
     * @param selectedFileIndex [Integer] Index of the file
     */
    fun setSelectedFileIndex(selectedFileIndex: Int) {
        var newSelectedFileIndex = selectedFileIndex
        val torrentInfo = torrentHandle.torrentFile()
        val fileStorage = torrentInfo.files()
        if (newSelectedFileIndex == -1) {
            var highestFileSize: Long = 0
            var selectedFile = -1
            for (i in 0 until fileStorage.numFiles()) {
                val fileSize = fileStorage.fileSize(i)
                if (highestFileSize < fileSize) {
                    highestFileSize = fileSize
                    torrentHandle.filePriority(selectedFile, Priority.IGNORE)
                    selectedFile = i
                    torrentHandle.filePriority(i, Priority.DEFAULT)
                } else {
                    torrentHandle.filePriority(i, Priority.IGNORE)
                }
            }
            newSelectedFileIndex = selectedFile
        } else {
            for (i in 0 until fileStorage.numFiles()) {
                if (i == newSelectedFileIndex) {
                    torrentHandle.filePriority(i, Priority.DEFAULT)
                } else {
                    torrentHandle.filePriority(i, Priority.IGNORE)
                }
            }
        }
        this.selectedFileIndex = newSelectedFileIndex
        val piecePriorities = torrentHandle.piecePriorities()
        var firstPieceIndexLocal = -1
        var lastPieceIndexLocal = -1
        for (i in piecePriorities.indices) {
            if (piecePriorities[i] != Priority.IGNORE) {
                if (firstPieceIndexLocal == -1) {
                    firstPieceIndexLocal = i
                }
                piecePriorities[i] = Priority.IGNORE
            } else {
                if (firstPieceIndexLocal != -1 && lastPieceIndexLocal == -1) {
                    lastPieceIndexLocal = i - 1
                }
            }
        }
        if (firstPieceIndexLocal == -1) {
            firstPieceIndexLocal = 0
        }
        if (lastPieceIndexLocal == -1) {
            lastPieceIndexLocal = piecePriorities.size - 1
        }
        firstPieceIndex = firstPieceIndexLocal
        interestedPieceIndex = firstPieceIndex
        lastPieceIndex = lastPieceIndexLocal
    }

    val fileNames: Array<String?>
        /**
         * Get the filenames of the files in the torrent
         *
         * @return [String[]]
         */
        get() {
            val fileStorage = torrentHandle.torrentFile().files()
            val fileNames = arrayOfNulls<String>(fileStorage.numFiles())
            for (i in 0 until fileStorage.numFiles()) {
                fileNames[i] = fileStorage.fileName(i)
            }
            return fileNames
        }

    /**
     * Prepare torrent for playback.
     */
    fun startDownload() {
        if (state == State.STREAMING || state == State.STARTING) return
        state = State.STARTING
        val priorities = torrentHandle.piecePriorities()
        for (i in priorities.indices) {
            if (priorities[i] != Priority.IGNORE) {
                torrentHandle.piecePriority(i, Priority.DEFAULT)
            }
        }
        hasPieces = BooleanArray(lastPieceIndex - firstPieceIndex + 1) { false }
        torrentStreamReferences.clear()
        torrentHandle.resume()
    }

    /**
     * Check if the piece that contains the specified bytes were downloaded already
     *
     * @param bytes The bytes you're interested in
     * @return `true` if downloaded, `false` if not
     */
    fun hasBytes(bytes: Long): Boolean {
        if (hasPieces == null) {
            return false
        }
        val pieceIndex = (bytes / torrentHandle.torrentFile().pieceLength()).toInt()
        return hasPieces!![pieceIndex]
    }

    /**
     * Set the bytes of the selected file that you're interested in
     * The piece of that specific offset is selected and that piece plus the 1 preceding and the 3 after it.
     * These pieces will then be prioritised, which results in continuing the sequential download after that piece
     *
     * @param bytes The bytes you're interested in
     */
    fun setInterestedBytes(bytes: Long) {
        val hasPieces = this.hasPieces ?: return
        val pieceIndex = (bytes / torrentHandle.torrentFile().pieceLength()).toInt()
        interestedPieceIndex = pieceIndex
        if (!hasPieces[pieceIndex] &&
            torrentHandle.piecePriority(pieceIndex + firstPieceIndex) != Priority.TOP_PRIORITY
        ) {
            val pieces = 5
            for (i in hasPieces.indices) {
                if (!hasPieces[i]) {
                    if (i < pieceIndex) {
                        torrentHandle.piecePriority(i + firstPieceIndex, Priority.IGNORE)
                    } else if (i > pieceIndex + pieces) {
                        torrentHandle.piecePriority(i + firstPieceIndex, Priority.IGNORE)
                    } else {
                        // Set full priority to first found piece that is not confirmed finished
                        torrentHandle.piecePriority(i + firstPieceIndex, Priority.TOP_PRIORITY)
                        torrentHandle.setPieceDeadline(i + firstPieceIndex, 1000)
                    }
                }
            }
        }
    }
    /**
     * Checks if the interesting pieces are downloaded already
     *
     * @return `true` if the 5 pieces that were selected using `setInterestedBytes` are all reported complete including the `nextPieces`, `false` if not
     */
    /**
     * Checks if the interesting pieces are downloaded already
     *
     * @return `true` if the 5 pieces that were selected using `setInterestedBytes` are all reported complete, `false` if not
     */
    @JvmOverloads
    fun hasInterestedBytes(nextPieces: Int = 5): Boolean {
        for (i in 0 until 5 + nextPieces) {
            val index = interestedPieceIndex + i
            if (hasPieces!!.size <= index || index < 0) {
                continue
            }
            if (!hasPieces!![interestedPieceIndex + i]) {
                return false
            }
        }
        return true
    }

    /**
     * Start sequential mode downloading
     */
    private fun startSequentialMode() {
        resetPriorities()
        if (hasPieces == null) {
            torrentHandle.flags = torrentHandle.flags.and_(TorrentFlags.SEQUENTIAL_DOWNLOAD)
        }
    }

    private fun pieceFinished(alert: PieceFinishedAlert) {
        if (state != State.STREAMING) {
            startSequentialMode()
            state = State.STREAMING
        }
        if (state == State.STREAMING && hasPieces != null) {
            val pieceIndex = alert.pieceIndex() - firstPieceIndex
            Log.e("TAG", "pieceFinished: $pieceIndex")
            hasPieces?.set(pieceIndex, true)
            if (pieceIndex >= interestedPieceIndex && hasInterestedBytes()) {
                for (i in pieceIndex until hasPieces!!.size) {
                    // Set full priority to first found piece that is not confirmed finished
                    if (!hasPieces!![i]) {
                        torrentHandle.piecePriority(i + firstPieceIndex, Priority.TOP_PRIORITY)
                        torrentHandle.setPieceDeadline(i + firstPieceIndex, 1000)
                        break
                    }
                }
            }
        }
    }

    override fun types() = intArrayOf(
        AlertType.PIECE_FINISHED.swig(),
    )

    override fun alert(alert: Alert<*>) {
        when (alert) {
            is PieceFinishedAlert -> pieceFinished(alert)
        }
        val i = torrentStreamReferences.iterator()
        while (i.hasNext()) {
            val reference = i.next()
            val inputStream = reference.get()
            if (inputStream == null) {
                i.remove()
                continue
            }
            inputStream.alert(alert)
        }
    }

    companion object {
        private const val SEQUENTIAL_CONCURRENT_PIECES_COUNT = 5
    }
}