package com.skyd.anivu.ui.adapter.decoration

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.skyd.anivu.ext.dp
import com.skyd.anivu.ui.adapter.variety.AniSpanSize.Companion.MAX_SPAN_SIZE
import com.skyd.anivu.ui.adapter.variety.VarietyAdapter
import kotlin.math.roundToInt


class AniVuItemDecoration : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        val layoutParams = view.layoutParams as GridLayoutManager.LayoutParams
        val spanSize = layoutParams.spanSize
        val spanIndex = layoutParams.spanIndex

        val item = (parent.adapter as? VarietyAdapter)
            ?.dataList
            // 注意这里使用getChildLayoutPosition的目的
            // 如果使用getChildAdapterPosition，刷新的时候可能会（边框）闪动一下，（返回-1）
            ?.getOrNull(parent.getChildLayoutPosition(view))
        if (needVerticalMargin(item?.javaClass)) {
            outRect.top = 10.dp
            outRect.bottom = 2.dp
        }
        if (spanSize == MAX_SPAN_SIZE) {
            /**
             * 只有一列
             */
            if (noHorizontalMargin(item?.javaClass)) return
            outRect.left = HORIZONTAL_PADDING
            outRect.right = HORIZONTAL_PADDING
        } else if (spanSize == MAX_SPAN_SIZE / 2) {
            /**
             * 只有两列，没有在中间的item
             * 2x = ITEM_SPACING
             */
            val x: Int = (ITEM_SPACING / 2f).roundToInt()
            if (spanIndex == 0) {
                outRect.left = HORIZONTAL_PADDING
                outRect.right = x
            } else {
                outRect.left = x
                outRect.right = HORIZONTAL_PADDING
            }
        } else if (spanSize == MAX_SPAN_SIZE / 3) {
            /**
             * 只有三列，一个在中间的item
             * HORIZONTAL_PADDING + x = 2y
             * x + y = ITEM_SPACING
             */
            val y: Int = ((HORIZONTAL_PADDING + ITEM_SPACING) / 3f).roundToInt()
            val x: Int = ITEM_SPACING - y
            if (spanIndex == 0) {
                outRect.left = HORIZONTAL_PADDING
                outRect.right = x
            } else if (spanIndex + spanSize == MAX_SPAN_SIZE) {
                // 最右侧最后一个
                outRect.left = x
                outRect.right = HORIZONTAL_PADDING
            } else {
                outRect.left = y
                outRect.right = y
            }
        } else if (spanSize == MAX_SPAN_SIZE / 5) {
            /**
             * 只有五列
             * HORIZONTAL_PADDING + x = y + z
             * x + y = ITEM_SPACING
             * z + (HORIZONTAL_PADDING + x) / 2 = ITEM_SPACING
             */
            val x: Int = ((4 * ITEM_SPACING - 3 * HORIZONTAL_PADDING) / 5f).roundToInt()
            val y: Int = ITEM_SPACING - x
            val z: Int = HORIZONTAL_PADDING + x - y
            if (spanIndex == 0) {
                // 最左侧第一个
                outRect.left = HORIZONTAL_PADDING
                outRect.right = x
            } else if (spanIndex + spanSize == MAX_SPAN_SIZE) {
                // 最右侧最后一个
                outRect.left = x
                outRect.right = HORIZONTAL_PADDING
            } else if (spanIndex == spanSize) {
                // 第二个
                outRect.left = y
                outRect.right = z
            } else if (spanIndex == MAX_SPAN_SIZE - 2 * spanSize) {
                // 倒数第二个
                outRect.left = z
                outRect.right = y
            } else {
                // 最中间的
                outRect.left = ((HORIZONTAL_PADDING + x) / 2f).roundToInt()
                outRect.right = ((HORIZONTAL_PADDING + x) / 2f).roundToInt()
            }
        } else {
            /**
             * 多于三列（不包括五列），有在中间的item
             */
            if ((MAX_SPAN_SIZE / spanSize) % 2 == 0) {
                /**
                 * 偶数个item
                 * HORIZONTAL_PADDING + x = y + ITEM_SPACING / 2
                 * x + y = ITEM_SPACING
                 */
                val y: Int = ((HORIZONTAL_PADDING + ITEM_SPACING / 2f) / 2f).roundToInt()
                val x: Int = ITEM_SPACING - y
                if (spanIndex == 0) {
                    // 最左侧第一个
                    outRect.left = HORIZONTAL_PADDING
                    outRect.right = x
                } else if (spanIndex + spanSize == MAX_SPAN_SIZE) {
                    // 最右侧最后一个
                    outRect.left = x
                    outRect.right = HORIZONTAL_PADDING
                } else {
                    // 中间的项目
                    if (spanIndex < MAX_SPAN_SIZE / 2) {
                        // 左侧部分
                        outRect.left = y
                        outRect.right = ITEM_SPACING / 2
                    } else {
                        // 右侧部分
                        outRect.left = ITEM_SPACING / 2
                        outRect.right = y
                    }
                }
            } else {
                /**
                 * 奇数个item，严格大于5的奇数（暂无需求，未实现）
                 */
            }
        }
    }

    companion object {
        val ITEM_SPACING: Int = 12.dp
        val HORIZONTAL_PADDING: Int = 16.dp

        private val noHorizontalMarginType: Set<Class<*>> = setOf(

        )

        fun noHorizontalMargin(clz: Class<*>?): Boolean {
            clz ?: return true
            return clz in noHorizontalMarginType
        }

        private val needVerticalMarginType: Set<Class<*>> = setOf(

        )

        fun needVerticalMargin(clz: Class<*>?): Boolean {
            clz ?: return false
            return clz in needVerticalMarginType
        }
    }
}