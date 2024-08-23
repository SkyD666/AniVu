package com.skyd.anivu.ui.screen.read

import com.skyd.anivu.base.mvi.MviIntent

sealed interface ReadIntent : MviIntent {
    data class Init(val articleId: String) : ReadIntent
    data class Favorite(val articleId: String, val favorite: Boolean) : ReadIntent
    data class Read(val articleId: String, val read: Boolean) : ReadIntent
    data class DownloadImage(val url: String, val title: String?) : ReadIntent
}