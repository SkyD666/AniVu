package com.skyd.anivu.ext

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.URLUtil
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.skyd.anivu.R
import com.skyd.anivu.appContext
import com.skyd.anivu.ui.component.showToast
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

fun String.openBrowser(context: Context) {
    Uri.parse(this).openBrowser(context)
}

fun Uri.openBrowser(context: Context) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, this)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        e.printStackTrace()
        context.getString(R.string.no_browser_found, path).showToast(Toast.LENGTH_LONG)
    }
}

fun Uri.openWith(context: Context) = openChooser(
    context = context,
    action = Intent.ACTION_VIEW,
    chooserTitle = context.getString(R.string.open_with),
)

fun Uri.share(context: Context) = openChooser(
    context = context,
    action = Intent.ACTION_SEND,
    chooserTitle = context.getString(R.string.share),
)

private fun Uri.openChooser(context: Context, action: String, chooserTitle: CharSequence) {
    try {
        val mimeType = context.contentResolver.getType(this)
        val intent = Intent.createChooser(
            Intent().apply {
                this.action = action
                putExtra(Intent.EXTRA_STREAM, this@openChooser)
                setDataAndType(this@openChooser, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            },
            chooserTitle
        )
        ContextCompat.startActivity(context, intent, null)
    } catch (e: Exception) {
        e.printStackTrace()
        context.getString(R.string.failed_msg, e.message).showToast(Toast.LENGTH_LONG)
    }
}

fun Uri.isLocal(): Boolean = URLUtil.isFileUrl(toString()) || URLUtil.isContentUrl(toString())

fun Uri.isNetwork(): Boolean = URLUtil.isNetworkUrl(toString())

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
    runCatching {
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
    }.onFailure {
        when (it) {
            is NoSuchAlgorithmException -> it.printStackTrace()
            is IOException -> it.printStackTrace()
            else -> throw it
        }
    }
    return bi?.toString(16)
}

inline val String.extName: String
    get() = substringAfterLast(".", missingDelimiterValue = "")