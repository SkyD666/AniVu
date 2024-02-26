package com.skyd.anivu.ui.adapter.variety.proxy


import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import com.skyd.anivu.databinding.ItemMore1Binding
import com.skyd.anivu.model.bean.MoreBean
import com.skyd.anivu.ui.adapter.variety.More1ViewHolder
import com.skyd.anivu.ui.adapter.variety.VarietyAdapter


class More1Proxy(
    private val onClick: (Int) -> Unit,
) : VarietyAdapter.Proxy<MoreBean, ItemMore1Binding, More1ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): More1ViewHolder {
        val holder = More1ViewHolder(
            ItemMore1Binding
                .inflate(LayoutInflater.from(parent.context), parent, false),
        )
        holder.itemView.setOnClickListener {
            onClick(holder.bindingAdapterPosition)
        }
        return holder
    }

    override fun onBindViewHolder(
        holder: More1ViewHolder,
        data: MoreBean,
        index: Int,
        action: ((Any?) -> Unit)?
    ) {
        holder.binding.ivMore1Icon.apply {
            imageTintList = ColorStateList.valueOf(data.iconTint)
            setImageDrawable(data.icon)

            backgroundTintList = ColorStateList.valueOf(data.backgroundTint)
            background = data.background
        }
        holder.binding.tvMore1Title.text = data.title
    }
}