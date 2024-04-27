package com.skyd.anivu.ui.adapter.variety.proxy


import android.view.LayoutInflater
import android.view.ViewGroup
import com.skyd.anivu.R
import com.skyd.anivu.databinding.ItemDownload1Binding
import com.skyd.anivu.ext.disable
import com.skyd.anivu.ext.enable
import com.skyd.anivu.ext.fileSize
import com.skyd.anivu.ext.toPercentage
import com.skyd.anivu.model.bean.download.DownloadInfoBean
import com.skyd.anivu.ui.adapter.variety.Download1ViewHolder
import com.skyd.anivu.ui.adapter.variety.VarietyAdapter


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
                DownloadInfoBean.DownloadState.Seeding,
                DownloadInfoBean.DownloadState.Downloading -> onPause(data)

                DownloadInfoBean.DownloadState.SeedingPaused,
                DownloadInfoBean.DownloadState.Paused -> onResume(data)

                DownloadInfoBean.DownloadState.Completed,
                DownloadInfoBean.DownloadState.StorageMovedFailed,
                DownloadInfoBean.DownloadState.ErrorPaused -> onResume(data)

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
        holder.binding.btnDownload1Cancel.enable()
        updateName(holder, data)
        updateProgress(holder, data)
        updateDescription(holder, data)
        updateUploadPayloadRate(holder, data)
        updateDownloadPayloadRate(holder, data)
        updateSize(holder, data)
        updatePeerInfo(holder, data)
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

                        DownloadInfoBean.PAYLOAD_UPLOAD_PAYLOAD_RATE -> {
                            updateUploadPayloadRate(holder, data)
                        }

                        DownloadInfoBean.PAYLOAD_DOWNLOAD_PAYLOAD_RATE -> {
                            updateDownloadPayloadRate(holder, data)
                        }

                        DownloadInfoBean.PAYLOAD_SIZE -> {
                            updateSize(holder, data)
                        }

                        DownloadInfoBean.PAYLOAD_PEER_INFO -> {
                            updatePeerInfo(holder, data)
                        }

                        DownloadInfoBean.PAYLOAD_DOWNLOAD_STATE -> {
                            updateButtonProgressStateAndDescription(holder, data)
                        }

                        DownloadInfoBean.PAYLOAD_NAME -> {
                            updateName(holder, data)
                        }

                        DownloadInfoBean.PAYLOAD_DOWNLOADING_DIR_NAME -> {
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
            tvDownload1Progress.text = data.progress.toPercentage()
            when (data.downloadState) {
                DownloadInfoBean.DownloadState.Seeding,
                DownloadInfoBean.DownloadState.SeedingPaused,
                DownloadInfoBean.DownloadState.Downloading,
                DownloadInfoBean.DownloadState.StorageMovedFailed,
                DownloadInfoBean.DownloadState.ErrorPaused,
                DownloadInfoBean.DownloadState.Paused -> {
                    lpDownload1.isIndeterminate = false
                    lpDownload1.setProgress((data.progress * 100).toInt(), true)
                }

                DownloadInfoBean.DownloadState.Init -> {
                    lpDownload1.isIndeterminate = true
                }

                DownloadInfoBean.DownloadState.Completed -> {
                    lpDownload1.isIndeterminate = false
                    lpDownload1.setProgress(100, true)
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

    private fun updateUploadPayloadRate(
        holder: Download1ViewHolder,
        data: DownloadInfoBean,
    ) {
        val context = holder.itemView.context
        holder.binding.tvDownload1UploadPayloadRate.text = context.getString(
            R.string.download_upload_payload_rate,
            data.uploadPayloadRate.toLong().fileSize(context) + "/s"
        )
    }

    private fun updateDownloadPayloadRate(
        holder: Download1ViewHolder,
        data: DownloadInfoBean,
    ) {
        val context = holder.itemView.context
        holder.binding.tvDownload1DownloadPayloadRate.text = context.getString(
            R.string.download_download_payload_rate,
            data.downloadPayloadRate.toLong().fileSize(context) + "/s"
        )
    }

    private fun updateSize(
        holder: Download1ViewHolder,
        data: DownloadInfoBean,
    ) {
//        if (data.size != 0L) {
//            holder.binding.tvDownload1Size.text = data.size.fileSize(holder.itemView.context)
//        } else {
//            holder.binding.tvDownload1Size.gone()
//        }
    }

    private fun updatePeerInfo(
        holder: Download1ViewHolder,
        data: DownloadInfoBean,
    ) {
        holder.binding.tvDownload1PeerCount.text = holder.itemView.context.getString(
            R.string.download_peer_count,
            data.peerInfoList.count()
        )
    }

    private fun updateButtonProgressStateAndDescription(
        holder: Download1ViewHolder,
        data: DownloadInfoBean,
    ) {
        holder.binding.apply {
            val context = holder.itemView.context
            when (data.downloadState) {
                DownloadInfoBean.DownloadState.Seeding -> {
                    btnDownload1Pause.enable()
                    btnDownload1Pause.setIconResource(R.drawable.ic_pause_24)
                    btnDownload1Pause.contentDescription =
                        context.getString(R.string.download_pause)
                    tvDownload1Description.text = data.description
                    lpDownload1.isIndeterminate = false
                }

                DownloadInfoBean.DownloadState.Downloading -> {
                    btnDownload1Pause.enable()
                    btnDownload1Pause.setIconResource(R.drawable.ic_pause_24)
                    btnDownload1Pause.contentDescription =
                        context.getString(R.string.download_pause)
                    tvDownload1Description.text = data.description
                    lpDownload1.isIndeterminate = false
                }

                DownloadInfoBean.DownloadState.StorageMovedFailed,
                DownloadInfoBean.DownloadState.ErrorPaused -> {
                    btnDownload1Pause.enable()
                    btnDownload1Pause.setIconResource(R.drawable.ic_refresh_24)
                    btnDownload1Pause.contentDescription =
                        context.getString(R.string.download_retry)
                    tvDownload1Description.text = context.getString(R.string.download_error_paused)
                    lpDownload1.isIndeterminate = false
                }

                DownloadInfoBean.DownloadState.SeedingPaused -> {
                    btnDownload1Pause.enable()
                    btnDownload1Pause.setIconResource(R.drawable.ic_cloud_upload_24)
                    btnDownload1Pause.contentDescription =
                        context.getString(R.string.download_click_to_seeding)
                    tvDownload1Description.text = context.getString(R.string.download_paused)
                    lpDownload1.isIndeterminate = false
                }

                DownloadInfoBean.DownloadState.Paused -> {
                    btnDownload1Pause.enable()
                    btnDownload1Pause.setIconResource(R.drawable.ic_play_arrow_24)
                    btnDownload1Pause.contentDescription = context.getString(R.string.download)
                    tvDownload1Description.text = context.getString(R.string.download_paused)
                    lpDownload1.isIndeterminate = false
                }

                DownloadInfoBean.DownloadState.Init -> {
                    btnDownload1Pause.disable()
                    btnDownload1Pause.setIconResource(R.drawable.ic_play_arrow_24)
                    btnDownload1Pause.contentDescription = context.getString(R.string.download)
                    tvDownload1Description.text = context.getString(R.string.download_initializing)
                    lpDownload1.isIndeterminate = true
                }

                DownloadInfoBean.DownloadState.Completed -> {
                    btnDownload1Pause.enable()
                    btnDownload1Pause.setIconResource(R.drawable.ic_cloud_upload_24)
                    btnDownload1Pause.contentDescription =
                        context.getString(R.string.download_click_to_seeding)
                    tvDownload1Description.text = context.getString(R.string.download_completed)
                    lpDownload1.isIndeterminate = false
                }
            }
        }
    }
}