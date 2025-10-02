package com.example.aurawellnesstracker.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.aurawellnesstracker.R
import com.example.aurawellnesstracker.model.Habit
import com.example.aurawellnesstracker.model.HabitCategory
import com.example.aurawellnesstracker.utils.HabitManager
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.*

class Habit : AppCompatActivity() {
    private lateinit var habitsListLayout: LinearLayout
    private lateinit var emptyHabitsText: TextView
    private lateinit var completedHabitsCount: TextView
    private lateinit var totalHabitsCount: TextView
    private lateinit var completionRate: TextView
    private lateinit var progressText: TextView
    private lateinit var todayDateText: TextView

    private var selectedCategory = HabitCategory.ALL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_habit)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize HabitManager with context
        HabitManager.initialize(this)

        initializeViews()
        setupBottomNavigation()
        setupClickListeners()
        updateUI()
    }

    override fun onResume() {
        super.onResume()
        // Reset daily habits if it's a new day
        HabitManager.resetDailyHabits(this)
        updateUI()
    }

    private fun initializeViews() {
        habitsListLayout = findViewById(R.id.habitsListLayout)
        emptyHabitsText = findViewById(R.id.emptyHabitsText)
        completedHabitsCount = findViewById(R.id.completedHabitsCount)
        totalHabitsCount = findViewById(R.id.totalHabitsCount)
        completionRate = findViewById(R.id.completionRate)
        progressText = findViewById(R.id.progressText)
        todayDateText = findViewById(R.id.todayDateText)

        // Set today's date
        val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        todayDateText.text = dateFormat.format(Date())
    }

    private fun setupClickListeners() {
        // Add Habit Button
        findViewById<MaterialButton>(R.id.addHabitButton).setOnClickListener {
            val intent = Intent(this, AddEditHabitActivity::class.java)
            startActivityForResult(intent, ADD_HABIT_REQUEST)
        }

        // Complete All Button
        findViewById<MaterialButton>(R.id.completeAllButton).setOnClickListener {
            completeAllHabits()
        }

        // Category Buttons
        findViewById<MaterialButton>(R.id.categoryAll).setOnClickListener {
            selectedCategory = HabitCategory.ALL
            updateUI()
        }
        findViewById<MaterialButton>(R.id.categoryHealth).setOnClickListener {
            selectedCategory = HabitCategory.HEALTH
            updateUI()
        }
        findViewById<MaterialButton>(R.id.categoryFitness).setOnClickListener {
            selectedCategory = HabitCategory.FITNESS
            updateUI()
        }
        findViewById<MaterialButton>(R.id.categoryProductivity).setOnClickListener {
            selectedCategory = HabitCategory.PRODUCTIVITY
            updateUI()
        }
        findViewById<MaterialButton>(R.id.categoryMindfulness).setOnClickListener {
            selectedCategory = HabitCategory.MINDFULNESS
            updateUI()
        }
        findViewById<MaterialButton>(R.id.categoryLearning).setOnClickListener {
            selectedCategory = HabitCategory.LEARNING
            updateUI()
        }
    }

    private fun updateUI() {
        updateProgressStats()
        updateHabitsList()
    }

    private fun updateProgressStats() {
        val (completed, total, rate) = HabitManager.getTodayProgress()

        completedHabitsCount.text = completed.toString()
        totalHabitsCount.text = total.toString()
        completionRate.text = "$rate%"

        // Update progress text based on completion rate
        progressText.text = when {
            rate == 100 -> "Excellent! All done! 🎉"
            rate >= 80 -> "Great job! Almost there! 👏"
            rate >= 50 -> "Good progress! Keep it up! 💪"
            rate > 0 -> "Good start! Keep going! 🌟"
            else -> "Let's start building habits! 🚀"
        }
    }

    private fun updateHabitsList() {
        habitsListLayout.removeAllViews()

        val habits = HabitManager.getHabitsByCategory(selectedCategory)

        if (habits.isEmpty()) {
            emptyHabitsText.visibility = View.VISIBLE
        } else {
            emptyHabitsText.visibility = View.GONE

            habits.forEach { habit ->
                val habitItemView = createHabitItemView(habit)
                habitsListLayout.addView(habitItemView)
            }
        }
    }

    private fun createHabitItemView(habit: Habit): View {
        val habitItemView = layoutInflater.inflate(R.layout.item_habit, habitsListLayout, false)

        val titleTextView = habitItemView.findViewById<TextView>(R.id.habitTitle)
        val descriptionTextView = habitItemView.findViewById<TextView>(R.id.habitDescription)
        val categoryTextView = habitItemView.findViewById<TextView>(R.id.habitCategory)
        val completionCheckbox = habitItemView.findViewById<MaterialButton>(R.id.completionCheckbox)
        val streakTextView = habitItemView.findViewById<TextView>(R.id.streakText)
        val editButton = habitItemView.findViewById<ImageButton>(R.id.editButton)

        titleTextView.text = habit.title
        descriptionTextView.text = habit.description
        categoryTextView.text = habit.category.name
        streakTextView.text = "${habit.completedDays} days"

        // Update completion state
        updateCompletionUI(completionCheckbox, habit.isCompleted)

        // Toggle completion
        completionCheckbox.setOnClickListener {
            HabitManager.toggleHabitCompletion(this@Habit, habit.id)
            updateCompletionUI(completionCheckbox, !habit.isCompleted)
            updateUI()
        }

        // Edit button click
        editButton.setOnClickListener {
            editHabit(habit)
        }

        // Long click to edit/delete (keep for accessibility)
        habitItemView.setOnLongClickListener {
            showHabitOptions(habit)
            true
        }

        return habitItemView
    }

    private fun updateCompletionUI(button: MaterialButton, isCompleted: Boolean) {
        if (isCompleted) {
            button.text = "✓"
            button.setBackgroundColor(ContextCompat.getColor(this, R.color.green))
        } else {
            button.text = ""
            button.setBackgroundColor(ContextCompat.getColor(this, R.color.gray))
        }
    }

    private fun showHabitOptions(habit: Habit) {
        val options = arrayOf("Edit", "Delete")

        AlertDialog.Builder(this)
            .setTitle(habit.title)
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> editHabit(habit)
                    1 -> deleteHabit(habit)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun editHabit(habit: Habit) {
        val intent = Intent(this, AddEditHabitActivity::class.java)
        intent.putExtra("HABIT", habit)
        startActivityForResult(intent, EDIT_HABIT_REQUEST)
    }

    private fun deleteHabit(habit: Habit) {
        AlertDialog.Builder(this)
            .setTitle("Delete Habit")
            .setMessage("Are you sure you want to delete '${habit.title}'?")
            .setPositiveButton("Delete") { dialog, which ->
                HabitManager.deleteHabit(this@Habit, habit.id)
                updateUI()
                Toast.makeText(this, "Habit deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun completeAllHabits() {
        HabitManager.completeAllHabits(this)
        updateUI()
        Toast.makeText(this, "All habits completed!", Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            updateUI()
        }
    }

    companion object {
        private const val ADD_HABIT_REQUEST = 1001
        private const val EDIT_HABIT_REQUEST = 1002
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