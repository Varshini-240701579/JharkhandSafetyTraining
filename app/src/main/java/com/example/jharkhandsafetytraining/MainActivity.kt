package com.example.jharkhandsafetytraining

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.jharkhandsafetytraining.auth.AuthViewModel
import com.example.jharkhandsafetytraining.auth.LoginScreen
import com.example.jharkhandsafetytraining.auth.RegisterScreen
import com.example.jharkhandsafetytraining.quiz.QuizViewModel
import com.example.jharkhandsafetytraining.screens.CertificateScreen
import com.example.jharkhandsafetytraining.screens.HomeScreen
import com.example.jharkhandsafetytraining.screens.ModuleDetailScreen
import com.example.jharkhandsafetytraining.screens.QuizScreen
import com.example.jharkhandsafetytraining.screens.VerifierScreen
import com.example.jharkhandsafetytraining.ui.theme.JharkhandSafetyTrainingTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JharkhandSafetyTrainingTheme {
                val navController = rememberNavController()
                val authViewModel: AuthViewModel = viewModel()
                val quizViewModel: QuizViewModel = viewModel()
                var pendingVerifierPayload by remember { mutableStateOf<String?>(null) }

                val startDestination = if (authViewModel.isLoggedIn()) {
                    Screen.Home.route
                } else {
                    Screen.Login.route
                }

                NavHost(navController = navController, startDestination = startDestination) {
                    composable(Screen.Login.route) {
                        LoginScreen(
                            viewModel = authViewModel,
                            onLoggedIn = {
                                quizViewModel.refreshUserAndProgress()
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
                                }
                            },
                            onGoToRegister = {
                                navController.navigate(Screen.Register.route)
                            }
                        )
                    }

                    composable(Screen.Register.route) {
                        RegisterScreen(
                            viewModel = authViewModel,
                            onRegistered = {
                                quizViewModel.refreshUserAndProgress()
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
                                }
                            },
                            onBackToLogin = {
                                navController.popBackStack()
                            }
                        )
                    }

                    composable(Screen.Home.route) {
                        HomeScreen(
                            onModuleClick = { moduleId ->
                                navController.navigate(Screen.ModuleDetail.passModuleId(moduleId))
                            },
                            onOpenCertificate = {
                                navController.navigate(Screen.Certificate.route)
                            },
                            onOpenVerifier = {
                                pendingVerifierPayload = null
                                navController.navigate(Screen.Verifier.route)
                            },
                            onLogout = {
                                authViewModel.logout()
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(Screen.Home.route) { inclusive = true }
                                }
                            }
                        )
                    }

                    composable(
                        route = Screen.ModuleDetail.route,
                        arguments = listOf(navArgument("moduleId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val moduleId = backStackEntry.arguments?.getString("moduleId") ?: ""
                        ModuleDetailScreen(
                            moduleId = moduleId,
                            onStartAR = {
                                // AR team contract connects here
                            },
                            onStartQuiz = {
                                navController.navigate(Screen.Quiz.passModuleId(moduleId))
                            },
                            onBack = {
                                navController.popBackStack()
                            }
                        )
                    }

                    composable(
                        route = Screen.Quiz.route,
                        arguments = listOf(navArgument("moduleId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val moduleId = backStackEntry.arguments?.getString("moduleId") ?: ""
                        QuizScreen(
                            moduleId = moduleId,
                            userLanguage = quizViewModel.currentUser?.language ?: "hi",
                            quizViewModel = quizViewModel,
                            onQuizPassed = { _, _ ->
                                navController.popBackStack(Screen.Home.route, inclusive = false)
                            },
                            onBackToModule = {
                                navController.popBackStack()
                            },
                            onOpenCertificate = {
                                navController.navigate(Screen.Certificate.route)
                            }
                        )
                    }

                    composable(Screen.Certificate.route) {
                        CertificateScreen(
                            onBack = {
                                navController.popBackStack()
                            },
                            onOpenVerifier = { signedPayload ->
                                pendingVerifierPayload = signedPayload
                                navController.navigate(Screen.Verifier.route)
                            }
                        )
                    }

                    composable(Screen.Verifier.route) {
                        VerifierScreen(
                            initialPayload = pendingVerifierPayload,
                            onBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }
    }
}