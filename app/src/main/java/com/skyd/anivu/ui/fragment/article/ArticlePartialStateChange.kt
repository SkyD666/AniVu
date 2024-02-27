package com.skyd.anivu.ui.fragment.article

import androidx.paging.PagingData
import com.skyd.anivu.model.bean.ArticleBean


internal sealed interface ArticlePartialStateChange {
    fun reduce(oldState: ArticleState): ArticleState

    sealed interface LoadingDialog : ArticlePartialStateChange {
        data object Show : LoadingDialog {
            override fun reduce(oldState: ArticleState) = oldState.copy(loadingDialog = true)
        }
    }

    sealed interface ArticleList : ArticlePartialStateChange {
        override fun reduce(oldState: ArticleState): ArticleState {
            return when (this) {
                is Success -> oldState.copy(
                    articleListState = ArticleListState.Success(articlePagingData = articlePagingData)
                        .apply { loading = false },
                    loadingDialog = false,
                )

                is Failed -> oldState.copy(
                    articleListState = ArticleListState.Failed(msg = msg).apply { loading = false },
                    loadingDialog = false,
                )

                Loading -> oldState.copy(
                    articleListState = oldState.articleListState.apply { loading = false },
                    loadingDialog = false,
                )
            }
        }

        data class Success(val articlePagingData: PagingData<ArticleBean>) : ArticleList
        data class Failed(val msg: String) : ArticleList
        data object Loading : ArticleList
    }

    sealed interface RefreshArticleList : ArticlePartialStateChange {
        override fun reduce(oldState: ArticleState): ArticleState {
            return when (this) {
                is Success,
                is Failed -> {
                    val articleListState = oldState.articleListState
                    oldState.copy(
                        articleListState = when (articleListState) {
                            is ArticleListState.Init -> {
                                articleListState.apply { loading = false }
                            }

                            is ArticleListState.Failed -> {
                                articleListState.copy().apply { loading = false }
                            }

                            is ArticleListState.Success -> {
                                ArticleListState.Success(articleListState.articlePagingData)
                                    .apply { loading = false }
                            }
                        },
                        loadingDialog = false,
                    )
                }

                is Loading -> oldState.copy(
                    articleListState = oldState.articleListState.apply { loading = true },
                    loadingDialog = false,
                )
            }
        }

        data object Success : RefreshArticleList
        data object Loading : RefreshArticleList
        data class Failed(val msg: String) : RefreshArticleList
    }
}