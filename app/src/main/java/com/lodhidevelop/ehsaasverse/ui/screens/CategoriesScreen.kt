package com.lodhidevelop.ehsaasverse.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lodhidevelop.ehsaasverse.R
import com.lodhidevelop.ehsaasverse.ui.theme.PrimaryMaroon
import com.lodhidevelop.ehsaasverse.ui.viewmodel.CategoriesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    viewModel: CategoriesViewModel,
    contentPadding: PaddingValues,
    onCategoryClick: (String) -> Unit,
    onMenuClick: () -> Unit = {},
    onFavoritesClick: () -> Unit = {}
) {
    val categories by viewModel.categories.collectAsState()
    val newArrivalCategories by viewModel.newArrivalCategories.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "محفلِ شاعری", 
                        color = Color.White,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Rounded.Menu, contentDescription = null, tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = onFavoritesClick) {
                        Icon(Icons.Rounded.Favorite, contentDescription = null, tint = Color.White)
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
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .padding(innerPadding)
                .padding(bottom = contentPadding.calculateBottomPadding())
                .fillMaxSize(),
            contentPadding = PaddingValues(8.dp)
        ) {
            itemsIndexed(categories) { index, category ->
                val isNew = category == "Nayi Shayari" || newArrivalCategories.contains(category)
                CategoryCard(
                    category = category,
                    index = index,
                    isNew = isNew,
                    onClick = { onCategoryClick(category) }
                )
            }
        }
    }
}
