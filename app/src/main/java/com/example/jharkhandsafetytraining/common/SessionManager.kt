package com.example.jharkhandsafetytraining.common

import android.content.Context

class SessionManager(context: Context) {

    private val prefs =
        context.applicationContext.getSharedPreferences("session", Context.MODE_PRIVATE)

    fun saveLogin(userId: Long, role: String, language: String) {
        prefs.edit()
            .putLong("user_id", userId)
            .putString("role", role)
            .putString("language", language)
            .apply()
    }

    fun isLoggedIn(): Boolean = prefs.getLong("user_id", -1L) != -1L

    fun userId(): Long = prefs.getLong("user_id", -1L)

    fun role(): String = prefs.getString("role", "WORKER") ?: "WORKER"

    fun language(): String = prefs.getString("language", "en") ?: "en"

    fun setLanguage(lang: String) {
        prefs.edit().putString("language", lang).apply()
    }

    fun logout() {
        prefs.edit().clear().apply()
    }
}