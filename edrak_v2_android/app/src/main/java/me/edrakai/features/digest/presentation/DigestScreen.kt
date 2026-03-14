package me.edrakai.features.digest.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import me.edrakai.ui.theme.EdrakColors

@Composable
fun DigestScreen(onNavigateBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Daily Digest Screen — Phase 5C", color = EdrakColors.Slate400)
            TextButton(onClick = onNavigateBack) { Text("Back") }
        }
    }
}
