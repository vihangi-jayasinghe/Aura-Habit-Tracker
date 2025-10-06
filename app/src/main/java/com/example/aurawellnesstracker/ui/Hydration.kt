package com.example.aurawellnesstracker.ui

import android.content.Intent
import android.graphics.*
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
import com.example.aurawellnesstracker.utils.DailyWaterData
import com.example.aurawellnesstracker.utils.HydrationManager
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.aurawellnesstracker.utils.NotificationHelper
import com.example.aurawellnesstracker.utils.ReminderScheduler
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
    private lateinit var weeklyChartLayout: LinearLayout

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

            // Load weekly chart with delay to ensure layout is ready
            weeklyChartLayout.post {
                loadWeeklyChart()
            }

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
        weeklyChartLayout = findViewById(R.id.weeklyChartLayout)
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
                    loadWeeklyChart() // Refresh chart when goal changes
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

    private fun testNotificationImmediately() {
        Log.d("HydrationTest", "Manual test notification triggered")
        NotificationHelper(this).showHydrationReminder()
        Toast.makeText(this, "Test notification sent!", Toast.LENGTH_SHORT).show()
    }

    private fun debugReminderSchedule() {
        val workManager = WorkManager.getInstance(this)
        workManager.getWorkInfosForUniqueWorkLiveData(ReminderScheduler.REMINDER_WORK_NAME)
            .observe(this) { workInfos ->
                workInfos.forEach { workInfo ->
                    Log.d("HydrationDebug", "Work State: ${workInfo.state}")
                    Log.d("HydrationDebug", "Work ID: ${workInfo.id}")
                    Log.d("HydrationDebug", "Tags: ${workInfo.tags}")

                    // Alternative way to check if work is scheduled
                    if (workInfo.state == WorkInfo.State.ENQUEUED) {
                        Log.d("HydrationDebug", "Work is scheduled and waiting")
                    }
                }

                // Log the count of work infos
                Log.d("HydrationDebug", "Total work infos: ${workInfos.size}")

                // Check if any work is enqueued
                val hasEnqueuedWork = workInfos.any { it.state == WorkInfo.State.ENQUEUED }
                Log.d("HydrationDebug", "Has enqueued work: $hasEnqueuedWork")
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
            loadWeeklyChart() // Refresh chart when water is added
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

        // Load water history
        loadWaterHistory()

        // Update goal seekbar
        waterGoalSeekBar.progress = (goal / 100) - 10
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
            loadWeeklyChart() // Refresh chart when entry is deleted
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

    private fun loadWeeklyChart() {
        try {
            weeklyChartLayout.removeAllViews()
            val weeklyData = hydrationManager.getWeeklyWaterData()

            if (weeklyData.isEmpty()) {
                val emptyText = TextView(this).apply {
                    text = "No weekly data available"
                    setTextColor(getColor(R.color.text_secondary))
                    textSize = 14f
                    gravity = android.view.Gravity.CENTER
                    setPadding(0, 40, 0, 40)
                }
                weeklyChartLayout.addView(emptyText)
                return
            }

            // Create line chart view
            val lineChartView = LineChartView(this, weeklyData)
            weeklyChartLayout.addView(lineChartView)

        } catch (e: Exception) {
            Log.e("WeeklyChart", "Error loading weekly chart", e)
            // Show error message
            val errorText = TextView(this).apply {
                text = "Error loading chart"
                setTextColor(getColor(R.color.error_color))
                textSize = 14f
                gravity = android.view.Gravity.CENTER
                setPadding(0, 40, 0, 40)
            }
            weeklyChartLayout.addView(errorText)
        }
    }

    companion object {
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001
    }

    // Add this method to check and request permission
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

    // Add this to handle permission result
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
            loadWeeklyChart()
        }

        findViewById<ImageView>(R.id.profileBtn11).setOnClickListener {
            val intent = Intent(this, Settings::class.java)
            startActivity(intent)
            finish()
            overridePendingTransition(0, 0)
        }
    }
}

