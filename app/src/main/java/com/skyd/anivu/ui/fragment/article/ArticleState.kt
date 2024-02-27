package com.skyd.anivu.ui.fragment.article

import androidx.paging.PagingData
import com.skyd.anivu.base.mvi.MviViewState
import com.skyd.anivu.model.bean.ArticleBean

data class ArticleState(
    val articleListState: ArticleListState,
    val loadingDialog: Boolean,
) : MviViewState {
    companion object {
        fun initial() = ArticleState(
            articleListState = ArticleListState.Init,
            loadingDialog = false,
        )
    }
}

sealed class ArticleListState(var loading: Boolean = false) {
    class Success(val articlePagingData: PagingData<ArticleBean>) : ArticleListState()
    data object Init : ArticleListState()
    data class Failed(val msg: String) : ArticleListState()
}