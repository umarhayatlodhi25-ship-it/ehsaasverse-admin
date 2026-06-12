package com.lodhidevelop.ehsaasverse.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.res.stringResource
import com.lodhidevelop.ehsaasverse.R
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lodhidevelop.ehsaasverse.data.model.Shayari
import com.lodhidevelop.ehsaasverse.ui.viewmodel.SavedViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedScreen(
    viewModel: SavedViewModel,
    contentPadding: PaddingValues,
    onShayariClick: (Shayari) -> Unit,
) {
    val savedShayari by viewModel.savedShayari.collectAsState()

    // Refresh favorites when the screen is shown
    LaunchedEffect(Unit) {
        viewModel.loadFavorites()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_vault), style = MaterialTheme.typography.headlineSmall) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        bottomBar = {
            BannerAd()
        }
    ) { innerPadding ->
        if (savedShayari.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(bottom = contentPadding.calculateBottomPadding())
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.empty_vault),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(bottom = contentPadding.calculateBottomPadding())
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(savedShayari) { shayari ->
                    ShayariListItem(
                        shayari = shayari,
                        isFavorite = true,
                        onFavoriteClick = { viewModel.toggleFavorite(shayari) },
                        onClick = { onShayariClick(shayari) }
                    )
                }
            }
        }
    }
}
