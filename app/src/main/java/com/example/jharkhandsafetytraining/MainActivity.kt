package com.example.jharkhandsafetytraining

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.jharkhandsafetytraining.auth.AuthViewModel
import com.example.jharkhandsafetytraining.auth.LoginScreen
import com.example.jharkhandsafetytraining.auth.RegisterScreen
import com.example.jharkhandsafetytraining.certificate.CertificateScreen
import com.example.jharkhandsafetytraining.data.AppDatabase
import com.example.jharkhandsafetytraining.quiz.QuizScreen
import com.example.jharkhandsafetytraining.screens.HomeScreen
import com.example.jharkhandsafetytraining.screens.ModuleDetailScreen
import com.example.jharkhandsafetytraining.ui.theme.JharkhandSafetyTrainingTheme

fun mapToQuizModuleId(raw: String): String = when (raw) {
    "MOD_FIRE" -> "fire"
    "MOD_GAS" -> "gas"
    "MOD_MACHINERY" -> "machinery"
    "MOD_PPE" -> "ppe"
    else -> raw.lowercase() // MOD_COLLAPSE, MOD_FLOOD, MOD_OXYGEN — no quiz content yet
}

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
                                navController.navigate("quiz/$moduleId")
                            }
                        )
                    }

                    composable(
                        route = "quiz/{moduleId}",
                        arguments = listOf(navArgument("moduleId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val rawModuleId = backStackEntry.arguments?.getString("moduleId") ?: ""
                        val quizModuleId = mapToQuizModuleId(rawModuleId)
                        val dao = remember { AppDatabase.getInstance(applicationContext).trainingDao() }
                        QuizScreen(
                            moduleId = quizModuleId,
                            userId = 1L, // TODO: replace with real logged-in user id from authViewModel
                            dao = dao,
                            onFinished = {
                                navController.navigate("certificate/$quizModuleId")
                            }
                        )
                    }

                    composable(
                        route = "certificate/{moduleId}",
                        arguments = listOf(navArgument("moduleId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val moduleId = backStackEntry.arguments?.getString("moduleId") ?: ""
                        val dao = remember { AppDatabase.getInstance(applicationContext).trainingDao() }
                        CertificateScreen(
                            moduleId = moduleId,
                            userId = 1L, // TODO: same placeholder, replace with real user id
                            dao = dao,
                            onDone = {
                                navController.navigate("home") {
                                    popUpTo("home") { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}