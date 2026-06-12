package com.lodhidevelop.ehsaasverse.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.shape.CircleShape
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.lodhidevelop.ehsaasverse.AdsManager
import com.lodhidevelop.ehsaasverse.R
import com.lodhidevelop.ehsaasverse.data.model.Shayari
import com.lodhidevelop.ehsaasverse.ui.theme.PrimaryGold
import com.lodhidevelop.ehsaasverse.ui.theme.PrimaryMaroon

@Composable
fun BannerAd(modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = AdsManager.BANNER_AD_ID
                adListener = object : AdListener() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        val errorMessage = when(adError.code) {
                            AdRequest.ERROR_CODE_NO_FILL -> "NO_FILL: AdMob has no ad for this unit right now. This is common for new apps/units."
                            AdRequest.ERROR_CODE_NETWORK_ERROR -> "NETWORK_ERROR: Check your internet connection."
                            AdRequest.ERROR_CODE_INTERNAL_ERROR -> "INTERNAL_ERROR: Something went wrong with AdMob."
                            AdRequest.ERROR_CODE_INVALID_REQUEST -> "INVALID_REQUEST: Check your Ad Unit ID and Manifest Application ID."
                            else -> adError.message
                        }
                        android.util.Log.e("BannerAd", "Ad Failed: $errorMessage (Code: ${adError.code})")
                    }
                    override fun onAdLoaded() {
                        android.util.Log.d("BannerAd", "Real Ad Loaded Successfully!")
                    }
                }
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}

@Composable
fun ShayariListItem(
    shayari: Shayari,
    isAdmin: Boolean = false,
    isFavorite: Boolean = false,
    onCopyClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    onFavoriteClick: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    onClick: () -> Unit = {}
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Text(
                        text = shayari.urdu,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.DarkGray,
                        textAlign = TextAlign.Center,
                        lineHeight = 36.sp
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFEEEEEE))
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isAdmin) {
                    IconButton(onClick = onEditClick) {
                        Icon(Icons.Rounded.Edit, contentDescription = "Edit", tint = Color(0xFF00796B), modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(20.dp))
                    }
                }

                IconButton(onClick = onCopyClick) {
                    Icon(
                        imageVector = Icons.Rounded.ContentCopy,
                        contentDescription = "Copy",
                        tint = Color(0xFF555555),
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = onShareClick) {
                    Icon(
                        imageVector = Icons.Rounded.Share,
                        contentDescription = "Share",
                        tint = Color(0xFF555555),
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = onFavoriteClick) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) Color.Red else Color(0xFF555555),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

private val categoryColors = listOf(
    Color(0xFF8B1A24), // Primary Maroon
    Color(0xFF6D121B), // Darker Maroon
    Color(0xFFB02A33), // Lighter Maroon
    Color(0xFF4E0D14), // Deep Maroon
    Color(0xFF9C202B), // Medium Maroon
    Color(0xFFE5C68A), // Primary Gold
    Color(0xFFD4AF37), // Metallic Gold
    Color(0xFFC5A059), // Muted Gold
)

@Composable
fun CategoryCard(
    category: String,
    index: Int,
    isNew: Boolean = false,
    onClick: () -> Unit
) {
    val urduName = when (category.lowercase()) {
        "nayi shayari" -> "نئی شاعری"
        "love" -> "عشق"
        "motivation" -> "حوصلہ"
        "sad" -> "اداسی"
        "friendship" -> "دوستی"
        "yaad" -> "یاد"
        "zindagi" -> "زندگی"
        "khwab" -> "خواب"
        "intezar" -> "انتظار"
        "dard" -> "درد"
        "dua" -> "دعا"
        "barish" -> "بارش"
        "tanhai" -> "تنہائی"
        "bewafa" -> "بے وفا"
        "mashhoor" -> "مشہور اشعار"
        "judai" -> "جدائی"
        "aansu" -> "آنسو"
        else -> category
    }

    val containerColor = if (category == "Nayi Shayari") PrimaryGold else categoryColors[index % categoryColors.size]
    val contentColor = if (category == "Nayi Shayari") PrimaryMaroon else Color.White

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .padding(6.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (isNew) {
                Surface(
                    color = Color.Red,
                    shape = CircleShape,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(8.dp)
                ) {}
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = urduName,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp
                    ),
                    color = contentColor,
                    textAlign = TextAlign.Center
                )
                if (category == "Nayi Shayari") {
                    Text(
                        text = "Daily Update",
                        style = MaterialTheme.typography.labelSmall,
                        color = contentColor.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}
