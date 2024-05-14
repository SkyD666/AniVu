package com.skyd.anivu.base

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.skyd.anivu.model.preference.appearance.ThemePreference

abstract class BaseActivity<T : ViewBinding> : AppCompatActivity() {
    protected lateinit var binding: T

    protected abstract fun getViewBinding(): T

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        initTheme()
        beforeSetContentView()

        binding = getViewBinding()
        setContentView(binding.root)

        binding.initView()
    }

    protected open fun T.initView() = Unit

    protected open fun beforeSetContentView() = Unit

    private fun initTheme() {
        setTheme(ThemePreference.toResId(this))
    }
}
