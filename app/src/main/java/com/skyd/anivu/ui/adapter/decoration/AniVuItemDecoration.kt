package com.skyd.anivu.ui.adapter.decoration

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.skyd.anivu.ext.dp
import com.skyd.anivu.ui.adapter.variety.AniSpanSize.Companion.MAX_SPAN_SIZE
import com.skyd.anivu.ui.adapter.variety.VarietyAdapter
import kotlin.math.roundToInt


class AniVuItemDecoration(
    private val hItemSpace: Int = H_ITEM_SPACE,
    private val horizontalSpace: Int = HORIZONTAL_PADDING,
) : RecyclerView.ItemDecoration() {
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
        if (spanSize == MAX_SPAN_SIZE) {
            /**
             * 只有一列
             */
            if (noHorizontalMargin(item?.javaClass)) return
            outRect.left = horizontalSpace
            outRect.right = horizontalSpace
        } else if (spanSize == MAX_SPAN_SIZE / 2) {
            /**
             * 只有两列，没有在中间的item
             * 2x = hItemSpace
             */
            val x: Int = (hItemSpace / 2f).roundToInt()
            if (spanIndex == 0) {
                outRect.left = horizontalSpace
                outRect.right = x
            } else {
                outRect.left = x
                outRect.right = horizontalSpace
            }
        } else if (spanSize == MAX_SPAN_SIZE / 3) {
            /**
             * 只有三列，一个在中间的item
             * horizontalSpace + x = 2y
             * x + y = hItemSpace
             */
            val y: Int = ((horizontalSpace + hItemSpace) / 3f).roundToInt()
            val x: Int = hItemSpace - y
            if (spanIndex == 0) {
                outRect.left = horizontalSpace
                outRect.right = x
            } else if (spanIndex + spanSize == MAX_SPAN_SIZE) {
                // 最右侧最后一个
                outRect.left = x
                outRect.right = horizontalSpace
            } else {
                outRect.left = y
                outRect.right = y
            }
        } else if (spanSize == MAX_SPAN_SIZE / 5) {
            /**
             * 只有五列
             * horizontalSpace + x = y + z
             * x + y = hItemSpace
             * z + (horizontalSpace + x) / 2 = hItemSpace
             */
            val x: Int = ((4 * hItemSpace - 3 * horizontalSpace) / 5f).roundToInt()
            val y: Int = hItemSpace - x
            val z: Int = horizontalSpace + x - y
            if (spanIndex == 0) {
                // 最左侧第一个
                outRect.left = horizontalSpace
                outRect.right = x
            } else if (spanIndex + spanSize == MAX_SPAN_SIZE) {
                // 最右侧最后一个
                outRect.left = x
                outRect.right = horizontalSpace
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
                outRect.left = ((horizontalSpace + x) / 2f).roundToInt()
                outRect.right = ((horizontalSpace + x) / 2f).roundToInt()
            }
        } else {
            /**
             * 多于三列（不包括五列），有在中间的item
             */
            if ((MAX_SPAN_SIZE / spanSize) % 2 == 0) {
                /**
                 * 偶数个item
                 * horizontalSpace + x = y + hItemSpace / 2
                 * x + y = hItemSpace
                 */
                val y: Int = ((horizontalSpace + hItemSpace / 2f) / 2f).roundToInt()
                val x: Int = hItemSpace - y
                if (spanIndex == 0) {
                    // 最左侧第一个
                    outRect.left = horizontalSpace
                    outRect.right = x
                } else if (spanIndex + spanSize == MAX_SPAN_SIZE) {
                    // 最右侧最后一个
                    outRect.left = x
                    outRect.right = horizontalSpace
                } else {
                    // 中间的项目
                    if (spanIndex < MAX_SPAN_SIZE / 2) {
                        // 左侧部分
                        outRect.left = y
                        outRect.right = hItemSpace / 2
                    } else {
                        // 右侧部分
                        outRect.left = hItemSpace / 2
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
        val H_ITEM_SPACE: Int = 16.dp
        val HORIZONTAL_PADDING: Int = 16.dp
        val VERTICAL_PADDING: Int = 16.dp

        private val noHorizontalMarginType: Set<Class<*>> = setOf(

        )

        fun noHorizontalMargin(clz: Class<*>?): Boolean {
            clz ?: return true
            return clz in noHorizontalMarginType
        }
    }
}