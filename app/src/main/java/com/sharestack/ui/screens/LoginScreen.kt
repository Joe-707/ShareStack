package com.sharestack.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sharestack.ui.theme.ShareStackTheme

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit = {},
    onNavigateToHome: (email: String, password: String) -> Unit = { _, _ -> }
) {
    // ✅ Clear fields when screen is first shown
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loginError by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(all = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Logo / Title
        Text(
            text = "ShareStack",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Pool together. Invest smarter.",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Show error message if login fails
        if (loginError != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = loginError!!,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(all = 12.dp),
                    fontSize = 14.sp
                )
            }
        }

        // Email Input Field
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                loginError = null
            },
            label = { Text("Email Address") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password Input Field
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                loginError = null
            },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Login Button
        Button(
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    loginError = "Please enter both email and password"
                } else {
                    isLoading = true
                    loginError = null
                    onNavigateToHome(email, password)
                }
            },
            enabled = email.isNotBlank() && password.isNotBlank() && !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            if (isLoading) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Logging in...", fontSize = 18.sp)
                }
            } else {
                Text("Login", fontSize = 18.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = onNavigateToRegister,
            enabled = !isLoading
        ) {
            Text("Don't have an account? Create one")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewLoginScreen() {
    ShareStackTheme {
        LoginScreen()
    }
}