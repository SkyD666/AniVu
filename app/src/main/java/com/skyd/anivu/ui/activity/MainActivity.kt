package com.skyd.anivu.ui.activity

import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import com.skyd.anivu.base.BaseActivity
import com.skyd.anivu.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.navHostFragmentMain.getFragment<NavHostFragment>().navController
    }

    override fun getViewBinding() = ActivityMainBinding.inflate(layoutInflater)
}