package com.lodhidevelop.ehsaasverse.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lodhidevelop.ehsaasverse.data.model.PhotoShayari
import com.lodhidevelop.ehsaasverse.data.repository.ShayariRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class PhotoShayariViewModel(private val repository: ShayariRepository) : ViewModel() {

    private val _allPhotos = MutableStateFlow<List<PhotoShayari>>(emptyList())
    
    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _displayPhotos = MutableStateFlow<List<PhotoShayari>>(emptyList())
    val displayPhotos: StateFlow<List<PhotoShayari>> = _displayPhotos.asStateFlow()

    private val _categories = MutableStateFlow<List<String>>(listOf("All"))
    val categories: StateFlow<List<String>> = _categories.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadPhotos()
        setupFiltering()
    }

    private fun loadPhotos() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getPhotoShayariFlow().collect { list ->
                _allPhotos.value = list
                
                // Extract unique categories from photos
                val photoCats = list.map { it.category }.distinct().sorted()
                _categories.value = listOf("All") + photoCats
                
                _isLoading.value = false
            }
        }
    }

    private fun setupFiltering() {
        viewModelScope.launch {
            combine(_allPhotos, _selectedCategory) { photos, category ->
                if (category == "All") photos
                else photos.filter { it.category.equals(category, ignoreCase = true) }
            }.collect { filtered ->
                _displayPhotos.value = filtered
            }
        }
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }
}
