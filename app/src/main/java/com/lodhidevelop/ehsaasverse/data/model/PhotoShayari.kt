package com.lodhidevelop.ehsaasverse.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class PhotoShayari(
    val id: String = "",
    @SerialName("image_url")
    val imageUrl: String = "",
    val urdu: String = "",
    val roman: String = "",
    val english: String = "",
    val category: String = "General"
)
