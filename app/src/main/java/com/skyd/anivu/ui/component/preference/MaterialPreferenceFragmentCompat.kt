package com.skyd.anivu.ui.component.preference

import android.os.Bundle
import android.view.View
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.ListPreferenceDialogFragmentCompat
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.color.MaterialColors
import com.skyd.anivu.base.BaseFragment
import com.skyd.anivu.ui.component.dialog.InputDialogBuilder

abstract class MaterialPreferenceFragmentCompat : PreferenceFragmentCompat() {

    // https://github.com/material-components/material-components-android/issues/2732
    override fun onDisplayPreferenceDialog(preference: Preference) {
        when (preference) {
            is EditTextPreference -> {
                InputDialogBuilder(requireContext())
                    .setInitInputText(preference.text.orEmpty())
                    .setPositiveButton(preference.positiveButtonText) { _, _, text ->
                        preference.text = text
                    }
                    .setIcon(preference.dialogIcon)
                    .setTitle(preference.dialogTitle)
                    .setNegativeButton(preference.negativeButtonText) { _, _ -> }
                    .show()
            }

            is ListPreference -> {
                ListPreferenceDialogFragmentCompat.newInstance(preference.getKey())
            }

            else -> /*if (preference is MultiSelectListPreference) {
                MultiSelectListPreferenceDialogFragmentCompat.newInstance(preference.getKey())
            } else */ {
                throw IllegalArgumentException(
                    "Cannot display dialog for an unknown Preference type: "
                            + preference.javaClass.simpleName
                            + ". Make sure to implement onPreferenceDisplayDialog() to handle "
                            + "displaying a custom dialog for this Preference."
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Fix https://github.com/material-components/material-components-android/issues/1984#issuecomment-1089710991
        view.setBackgroundColor(MaterialColors.getColor(view, android.R.attr.colorBackground))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        transitionProvider?.let { provider ->
            enterTransition = provider.enterTransition
            returnTransition = provider.returnTransition
            exitTransition = provider.exitTransition
            reenterTransition = provider.reenterTransition
        }
    }

    private val defaultTransitionProvider = BaseFragment.TransitionProvider()

    protected val nullTransitionProvider: BaseFragment.TransitionProvider? = null

    protected open val transitionProvider: BaseFragment.TransitionProvider? =
        defaultTransitionProvider
}