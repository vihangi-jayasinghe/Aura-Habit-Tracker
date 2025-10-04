package com.example.aurawellnesstracker

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.aurawellnesstracker.utils.UserManager

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Initialize UserManager
        UserManager.initialize(this)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val appNameTextView = findViewById<TextView>(R.id.appNameTextView)

        // Initially hide the app name
        appNameTextView.alpha = 0f

        // Show the app name after 1 second
        Handler(Looper.getMainLooper()).postDelayed({
            appNameTextView.animate()
                .alpha(1f)
                .setDuration(1000)
                .start()
        }, 1000)

        // Check if user is already logged in
        Handler(Looper.getMainLooper()).postDelayed({
            if (UserManager.isUserLoggedIn()) {
                // User is logged in, go directly to Home
                val intent = Intent(this, com.example.aurawellnesstracker.ui.Home::class.java)
                startActivity(intent)
            } else {
                // User is not logged in, go to onboarding
                val intent = Intent(this, OnBoarding1::class.java)
                startActivity(intent)
            }
            finish()
        }, 3000)
    }
}