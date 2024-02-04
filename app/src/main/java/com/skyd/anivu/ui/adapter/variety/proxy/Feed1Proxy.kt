package com.skyd.anivu.ui.adapter.variety.proxy


import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.ViewGroup
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.skyd.anivu.R
import com.skyd.anivu.ext.activity
import com.skyd.anivu.ext.readable
import com.skyd.anivu.ext.toHtml
import com.skyd.anivu.model.bean.FeedBean
import com.skyd.anivu.ui.adapter.variety.Feed1ViewHolder
import com.skyd.anivu.ui.adapter.variety.VarietyAdapter
import com.skyd.anivu.ui.fragment.article.ArticleFragment


class Feed1Proxy(
    private val onRemove: (FeedBean) -> Unit,
) : VarietyAdapter.Proxy<FeedBean, Feed1ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        Feed1ViewHolder(
            view = LayoutInflater.from(parent.context).inflate(R.layout.item_feed_1, parent, false),
        )

    override fun onBindViewHolder(
        holder: Feed1ViewHolder,
        data: FeedBean,
        index: Int,
        action: ((Any?) -> Unit)?
    ) {
        val activity = holder.itemView.activity
        holder.tvFeed1Title.text = data.title?.toHtml()
        holder.tvFeed1Desc.text = data.description?.readable()
        holder.itemView.setOnCreateContextMenuListener { menu, _, _ ->
            MenuInflater(holder.itemView.context).inflate(R.menu.menu_feed_item, menu)
            menu?.findItem(R.id.action_feed_item_remove)?.apply {
                setOnMenuItemClickListener {
                    onRemove(data)
                    true
                }
            }
        };
        holder.itemView.setOnClickListener {
            val bundle = Bundle().apply {
                putString(ArticleFragment.FEED_URL_KEY, data.url)
            }
            findNavController(activity, R.id.nav_host_fragment_main)
                .navigate(R.id.action_to_article_fragment, bundle)
        }
    }
}