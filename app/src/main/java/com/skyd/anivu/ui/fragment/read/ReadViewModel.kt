package com.skyd.anivu.ui.fragment.read

import androidx.lifecycle.viewModelScope
import com.skyd.anivu.R
import com.skyd.anivu.appContext
import com.skyd.anivu.base.mvi.AbstractMviViewModel
import com.skyd.anivu.base.mvi.MviSingleEvent
import com.skyd.anivu.ext.startWith
import com.skyd.anivu.model.repository.ReadRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import javax.inject.Inject

@HiltViewModel
class ReadViewModel @Inject constructor(
    private val readRepo: ReadRepository
) : AbstractMviViewModel<ReadIntent, ReadState, MviSingleEvent>() {

    override val viewState: StateFlow<ReadState>

    init {
        val initialVS = ReadState.initial()

        viewState = merge(
            intentSharedFlow.filterIsInstance<ReadIntent.Init>().take(1),
            intentSharedFlow.filterNot { it is ReadIntent.Init }
        )
            .shareWhileSubscribed()
            .toReadPartialStateChangeFlow()
            .debugLog("ReadPartialStateChange")
            .scan(initialVS) { vs, change -> change.reduce(vs) }
            .debugLog("ViewState")
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                initialVS
            )
    }

    private fun SharedFlow<ReadIntent>.toReadPartialStateChangeFlow(): Flow<ReadPartialStateChange> {
        return merge(
            filterIsInstance<ReadIntent.Init>().flatMapConcat { intent ->
                readRepo.requestArticleWithEnclosure(intent.articleId).map {
                    if (it == null) {
                        ReadPartialStateChange.ArticleResult.Failed(
                            appContext.getString(R.string.read_fragment_article_id_illegal)
                        )
                    } else {
                        ReadPartialStateChange.ArticleResult.Success(article = it)
                    }
                }.startWith(ReadPartialStateChange.ArticleResult.Loading)
            },
        )
    }
}