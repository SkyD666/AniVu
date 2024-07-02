package com.skyd.anivu.model.bean

import androidx.annotation.Keep
import com.skyd.anivu.base.BaseBean
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class FaviconBean(
    @SerialName("url")
    val url: String?,
    @SerialName("icons")
    val icons: List<FaviconItem>?,
) : BaseBean {
    @Keep
    @Serializable
    data class FaviconItem(
        @SerialName("url")
        val url: String?,
        @SerialName("width")
        val width: Int?,
        @SerialName("height")
        val height: Int?,
        @SerialName("format")
        val format: String?,
        @SerialName("bytes")
        val bytes: Long?,
        @SerialName("error")
        val error: String?,
        @SerialName("sha1sum")
        val sha1sum: String?,
    ) : BaseBean
}
