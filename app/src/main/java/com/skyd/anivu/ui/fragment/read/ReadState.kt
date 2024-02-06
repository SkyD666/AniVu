package com.skyd.anivu.ui.fragment.read

import com.skyd.anivu.base.mvi.MviViewState
import com.skyd.anivu.model.bean.ArticleWithEnclosureBean

data class ReadState(
    val articleState: ArticleState,
) : MviViewState {
    companion object {
        fun initial() = ReadState(
            articleState = ArticleState.Init,
        )
    }
}

sealed interface ArticleState {
    data class Success(val article: ArticleWithEnclosureBean) : ArticleState
    data object Init : ArticleState
    data object Loading : ArticleState
    data class Failed(val msg: String) : ArticleState
}