package com.example.aurawellnesstracker

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.aurawellnesstracker.ui.Home
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class Signin : AppCompatActivity() {

    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var signInButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_signin)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        initializeViews()
        setupButtonClickListeners()
        setupRealTimeValidation()
    }

    private fun initializeViews() {
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        signInButton = findViewById(R.id.signInButton)
    }

    private fun setupButtonClickListeners() {
        // Don't have account text - navigate to Signup
        val signUpText = findViewById<TextView>(R.id.signUpText)
        signUpText.setOnClickListener {
            val intent = Intent(this, Signup::class.java)
            startActivity(intent)
            finish()
        }

        // Sign In button - navigate to Home (after successful login)
        signInButton.setOnClickListener {
            if (validateSignInForm()) {
                val intent = Intent(this, Home::class.java)
                startActivity(intent)
                finish()
            }
        }

        // Forgot Password
        val forgotPasswordText = findViewById<TextView>(R.id.forgotPasswordText)
        forgotPasswordText.setOnClickListener {
            Toast.makeText(this, "Forgot Password feature coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupRealTimeValidation() {
        // Email real-time validation
        emailEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validateEmailField()
                updateSignInButtonState()
            }
        })

        // Password real-time validation
        passwordEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validatePasswordField()
                updateSignInButtonState()
            }
        })
    }

    private fun validateEmailField(): Boolean {
        val email = emailEditText.text.toString().trim()
        return when {
            email.isEmpty() -> {
                emailEditText.error = "Email is required"
                false
            }
            !isValidEmail(email) -> {
                emailEditText.error = "Please enter a valid email address"
                false
            }
            else -> {
                emailEditText.error = null
                true
            }
        }
    }

    private fun validatePasswordField(): Boolean {
        val password = passwordEditText.text.toString().trim()
        return when {
            password.isEmpty() -> {
                passwordEditText.error = "Password is required"
                false
            }
            password.length < 6 -> {
                passwordEditText.error = "Password must be at least 6 characters"
                false
            }
            else -> {
                passwordEditText.error = null
                true
            }
        }
    }

    private fun updateSignInButtonState() {
        val isEmailValid = validateEmailField()
        val isPasswordValid = validatePasswordField()

        signInButton.isEnabled = isEmailValid && isPasswordValid
        signInButton.alpha = if (signInButton.isEnabled) 1.0f else 0.5f
    }

    private fun validateSignInForm(): Boolean {
        val isEmailValid = validateEmailField()
        val isPasswordValid = validatePasswordField()

        if (!isEmailValid || !isPasswordValid) {
            Toast.makeText(this, "Please fix the errors above", Toast.LENGTH_SHORT).show()
            return false
        }

        // Here you would typically make an API call to authenticate the user
        Toast.makeText(this, "Signing in...", Toast.LENGTH_SHORT).show()
        return true
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$"
        return email.matches(emailRegex.toRegex())
    }
}