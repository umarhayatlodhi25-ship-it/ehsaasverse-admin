package com.lodhidevelop.ehsaasverse.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lodhidevelop.ehsaasverse.data.model.Shayari
import com.lodhidevelop.ehsaasverse.data.repository.ShayariRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

class ShayariListViewModel(
    private val repository: ShayariRepository,
    private val categoryName: String? = null,
    private val poetName: String? = null,
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _displayList = MutableStateFlow<List<Shayari>>(emptyList())
    val displayList: StateFlow<List<Shayari>> = _displayList.asStateFlow()

    private val _favoriteUrduList = MutableStateFlow<Set<String>>(emptySet())
    val favoriteUrduList: StateFlow<Set<String>> = _favoriteUrduList.asStateFlow()

    private var fullList: List<Shayari> = emptyList()

    init {
        loadShayari()
        loadFavorites()
    }

    private fun loadFavorites() {
        viewModelScope.launch {
            val favs = repository.getFavoriteShayari()
            _favoriteUrduList.value = favs.map { it.urdu.trim() }.toSet()
        }
    }

    private fun loadShayari() {
        viewModelScope.launch {
            val local = if (categoryName != null && categoryName != "Nayi Shayari") {
                repository.getShayariByCategory(categoryName)
            } else if (poetName != null) {
                repository.getShayariByPoet(poetName)
            } else {
                emptyList()
            }
            fullList = local.sortedByDescending { it.timestamp }
            _displayList.value = fullList

            repository.getFirestoreShayariFlow().collect { remoteShayari ->
                withContext(Dispatchers.Default) {
                    val filteredRemote = if (categoryName == "Nayi Shayari") {
                        remoteShayari.sortedByDescending { it.timestamp }.take(50)
                    } else if (categoryName != null) {
                        remoteShayari.filter { 
                            it.category.trim().equals(categoryName.trim(), ignoreCase = true) 
                        }
                    } else if (poetName != null) {
                        remoteShayari.filter { it.poet.trim().equals(poetName.trim(), ignoreCase = true) }
                    } else {
                        remoteShayari
                    }
                    
                    fullList = (local + filteredRemote).distinctBy { it.urdu.trim() }
                        .sortedByDescending { it.timestamp }
                    
                    withContext(Dispatchers.Main) {
                        _displayList.value = fullList
                    }
                }
            }
        }
    }

    fun toggleFavorite(shayari: Shayari) {
        viewModelScope.launch {
            repository.toggleFavorite(shayari)
            loadFavorites()
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _displayList.value = fullList
        } else {
            _displayList.value = fullList.filter { 
                it.urdu.contains(query, ignoreCase = true) || 
                it.poet.contains(query, ignoreCase = true) ||
                it.roman.contains(query, ignoreCase = true)
            }
        }
    }
}
