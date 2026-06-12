# EhsaasVerse Custom ProGuard Rules

# 1. Supabase & Ktor
-keep class io.github.jan.supabase.** { *; }
-keep class io.ktor.** { *; }

# 2. Kotlinx Serialization
-keepattributes *Annotation*, EnclosingMethod, Signature
-keepclassmembers class ** {
    @kotlinx.serialization.SerialName <fields>;
}
-keep,allowobfuscation,allowoptimization class com.lodhidevelop.ehsaasverse.data.model.** { *; }

# 3. Cloudinary
-keep class com.cloudinary.** { *; }

# 4. AdMob & Play Services
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.android.gms.common.** { *; }

# 5. Firebase
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.internal.firebase_auth.** { *; }

# 6. Room Database
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# 7. Coil (Image Loading)
-keep class coil.** { *; }
-dontwarn coil.**

# 8. Moshi (if used for complex JSON)
-keep class com.squareup.moshi.** { *; }
-keep class * extends com.squareup.moshi.JsonAdapter
-keep @com.squareup.moshi.Json class *
-keepclassmembers class * {
    @com.squareup.moshi.Json *;
}

# 9. Suppress warnings for missing classes (Glide, Picasso, and Java Management)
-dontwarn com.bumptech.glide.**
-dontwarn com.squareup.picasso.**
-dontwarn java.lang.management.**