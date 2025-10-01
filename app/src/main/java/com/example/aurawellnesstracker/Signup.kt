package com.example.aurawellnesstracker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Signup : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_signup)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setupButtonClickListeners()
    }

    private fun setupButtonClickListeners() {
        // Create Account button - navigate to Signin
        val createAccountButton = findViewById<Button>(R.id.signUpButton)
        createAccountButton.setOnClickListener {
            val intent = Intent(this, Signin::class.java)
            startActivity(intent)
            finish() // Optional: remove Signup from back stack
        }

        // Already have account text - navigate to Signin
        val signInText = findViewById<TextView>(R.id.signInText)
        signInText.setOnClickListener {
            val intent = Intent(this, Signin::class.java)
            startActivity(intent)
            finish() // Optional: remove Signup from back stack
        }
    }
}