package com.skyd.anivu.ui.fragment.feed

import com.skyd.anivu.model.bean.FeedBean
import com.skyd.anivu.model.bean.GroupBean


internal sealed interface FeedPartialStateChange {
    fun reduce(oldState: FeedState): FeedState

    sealed interface LoadingDialog : FeedPartialStateChange {
        data object Show : LoadingDialog {
            override fun reduce(oldState: FeedState) = oldState.copy(loadingDialog = true)
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

        data class Success(val feed: FeedBean) : AddFeed
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

        data class Success(val feed: FeedBean) : EditFeed
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

    sealed interface ReadAll : FeedPartialStateChange {
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

        data class Success(val count: Int) : ReadAll
        data class Failed(val msg: String) : ReadAll
    }

    sealed interface RefreshFeed : FeedPartialStateChange {
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

        data object Success : RefreshFeed
        data class Failed(val msg: String) : RefreshFeed
    }

    sealed interface CreateGroup : FeedPartialStateChange {
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

        data object Success : CreateGroup
        data class Failed(val msg: String) : CreateGroup
    }

    sealed interface DeleteGroup : FeedPartialStateChange {
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

        data object Success : DeleteGroup
        data class Failed(val msg: String) : DeleteGroup
    }

    sealed interface MoveFeedsToGroup : FeedPartialStateChange {
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

        data object Success : MoveFeedsToGroup
        data class Failed(val msg: String) : MoveFeedsToGroup
    }

    sealed interface EditGroup : FeedPartialStateChange {
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

        data class Success(val group: GroupBean) : EditGroup
        data class Failed(val msg: String) : EditGroup
    }

    sealed interface FeedList : FeedPartialStateChange {
        override fun reduce(oldState: FeedState): FeedState {
            return when (this) {
                is Success -> oldState.copy(
                    groupListState = GroupListState.Success(dataList = dataList),
                )

                is Failed -> oldState.copy(
                    groupListState = GroupListState.Failed(msg = msg),
                )

                Loading -> oldState.copy(
                    groupListState = GroupListState.Loading,
                )
            }
        }

        data class Success(val dataList: List<Any>) : FeedList
        data class Failed(val msg: String) : FeedList
        data object Loading : FeedList
    }
}
