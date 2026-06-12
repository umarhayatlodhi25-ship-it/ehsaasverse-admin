package com.lodhidevelop.ehsaasverse.ui.navigation

import kotlinx.serialization.Serializable
import androidx.navigation3.runtime.NavKey as BaseNavKey

sealed interface NavKey : BaseNavKey {
    @Serializable
    data object Home : NavKey
    
    @Serializable
    data object Categories : NavKey

    @Serializable
    data object PhotoShayari : NavKey

    @Serializable
    data object AIMuse : NavKey
    
    @Serializable
    data object Saved : NavKey

    @Serializable
    data object About : NavKey

    @Serializable
    data object Privacy : NavKey

    @Serializable
    data object Login : NavKey

    @Serializable
    data object Signup : NavKey

    @Serializable
    data object Admin : NavKey

    @Serializable
    data class AddShayari(val initialCategory: String? = null) : NavKey

    @Serializable
    data class CategoryDetail(val categoryName: String) : NavKey

    @Serializable
    data class ShayariDetail(
        val id: Int,
        val urdu: String, 
        val roman: String, 
        val english: String, 
        val category: String, 
        val poet: String,
        val docId: String? = null
    ) : NavKey
}
