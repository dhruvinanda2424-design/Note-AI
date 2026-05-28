package com.example.data.local

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "notes_user_session"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_JWT_TOKEN = "jwt_token"
        private const val KEY_REMEMBER_ME = "remember_me"
        private const val KEY_DARK_MODE = "dark_mode_enabled"
    }

    fun isDarkModeEnabled(systemDefault: Boolean): Boolean {
        return prefs.getBoolean(KEY_DARK_MODE, systemDefault)
    }

    fun setDarkModeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
    }

    fun saveSession(email: String, name: String, token: String, rememberMe: Boolean) {
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_USER_EMAIL, email)
            putString(KEY_USER_NAME, name)
            putString(KEY_JWT_TOKEN, token)
            putBoolean(KEY_REMEMBER_ME, rememberMe)
            apply()
        }
    }

    fun isUserLoggedIn(): Boolean {
        // If not remember me, we can optionally clear/check, but typically keep standard stay authenticated unless logged out
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false) && getCurrentUserEmail() != null
    }

    fun getCurrentUserEmail(): String? {
        return prefs.getString(KEY_USER_EMAIL, null)
    }

    fun getCurrentUserName(): String {
        return prefs.getString(KEY_USER_NAME, "User") ?: "User"
    }

    fun getJwtToken(): String? {
        return prefs.getString(KEY_JWT_TOKEN, null)
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
