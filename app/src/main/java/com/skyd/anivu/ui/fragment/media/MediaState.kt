package com.skyd.anivu.ui.fragment.media

import com.skyd.anivu.base.mvi.MviViewState
import com.skyd.anivu.model.bean.VideoBean

data class MediaState(
    val mediaListState: MediaListState,
    val loadingDialog: Boolean,
) : MviViewState {
    companion object {
        fun initial() = MediaState(
            mediaListState = MediaListState.Init(),
            loadingDialog = false,
        )
    }
}

sealed class MediaListState(open val loading: Boolean) {
    data class Success(val list: List<VideoBean>, override val loading: Boolean = false) :
        MediaListState(loading)

    data class Init(override val loading: Boolean = false) : MediaListState(loading)
    data class Failed(val msg: String, override val loading: Boolean = false) :
        MediaListState(loading)
}