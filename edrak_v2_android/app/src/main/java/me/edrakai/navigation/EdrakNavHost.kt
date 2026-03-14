package me.edrakai.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import me.edrakai.features.auth.presentation.LoginScreen
import me.edrakai.features.auth.presentation.RegisterScreen
import me.edrakai.features.digest.presentation.DigestScreen
import me.edrakai.features.home.presentation.HomeScreen
import me.edrakai.features.listening.presentation.ListeningScreen
import me.edrakai.features.onboarding.presentation.OnboardingScreen

sealed class EdrakRoute(val route: String) {
    data object Login : EdrakRoute("login")
    data object Register : EdrakRoute("register")
    data object Onboarding : EdrakRoute("onboarding")
    data object Home : EdrakRoute("home")
    data object Listening : EdrakRoute("listening")
    data object Digest : EdrakRoute("digest")
}

@Composable
fun EdrakNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = EdrakRoute.Login.route,
    ) {
        composable(EdrakRoute.Login.route) {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(EdrakRoute.Register.route) },
                onNavigateToOnboarding = {
                    navController.navigate(EdrakRoute.Onboarding.route) {
                        popUpTo(EdrakRoute.Login.route) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(EdrakRoute.Home.route) {
                        popUpTo(EdrakRoute.Login.route) { inclusive = true }
                    }
                },
            )
        }

        composable(EdrakRoute.Register.route) {
            RegisterScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToOnboarding = {
                    navController.navigate(EdrakRoute.Onboarding.route) {
                        popUpTo(EdrakRoute.Login.route) { inclusive = true }
                    }
                },
            )
        }

        composable(EdrakRoute.Onboarding.route) {
            OnboardingScreen(
                onNavigateToHome = {
                    navController.navigate(EdrakRoute.Home.route) {
                        popUpTo(EdrakRoute.Onboarding.route) { inclusive = true }
                    }
                },
            )
        }

        composable(EdrakRoute.Home.route) {
            HomeScreen(
                onNavigateToListening = { navController.navigate(EdrakRoute.Listening.route) },
                onNavigateToDigest = { navController.navigate(EdrakRoute.Digest.route) },
            )
        }

        composable(EdrakRoute.Listening.route) {
            ListeningScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable(EdrakRoute.Digest.route) {
            DigestScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }
    }
}
