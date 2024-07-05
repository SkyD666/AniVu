package com.skyd.anivu.ui.fragment.media

import com.skyd.anivu.base.mvi.MviIntent
import com.skyd.anivu.model.bean.MediaGroupBean
import com.skyd.anivu.model.bean.VideoBean
import java.io.File

sealed interface MediaIntent : MviIntent {
    data class Init(val path: String?, val isMediaLibRoot: Boolean) : MediaIntent
    data class Refresh(val path: String?, val isMediaLibRoot: Boolean) : MediaIntent
    data class Delete(val file: File) : MediaIntent
    data class ChangeMediaGroup(val path: String?, val videoBean: VideoBean, val group: MediaGroupBean) : MediaIntent
    data class CreateGroup(val path: String?, val group: MediaGroupBean) : MediaIntent
    data class DeleteGroup(val path: String?, val group: MediaGroupBean) : MediaIntent
    data class RenameGroup(val path: String?, val group: MediaGroupBean, val newName: String) :
        MediaIntent

    data class MoveFilesToGroup(
        val path: String?,
        val from: MediaGroupBean,
        val to: MediaGroupBean
    ) : MediaIntent
}