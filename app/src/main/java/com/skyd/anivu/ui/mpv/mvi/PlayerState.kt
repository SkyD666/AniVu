package com.skyd.anivu.ui.mpv.mvi

import com.skyd.anivu.base.mvi.MviViewState

data class PlayerState(
    val needLoadLastPlayPosition: Boolean,
    val loadingDialog: Boolean,
) : MviViewState {
    companion object {
        fun initial() = PlayerState(
            needLoadLastPlayPosition = true,
            loadingDialog = true,
        )
    }
}