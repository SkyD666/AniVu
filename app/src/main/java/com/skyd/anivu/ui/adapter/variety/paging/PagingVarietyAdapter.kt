package com.skyd.anivu.ui.adapter.variety.paging

import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingData
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.viewbinding.ViewBinding
import com.skyd.anivu.BuildConfig
import com.skyd.anivu.ui.adapter.variety.BaseViewHolder
import com.skyd.anivu.ui.adapter.variety.EmptyViewHolder
import com.skyd.anivu.ui.adapter.variety.VarietyAdapter.Proxy
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.lang.reflect.ParameterizedType

class PagingVarietyAdapter(
    private var proxyList: MutableList<Proxy<*, *, *>> = mutableListOf(),
    dispatcher: CoroutineDispatcher = Dispatchers.IO
) : PagingDataAdapter<Any, ViewHolder>(PagingDiffCallback()) {
    private val coroutineScope = CoroutineScope(dispatcher)

    var action: ((Any?) -> Unit)? = null
    var onAttachedToRecyclerView: ((recyclerView: RecyclerView) -> Unit)? = null
    var onDetachedFromRecyclerView: ((recyclerView: RecyclerView) -> Unit)? = null
    var onFailedToRecycleView: ((holder: ViewHolder) -> Boolean)? = null
    var onViewAttachedToWindow: ((holder: ViewHolder) -> Unit)? = null
    var onViewDetachedFromWindow: ((holder: ViewHolder) -> Unit)? = null
    var onViewRecycled: ((holder: ViewHolder) -> Unit)? = null

    fun submitDataAsync(pagingData: PagingData<out Any>) {
        coroutineScope.launch {
            @Suppress("UNCHECKED_CAST")
            submitData(pagingData as PagingData<Any>)
        }
    }

    fun clear() {
        submitDataAsync(PagingData.empty())
    }

    fun getItemByIndex(index: Int): Any? = getItem(index)

    fun <T, VB : ViewBinding, VH : BaseViewHolder<VB>> addProxy(proxy: Proxy<T, VB, VH>) {
        proxyList.add(proxy)
    }

    fun <T, VB : ViewBinding, VH : BaseViewHolder<VB>> removeProxy(proxy: Proxy<T, VB, VH>) {
        proxyList.remove(proxy)
    }

    override fun onViewAttachedToWindow(holder: ViewHolder) {
        onViewAttachedToWindow?.invoke(holder)
    }

    override fun onViewDetachedFromWindow(holder: ViewHolder) {
        onViewDetachedFromWindow?.invoke(holder)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        onAttachedToRecyclerView?.invoke(recyclerView)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        coroutineScope.cancel()
        onDetachedFromRecyclerView?.invoke(recyclerView)
    }

    override fun onViewRecycled(holder: ViewHolder) {
        onViewRecycled?.invoke(holder)
    }

    override fun onFailedToRecycleView(holder: ViewHolder): Boolean {
        return onFailedToRecycleView?.invoke(holder) ?: super.onFailedToRecycleView(holder)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // debug模式下让他崩溃，以便检查错误出处
        if (viewType == -1 && !BuildConfig.DEBUG) return EmptyViewHolder(View(parent.context))
        return proxyList[viewType].onCreateViewHolder(parent, viewType)
    }

    @Suppress("UNCHECKED_CAST")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val type = getItemViewType(position)
        if (type != -1) {
            (proxyList[type] as Proxy<Any, ViewBinding, BaseViewHolder<ViewBinding>>)
                .onBindViewHolder(
                    holder as BaseViewHolder<ViewBinding>,
                    getItem(position)!!,
                    position,
                    action,
                )
        }
    }

    // 布局刷新
    @Suppress("UNCHECKED_CAST")
    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        val type = getItemViewType(position)
        if (type != -1) {
            (proxyList[type] as Proxy<Any, ViewBinding, BaseViewHolder<ViewBinding>>)
                .onBindViewHolder(
                    holder as BaseViewHolder<ViewBinding>,
                    getItem(position)!!,
                    position,
                    action,
                    payloads
                )
        }
    }

    override fun getItemViewType(position: Int): Int {
        return getProxyIndex(getItem(position)!!)
    }

    // 获取策略在列表中的索引，可能返回-1
    private fun getProxyIndex(data: Any): Int = proxyList.indexOfFirst {
        // 如果Proxy<T,VH>中的第一个类型参数T和数据的类型相同，则返回对应策略的索引
        (it.javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0].let { argument ->
            if (argument.toString() == data.javaClass.toString())
                true    // 正常情况
            else {
                // Proxy第一个泛型是类似List<T>，又嵌套了个泛型
                if (argument is ParameterizedType)
                    argument.rawType.toString() == data.javaClass.toString()
                else false
            }
        }
    }
}
