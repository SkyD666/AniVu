package com.skyd.anivu.ext

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.text.Html
import android.text.Spanned
import androidx.core.text.HtmlCompat
import net.dankito.readability4j.extended.Readability4JExtended

fun String.toEncodedUrl(): String {
    return Uri.encode(this, ":/-![].,%?&=")
}

fun String.toDecodedUrl(): String {
    return Uri.decode(this)
}

fun String.toHtml(@SuppressLint("InlinedApi") flag: Int = Html.FROM_HTML_MODE_LEGACY): Spanned {
    return Html.fromHtml(this, flag)
}

fun String.toHtml(
    @SuppressLint("InlinedApi") flag: Int = Html.FROM_HTML_MODE_LEGACY,
    imageGetter: Html.ImageGetter,
    tagHandler: Html.TagHandler?,
): Spanned {
    return Html.fromHtml(this, flag, imageGetter, tagHandler)
}

fun String.toRemoveHtml(): String {
    return Html.fromHtml(this, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
}

fun String.readable(): String =
    Readability4JExtended("", this).parse().textContent?.trim().orEmpty()

fun String.copy(context: Context) {
    try {
        val systemService = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        systemService.setPrimaryClip(ClipData.newPlainText("text", this))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun String.validateFileName(maxFilenameLength: Int = 255): String {
    if (isEmpty()) {
        return ""
    }

    // 获取文件名和后缀名
    var name: String = this
    var extension = ""
    val dotIndex = lastIndexOf(".")
    if (dotIndex != -1) {
        name = substring(0, dotIndex)
        extension = substring(dotIndex)
    }

    // 检查文件名长度
    if (length > maxFilenameLength) {
        // 如果文件名过长，则截断后半部分
        name = name.substring(0, maxFilenameLength - extension.length)
    }

    // 去除文件名中的非法字符
    name = name.replace("[\\\\/:*?\"<>|]".toRegex(), "")

    // 返回合法的文件名和后缀名
    return name + extension
}

fun <C : CharSequence> C?.ifNullOfBlank(defaultValue: () -> C): C =
    if (!isNullOrBlank()) this else defaultValue()