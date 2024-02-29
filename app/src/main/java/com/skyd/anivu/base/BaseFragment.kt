package com.skyd.anivu.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.skyd.anivu.R
import com.skyd.anivu.ext.popBackStackWithLifecycle

abstract class BaseFragment<T : ViewBinding> : Fragment() {

    private var _binding: T? = null

    // This property is only valid between onCreateView and onDestroyView.
    protected val binding get() = _binding!!

    protected abstract fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): T

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = getViewBinding(inflater, container)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.initView()
        binding.setWindowInsets()
        // Fix https://github.com/material-components/material-components-android/issues/1984#issuecomment-1089710991
        view.setBackgroundColor(MaterialColors.getColor(view, android.R.attr.colorBackground))
    }

    protected open fun T.initView() {}

    protected open fun T.setWindowInsets() {}

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    protected fun checkArgument(
        messageRes: Int,
        action: () -> Boolean,
        onSuccess: () -> Unit,
        onError: (() -> Unit)? = null,
    ) {
        if (action()) {
            onSuccess()
        } else {
            if (onError == null) {
                MaterialAlertDialogBuilder(requireContext())
                    .setIcon(R.drawable.ic_warning_24)
                    .setTitle(R.string.warning)
                    .setMessage(messageRes)
                    .setCancelable(false)
                    .setPositiveButton(R.string.exit) { _, _ ->
                        findNavController().popBackStackWithLifecycle()
                    }
                    .show()
            } else onError()
        }
    }
}