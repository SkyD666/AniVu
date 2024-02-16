package com.skyd.anivu.ui.fragment.search

import com.skyd.anivu.base.mvi.MviViewState

data class SearchState(
    val searchResultState: SearchResultState,
) : MviViewState {
    companion object {
        fun initial() = SearchState(
            searchResultState = SearchResultState.Init,
        )
    }
}

sealed interface SearchResultState {
    data class Success(val result: List<Any>) : SearchResultState
    data object Init : SearchResultState
    data object Loading : SearchResultState
    data class Failed(val msg: String) : SearchResultState
}