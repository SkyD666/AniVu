package com.skyd.anivu.ui.fragment.settings.data


internal sealed interface DataPartialStateChange {
    fun reduce(oldState: DataState): DataState

    sealed interface LoadingDialog : DataPartialStateChange {
        data object Show : LoadingDialog {
            override fun reduce(oldState: DataState) = oldState.copy(loadingDialog = true)
        }
    }

    sealed interface ClearCacheResult : DataPartialStateChange {
        override fun reduce(oldState: DataState): DataState {
            return when (this) {
                is Success,
                is Failed -> oldState.copy(
                    loadingDialog = false,
                )
            }
        }

        data class Success(val deletedSize: Long) : ClearCacheResult
        data class Failed(val msg: String) : ClearCacheResult
    }

    sealed interface DeleteArticleBeforeResult : DataPartialStateChange {
        override fun reduce(oldState: DataState): DataState {
            return when (this) {
                is Success,
                is Failed -> oldState.copy(
                    loadingDialog = false,
                )
            }
        }

        data class Success(val count: Int) : DeleteArticleBeforeResult
        data class Failed(val msg: String) : DeleteArticleBeforeResult
    }
}
