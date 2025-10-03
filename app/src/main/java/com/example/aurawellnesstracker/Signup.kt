package com.example.aurawellnesstracker

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.aurawellnesstracker.model.User
import com.example.aurawellnesstracker.utils.UserManager
import com.google.android.material.textfield.TextInputEditText

class Signup : AppCompatActivity() {

    private lateinit var fullNameEditText: TextInputEditText
    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var confirmPasswordEditText: TextInputEditText
    private lateinit var termsCheckbox: CheckBox
    private lateinit var signUpButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_signup)
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
        fullNameEditText = findViewById(R.id.fullNameEditText)
        emailEditText = findViewById(R.id.signupEmailEditText)
        passwordEditText = findViewById(R.id.signupPasswordEditText)
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText)
        termsCheckbox = findViewById(R.id.termsCheckbox)
        signUpButton = findViewById(R.id.signUpButton)
    }

    private fun setupButtonClickListeners() {
        // Create Account button
        signUpButton.setOnClickListener {
            if (validateSignUpForm()) {
                performSignUp()
            }
        }

        // Already have account text - navigate to Signin
        val signInText = findViewById<TextView>(R.id.signInText)
        signInText.setOnClickListener {
            val intent = Intent(this, Signin::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun performSignUp() {
        val name = fullNameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()

        // Create and store user
        val user = User(name, email)
        UserManager.setCurrentUser(user)

        Toast.makeText(this, "Account created successfully! Welcome, $name!", Toast.LENGTH_SHORT).show()

        // Navigate directly to Home after signup
        val intent = Intent(this, com.example.aurawellnesstracker.ui.Home::class.java)
        startActivity(intent)
        finish()
    }

    private fun setupRealTimeValidation() {
        // Full Name real-time validation
        fullNameEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validateFullNameField()
                updateSignUpButtonState()
            }
        })

        // Email real-time validation
        emailEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validateEmailField()
                updateSignUpButtonState()
            }
        })

        // Password real-time validation
        passwordEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validatePasswordField()
                validateConfirmPasswordField() // Also validate confirm password when password changes
                updateSignUpButtonState()
            }
        })

        // Confirm Password real-time validation
        confirmPasswordEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validateConfirmPasswordField()
                updateSignUpButtonState()
            }
        })

        // Terms checkbox listener
        termsCheckbox.setOnCheckedChangeListener { _, _ ->
            updateSignUpButtonState()
        }
    }

    private fun validateFullNameField(): Boolean {
        val fullName = fullNameEditText.text.toString().trim()
        return when {
            fullName.isEmpty() -> {
                fullNameEditText.error = "Full name is required"
                false
            }
            fullName.length < 2 -> {
                fullNameEditText.error = "Full name must be at least 2 characters"
                false
            }
            else -> {
                fullNameEditText.error = null
                true
            }
        }
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
            !isValidPassword(password) -> {
                passwordEditText.error = "Must contain letter and number"
                false
            }
            else -> {
                passwordEditText.error = null
                true
            }
        }
    }

    private fun validateConfirmPasswordField(): Boolean {
        val password = passwordEditText.text.toString().trim()
        val confirmPassword = confirmPasswordEditText.text.toString().trim()

        return when {
            confirmPassword.isEmpty() -> {
                confirmPasswordEditText.error = "Please confirm your password"
                false
            }
            password != confirmPassword -> {
                confirmPasswordEditText.error = "Passwords do not match"
                false
            }
            else -> {
                confirmPasswordEditText.error = null
                true
            }
        }
    }

    private fun updateSignUpButtonState() {
        val isFullNameValid = validateFullNameField()
        val isEmailValid = validateEmailField()
        val isPasswordValid = validatePasswordField()
        val isConfirmPasswordValid = validateConfirmPasswordField()
        val isTermsAccepted = termsCheckbox.isChecked

        signUpButton.isEnabled = isFullNameValid && isEmailValid && isPasswordValid &&
                isConfirmPasswordValid && isTermsAccepted
        signUpButton.alpha = if (signUpButton.isEnabled) 1.0f else 0.5f
    }

    private fun validateSignUpForm(): Boolean {
        val isFullNameValid = validateFullNameField()
        val isEmailValid = validateEmailField()
        val isPasswordValid = validatePasswordField()
        val isConfirmPasswordValid = validateConfirmPasswordField()
        val isTermsAccepted = termsCheckbox.isChecked

        if (!isFullNameValid || !isEmailValid || !isPasswordValid || !isConfirmPasswordValid) {
            Toast.makeText(this, "Please fix the errors above", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!isTermsAccepted) {
            Toast.makeText(this, "Please agree to the Terms of Service and Privacy Policy", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$"
        return email.matches(emailRegex.toRegex())
    }

    private fun isValidPassword(password: String): Boolean {
        // Password must contain at least one letter and one number
        val letterRegex = ".*[A-Za-z].*".toRegex()
        val digitRegex = ".*\\d.*".toRegex()
        return letterRegex.matches(password) && digitRegex.matches(password)
    }
}