package com.lodhidevelop.ehsaasverse.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.ai.client.generativeai.GenerativeModel
import com.google.firebase.firestore.FirebaseFirestore
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import com.lodhidevelop.ehsaasverse.data.model.PhotoShayari
import com.lodhidevelop.ehsaasverse.data.model.Shayari
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.Json

class AdminViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val COLLECTION_NAME = "verse_official_v1"
    
    private val supabase = createSupabaseClient(
        supabaseUrl = "https://jgdrdtirmuvkoznfuuog.supabase.co",
        supabaseKey = "sb_publishable_23VXzWVp2ofyGLegPbOMBw_rElW679g"
    ) {
        install(Postgrest)
    }

    private val generativeModel by lazy {
        GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = "AIzaSyDOvm9YbwuXT00eARa7ZREI0TAPAMxKLhA"
        )
    }
    private val json = Json { ignoreUnknownKeys = true }

    // Cloudinary Config - LIVE
    private val cloudName = "dmhrr3rw1"
    private val uploadPreset = "ehsaas_preset"

    private val _allShayari = MutableStateFlow<List<Shayari>>(emptyList())
    val allShayari: StateFlow<List<Shayari>> = _allShayari.asStateFlow()

    private val _allPhotos = MutableStateFlow<List<PhotoShayari>>(emptyList())
    val allPhotos: StateFlow<List<PhotoShayari>> = _allPhotos.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _generationStatus = MutableStateFlow("")
    val generationStatus: StateFlow<String> = _generationStatus.asStateFlow()

    init {
        loadAllData()
    }

    private fun initCloudinary(context: Context) {
        try {
            val config = mapOf(
                "cloud_name" to cloudName,
                "secure" to true
            )
            MediaManager.init(context, config)
        } catch (e: Exception) {
            // Already initialized
        }
    }

    fun loadAllData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Listen to Shayari (Firebase)
                firestore.collection(COLLECTION_NAME).addSnapshotListener { snapshot, e ->
                    if (e != null) return@addSnapshotListener
                    viewModelScope.launch(kotlinx.coroutines.Dispatchers.Default) {
                        val shayari = snapshot?.documents?.mapNotNull { doc ->
                            doc.toObject(Shayari::class.java)?.copy(docId = doc.id)
                        } ?: emptyList()
                        _allShayari.value = shayari
                    }
                }
                
                // Fetch Photos (Supabase)
                loadPhotosFromSupabase()
                
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadPhotosFromSupabase() {
        viewModelScope.launch {
            try {
                val photos = supabase.from("photo_shayari")
                    .select()
                    .decodeList<PhotoShayari>()
                _allPhotos.value = photos
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun addPhotoShayari(imageUrl: String, category: String = "General") {
        viewModelScope.launch {
            try {
                supabase.from("photo_shayari").insert(
                    PhotoShayari(imageUrl = imageUrl, category = category)
                )
                loadPhotosFromSupabase()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun uploadPhotoShayari(uri: Uri, context: Context, category: String = "General") {
        initCloudinary(context)
        _isLoading.value = true
        _generationStatus.value = "Uploading to Cloudinary..."

        if (cloudName == "YOUR_CLOUD_NAME") {
            _generationStatus.value = "Error: Please set your Cloudinary Cloud Name in AdminViewModel"
            _isLoading.value = false
            return
        }

        MediaManager.get().upload(uri)
            .unsigned(uploadPreset)
            .callback(object : UploadCallback {
                override fun onStart(requestId: String?) {}
                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                    val url = resultData?.get("secure_url") as? String
                    if (url != null) {
                        addPhotoShayari(url, category)
                        _generationStatus.value = "Success! Image uploaded."
                    }
                    _isLoading.value = false
                }
                override fun onError(requestId: String?, error: ErrorInfo?) {
                    _generationStatus.value = "Cloudinary Error: ${error?.description ?: "Unknown error"}"
                    _isLoading.value = false
                }
                override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
            }).dispatch()
    }

    fun deletePhotoShayari(photoId: String) {
        viewModelScope.launch {
            try {
                supabase.from("photo_shayari").delete {
                    filter {
                        eq("id", photoId)
                    }
                }
                loadPhotosFromSupabase()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun generateBulkShayari(category: String, count: Int = 10) {
        viewModelScope.launch {
            _isGenerating.value = true
            _generationStatus.value = "Generating $count Shayari for $category..."
            try {
                val prompt = """
                    Generate a JSON array of $count unique and beautiful Urdu shayari objects for the category '$category'.
                    Each object must have these fields:
                    - urdu: The shayari in Urdu script.
                    - roman: "" (empty string)
                    - english: "" (empty string)
                    - category: '$category'
                    - poet: Name of the poet or 'Unknown'.
                    - id: 0 (placeholder)
                    
                    Return ONLY the JSON array, no extra text.
                """.trimIndent()

                val response = generativeModel.generateContent(prompt)
                val jsonString = response.text?.trim() ?: ""
                val cleanedJson = jsonString.removeSurrounding("```json", "```").removeSurrounding("```").trim()

                val generatedList = json.decodeFromString<List<Shayari>>(cleanedJson)
                _generationStatus.value = "Saving to cloud..."
                
                for (shayari in generatedList) {
                    val finalShayari = shayari.copy(
                        id = (System.currentTimeMillis() / 1000).toInt() + (0..1000).random(),
                        timestamp = System.currentTimeMillis()
                    )
                    firestore.collection(COLLECTION_NAME).add(finalShayari).await()
                }

                _generationStatus.value = "Successfully added ${generatedList.size} shayari!"
            } catch (e: Exception) {
                _generationStatus.value = "Error: ${e.localizedMessage}"
            } finally {
                _isGenerating.value = false
            }
        }
    }

    fun updateShayari(oldUrdu: String, newShayari: Shayari) {
        viewModelScope.launch {
            try {
                if (newShayari.docId != null) {
                    firestore.collection(COLLECTION_NAME).document(newShayari.docId).set(newShayari).await()
                } else {
                    val snapshot = firestore.collection(COLLECTION_NAME)
                        .whereEqualTo("urdu", oldUrdu)
                        .get().await()
                    for (doc in snapshot.documents) {
                        doc.reference.set(newShayari).await()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteShayari(shayari: Shayari) {
        viewModelScope.launch {
            try {
                if (shayari.docId != null) {
                    firestore.collection(COLLECTION_NAME).document(shayari.docId).delete().await()
                } else {
                    val snapshot = firestore.collection(COLLECTION_NAME)
                        .whereEqualTo("urdu", shayari.urdu)
                        .get().await()
                    for (doc in snapshot.documents) {
                        doc.reference.delete().await()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun clearCategory(category: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val snapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("category", category)
                    .get().await()
                for (doc in snapshot.documents) {
                    doc.reference.delete().await()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
