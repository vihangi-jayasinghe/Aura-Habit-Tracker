package com.example.aurawellnesstracker.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.aurawellnesstracker.model.User

object UserManager {
    private lateinit var sharedPreferences: SharedPreferences
    private var currentUser: User? = null

    fun initialize(context: Context) {
        sharedPreferences = context.getSharedPreferences("AuraWellnessPrefs", Context.MODE_PRIVATE)
        loadUserFromPreferences()
    }

    private fun loadUserFromPreferences() {
        val name = sharedPreferences.getString("user_name", null)
        val email = sharedPreferences.getString("user_email", null)

        if (name != null && email != null) {
            currentUser = User(name, email)
        }
    }

    fun getCurrentUser(): User? {
        return currentUser
    }

    fun setCurrentUser(user: User) {
        currentUser = user
        with(sharedPreferences.edit()) {
            putString("user_name", user.name)
            putString("user_email", user.email)
            putBoolean("is_logged_in", true)
            apply()
        }
    }

    fun updateUserDetails(name: String, email: String) {
        currentUser = currentUser?.copy(name = name, email = email)
        with(sharedPreferences.edit()) {
            putString("user_name", name)
            putString("user_email", email)
            apply()
        }
    }

    fun logout() {
        currentUser = null
        with(sharedPreferences.edit()) {
            remove("user_name")
            remove("user_email")
            remove("is_logged_in")
            apply()
        }
    }

    fun isUserLoggedIn(): Boolean {
        return sharedPreferences.getBoolean("is_logged_in", false) && currentUser != null
    }
}