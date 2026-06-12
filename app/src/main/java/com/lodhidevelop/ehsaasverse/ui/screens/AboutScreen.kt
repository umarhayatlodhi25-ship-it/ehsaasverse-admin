package com.lodhidevelop.ehsaasverse.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lodhidevelop.ehsaasverse.ui.theme.PrimaryMaroon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About Us", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryMaroon)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "EhsaasVerse",
                style = MaterialTheme.typography.displaySmall,
                color = PrimaryMaroon,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "Version 1.0.0",
                style = MaterialTheme.typography.labelMedium,
                color = Color.Gray
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "EhsaasVerse is your personal vault for the world's most beautiful Urdu poetry. Our mission is to preserve the rich heritage of Urdu literature and make it accessible to everyone in a modern, elegant way.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            AboutSection(
                title = "AI Muse",
                description = "Powered by Google's Gemini AI, we help you compose your own verses and explore new poetic horizons."
            )
            
            AboutSection(
                title = "Cloud Mehfil",
                description = "Explore thousands of shayari entries across dozens of categories, synced in real-time from the cloud."
            )
            
            AboutSection(
                title = "Personal Vault",
                description = "Save your favorite verses and keep them close to your heart, even when you're offline."
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Text(
                text = "Developed by Lodhi Develop",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = PrimaryMaroon
            )
            Text(
                text = "© 2025 All Rights Reserved",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun AboutSection(title: String, description: String) {
    Column(
        modifier = Modifier.padding(vertical = 12.dp).fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = PrimaryMaroon
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = Color.DarkGray
        )
    }
}
