package com.skyd.anivu.ui.fragment.settings.data

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.ViewCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.skyd.anivu.R
import com.skyd.anivu.base.BaseFragment
import com.skyd.anivu.databinding.FragmentDataBinding
import com.skyd.anivu.ext.addInsetsByPadding
import com.skyd.anivu.ext.collectIn
import com.skyd.anivu.ext.popBackStackWithLifecycle
import com.skyd.anivu.model.bean.settings.SettingsBaseBean
import com.skyd.anivu.ui.adapter.variety.AniSpanSize
import com.skyd.anivu.ui.adapter.variety.VarietyAdapter
import com.skyd.anivu.ui.adapter.variety.proxy.settings.SettingsBaseProxy
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach


@AndroidEntryPoint
class DataFragment : BaseFragment<FragmentDataBinding>() {
    private val viewModel by viewModels<DataViewModel>()
    private val intents = Channel<DataIntent>()
    private lateinit var deleteWarningDialog: AlertDialog
    private var waitingDialog: AlertDialog? = null
    private val adapter = VarietyAdapter(mutableListOf()).apply {
        addProxy(SettingsBaseProxy(adapter = this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        deleteWarningDialog = MaterialAlertDialogBuilder(requireContext())
            .setIcon(R.drawable.ic_warning_24)
            .setTitle(R.string.warning)
            .setCancelable(false)
            .setMessage(R.string.data_fragment_clear_cache_warning)
            .setPositiveButton(R.string.clear) { _, _ -> intents.trySend(DataIntent.ClearCache) }
            .setNegativeButton(R.string.cancel, null)
            .create()
        adapter.dataList = getSettingsList()
    }

    private fun updateState(dataState: DataState) {
        if (dataState.loadingDialog) {
            if (waitingDialog == null || !waitingDialog!!.isShowing) {
                waitingDialog = MaterialAlertDialogBuilder(requireContext())
                    .setIcon(R.drawable.ic_info_24)
                    .setTitle(R.string.info)
                    .setCancelable(false)
                    .setMessage(R.string.waiting)
                    .show()
            } else if (waitingDialog!!.isShowing) {
                waitingDialog?.show()
            }
        } else {
            waitingDialog?.dismiss()
            waitingDialog = null
        }
    }

    private fun showEvent(dataEvent: DataEvent) {
        when (dataEvent) {
            is DataEvent.ClearCacheResultEvent.Success -> {
                showSnackbar(text = dataEvent.msg)
            }

            is DataEvent.ClearCacheResultEvent.Failed -> {
                showSnackbar(text = dataEvent.msg)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        intents
            .consumeAsFlow()
            .onEach(viewModel::processIntent)
            .launchIn(lifecycleScope)

        viewModel.viewState.collectIn(this) { updateState(it) }
        viewModel.singleEvent.collectIn(this) { showEvent(it) }
    }

    override fun FragmentDataBinding.initView() {
        topAppBar.setNavigationOnClickListener { findNavController().popBackStackWithLifecycle() }

        rvDataFragment.layoutManager = GridLayoutManager(
            requireContext(),
            AniSpanSize.MAX_SPAN_SIZE
        ).apply {
            spanSizeLookup = AniSpanSize(adapter)
        }
        rvDataFragment.adapter = adapter
    }

    override fun FragmentDataBinding.setWindowInsets() {
        ablDataFragment.addInsetsByPadding(top = true, left = true, right = true)
        // Fix: https://github.com/material-components/material-components-android/issues/1310
        ViewCompat.setOnApplyWindowInsetsListener(ctlDataFragment, null)
        rvDataFragment.addInsetsByPadding(bottom = true, left = true, right = true)
    }

    private fun getSettingsList(): List<Any> = mutableListOf(
        SettingsBaseBean(
            title = getString(R.string.data_fragment_clear_cache),
            description = getString(R.string.data_fragment_clear_cache_description),
            icon = AppCompatResources.getDrawable(
                requireContext(), R.drawable.ic_delete_24
            )!!,
            action = { deleteWarningDialog.show() },
        ),
    )

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentDataBinding.inflate(inflater, container, false)
}