package com.skyd.anivu.ui.adapter.variety.proxy


import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.updatePadding
import com.skyd.anivu.R
import com.skyd.anivu.databinding.ItemFeed1Binding
import com.skyd.anivu.ext.dp
import com.skyd.anivu.ext.findMainNavController
import com.skyd.anivu.ext.gone
import com.skyd.anivu.ext.readable
import com.skyd.anivu.ext.toHtml
import com.skyd.anivu.ext.tryAddIcon
import com.skyd.anivu.ext.visible
import com.skyd.anivu.model.bean.FeedBean
import com.skyd.anivu.ui.adapter.variety.Feed1ViewHolder
import com.skyd.anivu.ui.adapter.variety.VarietyAdapter
import com.skyd.anivu.ui.fragment.article.ArticleFragment


class Feed1Proxy(
    private val onRemove: ((FeedBean) -> Unit)? = null,
    private val onEdit: ((FeedBean) -> Unit)? = null,
) : VarietyAdapter.Proxy<FeedBean, ItemFeed1Binding, Feed1ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        Feed1ViewHolder(
            ItemFeed1Binding.inflate(LayoutInflater.from(parent.context), parent, false),
        )

    override fun onBindViewHolder(
        holder: Feed1ViewHolder,
        data: FeedBean,
        index: Int,
        action: ((Any?) -> Unit)?
    ) {
        holder.binding.apply {
            tvFeed1Title.text = data.title?.toHtml()
            tvFeed1Desc.text = data.description?.readable()
            if (onRemove == null && onEdit == null) {
                tvFeed1Title.updatePadding(right = 16.dp)
                tvFeed1Desc.updatePadding(right = 16.dp)
                btnFeed1Options.gone()
            } else {
                tvFeed1Title.updatePadding(right = 0)
                tvFeed1Desc.updatePadding(right = 0)
                btnFeed1Options.visible()
            }
            btnFeed1Options.setOnClickListener { v ->
                val popup = PopupMenu(v.context, v)
                popup.menuInflater.inflate(R.menu.menu_feed_item, popup.menu)
                popup.setOnMenuItemClickListener { menuItem: MenuItem ->
                    when (menuItem.itemId) {
                        R.id.action_feed_item_remove -> {
                            onRemove?.invoke(data)
                            true
                        }

                        R.id.action_feed_item_edit -> {
                            onEdit?.invoke(data)
                            true
                        }

                        else -> false
                    }
                }
                popup.menu.apply {
                    findItem(R.id.action_feed_item_remove).setVisible(onRemove != null)
                    findItem(R.id.action_feed_item_edit).setVisible(onEdit != null)
                    tryAddIcon(v.context)
                }
                popup.show()
            }
        }
        holder.itemView.setOnClickListener {
            val bundle = Bundle().apply {
                putString(ArticleFragment.FEED_URL_KEY, data.url)
            }
            it.findMainNavController().navigate(R.id.action_to_article_fragment, bundle)
        }
    }
}