package com.skyd.anivu.ui.component.preference

import android.content.Context
import android.util.AttributeSet
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import androidx.recyclerview.widget.RecyclerView
import com.skyd.anivu.R
import com.skyd.anivu.ext.dp
import com.skyd.anivu.ui.adapter.variety.VarietyAdapter
import com.skyd.anivu.ui.adapter.variety.proxy.ColorPalette1Proxy
import com.skyd.anivu.ui.component.SpaceItemDecoration

class ColorPalettesPreference : Preference {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) :
            this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
            this(context, attrs, defStyleAttr, 0)

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        layoutResource = R.layout.m3_preference_color_palettes
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        val recyclerView = holder.findViewById(R.id.rv_color_palettes) as? RecyclerView
        if (recyclerView != null) {
            if (recyclerView.adapter == null) {
                recyclerView.adapter = adapter
            }
            if (recyclerView.itemDecorationCount == 0) {
                recyclerView.addItemDecoration(spaceItemDecoration)
            }
        }
    }

    private val spaceItemDecoration = SpaceItemDecoration(vertical = false, spaceSize = 10.dp)
    private val adapter = VarietyAdapter(mutableListOf(
        ColorPalette1Proxy { position ->
            val colorPalette = colorPalettes[position]
            val themeName = colorPalette.name
            callChangeListener(themeName)
            notifyChanged()
        }
    ))

    var colorPalettes: List<ColorPalette> = listOf()
        set(value) {
            field = value
            adapter.dataList = value
        }

    data class ColorPalette(
        val name: String,
        val iconBackgroundColor: Int,
        val color1: Int,
        val color2: Int,
        val color3: Int,
        val description: String,
    )
}