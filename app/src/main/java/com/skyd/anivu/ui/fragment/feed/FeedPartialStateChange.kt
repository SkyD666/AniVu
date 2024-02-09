package com.skyd.anivu.ui.fragment.feed

import com.skyd.anivu.model.bean.FeedBean


internal sealed interface FeedPartialStateChange {
    fun reduce(oldState: FeedState): FeedState

    sealed interface LoadingDialog : FeedPartialStateChange {
        data object Show : LoadingDialog {
            override fun reduce(oldState: FeedState) = oldState.copy(loadingDialog = true)
        }

        data object Close : LoadingDialog {
            override fun reduce(oldState: FeedState) = oldState.copy(loadingDialog = false)
        }
    }

    sealed interface AddFeed : FeedPartialStateChange {
        override fun reduce(oldState: FeedState): FeedState {
            return when (this) {
                is Success -> oldState.copy(
                    loadingDialog = false,
                )

                is Failed -> oldState.copy(
                    loadingDialog = false,
                )
            }
        }

        data object Success : AddFeed
        data class Failed(val msg: String) : AddFeed
    }

    sealed interface EditFeed : FeedPartialStateChange {
        override fun reduce(oldState: FeedState): FeedState {
            return when (this) {
                is Success -> oldState.copy(
                    loadingDialog = false,
                )

                is Failed -> oldState.copy(
                    loadingDialog = false,
                )
            }
        }

        data object Success : EditFeed
        data class Failed(val msg: String) : EditFeed
    }

    sealed interface RemoveFeed : FeedPartialStateChange {
        override fun reduce(oldState: FeedState): FeedState {
            return when (this) {
                is Success -> oldState.copy(
                    loadingDialog = false,
                )

                is Failed -> oldState.copy(
                    loadingDialog = false,
                )
            }
        }

        data object Success : RemoveFeed
        data class Failed(val msg: String) : RemoveFeed
    }

    sealed interface FeedList : FeedPartialStateChange {
        override fun reduce(oldState: FeedState): FeedState {
            return when (this) {
                is Success -> oldState.copy(
                    feedListState = FeedListState.Success(feedList = feedList),
                    loadingDialog = false,
                )

                is Failed -> oldState.copy(
                    feedListState = FeedListState.Failed(msg = msg),
                    loadingDialog = false,
                )

                Loading -> oldState.copy(
                    feedListState = FeedListState.Loading,
                    loadingDialog = false,
                )
            }
        }

        data class Success(val feedList: List<FeedBean>) : FeedList
        data class Failed(val msg: String) : FeedList
        data object Loading : FeedList
    }
}
