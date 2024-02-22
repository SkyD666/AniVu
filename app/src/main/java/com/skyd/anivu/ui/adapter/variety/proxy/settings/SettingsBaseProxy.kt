package com.skyd.anivu.ui.adapter.variety.proxy.settings


import android.view.LayoutInflater
import android.view.ViewGroup
import com.skyd.anivu.databinding.ItemSettingsBaseBinding
import com.skyd.anivu.model.bean.settings.SettingsBaseBean
import com.skyd.anivu.ui.adapter.variety.SettingsBaseViewHolder
import com.skyd.anivu.ui.adapter.variety.VarietyAdapter


class SettingsBaseProxy(
    private val adapter: VarietyAdapter,
) :
    VarietyAdapter.Proxy<SettingsBaseBean, ItemSettingsBaseBinding, SettingsBaseViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SettingsBaseViewHolder {
        val holder = SettingsBaseViewHolder(
            ItemSettingsBaseBinding
                .inflate(LayoutInflater.from(parent.context), parent, false),
        )
        holder.itemView.setOnClickListener {
            val data = adapter.dataList.getOrNull(holder.bindingAdapterPosition)
            if (data !is SettingsBaseBean) return@setOnClickListener
            data.action?.invoke(data)
        }
        return holder
    }

    override fun onBindViewHolder(
        holder: SettingsBaseViewHolder,
        data: SettingsBaseBean,
        index: Int,
        action: ((Any?) -> Unit)?
    ) {
        holder.binding.apply {
            tvSettingsBaseTitle.text = data.title
            tvSettingsBaseDesc.text = data.description
            ivSettingsBaseIcon.setImageDrawable(data.icon)
        }
    }
}