package com.skyd.anivu.ui.player

import org.libtorrent4j.AlertListener
import org.libtorrent4j.Priority
import org.libtorrent4j.TorrentFlags
import org.libtorrent4j.TorrentHandle
import org.libtorrent4j.alerts.Alert
import org.libtorrent4j.alerts.AlertType
import org.libtorrent4j.alerts.BlockFinishedAlert
import org.libtorrent4j.alerts.PieceFinishedAlert
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.lang.ref.WeakReference

class Torrent(
    val torrentHandle: TorrentHandle,
    private val prepareSize: Long = 15 * 1024L * 1024L,
) : AlertListener {
    enum class State {
        RETRIEVING_META,
        STARTING,
        STREAMING
    }

    /**
     * Get amount of pieces to prepare
     *
     * @return Amount of pieces to prepare
     */
    private var piecesToPrepare: Int = 0
    private var lastPieceIndex: Int = -1
    private var firstPieceIndex: Int = -1
    private var selectedFileIndex = -1

    /**
     * Get the index of the piece we're currently interested in
     *
     * @return Interested piece index
     */
    private var interestedPieceIndex = 0
    private var prepareProgress = 0.0
    private var progressStep = 0.0
    private var preparePieces: MutableList<Int> = mutableListOf()
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
                torrentHandle.piecePriority(i, Priority.DEFAULT)
            } else {
                torrentHandle.piecePriority(i, Priority.IGNORE)
            }
        }
    }

    val videoFile: File
        get() = File(
            torrentHandle.savePath() + "/" + torrentHandle.torrentFile().files()
                .filePath(selectedFileIndex)
        )

    val videoStream: InputStream
        /**
         * Get an InputStream for the video file.
         * Read is be blocked until the requested piece(s) is downloaded.
         *
         * @return [InputStream]
         */
        get() {
            val file = videoFile
            val inputStream = TorrentInputStream(this, FileInputStream(file))
            torrentStreamReferences.add(WeakReference(inputStream))
            return inputStream
        }
    val saveLocation: File
        /**
         * Get the location of the file that is being downloaded
         *
         * @return [File] The file location
         */
        get() = File(torrentHandle.savePath() + "/" + torrentHandle.getName())

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
        val pieceCount = lastPieceIndexLocal - firstPieceIndexLocal + 1
        val pieceLength = torrentHandle.torrentFile().pieceLength()
        var activePieceCount: Int
        if (pieceLength > 0) {
            activePieceCount = (prepareSize / pieceLength).toInt()
            if (activePieceCount < MIN_PREPARE_COUNT) {
                activePieceCount = MIN_PREPARE_COUNT
            } else if (activePieceCount > MAX_PREPARE_COUNT) {
                activePieceCount = MAX_PREPARE_COUNT
            }
        } else {
            activePieceCount = DEFAULT_PREPARE_COUNT
        }
        if (pieceCount < activePieceCount) {
            activePieceCount = pieceCount / 2
        }
        firstPieceIndex = firstPieceIndexLocal
        interestedPieceIndex = firstPieceIndex
        lastPieceIndex = lastPieceIndexLocal
        piecesToPrepare = activePieceCount
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
     * Prepare torrent for playback. Prioritize the first `piecesToPrepare` pieces and the last `piecesToPrepare` pieces
     * from `firstPieceIndex` and `lastPieceIndex`. Ignore all other pieces.
     */
    fun startDownload() {
        if (state == State.STREAMING || state == State.STARTING) return
        state = State.STARTING
        val indices: MutableList<Int> = ArrayList()
        val priorities = torrentHandle.piecePriorities()
        for (i in priorities.indices) {
            if (priorities[i] != Priority.IGNORE) {
                torrentHandle.piecePriority(i, Priority.DEFAULT)
            }
        }
        for (i in 0 until piecesToPrepare) {
            indices.add(lastPieceIndex - i)
            torrentHandle.piecePriority(lastPieceIndex - i, Priority.TOP_PRIORITY)
            torrentHandle.setPieceDeadline(lastPieceIndex - i, 1000)
        }
        for (i in 0 until piecesToPrepare) {
            indices.add(firstPieceIndex + i)
            torrentHandle.piecePriority(firstPieceIndex + i, Priority.TOP_PRIORITY)
            torrentHandle.setPieceDeadline(firstPieceIndex + i, 1000)
        }
        preparePieces = indices
        hasPieces = BooleanArray(lastPieceIndex - firstPieceIndex + 1) { false }
        val torrentInfo = torrentHandle.torrentFile()
        val status = torrentHandle.status()
        val blockCount = (indices.size * torrentInfo.pieceLength() / status.blockSize()).toDouble()
        progressStep = 100 / blockCount
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
        if (hasPieces == null && bytes >= 0) {
            return
        }
        val pieceIndex = (bytes / torrentHandle.torrentFile().pieceLength()).toInt()
        interestedPieceIndex = pieceIndex
        if (!hasPieces!![pieceIndex] && torrentHandle.piecePriority(pieceIndex + firstPieceIndex) != Priority.TOP_PRIORITY) {
            interestedPieceIndex = pieceIndex
            var pieces = 5
            for (i in pieceIndex until hasPieces!!.size) {
                // Set full priority to first found piece that is not confirmed finished
                if (!hasPieces!![i]) {
                    torrentHandle.piecePriority(i + firstPieceIndex, Priority.TOP_PRIORITY)
                    torrentHandle.setPieceDeadline(i + firstPieceIndex, 1000)
                    pieces--
                    if (pieces == 0) {
                        break
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
        } else {
            for (i in firstPieceIndex + piecesToPrepare until firstPieceIndex + piecesToPrepare + SEQUENTIAL_CONCURRENT_PIECES_COUNT) {
                torrentHandle.piecePriority(i, Priority.TOP_PRIORITY)
                torrentHandle.setPieceDeadline(i, 1000)
            }
        }
    }

    private fun pieceFinished(alert: PieceFinishedAlert) {
        if (state == State.STREAMING && hasPieces != null) {
            val pieceIndex = alert.pieceIndex() - firstPieceIndex
            hasPieces!![pieceIndex] = true
            if (pieceIndex >= interestedPieceIndex) {
                for (i in pieceIndex until hasPieces!!.size) {
                    // Set full priority to first found piece that is not confirmed finished
                    if (!hasPieces!![i]) {
                        torrentHandle.piecePriority(i + firstPieceIndex, Priority.TOP_PRIORITY)
                        torrentHandle.setPieceDeadline(i + firstPieceIndex, 1000)
                        break
                    }
                }
            }
        } else {
            preparePieces.removeIf { index: Int -> index == alert.pieceIndex() }
            hasPieces?.set(alert.pieceIndex() - firstPieceIndex, true)
            if (preparePieces.isEmpty()) {
                startSequentialMode()
                prepareProgress = 100.0
                state = State.STREAMING
            }
        }
    }

    private fun blockFinished(alert: BlockFinishedAlert) {
        for (index in preparePieces) {
            if (index == alert.pieceIndex()) {
                prepareProgress += progressStep
                break
            }
        }
    }

    override fun types() = intArrayOf(
        AlertType.PIECE_FINISHED.swig(),
        AlertType.BLOCK_FINISHED.swig(),
    )

    override fun alert(alert: Alert<*>) {
        when (alert) {
            is PieceFinishedAlert -> pieceFinished(alert)
            is BlockFinishedAlert -> blockFinished(alert)
            else -> {}
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
        private const val MAX_PREPARE_COUNT = 20
        private const val MIN_PREPARE_COUNT = 2
        private const val DEFAULT_PREPARE_COUNT = 5
        private const val SEQUENTIAL_CONCURRENT_PIECES_COUNT = 5
    }
}