package com.skyd.anivu.ui.fragment.media

import com.skyd.anivu.base.mvi.MviViewState
import com.skyd.anivu.model.bean.VideoBean

data class MediaState(
    val mediaListState: MediaListState,
    val loadingDialog: Boolean,
) : MviViewState {
    companion object {
        fun initial() = MediaState(
            mediaListState = MediaListState.Init,
            loadingDialog = false,
        )
    }
}

sealed class MediaListState(var loading: Boolean = false) {
    class Success(val list: List<VideoBean>) : MediaListState()
    data object Init : MediaListState()
    data class Failed(val msg: String) : MediaListState()
}