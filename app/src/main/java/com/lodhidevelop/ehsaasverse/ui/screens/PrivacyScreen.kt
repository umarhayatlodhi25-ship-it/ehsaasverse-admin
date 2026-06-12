package com.lodhidevelop.ehsaasverse.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lodhidevelop.ehsaasverse.ui.theme.PrimaryMaroon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyScreen(
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy Policy", color = Color.White) },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Your Privacy Matters",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = PrimaryMaroon
            )
            
            Text(
                text = "Last Updated: May 2024",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )

            PrivacySection(
                title = "1. Information Collection",
                content = "We collect minimal information required to provide our services. This includes account information if you sign up (email) and your favorite shayari preferences stored locally or in our cloud database."
            )

            PrivacySection(
                title = "2. Use of Services",
                content = "EhsaasVerse uses Google Firebase for authentication and cloud storage, and Google AdMob for displaying advertisements. These third-party services may collect information used to identify you."
            )

            PrivacySection(
                title = "3. AI Features",
                content = "The AI Muse feature is powered by Google's Gemini API. Prompts you enter to generate shayari are processed by Google's services to provide you with the generated content."
            )

            PrivacySection(
                title = "4. Data Security",
                content = "We value your trust in providing us your personal information, thus we are striving to use commercially acceptable means of protecting it. But remember that no method of transmission over the internet is 100% secure."
            )

            PrivacySection(
                title = "5. Changes to This Policy",
                content = "We may update our Privacy Policy from time to time. Thus, you are advised to review this page periodically for any changes."
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "If you have any questions, feel free to contact us through our website.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun PrivacySection(title: String, content: String) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = PrimaryMaroon
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.DarkGray
        )
    }
}
