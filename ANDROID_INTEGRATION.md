# EhsaasVerse Android Integration Guide

This guide provides the Kotlin code required to connect your Android app with the new Production Backend (Firestore & Supabase).

## 1. Dependencies (build.gradle.kts)

```kotlin
dependencies {
    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))
    implementation("com.google.firebase:firebase-firestore-ktx")
    
    // Supabase
    implementation("io.github.jan.supabase:postgrest-kt:2.5.0")
    implementation("io.github.jan.supabase:gotrue-kt:2.5.0")
    
    // Image Loading
    implementation("io.coil-kt:coil-compose:2.6.0")
}
```

## 2. Text Shayari Repository (Firestore)

```kotlin
class ShayariRepository {
    private val db = FirebaseFirestore.getInstance()
    private val textCollection = db.collection("text_shayari")

    fun getLatestShayari(): Flow<List<Shayari>> = callbackFlow {
        val listener = textCollection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val list = snapshot?.toObjects(Shayari::class.java) ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    fun getShayariByCategory(category: String): Flow<List<Shayari>> = callbackFlow {
        val listener = textCollection
            .whereEqualTo("category", category)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val list = snapshot?.toObjects(Shayari::class.java) ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }
}
```

## 3. Photo Shayari Service (Supabase)

```kotlin
class PhotoShayariService(private val supabase: SupabaseClient) {

    suspend fun fetchPhotos(): List<PhotoShayari> {
        return supabase.from("image_shayari")
            .select()
            .decodeList<PhotoShayari>()
    }

    suspend fun fetchPhotosByCategory(category: String): List<PhotoShayari> {
        return supabase.from("image_shayari")
            .select {
                filter {
                    eq("category", category)
                }
            }
            .decodeList<PhotoShayari>()
    }
}
```

## 4. Models

```kotlin
data class Shayari(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val category: String = "",
    val author: String = "Unknown",
    val createdAt: Long = 0
)

data class PhotoShayari(
    val id: String,
    val title: String?,
    val category: String,
    val image_url: String,
    val created_at: String
)
```
