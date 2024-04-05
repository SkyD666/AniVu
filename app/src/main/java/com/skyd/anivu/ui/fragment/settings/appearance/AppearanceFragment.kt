package com.skyd.anivu.ui.fragment.settings.appearance

import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceScreen
import com.skyd.anivu.R
import com.skyd.anivu.base.BasePreferenceFragmentCompat
import com.skyd.anivu.model.preference.appearance.ThemePreference
import com.skyd.anivu.ui.component.preference.ColorPalettesPreference
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class AppearanceFragment : BasePreferenceFragmentCompat() {
    override val title by lazy { resources.getString(R.string.appearance_fragment_name) }
    override fun Context.onAddPreferences(
        savedInstanceState: Bundle?,
        rootKey: String?,
        screen: PreferenceScreen
    ) {
        val themeCategory = PreferenceCategory(this).apply {
            key = "themeCategory"
            title = getString(R.string.appearance_fragment_theme_category)
            screen.addPreference(this)
        }

        ColorPalettesPreference(this).apply {
            key = "colorPalettes"
            val iconBackgroundColorTypedValue = TypedValue()
            val color1TypedValue = TypedValue()
            val color2TypedValue = TypedValue()
            val color3TypedValue = TypedValue()
            val theme = resources.newTheme()
            colorPalettes = (ThemePreference.values.toList() - ThemePreference.DYNAMIC).map {
                theme.applyStyle(ThemePreference.toResId(requireContext(), it), true)

                theme.resolveAttribute(
                    com.google.android.material.R.attr.colorOnPrimaryFixedVariant,
                    iconBackgroundColorTypedValue,
                    true
                )
                theme.resolveAttribute(
                    com.google.android.material.R.attr.colorPrimaryFixedDim,
                    color1TypedValue,
                    true
                )
                theme.resolveAttribute(
                    com.google.android.material.R.attr.colorSecondaryFixed,
                    color2TypedValue,
                    true
                )
                theme.resolveAttribute(
                    com.google.android.material.R.attr.colorTertiaryFixedDim,
                    color3TypedValue,
                    true
                )
                ColorPalettesPreference.ColorPalette(
                    name = it,
                    iconBackgroundColor = iconBackgroundColorTypedValue.data,
                    color1 = color1TypedValue.data,
                    color2 = color2TypedValue.data,
                    color3 = color3TypedValue.data,
                    description = ThemePreference.toDisplayName(requireContext(), it),
                )
            }
            setOnPreferenceChangeListener { _, newValue ->
                ThemePreference.put(requireContext(), lifecycleScope, newValue as String) {
                    requireActivity().recreate()
                }
                true
            }
            themeCategory.addPreference(this)
        }
    }
}