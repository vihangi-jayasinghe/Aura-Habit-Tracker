package com.example.aurawellnesstracker.ui

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.aurawellnesstracker.R
import com.example.aurawellnesstracker.Signin
import com.example.aurawellnesstracker.model.User
import com.example.aurawellnesstracker.utils.UserManager
import com.google.android.material.button.MaterialButton

class Settings : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var userNameText: TextView
    private lateinit var userEmailText: TextView
    private lateinit var editProfileButton: MaterialButton
    private lateinit var themeSpinner: Spinner
    private lateinit var waterGoalText: TextView
    private lateinit var habitGoalText: TextView
    private lateinit var appVersionText: TextView
    private lateinit var buildDateText: TextView
    private lateinit var logoutButton: Button

    // Notification switches
    private lateinit var pushNotificationsSwitch: Switch
    private lateinit var moodRemindersSwitch: Switch
    private lateinit var hydrationRemindersSwitch: Switch
    private lateinit var habitRemindersSwitch: Switch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)

        sharedPreferences = getSharedPreferences("AuraWellnessPrefs", MODE_PRIVATE)
        initializeViews()
        setupBottomNavigation()
        setupLogoutButton()
        setupUserDetails()
        setupThemeSpinner()
        setupNotificationSwitches()
        setupAppInfo()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh user details when returning to settings
        setupUserDetails()
    }

    private fun initializeViews() {
        userNameText = findViewById(R.id.userNameText)
        userEmailText = findViewById(R.id.userEmailText)
        editProfileButton = findViewById(R.id.editProfileButton)
        themeSpinner = findViewById(R.id.themeSpinner)
        waterGoalText = findViewById(R.id.waterGoalText)
        habitGoalText = findViewById(R.id.habitGoalText)
        appVersionText = findViewById(R.id.appVersionText)
        buildDateText = findViewById(R.id.buildDateText)
        logoutButton = findViewById(R.id.logoutButton)

        // Notification switches
        pushNotificationsSwitch = findViewById(R.id.pushNotificationsSwitch)
        moodRemindersSwitch = findViewById(R.id.moodRemindersSwitch)
        hydrationRemindersSwitch = findViewById(R.id.hydrationRemindersSwitch)
        habitRemindersSwitch = findViewById(R.id.habitRemindersSwitch)
    }

    private fun setupUserDetails() {
        // Get current user from UserManager or SharedPreferences
        val currentUser = getCurrentUser()

        if (currentUser != null) {
            userNameText.text = currentUser.name
            userEmailText.text = currentUser.email
        } else {
            // Fallback to shared preferences or default values
            userNameText.text = sharedPreferences.getString("user_name", "User")
            userEmailText.text = sharedPreferences.getString("user_email", "user@example.com")
        }

        // Set up edit profile button
        editProfileButton.setOnClickListener {
            showEditProfileDialog()
        }
    }

    private fun getCurrentUser(): User? {
        return try {
            // Try to get from UserManager first
            UserManager.getCurrentUser()
        } catch (e: Exception) {
            // Fallback to shared preferences
            val name = sharedPreferences.getString("user_name", null)
            val email = sharedPreferences.getString("user_email", null)

            if (name != null && email != null) {
                User(name, email)
            } else {
                null
            }
        }
    }

    private fun showEditProfileDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_profile, null)
        val nameEditText = dialogView.findViewById<EditText>(R.id.nameEditText)
        val emailEditText = dialogView.findViewById<EditText>(R.id.emailEditText)

        // Pre-fill current values
        nameEditText.setText(userNameText.text)
        emailEditText.setText(userEmailText.text)

        val dialog = android.app.AlertDialog.Builder(this)
            .setTitle("Edit Profile")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val newName = nameEditText.text.toString().trim()
                val newEmail = emailEditText.text.toString().trim()

                if (newName.isNotEmpty() && newEmail.isNotEmpty()) {
                    // Update user details
                    updateUserDetails(newName, newEmail)
                } else {
                    Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun updateUserDetails(name: String, email: String) {
        // Update in shared preferences
        with(sharedPreferences.edit()) {
            putString("user_name", name)
            putString("user_email", email)
            apply()
        }

        // Update in UserManager if available
        try {
            UserManager.updateUserDetails(name, email)
        } catch (e: Exception) {
            // UserManager might not be implemented yet
        }

        // Update UI
        userNameText.text = name
        userEmailText.text = email

        Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
    }

    private fun setupThemeSpinner() {
        val themes = arrayOf("System Default", "Light", "Dark")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, themes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        themeSpinner.adapter = adapter

        // Set current theme
        val currentTheme = sharedPreferences.getInt("app_theme", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        val themePosition = when (currentTheme) {
            AppCompatDelegate.MODE_NIGHT_NO -> 1 // Light
            AppCompatDelegate.MODE_NIGHT_YES -> 2 // Dark
            else -> 0 // System Default
        }
        themeSpinner.setSelection(themePosition)

        themeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val selectedTheme = when (position) {
                    1 -> AppCompatDelegate.MODE_NIGHT_NO
                    2 -> AppCompatDelegate.MODE_NIGHT_YES
                    else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                }

                if (selectedTheme != currentTheme) {
                    with(sharedPreferences.edit()) {
                        putInt("app_theme", selectedTheme)
                        apply()
                    }
                    AppCompatDelegate.setDefaultNightMode(selectedTheme)
                    recreate() // Restart activity to apply theme
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupNotificationSwitches() {
        // Load saved notification preferences
        pushNotificationsSwitch.isChecked = sharedPreferences.getBoolean("push_notifications", true)
        moodRemindersSwitch.isChecked = sharedPreferences.getBoolean("mood_reminders", true)
        hydrationRemindersSwitch.isChecked = sharedPreferences.getBoolean("hydration_reminders", true)
        habitRemindersSwitch.isChecked = sharedPreferences.getBoolean("habit_reminders", true)

        // Set up listeners
        pushNotificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("push_notifications", isChecked).apply()
        }

        moodRemindersSwitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("mood_reminders", isChecked).apply()
        }

        hydrationRemindersSwitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("hydration_reminders", isChecked).apply()
        }

        habitRemindersSwitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("habit_reminders", isChecked).apply()
        }
    }

    private fun setupAppInfo() {
        // Set water goal
        val waterGoal = sharedPreferences.getFloat("daily_water_goal", 2.0f)
        waterGoalText.text = String.format("%.1fL", waterGoal)

        // Set habit goal
        val habitGoal = sharedPreferences.getInt("daily_habit_goal", 5)
        habitGoalText.text = "$habitGoal habits"

        // Set app version
        try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            appVersionText.text = packageInfo.versionName
        } catch (e: Exception) {
            appVersionText.text = "1.0.0"
        }

        // Set build date (you might want to get this from BuildConfig)
        buildDateText.text = "September 2025" // Replace with actual build date if available
    }

    private fun setupLogoutButton() {
        logoutButton.setOnClickListener {
            showLogoutConfirmation()
        }
    }

    private fun showLogoutConfirmation() {
        android.app.AlertDialog.Builder(this)
            .setTitle("Log Out")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Log Out") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performLogout() {
        // Clear user session
        with(sharedPreferences.edit()) {
            remove("user_name")
            remove("user_email")
            remove("is_logged_in")
            apply()
        }

        // Clear UserManager session if available
        try {
            UserManager.logout()
        } catch (e: Exception) {
            // UserManager might not be implemented yet
        }

        // Navigate to Signin page
        val intent = Intent(this, Signin::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    private fun setupBottomNavigation() {
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