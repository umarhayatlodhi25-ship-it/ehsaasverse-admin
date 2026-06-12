package com.lodhidevelop.ehsaasverse.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lodhidevelop.ehsaasverse.data.model.Shayari
import com.lodhidevelop.ehsaasverse.data.repository.ShayariRepository
import com.lodhidevelop.ehsaasverse.AdsManager
import com.lodhidevelop.ehsaasverse.ui.screens.*
import com.lodhidevelop.ehsaasverse.ui.viewmodel.*
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import kotlinx.coroutines.launch

@Composable
fun NavigationGraph(
    backStack: NavBackStack<androidx.navigation3.runtime.NavKey>,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    onMenuClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val repository = remember { ShayariRepository(context) }
    
    // Initial ad load
    LaunchedEffect(Unit) {
        AdsManager.loadInterstitialAd(context)
    }
    val authViewModel: AuthViewModel = viewModel()
    val adminViewModel: AdminViewModel = viewModel()
    val currentUser by authViewModel.currentUser.collectAsState()
    val isAdmin by authViewModel.isAdmin.collectAsState()

    var editingShayari by remember { mutableStateOf<Shayari?>(null) }

    // Smart Back logic to prevent app exit from sub-pages
    val onBack: () -> Unit = {
        val lastKey = backStack.lastOrNull()
        if (backStack.size > 1) {
            backStack.removeAt(backStack.size - 1)
        } else if (lastKey != null && lastKey != NavKey.Home) {
            backStack.clear()
            backStack.add(NavKey.Home)
        }
    }

    NavDisplay(
        backStack = backStack,
        onBack = onBack,
        modifier = modifier
    ) { key ->
        NavEntry(key) {
            when (key) {
                NavKey.Admin -> {
                    AdminScreen(
                        viewModel = adminViewModel,
                        onBackClick = onBack,
                        onAddClick = { backStack.add(NavKey.AddShayari()) }
                    )
                }
                NavKey.Login -> {
                    LoginScreen(
                        viewModel = authViewModel,
                        onSignupClick = { backStack.add(NavKey.Signup) },
                        onSuccess = onBack
                    )
                }
                NavKey.Signup -> {
                    SignupScreen(
                        viewModel = authViewModel,
                        onLoginClick = onBack,
                        onSuccess = { 
                            if (backStack.size > 0) backStack.removeAt(backStack.size - 1)
                            onBack()
                        }
                    )
                }
                NavKey.Home -> {
                    val viewModel: HomeViewModel = viewModel { HomeViewModel(repository) }
                    HomeScreen(
                        viewModel = viewModel,
                        contentPadding = contentPadding,
                        currentUser = currentUser,
                        isAdmin = isAdmin,
                        onLoginClick = { backStack.add(NavKey.Login) },
                        onLogoutClick = { authViewModel.logout() },
                        onAdminClick = { backStack.add(NavKey.Admin) },
                        onEditClick = { editingShayari = it },
                        onDeleteClick = { adminViewModel.deleteShayari(it) },
                        onMenuClick = onMenuClick,
                        onCategoryClick = { category ->
                            AdsManager.showInterstitialAd(context) {
                                backStack.add(NavKey.CategoryDetail(category))
                            }
                        },
                        onShayariClick = { shayari ->
                            AdsManager.showInterstitialAd(context) {
                                backStack.add(
                                    NavKey.ShayariDetail(
                                        id = shayari.id,
                                        urdu = shayari.urdu,
                                        roman = shayari.roman,
                                        english = shayari.english,
                                        category = shayari.category,
                                        poet = shayari.poet,
                                        docId = shayari.docId
                                    )
                                )
                            }
                        },
                        onAddClick = {
                            if (currentUser == null) {
                                backStack.add(NavKey.Login)
                            } else {
                                backStack.add(NavKey.AddShayari())
                            }
                        },
                        onSeeAllCategoriesClick = { backStack.add(NavKey.Categories) },
                        onSearchClick = { 
                            // Navigate to Categories which has a better list overview for now
                            // OR create a dedicated Search screen
                            backStack.add(NavKey.CategoryDetail("Nayi Shayari"))
                        }
                    )
                }
                NavKey.Categories -> {
                    val viewModel: CategoriesViewModel = viewModel { CategoriesViewModel(repository) }
                    CategoriesScreen(
                        viewModel = viewModel,
                        contentPadding = contentPadding,
                        onMenuClick = onMenuClick,
                        onCategoryClick = { category ->
                            AdsManager.showInterstitialAd(context) {
                                backStack.add(NavKey.CategoryDetail(category))
                            }
                        },
                        onFavoritesClick = { backStack.add(NavKey.Saved) }
                    )
                }
                NavKey.PhotoShayari -> {
                    val viewModel: PhotoShayariViewModel = viewModel { PhotoShayariViewModel(repository) }
                    PhotoShayariScreen(
                        viewModel = viewModel,
                        contentPadding = contentPadding,
                        onMenuClick = onMenuClick
                    )
                }
                NavKey.AIMuse -> {
                    val viewModel: AIMuseViewModel = viewModel()
                    AIMuseScreen(
                        viewModel = viewModel,
                        contentPadding = contentPadding
                    )
                }
                NavKey.Saved -> {
                    val viewModel: SavedViewModel = viewModel { SavedViewModel(repository) }
                    SavedScreen(
                        viewModel = viewModel,
                        contentPadding = contentPadding,
                        onShayariClick = { shayari ->
                            backStack.add(
                                NavKey.ShayariDetail(
                                    id = shayari.id,
                                    urdu = shayari.urdu,
                                    roman = shayari.roman,
                                    english = shayari.english,
                                    category = shayari.category,
                                    poet = shayari.poet,
                                    docId = shayari.docId
                                )
                            )
                        }
                    )
                }
                NavKey.About -> {
                    AboutScreen(onBackClick = onBack)
                }
                NavKey.Privacy -> {
                    PrivacyScreen(onBackClick = onBack)
                }
                is NavKey.CategoryDetail -> {
                    val viewModel: ShayariListViewModel = viewModel { ShayariListViewModel(repository, key.categoryName) }
                    ShayariListScreen(
                        categoryName = key.categoryName,
                        viewModel = viewModel,
                        contentPadding = contentPadding,
                        isAdmin = isAdmin,
                        onShayariClick = { shayari ->
                            AdsManager.showInterstitialAd(context) {
                                backStack.add(
                                    NavKey.ShayariDetail(
                                        id = shayari.id,
                                        urdu = shayari.urdu,
                                        roman = shayari.roman,
                                        english = shayari.english,
                                        category = shayari.category,
                                        poet = shayari.poet,
                                        docId = shayari.docId
                                    )
                                )
                            }
                        },
                        onBackClick = onBack,
                        onAddClick = {
                            if (currentUser == null) {
                                backStack.add(NavKey.Login)
                            } else {
                                backStack.add(NavKey.AddShayari(initialCategory = key.categoryName))
                            }
                        },
                        onEditClick = { editingShayari = it },
                        onDeleteClick = { adminViewModel.deleteShayari(it) }
                    )
                }
                is NavKey.ShayariDetail -> {
                    val shayari = Shayari(
                        id = key.id,
                        urdu = key.urdu,
                        roman = key.roman,
                        english = key.english,
                        category = key.category,
                        poet = key.poet,
                        docId = key.docId
                    )
                    val viewModel: ShayariDetailViewModel = viewModel { ShayariDetailViewModel(repository, shayari) }
                    ShayariDetailScreen(
                        viewModel = viewModel,
                        contentPadding = contentPadding,
                        onBackClick = onBack
                    )
                }
                is NavKey.AddShayari -> {
                    val scope = rememberCoroutineScope()
                    AddShayariScreen(
                        initialCategory = key.initialCategory,
                        onBackClick = onBack,
                        onSaveClick = { urdu, roman, english, category, poet ->
                            scope.launch {
                                repository.saveUserShayari(urdu, roman, english, category, poet)
                                onBack()
                            }
                        }
                    )
                }
                else -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Unknown Screen")
                    }
                }
            }
        }
    }

    // Edit Dialog for Global Use
    editingShayari?.let { shayari ->
        EditShayariDialog(
            shayari = shayari,
            onDismiss = { editingShayari = null },
            onSave = { updatedShayari ->
                adminViewModel.updateShayari(shayari.urdu, updatedShayari)
                editingShayari = null
            }
        )
    }
}
