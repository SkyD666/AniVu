package com.skyd.anivu.ui.adapter.variety

import androidx.recyclerview.widget.GridLayoutManager
import com.skyd.anivu.appContext
import com.skyd.anivu.ext.screenIsLand
import com.skyd.anivu.model.bean.FeedBean
import com.skyd.anivu.model.bean.MoreBean

class AniSpanSize(
    private val adapter: VarietyAdapter,
    private val enableLandScape: Boolean = true
) : GridLayoutManager.SpanSizeLookup() {
    companion object {
        const val MAX_SPAN_SIZE = 60

        fun getSpanSize(data: Any?, enableLandScape: Boolean): Int {
            return if (enableLandScape && appContext.screenIsLand) {
                when (data) {
                    is FeedBean -> MAX_SPAN_SIZE
                    is MoreBean -> MAX_SPAN_SIZE / 3
                    else -> MAX_SPAN_SIZE
                }
            } else {
                when (data) {
                    is FeedBean -> MAX_SPAN_SIZE
                    is MoreBean -> MAX_SPAN_SIZE / 2
                    else -> MAX_SPAN_SIZE
                }
            }
        }
    }

    override fun getSpanSize(position: Int): Int =
        getSpanSize(adapter.dataList[position], enableLandScape)
}