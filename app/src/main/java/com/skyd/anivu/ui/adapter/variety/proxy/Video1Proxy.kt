package com.skyd.anivu.ui.adapter.variety.proxy


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.skyd.anivu.R
import com.skyd.anivu.ext.fileSize
import com.skyd.anivu.ext.toDateTimeString
import com.skyd.anivu.model.bean.VideoBean
import com.skyd.anivu.ui.adapter.variety.VarietyAdapter
import com.skyd.anivu.ui.adapter.variety.Video1ViewHolder


class Video1Proxy(
    private val adapter: VarietyAdapter,
    private val onPlay: (VideoBean) -> Unit,
    private val onOpenDir: (VideoBean) -> Unit,
) : VarietyAdapter.Proxy<VideoBean, Video1ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val holder = Video1ViewHolder(
            view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_video_1, parent, false),
        )
        holder.btnVideo1Play.setOnClickListener {
            val data = adapter.dataList.getOrNull(holder.bindingAdapterPosition)
            if (data !is VideoBean) return@setOnClickListener
            onPlay(data)
        }
        holder.itemView.setOnClickListener {
            val data = adapter.dataList.getOrNull(holder.bindingAdapterPosition)
            if (data !is VideoBean) return@setOnClickListener
            if (data.isDir) {
                onOpenDir(data)
            } else if (data.isMedia(parent.context)) {
                onPlay(data)
            }
        }
        return holder
    }

    override fun onBindViewHolder(
        holder: Video1ViewHolder,
        data: VideoBean,
        index: Int,
        action: ((Any?) -> Unit)?
    ) {
        val context = holder.itemView.context
        holder.tvVideo1Title.text = data.name
        holder.tvVideo1Date.text = data.date.toDateTimeString()
        holder.tvVideo1Size.text = data.size.fileSize(context)
        holder.btnVideo1Play.isVisible = data.isMedia(context)
    }
}