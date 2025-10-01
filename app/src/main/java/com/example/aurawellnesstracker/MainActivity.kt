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

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

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

        // Navigate to OnBoarding1 after 3 seconds total
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, OnBoarding1::class.java)
            startActivity(intent)
            finish()
        }, 3000)
    }
}