package com.skyd.anivu.base

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.viewbinding.ViewBinding

abstract class BaseDialogFragment<T : ViewBinding> : DialogFragment() {
    var onDismissListener: ((DialogInterface) -> Unit)? = null

    private var _binding: T? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
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
    }

    protected open fun T.initView() = Unit

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissListener?.invoke(dialog)
    }
}