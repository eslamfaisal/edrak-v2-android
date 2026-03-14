package me.edrakai.features.onboarding.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import me.edrakai.ui.theme.EdrakColors

@Composable
fun OnboardingScreen(onNavigateToHome: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Voice Setup — Phase 1C", color = EdrakColors.Slate400)
            Button(onClick = onNavigateToHome) { Text("Skip to Home (dev)") }
        }
    }
}
