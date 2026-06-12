package com.lodhidevelop.ehsaasverse.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lodhidevelop.ehsaasverse.ui.theme.PrimaryMaroon
import com.lodhidevelop.ehsaasverse.ui.viewmodel.AuthState
import com.lodhidevelop.ehsaasverse.ui.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onSignupClick: () -> Unit,
    onSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val authState by viewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            onSuccess()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("EhsaasVerse", style = MaterialTheme.typography.headlineLarge, color = PrimaryMaroon, fontWeight = FontWeight.Bold)
        Text("Welcome Back", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
        
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (authState is AuthState.Loading) {
            CircularProgressIndicator(color = PrimaryMaroon)
        } else {
            Button(
                onClick = { viewModel.login(email, password) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryMaroon)
            ) {
                Text("Login", color = Color.White)
            }
        }

        if (authState is AuthState.Error) {
            Text((authState as AuthState.Error).message, color = Color.Red, modifier = Modifier.padding(top = 8.dp))
        }

        TextButton(onClick = onSignupClick) {
            Text("Don't have an account? Sign Up", color = PrimaryMaroon)
        }
    }
}

@Composable
fun SignupScreen(
    viewModel: AuthViewModel,
    onLoginClick: () -> Unit,
    onSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val authState by viewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            onSuccess()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("EhsaasVerse", style = MaterialTheme.typography.headlineLarge, color = PrimaryMaroon, fontWeight = FontWeight.Bold)
        Text("Create an Account", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (authState is AuthState.Loading) {
            CircularProgressIndicator(color = PrimaryMaroon)
        } else {
            Button(
                onClick = { viewModel.signup(email, password) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryMaroon)
            ) {
                Text("Sign Up", color = Color.White)
            }
        }

        if (authState is AuthState.Error) {
            Text((authState as AuthState.Error).message, color = Color.Red, modifier = Modifier.padding(top = 8.dp))
        }

        TextButton(onClick = onLoginClick) {
            Text("Already have an account? Login", color = PrimaryMaroon)
        }
    }
}
