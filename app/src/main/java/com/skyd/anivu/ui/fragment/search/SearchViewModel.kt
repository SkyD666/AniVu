package com.skyd.anivu.ui.fragment.search

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.skyd.anivu.base.mvi.AbstractMviViewModel
import com.skyd.anivu.base.mvi.MviSingleEvent
import com.skyd.anivu.ext.catchMap
import com.skyd.anivu.ext.startWith
import com.skyd.anivu.model.repository.SearchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchRepo: SearchRepository
) : AbstractMviViewModel<SearchIntent, SearchState, MviSingleEvent>() {

    override val viewState: StateFlow<SearchState>

    init {
        val initialVS = SearchState.initial()

        viewState = merge(
            intentSharedFlow.filterIsInstance<SearchIntent.Init>().take(1),
            intentSharedFlow.filterNot { it is SearchIntent.Init }
        )
            .shareWhileSubscribed()
            .toSearchPartialStateChangeFlow()
            .debugLog("SearchPartialStateChange")
            .scan(initialVS) { vs, change -> change.reduce(vs) }
            .debugLog("ViewState")
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                initialVS
            )
    }

    private fun SharedFlow<SearchIntent>.toSearchPartialStateChangeFlow(): Flow<SearchPartialStateChange> {
        return merge(
            filterIsInstance<SearchIntent.Init>().flatMapConcat {
                flowOf(Unit).map {
                    SearchPartialStateChange.SearchResult.Success(result = flowOf(PagingData.empty()))
                }.startWith(SearchPartialStateChange.SearchResult.Loading)
                    .catchMap { SearchPartialStateChange.SearchResult.Failed(it.message.toString()) }
            },
            filterIsInstance<SearchIntent.SearchAll>().flatMapConcat { intent ->
                flowOf(searchRepo.requestSearchAll(intent.query).cachedIn(viewModelScope)).map {
                    SearchPartialStateChange.SearchResult.Success(result = it)
                }.startWith(SearchPartialStateChange.SearchResult.Loading)
                    .catchMap { SearchPartialStateChange.SearchResult.Failed(it.message.toString()) }
                    .take(2)
            },
            filterIsInstance<SearchIntent.SearchFeed>().flatMapConcat { intent ->
                flowOf(searchRepo.requestSearchFeed(intent.query).cachedIn(viewModelScope)).map {
                    @Suppress("UNCHECKED_CAST")
                    SearchPartialStateChange.SearchResult.Success(result = it as Flow<PagingData<Any>>)
                }.startWith(SearchPartialStateChange.SearchResult.Loading)
                    .catchMap { SearchPartialStateChange.SearchResult.Failed(it.message.toString()) }
                    .take(2)
            },
            filterIsInstance<SearchIntent.SearchArticle>().flatMapConcat { intent ->
                flowOf(
                    searchRepo.requestSearchArticle(intent.feedUrls, intent.query)
                        .cachedIn(viewModelScope)
                ).map {
                    @Suppress("UNCHECKED_CAST")
                    SearchPartialStateChange.SearchResult.Success(result = it as Flow<PagingData<Any>>)
                }.startWith(SearchPartialStateChange.SearchResult.Loading)
                    .catchMap { SearchPartialStateChange.SearchResult.Failed(it.message.toString()) }
                    .take(2)
            },
        )
    }
}