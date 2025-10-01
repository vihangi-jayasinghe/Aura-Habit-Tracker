package com.example.aurawellnesstracker

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.aurawellnesstracker.ui.Home

class Signin : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_signin)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setupButtonClickListeners()
    }

    private fun setupButtonClickListeners() {
        // Don't have account text - navigate to Signup
        val signUpText = findViewById<TextView>(R.id.signUpText)
        signUpText.setOnClickListener {
            val intent = Intent(this, Signup::class.java)
            startActivity(intent)
            finish() // Optional: remove Signin from back stack
        }

        // Sign In button - navigate to Home (after successful login)
        val signInButton = findViewById<com.google.android.material.button.MaterialButton>(R.id.signInButton)
        signInButton.setOnClickListener {
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
            finish() // Remove Signin from back stack
        }
    }
}