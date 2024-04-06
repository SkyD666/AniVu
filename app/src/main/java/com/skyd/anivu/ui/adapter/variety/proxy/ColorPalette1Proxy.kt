package com.skyd.anivu.ui.adapter.variety.proxy


import android.view.LayoutInflater
import android.view.ViewGroup
import com.skyd.anivu.databinding.ItemColorPalette1Binding
import com.skyd.anivu.ext.dataStore
import com.skyd.anivu.ext.getOrDefault
import com.skyd.anivu.model.preference.appearance.ThemePreference
import com.skyd.anivu.ui.adapter.variety.ColorPalette1ViewHolder
import com.skyd.anivu.ui.adapter.variety.VarietyAdapter
import com.skyd.anivu.ui.component.preference.ColorPalettesPreference


class ColorPalette1Proxy(
    private val onClick: (Int) -> Unit,
) : VarietyAdapter.Proxy<ColorPalettesPreference.ColorPalette, ItemColorPalette1Binding, ColorPalette1ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorPalette1ViewHolder {
        val holder = ColorPalette1ViewHolder(
            ItemColorPalette1Binding
                .inflate(LayoutInflater.from(parent.context), parent, false),
        )
        holder.binding.root.setOnClickListener {
            onClick(holder.bindingAdapterPosition)
        }
        return holder
    }

    override fun onBindViewHolder(
        holder: ColorPalette1ViewHolder,
        data: ColorPalettesPreference.ColorPalette,
        index: Int,
        action: ((Any?) -> Unit)?
    ) {
        holder.binding.root.apply {
            iconBackgroundColor = data.iconBackgroundColor
            color1 = data.color1
            color2 = data.color2
            color3 = data.color3
            isChecked = context.dataStore.getOrDefault(ThemePreference) == data.name
            contentDescription = data.description
        }
    }
}