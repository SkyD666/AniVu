package com.skyd.anivu.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.skyd.anivu.R
import com.skyd.anivu.ext.popBackStackWithLifecycle

abstract class BaseFragment<T : ViewBinding> : Fragment() {

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

    protected open fun T.initView() {}

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    protected fun findMainNavController() =
        Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_main)

    protected fun showSnackbar(
        text: CharSequence,
        @BaseTransientBottomBar.Duration duration: Int = Snackbar.LENGTH_LONG,
        actionText: String? = resources.getString(R.string.close),
        onActionClick: (View) -> Unit? = {},
    ) {
        Snackbar.make(
            requireActivity().findViewById(android.R.id.content),
            text,
            duration
        ).run {
            if (actionText == null) this
            else setAction(actionText) { onActionClick(it) }
        }.show()
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