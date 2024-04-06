package com.skyd.anivu.ui.fragment.settings.appearance

import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceScreen
import androidx.preference.SwitchPreferenceCompat
import com.google.android.material.color.DynamicColors
import com.skyd.anivu.R
import com.skyd.anivu.base.BasePreferenceFragmentCompat
import com.skyd.anivu.ext.dataStore
import com.skyd.anivu.ext.getOrDefault
import com.skyd.anivu.ext.inDarkMode
import com.skyd.anivu.model.preference.appearance.DarkModePreference
import com.skyd.anivu.model.preference.appearance.ThemePreference
import com.skyd.anivu.ui.component.preference.ColorPalettesPreference
import com.skyd.anivu.ui.component.preference.ToggleGroupPreference
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class AppearanceFragment : BasePreferenceFragmentCompat() {
    override val title by lazy { resources.getString(R.string.appearance_fragment_name) }

    private var useDynamicTheme: SwitchPreferenceCompat? = null

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

        ToggleGroupPreference(this).apply {
            buttons = DarkModePreference.values.map {
                ToggleGroupPreference.ButtonData(
                    tag = it,
                    text = DarkModePreference.toDisplayName(requireContext(), it),
                )
            }
            check(requireContext().dataStore.getOrDefault(DarkModePreference))
            setOnPreferenceChangeListener { _, newValue ->
                DarkModePreference.put(requireContext(), lifecycleScope, newValue as Int)
                true
            }
            themeCategory.addPreference(this)
        }

        val colorPalettesPreference = ColorPalettesPreference(this).apply {
            key = "colorPalettes"
            isPersistent = false
            colorPalettes = getColorPalettes()
            setOnPreferenceChangeListener { _, newValue ->
                ThemePreference.put(requireContext(), lifecycleScope, newValue as String) {
                    requireActivity().recreate()
                    useDynamicTheme?.isChecked = requireContext().dataStore
                        .getOrDefault(ThemePreference) == ThemePreference.DYNAMIC
                }
                true
            }
            themeCategory.addPreference(this)
        }

        if (DynamicColors.isDynamicColorAvailable()) {
            useDynamicTheme = SwitchPreferenceCompat(this).apply {
                key = "useDynamicTheme"
                title = getString(R.string.appearance_fragment_use_dynamic_theme)
                summary = getString(R.string.appearance_fragment_use_dynamic_theme_description)
                setIcon(R.drawable.ic_colorize_24)
                isChecked =
                    requireContext().dataStore.getOrDefault(ThemePreference) == ThemePreference.DYNAMIC
                setOnPreferenceChangeListener { _, newValue ->
                    ThemePreference.put(
                        context = requireContext(),
                        scope = lifecycleScope,
                        value = if (newValue as Boolean) ThemePreference.DYNAMIC
                        else ThemePreference.basicValues.first(),
                    ) {
                        requireActivity().recreate()
                        colorPalettesPreference.colorPalettes = getColorPalettes()
                    }
                    true
                }
                themeCategory.addPreference(this)
            }
        }
    }

    private fun getColorPalettes(): List<ColorPalettesPreference.ColorPalette> {
        val iconBackgroundColorTypedValue = TypedValue()
        val color1TypedValue = TypedValue()
        val color2TypedValue = TypedValue()
        val color3TypedValue = TypedValue()
        val theme = resources.newTheme()
        return ThemePreference.basicValues.map {
            theme.applyStyle(ThemePreference.toResId(requireContext(), it), true)

            theme.resolveAttribute(
                if (requireContext().inDarkMode()) {
                    com.google.android.material.R.attr.colorOnPrimary
                } else com.google.android.material.R.attr.colorOnPrimaryFixed,
                iconBackgroundColorTypedValue,
                true
            )
            theme.resolveAttribute(
                if (requireContext().inDarkMode()) {
                    com.google.android.material.R.attr.colorPrimaryContainer
                } else com.google.android.material.R.attr.colorPrimary,
                color1TypedValue,
                true
            )
            theme.resolveAttribute(
                com.google.android.material.R.attr.colorSecondaryFixedDim,
                color2TypedValue,
                true
            )
            theme.resolveAttribute(
                if (requireContext().inDarkMode()) {
                    com.google.android.material.R.attr.colorTertiaryContainer
                } else com.google.android.material.R.attr.colorTertiary,
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
    }
}