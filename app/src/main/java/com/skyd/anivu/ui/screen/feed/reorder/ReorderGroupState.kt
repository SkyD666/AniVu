package com.skyd.anivu.ui.screen.feed.reorder

import com.skyd.anivu.base.mvi.MviViewState
import com.skyd.anivu.model.bean.GroupVo

data class ReorderGroupState(
    val groupListState: GroupListState,
    val loadingDialog: Boolean,
) : MviViewState {
    companion object {
        fun initial() = ReorderGroupState(
            groupListState = GroupListState.Init,
            loadingDialog = false,
        )
    }
}

sealed interface GroupListState {
    data class Success(val dataList: List<GroupVo>) : GroupListState
    data object Init : GroupListState
    data class Failed(val msg: String) : GroupListState
}