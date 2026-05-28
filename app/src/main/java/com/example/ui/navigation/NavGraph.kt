package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ForgotPasswordScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.SignupScreen
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.AuthNavigationEvent
import com.example.ui.viewmodel.NotesViewModel
import kotlinx.coroutines.flow.collect

object Screen {
    const val Login = "login"
    const val Signup = "signup"
    const val ForgotPassword = "forgot_password"
    const val Dashboard = "dashboard"
}

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel,
    notesViewModel: NotesViewModel,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    // Determine the start destination based on whether the user is logged in
    val startDest = if (authViewModel.isUserLoggedIn()) Screen.Dashboard else Screen.Login

    // Listen to shared auth VM events to handle navigation out-of-band cleanly
    LaunchedEffect(key1 = true) {
        authViewModel.navigationEvents.collect { event ->
            when (event) {
                is AuthNavigationEvent.NavigateToDashboard -> {
                    navController.navigate(Screen.Dashboard) {
                        popUpTo(Screen.Login) { inclusive = true }
                        launchSingleTop = true
                    }
                }
                is AuthNavigationEvent.NavigateToLogin -> {
                    navController.navigate(Screen.Login) {
                        popUpTo(Screen.Dashboard) { inclusive = true }
                        launchSingleTop = true
                    }
                }
                is AuthNavigationEvent.NavigateToLoginWithSuccess -> {
                    val popped = navController.popBackStack(Screen.Login, inclusive = false)
                    if (!popped) {
                        navController.navigate(Screen.Login) {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDest,
        modifier = modifier
    ) {
        composable(Screen.Login) {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToSignup = {
                    navController.navigate(Screen.Signup) {
                        launchSingleTop = true
                    }
                },
                onNavigateToForgotPassword = {
                    authViewModel.clearResetForm()
                    navController.navigate(Screen.ForgotPassword) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Screen.Signup) {
            SignupScreen(
                viewModel = authViewModel,
                onNavigateToLogin = {
                    val popped = navController.popBackStack(Screen.Login, inclusive = false)
                    if (!popped) {
                        navController.navigate(Screen.Login) {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }

        composable(Screen.ForgotPassword) {
            ForgotPasswordScreen(
                viewModel = authViewModel,
                onNavigateBack = {
                    val popped = navController.popBackStack(Screen.Login, inclusive = false)
                    if (!popped) {
                        navController.navigate(Screen.Login) {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }

        composable(Screen.Dashboard) {
            DashboardScreen(
                authViewModel = authViewModel,
                notesViewModel = notesViewModel,
                onLogout = {
                    navController.navigate(Screen.Login) {
                        popUpTo(Screen.Dashboard) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}
