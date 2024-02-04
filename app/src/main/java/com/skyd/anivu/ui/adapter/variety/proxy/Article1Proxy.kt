package com.skyd.anivu.ui.adapter.variety.proxy


import android.view.LayoutInflater
import android.view.ViewGroup
import com.skyd.anivu.R
import com.skyd.anivu.ext.gone
import com.skyd.anivu.ext.readable
import com.skyd.anivu.ext.toDateTimeString
import com.skyd.anivu.ext.toHtml
import com.skyd.anivu.ext.visible
import com.skyd.anivu.model.bean.ArticleBean
import com.skyd.anivu.ui.adapter.variety.Article1ViewHolder
import com.skyd.anivu.ui.adapter.variety.VarietyAdapter
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
        holder.tvArticle1Title.text = data.title?.toHtml()
        holder.tvArticle1Desc.text = data.description?.readable()
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
//            data.route.route(activity)
        }
    }
}