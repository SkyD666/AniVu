package com.skyd.anivu.ext

import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.skyd.anivu.R


fun Fragment.findMainNavController(): NavController {
    return Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_main)
}