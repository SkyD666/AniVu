package com.skyd.anivu.ui.adapter.variety

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AdapterListUpdateCallback
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.skyd.anivu.BuildConfig
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import java.lang.reflect.ParameterizedType

class VarietyAdapter(
    private var proxyList: MutableList<Proxy<*, *>> = mutableListOf(),
    dispatcher: CoroutineDispatcher = Dispatchers.IO
) : RecyclerView.Adapter<ViewHolder>() {
    private val dataDiffer = AsyncListDiffer(AdapterListUpdateCallback(this), dispatcher)

    // 一定要保证set时传入的List是新的List，与旧的引用不能相同，里面的Item引用最好也不要相同
    var dataList: List<Any>
        set(value) {
            dataDiffer.submitList(value)
        }
        get() = dataDiffer.oldList

    var action: ((Any?) -> Unit)? = null
    var onAttachedToRecyclerView: ((recyclerView: RecyclerView) -> Unit)? = null
    var onDetachedFromRecyclerView: ((recyclerView: RecyclerView) -> Unit)? = null
    var onFailedToRecycleView: ((holder: ViewHolder) -> Boolean)? = null
    var onViewAttachedToWindow: ((holder: ViewHolder) -> Unit)? = null
    var onViewDetachedFromWindow: ((holder: ViewHolder) -> Unit)? = null
    var onViewRecycled: ((holder: ViewHolder) -> Unit)? = null

    fun <T, VH : ViewHolder> addProxy(proxy: Proxy<T, VH>) {
        proxyList.add(proxy)
    }

    fun <T, VH : ViewHolder> removeProxy(proxy: Proxy<T, VH>) {
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
        dataDiffer.cancel()
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
        if (type != -1) (proxyList[type] as Proxy<Any, ViewHolder>)
            .onBindViewHolder(holder, dataList[position], position, action)
    }

    // 布局刷新
    @Suppress("UNCHECKED_CAST")
    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        val type = getItemViewType(position)
        if (type != -1) (proxyList[type] as Proxy<Any, ViewHolder>)
            .onBindViewHolder(holder, dataList[position], position, action, payloads)
    }

    override fun getItemCount(): Int = dataList.size

    override fun getItemViewType(position: Int): Int {
        return getProxyIndex(dataList[position])
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

    // 抽象策略类
    abstract class Proxy<T, VH : ViewHolder> {
        abstract fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
        abstract fun onBindViewHolder(
            holder: VH,
            data: T,
            index: Int,
            action: ((Any?) -> Unit)? = null
        )

        open fun onBindViewHolder(
            holder: VH,
            data: T,
            index: Int,
            action: ((Any?) -> Unit)? = null,
            payloads: MutableList<Any>
        ) {
            onBindViewHolder(holder, data, index, action)
        }
    }
}
