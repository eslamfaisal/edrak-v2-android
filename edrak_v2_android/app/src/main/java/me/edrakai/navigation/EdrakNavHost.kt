package me.edrakai.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import me.edrakai.core.security.TokenManager
import me.edrakai.features.auth.presentation.LoginScreen
import me.edrakai.features.auth.presentation.RegisterScreen
import me.edrakai.features.digest.presentation.DigestScreen
import me.edrakai.features.home.presentation.HomeScreen
import me.edrakai.features.listening.presentation.ListeningScreen
import me.edrakai.features.lock.presentation.LockScreen
import me.edrakai.features.onboarding.presentation.OnboardingScreen
import me.edrakai.ui.theme.EdrakColors

sealed class EdrakRoute(val route: String) {
    data object Login      : EdrakRoute("login")
    data object Register   : EdrakRoute("register")
    data object Onboarding : EdrakRoute("onboarding")
    data object Lock       : EdrakRoute("lock")       // voice passphrase lock
    data object Home       : EdrakRoute("home")
    data object Listening  : EdrakRoute("listening")
    data object Digest     : EdrakRoute("digest")
}

private val BOTTOM_NAV_ROUTES = setOf(
    EdrakRoute.Home.route,
    EdrakRoute.Listening.route,
    EdrakRoute.Digest.route,
)

@Composable
fun EdrakNavHost(tokenManager: TokenManager) {
    /**
     * Navigation logic on cold start:
     *  - Not logged in              → Login
     *  - Logged in, no voice setup  → Onboarding
     *  - Logged in, voice complete  → Lock (voice passphrase gate)
     */
    val startDestination = remember {
        when {
            !tokenManager.isLoggedIn()           -> EdrakRoute.Login.route
            !tokenManager.isVoiceSetupComplete() -> EdrakRoute.Onboarding.route
            else                                 -> EdrakRoute.Lock.route
        }
    }

    val nav = rememberNavController()
    val backStackEntry by nav.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        containerColor = EdrakColors.DeepMidnightBlue,
        bottomBar = {
            if (currentRoute in BOTTOM_NAV_ROUTES) {
                EdrakBottomNav(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        nav.navigate(route) {
                            popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
        },
    ) { paddingValues ->
        NavHost(
            navController = nav,
            startDestination = startDestination,
            modifier = Modifier.padding(paddingValues),
        ) {
            // ── Auth ─────────────────────────────────────────────────────────

            composable(EdrakRoute.Login.route) {
                LoginScreen(
                    onNavigateToRegister   = { nav.navigate(EdrakRoute.Register.route) },
                    onNavigateToHome       = {
                        nav.navigate(EdrakRoute.Home.route) {
                            popUpTo(EdrakRoute.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToVoiceSetup = {
                        nav.navigate(EdrakRoute.Onboarding.route) {
                            popUpTo(EdrakRoute.Login.route) { inclusive = true }
                        }
                    },
                )
            }

            composable(EdrakRoute.Register.route) {
                RegisterScreen(
                    onNavigateToLogin      = { nav.popBackStack() },
                    onNavigateToVoiceSetup = {
                        nav.navigate(EdrakRoute.Onboarding.route) {
                            popUpTo(EdrakRoute.Login.route) { inclusive = true }
                        }
                    },
                )
            }

            // ── Onboarding ───────────────────────────────────────────────────

            composable(EdrakRoute.Onboarding.route) {
                OnboardingScreen(
                    onNavigateToHome = {
                        nav.navigate(EdrakRoute.Home.route) {
                            popUpTo(EdrakRoute.Onboarding.route) { inclusive = true }
                        }
                    },
                )
            }

            // ── Voice Lock ───────────────────────────────────────────────────

            composable(EdrakRoute.Lock.route) {
                LockScreen(
                    onUnlocked = {
                        nav.navigate(EdrakRoute.Home.route) {
                            popUpTo(EdrakRoute.Lock.route) { inclusive = true }
                        }
                    },
                    onLogout = {
                        nav.navigate(EdrakRoute.Login.route) {
                            popUpTo(EdrakRoute.Lock.route) { inclusive = true }
                        }
                    },
                )
            }

            // ── Main ─────────────────────────────────────────────────────────

            composable(EdrakRoute.Home.route) {
                HomeScreen(
                    onNavigateToListening = { nav.navigate(EdrakRoute.Listening.route) },
                    onNavigateToDigest    = { nav.navigate(EdrakRoute.Digest.route) },
                )
            }

            composable(EdrakRoute.Listening.route) {
                ListeningScreen(onNavigateBack = { nav.popBackStack() })
            }

            composable(EdrakRoute.Digest.route) {
                DigestScreen(onNavigateBack = { nav.popBackStack() })
            }
        }
    }
}

// ─── Bottom Navigation Bar ────────────────────────────────────────────────────

private data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
)

@Composable
private fun EdrakBottomNav(currentRoute: String?, onNavigate: (String) -> Unit) {
    val items = listOf(
        BottomNavItem(EdrakRoute.Home.route,      "Home",      Icons.Default.Home),
        BottomNavItem(EdrakRoute.Listening.route, "Listening", Icons.Default.Mic),
        BottomNavItem(EdrakRoute.Digest.route,    "Digest",    Icons.Default.Summarize),
    )

    NavigationBar(containerColor = Color(0xFF060A18), tonalElevation = 0.dp) {
        items.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick  = { onNavigate(item.route) },
                icon     = { Icon(item.icon, contentDescription = item.label, tint = if (selected) EdrakColors.NeonCyan else EdrakColors.Slate500) },
                label    = { Text(item.label, color = if (selected) EdrakColors.NeonCyan else EdrakColors.Slate500, fontSize = 11.sp) },
                colors   = NavigationBarItemDefaults.colors(indicatorColor = EdrakColors.NeonCyan.copy(alpha = 0.12f)),
            )
        }
    }
}
