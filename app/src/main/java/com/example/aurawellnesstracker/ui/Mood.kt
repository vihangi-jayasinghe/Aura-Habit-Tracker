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

class Mood : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mood)

        setupBottomNavigation()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

//        val addButton = findViewById<Button>(R.id.addMoodButton)
//
//        addButton.setOnClickListener {
//            val intent = Intent(this, AddMood::class.java)
//            startActivity(intent)
//        }
    }

    private fun setupBottomNavigation() {
//        // Home navigation
//        findViewById<ImageView>(R.id.homeBtn10).setOnClickListener {
//            startActivity(Intent(this, Home::class.java))
//        }
//
//        // Mood navigation - Already on Mood, do nothing
//        findViewById<ImageView>(R.id.productBtn10).setOnClickListener {
//            // Already on Mood page
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
//        // Settings navigation
//        findViewById<ImageView>(R.id.profileBtn11).setOnClickListener {
//            startActivity(Intent(this, Settings::class.java))
//        }

        findViewById<ImageView>(R.id.homeBtn10).setOnClickListener {
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }
        findViewById<ImageView>(R.id.productBtn10).setOnClickListener {
            val intent = Intent(this, Mood::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }
        findViewById<ImageView>(R.id.expertsBtn10).setOnClickListener {
            val intent = Intent(this, Habit::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }
        findViewById<ImageView>(R.id.profileBtn10).setOnClickListener {
            val intent = Intent(this, Hydration::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }
        findViewById<ImageView>(R.id.profileBtn11).setOnClickListener {
            val intent = Intent(this, Settings::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }
    }

}