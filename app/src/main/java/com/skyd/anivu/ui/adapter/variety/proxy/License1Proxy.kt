package com.skyd.anivu.ui.adapter.variety.proxy


import android.view.LayoutInflater
import android.view.ViewGroup
import com.skyd.anivu.databinding.ItemLicense1Binding
import com.skyd.anivu.ext.openBrowser
import com.skyd.anivu.model.bean.LicenseBean
import com.skyd.anivu.ui.adapter.variety.License1ViewHolder
import com.skyd.anivu.ui.adapter.variety.VarietyAdapter


class License1Proxy(
    private val adapter: VarietyAdapter,
) :
    VarietyAdapter.Proxy<LicenseBean, ItemLicense1Binding, License1ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): License1ViewHolder {
        val holder = License1ViewHolder(
            ItemLicense1Binding
                .inflate(LayoutInflater.from(parent.context), parent, false),
        )
        holder.itemView.setOnClickListener {
            val data = adapter.dataList.getOrNull(holder.bindingAdapterPosition)
            if (data !is LicenseBean) return@setOnClickListener
            it.context.openBrowser(data.link)
        }
        return holder
    }

    override fun onBindViewHolder(
        holder: License1ViewHolder,
        data: LicenseBean,
        index: Int,
        action: ((Any?) -> Unit)?
    ) {
        holder.binding.apply {
            tvLicense1Title.text = data.name
            tvLicense1License.text = data.license
            tvLicense1Link.text = data.link
        }
    }
}