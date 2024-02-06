package com.skyd.anivu.ui.activity

import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.fragment.NavHostFragment
import com.skyd.anivu.base.BaseActivity
import com.skyd.anivu.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        setSystemBarsColor(
//            view = findViewById(android.R.id.content),
//            darkMode = isInDark(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM),
//        )

        binding.navHostFragmentMain.getFragment<NavHostFragment>().navController
    }

    override fun getViewBinding() = ActivityMainBinding.inflate(layoutInflater)

    fun isInDark(value: Int) = when (value) {
        AppCompatDelegate.MODE_NIGHT_YES -> true
        AppCompatDelegate.MODE_NIGHT_NO -> false
        else -> (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES
    }

    private fun setSystemBarsColor(view: View, darkMode: Boolean) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.apply {
            statusBarColor = android.graphics.Color.TRANSPARENT
            navigationBarColor = android.graphics.Color.TRANSPARENT
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                navigationBarDividerColor = android.graphics.Color.TRANSPARENT
            }
            // 状态栏和导航栏字体颜色
            WindowInsetsControllerCompat(this, view).apply {
                isAppearanceLightStatusBars = !darkMode
                isAppearanceLightNavigationBars = !darkMode
            }
        }
    }
}