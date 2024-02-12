package com.skyd.anivu.model.bean

import com.skyd.anivu.base.BaseBean
import kotlinx.serialization.Serializable

@Serializable
class LicenseBean(
    val name: String,
    val license: String,
    val link: String
) : BaseBean