package com.skyd.anivu.ui.component.lazyverticalgrid.adapter.proxy

import androidx.compose.runtime.Composable
import com.skyd.anivu.model.bean.GroupBean
import com.skyd.anivu.ui.component.lazyverticalgrid.adapter.LazyGridAdapter

class DefaultGroup1Proxy(
    private val group1Proxy: Group1Proxy,
) : LazyGridAdapter.Proxy<GroupBean.DefaultGroup>() {
    @Composable
    override fun Draw(index: Int, data: GroupBean.DefaultGroup) {
        Group1Item(
            data = data,
            initExpand = group1Proxy.isExpand,
            onExpandChange = group1Proxy.onExpandChange,
            onShowAllArticles = group1Proxy.onShowAllArticles,
            onDelete = group1Proxy.onDelete,
            onFeedsMoveTo = group1Proxy.onMoveFeedsTo,
        )
    }
}