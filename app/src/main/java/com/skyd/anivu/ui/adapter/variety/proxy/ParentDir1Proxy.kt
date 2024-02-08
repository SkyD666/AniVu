package com.skyd.anivu.ui.adapter.variety.proxy


import android.view.LayoutInflater
import android.view.ViewGroup
import com.skyd.anivu.databinding.ItemParentDir1Binding
import com.skyd.anivu.model.bean.ParentDirBean
import com.skyd.anivu.ui.adapter.variety.ParentDir1ViewHolder
import com.skyd.anivu.ui.adapter.variety.VarietyAdapter


class ParentDir1Proxy(
    private val onClick: () -> Unit,
) : VarietyAdapter.Proxy<ParentDirBean, ItemParentDir1Binding, ParentDir1ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParentDir1ViewHolder {
        val holder = ParentDir1ViewHolder(
            ItemParentDir1Binding
                .inflate(LayoutInflater.from(parent.context), parent, false),
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