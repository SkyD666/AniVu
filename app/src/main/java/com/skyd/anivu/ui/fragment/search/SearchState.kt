package com.skyd.anivu.ui.fragment.search

import androidx.paging.PagingData
import com.skyd.anivu.base.mvi.MviViewState
import kotlinx.coroutines.flow.Flow

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
    data class Success(val result: Flow<PagingData<Any>>) : SearchResultState
    data object Init : SearchResultState
    data object Loading : SearchResultState
    data class Failed(val msg: String) : SearchResultState
}