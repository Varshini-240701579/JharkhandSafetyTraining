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

    fun isLoggedIn(): Boolean = session.getUserId() != null

    fun login(phone: String, pin: String, onSuccess: () -> Unit) {
        errorMessage = null
        if (phone.length != 10 || pin.length != 4) {
            errorMessage = "Enter a valid 10-digit phone and 4-digit PIN"
            return
        }
        isLoading = true
        viewModelScope.launch {
            when (val result = repo.login(phone, pin)) {
                is AuthResult.Success -> {
                    session.saveUserId(result.user.id)
                    isLoading = false
                    onSuccess()
                }
                is AuthResult.Failure -> {
                    isLoading = false
                    errorMessage = result.message
                }
            }
        }
    }

    fun register(name: String, phone: String, pin: String, language: String, onSuccess: () -> Unit) {
        errorMessage = null
        if (name.isBlank() || phone.length != 10 || pin.length != 4) {
            errorMessage = "Fill all fields correctly"
            return
        }
        isLoading = true
        viewModelScope.launch {
            when (val result = repo.register(name, phone, pin, language)) {
                is AuthResult.Success -> {
                    session.saveUserId(result.user.id)
                    isLoading = false
                    onSuccess()
                }
                is AuthResult.Failure -> {
                    isLoading = false
                    errorMessage = result.message
                }
            }
        }
    }

    fun clearError() {
        errorMessage = null
    }
}