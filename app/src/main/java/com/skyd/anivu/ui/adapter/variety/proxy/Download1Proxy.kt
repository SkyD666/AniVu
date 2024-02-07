package com.skyd.anivu.ui.adapter.variety.proxy


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.skyd.anivu.R
import com.skyd.anivu.ext.disable
import com.skyd.anivu.ext.enable
import com.skyd.anivu.model.bean.DownloadInfoBean
import com.skyd.anivu.ui.adapter.variety.Download1ViewHolder
import com.skyd.anivu.ui.adapter.variety.VarietyAdapter
import com.skyd.anivu.util.floatToPercentage


class Download1Proxy(
    private val adapter: VarietyAdapter,
    private val onPause: (DownloadInfoBean) -> Unit,
    private val onResume: (DownloadInfoBean) -> Unit,
    private val onCancel: (DownloadInfoBean) -> Unit,
) : VarietyAdapter.Proxy<DownloadInfoBean, Download1ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val holder = Download1ViewHolder(
            view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_download_1, parent, false),
        )

        holder.btnDownload1Pause.setOnClickListener {
            val data = adapter.dataList.getOrNull(holder.bindingAdapterPosition)
            if (data !is DownloadInfoBean) return@setOnClickListener

            when (data.downloadState) {
                DownloadInfoBean.DownloadState.Downloading -> onPause(data)
                DownloadInfoBean.DownloadState.Paused -> onResume(data)
                else -> Unit
            }
        }
        holder.btnDownload1Cancel.setOnClickListener {
            val data = adapter.dataList.getOrNull(holder.bindingAdapterPosition)
            if (data !is DownloadInfoBean) return@setOnClickListener

            onCancel(data)

            it.disable()
            holder.btnDownload1Pause.disable()
        }

        return holder
    }

    override fun onBindViewHolder(
        holder: Download1ViewHolder,
        data: DownloadInfoBean,
        index: Int,
        action: ((Any?) -> Unit)?
    ) {
        updateName(holder, data)
        updateProgress(holder, data)
        updateDescription(holder, data)
        updateButtonProgressStateAndDescription(holder, data)
    }

    override fun onBindViewHolder(
        holder: Download1ViewHolder,
        data: DownloadInfoBean,
        index: Int,
        action: ((Any?) -> Unit)?,
        payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, data, index, action)
            return
        }
        payloads.forEach {
            if (it is List<*>) {
                it.forEach { item ->
                    when (item) {
                        DownloadInfoBean.PAYLOAD_PROGRESS -> {
                            updateProgress(holder, data)
                        }

                        DownloadInfoBean.PAYLOAD_DESCRIPTION -> {
                            updateDescription(holder, data)
                        }

                        DownloadInfoBean.PAYLOAD_DOWNLOAD_STATE -> {
                            updateButtonProgressStateAndDescription(holder, data)
                        }

                        DownloadInfoBean.PAYLOAD_NAME -> {
                            updateName(holder, data)
                        }

                        DownloadInfoBean.PAYLOAD_DOWNLOADING_DIR_NAME -> {
                        }

                        DownloadInfoBean.PAYLOAD_SIZE -> {
                        }
                    }
                }
            }
        }
    }

    private fun updateProgress(
        holder: Download1ViewHolder,
        data: DownloadInfoBean,
    ) {
        holder.tvDownload1Progress.text = floatToPercentage(data.progress)
        when (data.downloadState) {
            DownloadInfoBean.DownloadState.Downloading,
            DownloadInfoBean.DownloadState.Paused -> {
                holder.lpDownload1.isIndeterminate = false
                holder.lpDownload1.progress = (data.progress * 100).toInt()
            }

            DownloadInfoBean.DownloadState.Init -> {
                holder.lpDownload1.isIndeterminate = true
            }

            DownloadInfoBean.DownloadState.Completed -> {
                holder.lpDownload1.isIndeterminate = false
                holder.lpDownload1.progress = 100
            }
        }
    }

    private fun updateName(
        holder: Download1ViewHolder,
        data: DownloadInfoBean,
    ) {
        holder.tvDownload1Name.text = data.name
    }

    private fun updateDescription(
        holder: Download1ViewHolder,
        data: DownloadInfoBean,
    ) {
        if (data.downloadState == DownloadInfoBean.DownloadState.Downloading) {
            holder.tvDownload1Description.text = data.description
        }
    }

    private fun updateButtonProgressStateAndDescription(
        holder: Download1ViewHolder,
        data: DownloadInfoBean,
    ) {
        when (data.downloadState) {
            DownloadInfoBean.DownloadState.Downloading -> {
                holder.btnDownload1Pause.enable()
                holder.btnDownload1Pause.setIconResource(R.drawable.ic_pause_24)
                holder.btnDownload1Cancel.enable()
                holder.tvDownload1Description.text = data.description
                holder.lpDownload1.isIndeterminate = false
            }

            DownloadInfoBean.DownloadState.Paused -> {
                holder.btnDownload1Pause.enable()
                holder.btnDownload1Pause.setIconResource(R.drawable.ic_play_arrow_24)
                holder.btnDownload1Cancel.enable()
                holder.tvDownload1Description.text =
                    holder.itemView.context.getString(R.string.download_paused)
                holder.lpDownload1.isIndeterminate = false
            }

            DownloadInfoBean.DownloadState.Init -> {
                holder.btnDownload1Pause.disable()
                holder.btnDownload1Pause.setIconResource(R.drawable.ic_play_arrow_24)
                holder.btnDownload1Cancel.enable()
                holder.tvDownload1Description.text =
                    holder.itemView.context.getString(R.string.download_initializing)
                holder.lpDownload1.isIndeterminate = true
            }

            DownloadInfoBean.DownloadState.Completed -> {
                holder.btnDownload1Pause.disable()
                holder.btnDownload1Pause.setIconResource(R.drawable.ic_play_arrow_24)
                holder.btnDownload1Cancel.enable()
                holder.tvDownload1Description.text =
                    holder.itemView.context.getString(R.string.download_completed)
                holder.lpDownload1.isIndeterminate = false
            }
        }
    }
}