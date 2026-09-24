package com.example.jharkhandsafetytraining.data

sealed class AuthResult {
    data class Success(val user: User) : AuthResult()
    data class Failure(val message: String) : AuthResult()
}