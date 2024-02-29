package com.skyd.anivu.ext

import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.skyd.anivu.R


fun Fragment.findMainNavController(): NavController {
    return Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_main)
}

// https://github.com/material-components/material-components-android/issues/3446
fun Fragment.showSnackbar(
    text: CharSequence,
    @BaseTransientBottomBar.Duration duration: Int = Snackbar.LENGTH_LONG,
    actionText: String? = resources.getString(R.string.close),
    onActionClick: (View) -> Unit? = {},
): Snackbar {
    val snackbar = Snackbar.make(
        view!!.rootView,
        text,
        duration
    ).run {
        if (actionText == null) this
        else setAction(actionText) { onActionClick(it) }
    }
    snackbar.show()
    return snackbar
}