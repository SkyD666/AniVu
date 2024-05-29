package com.skyd.anivu.ui.adapter.variety.proxy


import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import com.skyd.anivu.databinding.ItemEnclosure1Binding
import com.skyd.anivu.ext.activity
import com.skyd.anivu.ext.copy
import com.skyd.anivu.ext.fileSize
import com.skyd.anivu.ext.gone
import com.skyd.anivu.ext.visible
import com.skyd.anivu.model.bean.EnclosureBean
import com.skyd.anivu.model.worker.download.doIfMagnetOrTorrentLink
import com.skyd.anivu.ui.activity.PlayActivity
import com.skyd.anivu.ui.adapter.variety.Enclosure1ViewHolder
import com.skyd.anivu.ui.adapter.variety.VarietyAdapter


class Enclosure1Proxy(
    private val onDownload: (EnclosureBean) -> Unit,
) : VarietyAdapter.Proxy<EnclosureBean, ItemEnclosure1Binding, Enclosure1ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Enclosure1ViewHolder =
        Enclosure1ViewHolder(
            ItemEnclosure1Binding
                .inflate(LayoutInflater.from(parent.context), parent, false),
        )

    override fun onBindViewHolder(
        holder: Enclosure1ViewHolder,
        data: EnclosureBean,
        index: Int,
        action: ((Any?) -> Unit)?
    ) {
        val context = holder.itemView.context
        holder.binding.apply {
            tvEnclosure1Url.text = data.url
            tvEnclosure1Length.text = data.length.fileSize(context)
            tvEnclosure1Type.text = data.type
            tvEnclosure1Url.setOnClickListener {
                data.url.copy(context)
            }
            if (data.isMedia) {
                btnEnclosure1Play.visible()
                btnEnclosure1Play.setOnClickListener {
                    try {
                        PlayActivity.play(context.activity, Uri.parse(data.url))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            } else {
                btnEnclosure1Play.gone()
            }
            doIfMagnetOrTorrentLink(
                link = data.url,
                onSupported = {
                    btnEnclosure1Download.visible()
                    btnEnclosure1Download.setOnClickListener {
                        onDownload(data)
                    }
                },
                onUnsupported = { btnEnclosure1Download.gone() }
            )
        }
    }
}