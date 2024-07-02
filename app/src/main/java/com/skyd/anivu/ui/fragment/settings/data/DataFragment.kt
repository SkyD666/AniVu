package com.skyd.anivu.ui.fragment.settings.data

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceScreen
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.skyd.anivu.R
import com.skyd.anivu.base.BasePreferenceFragmentCompat
import com.skyd.anivu.ext.collectIn
import com.skyd.anivu.ext.findMainNavController
import com.skyd.anivu.ext.showSnackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach


@AndroidEntryPoint
class DataFragment : BasePreferenceFragmentCompat() {
    override val title by lazy { resources.getString(R.string.data_fragment_name) }
    private val viewModel by viewModels<DataViewModel>()
    private val intents = Channel<DataIntent>()
    private lateinit var deleteWarningDialog: AlertDialog
    private var waitingDialog: AlertDialog? = null

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
            is DataEvent.ClearCacheResultEvent.Success -> showSnackbar(text = dataEvent.msg)
            is DataEvent.ClearCacheResultEvent.Failed -> showSnackbar(text = dataEvent.msg)
            is DataEvent.DeleteArticleBeforeResultEvent.Failed -> showSnackbar(text = dataEvent.msg)
            is DataEvent.DeleteArticleBeforeResultEvent.Success -> showSnackbar(text = dataEvent.msg)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        intents
            .consumeAsFlow()
            .onEach(viewModel::processIntent)
            .launchIn(lifecycleScope)

        viewModel.viewState.collectIn(lifecycleOwner = this, action = ::updateState)
        viewModel.singleEvent.collectIn(lifecycleOwner = this, action = ::showEvent)
    }

    override fun Context.onAddPreferences(
        savedInstanceState: Bundle?,
        rootKey: String?,
        screen: PreferenceScreen
    ) {
        val clearUpCategory = PreferenceCategory(this).apply {
            key = "clearUpCategory"
            title = getString(R.string.data_fragment_clear_up_category)
            screen.addPreference(this)
        }
        Preference(this).apply {
            key = "clearCache"
            title = getString(R.string.data_fragment_clear_cache)
            summary = getString(R.string.data_fragment_clear_cache_description)
            setIcon(R.drawable.ic_delete_24)
            setOnPreferenceClickListener {
                deleteWarningDialog.show()
                true
            }
            clearUpCategory.addPreference(this)
        }
        Preference(this).apply {
            key = "deleteArticleBefore"
            title = getString(R.string.data_fragment_delete_article_before)
            summary = getString(R.string.data_fragment_delete_article_before_description)
            setIcon(R.drawable.ic_today_24)
            setOnPreferenceClickListener {
                val datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText(getString(R.string.select_date))
                    .setPositiveButtonText(getString(R.string.delete))
                    .setSelection(MaterialDatePicker.thisMonthInUtcMilliseconds())
                    .build()

                datePicker.addOnPositiveButtonClickListener {
                    intents.trySend(DataIntent.DeleteArticleBefore(it))
                }
                datePicker.show(
                    requireActivity().supportFragmentManager,
                    "deleteArticleBeforePicker",
                )
                true
            }
            clearUpCategory.addPreference(this)
        }
        Preference(this).apply {
            key = "autoDeleteArticle"
            title = getString(R.string.auto_delete_screen_name)
            summary = getString(R.string.auto_delete_article_fragment_description)
            setIcon(R.drawable.ic_auto_delete_24)
            setOnPreferenceClickListener {
                findMainNavController().navigate(R.id.action_to_auto_delete_article_fragment)
                true
            }
            clearUpCategory.addPreference(this)
        }
        val syncCategory = PreferenceCategory(this).apply {
            key = "syncCategory"
            title = getString(R.string.data_fragment_sync_category)
            screen.addPreference(this)
        }
        Preference(this).apply {
            key = "importExportOpml"
            title = getString(R.string.import_export_screen_name)
            summary = getString(R.string.import_export_screen_description)
            setIcon(R.drawable.ic_swap_vert_24)
            setOnPreferenceClickListener {
                findMainNavController().navigate(R.id.action_to_import_export_fragment)
                true
            }
            syncCategory.addPreference(this)
        }
    }
}