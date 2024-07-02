package com.skyd.anivu.ui.fragment.download

import com.skyd.anivu.base.mvi.MviIntent

sealed interface DownloadIntent : MviIntent {
    data object Init : DownloadIntent
    data class AddDownload(val link: String) : DownloadIntent
}