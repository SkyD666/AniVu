package com.skyd.anivu.ext

import android.net.Uri
import android.provider.OpenableColumns
import com.skyd.anivu.appContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


fun Uri.copyTo(target: File): File {
    return appContext.contentResolver.openInputStream(this)!!.use { it.saveTo(target) }
}

fun Uri.fileName(): String? {
    var name: String? = null
    runCatching {
        appContext.contentResolver.query(
            this, null, null, null, null
        )?.use { cursor ->
            /*
             * Get the column indexes of the data in the Cursor,
             * move to the first row in the Cursor, get the data.
             */
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            name = cursor.getString(nameIndex)
        }
    }.onFailure { it.printStackTrace() }

    return name ?: path?.substringAfterLast("/")?.toDecodedUrl()
}

fun InputStream.saveTo(target: File): File {
    val parentFile = target.parentFile
    if (parentFile?.exists() == false) {
        parentFile.mkdirs()
    }
    if (!target.exists()) {
        target.createNewFile()
    }
    FileOutputStream(target).use { copyTo(it) }
    return target
}

fun File.md5(): String? {
    var bi: BigInteger? = null
    try {
        val buffer = ByteArray(4096)
        var len: Int
        val md = MessageDigest.getInstance("MD5")
        FileInputStream(this).use { fis ->
            while (fis.read(buffer).also { len = it } != -1) {
                md.update(buffer, 0, len)
            }
        }
        val b = md.digest()
        bi = BigInteger(1, b)
    } catch (e: NoSuchAlgorithmException) {
        e.printStackTrace()
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return bi?.toString(16)
}

inline val String.extName: String
    get() = substringAfterLast(".", missingDelimiterValue = "")