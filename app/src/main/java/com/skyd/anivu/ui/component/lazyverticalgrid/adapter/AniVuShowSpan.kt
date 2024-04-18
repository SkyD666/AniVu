package com.skyd.anivu.ui.component.lazyverticalgrid.adapter

import android.content.Context
import com.skyd.anivu.ext.screenIsLand
import com.skyd.anivu.model.bean.MoreBean

const val MAX_SPAN_SIZE = 60
fun anivuShowSpan(
    data: Any,
    enableLandScape: Boolean = true,
    context: Context
): Int = if (enableLandScape && context.screenIsLand) {
    when (data) {
        is MoreBean -> MAX_SPAN_SIZE / 3
        else -> MAX_SPAN_SIZE / 3
    }
} else {
    when (data) {
        is MoreBean -> MAX_SPAN_SIZE / 2
        else -> MAX_SPAN_SIZE / 1
    }
}