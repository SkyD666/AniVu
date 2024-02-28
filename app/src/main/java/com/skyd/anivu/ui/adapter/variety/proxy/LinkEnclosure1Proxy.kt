package com.skyd.anivu.ui.adapter.variety.proxy


import android.view.LayoutInflater
import android.view.ViewGroup
import com.skyd.anivu.databinding.ItemLinkEnclosure1Binding
import com.skyd.anivu.ext.copy
import com.skyd.anivu.model.bean.LinkEnclosureBean
import com.skyd.anivu.ui.adapter.variety.LinkEnclosure1ViewHolder
import com.skyd.anivu.ui.adapter.variety.VarietyAdapter


class LinkEnclosure1Proxy(
    private val onDownload: (LinkEnclosureBean) -> Unit,
) : VarietyAdapter.Proxy<LinkEnclosureBean, ItemLinkEnclosure1Binding, LinkEnclosure1ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        LinkEnclosure1ViewHolder(
            ItemLinkEnclosure1Binding
                .inflate(LayoutInflater.from(parent.context), parent, false),
        )

    override fun onBindViewHolder(
        holder: LinkEnclosure1ViewHolder,
        data: LinkEnclosureBean,
        index: Int,
        action: ((Any?) -> Unit)?
    ) {
        val context = holder.itemView.context
        holder.binding.apply {
            tvEnclosure1Url.text = data.link
            tvEnclosure1Url.setOnClickListener {
                data.link.copy(context)
            }
            btnEnclosure1Download.setOnClickListener {
                onDownload(data)
            }
        }
    }
}