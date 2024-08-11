package com.skyd.anivu.ui.screen.media.list

import androidx.compose.ui.util.fastFirstOrNull
import com.skyd.anivu.model.bean.MediaGroupBean
import com.skyd.anivu.model.bean.VideoBean
import java.io.File


internal sealed interface MediaListPartialStateChange {
    fun reduce(oldState: MediaListState): MediaListState

    sealed interface LoadingDialog : MediaListPartialStateChange {
        data object Show : LoadingDialog {
            override fun reduce(oldState: MediaListState) = oldState.copy(loadingDialog = true)
        }
    }

    sealed interface MediaListResult : MediaListPartialStateChange {
        override fun reduce(oldState: MediaListState): MediaListState {
            return when (this) {
                is Success -> oldState.copy(
                    listState = ListState.Success(list = list),
                    groups = groups,
                    loadingDialog = false,
                )

                is Failed -> oldState.copy(
                    listState = ListState.Failed(msg = msg),
                    loadingDialog = false,
                )

                Loading -> oldState.copy(
                    listState = oldState.listState.let {
                        when (it) {
                            is ListState.Failed -> it.copy(loading = true)
                            is ListState.Init -> it.copy(loading = true)
                            is ListState.Success -> it.copy(loading = true)
                        }
                    },
                    loadingDialog = false,
                )
            }
        }

        data class Success(val list: List<VideoBean>, val groups: List<MediaGroupBean>) :
            MediaListResult

        data class Failed(val msg: String) : MediaListResult
        data object Loading : MediaListResult
    }

    sealed interface DeleteFileResult : MediaListPartialStateChange {
        override fun reduce(oldState: MediaListState): MediaListState {
            return when (this) {
                is Success -> {
                    val listState = oldState.listState
                    oldState.copy(
                        listState = if (listState is ListState.Success) {
                            ListState.Success(listState.list.toMutableList().apply {
                                fastFirstOrNull { it.file == file }?.let { remove(it) }
                            })
                        } else {
                            listState
                        },
                        loadingDialog = false,
                    )
                }

                is Failed -> oldState.copy(
                    loadingDialog = false,
                )
            }
        }

        data class Success(val file: File) : DeleteFileResult
        data class Failed(val msg: String) : DeleteFileResult
    }
}
