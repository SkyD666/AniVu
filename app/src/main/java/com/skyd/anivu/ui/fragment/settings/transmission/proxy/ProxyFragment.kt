package com.skyd.anivu.ui.fragment.settings.transmission.proxy

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.preference.DropDownPreference
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.Preference.SummaryProvider
import androidx.preference.PreferenceScreen
import com.skyd.anivu.R
import com.skyd.anivu.base.BasePreferenceFragmentCompat
import com.skyd.anivu.ext.dataStore
import com.skyd.anivu.ext.getOrDefault
import com.skyd.anivu.model.preference.proxy.ProxyHostnamePreference
import com.skyd.anivu.model.preference.proxy.ProxyModePreference
import com.skyd.anivu.model.preference.proxy.ProxyPasswordPreference
import com.skyd.anivu.model.preference.proxy.ProxyPortPreference
import com.skyd.anivu.model.preference.proxy.ProxyTypePreference
import com.skyd.anivu.model.preference.proxy.ProxyUsernamePreference
import com.skyd.anivu.model.preference.proxy.UseProxyPreference
import com.skyd.anivu.ui.component.preference.BannerSwitchPreference
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ProxyFragment : BasePreferenceFragmentCompat() {
    override val title by lazy { resources.getString(R.string.proxy_fragment_name) }
    private lateinit var bannerSwitchPreference: BannerSwitchPreference
    private lateinit var proxyModePreference: DropDownPreference
    private lateinit var proxyTypePreference: DropDownPreference
    private lateinit var proxyHostnamePreference: EditTextPreference
    private lateinit var proxyPortPreference: EditTextPreference
    private lateinit var proxyUsernamePreference: EditTextPreference
    private lateinit var proxyPasswordPreference: EditTextPreference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)

        proxyModePreference.dependency = bannerSwitchPreference.key
        proxyTypePreference.dependency = bannerSwitchPreference.key
        proxyHostnamePreference.dependency = bannerSwitchPreference.key
        proxyPortPreference.dependency = bannerSwitchPreference.key
        proxyUsernamePreference.dependency = bannerSwitchPreference.key
        proxyPasswordPreference.dependency = bannerSwitchPreference.key
    }

    override fun Context.onAddPreferences(
        savedInstanceState: Bundle?,
        rootKey: String?,
        screen: PreferenceScreen
    ) {
        bannerSwitchPreference = BannerSwitchPreference(this).apply {
            key = "useProxy"
            title = getString(R.string.proxy_fragment_use_proxy)
            isChecked = requireContext().dataStore.getOrDefault(UseProxyPreference)
            setIcon(if (isChecked) R.drawable.ic_vpn_key_24 else R.drawable.ic_vpn_key_off_24)
            setOnPreferenceChangeListener { _, newValue ->
                val value = newValue as Boolean
                setIcon(if (value) R.drawable.ic_vpn_key_24 else R.drawable.ic_vpn_key_off_24)
                UseProxyPreference.put(requireContext(), lifecycleScope, value)
                true
            }
            screen.addPreference(this)
        }

        proxyModePreference = DropDownPreference(this).apply {
            key = "proxyMode"
            title = getString(R.string.proxy_fragment_mode)
            value = ProxyModePreference.toDisplayName(context)
            summaryProvider = ListPreference.SimpleSummaryProvider.getInstance()
            entries = arrayOf(
                ProxyModePreference.toDisplayName(context, ProxyModePreference.AUTO_MODE),
                ProxyModePreference.toDisplayName(context, ProxyModePreference.MANUAL_MODE),
            )
            entryValues = arrayOf(
                ProxyModePreference.AUTO_MODE,
                ProxyModePreference.MANUAL_MODE,
            )
            setOnPreferenceChangeListener { _, newValue ->
                val value = newValue as String
                val enable = value != ProxyModePreference.AUTO_MODE
                proxyTypePreference.isEnabled = enable
                proxyHostnamePreference.isEnabled = enable
                proxyPortPreference.isEnabled = enable
                proxyUsernamePreference.isEnabled = enable
                proxyPasswordPreference.isEnabled = enable
                ProxyModePreference.put(requireContext(), lifecycleScope, value)
                true
            }
            screen.addPreference(this)
        }

        proxyTypePreference = DropDownPreference(this).apply {
            key = "proxyType"
            title = getString(R.string.proxy_fragment_type)
            setIcon(R.drawable.ic_http_24)
            value = context.dataStore.getOrDefault(ProxyTypePreference)
            summaryProvider = ListPreference.SimpleSummaryProvider.getInstance()
            entries = arrayOf(
                ProxyTypePreference.HTTP,
                ProxyTypePreference.SOCKS4,
                ProxyTypePreference.SOCKS5,
            )
            entryValues = arrayOf(
                ProxyTypePreference.HTTP,
                ProxyTypePreference.SOCKS4,
                ProxyTypePreference.SOCKS5,
            )
            setOnPreferenceChangeListener { _, newValue ->
                ProxyTypePreference.put(requireContext(), lifecycleScope, newValue as String)
                true
            }
            isEnabled = requireContext().dataStore.getOrDefault(ProxyModePreference) !=
                    ProxyModePreference.AUTO_MODE
            screen.addPreference(this)
        }

        proxyHostnamePreference = EditTextPreference(this).apply {
            key = "proxyHostname"
            title = getString(R.string.proxy_fragment_hostname)
            dialogTitle = getString(R.string.proxy_fragment_hostname)
            setIcon(R.drawable.ic_devices_24)
            setDialogIcon(R.drawable.ic_devices_24)
            text = requireContext().dataStore.getOrDefault(ProxyHostnamePreference)
            summaryProvider = EditTextPreference.SimpleSummaryProvider.getInstance()
            setOnPreferenceChangeListener { _, newValue ->
                ProxyHostnamePreference.put(requireContext(), lifecycleScope, newValue as String)
                true
            }
            isEnabled = requireContext().dataStore.getOrDefault(ProxyModePreference) !=
                    ProxyModePreference.AUTO_MODE
            screen.addPreference(this)
        }

        proxyPortPreference = EditTextPreference(this).apply {
            key = "proxyPort"
            title = getString(R.string.proxy_fragment_port)
            dialogTitle = getString(R.string.proxy_fragment_port)
            setIcon(R.drawable.ic_podcasts_24)
            setDialogIcon(R.drawable.ic_podcasts_24)
            text = requireContext().dataStore.getOrDefault(ProxyPortPreference).toString()
            summaryProvider = EditTextPreference.SimpleSummaryProvider.getInstance()
            setOnPreferenceChangeListener { _, newValue ->
                runCatching {
                    val port = (newValue as String).toInt()
                    check(port in 1..65535)
                    ProxyPortPreference.put(requireContext(), lifecycleScope, port)
                }
                true
            }
            isEnabled = requireContext().dataStore.getOrDefault(ProxyModePreference) !=
                    ProxyModePreference.AUTO_MODE
            screen.addPreference(this)
        }

        proxyUsernamePreference = EditTextPreference(this).apply {
            key = "proxyUsername"
            title = getString(R.string.proxy_fragment_username)
            dialogTitle = getString(R.string.proxy_fragment_username)
            setIcon(R.drawable.ic_person_24)
            setDialogIcon(R.drawable.ic_person_24)
            text = requireContext().dataStore.getOrDefault(ProxyUsernamePreference).toString()
            summaryProvider = EditTextPreference.SimpleSummaryProvider.getInstance()
            setOnPreferenceChangeListener { _, newValue ->
                ProxyUsernamePreference.put(requireContext(), lifecycleScope, newValue as String)
                true
            }
            isEnabled = requireContext().dataStore.getOrDefault(ProxyModePreference) !=
                    ProxyModePreference.AUTO_MODE
            screen.addPreference(this)
        }

        proxyPasswordPreference = EditTextPreference(this).apply {
            key = "proxyPassword"
            title = getString(R.string.proxy_fragment_password)
            dialogTitle = getString(R.string.proxy_fragment_password)
            setIcon(R.drawable.ic_key_vertical_24)
            setDialogIcon(R.drawable.ic_key_vertical_24)
            text = requireContext().dataStore.getOrDefault(ProxyPasswordPreference).toString()
            summaryProvider = SummaryProvider<EditTextPreference> {
                if (it.text.isNullOrBlank()) resources.getString(R.string.not_configure)
                else resources.getString(R.string.configured)
            }
            setOnPreferenceChangeListener { _, newValue ->
                ProxyPasswordPreference.put(requireContext(), lifecycleScope, newValue as String)
                true
            }
            isEnabled = requireContext().dataStore.getOrDefault(ProxyModePreference) !=
                    ProxyModePreference.AUTO_MODE
            screen.addPreference(this)
        }
    }
}