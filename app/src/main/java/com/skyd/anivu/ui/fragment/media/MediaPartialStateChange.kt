package com.skyd.anivu.ui.fragment.media

import com.skyd.anivu.model.bean.VideoBean
import java.io.File


internal sealed interface MediaPartialStateChange {
    fun reduce(oldState: MediaState): MediaState

    sealed interface LoadingDialog : MediaPartialStateChange {
        data object Show : LoadingDialog {
            override fun reduce(oldState: MediaState) = oldState.copy(loadingDialog = true)
        }
    }

    sealed interface MediaListResult : MediaPartialStateChange {
        override fun reduce(oldState: MediaState): MediaState {
            return when (this) {
                is Success -> oldState.copy(
                    mediaListState = MediaListState.Success(list = list),
                    loadingDialog = false,
                )

                is Failed -> oldState.copy(
                    mediaListState = MediaListState.Failed(msg = msg),
                    loadingDialog = false,
                )

                Loading -> oldState.copy(
                    mediaListState = oldState.mediaListState.let {
                        when (it) {
                            is MediaListState.Failed -> it.copy(loading = false)
                            is MediaListState.Init -> it.copy(loading = false)
                            is MediaListState.Success -> it.copy(loading = false)
                        }
                    },
                    loadingDialog = false,
                )
            }
        }

        data class Success(val list: List<VideoBean>) : MediaListResult
        data class Failed(val msg: String) : MediaListResult
        data object Loading : MediaListResult
    }

    sealed interface DeleteUriResult : MediaPartialStateChange {
        override fun reduce(oldState: MediaState): MediaState {
            return when (this) {
                is Success -> {
                    val mediaListState = oldState.mediaListState
                    oldState.copy(
                        mediaListState = if (mediaListState is MediaListState.Success) {
                            val list = mediaListState.list.toMutableList()
                            list.removeIf { it.file == file }
                            MediaListState.Success(list)
                        } else {
                            mediaListState
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
