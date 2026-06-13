package com.lodhidevelop.ehsaasverse.data.model

import com.google.firebase.firestore.IgnoreExtraProperties
import kotlinx.serialization.Serializable

@Serializable
@IgnoreExtraProperties
data class PhotoCategory(
    val id: String = "",
    val name: String = "",
    val image: String = "",
    val status: Boolean = true,
    val type: String = "photo",
    val timestamp: Long = System.currentTimeMillis()
)
