package com.skyd.anivu.base

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.preference.PreferenceScreen
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.appbar.MaterialToolbar
import com.skyd.anivu.R
import com.skyd.anivu.ext.addInsetsByPadding
import com.skyd.anivu.ext.findMainNavController
import com.skyd.anivu.ext.popBackStackWithLifecycle
import com.skyd.anivu.ui.component.preference.MaterialPreferenceFragmentCompat

abstract class BasePreferenceFragmentCompat : MaterialPreferenceFragmentCompat() {
    abstract val title: String

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<MaterialToolbar>(R.id.topAppBar).apply {
            title = this@BasePreferenceFragmentCompat.title
            setNavigationOnClickListener { findMainNavController().popBackStackWithLifecycle() }
        }

        view.findViewById<AppBarLayout>(R.id.abl_settings_fragment).apply {
            addInsetsByPadding(top = true, left = true, right = true)
        }

        view.findViewById<CollapsingToolbarLayout>(R.id.ctl_settings_fragment).apply {
            // Fix: https://github.com/material-components/material-components-android/issues/1310
            ViewCompat.setOnApplyWindowInsetsListener(this, null)
        }

        listView.apply {
            clipToPadding = false
            addInsetsByPadding(bottom = true, left = true, right = true)
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val context = preferenceManager.context
        val screen = preferenceManager.createPreferenceScreen(context)

        context.onAddPreferences(savedInstanceState, rootKey, screen)

        preferenceScreen = screen
    }

    abstract fun Context.onAddPreferences(
        savedInstanceState: Bundle?,
        rootKey: String?,
        screen: PreferenceScreen,
    )
}