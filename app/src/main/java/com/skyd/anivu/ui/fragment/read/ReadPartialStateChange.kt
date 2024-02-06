package com.skyd.anivu.ui.fragment.read

import com.skyd.anivu.model.bean.ArticleWithEnclosureBean


internal sealed interface ReadPartialStateChange {
    fun reduce(oldState: ReadState): ReadState

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
}
