package com.lodhidevelop.ehsaasverse.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lodhidevelop.ehsaasverse.ui.viewmodel.AIMuseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIMuseScreen(
    viewModel: AIMuseViewModel,
    contentPadding: PaddingValues = PaddingValues()
) {
    var prompt by remember { mutableStateOf("") }
    val response by viewModel.uiState.collectAsState()
    
    Scaffold(
        modifier = Modifier.padding(bottom = contentPadding.calculateBottomPadding()),
        topBar = {
            TopAppBar(
                title = { Text("AI Muse") }
            )
        },
        bottomBar = {
            BannerAd()
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = prompt,
                onValueChange = { prompt = it },
                label = { Text("Apni Baat Likhein...") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {
                    viewModel.generateShayari(prompt)
                }
            ) {
                Text("Generate Shayari")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(text = response)
        }
    }
}
