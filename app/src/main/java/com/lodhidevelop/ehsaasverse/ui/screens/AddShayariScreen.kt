package com.lodhidevelop.ehsaasverse.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lodhidevelop.ehsaasverse.ui.theme.AppGreen
import com.lodhidevelop.ehsaasverse.ui.theme.PrimaryMaroon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddShayariScreen(
    initialCategory: String? = null,
    onBackClick: () -> Unit,
    onSaveClick: (urdu: String, roman: String, english: String, category: String, poet: String) -> Unit
) {
    var urdu by remember { mutableStateOf("") }
    var roman by remember { mutableStateOf("") }
    var english by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(initialCategory ?: "") }
    var poet by remember { mutableStateOf("") }

    val categories = listOf(
        "Love", "Sad", "Motivation", "Friendship", "Yaad", "Zindagi", 
        "Khwab", "Intezar", "Dard", "Dua", "Barish", "Tanhai", 
        "Bewafa", "Mashhoor", "Judai", "Aansu"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Shayari", color = Color.White) },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Select Category", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    FilterChip(
                        selected = category == cat,
                        onClick = { category = cat },
                        label = { Text(cat) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryMaroon,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Or Type New Category") },
                modifier = Modifier.fillMaxWidth()
            )

            Divider()

            OutlinedTextField(
                value = urdu,
                onValueChange = { urdu = it },
                label = { Text("Urdu Shayari") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            OutlinedTextField(
                value = roman,
                onValueChange = { roman = it },
                label = { Text("Roman Urdu") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = english,
                onValueChange = { english = it },
                label = { Text("English Translation") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = poet,
                onValueChange = { poet = it },
                label = { Text("Poet Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    if (urdu.isNotBlank() && category.isNotBlank()) {
                        onSaveClick(urdu, roman, english, category, if (poet.isBlank()) "Unknown" else poet)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryMaroon),
                enabled = urdu.isNotBlank() && category.isNotBlank()
            ) {
                Text("Post Shayari", color = Color.White)
            }
        }
    }
}
