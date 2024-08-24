package com.skyd.anivu.ui.mpv.mvi


internal sealed interface PlayerPartialStateChange {
    fun reduce(oldState: PlayerState): PlayerState

    sealed interface LoadingDialog : PlayerPartialStateChange {
        data object Show : LoadingDialog {
            override fun reduce(oldState: PlayerState) = oldState.copy(loadingDialog = true)
        }
    }

    sealed interface TrySeekToLast : PlayerPartialStateChange {
        override fun reduce(oldState: PlayerState): PlayerState {
            return when (this) {
                is Success,
                NoNeed,
                is Failed -> oldState.copy(
                    needLoadLastPlayPosition = false,
                    loadingDialog = false,
                )
            }
        }

        data class Success(val position: Long) : TrySeekToLast
        data object NoNeed : TrySeekToLast
        data class Failed(val msg: String) : TrySeekToLast
    }
}
