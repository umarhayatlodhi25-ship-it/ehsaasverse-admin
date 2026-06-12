package com.lodhidevelop.ehsaasverse.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.lodhidevelop.ehsaasverse.data.model.Shayari
import com.lodhidevelop.ehsaasverse.data.model.PhotoShayari
import com.lodhidevelop.ehsaasverse.ui.theme.PrimaryMaroon
import com.lodhidevelop.ehsaasverse.ui.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    viewModel: AdminViewModel,
    onBackClick: () -> Unit,
    onAddClick: () -> Unit
) {
    val shayariList by viewModel.allShayari.collectAsState()
    val photoList by viewModel.allPhotos.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val status by viewModel.generationStatus.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    var selectedCategory by remember { mutableStateOf("Love") }
    val categories = listOf(
        "Love", "Sad", "Motivation", "Friendship", "Yaad", "Zindagi", 
        "Khwab", "Intezar", "Dard", "Dua", "Barish", "Tanhai", 
        "Bewafa", "Mashhoor", "Judai", "Aansu"
    )

    var editingShayari by remember { mutableStateOf<Shayari?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Panel", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null, tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadAllData() }) {
                        Icon(Icons.Rounded.Refresh, contentDescription = "Refresh", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryMaroon)
            )
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = onAddClick,
                    containerColor = PrimaryMaroon,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = "Post Shayari", modifier = Modifier.size(28.dp))
                }
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            
            TabRow(selectedTabIndex = selectedTab, containerColor = Color.White, contentColor = PrimaryMaroon) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Text Shayari") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Photo Shayari") })
            }

            if (selectedTab == 0) {
                // Bulk Generation Section for Text
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PrimaryMaroon.copy(alpha = 0.05f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = PrimaryMaroon)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("AI Bulk Generator", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(categories) { cat ->
                                FilterChip(
                                    selected = selectedCategory == cat,
                                    onClick = { selectedCategory = cat },
                                    label = { Text(cat) }
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        if (isGenerating) {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = PrimaryMaroon)
                            Text(status, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 4.dp))
                        } else {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { viewModel.generateBulkShayari(selectedCategory, 10) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryMaroon)
                                ) {
                                    Text("Generate 10 AI")
                                }
                                Button(
                                    onClick = { viewModel.clearCategory(selectedCategory) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                                ) {
                                    Text("Clear $selectedCategory")
                                }
                            }
                        }
                    }
                }

                if (isLoading) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryMaroon)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text("Text Shayari Inventory (${shayariList.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        
                        items(shayariList) { shayari ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(shayari.urdu, style = MaterialTheme.typography.bodyLarge)
                                        Text("By: ${shayari.poet} | Cat: ${shayari.category}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    }
                                    Row {
                                        IconButton(onClick = { editingShayari = shayari }) {
                                            Icon(Icons.Rounded.Edit, contentDescription = "Edit", tint = PrimaryMaroon)
                                        }
                                        IconButton(onClick = { viewModel.deleteShayari(shayari) }) {
                                            Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = Color.Red)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Photo Shayari Management
                val context = androidx.compose.ui.platform.LocalContext.current
                val launcher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.GetContent()
                ) { uri ->
                    uri?.let { viewModel.uploadPhotoShayari(it, context, selectedCategory) }
                }

                Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
                    Text("Add Photo Shayari", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text("Selected Category for Upload: $selectedCategory", style = MaterialTheme.typography.bodySmall, color = PrimaryMaroon)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 8.dp)) {
                        items(categories) { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat) }
                            )
                        }
                    }
                    
                    Button(
                        onClick = { launcher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryMaroon)
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Pick & Upload to $selectedCategory")
                    }

                    if (isLoading) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), color = PrimaryMaroon)
                        Text(status, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 4.dp))
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text("Bulk URL Add for $selectedCategory", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    var bulkUrls by remember { mutableStateOf("") }
                    OutlinedTextField(
                        value = bulkUrls,
                        onValueChange = { bulkUrls = it },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        placeholder = { Text("Enter direct image URLs (one per line)...") }
                    )
                    Button(
                        onClick = {
                            val list = bulkUrls.split("\n").filter { it.isNotBlank() }
                            list.forEach { viewModel.addPhotoShayari(it.trim(), selectedCategory) }
                            bulkUrls = ""
                        },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                    ) {
                        Text("Add All to $selectedCategory")
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text("Photo Inventory (${photoList.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // We can't use LazyColumn inside a verticalScroll Column directly without height
                    Box(modifier = Modifier.height(400.dp)) {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(photoList) { photo ->
                                Card(modifier = Modifier.fillMaxWidth()) {
                                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Text("${photo.category}: ${photo.imageUrl}", modifier = Modifier.weight(1f), maxLines = 1, style = MaterialTheme.typography.labelSmall)
                                        IconButton(onClick = { viewModel.deletePhotoShayari(photo.id) }) {
                                            Icon(Icons.Rounded.Delete, contentDescription = null, tint = Color.Red)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Edit Dialog
    editingShayari?.let { shayari ->
        EditShayariDialog(
            shayari = shayari,
            onDismiss = { editingShayari = null },
            onSave = { updatedShayari ->
                viewModel.updateShayari(shayari.urdu, updatedShayari)
                editingShayari = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditShayariDialog(
    shayari: Shayari,
    onDismiss: () -> Unit,
    onSave: (Shayari) -> Unit
) {
    var urdu by remember { mutableStateOf(shayari.urdu) }
    var roman by remember { mutableStateOf(shayari.roman) }
    var english by remember { mutableStateOf(shayari.english) }
    var category by remember { mutableStateOf(shayari.category) }
    var poet by remember { mutableStateOf(shayari.poet) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.9f),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Edit Shayari", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                
                OutlinedTextField(value = urdu, onValueChange = { urdu = it }, label = { Text("Urdu") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = roman, onValueChange = { roman = it }, label = { Text("Roman") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = english, onValueChange = { english = it }, label = { Text("English") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = poet, onValueChange = { poet = it }, label = { Text("Poet") }, modifier = Modifier.fillMaxWidth())

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { 
                            onSave(shayari.copy(urdu = urdu, roman = roman, english = english, category = category, poet = poet)) 
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryMaroon)
                    ) {
                        Text("Save Changes")
                    }
                }
            }
        }
    }
}
