package com.skyd.anivu.ui.component

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class SpaceItemDecoration(
    private val vertical: Boolean = true,
    private val spaceSize: Int
) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        if (parent.getChildAdapterPosition(view) != parent.adapter!!.itemCount - 1) {
            if (vertical) {
                outRect.bottom = spaceSize
            } else {
                outRect.right = spaceSize
            }
        }
    }
}