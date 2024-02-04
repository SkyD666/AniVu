package com.skyd.anivu.ui.fragment.article

import com.skyd.anivu.model.bean.ArticleBean


internal sealed interface ArticlePartialStateChange {
    fun reduce(oldState: ArticleState): ArticleState

    sealed interface LoadingDialog : ArticlePartialStateChange {
        data object Show : LoadingDialog {
            override fun reduce(oldState: ArticleState) = oldState.copy(loadingDialog = true)
        }

        data object Close : LoadingDialog {
            override fun reduce(oldState: ArticleState) = oldState.copy(loadingDialog = false)
        }
    }

    sealed interface ArticleList : ArticlePartialStateChange {
        override fun reduce(oldState: ArticleState): ArticleState {
            return when (this) {
                is Success -> oldState.copy(
                    articleListState = ArticleListState.Success(articleList = articleList),
                    loadingDialog = false,
                )

                is Failed -> oldState.copy(
                    articleListState = ArticleListState.Failed(msg = msg),
                    loadingDialog = false,
                )

                Loading -> oldState.copy(
                    articleListState = ArticleListState.Loading,
                    loadingDialog = false,
                )
            }
        }

        data class Success(val articleList: List<ArticleBean>) : ArticleList
        data class Failed(val msg: String) : ArticleList
        data object Loading : ArticleList
    }

    sealed interface RefreshArticleList : ArticlePartialStateChange {
        override fun reduce(oldState: ArticleState): ArticleState {
            return when (this) {
                is Success -> oldState.copy(
                    loadingDialog = false,
                )

                is Failed -> oldState.copy(
                    loadingDialog = false,
                )
            }
        }

        data object Success : RefreshArticleList
        data class Failed(val msg: String) : RefreshArticleList
    }
}