package com.skyd.anivu.ui.adapter.variety.proxy


import android.view.LayoutInflater
import android.view.ViewGroup
import com.skyd.anivu.databinding.ItemOtherWorks1Binding
import com.skyd.anivu.ext.openBrowser
import com.skyd.anivu.model.bean.OtherWorksBean
import com.skyd.anivu.ui.adapter.variety.OtherWorks1ViewHolder
import com.skyd.anivu.ui.adapter.variety.VarietyAdapter


class OtherWorks1Proxy(
    private val adapter: VarietyAdapter,
) :
    VarietyAdapter.Proxy<OtherWorksBean, ItemOtherWorks1Binding, OtherWorks1ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OtherWorks1ViewHolder {
        val holder = OtherWorks1ViewHolder(
            ItemOtherWorks1Binding
                .inflate(LayoutInflater.from(parent.context), parent, false),
        )
        holder.itemView.setOnClickListener {
            val data = adapter.dataList.getOrNull(holder.bindingAdapterPosition)
            if (data !is OtherWorksBean) return@setOnClickListener
            data.url.openBrowser(it.context)
        }
        return holder
    }

    override fun onBindViewHolder(
        holder: OtherWorks1ViewHolder,
        data: OtherWorksBean,
        index: Int,
        action: ((Any?) -> Unit)?
    ) {
        holder.binding.apply {
            ivOtherWorks1Icon.setImageDrawable(data.icon)
            tvOtherWorks1Title.text = data.name
            tvOtherWorks1Description.text = data.description
        }
    }
}