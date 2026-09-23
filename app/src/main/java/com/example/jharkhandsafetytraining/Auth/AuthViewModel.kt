package com.example.jharkhandsafetytraining.auth

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.jharkhandsafetytraining.common.SessionManager
import com.example.jharkhandsafetytraining.data.AppDatabase
import com.example.jharkhandsafetytraining.data.AuthRepository
import com.example.jharkhandsafetytraining.data.AuthResult
import kotlinx.coroutines.launch

class AuthViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = AuthRepository(AppDatabase.getInstance(app).userDao())
    private val session = SessionManager(app)

    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun isLoggedIn(): Boolean = session.isLoggedIn()

    fun clearError() { errorMessage = null }

    fun register(name: String, phone: String, pin: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            when (val result = repo.register(name, phone, pin, session.language())) {
                is AuthResult.Success -> {
                    session.saveLogin(result.user.id, result.user.role, result.user.language)
                    onSuccess()
                }
                is AuthResult.Error -> errorMessage = result.message
            }
            isLoading = false
        }
    }

    fun login(phone: String, pin: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            when (val result = repo.login(phone, pin)) {
                is AuthResult.Success -> {
                    session.saveLogin(result.user.id, result.user.role, result.user.language)
                    onSuccess()
                }
                is AuthResult.Error -> errorMessage = result.message
            }
            isLoading = false
        }
    }
}