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

fun String.toHtml(@SuppressLint("InlinedApi") flag: Int = Html.FROM_HTML_MODE_LEGACY): Spanned {
    return Html.fromHtml(this, flag)
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