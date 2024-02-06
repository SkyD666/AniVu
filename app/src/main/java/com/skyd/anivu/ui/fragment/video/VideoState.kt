package com.skyd.anivu.ui.fragment.video

import com.skyd.anivu.base.mvi.MviViewState
import com.skyd.anivu.model.bean.VideoBean

data class VideoState(
    val videoListState: VideoListState,
    val loadingDialog: Boolean,
) : MviViewState {
    companion object {
        fun initial() = VideoState(
            videoListState = VideoListState.Init,
            loadingDialog = false,
        )
    }
}

sealed interface VideoListState {
    data class Success(val videoList: List<VideoBean>) : VideoListState
    data object Init : VideoListState
    data object Loading : VideoListState
    data class Failed(val msg: String) : VideoListState
}