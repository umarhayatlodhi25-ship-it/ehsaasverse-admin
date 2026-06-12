package com.lodhidevelop.ehsaasverse.data.repository

import android.content.Context
import com.lodhidevelop.ehsaasverse.data.local.AppDatabase
import com.lodhidevelop.ehsaasverse.data.local.FavoriteManager
import com.lodhidevelop.ehsaasverse.data.local.UserShayariEntity
import com.lodhidevelop.ehsaasverse.data.local.ShayariCacheEntity
import com.lodhidevelop.ehsaasverse.data.local.PhotoShayariCacheEntity
import com.lodhidevelop.ehsaasverse.data.model.Shayari
import com.lodhidevelop.ehsaasverse.data.model.PhotoShayari
import com.google.firebase.firestore.FirebaseFirestore
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.Json

class ShayariRepository(private val context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true
    }

    private val favoriteManager = FavoriteManager(context)
    private val db = AppDatabase.getDatabase(context)
    private val shayariDao = db.shayariDao()
    private val firestore = FirebaseFirestore.getInstance()
    private val COLLECTION_NAME = "verse_official_v1"

    private val supabase = createSupabaseClient(
        supabaseUrl = "https://jgdrdtirmuvkoznfuuog.supabase.co",
        supabaseKey = "sb_publishable_23VXzWVp2ofyGLegPbOMBw_rElW679g"
    ) {
        install(Postgrest)
    }

    private var cachedShayari: List<Shayari>? = null

    fun getFirestoreShayariFlow() = callbackFlow {
        try {
            val listener = firestore.collection(COLLECTION_NAME).addSnapshotListener { snapshot, e ->
                if (e != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                launch(kotlinx.coroutines.Dispatchers.Default) {
                    val shayari = snapshot?.documents?.mapNotNull { doc ->
                        try {
                            doc.toObject(Shayari::class.java)?.copy(
                                docId = doc.id,
                                category = doc.getString("category")?.trim() ?: ""
                            )
                        } catch (_: Exception) {
                            null
                        }
                    } ?: emptyList()
                    
                    // Save to local cache for offline use
                    if (shayari.isNotEmpty()) {
                        launch(kotlinx.coroutines.Dispatchers.IO) {
                            val cacheList = shayari.map { s ->
                                ShayariCacheEntity(
                                    urdu = s.urdu,
                                    id = s.id,
                                    roman = s.roman,
                                    english = s.english,
                                    category = s.category,
                                    poet = s.poet,
                                    docId = s.docId,
                                    timestamp = s.timestamp
                                )
                            }
                            shayariDao.insertCacheList(cacheList)
                        }
                    }
                    
                    trySend(shayari)
                }
            }
            awaitClose { listener.remove() }
        } catch (e: Exception) {
            trySend(emptyList())
            close(e)
        }
    }

    fun getPhotoShayariFlow() = flow {
        // 1. Emit local assets photos
        val localPhotos = getLocalPhotoShayari()
        
        // 2. Emit cached remote photos from Room
        val cachedPhotos = try {
            shayariDao.getCachedPhotoShayari().map { cached ->
                PhotoShayari(
                    id = cached.id,
                    imageUrl = cached.imageUrl,
                    urdu = cached.urdu,
                    roman = cached.roman,
                    english = cached.english,
                    category = cached.category
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
        
        val initialList = (localPhotos + cachedPhotos).distinctBy { it.imageUrl }
        emit(initialList)

        // 3. Fetch fresh data from Supabase and update cache
        try {
            val remotePhotos = supabase.from("photo_shayari")
                .select()
                .decodeList<PhotoShayari>()
            
            if (remotePhotos.isNotEmpty()) {
                val cacheList = remotePhotos.map { p ->
                    PhotoShayariCacheEntity(
                        imageUrl = p.imageUrl,
                        id = p.id,
                        urdu = p.urdu,
                        roman = p.roman,
                        english = p.english,
                        category = p.category
                    )
                }
                shayariDao.insertPhotoCacheList(cacheList)
            }
            
            val combined = (localPhotos + remotePhotos).distinctBy { it.imageUrl }
            emit(combined)
        } catch (e: Exception) {
            e.printStackTrace()
            // Keep showing initial if remote fails
        }
    }

    private fun getLocalPhotoShayari(): List<PhotoShayari> {
        return try {
            val jsonString = context.assets.open("photo_shayari.json").bufferedReader().use { it.readText() }
            json.decodeFromString<List<PhotoShayari>>(jsonString)
        } catch (e: Throwable) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getFirestoreShayari(): List<Shayari> {
        return try {
            val snapshot = firestore.collection(COLLECTION_NAME).get().await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Shayari::class.java)?.copy(
                    docId = doc.id,
                    category = doc.getString("category")?.trim() ?: ""
                )
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getAllShayari(): List<Shayari> = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        cachedShayari?.let { return@withContext it }
        
        val assetShayari = try {
            val jsonString = context.assets.open("shayari.json").bufferedReader().use { it.readText() }
            json.decodeFromString<List<Shayari>>(jsonString)
        } catch (e: Throwable) {
            e.printStackTrace()
            emptyList()
        }

        val userShayari = try {
            shayariDao.getAllUserShayari().map { saved ->
                Shayari(
                    id = saved.id,
                    urdu = saved.urdu.trim(),
                    roman = saved.roman,
                    english = saved.english,
                    category = saved.category.trim().lowercase().replaceFirstChar { it.uppercase() },
                    poet = saved.poet,
                )
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            emptyList()
        }

        val cachedRemoteShayari = try {
            shayariDao.getCachedShayari().map { cached ->
                Shayari(
                    id = cached.id,
                    urdu = cached.urdu.trim(),
                    roman = cached.roman,
                    english = cached.english,
                    category = cached.category.trim().lowercase().replaceFirstChar { it.uppercase() },
                    poet = cached.poet,
                    docId = cached.docId,
                    timestamp = cached.timestamp
                )
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            emptyList()
        }

        val combinedShayari = (assetShayari + userShayari + cachedRemoteShayari)
            .map { it.copy(category = it.category.trim().lowercase().replaceFirstChar { it.uppercase() }) }
            .distinctBy { it.urdu.trim() }

        cachedShayari = combinedShayari
        combinedShayari
    }

    fun getOfflinePhotoShayariFlow(): Flow<List<PhotoShayari>> = flow {
        emit(getLocalPhotoShayari())
    }

    suspend fun saveUserShayari(urdu: String, roman: String, english: String, category: String, poet: String) {
        val entity = UserShayariEntity(
            urdu = urdu,
            roman = roman,
            english = english,
            category = category,
            poet = poet
        )
        shayariDao.insertShayari(entity)
        
        // Firestore mein bhi save karte hain
        try {
            val shayari = Shayari(
                id = (System.currentTimeMillis() / 1000).toInt(),
                urdu = urdu.trim(),
                roman = roman.trim(),
                english = english.trim(),
                category = category.trim(),
                poet = poet.trim(),
                timestamp = System.currentTimeMillis()
            )
            firestore.collection(COLLECTION_NAME).add(shayari).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Invalidate cache to force reload if needed
        cachedShayari = null
    }

    suspend fun getShayariByCategory(category: String): List<Shayari> {
        return getAllShayari().filter { it.category.equals(category, ignoreCase = true) }
    }

    suspend fun getFirestoreCategories(): List<String> {
        return try {
            val firestoreShayari = getFirestoreShayari()
            firestoreShayari.asSequence().map { it.category }.distinct().toList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun getCategories(): List<String> {
        return getAllShayari().map { it.category }.distinct()
    }

    suspend fun getPoets(): List<String> {
        return getAllShayari().asSequence().map { it.poet }.filter { it.isNotBlank() && (it != "Unknown") }.distinct().toList()
    }

    suspend fun getFirestorePoets(): List<String> {
        return try {
            val firestoreShayari = getFirestoreShayari()
            firestoreShayari.map { it.poet }.filter { it.isNotBlank() && it != "Unknown" }.distinct()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getShayariByPoet(poet: String): List<Shayari> {
        return getAllShayari().filter { it.poet.equals(poet, ignoreCase = true) }
    }

    suspend fun getTrendingShayari(): List<Shayari> {
        // Return 10 random shayari as "trending"
        return getAllShayari().shuffled().take(10)
    }

    suspend fun getFavoriteShayari(): List<Shayari> = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            shayariDao.getAllFavorites().map { saved ->
                Shayari(
                    id = saved.id,
                    urdu = saved.urdu,
                    roman = saved.roman,
                    english = saved.english,
                    category = saved.category,
                    poet = saved.poet,
                    timestamp = saved.timestamp
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun toggleFavorite(shayari: Shayari) = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            if (shayariDao.isFavorite(shayari.urdu.trim())) {
                shayariDao.deleteFavoriteByUrdu(shayari.urdu.trim())
            } else {
                val entity = com.lodhidevelop.ehsaasverse.data.local.FavoriteShayariEntity(
                    urdu = shayari.urdu.trim(),
                    id = shayari.id,
                    roman = shayari.roman,
                    english = shayari.english,
                    category = shayari.category,
                    poet = shayari.poet,
                    timestamp = System.currentTimeMillis()
                )
                shayariDao.insertFavorite(entity)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun isFavorite(urdu: String): Boolean = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            shayariDao.isFavorite(urdu.trim())
        } catch (e: Exception) {
            false
        }
    }
}
