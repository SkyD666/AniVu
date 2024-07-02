package com.skyd.anivu.ui.fragment.search

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.skyd.anivu.base.mvi.AbstractMviViewModel
import com.skyd.anivu.ext.catchMap
import com.skyd.anivu.ext.startWith
import com.skyd.anivu.model.repository.ArticleRepository
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
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchRepo: SearchRepository,
    private val articleRepo: ArticleRepository
) : AbstractMviViewModel<SearchIntent, SearchState, SearchEvent>() {

    override val viewState: StateFlow<SearchState>

    init {
        val initialVS = SearchState.initial()

        viewState = merge(
            intentSharedFlow.filterIsInstance<SearchIntent.ListenSearchFeed>().take(1),
            intentSharedFlow.filterIsInstance<SearchIntent.ListenSearchArticle>().take(1),
            intentSharedFlow.filterNot {
                it is SearchIntent.ListenSearchFeed || it is SearchIntent.ListenSearchArticle
            }
        )
            .shareWhileSubscribed()
            .toSearchPartialStateChangeFlow()
            .debugLog("SearchPartialStateChange")
            .sendSingleEvent()
            .scan(initialVS) { vs, change -> change.reduce(vs) }
            .debugLog("ViewState")
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                initialVS
            )
    }

    private fun Flow<SearchPartialStateChange>.sendSingleEvent(): Flow<SearchPartialStateChange> {
        return onEach { change ->
            val event = when (change) {
                is SearchPartialStateChange.FavoriteArticle.Failed -> {
                    SearchEvent.FavoriteArticleResultEvent.Failed(change.msg)
                }

                is SearchPartialStateChange.ReadArticle.Failed -> {
                    SearchEvent.ReadArticleResultEvent.Failed(change.msg)
                }

                else -> return@onEach
            }
            sendEvent(event)
        }
    }

    private fun SharedFlow<SearchIntent>.toSearchPartialStateChangeFlow(): Flow<SearchPartialStateChange> {
        return merge(
            filterIsInstance<SearchIntent.ListenSearchFeed>().flatMapConcat {
                flowOf(searchRepo.listenSearchFeed().cachedIn(viewModelScope)).map {
                    @Suppress("UNCHECKED_CAST")
                    SearchPartialStateChange.SearchResult.Success(result = it as Flow<PagingData<Any>>)
                }.startWith(SearchPartialStateChange.SearchResult.Loading)
                    .catchMap { SearchPartialStateChange.SearchResult.Failed(it.message.toString()) }
            },
            filterIsInstance<SearchIntent.ListenSearchArticle>().flatMapConcat { intent ->
                flowOf(
                    searchRepo.listenSearchArticle(intent.feedUrls).cachedIn(viewModelScope)
                ).map {
                    @Suppress("UNCHECKED_CAST")
                    SearchPartialStateChange.SearchResult.Success(result = it as Flow<PagingData<Any>>)
                }.startWith(SearchPartialStateChange.SearchResult.Loading)
                    .catchMap { SearchPartialStateChange.SearchResult.Failed(it.message.toString()) }
            },
            filterIsInstance<SearchIntent.UpdateQuery>().flatMapConcat { intent ->
                flowOf(searchRepo.updateQuery(intent.query)).map {
                    SearchPartialStateChange.UpdateQuery.Success
                }
            },
            filterIsInstance<SearchIntent.UpdateSort>().flatMapConcat { intent ->
                flowOf(searchRepo.updateSort(intent.dateDesc)).map {
                    SearchPartialStateChange.UpdateSort.Success
                }
            },
            filterIsInstance<SearchIntent.Favorite>().flatMapConcat { intent ->
                articleRepo.favoriteArticle(intent.articleId, intent.favorite).map {
                    SearchPartialStateChange.FavoriteArticle.Success
                }.startWith(SearchPartialStateChange.LoadingDialog.Show)
                    .catchMap { SearchPartialStateChange.FavoriteArticle.Failed(it.message.toString()) }
            },
            filterIsInstance<SearchIntent.Read>().flatMapConcat { intent ->
                articleRepo.readArticle(intent.articleId, intent.read).map {
                    SearchPartialStateChange.ReadArticle.Success
                }.startWith(SearchPartialStateChange.LoadingDialog.Show)
                    .catchMap { SearchPartialStateChange.ReadArticle.Failed(it.message.toString()) }
            },
        )
    }
}