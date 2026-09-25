package com.example.jharkhandsafetytraining

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.jharkhandsafetytraining.auth.AuthViewModel
import com.example.jharkhandsafetytraining.auth.LoginScreen
import com.example.jharkhandsafetytraining.auth.RegisterScreen
import com.example.jharkhandsafetytraining.screens.HomeScreen
import com.example.jharkhandsafetytraining.screens.ModuleDetailScreen
import com.example.jharkhandsafetytraining.ui.theme.JharkhandSafetyTrainingTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JharkhandSafetyTrainingTheme {
                val navController = rememberNavController()
                val authViewModel: AuthViewModel = viewModel()
                val start = if (authViewModel.isLoggedIn()) "home" else "login"

                NavHost(navController = navController, startDestination = start) {
                    composable("login") {
                        LoginScreen(
                            viewModel = authViewModel,
                            onLoggedIn = {
                                navController.navigate("home") {
                                    popUpTo("login") { inclusive = true }
                                }
                            },
                            onGoToRegister = { navController.navigate("register") }
                        )
                    }

                    composable("register") {
                        RegisterScreen(
                            viewModel = authViewModel,
                            onRegistered = {
                                navController.navigate("home") {
                                    popUpTo("login") { inclusive = true }
                                }
                            },
                            onBackToLogin = { navController.popBackStack() }
                        )
                    }

                    composable("home") {
                        HomeScreen(
                            onModuleClick = { moduleId ->
                                navController.navigate("module_detail/$moduleId")
                            }
                        )
                    }

                    composable(
                        route = "module_detail/{moduleId}",
                        arguments = listOf(navArgument("moduleId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val moduleId = backStackEntry.arguments?.getString("moduleId") ?: ""
                        ModuleDetailScreen(
                            moduleId = moduleId,
                            onStartAR = {
                                // AR team contract connects here
                            },
                            onStartQuiz = {
                                // navController.navigate("quiz/$moduleId")
                            }
                        )
                    }
                }
            }
        }
    }
}