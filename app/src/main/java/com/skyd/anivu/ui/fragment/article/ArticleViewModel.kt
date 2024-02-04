package com.skyd.anivu.ui.fragment.article

import androidx.lifecycle.viewModelScope
import com.skyd.anivu.base.mvi.AbstractMviViewModel
import com.skyd.anivu.ext.catchMap
import com.skyd.anivu.ext.startWith
import com.skyd.anivu.model.repository.ArticleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArticleViewModel @Inject constructor(
    private val articleRepo: ArticleRepository
) : AbstractMviViewModel<ArticleIntent, ArticleState, ArticleEvent>() {

    override val viewState: StateFlow<ArticleState>

    init {
        val initialVS = ArticleState.initial()

        viewState = merge(
            intentSharedFlow.filterIsInstance<ArticleIntent.Init>().take(1),
            intentSharedFlow.filterNot { it is ArticleIntent.Init }
        )
            .shareWhileSubscribed()
            .toArticlePartialStateChangeFlow()
            .debugLog("ArticlePartialStateChange")
            .sendSingleEvent()
            .scan(initialVS) { vs, change -> change.reduce(vs) }
            .debugLog("ViewState")
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                initialVS
            )
    }

    private fun Flow<ArticlePartialStateChange>.sendSingleEvent(): Flow<ArticlePartialStateChange> {
        return onEach { change ->
            val event = when (change) {
                is ArticlePartialStateChange.ArticleList.Failed -> {
                    ArticleEvent.InitArticleListResultEvent.Failed(change.msg)
                }

                is ArticlePartialStateChange.RefreshArticleList.Failed -> {
                    ArticleEvent.RefreshArticleListResultEvent.Failed(change.msg)
                }

                else -> return@onEach
            }
            sendEvent(event)
        }
    }

    private fun SharedFlow<ArticleIntent>.toArticlePartialStateChangeFlow(): Flow<ArticlePartialStateChange> {
        return merge(
            filterIsInstance<ArticleIntent.Init>().flatMapConcat { intent ->
                // 异步同步文章
                viewModelScope.launch {
                    articleRepo.refreshArticleList(intent.url)
                        .catch { }
                        .collect()
                }
                articleRepo.requestArticleList(intent.url).onStart {
                }.map {
                    ArticlePartialStateChange.ArticleList.Success(articleList = it)
                }.startWith(ArticlePartialStateChange.ArticleList.Loading).catchMap {
                    ArticlePartialStateChange.ArticleList.Failed(it.message.toString())
                }
            },
            filterIsInstance<ArticleIntent.Refresh>().flatMapConcat { intent ->
                articleRepo.refreshArticleList(intent.url).map {
                    ArticlePartialStateChange.RefreshArticleList.Success
                }.startWith(ArticlePartialStateChange.LoadingDialog.Show).catchMap {
                    ArticlePartialStateChange.RefreshArticleList.Failed(it.message.toString())
                }
            },
        )
    }
}