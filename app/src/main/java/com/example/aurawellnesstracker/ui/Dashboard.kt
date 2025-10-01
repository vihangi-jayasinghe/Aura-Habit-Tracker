package com.example.aurawellnesstracker.ui

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.aurawellnesstracker.R

class Dashboard : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard)

//        // Home navigation
//        findViewById<ImageView>(R.id.homeBtn10).setOnClickListener {
//            startActivity(Intent(this, Home::class.java))
//        }
//
//        // Product / Mood navigation
//        findViewById<ImageView>(R.id.productBtn10).setOnClickListener {
//            startActivity(Intent(this, Mood::class.java))
//        }
//
//        // Experts / Habits navigation
//        findViewById<ImageView>(R.id.expertsBtn10).setOnClickListener {
//            startActivity(Intent(this, Habit::class.java))
//        }
//
//        // Profile / Hydration navigation
//        findViewById<ImageView>(R.id.profileBtn10).setOnClickListener {
//            startActivity(Intent(this, Hydration::class.java))
//        }
//
//        // Settings navigation - FIXED: Use correct ID
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