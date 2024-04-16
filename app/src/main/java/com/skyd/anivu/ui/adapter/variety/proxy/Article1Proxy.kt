package com.skyd.anivu.ui.adapter.variety.proxy


import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.FragmentActivity
import com.skyd.anivu.R
import com.skyd.anivu.databinding.ItemArticle1Binding
import com.skyd.anivu.ext.findMainNavController
import com.skyd.anivu.ext.gone
import com.skyd.anivu.ext.readable
import com.skyd.anivu.ext.toDateTimeString
import com.skyd.anivu.ext.toHtml
import com.skyd.anivu.ext.tryActivity
import com.skyd.anivu.ext.tryAddIcon
import com.skyd.anivu.ext.visible
import com.skyd.anivu.model.bean.ArticleWithEnclosureBean
import com.skyd.anivu.ui.adapter.variety.Article1ViewHolder
import com.skyd.anivu.ui.adapter.variety.VarietyAdapter
import com.skyd.anivu.ui.fragment.read.EnclosureBottomSheet
import com.skyd.anivu.ui.fragment.read.ReadFragment
import com.skyd.anivu.util.CoilUtil.loadImage

class Article1Proxy :
    VarietyAdapter.Proxy<ArticleWithEnclosureBean, ItemArticle1Binding, Article1ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = Article1ViewHolder(
        ItemArticle1Binding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(
        holder: Article1ViewHolder,
        data: ArticleWithEnclosureBean,
        index: Int,
        action: ((Any?) -> Unit)?
    ) {
        val activity = holder.itemView.tryActivity
        val article = data.article
        holder.binding.apply {
            tvArticle1Title.text = article.title?.toHtml()
            article.description?.readable().let { description ->
                if (description.isNullOrBlank()) {
                    tvArticle1Desc.gone()
                } else {
                    tvArticle1Desc.visible()
                    tvArticle1Desc.text = description
                }
            }
            article.date?.toDateTimeString().let { dateTime ->
                if (dateTime.isNullOrBlank()) {
                    tvArticle1Date.gone()
                } else {
                    tvArticle1Date.visible()
                    tvArticle1Date.text = dateTime
                }
            }
            article.author.let { author ->
                if (author.isNullOrBlank()) {
                    tvArticle1Author.gone()
                } else {
                    tvArticle1Author.visible()
                    tvArticle1Author.text = author
                }
            }
            if (article.image.isNullOrBlank()) {
                cvArticle1Image.gone()
            } else {
                cvArticle1Image.visible()
                ivArticle1Image.loadImage(article.image)
            }

            btnArticle1Options.setOnClickListener { v ->
                val popup = PopupMenu(v.context, v)
                popup.menuInflater.inflate(R.menu.menu_article_item, popup.menu)
                popup.setOnMenuItemClickListener { menuItem: MenuItem ->
                    when (menuItem.itemId) {
                        R.id.action_article_item_enclosures -> {
                            EnclosureBottomSheet().apply {
                                show(
                                    (activity as FragmentActivity).supportFragmentManager,
                                    EnclosureBottomSheet.TAG
                                )
                                updateData(
                                    ReadFragment.getEnclosuresList(holder.itemView.context, data)
                                )
                            }
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
            val bundle = Bundle().apply {
                putString(ReadFragment.ARTICLE_ID_KEY, article.articleId)
            }
            it.findMainNavController().navigate(R.id.action_to_read_fragment, bundle)
        }
    }
}