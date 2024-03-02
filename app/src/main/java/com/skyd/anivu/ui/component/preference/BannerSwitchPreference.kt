package com.skyd.anivu.ui.component.preference

import android.content.Context
import android.util.AttributeSet
import androidx.preference.SwitchPreferenceCompat
import com.skyd.anivu.R

class BannerSwitchPreference : SwitchPreferenceCompat {
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) :
            super(context, attrs, defStyleAttr, defStyleRes)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
            this(context, attrs, defStyleAttr, R.style.Preference_MaterialBannerSwitchPreference)

    constructor(context: Context, attrs: AttributeSet?) :
            super(context, attrs, R.attr.bannerSwitchPreferenceStyle)

    constructor(context: Context) : this(context, null)
}