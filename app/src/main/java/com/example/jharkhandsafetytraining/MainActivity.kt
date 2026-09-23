package com.example.jharkhandsafetytraining

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.jharkhandsafetytraining.auth.AuthViewModel
import com.example.jharkhandsafetytraining.auth.LoginScreen
import com.example.jharkhandsafetytraining.auth.RegisterScreen
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
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Home (coming next)")
                        }
                    }
                }
            }
        }
    }
}