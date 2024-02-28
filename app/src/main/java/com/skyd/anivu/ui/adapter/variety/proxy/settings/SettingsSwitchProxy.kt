package com.skyd.anivu.ui.adapter.variety.proxy.settings


import android.view.LayoutInflater
import android.view.ViewGroup
import com.skyd.anivu.databinding.ItemSettingsSwitchBinding
import com.skyd.anivu.model.bean.settings.SettingsSwitchBean
import com.skyd.anivu.ui.adapter.variety.SettingsSwitchViewHolder
import com.skyd.anivu.ui.adapter.variety.VarietyAdapter


class SettingsSwitchProxy(
    private val adapter: VarietyAdapter,
) : VarietyAdapter.Proxy<SettingsSwitchBean, ItemSettingsSwitchBinding, SettingsSwitchViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SettingsSwitchViewHolder {
        val holder = SettingsSwitchViewHolder(
            ItemSettingsSwitchBinding
                .inflate(LayoutInflater.from(parent.context), parent, false),
        )

        holder.itemView.setOnClickListener {
            val data = adapter.dataList.getOrNull(holder.bindingAdapterPosition)
            if (data !is SettingsSwitchBean) return@setOnClickListener
            holder.binding.apply {
                switchSettingsSwitch.isChecked = !switchSettingsSwitch.isChecked
                data.onCheckedChanged?.invoke(switchSettingsSwitch.isChecked)
            }
        }
        return holder
    }

    override fun onBindViewHolder(
        holder: SettingsSwitchViewHolder,
        data: SettingsSwitchBean,
        index: Int,
        action: ((Any?) -> Unit)?
    ) {
        holder.binding.apply {
            tvSettingsSwitchTitle.text = data.title
            tvSettingsSwitchDesc.text = data.description
            ivSettingsSwitchIcon.setImageDrawable(data.icon)
            switchSettingsSwitch.isChecked = data.isChecked()
        }
    }
}