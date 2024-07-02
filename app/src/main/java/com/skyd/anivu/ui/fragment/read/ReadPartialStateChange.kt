package com.skyd.anivu.ui.fragment.read

import com.skyd.anivu.model.bean.ArticleWithEnclosureBean


internal sealed interface ReadPartialStateChange {
    fun reduce(oldState: ReadState): ReadState

    sealed interface LoadingDialog : ReadPartialStateChange {
        data object Show : LoadingDialog {
            override fun reduce(oldState: ReadState) = oldState.copy(loadingDialog = true)
        }
    }

    sealed interface ArticleResult : ReadPartialStateChange {
        override fun reduce(oldState: ReadState): ReadState {
            return when (this) {
                is Success -> oldState.copy(
                    articleState = ArticleState.Success(article = article),
                )

                is Failed -> oldState.copy(
                    articleState = ArticleState.Failed(msg = msg),
                )

                Loading -> oldState.copy(
                    articleState = ArticleState.Loading,
                )
            }
        }

        data class Success(val article: ArticleWithEnclosureBean) : ArticleResult
        data class Failed(val msg: String) : ArticleResult
        data object Loading : ArticleResult
    }

    sealed interface FavoriteArticle : ReadPartialStateChange {
        override fun reduce(oldState: ReadState): ReadState {
            return when (this) {
                is Success,
                is Failed -> oldState.copy(
                    loadingDialog = false,
                )
            }
        }

        data object Success : FavoriteArticle
        data class Failed(val msg: String) : FavoriteArticle
    }

    sealed interface ReadArticle : ReadPartialStateChange {
        override fun reduce(oldState: ReadState): ReadState {
            return when (this) {
                is Success,
                is Failed -> oldState.copy(
                    loadingDialog = false,
                )
            }
        }

        data object Success : ReadArticle
        data class Failed(val msg: String) : ReadArticle
    }
}
