package me.edrakai.features.auth.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.edrakai.ui.theme.EdrakColors

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onNavigateToOnboarding: () -> Unit,
    onNavigateToHome: () -> Unit,
) {
    // Full implementation in Prompt 1B
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Edrak V2", style = MaterialTheme.typography.headlineLarge, color = EdrakColors.Primary)
            Text("Login Screen — Phase 1B", color = EdrakColors.Slate400)
            Button(onClick = onNavigateToHome) { Text("Go to Home (dev)") }
            TextButton(onClick = onNavigateToRegister) { Text("Register") }
        }
    }
}

@Composable
fun RegisterScreen(
    onNavigateBack: () -> Unit,
    onNavigateToOnboarding: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Register Screen — Phase 1B", color = EdrakColors.Slate400)
            Button(onClick = onNavigateToOnboarding) { Text("Go to Onboarding (dev)") }
            TextButton(onClick = onNavigateBack) { Text("Back to Login") }
        }
    }
}
