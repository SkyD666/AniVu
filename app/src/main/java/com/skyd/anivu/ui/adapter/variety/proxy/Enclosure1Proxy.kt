package com.skyd.anivu.ui.adapter.variety.proxy


import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.skyd.anivu.R
import com.skyd.anivu.ext.copy
import com.skyd.anivu.ext.fileSize
import com.skyd.anivu.model.bean.EnclosureBean
import com.skyd.anivu.ui.adapter.variety.Enclosure1ViewHolder
import com.skyd.anivu.ui.adapter.variety.VarietyAdapter


class Enclosure1Proxy(
    private val onDownload: (EnclosureBean) -> Unit,
) : VarietyAdapter.Proxy<EnclosureBean, Enclosure1ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        Enclosure1ViewHolder(
            view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_enclosure_1, parent, false),
        )

    override fun onBindViewHolder(
        holder: Enclosure1ViewHolder,
        data: EnclosureBean,
        index: Int,
        action: ((Any?) -> Unit)?
    ) {
        val context = holder.itemView.context
        holder.tvEnclosure1Url.text = data.url
        holder.tvEnclosure1Length.text = data.length.fileSize(context)
        holder.tvEnclosure1Type.text = data.type
        holder.tvEnclosure1Url.setOnClickListener {
            data.url.copy(context)
        }
        holder.btnEnclosure1Download.setOnClickListener {
            onDownload(data)
        }
    }
}