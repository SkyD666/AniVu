package com.skyd.anivu.base

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding

abstract class BaseActivity<T : ViewBinding> : AppCompatActivity() {
    protected lateinit var binding: T

    protected abstract fun getViewBinding(): T

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        binding = getViewBinding()

        beforeSetContentView()
        setContentView(binding.root)

        binding.initView()
    }

    protected open fun T.initView() {}

    protected open fun beforeSetContentView() {}
}