// Custom Line Chart View
class LineChartView(context: android.content.Context, private val weeklyData: List<DailyWaterData>) : View(context) {
    private val paintLine = Paint().apply {
        color = Color.parseColor("#2196F3")
        style = Paint.Style.STROKE
        strokeWidth = 4f
        isAntiAlias = true
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val paintFill = Paint().apply {
        color = Color.parseColor("#E3F2FD")
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val paintPoint = Paint().apply {
        color = Color.parseColor("#2196F3")
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val paintText = Paint().apply {
        color = Color.parseColor("#757575")
        textSize = 36f
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }

    private val paintGrid = Paint().apply {
        color = Color.parseColor("#E0E0E0")
        style = Paint.Style.STROKE
        strokeWidth = 1f
        isAntiAlias = true
    }

    private val paddingLeft = 60f
    private val paddingRight = 60f
    private val paddingTop = 40f
    private val paddingBottom = 60f

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (weeklyData.isEmpty()) return

        val width = width.toFloat()
        val height = height.toFloat()
        val chartWidth = width - paddingLeft - paddingRight
        val chartHeight = height - paddingTop - paddingBottom

        // Draw grid lines
        for (i in 0..4) {
            val y = paddingTop + (chartHeight * i / 4)
            canvas.drawLine(paddingLeft, y, width - paddingRight, y, paintGrid)
        }

        // Draw Y-axis labels (0%, 25%, 50%, 75%, 100%)
        for (i in 0..4) {
            val y = paddingTop + (chartHeight * i / 4)
            val percentage = 100 - (i * 25)
            canvas.drawText("$percentage%", paddingLeft - 30, y + 10, paintText.apply { textSize = 28f })
        }

        // Find max percentage for scaling
        val maxPercentage = weeklyData.maxOfOrNull { it.percentage } ?: 100
        val scaleFactor = if (maxPercentage > 0) 100f / maxPercentage else 1f

        // Prepare points for the line
        val points = mutableListOf<PointF>()
        val xStep = chartWidth / (weeklyData.size - 1)

        weeklyData.forEachIndexed { index, dayData ->
            val x = paddingLeft + (index * xStep)
            // Scale the percentage to fit within chart height, but cap at 100%
            val scaledPercentage = (dayData.percentage * scaleFactor).coerceAtMost(100f)
            val y = paddingTop + chartHeight - (chartHeight * scaledPercentage / 100f)
            points.add(PointF(x, y))

            // Draw day labels at bottom
            canvas.drawText(dayData.dayName, x, height - paddingBottom + 40, paintText.apply { textSize = 32f })

            // Draw percentage labels above points
            canvas.drawText("${dayData.percentage}%", x, y - 20, paintText.apply {
                textSize = 28f
                color = when {
                    dayData.percentage >= 100 -> Color.parseColor("#4CAF50")
                    dayData.percentage >= 50 -> Color.parseColor("#2196F3")
                    else -> Color.parseColor("#F44336")
                }
            })
        }

        // Draw filled area under the line
        if (points.size >= 2) {
            val path = Path()
            path.moveTo(paddingLeft, height - paddingBottom)
            points.forEach { point ->
                path.lineTo(point.x, point.y)
            }
            path.lineTo(width - paddingRight, height - paddingBottom)
            path.close()
            canvas.drawPath(path, paintFill)
        }

        // Draw the line
        if (points.size >= 2) {
            for (i in 0 until points.size - 1) {
                canvas.drawLine(points[i].x, points[i].y, points[i + 1].x, points[i + 1].y, paintLine)
            }
        }

        // Draw points
        points.forEach { point ->
            canvas.drawCircle(point.x, point.y, 8f, paintPoint)
            // Draw outer circle
            paintPoint.color = Color.parseColor("#BBDEFB")
            canvas.drawCircle(point.x, point.y, 12f, paintPoint)
            paintPoint.color = Color.parseColor("#2196F3")
            canvas.drawCircle(point.x, point.y, 6f, paintPoint)
        }
    }
}