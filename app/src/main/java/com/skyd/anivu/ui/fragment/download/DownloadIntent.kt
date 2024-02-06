package com.skyd.anivu.ui.fragment.download

import com.skyd.anivu.base.mvi.MviIntent

sealed interface DownloadIntent : MviIntent {
    data object Init : DownloadIntent
    data class DeleteDownloadTaskInfo(
        val articleId: String, val link: String, val file: String?
    ) : DownloadIntent
}