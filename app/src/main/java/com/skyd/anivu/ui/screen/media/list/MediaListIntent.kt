package com.skyd.anivu.ui.screen.media.list

import com.skyd.anivu.base.mvi.MviIntent
import com.skyd.anivu.model.bean.MediaGroupBean
import java.io.File

sealed interface MediaListIntent : MviIntent {
    data class Init(val path: String?, val group: MediaGroupBean?, val version: Long?) :
        MediaListIntent

    data class Refresh(val path: String?, val group: MediaGroupBean?) : MediaListIntent
    data class DeleteFile(val file: File) : MediaListIntent
}