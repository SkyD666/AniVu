package com.skyd.anivu.ui.adapter.variety.proxy


import android.view.LayoutInflater
import android.view.ViewGroup
import com.skyd.anivu.R
import com.skyd.anivu.databinding.ItemDownload1Binding
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
) : VarietyAdapter.Proxy<DownloadInfoBean, ItemDownload1Binding, Download1ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Download1ViewHolder {
        val holder = Download1ViewHolder(
            ItemDownload1Binding
                .inflate(LayoutInflater.from(parent.context), parent, false),
        )

        holder.binding.btnDownload1Pause.setOnClickListener {
            val data = adapter.dataList.getOrNull(holder.bindingAdapterPosition)
            if (data !is DownloadInfoBean) return@setOnClickListener

            when (data.downloadState) {
                DownloadInfoBean.DownloadState.Downloading -> onPause(data)
                DownloadInfoBean.DownloadState.Paused -> onResume(data)
                else -> Unit
            }
        }
        holder.binding.btnDownload1Cancel.setOnClickListener {
            val data = adapter.dataList.getOrNull(holder.bindingAdapterPosition)
            if (data !is DownloadInfoBean) return@setOnClickListener

            onCancel(data)

            it.disable()
            holder.binding.btnDownload1Pause.disable()
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
        holder.binding.apply {
            tvDownload1Progress.text = floatToPercentage(data.progress)
            when (data.downloadState) {
                DownloadInfoBean.DownloadState.Downloading,
                DownloadInfoBean.DownloadState.Paused -> {
                    lpDownload1.isIndeterminate = false
                    lpDownload1.progress = (data.progress * 100).toInt()
                }

                DownloadInfoBean.DownloadState.Init -> {
                    lpDownload1.isIndeterminate = true
                }

                DownloadInfoBean.DownloadState.Completed -> {
                    lpDownload1.isIndeterminate = false
                    lpDownload1.progress = 100
                }
            }
        }
    }

    private fun updateName(
        holder: Download1ViewHolder,
        data: DownloadInfoBean,
    ) {
        holder.binding.tvDownload1Name.text = data.name
    }

    private fun updateDescription(
        holder: Download1ViewHolder,
        data: DownloadInfoBean,
    ) {
        if (data.downloadState == DownloadInfoBean.DownloadState.Downloading) {
            holder.binding.tvDownload1Description.text = data.description
        }
    }

    private fun updateButtonProgressStateAndDescription(
        holder: Download1ViewHolder,
        data: DownloadInfoBean,
    ) {
        holder.binding.apply {
            when (data.downloadState) {
                DownloadInfoBean.DownloadState.Downloading -> {
                    btnDownload1Pause.enable()
                    btnDownload1Pause.setIconResource(R.drawable.ic_pause_24)
                    btnDownload1Cancel.enable()
                    tvDownload1Description.text = data.description
                    lpDownload1.isIndeterminate = false
                }

                DownloadInfoBean.DownloadState.Paused -> {
                    btnDownload1Pause.enable()
                    btnDownload1Pause.setIconResource(R.drawable.ic_play_arrow_24)
                    btnDownload1Cancel.enable()
                    tvDownload1Description.text =
                        holder.itemView.context.getString(R.string.download_paused)
                    lpDownload1.isIndeterminate = false
                }

                DownloadInfoBean.DownloadState.Init -> {
                    btnDownload1Pause.disable()
                    btnDownload1Pause.setIconResource(R.drawable.ic_play_arrow_24)
                    btnDownload1Cancel.enable()
                    tvDownload1Description.text =
                        holder.itemView.context.getString(R.string.download_initializing)
                    lpDownload1.isIndeterminate = true
                }

                DownloadInfoBean.DownloadState.Completed -> {
                    btnDownload1Pause.disable()
                    btnDownload1Pause.setIconResource(R.drawable.ic_play_arrow_24)
                    btnDownload1Cancel.enable()
                    tvDownload1Description.text =
                        holder.itemView.context.getString(R.string.download_completed)
                    lpDownload1.isIndeterminate = false
                }
            }
        }
    }
}