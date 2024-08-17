package com.skyd.anivu.ui.screen.feed.reorder

import com.skyd.anivu.base.mvi.MviIntent

sealed interface ReorderGroupIntent : MviIntent {
    data object Init : ReorderGroupIntent
    data object Reset : ReorderGroupIntent
    data class ReorderView(
        val from: Int,
        val to: Int,
    ) : ReorderGroupIntent
    data class Reorder(
        val movedGroupId: String,
        val newPreviousGroupId: String? = null,
        val newNextGroupId: String? = null,
    ) : ReorderGroupIntent
}