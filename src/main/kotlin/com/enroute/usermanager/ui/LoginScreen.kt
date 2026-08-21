package com.enroute.usermanager.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.enroute.usermanager.api.AdminApiClient
import com.enroute.usermanager.api.ApiResult
import com.enroute.usermanager.theme.EnrouteColors
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    client: AdminApiClient,
    initialBaseUrl: String,
    onLoggedIn: (baseUrl: String) -> Unit
) {
    var baseUrl by remember { mutableStateOf(initialBaseUrl) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun submit() {
        if (username.isBlank() || password.isBlank() || baseUrl.isBlank()) {
            errorMessage = "Server URL, username, and password are all required"
            return
        }

        errorMessage = null
        isLoading = true
        client.updateBaseUrl(baseUrl)

        scope.launch {
            when (val result = client.login(username.trim(), password)) {
                is ApiResult.Success -> {
                    isLoading = false
                    onLoggedIn(baseUrl)
                }
                is ApiResult.Failure -> {
                    isLoading = false
                    errorMessage = result.message
                }
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.width(420.dp),
            colors = CardDefaults.cardColors(containerColor = EnrouteColors.Surface)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Enroute User Manager",
                    style = MaterialTheme.typography.headlineSmall,
                    color = EnrouteColors.TextPrimary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Sign in with an admin or moderator account",
                    style = MaterialTheme.typography.bodySmall,
                    color = EnrouteColors.TextSecondary
                )

                Spacer(Modifier.height(24.dp))

                OutlinedTextField(
                    value = baseUrl,
                    onValueChange = { baseUrl = it },
                    label = { Text("Server URL") },
                    placeholder = { Text("https://forum.example.com") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )

                errorMessage?.let {
                    Spacer(Modifier.height(12.dp))
                    Text(it, color = EnrouteColors.Danger, style = MaterialTheme.typography.bodySmall)
                }

                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = ::submit,
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = EnrouteColors.AccentPrimary)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Sign in", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
