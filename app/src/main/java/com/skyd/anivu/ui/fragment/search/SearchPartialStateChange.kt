package com.skyd.anivu.ui.fragment.search


internal sealed interface SearchPartialStateChange {
    fun reduce(oldState: SearchState): SearchState

    sealed interface SearchResult : SearchPartialStateChange {
        override fun reduce(oldState: SearchState): SearchState {
            return when (this) {
                is Success -> oldState.copy(
                    searchResultState = SearchResultState.Success(result = result),
                )

                is Failed -> oldState.copy(
                    searchResultState = SearchResultState.Failed(msg = msg),
                )

                Loading -> oldState.copy(
                    searchResultState = SearchResultState.Loading,
                )
            }
        }

        data class Success(val result: List<Any>) : SearchResult
        data class Failed(val msg: String) : SearchResult
        data object Loading : SearchResult
    }
}
