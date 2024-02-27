package com.skyd.anivu.ui.adapter.variety.paging

import androidx.recyclerview.widget.GridLayoutManager
import com.skyd.anivu.ui.adapter.variety.AniSpanSize

class PagingAniSpanSize(
    private val adapter: PagingVarietyAdapter,
    private val enableLandScape: Boolean = true
) : GridLayoutManager.SpanSizeLookup() {
    override fun getSpanSize(position: Int): Int =
        AniSpanSize.getSpanSize(adapter.getItemByIndex(position), enableLandScape)
}