package com.lodhidevelop.ehsaasverse.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseUser
import com.lodhidevelop.ehsaasverse.R
import com.lodhidevelop.ehsaasverse.data.model.Shayari
import com.lodhidevelop.ehsaasverse.ui.theme.PrimaryGold
import com.lodhidevelop.ehsaasverse.ui.theme.PrimaryMaroon
import com.lodhidevelop.ehsaasverse.ui.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    contentPadding: PaddingValues,
    onCategoryClick: (String) -> Unit,
    onShayariClick: (Shayari) -> Unit,
    onAddClick: () -> Unit = {},
    currentUser: FirebaseUser? = null,
    isAdmin: Boolean = false,
    onLogoutClick: () -> Unit = {},
    onLoginClick: () -> Unit = {},
    onAdminClick: () -> Unit = {},
    onEditClick: (Shayari) -> Unit = {},
    onDeleteClick: (Shayari) -> Unit = {},
    onMenuClick: () -> Unit = {},
    onSeeAllCategoriesClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
) {
    val sherOfTheDay by viewModel.sherOfTheDay.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val categorizedShayari by viewModel.categorizedShayari.collectAsState()
    val newArrivals by viewModel.newArrivals.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val favorites by viewModel.favoriteUrduList.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (currentUser != null) "LOGGED IN AS: ${currentUser.email}" else stringResource(R.string.greeting).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        )
                        Text(
                            text = stringResource(R.string.app_title),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 24.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Rounded.Menu, contentDescription = "Menu")
                    }
                },
                actions = {
                    if (isAdmin) {
                        Surface(
                            onClick = onAddClick,
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 4.dp,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.Add, 
                                    contentDescription = "Post Shayari", 
                                    tint = PrimaryMaroon,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            onClick = onAdminClick,
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 2.dp,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Rounded.AdminPanelSettings, contentDescription = "Admin Panel", tint = PrimaryMaroon)
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    IconButton(onClick = { if (currentUser != null) onLogoutClick() else onLoginClick() }) {
                        Icon(
                            imageVector = if (currentUser != null) Icons.AutoMirrored.Rounded.Logout else Icons.Rounded.AccountCircle,
                            contentDescription = if (currentUser != null) "Logout" else "Login"
                        )
                    }
                    Surface(
                        onClick = onSearchClick,
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 2.dp,
                        modifier = Modifier.padding(end = 16.dp).size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Rounded.Search, contentDescription = stringResource(R.string.search_content_desc))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            if (isAdmin) {
                FloatingActionButton(
                    onClick = onAddClick,
                    containerColor = PrimaryMaroon,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.padding(bottom = contentPadding.calculateBottomPadding())
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = "Post Shayari", modifier = Modifier.size(28.dp))
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .padding(bottom = contentPadding.calculateBottomPadding())
                .fillMaxSize()
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                val currentSherOfTheDay = sherOfTheDay
                if (currentSherOfTheDay != null) {
                    SherOfTheDayCard(
                        shayari = currentSherOfTheDay,
                        isAdmin = isAdmin,
                        isFavorite = favorites.contains(currentSherOfTheDay.urdu.trim()),
                        onClick = { onShayariClick(currentSherOfTheDay) },
                        onEditClick = { onEditClick(currentSherOfTheDay) },
                        onDeleteClick = { onDeleteClick(currentSherOfTheDay) },
                        onFavoriteClick = { viewModel.toggleFavorite(currentSherOfTheDay) },
                        onShareClick = {
                            val shareText = "${currentSherOfTheDay.urdu}\n\n— ${currentSherOfTheDay.poet}\n\nShared via EhsaasVerse"
                            val sendIntent = android.content.Intent().apply {
                                action = android.content.Intent.ACTION_SEND
                                putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                                type = "text/plain"
                            }
                            context.startActivity(android.content.Intent.createChooser(sendIntent, null))
                        },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                BannerAd(modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (newArrivals.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    SectionHeader(
                        title = "Nayi Shayari (New)",
                        onSeeAllClick = { /* Show all recent */ },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                items(newArrivals, key = { "new_${it.docId ?: it.urdu}" }) { shayari ->
                    TrendingShayariCard(
                        shayari = shayari,
                        isAdmin = isAdmin,
                        isFavorite = favorites.contains(shayari.urdu.trim()),
                        onClick = { onShayariClick(shayari) },
                        onEditClick = { onEditClick(shayari) },
                        onDeleteClick = { onDeleteClick(shayari) },
                        onFavoriteClick = { viewModel.toggleFavorite(shayari) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.mehfil_by_mood),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isAdmin) {
                            if (isGenerating) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = PrimaryMaroon, strokeWidth = 2.dp)
                            } else {
                                IconButton(
                                    onClick = { viewModel.generateAIShayari() },
                                    modifier = Modifier.size(32.dp).background(PrimaryMaroon.copy(alpha = 0.1f), CircleShape)
                                ) {
                                    Icon(Icons.Rounded.AutoAwesome, contentDescription = "AI Generate", tint = PrimaryMaroon, modifier = Modifier.size(18.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = onAddClick,
                                modifier = Modifier.size(32.dp).background(PrimaryMaroon.copy(alpha = 0.1f), CircleShape)
                            ) {
                                Icon(Icons.Rounded.Add, contentDescription = "Post", tint = PrimaryMaroon, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = onAdminClick,
                                modifier = Modifier.size(32.dp).background(PrimaryMaroon.copy(alpha = 0.1f), CircleShape)
                            ) {
                                Icon(Icons.Rounded.Edit, contentDescription = "Edit/Admin", tint = PrimaryMaroon, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        TextButton(onClick = onSeeAllCategoriesClick) {
                            Text(stringResource(R.string.see_all), color = PrimaryMaroon)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(categories) { category ->
                        CategoryHomeCard(
                            category = category,
                            onClick = { onCategoryClick(category) }
                        )
                    }
                }
            }

            categorizedShayari.forEach { (category, shayariList) ->
                item {
                    SectionHeader(
                        title = "$category Shayari",
                        onSeeAllClick = { onCategoryClick(category) },
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                items(shayariList, key = { "${category}_${it.docId ?: it.urdu}" }) { shayari ->
                    TrendingShayariCard(
                        shayari = shayari,
                        isAdmin = isAdmin,
                        isFavorite = favorites.contains(shayari.urdu.trim()),
                        onClick = { onShayariClick(shayari) },
                        onEditClick = { onEditClick(shayari) },
                        onDeleteClick = { onDeleteClick(shayari) },
                        onFavoriteClick = { viewModel.toggleFavorite(shayari) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrendingShayariCard(
    shayari: Shayari,
    isAdmin: Boolean = false,
    isFavorite: Boolean = false,
    onClick: () -> Unit,
    onEditClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    onFavoriteClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.Center
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Text(
                    text = shayari.urdu,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row {
                    if (isAdmin) {
                        IconButton(onClick = onEditClick) {
                            Icon(Icons.Rounded.Edit, contentDescription = "Edit", tint = PrimaryMaroon, modifier = Modifier.size(20.dp))
                        }
                        IconButton(onClick = onDeleteClick) {
                            Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(20.dp))
                        }
                    }
                    IconButton(onClick = onFavoriteClick) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) Color.Red else PrimaryMaroon,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Text(
                    text = "— ${shayari.poet}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    onSeeAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
        TextButton(onClick = onSeeAllClick) {
            Text(stringResource(R.string.see_all), color = PrimaryMaroon)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SherOfTheDayCard(
    shayari: Shayari,
    isAdmin: Boolean = false,
    isFavorite: Boolean = false,
    onClick: () -> Unit,
    onEditClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    onFavoriteClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = PrimaryMaroon),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                color = Color.White.copy(alpha = 0.15f),
                shape = CircleShape,
                modifier = Modifier.wrapContentSize()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = PrimaryGold,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.sher_of_the_day).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = PrimaryGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Text(
                    text = shayari.urdu,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    lineHeight = 36.sp,
                    fontSize = 24.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(4.dp).background(PrimaryGold, CircleShape))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${shayari.poet} • ${shayari.category.uppercase()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = PrimaryGold,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Row {
                    if (isAdmin) {
                        IconButton(onClick = onEditClick, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Rounded.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                        IconButton(onClick = onDeleteClick, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Rounded.Delete, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                    IconButton(onClick = onFavoriteClick, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                            contentDescription = null,
                            tint = if (isFavorite) Color.Red else Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onShareClick, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Rounded.IosShare, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryHomeCard(
    category: String,
    onClick: () -> Unit
) {
    val urduWord = when (category.lowercase()) {
        "love" -> "عشق"
        "sad" -> "اداسی"
        "motivation" -> "حوصلہ"
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

    Card(
        onClick = onClick,
        modifier = Modifier.size(width = 120.dp, height = 150.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = urduWord,
                style = MaterialTheme.typography.headlineSmall.copy(fontSize = 24.sp),
                color = PrimaryMaroon,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = category,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
