package me.edrakai.features.home.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.edrakai.ui.theme.EdrakColors

@Composable
fun HomeScreen(
    onNavigateToListening: () -> Unit,
    onNavigateToDigest: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Home Screen — Phase 5A", color = EdrakColors.Slate400)
            Button(onClick = onNavigateToListening) { Text("Open Listening") }
            Button(onClick = onNavigateToDigest) { Text("Open Digest") }
        }
    }
}
