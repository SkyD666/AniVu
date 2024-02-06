package com.skyd.anivu.ui.adapter.variety.proxy


import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.skyd.anivu.R
import com.skyd.anivu.ext.activity
import com.skyd.anivu.ext.gone
import com.skyd.anivu.ext.readable
import com.skyd.anivu.ext.toDateTimeString
import com.skyd.anivu.ext.toHtml
import com.skyd.anivu.ext.visible
import com.skyd.anivu.model.bean.ArticleBean
import com.skyd.anivu.ui.adapter.variety.Article1ViewHolder
import com.skyd.anivu.ui.adapter.variety.VarietyAdapter
import com.skyd.anivu.ui.fragment.read.ReadFragment
import com.skyd.anivu.util.CoilUtil.loadImage

class Article1Proxy : VarietyAdapter.Proxy<ArticleBean, Article1ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = Article1ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_article_1, parent, false)
    )

    override fun onBindViewHolder(
        holder: Article1ViewHolder,
        data: ArticleBean,
        index: Int,
        action: ((Any?) -> Unit)?
    ) {
        val activity = holder.itemView.activity
        holder.tvArticle1Title.text = data.title?.toHtml()
        data.description?.readable().let { description ->
            if (description.isNullOrBlank()) {
                holder.tvArticle1Desc.gone()
            } else {
                holder.tvArticle1Desc.visible()
                holder.tvArticle1Desc.text = description
            }
        }
        data.date?.toDateTimeString().let { dateTime ->
            if (!dateTime.isNullOrBlank()) {
                holder.tvArticle1Date.text = dateTime
            }
        }
        if (data.image.isNullOrBlank()) {
            holder.ivArticle1Image.gone()
        } else {
            holder.ivArticle1Image.visible()
            holder.ivArticle1Image.loadImage(data.image)
        }
        holder.itemView.setOnClickListener {
            val bundle = Bundle().apply {
                putString(ReadFragment.ARTICLE_ID_KEY, data.articleId)
            }
            Navigation.findNavController(activity, R.id.nav_host_fragment_main)
                .navigate(R.id.action_to_read_fragment, bundle)
        }
    }
}