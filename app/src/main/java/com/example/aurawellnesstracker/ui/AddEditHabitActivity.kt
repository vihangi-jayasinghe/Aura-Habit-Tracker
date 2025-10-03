package com.example.aurawellnesstracker.ui

import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.aurawellnesstracker.R
import com.example.aurawellnesstracker.model.Habit
import com.example.aurawellnesstracker.model.HabitCategory
import com.example.aurawellnesstracker.utils.HabitManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.*

class AddEditHabitActivity : AppCompatActivity() {

    private lateinit var titleEditText: TextInputEditText
    private lateinit var descriptionEditText: TextInputEditText
    private lateinit var targetDaysEditText: TextInputEditText
    private lateinit var reminderTimeEditText: TextInputEditText
    private lateinit var categorySpinner: Spinner
    private lateinit var saveButton: MaterialButton
    private lateinit var cancelButton: MaterialButton
    private lateinit var deleteButton: MaterialButton
    private lateinit var titleTextView: TextView

    private var isEditing = false
    private var currentHabit: Habit? = null
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_habit)

        // Initialize HabitManager
        HabitManager.initialize(this)

        initializeViews()
        setupViews()
        checkIfEditing()
        setupClickListeners()
    }

    private fun initializeViews() {
        // Initialize all views using findViewById
        titleEditText = findViewById(R.id.titleEditText)
        descriptionEditText = findViewById(R.id.descriptionEditText)
        targetDaysEditText = findViewById(R.id.targetDaysEditText)
        reminderTimeEditText = findViewById(R.id.reminderTimeEditText)
        categorySpinner = findViewById(R.id.categorySpinner)
        saveButton = findViewById(R.id.saveButton)
        cancelButton = findViewById(R.id.cancelButton)
        deleteButton = findViewById(R.id.deleteButton)
        titleTextView = findViewById(R.id.titleTextView)
    }

    private fun setupViews() {
        // Setup category spinner
        val categories = HabitCategory.values().filter { it != HabitCategory.ALL }
        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = categoryAdapter
    }

    private fun checkIfEditing() {
        currentHabit = intent.getParcelableExtra("HABIT")
        currentHabit?.let { habit ->
            isEditing = true
            titleEditText.setText(habit.title)
            descriptionEditText.setText(habit.description)
            targetDaysEditText.setText(habit.targetDays.toString())
            reminderTimeEditText.setText(habit.reminderTime ?: "")

            // Set category
            val position = HabitCategory.values().indexOf(habit.category)
            if (position != -1) {
                categorySpinner.setSelection(position)
            }

            // Update title for editing mode
            titleTextView.text = "Edit Habit"

            // Show delete button for editing
            deleteButton.visibility = android.view.View.VISIBLE
        }
    }

    private fun setupClickListeners() {
        saveButton.setOnClickListener {
            saveHabit()
        }

        cancelButton.setOnClickListener {
            finish()
        }

        deleteButton.setOnClickListener {
            deleteHabit()
        }

        reminderTimeEditText.setOnClickListener {
            showTimePicker()
        }

        // Make reminder time field focusable but not editable by keyboard
        reminderTimeEditText.isFocusable = false
        reminderTimeEditText.isClickable = true
    }

    private fun showTimePicker() {
        val timePicker = TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)

                val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
                reminderTimeEditText.setText(timeFormat.format(calendar.time))
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false
        )
        timePicker.show()
    }

    private fun saveHabit() {
        val title = titleEditText.text.toString().trim()
        val description = descriptionEditText.text.toString().trim()
        val targetDaysText = targetDaysEditText.text.toString().trim()
        val reminderTime = reminderTimeEditText.text.toString().trim()
        val selectedCategory = categorySpinner.selectedItem as? HabitCategory ?: HabitCategory.HEALTH

        // Validation
        if (title.isEmpty()) {
            titleEditText.error = "Please enter a habit title"
            return
        }

        if (targetDaysText.isEmpty()) {
            targetDaysEditText.error = "Please enter target days"
            return
        }

        val targetDays = targetDaysText.toIntOrNull()
        if (targetDays == null || targetDays <= 0) {
            targetDaysEditText.error = "Target days must be a number greater than 0"
            return
        }

        val habit = if (isEditing) {
            currentHabit!!.copy(
                title = title,
                description = description,
                category = selectedCategory,
                targetDays = targetDays,
                reminderTime = if (reminderTime.isNotEmpty()) reminderTime else null
            )
        } else {
            Habit(
                title = title,
                description = description,
                category = selectedCategory,
                targetDays = targetDays,
                reminderTime = if (reminderTime.isNotEmpty()) reminderTime else null
            )
        }

        val success = if (isEditing) {
            HabitManager.updateHabit(this, currentHabit!!.id, habit)
        } else {
            HabitManager.addHabit(this, habit)
        }

        if (success) {
            val message = if (isEditing) "Habit updated successfully" else "Habit created successfully"
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            setResult(RESULT_OK)
            finish()
        } else {
            Toast.makeText(this, "Failed to save habit", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteHabit() {
        currentHabit?.let { habit ->
            AlertDialog.Builder(this)
                .setTitle("Delete Habit")
                .setMessage("Are you sure you want to delete '${habit.title}'?")
                .setPositiveButton("Delete") { dialog, which ->
                    val success = HabitManager.deleteHabit(this, habit.id)
                    if (success) {
                        Toast.makeText(this, "Habit deleted", Toast.LENGTH_SHORT).show()
                        setResult(RESULT_OK)
                        finish()
                    } else {
                        Toast.makeText(this, "Failed to delete habit", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
}