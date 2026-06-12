package com.lodhidevelop.ehsaasverse.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lodhidevelop.ehsaasverse.data.model.Shayari
import com.lodhidevelop.ehsaasverse.data.repository.ShayariRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ShayariDetailViewModel(
    private val repository: ShayariRepository,
    val shayari: Shayari
) : ViewModel() {
    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    init {
        viewModelScope.launch {
            _isFavorite.value = repository.isFavorite(shayari.urdu)
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            repository.toggleFavorite(shayari)
            _isFavorite.value = repository.isFavorite(shayari.urdu)
        }
    }
}
