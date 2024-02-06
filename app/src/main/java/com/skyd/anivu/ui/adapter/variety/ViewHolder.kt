package com.skyd.anivu.ui.adapter.variety

import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.LinearProgressIndicator
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

class Enclosure1ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val tvEnclosure1Url: TextView = view.findViewById(R.id.tv_enclosure_1_url)
    val tvEnclosure1Length: TextView = view.findViewById(R.id.tv_enclosure_1_length)
    val tvEnclosure1Type: TextView = view.findViewById(R.id.tv_enclosure_1_type)
    val btnEnclosure1Download: Button = view.findViewById(R.id.btn_enclosure_1_download)
}

class Download1ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val tvDownload1Name: TextView = view.findViewById(R.id.tv_download_1_name)
    val tvDownload1Progress: TextView = view.findViewById(R.id.tv_download_1_progress)
    val lpDownload1: LinearProgressIndicator = view.findViewById(R.id.lp_download_1)
    val btnDownload1Pause: MaterialButton = view.findViewById(R.id.btn_download_1_pause)
    val btnDownload1Cancel: Button = view.findViewById(R.id.btn_download_1_cancel)
}

class Video1ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val tvVideo1Title: TextView = view.findViewById(R.id.tv_video_1_title)
    val tvVideo1Size: TextView = view.findViewById(R.id.tv_video_1_size)
    val tvVideo1Date: TextView = view.findViewById(R.id.tv_video_1_date)
    val btnVideo1Play: MaterialButton = view.findViewById(R.id.btn_video_1_play)
}

class ParentDir1ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val tvParentDir1Title: TextView = view.findViewById(R.id.tv_parent_dir_1_title)
}
