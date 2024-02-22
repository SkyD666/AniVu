package com.skyd.anivu.ui.adapter.variety.proxy


import android.media.MediaMetadataRetriever
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import com.skyd.anivu.R
import com.skyd.anivu.databinding.ItemMedia1Binding
import com.skyd.anivu.ext.fileSize
import com.skyd.anivu.ext.gone
import com.skyd.anivu.ext.openWith
import com.skyd.anivu.ext.toDateTimeString
import com.skyd.anivu.ext.toUri
import com.skyd.anivu.ext.tryAddIcon
import com.skyd.anivu.ext.visible
import com.skyd.anivu.model.bean.VideoBean
import com.skyd.anivu.ui.adapter.variety.Media1ViewHolder
import com.skyd.anivu.ui.adapter.variety.VarietyAdapter
import com.skyd.anivu.util.CoilUtil.loadImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random


class Media1Proxy(
    private val adapter: VarietyAdapter,
    private val onPlay: (VideoBean) -> Unit,
    private val onOpenDir: (VideoBean) -> Unit,
    private val onRemove: (VideoBean) -> Unit,
    private val coroutineScope: CoroutineScope,
) : VarietyAdapter.Proxy<VideoBean, ItemMedia1Binding, Media1ViewHolder>() {
    private val retriever: MediaMetadataRetriever by lazy { MediaMetadataRetriever() }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Media1ViewHolder {
        val holder = Media1ViewHolder(
            ItemMedia1Binding.inflate(LayoutInflater.from(parent.context), parent, false),
        )
        holder.binding.apply {
            btnMedia1Options.setOnClickListener { v ->
                val popup = PopupMenu(v.context, v)
                popup.menuInflater.inflate(R.menu.menu_media_item, popup.menu)

                popup.setOnMenuItemClickListener { menuItem: MenuItem ->
                    val data = adapter.dataList.getOrNull(holder.bindingAdapterPosition)
                    if (data !is VideoBean) return@setOnMenuItemClickListener false
                    when (menuItem.itemId) {
                        R.id.action_media_item_remove -> {
                            onRemove(data)
                            true
                        }

                        else -> false
                    }
                }
                popup.menu.tryAddIcon(v.context)
                popup.show()
            }
        }
        holder.itemView.setOnClickListener {
            val data = adapter.dataList.getOrNull(holder.bindingAdapterPosition)
            if (data !is VideoBean) return@setOnClickListener
            if (data.isDir) {
                onOpenDir(data)
            } else if (data.isMedia(parent.context)) {
                onPlay(data)
            } else {
                data.file.toUri(it.context).openWith(it.context)
            }
        }
        return holder
    }

    override fun onBindViewHolder(
        holder: Media1ViewHolder,
        data: VideoBean,
        index: Int,
        action: ((Any?) -> Unit)?
    ) {
        val context = holder.itemView.context
        holder.binding.apply {
            tvMedia1Title.text = data.name
            tvMedia1Date.text = data.date.toDateTimeString()
            tvMedia1Size.text = data.size.fileSize(context)
            ivMedia1IsVideo.isVisible = data.isMedia(context)
            if (data.isMedia(context)) {
                cvMedia1Preview.visible()
                ivMedia1Preview.setTag(R.id.image_view_tag_2, data.file.path)
                coroutineScope.launch(Dispatchers.IO) {
                    runCatching { retriever.setDataSource(data.file.path) }
                        .onFailure { return@launch }
                    val duration = retriever
                        .extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                        ?.toLongOrNull() ?: return@launch
                    val bitmap = retriever.getFrameAtTime(
                        Random.nextLong(duration) * 1000,
                        MediaMetadataRetriever.OPTION_CLOSEST_SYNC,
                    ) ?: return@launch
                    withContext(Dispatchers.Main) {
                        if (ivMedia1Preview.getTag(R.id.image_view_tag_2) == data.file.path) {
                            ivMedia1Preview.loadImage(bitmap)
                        }
                    }
                }
            } else {
                cvMedia1Preview.gone()
            }
        }
    }
}