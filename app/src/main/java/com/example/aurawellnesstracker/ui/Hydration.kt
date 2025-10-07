package com.example.aurawellnesstracker.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.aurawellnesstracker.R
import com.example.aurawellnesstracker.model.WaterEntry
import com.example.aurawellnesstracker.utils.HydrationManager
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.*

class Hydration : AppCompatActivity() {
    private lateinit var hydrationManager: HydrationManager
    private lateinit var hydrationProgress: ProgressBar
    private lateinit var currentWaterIntake: TextView
    private lateinit var progressText: TextView
    private lateinit var currentGoalText: TextView
    private lateinit var waterHistoryLayout: LinearLayout
    private lateinit var emptyHistoryText: TextView
    private lateinit var reminderSwitch: Switch
    private lateinit var waterGoalSeekBar: SeekBar
    private lateinit var reminderIntervalSpinner: Spinner
    private lateinit var todayDateText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_hydration)

        try {
            hydrationManager = HydrationManager(this)
            initializeViews()
            setupButtonListeners()
            setupBottomNavigation()
            loadTodayWaterData()
            setupReminderSettings()
            updateDateDisplay()

            checkAndRequestNotificationPermission()

            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        } catch (e: Exception) {
            Log.e("Hydration", "Error in onCreate", e)
            Toast.makeText(this, "App error occurred", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initializeViews() {
        hydrationProgress = findViewById(R.id.hydrationProgress)
        currentWaterIntake = findViewById(R.id.currentWaterIntake)
        progressText = findViewById(R.id.progressText)
        currentGoalText = findViewById(R.id.currentGoalText)
        waterHistoryLayout = findViewById(R.id.waterHistoryLayout)
        emptyHistoryText = findViewById(R.id.emptyHistoryText)
        reminderSwitch = findViewById(R.id.reminderSwitch)
        waterGoalSeekBar = findViewById(R.id.waterGoalSeekBar)
        reminderIntervalSpinner = findViewById(R.id.reminderIntervalSpinner)
        todayDateText = findViewById(R.id.todayDateText)
    }

    private fun setupButtonListeners() {
        // Quick add water buttons
        findViewById<MaterialButton>(R.id.waterSmall).setOnClickListener {
            addWater(250)
        }

        findViewById<MaterialButton>(R.id.waterMedium).setOnClickListener {
            addWater(500)
        }

        findViewById<MaterialButton>(R.id.waterLarge).setOnClickListener {
            addWater(1000)
        }

        // Custom water amount
        findViewById<MaterialButton>(R.id.addCustomWater).setOnClickListener {
            addCustomWater()
        }

        // Goal seekbar listener
        waterGoalSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val goal = (progress + 10) * 100 // Convert to ml (1.0L to 4.0L)
                    hydrationManager.setDailyGoal(goal)
                    updateProgressDisplay()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Reminder switch
        reminderSwitch.setOnCheckedChangeListener { _, isChecked ->
            hydrationManager.setReminderEnabled(isChecked)
            val message = if (isChecked) {
                "Reminders enabled"
            } else {
                "Reminders disabled"
            }
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun addWater(amount: Int) {
        val entry = WaterEntry(
            id = UUID.randomUUID().toString(),
            amount = amount
        )

        if (hydrationManager.addWaterEntry(entry)) {
            Toast.makeText(this, "Added ${amount}ml water", Toast.LENGTH_SHORT).show()
            loadTodayWaterData()
        }
    }

    private fun addCustomWater() {
        val customAmountEditText = findViewById<EditText>(R.id.customWaterAmount)
        val amountText = customAmountEditText.text.toString()

        if (amountText.isNotEmpty()) {
            val amount = amountText.toIntOrNull() ?: 0
            if (amount > 0) {
                addWater(amount)
                customAmountEditText.setText("")
            } else {
                Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadTodayWaterData() {
        val todayTotal = hydrationManager.getTodayTotalWater()
        val goal = hydrationManager.getDailyGoal()
        val percentage = hydrationManager.getCompletionPercentage()

        // Update progress
        hydrationProgress.progress = percentage
        currentWaterIntake.text = String.format("%.1f", todayTotal / 1000.0)
        currentGoalText.text = String.format("%.1fL", goal / 1000.0)

        // Update progress text
        progressText.text = "$percentage% Completed"

        // Update goal info text dynamically
        updateGoalInfoText(goal)

        // Load water history
        loadWaterHistory()

        // Update goal seekbar
        waterGoalSeekBar.progress = (goal / 100) - 10
    }

    private fun updateGoalInfoText(goal: Int) {
        // Find the goal info text view in the circular progress layout
        val circularProgressLayout = findViewById<RelativeLayout>(R.id.circularProgressLayout)
        val goalInfoText = circularProgressLayout?.findViewById<TextView>(R.id.goalInfoText)

        goalInfoText?.text = String.format("of %.1fL goal", goal / 1000.0)
    }

    private fun loadWaterHistory() {
        waterHistoryLayout.removeAllViews()
        val todayEntries = hydrationManager.getTodayWaterEntries()

        if (todayEntries.isEmpty()) {
            emptyHistoryText.visibility = View.VISIBLE
            return
        }

        emptyHistoryText.visibility = View.GONE

        todayEntries.sortedByDescending { it.timestamp }.forEach { entry ->
            val entryView = createWaterEntryView(entry)
            waterHistoryLayout.addView(entryView)
        }
    }

    private fun createWaterEntryView(entry: WaterEntry): View {
        val inflater = LayoutInflater.from(this)
        val entryView = inflater.inflate(R.layout.item_water_entry, waterHistoryLayout, false)

        val amountText = entryView.findViewById<TextView>(R.id.entryAmount)
        val timeText = entryView.findViewById<TextView>(R.id.entryTime)
        val deleteButton = entryView.findViewById<ImageButton>(R.id.deleteButton)

        amountText.text = "${entry.amount}ml"

        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        timeText.text = timeFormat.format(Date(entry.timestamp))

        deleteButton.setOnClickListener {
            deleteWaterEntry(entry.id)
        }

        return entryView
    }

    private fun deleteWaterEntry(entryId: String) {
        if (hydrationManager.deleteWaterEntry(entryId)) {
            Toast.makeText(this, "Water entry deleted", Toast.LENGTH_SHORT).show()
            loadTodayWaterData()
        }
    }

    private fun setupReminderSettings() {
        // Set initial switch state
        reminderSwitch.isChecked = hydrationManager.isReminderEnabled()

        // Setup reminder interval spinner
        val intervals = arrayOf("5 minutes", "10 minutes", "20 minutes", "1 hour")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, intervals)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        reminderIntervalSpinner.adapter = adapter

        // Set current interval
        val currentInterval = hydrationManager.getReminderInterval()
        val position = when (currentInterval) {
            5 -> 0
            10 -> 1
            20 -> 2
            60 -> 3
            else -> 1
        }
        reminderIntervalSpinner.setSelection(position)

        reminderIntervalSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val interval = when (position) {
                    0 -> 5
                    1 -> 10
                    2 -> 20
                    3 -> 60
                    else -> 10
                }
                hydrationManager.setReminderInterval(interval)

                // Show confirmation toast
                val intervalText = intervals[position]
                Toast.makeText(this@Hydration,
                    "Reminders set to $intervalText",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun updateDateDisplay() {
        val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        val currentDate = dateFormat.format(Date())
        todayDateText.text = currentDate
    }

    private fun updateProgressDisplay() {
        loadTodayWaterData()
    }

    companion object {
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001
    }

    private fun checkAndRequestNotificationPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission already granted
                    Log.d("Notification", "Notification permission granted")
                }
                ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) -> {
                    // Explain why you need the permission
                    Toast.makeText(this, "Notifications help remind you to drink water", Toast.LENGTH_LONG).show()
                    requestNotificationPermission()
                }
                else -> {
                    // Request the permission
                    requestNotificationPermission()
                }
            }
        }
    }

    private fun requestNotificationPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                NOTIFICATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            NOTIFICATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupBottomNavigation() {
        findViewById<ImageView>(R.id.homeBtn10).setOnClickListener {
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
            finish()
            overridePendingTransition(0, 0)
        }

        findViewById<ImageView>(R.id.productBtn10).setOnClickListener {
            val intent = Intent(this, Mood::class.java)
            startActivity(intent)
            finish()
            overridePendingTransition(0, 0)
        }

        findViewById<ImageView>(R.id.expertsBtn10).setOnClickListener {
            val intent = Intent(this, Habit::class.java)
            startActivity(intent)
            finish()
            overridePendingTransition(0, 0)
        }

        findViewById<ImageView>(R.id.profileBtn10).setOnClickListener {
            // Already on hydration page, refresh
            loadTodayWaterData()
        }

        findViewById<ImageView>(R.id.profileBtn11).setOnClickListener {
            val intent = Intent(this, Settings::class.java)
            startActivity(intent)
            finish()
            overridePendingTransition(0, 0)
        }
    }
}