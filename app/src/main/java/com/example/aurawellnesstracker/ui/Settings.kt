package com.example.aurawellnesstracker.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.aurawellnesstracker.R
import com.example.aurawellnesstracker.Signin

class Settings : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)

        setupBottomNavigation()
        setupLogoutButton()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupLogoutButton() {
        val logoutButton = findViewById<Button>(R.id.logoutButton)
        logoutButton.setOnClickListener {
            // Navigate to Signin page
            val intent = Intent(this, Signin::class.java)

            // Clear the back stack so user can't go back to app without signing in
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)

            startActivity(intent)
            finish() // Finish current activity
        }
    }

    private fun setupBottomNavigation() {
//        // Home navigation
//        findViewById<ImageView>(R.id.homeBtn10).setOnClickListener {
//            startActivity(Intent(this, Home::class.java))
//        }
//
//        // Mood navigation
//        findViewById<ImageView>(R.id.productBtn10).setOnClickListener {
//            startActivity(Intent(this, Mood::class.java))
//        }
//
//        // Habits navigation
//        findViewById<ImageView>(R.id.expertsBtn10).setOnClickListener {
//            startActivity(Intent(this, Habit::class.java))
//        }
//
//        // Hydration navigation
//        findViewById<ImageView>(R.id.profileBtn10).setOnClickListener {
//            startActivity(Intent(this, Hydration::class.java))
//        }
//
//        // Settings navigation - Already on Settings, do nothing
//        findViewById<ImageView>(R.id.profileBtn11).setOnClickListener {
//            // Already on Settings page
//        }

        findViewById<ImageView>(R.id.homeBtn10).setOnClickListener {
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }
        findViewById<ImageView>(R.id.productBtn10).setOnClickListener {
            val intent = Intent(this, Mood::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0) // This removes the animation
        }
        findViewById<ImageView>(R.id.expertsBtn10).setOnClickListener {
            val intent = Intent(this, Habit::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }
        findViewById<ImageView>(R.id.profileBtn10).setOnClickListener {
            val intent = Intent(this, Hydration::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0) // This removes the animation
        }
        findViewById<ImageView>(R.id.profileBtn11).setOnClickListener {
            val intent = Intent(this, Settings::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }
    }
}