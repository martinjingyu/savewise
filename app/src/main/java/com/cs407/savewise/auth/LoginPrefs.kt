package com.cs407.savewise.auth

import android.content.Context

data class SavedLogin(
    val email: String = "",
    val password: String = "",
    val rememberPassword: Boolean = false
)

object LoginPrefs {

    private const val PREFS_NAME = "login_prefs"
    private const val KEY_EMAIL = "email"
    private const val KEY_PASSWORD = "password"
    private const val KEY_REMEMBER_PASSWORD = "remember_password"

    fun load(context: Context): SavedLogin {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // Always load saved email (if any)
        val email = prefs.getString(KEY_EMAIL, "") ?: ""

        val rememberPassword = prefs.getBoolean(KEY_REMEMBER_PASSWORD, false)
        val password = if (rememberPassword) {
            prefs.getString(KEY_PASSWORD, "") ?: ""
        } else {
            ""
        }

        return SavedLogin(
            email = email,
            password = password,
            rememberPassword = rememberPassword
        )
    }

    fun save(
        context: Context,
        email: String,
        password: String,
        rememberPassword: Boolean
    ) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            // Always remember last login email
            putString(KEY_EMAIL, email)

            // Only store password if user opted in
            putBoolean(KEY_REMEMBER_PASSWORD, rememberPassword)
            if (rememberPassword) {
                // ⚠️ OK for class project, not for production
                putString(KEY_PASSWORD, password)
            } else {
                remove(KEY_PASSWORD)
            }
        }.apply()
    }

    fun clear(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }
}
