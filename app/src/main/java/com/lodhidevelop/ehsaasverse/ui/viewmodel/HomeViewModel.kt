package com.lodhidevelop.ehsaasverse.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.lodhidevelop.ehsaasverse.data.model.Shayari
import com.lodhidevelop.ehsaasverse.data.repository.ShayariRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json

class HomeViewModel(private val repository: ShayariRepository) : ViewModel() {
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val COLLECTION_NAME = "verse_official_v1"
    private val generativeModel by lazy {
        GenerativeModel(
            modelName = "gemini-pro",
            apiKey = "AIzaSyDOvm9YbwuXT00eARa7ZREI0TAPAMxKLhA"
        )
    }
    private val json = Json { ignoreUnknownKeys = true }

    private val _sherOfTheDay = MutableStateFlow<Shayari?>(null)
    val sherOfTheDay: StateFlow<Shayari?> = _sherOfTheDay.asStateFlow()

    private val _categories = MutableStateFlow<List<String>>(emptyList())
    val categories: StateFlow<List<String>> = _categories.asStateFlow()

    private val _categorizedShayari = MutableStateFlow<Map<String, List<Shayari>>>(emptyMap())
    val categorizedShayari: StateFlow<Map<String, List<Shayari>>> = _categorizedShayari.asStateFlow()

    private val _newArrivals = MutableStateFlow<List<Shayari>>(emptyList())
    val newArrivals: StateFlow<List<Shayari>> = _newArrivals.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _favoriteUrduList = MutableStateFlow<Set<String>>(emptySet())
    val favoriteUrduList: StateFlow<Set<String>> = _favoriteUrduList.asStateFlow()

    init {
        loadData()
        loadFavorites()
    }

    private fun loadFavorites() {
        viewModelScope.launch {
            val favs = repository.getFavoriteShayari()
            _favoriteUrduList.value = favs.map { it.urdu.trim() }.toSet()
        }
    }

    fun generateAIShayari() {
        viewModelScope.launch {
            _isGenerating.value = true
            try {
                val category = _categories.value.randomOrNull() ?: "Love"
                val prompt = """
                    Generate ONE unique and beautiful Urdu shayari object for the category '$category'.
                    Return ONLY the JSON object with fields: 
                    urdu: (The Urdu script text), 
                    roman: "", 
                    english: "", 
                    category: "$category", 
                    poet: "Unknown", 
                    id: 0.
                """.trimIndent()

                val response = generativeModel.generateContent(prompt)
                val jsonString = response.text?.trim() ?: ""
                val cleanedJson = jsonString.removeSurrounding("```json", "```").removeSurrounding("```").trim()
                
                val shayari = json.decodeFromString<Shayari>(cleanedJson)
                val finalShayari = shayari.copy(
                    id = (System.currentTimeMillis() / 1000).toInt(),
                    timestamp = System.currentTimeMillis()
                )
                
                firestore.collection(COLLECTION_NAME).add(finalShayari).await()
            } catch (e: Throwable) {
                e.printStackTrace()
            } finally {
                _isGenerating.value = false
            }
        }
    }

    fun toggleFavorite(shayari: Shayari) {
        viewModelScope.launch {
            repository.toggleFavorite(shayari)
            loadFavorites()
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                // 1. Load and Show Local Data IMMEDIATELY
                val localShayari = repository.getAllShayari()
                val localCategories = repository.getCategories()
                
                if (localCategories.isNotEmpty()) {
                    _categories.value = localCategories
                }
                if (localShayari.isNotEmpty()) {
                    updateCategorizedShayari(localShayari)
                }

                // 2. Load and generate today's AI Sher in parallel (Non-blocking)
                viewModelScope.launch {
                    checkAndGenerateDailySher()
                }

                // 3. Real-time Cloud Data (will update UI when data arrives)
                repository.getFirestoreShayariFlow().collect { remoteShayari ->
                    withContext(Dispatchers.Default) {
                        val allShayari = (localShayari + remoteShayari).distinctBy { it.urdu.trim() }
                        
                        // Update categories if remote data brings new ones
                        val allCategories = allShayari.map { it.category }.distinct()
                        
                        withContext(Dispatchers.Main) {
                            if (allCategories != _categories.value) {
                                _categories.value = allCategories
                            }
                            
                            // Daily Sher logic: Try to find today's AI sher first
                            val todayId = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.getDefault()).format(java.util.Date())
                            val todaySher = remoteShayari.find { it.docId == "daily_$todayId" }
                            
                            if (todaySher != null) {
                                _sherOfTheDay.value = todaySher
                            } else if (_sherOfTheDay.value == null && allShayari.isNotEmpty()) {
                                // Fallback to random if AI sher not yet synced and nothing is shown yet
                                val calendar = java.util.Calendar.getInstance()
                                val seed = (calendar.get(java.util.Calendar.YEAR) * 1000 + calendar.get(java.util.Calendar.DAY_OF_YEAR)).toLong()
                                val random = java.util.Random(seed)
                                _sherOfTheDay.value = allShayari[random.nextInt(allShayari.size)]
                            }
                        }

                        updateCategorizedShayari(allShayari)
                    }
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun checkAndGenerateDailySher() {
        val todayId = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.getDefault()).format(java.util.Date())
        val docPath = "daily_$todayId"
        
        try {
            val doc = firestore.collection(COLLECTION_NAME).document(docPath).get().await()
            if (!doc.exists()) {
                // Generate a masterpiece for today using AI
                val categories = listOf("Love", "Sad", "Zindagi", "Tanhai", "Dosti", "Ishq", "Khwab")
                val category = categories.random()
                
                val prompt = """
                    Generate ONE masterpiece Urdu shayari for the category '$category'.
                    This will be the official 'Sher of the Day'.
                    Return ONLY the JSON object with fields: 
                    urdu: (The Urdu script text), 
                    roman: "", 
                    english: "", 
                    category: "$category", 
                    poet: "Gemini AI", 
                    id: 0.
                """.trimIndent()

                val response = generativeModel.generateContent(prompt)
                val jsonString = response.text?.trim() ?: ""
                val cleanedJson = jsonString.removeSurrounding("```json", "```").removeSurrounding("```").trim()
                
                val shayari = json.decodeFromString<Shayari>(cleanedJson)
                val finalShayari = shayari.copy(
                    id = todayId.toInt(),
                    timestamp = System.currentTimeMillis()
                )
                
                firestore.collection(COLLECTION_NAME).document(docPath).set(finalShayari).await()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateCategorizedShayari(allShayari: List<Shayari>) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.Default) {
            val now = System.currentTimeMillis()
            
            // Show newest first in New Arrivals
            val newArrivals = allShayari
                .sortedByDescending { it.timestamp }
                .take(10)
            _newArrivals.value = newArrivals

            // Standard categories list to ensure order and cleanliness
            val officialCategories = listOf(
                "Love", "Sad", "Motivation", "Friendship", "Yaad", "Zindagi", 
                "Khwab", "Intezar", "Dard", "Dua", "Barish", "Tanhai", 
                "Bewafa", "Mashhoor", "Judai", "Aansu", "Ishq"
            )

            // Group by category with strict cleaning
            val mapped = allShayari
                .filter { it.category.trim() in officialCategories }
                .groupBy { it.category.trim() }
                .mapValues { entry -> 
                    entry.value.sortedByDescending { it.timestamp }.take(10)
                }

            _categorizedShayari.value = mapped
        }
    }
}
