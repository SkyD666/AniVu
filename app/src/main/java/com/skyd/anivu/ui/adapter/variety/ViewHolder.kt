package com.skyd.anivu.ui.adapter.variety

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.skyd.anivu.R

//UP_TODO 2022/1/22 12:31 0 ViewHolder直接使用ViewBinding
class EmptyViewHolder(view: View) : RecyclerView.ViewHolder(view)

class Feed1ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val tvFeed1Title: TextView = view.findViewById(R.id.tv_feed_1_title)
    val tvFeed1Desc: TextView = view.findViewById(R.id.tv_feed_1_desc)
}

class Article1ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val tvArticle1Title: TextView = view.findViewById(R.id.tv_article_1_title)
    val tvArticle1Desc: TextView = view.findViewById(R.id.tv_article_1_desc)
    val tvArticle1Date: TextView = view.findViewById(R.id.tv_article_1_date)
    val ivArticle1Image: ImageView = view.findViewById(R.id.iv_article_1_image)
}
