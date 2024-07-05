package com.skyd.anivu.ui.fragment.media

import com.skyd.anivu.model.bean.MediaGroupBean
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
                            is MediaListState.Failed -> it.copy(loading = true)
                            is MediaListState.Init -> it.copy(loading = true)
                            is MediaListState.Success -> it.copy(loading = true)
                        }
                    },
                    loadingDialog = false,
                )
            }
        }

        data class Success(val list: List<Any>) : MediaListResult
        data class Failed(val msg: String) : MediaListResult
        data object Loading : MediaListResult
    }

    sealed interface DeleteFileResult : MediaPartialStateChange {
        override fun reduce(oldState: MediaState): MediaState {
            return when (this) {
                is Success -> {
                    val mediaListState = oldState.mediaListState
                    oldState.copy(
                        mediaListState = if (mediaListState is MediaListState.Success) {
                            val list = mediaListState.list.toMutableList()
                            list.removeIf { it is VideoBean && it.file == file }
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

        data class Success(val file: File) : DeleteFileResult
        data class Failed(val msg: String) : DeleteFileResult
    }

    sealed interface DeleteGroup : MediaPartialStateChange {
        override fun reduce(oldState: MediaState): MediaState {
            return when (this) {
                is Success -> oldState.copy(
                    loadingDialog = false,
                )

                is Failed -> oldState.copy(
                    loadingDialog = false,
                )
            }
        }

        data object Success : DeleteGroup
        data class Failed(val msg: String) : DeleteGroup
    }

    sealed interface ChangeMediaGroup : MediaPartialStateChange {
        override fun reduce(oldState: MediaState): MediaState {
            return when (this) {
                is Success -> oldState.copy(
                    loadingDialog = false,
                )

                is Failed -> oldState.copy(
                    loadingDialog = false,
                )
            }
        }

        data object Success : ChangeMediaGroup
        data class Failed(val msg: String) : ChangeMediaGroup
    }

    sealed interface CreateGroup : MediaPartialStateChange {
        override fun reduce(oldState: MediaState): MediaState {
            return when (this) {
                is Success -> oldState.copy(
                    loadingDialog = false,
                )

                is Failed -> oldState.copy(
                    loadingDialog = false,
                )
            }
        }

        data object Success : CreateGroup
        data class Failed(val msg: String) : CreateGroup
    }

    sealed interface MoveFilesToGroup : MediaPartialStateChange {
        override fun reduce(oldState: MediaState): MediaState {
            return when (this) {
                is Success -> oldState.copy(
                    loadingDialog = false,
                )

                is Failed -> oldState.copy(
                    loadingDialog = false,
                )
            }
        }

        data object Success : MoveFilesToGroup
        data class Failed(val msg: String) : MoveFilesToGroup
    }

    sealed interface EditGroup : MediaPartialStateChange {
        override fun reduce(oldState: MediaState): MediaState {
            return when (this) {
                is Success -> oldState.copy(
                    loadingDialog = false,
                )

                is Failed -> oldState.copy(
                    loadingDialog = false,
                )
            }
        }

        data class Success(val group: MediaGroupBean) : EditGroup
        data class Failed(val msg: String) : EditGroup
    }
}
