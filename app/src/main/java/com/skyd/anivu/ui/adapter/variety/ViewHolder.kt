package com.skyd.anivu.ui.adapter.variety

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.skyd.anivu.databinding.ItemArticle1Binding
import com.skyd.anivu.databinding.ItemDownload1Binding
import com.skyd.anivu.databinding.ItemEnclosure1Binding
import com.skyd.anivu.databinding.ItemFeed1Binding
import com.skyd.anivu.databinding.ItemLicense1Binding
import com.skyd.anivu.databinding.ItemMedia1Binding
import com.skyd.anivu.databinding.ItemMore1Binding
import com.skyd.anivu.databinding.ItemOtherWorks1Binding
import com.skyd.anivu.databinding.ItemParentDir1Binding

abstract class BaseViewHolder<V : ViewBinding>(val binding: V) :
    RecyclerView.ViewHolder(binding.root)

class EmptyViewHolder(view: View) : RecyclerView.ViewHolder(view)

class Feed1ViewHolder(binding: ItemFeed1Binding) : BaseViewHolder<ItemFeed1Binding>(binding)

class Article1ViewHolder(binding: ItemArticle1Binding) :
    BaseViewHolder<ItemArticle1Binding>(binding)

class Enclosure1ViewHolder(binding: ItemEnclosure1Binding) :
    BaseViewHolder<ItemEnclosure1Binding>(binding)

class Download1ViewHolder(binding: ItemDownload1Binding) :
    BaseViewHolder<ItemDownload1Binding>(binding)

class Media1ViewHolder(binding: ItemMedia1Binding) :
    BaseViewHolder<ItemMedia1Binding>(binding)

class ParentDir1ViewHolder(binding: ItemParentDir1Binding) :
    BaseViewHolder<ItemParentDir1Binding>(binding)

class More1ViewHolder(binding: ItemMore1Binding) :
    BaseViewHolder<ItemMore1Binding>(binding)

class OtherWorks1ViewHolder(binding: ItemOtherWorks1Binding) :
    BaseViewHolder<ItemOtherWorks1Binding>(binding)

class License1ViewHolder(binding: ItemLicense1Binding) :
    BaseViewHolder<ItemLicense1Binding>(binding)
