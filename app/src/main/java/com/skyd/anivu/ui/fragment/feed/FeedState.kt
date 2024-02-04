package com.skyd.anivu.ui.fragment.feed

import com.skyd.anivu.base.mvi.MviViewState
import com.skyd.anivu.model.bean.FeedBean

data class FeedState(
    val feedListState: FeedListState,
    val loadingDialog: Boolean,
) : MviViewState {
    companion object {
        fun initial() = FeedState(
            feedListState = FeedListState.Init,
            loadingDialog = false,
        )
    }
}

sealed interface FeedListState {
    data class Success(val feedList: List<FeedBean>) : FeedListState
    data object Init : FeedListState
    data object Loading : FeedListState
    data class Failed(val msg: String) : FeedListState
}