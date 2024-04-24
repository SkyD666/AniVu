package com.skyd.anivu.ui.fragment.feed

import com.skyd.anivu.base.mvi.MviViewState

data class FeedState(
    val groupListState: GroupListState,
    val loadingDialog: Boolean,
) : MviViewState {
    companion object {
        fun initial() = FeedState(
            groupListState = GroupListState.Init,
            loadingDialog = false,
        )
    }
}

sealed interface GroupListState {
    data class Success(val dataList: List<Any>) : GroupListState
    data object Init : GroupListState
    data object Loading : GroupListState
    data class Failed(val msg: String) : GroupListState
}