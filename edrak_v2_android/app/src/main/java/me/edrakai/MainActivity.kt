package me.edrakai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import me.edrakai.core.security.TokenManager
import me.edrakai.navigation.EdrakNavHost
import me.edrakai.ui.theme.EdrakTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EdrakTheme {
                EdrakNavHost(tokenManager = tokenManager)
            }
        }
    }
}
