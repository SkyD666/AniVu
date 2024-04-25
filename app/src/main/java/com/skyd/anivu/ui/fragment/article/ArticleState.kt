package com.skyd.anivu.ui.fragment.article

import androidx.paging.PagingData
import com.skyd.anivu.base.mvi.MviViewState
import com.skyd.anivu.model.bean.ArticleWithFeed
import kotlinx.coroutines.flow.Flow

data class ArticleState(
    val articleListState: ArticleListState,
    val loadingDialog: Boolean,
) : MviViewState {
    companion object {
        fun initial() = ArticleState(
            articleListState = ArticleListState.Init(),
            loadingDialog = false,
        )
    }
}

sealed class ArticleListState(open val loading: Boolean = false) {
    data class Success(
        val articlePagingDataFlow: Flow<PagingData<ArticleWithFeed>>,
        override val loading: Boolean = false
    ) : ArticleListState()

    data class Init(override val loading: Boolean = false) : ArticleListState()
    data class Failed(val msg: String, override val loading: Boolean = false) :
        ArticleListState()
}