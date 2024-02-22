package com.skyd.anivu.ui.adapter.variety.proxy


import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.skyd.anivu.R
import com.skyd.anivu.databinding.ItemArticle1Binding
import com.skyd.anivu.ext.findMainNavController
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

class Article1Proxy : VarietyAdapter.Proxy<ArticleBean, ItemArticle1Binding, Article1ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = Article1ViewHolder(
        ItemArticle1Binding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(
        holder: Article1ViewHolder,
        data: ArticleBean,
        index: Int,
        action: ((Any?) -> Unit)?
    ) {
        holder.binding.apply {
            tvArticle1Title.text = data.title?.toHtml()
            data.description?.readable().let { description ->
                if (description.isNullOrBlank()) {
                    tvArticle1Desc.gone()
                } else {
                    tvArticle1Desc.visible()
                    tvArticle1Desc.text = description
                }
            }
            data.date?.toDateTimeString().let { dateTime ->
                if (dateTime.isNullOrBlank()) {
                    tvArticle1Date.gone()
                } else {
                    tvArticle1Date.visible()
                    tvArticle1Date.text = dateTime
                }
            }
            data.author.let { author ->
                if (author.isNullOrBlank()) {
                    tvArticle1Author.gone()
                } else {
                    tvArticle1Author.visible()
                    tvArticle1Author.text = author
                }
            }
            if (data.image.isNullOrBlank()) {
                cvArticle1Image.gone()
            } else {
                cvArticle1Image.visible()
                ivArticle1Image.loadImage(data.image)
            }
        }
        holder.itemView.setOnClickListener {
            val bundle = Bundle().apply {
                putString(ReadFragment.ARTICLE_ID_KEY, data.articleId)
            }
            it.findMainNavController().navigate(R.id.action_to_read_fragment, bundle)
        }
    }
}