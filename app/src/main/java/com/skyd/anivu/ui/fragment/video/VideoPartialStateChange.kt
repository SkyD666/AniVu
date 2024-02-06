package com.skyd.anivu.ui.fragment.video

import android.net.Uri
import com.skyd.anivu.model.bean.VideoBean
import java.io.File


internal sealed interface VideoPartialStateChange {
    fun reduce(oldState: VideoState): VideoState

    sealed interface LoadingDialog : VideoPartialStateChange {
        data object Show : LoadingDialog {
            override fun reduce(oldState: VideoState) = oldState.copy(loadingDialog = true)
        }
    }

    sealed interface VideoListResult : VideoPartialStateChange {
        override fun reduce(oldState: VideoState): VideoState {
            return when (this) {
                is Success -> oldState.copy(
                    videoListState = VideoListState.Success(videoList = videoList),
                    loadingDialog = false,
                )

                is Failed -> oldState.copy(
                    videoListState = VideoListState.Failed(msg = msg),
                    loadingDialog = false,
                )

                Loading -> oldState.copy(
                    videoListState = VideoListState.Loading,
                    loadingDialog = false,
                )
            }
        }

        data class Success(val videoList: List<VideoBean>) : VideoListResult
        data class Failed(val msg: String) : VideoListResult
        data object Loading : VideoListResult
    }

    sealed interface DeleteUriResult : VideoPartialStateChange {
        override fun reduce(oldState: VideoState): VideoState {
            return when (this) {
                is Success -> {
                    val videoListState = oldState.videoListState
                    oldState.copy(
                        videoListState = if (videoListState is VideoListState.Success) {
                            val videoList = videoListState.videoList.toMutableList()
                            videoList.removeIf { it.file == file }
                            VideoListState.Success(videoList)
                        } else {
                            videoListState
                        },
                        loadingDialog = false,
                    )
                }

                is Failed -> oldState.copy(
                    loadingDialog = false,
                )
            }
        }

        data class Success(val file: File) : DeleteUriResult
        data class Failed(val msg: String) : DeleteUriResult
    }
}
