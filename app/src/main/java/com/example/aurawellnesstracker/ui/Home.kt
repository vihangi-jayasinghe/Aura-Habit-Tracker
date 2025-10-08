package com.example.aurawellnesstracker.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.aurawellnesstracker.R
import com.example.aurawellnesstracker.utils.HabitManager
import com.example.aurawellnesstracker.utils.HydrationManager
import com.example.aurawellnesstracker.utils.MoodManager
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.*

class Home : AppCompatActivity() {

    private lateinit var hydrationManager: HydrationManager
    private lateinit var moodManager: MoodManager

    private lateinit var todayDateText: TextView
    private lateinit var moodStatValue: TextView
    private lateinit var hydrationStatValue: TextView
    private lateinit var habitsStatValue: TextView
    private lateinit var streakStatValue: TextView
    private lateinit var recentActivityLayout: LinearLayout
    private lateinit var emptyActivityText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        initializeManagers()
        initializeViews()
        setupButtonListeners()
        setupBottomNavigation()
        updateHomeData()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onResume() {
        super.onResume()
        updateHomeData()
    }

    private fun initializeManagers() {
        hydrationManager = HydrationManager(this)
        moodManager = MoodManager(this)
        HabitManager.initialize(this)
    }

    private fun initializeViews() {
        todayDateText = findViewById(R.id.todayDateText)
        moodStatValue = findViewById(R.id.moodStatValue)
        hydrationStatValue = findViewById(R.id.hydrationStatValue)
        habitsStatValue = findViewById(R.id.habitsStatValue)
        streakStatValue = findViewById(R.id.streakStatValue)
        recentActivityLayout = findViewById(R.id.recentActivityLayout)
        emptyActivityText = findViewById(R.id.emptyActivityText)
    }

    private fun setupButtonListeners() {
        findViewById<MaterialButton>(R.id.logMoodButton).setOnClickListener {
            val intent = Intent(this, Mood::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        findViewById<MaterialButton>(R.id.addWaterButton).setOnClickListener {
            val intent = Intent(this, Hydration::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        findViewById<MaterialButton>(R.id.trackHabitButton).setOnClickListener {
            val intent = Intent(this, com.example.aurawellnesstracker.ui.Habit::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        findViewById<MaterialButton>(R.id.viewProgressButton).setOnClickListener {
            val intent = Intent(this, OverviewActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }
    }

    private fun updateHomeData() {
        updateDateDisplay()
        updateStats()
        loadRecentActivity()
    }

    private fun updateDateDisplay() {
        val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        val currentDate = dateFormat.format(Date())
        todayDateText.text = currentDate
    }

    private fun updateStats() {
        // Update Mood Stat
        val todayMoodEntries = moodManager.getTodayMoodEntries()
        moodStatValue.text = if (todayMoodEntries.isNotEmpty()) {
            todayMoodEntries.first().emoji
        } else {
            "😊"
        }

        // Update Hydration Stat
        val todayWater = hydrationManager.getTodayTotalWater()
        hydrationStatValue.text = String.format("%.1fL", todayWater / 1000.0)

        // Update Habits Stat
        val allHabits = HabitManager.getAllHabits()
        val completedHabits = allHabits.count { it.isCompleted }
        habitsStatValue.text = "$completedHabits/${allHabits.size}"

        // Update Streak Stat
        val averageStreak = if (allHabits.isNotEmpty()) {
            allHabits.sumOf { it.completedDays } / allHabits.size
        } else {
            0
        }
        streakStatValue.text = "$averageStreak days"
    }

    private fun loadRecentActivity() {
        recentActivityLayout.removeAllViews()
        val activities = getRecentActivities()

        if (activities.isEmpty()) {
            emptyActivityText.visibility = View.VISIBLE
            return
        }

        emptyActivityText.visibility = View.GONE

        activities.forEach { activity ->
            val activityView = createActivityView(activity)
            recentActivityLayout.addView(activityView)
        }
    }

    private fun getRecentActivities(): List<HomeActivity> {
        val activities = mutableListOf<HomeActivity>()

        // Add today's mood entries
        val todayMoodEntries = moodManager.getTodayMoodEntries()
        todayMoodEntries.take(2).forEach { moodEntry ->
            activities.add(HomeActivity(
                type = "Mood",
                description = "Felt ${moodEntry.moodType}",
                time = moodEntry.timestamp,
                iconRes = R.drawable.ic_mood_happy
            ))
        }

        // Add today's water entries (last 2)
        val todayWaterEntries = hydrationManager.getTodayWaterEntries()
            .sortedByDescending { it.timestamp }
            .take(2)
        todayWaterEntries.forEach { waterEntry ->
            activities.add(HomeActivity(
                type = "Hydration",
                description = "Drank ${waterEntry.amount}ml water",
                time = waterEntry.timestamp,
                iconRes = R.drawable.ic_water
            ))
        }

        // Add today's completed habits
        val completedHabits = HabitManager.getAllHabits()
            .filter { it.isCompleted }
        completedHabits.take(2).forEach { habit ->
            activities.add(HomeActivity(
                type = "Habit",
                description = "Completed: ${habit.title}",
                time = habit.createdAt.time,
                iconRes = R.drawable.ic_habits
            ))
        }

        return activities.sortedByDescending { it.time }.take(5)
    }

    private fun createActivityView(activity: HomeActivity): View {
        val inflater = LayoutInflater.from(this)
        val activityView = inflater.inflate(R.layout.item_recent_activity, recentActivityLayout, false)

        val iconImage = activityView.findViewById<ImageView>(R.id.activityIcon)
        val typeText = activityView.findViewById<TextView>(R.id.activityType)
        val descriptionText = activityView.findViewById<TextView>(R.id.activityDescription)
        val timeText = activityView.findViewById<TextView>(R.id.activityTime)

        iconImage.setImageResource(activity.iconRes)
        typeText.text = activity.type
        descriptionText.text = activity.description

        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        timeText.text = timeFormat.format(Date(activity.time))

        return activityView
    }

    private fun setupBottomNavigation() {
        val homeBtn = findViewById<ImageView>(R.id.homeBtn10)

        // Set home icon to blue
        homeBtn.setColorFilter(ContextCompat.getColor(this, R.color.primary_color))

        homeBtn.setOnClickListener {
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

    data class HomeActivity(
        val type: String,
        val description: String,
        val time: Long,
        val iconRes: Int
    )
}