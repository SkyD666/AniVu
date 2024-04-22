package com.skyd.anivu.ui.adapter.variety

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.skyd.anivu.databinding.ItemColorPalette1Binding
import com.skyd.anivu.databinding.ItemDownload1Binding
import com.skyd.anivu.databinding.ItemEnclosure1Binding
import com.skyd.anivu.databinding.ItemLinkEnclosure1Binding

abstract class BaseViewHolder<V : ViewBinding>(val binding: V) :
    RecyclerView.ViewHolder(binding.root)

class EmptyViewHolder(view: View) : RecyclerView.ViewHolder(view)

class Enclosure1ViewHolder(binding: ItemEnclosure1Binding) :
    BaseViewHolder<ItemEnclosure1Binding>(binding)

class LinkEnclosure1ViewHolder(binding: ItemLinkEnclosure1Binding) :
    BaseViewHolder<ItemLinkEnclosure1Binding>(binding)

class Download1ViewHolder(binding: ItemDownload1Binding) :
    BaseViewHolder<ItemDownload1Binding>(binding)

class ColorPalette1ViewHolder(binding: ItemColorPalette1Binding) :
    BaseViewHolder<ItemColorPalette1Binding>(binding)
