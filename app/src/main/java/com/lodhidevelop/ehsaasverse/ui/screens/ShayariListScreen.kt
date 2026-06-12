package com.lodhidevelop.ehsaasverse.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lodhidevelop.ehsaasverse.ui.theme.PrimaryMaroon
import com.lodhidevelop.ehsaasverse.ui.viewmodel.ShayariListViewModel
import com.lodhidevelop.ehsaasverse.ui.screens.BannerAd

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShayariListScreen(
    categoryName: String,
    viewModel: ShayariListViewModel,
    contentPadding: PaddingValues,
    isAdmin: Boolean = false,
    onShayariClick: (com.lodhidevelop.ehsaasverse.data.model.Shayari) -> Unit,
    onBackClick: () -> Unit,
    onAddClick: () -> Unit = {},
    onEditClick: (com.lodhidevelop.ehsaasverse.data.model.Shayari) -> Unit = {},
    onDeleteClick: (com.lodhidevelop.ehsaasverse.data.model.Shayari) -> Unit = {}
) {
    val shayariList by viewModel.displayList.collectAsState()
    val favorites by viewModel.favoriteUrduList.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val urduTitle = when (categoryName.lowercase()) {
        "love" -> "عشق و محبت"
        "sad" -> "اداس شاعری"
        "motivation" -> "حوصلہ افزائی"
        "friendship" -> "دوستی"
        "yaad" -> "یادیں"
        "zindagi" -> "زندگی"
        "khwab" -> "خواب"
        "intezar" -> "انتظار"
        "dard" -> "درد"
        "dua" -> "دعا"
        "barish" -> "بارش"
        "tanhai" -> "تنہائی"
        "bewafa" -> "بے وفائی"
        "mashhoor" -> "مشہور اشعار"
        "judai" -> "جدائی"
        "aansu" -> "آنسو"
        "ishq" -> "عشق"
        else -> categoryName
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        urduTitle, 
                        color = Color.White,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null, tint = Color.White)
                    }
                },
                actions = {
                    if (isAdmin) {
                        IconButton(onClick = onAddClick) {
                            Icon(Icons.Rounded.Add, contentDescription = "Add", tint = Color.White)
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = PrimaryMaroon
                )
            )
        },
        bottomBar = {
            BannerAd()
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .padding(bottom = contentPadding.calculateBottomPadding())
                .fillMaxSize(),
            contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp)
        ) {
            items(shayariList, key = { it.docId ?: it.urdu.trim() }) { shayari ->
                ShayariListItem(
                    shayari = shayari,
                    isAdmin = isAdmin,
                    isFavorite = favorites.contains(shayari.urdu.trim()),
                    onClick = { onShayariClick(shayari) },
                    onEditClick = { onEditClick(shayari) },
                    onDeleteClick = { onDeleteClick(shayari) },
                    onFavoriteClick = { viewModel.toggleFavorite(shayari) },
                    onCopyClick = {
                        clipboardManager.setText(AnnotatedString(shayari.urdu))
                    },
                    onShareClick = {
                        val sendIntent = android.content.Intent().apply {
                            action = android.content.Intent.ACTION_SEND
                            putExtra(android.content.Intent.EXTRA_TEXT, shayari.urdu)
                            type = "text/plain"
                        }
                        context.startActivity(android.content.Intent.createChooser(sendIntent, null))
                    }
                )
            }
        }
    }
}
