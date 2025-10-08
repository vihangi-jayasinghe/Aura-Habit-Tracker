package com.example.aurawellnesstracker

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText

class ForgotPassword : AppCompatActivity() {

    private lateinit var emailEditText: TextInputEditText
    private lateinit var sendInstructionsButton: MaterialButton
    private lateinit var successCard: MaterialCardView
    private lateinit var successMessage: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_forgot_password)
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
        sendInstructionsButton = findViewById(R.id.sendInstructionsButton)
        successCard = findViewById(R.id.successCard)
        successMessage = findViewById(R.id.successMessage)
    }

    private fun setupButtonClickListeners() {
        // Back button
        findViewById<View>(R.id.backButton).setOnClickListener {
            onBackPressed()
        }

        // Send Instructions button
        sendInstructionsButton.setOnClickListener {
            if (validateEmail()) {
                sendResetInstructions()
            }
        }

        // Back to Sign In text
        findViewById<TextView>(R.id.signInText).setOnClickListener {
            val intent = Intent(this, Signin::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun setupRealTimeValidation() {
        emailEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validateEmailField()
                updateSendButtonState()
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

    private fun updateSendButtonState() {
        val isEmailValid = validateEmailField()
        sendInstructionsButton.isEnabled = isEmailValid
        sendInstructionsButton.alpha = if (sendInstructionsButton.isEnabled) 1.0f else 0.5f
    }

    private fun validateEmail(): Boolean {
        val isEmailValid = validateEmailField()

        if (!isEmailValid) {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun sendResetInstructions() {
        val email = emailEditText.text.toString().trim()

        // Show loading state
        sendInstructionsButton.text = "Sending..."
        sendInstructionsButton.isEnabled = false

        // Simulate API call delay
        emailEditText.postDelayed({
            // In a real app, you would call your backend API here
            // For demo purposes, we'll simulate a successful response

            // Hide the form and show success message
            findViewById<MaterialCardView>(R.id.resetCard).visibility = View.GONE
            successCard.visibility = View.VISIBLE
            successMessage.text = "Reset instructions sent to $email"

            // Show success message
            Toast.makeText(this, "Reset instructions sent successfully!", Toast.LENGTH_LONG).show()

            // Reset button state (in case user goes back)
            sendInstructionsButton.text = "Send Reset Instructions"
            sendInstructionsButton.isEnabled = true

        }, 2000) // 2 second delay to simulate network request
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$"
        return email.matches(emailRegex.toRegex())
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(0, 0)
    }
}