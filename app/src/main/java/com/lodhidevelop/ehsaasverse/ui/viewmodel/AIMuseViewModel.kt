package com.lodhidevelop.ehsaasverse.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AIMuseViewModel : ViewModel() {
    // Standard Gemini SDK (Doesn't require Firebase Blaze Plan)
    private val generativeModel by lazy {
        GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = "AIzaSyDOvm9YbwuXT00eARa7ZREI0TAPAMxKLhA",
        )
    }

    private val _uiState = MutableStateFlow("")
    val uiState: StateFlow<String> = _uiState

    fun generateShayari(prompt: String) {
        viewModelScope.launch {
            _uiState.value = "Generating..."
            try {
                val response = generativeModel.generateContent(
                    "Write a short, beautiful Urdu Shayari about: $prompt. Provide ONLY the Urdu script text."
                )
                _uiState.value = response.text ?: "No response from AI"
            } catch (_: Throwable) {
                _uiState.value = "AI feature ke liye internet zaroori hai. Offline mode mein local shayari browse karein."
            }
        }
    }
}
