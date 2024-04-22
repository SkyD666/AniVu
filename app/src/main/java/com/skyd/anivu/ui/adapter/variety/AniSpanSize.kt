package com.skyd.anivu.ui.adapter.variety

import androidx.recyclerview.widget.GridLayoutManager
import com.skyd.anivu.appContext
import com.skyd.anivu.ext.screenIsLand

class AniSpanSize(
    private val adapter: VarietyAdapter,
    private val enableLandScape: Boolean = true
) : GridLayoutManager.SpanSizeLookup() {
    companion object {
        const val MAX_SPAN_SIZE = 60

        fun getSpanSize(data: Any?, enableLandScape: Boolean): Int {
            return if (enableLandScape && appContext.screenIsLand) {
                when (data) {
                    else -> MAX_SPAN_SIZE
                }
            } else {
                when (data) {
                    else -> MAX_SPAN_SIZE
                }
            }
        }
    }

    override fun getSpanSize(position: Int): Int =
        getSpanSize(adapter.dataList[position], enableLandScape)
}