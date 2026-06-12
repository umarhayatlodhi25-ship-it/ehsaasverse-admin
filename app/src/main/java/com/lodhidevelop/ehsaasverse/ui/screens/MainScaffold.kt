package com.lodhidevelop.ehsaasverse.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Login
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseUser
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.NavKey as BaseNavKey
import com.lodhidevelop.ehsaasverse.R
import com.lodhidevelop.ehsaasverse.ui.navigation.NavKey
import com.lodhidevelop.ehsaasverse.ui.navigation.NavigationGraph
import com.lodhidevelop.ehsaasverse.ui.theme.PrimaryGold
import com.lodhidevelop.ehsaasverse.ui.theme.PrimaryMaroon
import kotlinx.coroutines.launch

@Composable
fun MainScaffold() {
    val backStack = rememberNavBackStack(NavKey.Home)
    val currentKey = backStack.lastOrNull() ?: NavKey.Home
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    val authViewModel: com.lodhidevelop.ehsaasverse.ui.viewmodel.AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val currentUser by authViewModel.currentUser.collectAsState()

    // Handle back press
    BackHandler(enabled = true) {
        if (drawerState.isOpen) {
            scope.launch { drawerState.close() }
        } else if (backStack.size > 1) {
            backStack.removeAt(backStack.size - 1)
        } else if (currentKey != NavKey.Home) {
            // If on another tab, go back to Home instead of exiting
            backStack.clear()
            backStack.add(NavKey.Home)
        } else {
            // On Home and back pressed, let the system handle it (exit)
            context.findActivity()?.finish()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = shouldShowBottomBar(currentKey),
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color.White,
                drawerShape = androidx.compose.foundation.shape.RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
                    Text(
                        text = stringResource(R.string.app_title),
                        style = MaterialTheme.typography.headlineMedium,
                        color = PrimaryMaroon,
                        fontWeight = FontWeight.Black
                    )
                    if (currentUser != null) {
                        Text(
                            text = currentUser?.email ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = PrimaryMaroon
                        )
                    } else {
                        Text(
                            text = "Express your emotions",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.LightGray.copy(alpha = 0.5f))
                
                val menuItems = mutableListOf(
                    DrawerItem(NavKey.Home, Icons.Rounded.Home, R.string.nav_home),
                    DrawerItem(NavKey.Categories, Icons.Rounded.AutoAwesomeMotion, R.string.nav_mehfil),
                    DrawerItem(NavKey.PhotoShayari, Icons.Rounded.Image, R.string.nav_poets),
                    DrawerItem(NavKey.AIMuse, Icons.Rounded.AutoAwesome, R.string.nav_ai_muse),
                    DrawerItem(NavKey.Saved, Icons.Rounded.Favorite, R.string.nav_vault),
                    DrawerItem(NavKey.About, Icons.Rounded.Info, R.string.nav_about),
                    DrawerItem(NavKey.Privacy, Icons.Rounded.Security, R.string.nav_privacy)
                )

                menuItems.forEach { item ->
                    NavigationDrawerItem(
                        label = { Text(stringResource(item.labelRes)) },
                        selected = currentKey == item.key,
                        onClick = {
                            scope.launch { drawerState.close() }
                            if (currentKey != item.key) {
                                backStack.clear()
                                backStack.add(item.key)
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = null) },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = PrimaryMaroon.copy(alpha = 0.1f),
                            selectedIconColor = PrimaryMaroon,
                            selectedTextColor = PrimaryMaroon,
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray
                        )
                    )
                }

                if (currentUser != null) {
                    NavigationDrawerItem(
                        label = { Text("Logout") },
                        selected = false,
                        onClick = {
                            scope.launch { 
                                drawerState.close()
                                authViewModel.logout()
                            }
                        },
                        icon = { Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = null) },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                        colors = NavigationDrawerItemDefaults.colors(
                            unselectedIconColor = Color.Red,
                            unselectedTextColor = Color.Red
                        )
                    )
                } else {
                    NavigationDrawerItem(
                        label = { Text("Login") },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            backStack.add(NavKey.Login)
                        },
                        icon = { Icon(Icons.AutoMirrored.Rounded.Login, contentDescription = null) },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.LightGray.copy(alpha = 0.5f))
                
                NavigationDrawerItem(
                    label = { Text("Share App") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        val sendIntent = android.content.Intent().apply {
                            action = android.content.Intent.ACTION_SEND
                            putExtra(android.content.Intent.EXTRA_TEXT, "Check out EhsaasVerse Shayari App!")
                            type = "text/plain"
                        }
                        context.startActivity(android.content.Intent.createChooser(sendIntent, null))
                    },
                    icon = { Icon(Icons.Rounded.Share, contentDescription = null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                
                NavigationDrawerItem(
                    label = { Text("Rate App") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        try {
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("market://details?id=${context.packageName}"))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}"))
                            context.startActivity(intent)
                        }
                    },
                    icon = { Icon(Icons.Rounded.Star, contentDescription = null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    ) {
        Scaffold(
            bottomBar = {
                if (shouldShowBottomBar(currentKey)) {
                    Column(modifier = Modifier.background(Color.Transparent)) {
                        BannerAd(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
                        NavigationBar(
                            containerColor = PrimaryMaroon,
                            tonalElevation = 8.dp,
                            windowInsets = NavigationBarDefaults.windowInsets
                        ) {
                            val items = listOf(
                                BottomNavItem(NavKey.Home, Icons.Rounded.Home, R.string.nav_home),
                                BottomNavItem(NavKey.Categories, Icons.Rounded.AutoAwesomeMotion, R.string.nav_mehfil),
                                BottomNavItem(NavKey.PhotoShayari, Icons.Rounded.Image, R.string.nav_poets),
                                BottomNavItem(NavKey.AIMuse, Icons.Rounded.AutoAwesome, R.string.nav_ai_muse),
                                BottomNavItem(NavKey.Saved, Icons.Rounded.Favorite, R.string.nav_vault)
                            )

                            items.forEach { item ->
                                val isAI = item.key == NavKey.AIMuse
                                val label = stringResource(item.labelRes)
                                NavigationBarItem(
                                    selected = currentKey == item.key,
                                    alwaysShowLabel = !isAI,
                                    onClick = {
                                        if (currentKey != item.key) {
                                            backStack.clear()
                                            backStack.add(item.key)
                                        }
                                    },
                                    icon = {
                                        if (isAI) {
                                            Surface(
                                                shape = CircleShape,
                                                color = PrimaryGold,
                                                modifier = Modifier.size(42.dp),
                                                tonalElevation = 4.dp
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(item.icon, contentDescription = label, tint = PrimaryMaroon, modifier = Modifier.size(20.dp))
                                                }
                                            }
                                        } else {
                                            Icon(item.icon, contentDescription = label)
                                        }
                                    },
                                    label = { 
                                        if (!isAI) {
                                            Text(
                                                text = label,
                                                fontSize = 10.sp,
                                                maxLines = 1
                                            )
                                        }
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = PrimaryGold,
                                        selectedTextColor = PrimaryGold,
                                        unselectedIconColor = Color.White.copy(alpha = 0.6f),
                                        unselectedTextColor = Color.White.copy(alpha = 0.6f),
                                        indicatorColor = Color.White.copy(alpha = 0.1f)
                                    )
                                )
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            NavigationGraph(
                backStack = backStack,
                modifier = Modifier.fillMaxSize(),
                contentPadding = innerPadding,
                onMenuClick = { scope.launch { drawerState.open() } }
            )
        }
    }
}

private fun shouldShowBottomBar(key: BaseNavKey): Boolean {
    return key is NavKey.Home || 
           key is NavKey.Categories || 
           key is NavKey.PhotoShayari ||
           key is NavKey.AIMuse || 
           key is NavKey.Saved ||
           key is NavKey.CategoryDetail ||
           key is NavKey.ShayariDetail
}

data class BottomNavItem(
    val key: NavKey,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val labelRes: Int
)

data class DrawerItem(
    val key: NavKey,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val labelRes: Int
)

fun Context.findActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
