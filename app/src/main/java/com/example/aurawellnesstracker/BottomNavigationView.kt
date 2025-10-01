package com.example.aurawellnesstracker

import android.app.Activity
import android.content.Intent
import com.example.aurawellnesstracker.ui.Habit
import com.example.aurawellnesstracker.ui.Home
import com.example.aurawellnesstracker.ui.Hydration
import com.example.aurawellnesstracker.ui.Mood
import com.example.aurawellnesstracker.ui.Settings
import com.google.android.material.bottomnavigation.BottomNavigationView

object NavHelper {

    fun handleBottomNav(activity: Activity, bottomNav: BottomNavigationView, currentId: Int) {
        bottomNav.selectedItemId = currentId

        bottomNav.setOnItemSelectedListener { item ->
            if (item.itemId == currentId) return@setOnItemSelectedListener true

            val intent = when (item.itemId) {
                R.id.homeBtn10 -> Intent(activity, Home::class.java)
                R.id.productBtn10 -> Intent(activity, Mood::class.java)
                R.id.expertsBtn10 -> Intent(activity, Habit::class.java)
                R.id.profileBtn10 -> Intent(activity, Hydration::class.java)
                R.id.profileBtn11 -> Intent(activity, Settings::class.java)
                else -> null
            } ?: return@setOnItemSelectedListener false

            // Bring existing instance to front, no flicker
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            activity.startActivity(intent)
            true
        }
    }
}