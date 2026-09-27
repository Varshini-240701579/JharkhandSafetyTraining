package com.example.jharkhandsafetytraining.common

import android.content.Context
import androidx.core.content.edit

class SessionManager(context: Context) {

    private val prefs = context.getSharedPreferences("session_prefs", Context.MODE_PRIVATE)

    fun saveUserId(userId: Long) {
        prefs.edit { putLong(KEY_USER_ID, userId) }
    }

    fun getUserId(): Long? {
        val id = prefs.getLong(KEY_USER_ID, -1L)
        return if (id == -1L) null else id
    }

    fun saveLanguage(languageCode: String) {
        prefs.edit { putString(KEY_LANGUAGE, languageCode) }
    }

    fun getLanguage(default: String = "hi"): String {
        return prefs.getString(KEY_LANGUAGE, default) ?: default
    }

    fun clearSession() {
        prefs.edit { remove(KEY_USER_ID) }
    }

    companion object {
        private const val KEY_USER_ID = "user_id"
        private const val KEY_LANGUAGE = "user_language"
    }
}