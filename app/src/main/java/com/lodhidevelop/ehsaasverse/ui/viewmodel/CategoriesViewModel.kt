package com.lodhidevelop.ehsaasverse.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lodhidevelop.ehsaasverse.data.repository.ShayariRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CategoriesViewModel(private val repository: ShayariRepository) : ViewModel() {

    private val _categories = MutableStateFlow<List<String>>(emptyList())
    val categories: StateFlow<List<String>> = _categories.asStateFlow()

    private val _newArrivalCategories = MutableStateFlow<Set<String>>(emptySet())
    val newArrivalCategories: StateFlow<Set<String>> = _newArrivalCategories.asStateFlow()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            try {
                // Base categories
                val local = repository.getCategories()
                _categories.value = local

                repository.getFirestoreShayariFlow().collect { allShayari ->
                    val remoteCategories = allShayari.map { it.category }.distinct()
                    _categories.value = (local + remoteCategories).distinct()

                    // Check which categories have new items (last 24h)
                    val now = System.currentTimeMillis()
                    val oneDay = 24 * 60 * 60 * 1000L
                    val recentCategories = allShayari
                        .filter { now - it.timestamp < oneDay }
                        .map { it.category }
                        .toSet()
                    _newArrivalCategories.value = recentCategories
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
