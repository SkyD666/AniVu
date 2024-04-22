package com.skyd.anivu.ui.fragment.article

import androidx.paging.PagingData
import com.skyd.anivu.model.bean.ArticleWithEnclosureBean
import kotlinx.coroutines.flow.Flow


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
                    articleListState = ArticleListState.Success(
                        articlePagingDataFlow = articlePagingDataFlow,
                        loading = false,
                    ),
                    loadingDialog = false,
                )

                is Failed -> oldState.copy(
                    articleListState = ArticleListState.Failed(msg = msg, loading = false),
                    loadingDialog = false,
                )

                Loading -> oldState.copy(
                    articleListState = oldState.articleListState.let {
                        when (it) {
                            is ArticleListState.Failed -> it.copy(loading = false)
                            is ArticleListState.Init -> it.copy(loading = false)
                            is ArticleListState.Success -> it.copy(loading = false)
                        }
                    },
                    loadingDialog = false,
                )
            }
        }

        data class Success(val articlePagingDataFlow: Flow<PagingData<ArticleWithEnclosureBean>>) :
            ArticleList

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
                            is ArticleListState.Init -> articleListState.copy(loading = false)
                            is ArticleListState.Failed -> articleListState.copy(loading = false)
                            is ArticleListState.Success -> ArticleListState.Success(
                                articlePagingDataFlow = articleListState.articlePagingDataFlow,
                                loading = false
                            )
                        },
                        loadingDialog = false,
                    )
                }

                is Loading -> oldState.copy(
                    articleListState = oldState.articleListState.let {
                        when (it) {
                            is ArticleListState.Failed -> it.copy(loading = true)
                            is ArticleListState.Init -> it.copy(loading = true)
                            is ArticleListState.Success -> it.copy(loading = true)
                        }
                    },
                    loadingDialog = false,
                )
            }
        }

        data object Success : RefreshArticleList
        data object Loading : RefreshArticleList
        data class Failed(val msg: String) : RefreshArticleList
    }
}