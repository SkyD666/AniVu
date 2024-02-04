package com.skyd.anivu.ext

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

fun getSwipeToDismissHelper(
    dragDirs: Int = 0,
    swipeDirs: Int = ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT,
    onSwiped: (RecyclerView.ViewHolder, Int) -> Unit,
): ItemTouchHelper {
    return ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
        dragDirs,
        swipeDirs
    ) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean = false

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            onSwiped(viewHolder, direction)
        }
    }
    )
}