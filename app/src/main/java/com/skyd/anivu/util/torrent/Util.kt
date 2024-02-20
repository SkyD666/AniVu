package com.skyd.anivu.util.torrent

import android.util.Log
import com.skyd.anivu.config.Const
import org.libtorrent4j.Vectors
import org.libtorrent4j.alerts.SaveResumeDataAlert
import org.libtorrent4j.swig.add_torrent_params
import org.libtorrent4j.swig.error_code
import org.libtorrent4j.swig.libtorrent
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


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