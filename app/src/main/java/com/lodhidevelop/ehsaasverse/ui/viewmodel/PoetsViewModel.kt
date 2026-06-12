package com.lodhidevelop.ehsaasverse.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lodhidevelop.ehsaasverse.data.repository.ShayariRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PoetsViewModel(private val repository: ShayariRepository) : ViewModel() {

    private val _poets = MutableStateFlow<List<String>>(emptyList())
    val poets: StateFlow<List<String>> = _poets.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadPoets()
    }

    private fun loadPoets() {
        viewModelScope.launch {
            _isLoading.value = true
            val local = repository.getPoets()
            _poets.value = local

            try {
                val remote = repository.getFirestorePoets()
                _poets.value = (local + remote).distinct().sorted()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
