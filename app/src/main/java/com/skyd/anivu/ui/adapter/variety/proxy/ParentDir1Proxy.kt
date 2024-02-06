package com.skyd.anivu.ui.adapter.variety.proxy


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.skyd.anivu.R
import com.skyd.anivu.model.bean.ParentDirBean
import com.skyd.anivu.ui.adapter.variety.ParentDir1ViewHolder
import com.skyd.anivu.ui.adapter.variety.VarietyAdapter


class ParentDir1Proxy(
    private val onClick: () -> Unit,
) : VarietyAdapter.Proxy<ParentDirBean, ParentDir1ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val holder = ParentDir1ViewHolder(
            view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_parent_dir_1, parent, false),
        )
        holder.itemView.setOnClickListener {
            onClick()
        }
        return holder
    }

    override fun onBindViewHolder(
        holder: ParentDir1ViewHolder,
        data: ParentDirBean,
        index: Int,
        action: ((Any?) -> Unit)?
    ) = Unit
}