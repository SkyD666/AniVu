package com.skyd.anivu.ui.component.lazyverticalgrid.adapter

import androidx.compose.runtime.Composable
import java.lang.reflect.ParameterizedType

class LazyGridAdapter(
    private var proxyList: MutableList<Proxy<*>> = mutableListOf(),
) {
    @Suppress("UNCHECKED_CAST")
    @Composable
    fun Draw(index: Int, data: Any) {
        val type: Int = getProxyIndex(data)
        if (type != -1) (proxyList[type] as Proxy<Any>).Draw(index, data)
    }

    // 获取策略在列表中的索引，可能返回-1
    private fun getProxyIndex(data: Any): Int = proxyList.indexOfFirst {
        // 如果Proxy<T>中的第一个类型参数T和数据的类型相同，则返回对应策略的索引
        (it.javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0].let { argument ->
            if (argument.toString() == data.javaClass.toString())
                true    // 正常情况
            else if (((argument as? ParameterizedType)?.rawType as? Class<*>)
                    ?.isAssignableFrom(data.javaClass) == true
            ) {
                true    // data是T的子类的情况
            } else {
                // Proxy第一个泛型是类似List<T>，又嵌套了个泛型
                if (argument is ParameterizedType)
                    argument.rawType.toString() == data.javaClass.toString()
                else false
            }
        }
    }

    // 抽象策略类
    abstract class Proxy<T> {
        @Composable
        abstract fun Draw(index: Int, data: T)
    }
}
