package com.skyd.anivu.ui.component.preference

import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.skyd.anivu.R
import com.skyd.anivu.ext.dp
import com.skyd.anivu.ui.adapter.variety.VarietyAdapter
import com.skyd.anivu.ui.adapter.variety.proxy.ColorPalette1Proxy
import com.skyd.anivu.ui.component.SpaceItemDecoration
import kotlinx.parcelize.Parcelize

class ColorPalettesPreference : Preference {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) :
            this(context, attrs, androidx.preference.R.attr.preferenceStyle)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
            this(context, attrs, defStyleAttr, R.style.Preference_Material3)

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        layoutResource = R.layout.m3_preference_color_palettes
    }

    private var rvLayoutManager: LayoutManager? = null
    private var rvLayoutManagerState: Parcelable? = null

    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        if (isPersistent) {
            // No need to save instance state since it's persistent
            return superState
        }
        return SavedState(
            superState = superState,
            rvLayoutManagerState = rvLayoutManager?.onSaveInstanceState(),
        )
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state == null || state.javaClass != SavedState::class.java) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state)
            return
        }

        val myState = state as SavedState
        super.onRestoreInstanceState(myState.superState)

        setRecyclerViewLayoutManagerState(myState.rvLayoutManagerState)
    }

    private fun setRecyclerViewLayoutManagerState(state: Parcelable?) {
        rvLayoutManagerState = state
        notifyChanged()
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        val recyclerView = holder.findViewById(R.id.rv_color_palettes) as? RecyclerView
        if (recyclerView?.layoutManager != rvLayoutManager) {
            rvLayoutManager = recyclerView?.layoutManager
        }
        if (recyclerView != null) {
            if (recyclerView.adapter == null) {
                recyclerView.adapter = adapter
            }
            if (recyclerView.itemDecorationCount == 0) {
                recyclerView.addItemDecoration(spaceItemDecoration)
            }
            if (rvLayoutManagerState != null) {
                recyclerView.layoutManager?.onRestoreInstanceState(rvLayoutManagerState)
                rvLayoutManagerState = null
            }
        }
    }

    override fun onDetached() {
        super.onDetached()

        rvLayoutManagerState = null
        rvLayoutManager = null
    }

    private val spaceItemDecoration = SpaceItemDecoration(vertical = false, spaceSize = 10.dp)
    private val adapter = VarietyAdapter(mutableListOf(
        ColorPalette1Proxy { position ->
            val colorPalette = colorPalettes[position]
            val themeName = colorPalette.name
            selectedColorPalette = position
            callChangeListener(themeName)
            notifyChanged()
        }
    ))

    var colorPalettes: List<ColorPalette> = listOf()
        set(value) {
            field = value
            adapter.dataList = value
            selectedColorPalette = null
        }

    var selectedColorPalette: Int? = null
        set(value) {
            field = value
            notifyChanged()
        }

    data class ColorPalette(
        val name: String,
        val iconBackgroundColor: Int,
        val color1: Int,
        val color2: Int,
        val color3: Int,
        val description: String,
    )

    @Parcelize
    data class SavedState(
        @JvmField
        val superState: Parcelable?,
        val rvLayoutManagerState: Parcelable? = null,
    ) : BaseSavedState(superState)
}