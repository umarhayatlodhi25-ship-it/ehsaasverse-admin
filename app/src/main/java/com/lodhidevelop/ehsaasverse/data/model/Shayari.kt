package com.lodhidevelop.ehsaasverse.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Shayari(
    val id: Int = 0,
    val urdu: String = "",
    val roman: String = "",
    val english: String = "",
    val category: String = "",
    val poet: String = "",
    val docId: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
