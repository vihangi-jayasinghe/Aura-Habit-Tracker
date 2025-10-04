package com.example.aurawellnesstracker.ui

import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.*

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

    private lateinit var pushNotificationsSwitch: Switch
    private lateinit var moodRemindersSwitch: Switch
    private lateinit var hydrationRemindersSwitch: Switch
    private lateinit var habitRemindersSwitch: Switch

    // Support buttons
    private lateinit var helpButton: MaterialButton
    private lateinit var contactButton: MaterialButton
    private lateinit var privacyButton: MaterialButton
    private lateinit var termsButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)

        sharedPreferences = getSharedPreferences("AuraWellnessPrefs", MODE_PRIVATE)
        initializeViews()
        setupBottomNavigation()
        setupUserDetails()
        setupThemeSpinner()
        setupNotificationSwitches()
        setupAppInfo()
        setupSupportButtons()
        setupDailyGoals()

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
        updateDailyGoalsDisplay()
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

        // Support buttons
        helpButton = findViewById(R.id.helpButton)
        contactButton = findViewById(R.id.contactButton)
        privacyButton = findViewById(R.id.privacyButton)
        termsButton = findViewById(R.id.termsButton)
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

        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle("Edit Profile")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val newName = nameEditText.text.toString().trim()
                val newEmail = emailEditText.text.toString().trim()

                if (newName.isNotEmpty() && newEmail.isNotEmpty()) {
                    if (isValidEmail(newEmail)) {
                        // Update user details
                        updateUserDetails(newName, newEmail)
                    } else {
                        Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
                    }
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

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$"
        return email.matches(emailRegex.toRegex())
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
            showNotificationToast("Push Notifications", isChecked)
        }

        moodRemindersSwitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("mood_reminders", isChecked).apply()
            showNotificationToast("Mood Reminders", isChecked)
        }

        hydrationRemindersSwitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("hydration_reminders", isChecked).apply()
            showNotificationToast("Hydration Reminders", isChecked)
        }

        habitRemindersSwitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("habit_reminders", isChecked).apply()
            showNotificationToast("Habit Reminders", isChecked)
        }
    }

    private fun showNotificationToast(type: String, enabled: Boolean) {
        val status = if (enabled) "enabled" else "disabled"
        Toast.makeText(this, "$type $status", Toast.LENGTH_SHORT).show()
    }

    private fun setupDailyGoals() {
        // Set up click listeners for daily goals to allow editing
        waterGoalText.setOnClickListener {
            showWaterGoalDialog()
        }

        habitGoalText.setOnClickListener {
            showHabitGoalDialog()
        }

        updateDailyGoalsDisplay()
    }

    private fun showWaterGoalDialog() {
        val currentGoal = sharedPreferences.getFloat("daily_water_goal", 2.0f)
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_goal, null)
        val goalEditText = dialogView.findViewById<EditText>(R.id.goalEditText)
        val goalUnitText = dialogView.findViewById<TextView>(R.id.goalUnitText)

        goalEditText.setText(currentGoal.toString())
        goalUnitText.text = "Liters"

        MaterialAlertDialogBuilder(this)
            .setTitle("Set Water Goal")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val newGoal = goalEditText.text.toString().toFloatOrNull()
                if (newGoal != null && newGoal > 0) {
                    sharedPreferences.edit().putFloat("daily_water_goal", newGoal).apply()
                    updateDailyGoalsDisplay()
                    Toast.makeText(this, "Water goal updated to ${newGoal}L", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Please enter a valid water goal", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showHabitGoalDialog() {
        val currentGoal = sharedPreferences.getInt("daily_habit_goal", 5)
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_goal, null)
        val goalEditText = dialogView.findViewById<EditText>(R.id.goalEditText)
        val goalUnitText = dialogView.findViewById<TextView>(R.id.goalUnitText)

        goalEditText.setText(currentGoal.toString())
        goalUnitText.text = "Habits"

        MaterialAlertDialogBuilder(this)
            .setTitle("Set Habit Goal")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val newGoal = goalEditText.text.toString().toIntOrNull()
                if (newGoal != null && newGoal > 0) {
                    sharedPreferences.edit().putInt("daily_habit_goal", newGoal).apply()
                    updateDailyGoalsDisplay()
                    Toast.makeText(this, "Habit goal updated to $newGoal habits", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Please enter a valid habit goal", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateDailyGoalsDisplay() {
        val waterGoal = sharedPreferences.getFloat("daily_water_goal", 2.0f)
        val habitGoal = sharedPreferences.getInt("daily_habit_goal", 5)

        waterGoalText.text = String.format("%.1fL", waterGoal)
        habitGoalText.text = "$habitGoal habits"
    }

    private fun setupAppInfo() {
        // Set app version
        try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            appVersionText.text = packageInfo.versionName
        } catch (e: Exception) {
            appVersionText.text = "1.0.0"
        }

        // Set build date
        val buildDate = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())
        buildDateText.text = buildDate

        // Set up logout button
        logoutButton.setOnClickListener {
            showLogoutConfirmation()
        }
    }

    private fun setupSupportButtons() {
        helpButton.setOnClickListener {
            showSupportDialog("Help & FAQ", "Frequently Asked Questions:\n\n• How to track mood?\n- Go to Mood section and select your current mood\n\n• How to log water?\n- Go to Hydration section and add water intake\n\n• How to create habits?\n- Go to Habits section and click 'Add Habit'")
        }

        contactButton.setOnClickListener {
            showSupportDialog("Contact Support", "Email: support@aurawellness.com\n\nPhone: +1 (555) 123-4567\n\nResponse Time: 24-48 hours\n\nWe're here to help you with any issues or questions!")
        }

        privacyButton.setOnClickListener {
            showSupportDialog("Privacy Policy", "Your Privacy Matters\n\nWe collect only essential data to provide our wellness tracking services. Your personal information is never shared with third parties without your consent.\n\nAll data is stored securely and encrypted.")
        }

        termsButton.setOnClickListener {
            showSupportDialog("Terms of Service", "Terms of Service\n\nBy using Aura Wellness Tracker, you agree to:\n\n• Use the app for personal wellness tracking\n• Not misuse the application\n• Keep your login credentials secure\n\nWe reserve the right to update these terms as needed.")
        }
    }

    private fun showSupportDialog(title: String, message: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showLogoutConfirmation() {
        MaterialAlertDialogBuilder(this)
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

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
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