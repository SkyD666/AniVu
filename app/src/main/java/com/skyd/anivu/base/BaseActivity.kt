package com.skyd.anivu.base

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding

abstract class BaseActivity<T : ViewBinding> : AppCompatActivity() {
    protected lateinit var binding: T

    protected abstract fun getViewBinding(): T

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            window.isStatusBarContrastEnforced = false
//            window.isNavigationBarContrastEnforced = false
//        }

        binding = getViewBinding()

        beforeSetContentView()
        setContentView(binding.root)

        binding.initView()
    }

    protected open fun T.initView() {}

    protected open fun beforeSetContentView() {}
}