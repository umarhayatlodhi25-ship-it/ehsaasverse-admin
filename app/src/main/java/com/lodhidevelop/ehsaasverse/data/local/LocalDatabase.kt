package com.lodhidevelop.ehsaasverse.data.local

import android.content.Context
import androidx.room.*
import com.lodhidevelop.ehsaasverse.data.model.Shayari
import com.lodhidevelop.ehsaasverse.data.model.PhotoShayari
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "user_shayari")
data class UserShayariEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val urdu: String,
    val roman: String,
    val english: String,
    val category: String,
    val poet: String
)

@Entity(tableName = "favorite_shayari")
data class FavoriteShayariEntity(
    @PrimaryKey val urdu: String, // Use urdu text as unique key
    val id: Int,
    val roman: String,
    val english: String,
    val category: String,
    val poet: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "shayari_cache")
data class ShayariCacheEntity(
    @PrimaryKey val urdu: String,
    val id: Int,
    val roman: String,
    val english: String,
    val category: String,
    val poet: String,
    val docId: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "photo_shayari_cache")
data class PhotoShayariCacheEntity(
    @PrimaryKey val imageUrl: String,
    val id: String,
    val urdu: String,
    val roman: String,
    val english: String,
    val category: String
)

@Dao
interface ShayariDao {
    @Query("SELECT * FROM user_shayari")
    suspend fun getAllUserShayari(): List<UserShayariEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShayari(shayari: UserShayariEntity)

    @Delete
    suspend fun deleteShayari(shayari: UserShayariEntity)

    // Favorites
    @Query("SELECT * FROM favorite_shayari ORDER BY timestamp DESC")
    suspend fun getAllFavorites(): List<FavoriteShayariEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteShayariEntity)

    @Query("DELETE FROM favorite_shayari WHERE urdu = :urdu")
    suspend fun deleteFavoriteByUrdu(urdu: String)

    @Query("SELECT EXISTS(SELECT * FROM favorite_shayari WHERE urdu = :urdu)")
    suspend fun isFavorite(urdu: String): Boolean

    // Online Cache
    @Query("SELECT * FROM shayari_cache ORDER BY timestamp DESC")
    suspend fun getCachedShayari(): List<ShayariCacheEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCacheList(list: List<ShayariCacheEntity>)

    // Photo Cache
    @Query("SELECT * FROM photo_shayari_cache")
    suspend fun getCachedPhotoShayari(): List<PhotoShayariCacheEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhotoCacheList(list: List<PhotoShayariCacheEntity>)
}

@Database(entities = [UserShayariEntity::class, FavoriteShayariEntity::class, ShayariCacheEntity::class, PhotoShayariCacheEntity::class], version = 5)
abstract class AppDatabase : RoomDatabase() {
    abstract fun shayariDao(): ShayariDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ehsaas_verse_db"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
