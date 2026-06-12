package com.lodhidevelop.ehsaasverse.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lodhidevelop.ehsaasverse.data.model.Shayari
import com.lodhidevelop.ehsaasverse.data.repository.ShayariRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SavedViewModel(private val repository: ShayariRepository) : ViewModel() {

    private val _savedShayari = MutableStateFlow<List<Shayari>>(emptyList())
    val savedShayari: StateFlow<List<Shayari>> = _savedShayari.asStateFlow()

    init {
        loadFavorites()
    }

    fun toggleFavorite(shayari: Shayari) {
        viewModelScope.launch {
            repository.toggleFavorite(shayari)
            loadFavorites()
        }
    }

    fun loadFavorites() {
        viewModelScope.launch {
            _savedShayari.value = repository.getFavoriteShayari()
        }
    }
}
