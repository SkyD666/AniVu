package com.skyd.anivu.ui.screen.feed.reorder

import com.skyd.anivu.model.bean.group.GroupVo


internal sealed interface ReorderGroupPartialStateChange {
    fun reduce(oldState: ReorderGroupState): ReorderGroupState

    sealed interface LoadingDialog : ReorderGroupPartialStateChange {
        data object Show : LoadingDialog {
            override fun reduce(oldState: ReorderGroupState) = oldState.copy(loadingDialog = true)
        }
    }

    sealed interface Reorder : ReorderGroupPartialStateChange {
        override fun reduce(oldState: ReorderGroupState): ReorderGroupState {
            return when (this) {
                is Failed -> oldState.copy(loadingDialog = false)
                is Success -> oldState.copy(loadingDialog = false)
            }
        }

        data object Success : Reorder
        data class Failed(val msg: String) : Reorder
    }

    sealed interface ReorderView : ReorderGroupPartialStateChange {
        override fun reduce(oldState: ReorderGroupState): ReorderGroupState {
            return when (this) {
                is Failed -> oldState.copy(loadingDialog = false)
                is Success -> if (oldState.groupListState is GroupListState.Success) {
                    oldState.copy(
                        loadingDialog = false,
                        groupListState = oldState.groupListState.copy(
                            dataList = oldState.groupListState.dataList.toMutableList().apply {
                                add(to, removeAt(from))
                            }
                        ),
                    )
                } else oldState
            }
        }

        data class Success(val from: Int, val to: Int) : ReorderView
        data class Failed(val msg: String) : ReorderView
    }

    sealed interface Reset : ReorderGroupPartialStateChange {
        override fun reduce(oldState: ReorderGroupState): ReorderGroupState {
            return when (this) {
                is Failed -> oldState.copy(loadingDialog = false)
                is Success -> oldState.copy(
                    groupListState = GroupListState.Success(dataList = dataList),
                    loadingDialog = false,
                )
            }
        }

        data class Success(val dataList: List<GroupVo>) : Reset
        data class Failed(val msg: String) : Reset
    }

    sealed interface GroupList : ReorderGroupPartialStateChange {
        override fun reduce(oldState: ReorderGroupState): ReorderGroupState {
            return when (this) {
                is Success -> oldState.copy(
                    groupListState = GroupListState.Success(dataList = dataList),
                    loadingDialog = false,
                )

                is Failed -> oldState.copy(
                    groupListState = GroupListState.Failed(msg = msg),
                    loadingDialog = false,
                )
            }
        }

        data class Success(val dataList: List<GroupVo>) : GroupList
        data class Failed(val msg: String) : GroupList
    }
}
