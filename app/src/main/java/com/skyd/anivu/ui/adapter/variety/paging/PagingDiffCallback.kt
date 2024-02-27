package com.skyd.anivu.ui.adapter.variety.paging

import androidx.recyclerview.widget.DiffUtil
import com.skyd.anivu.ui.adapter.variety.Diff

class PagingDiffCallback : DiffUtil.ItemCallback<Any>() {
    override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
        val oldDiff = oldItem as? Diff
        val newDiff = newItem as? Diff
        return if (oldDiff == null || newDiff == null) oldItem.hashCode() == newItem.hashCode()
        else oldDiff sameAs newItem
    }

    override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
        val oldDiff = oldItem as? Diff
        val newDiff = newItem as? Diff
        return if (oldDiff == null || newDiff == null) oldItem == newItem
        else oldDiff contentSameAs newDiff
    }
}
