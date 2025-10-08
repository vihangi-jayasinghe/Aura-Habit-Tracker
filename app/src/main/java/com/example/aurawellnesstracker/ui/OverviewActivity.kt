package com.example.aurawellnesstracker.ui

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.aurawellnesstracker.R
import com.example.aurawellnesstracker.utils.HabitManager
import com.example.aurawellnesstracker.utils.HydrationManager
import com.example.aurawellnesstracker.utils.MoodManager
import java.util.*

class OverviewActivity : AppCompatActivity() {

    private lateinit var hydrationManager: HydrationManager
    private lateinit var moodManager: MoodManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_overview)

        initializeManagers()
        setupBasicCharts()

        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            Log.d("OverviewActivity", "Back button clicked")
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        refreshData()
    }

    private fun refreshData() {
        setupBasicCharts()
    }

    private fun initializeManagers() {
        hydrationManager = HydrationManager(this)
        moodManager = MoodManager(this)
        HabitManager.initialize(this)
    }

    private fun setupBasicCharts() {
        setupMoodSummary()
        setupHydrationChart()
        setupHabitsStats()
    }

    private fun setupMoodSummary() {
        val moodData = getLast7DaysMoodData()
        val days = getLast7DaysLabels()

        val chartLayout = findViewById<LinearLayout>(R.id.moodChartLayout)
        chartLayout.removeAllViews()
        chartLayout.orientation = LinearLayout.VERTICAL

        // Modern mood chart
        val moodChart = createModernMoodChart(moodData, days)
        chartLayout.addView(moodChart)

        // Overall weekly stats
        val weeklyStats = createWeeklyMoodStats(moodData, days)
        chartLayout.addView(weeklyStats)
    }

    private fun createModernMoodChart(moodData: List<Float>, days: Array<String>): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, dpToPx(16))
            }

            // Title
            val title = TextView(this@OverviewActivity).apply {
                text = "7-Day Mood Trend"
                textSize = 18f
                setTextColor(ContextCompat.getColor(this@OverviewActivity, R.color.text_primary))
                setTypeface(typeface, Typeface.BOLD)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, dpToPx(16))
                }
            }
            addView(title)

            // Chart container
            val chartContainer = LinearLayout(this@OverviewActivity).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dpToPx(200)
                )
                setBackgroundResource(R.drawable.white_rounded_background)
                setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(8))
            }

            // Mood line chart area
            val chartArea = LinearLayout(this@OverviewActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0,
                    1f
                )
                gravity = Gravity.BOTTOM
            }

            val maxMoodValue = 5f
            val chartHeight = dpToPx(150)

            days.forEachIndexed { index, day ->
                val moodValue = moodData[index]
                chartArea.addView(createMoodChartColumn(day, moodValue, maxMoodValue, chartHeight))
            }

            chartContainer.addView(chartArea)

            // Single day labels at bottom (no duplicates)
            val dayLabelsLayout = LinearLayout(this@OverviewActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                gravity = Gravity.CENTER_HORIZONTAL
            }

            days.forEach { day ->
                val dayLabel = TextView(this@OverviewActivity).apply {
                    text = day
                    textSize = 10f
                    setTextColor(ContextCompat.getColor(this@OverviewActivity, R.color.text_secondary))
                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                    gravity = Gravity.CENTER
                }
                dayLabelsLayout.addView(dayLabel)
            }

            chartContainer.addView(dayLabelsLayout)
            addView(chartContainer)

            // Mood legend
            val legendLayout = createMoodLegend()
            addView(legendLayout)
        }
    }

    private fun createMoodChartColumn(day: String, moodValue: Float, maxValue: Float, chartHeight: Int): LinearLayout {
        val hasData = moodValue > 0
        val normalizedHeight = if (hasData) (moodValue / maxValue * chartHeight * 0.8).toInt() else 0
        val minHeight = dpToPx(4)
        val actualHeight = maxOf(normalizedHeight, minHeight)

        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1f
            )
            gravity = Gravity.BOTTOM

            // Emoji indicator (replaces the number)
            if (hasData) {
                val emojiIndicator = TextView(this@OverviewActivity).apply {
                    text = getMoodEmoji(moodValue)
                    textSize = 16f
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        gravity = Gravity.CENTER_HORIZONTAL
                        setMargins(0, 0, 0, dpToPx(4))
                    }
                    gravity = Gravity.CENTER
                }
                addView(emojiIndicator)
            } else {
                // Show a dash for no data
                val noDataIndicator = TextView(this@OverviewActivity).apply {
                    text = "—"
                    textSize = 14f
                    setTextColor(ContextCompat.getColor(this@OverviewActivity, R.color.text_secondary))
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        gravity = Gravity.CENTER_HORIZONTAL
                        setMargins(0, 0, 0, dpToPx(4))
                    }
                    gravity = Gravity.CENTER
                }
                addView(noDataIndicator)
            }

            // Mood bar with gradient effect
            val moodBar = LinearLayout(this@OverviewActivity).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    dpToPx(24),
                    actualHeight
                ).apply {
                    setMargins(dpToPx(4), 0, dpToPx(4), 0)
                }
                gravity = Gravity.CENTER

                if (hasData) {
                    // Main bar
                    val mainBar = View(this@OverviewActivity).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            dpToPx(20),
                            maxOf(actualHeight - dpToPx(8), dpToPx(4))
                        )
                        setBackgroundColor(getMoodColor(moodValue))
                    }

                    // Top rounded cap
                    val topCap = View(this@OverviewActivity).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            dpToPx(16),
                            dpToPx(4)
                        ).apply {
                            setMargins(0, 0, 0, -dpToPx(2))
                        }
                        setBackgroundResource(R.drawable.mood_bar_top_cap)
                        setBackgroundColor(getMoodColor(moodValue))
                    }

                    addView(mainBar)
                    if (actualHeight > dpToPx(12)) {
                        addView(topCap)
                    }
                } else {
                    // Empty state - dotted line
                    val emptyBar = View(this@OverviewActivity).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            dpToPx(16),
                            dpToPx(4)
                        )
                        setBackgroundResource(R.drawable.dotted_line)
                    }
                    addView(emptyBar)
                }
            }

            addView(moodBar)
        }
    }

    private fun createMoodLegend(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, dpToPx(12), 0, 0)
            }
            gravity = Gravity.CENTER

            val moodLevels = listOf(
                "😞" to "Poor",
                "😔" to "Low",
                "😐" to "Neutral",
                "🙂" to "Good",
                "😊" to "Excellent"
            )

            moodLevels.forEach { (emoji, label) ->
                val legendItem = LinearLayout(this@OverviewActivity).apply {
                    orientation = LinearLayout.HORIZONTAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(dpToPx(8), 0, dpToPx(8), 0)
                    }
                    gravity = Gravity.CENTER_VERTICAL
                }

                val emojiView = TextView(this@OverviewActivity).apply {
                    text = emoji
                    textSize = 12f
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(0, 0, dpToPx(2), 0)
                    }
                }

                val labelText = TextView(this@OverviewActivity).apply {
                    text = label
                    textSize = 10f
                    setTextColor(ContextCompat.getColor(this@OverviewActivity, R.color.text_secondary))
                }

                legendItem.addView(emojiView)
                legendItem.addView(labelText)
                addView(legendItem)
            }
        }
    }

    private fun getMoodEmoji(moodValue: Float): String {
        return when {
            moodValue >= 4.5 -> "😊" // Excellent
            moodValue >= 3.5 -> "🙂" // Good
            moodValue >= 2.5 -> "😐" // Neutral
            moodValue >= 1.5 -> "😔" // Low
            else -> "😞" // Poor
        }
    }

    private fun getMoodColor(moodValue: Float): Int {
        return when {
            moodValue >= 4.5 -> Color.parseColor("#4CAF50") // Green - Excellent
            moodValue >= 3.5 -> Color.parseColor("#8BC34A") // Light Green - Good
            moodValue >= 2.5 -> Color.parseColor("#FFC107") // Amber - Neutral
            moodValue >= 1.5 -> Color.parseColor("#FF9800") // Orange - Low
            else -> Color.parseColor("#F44336") // Red - Poor
        }
    }

    private fun createWeeklyMoodStats(moodData: List<Float>, days: Array<String>): LinearLayout {
        val validMoods = moodData.filter { it > 0 }
        val averageMood = if (validMoods.isNotEmpty()) validMoods.average() else 0.0
        val bestDayIndex = moodData.indexOfFirst { it == moodData.maxOrNull() }
        val worstDayIndex = moodData.indexOfFirst { it > 0 && it == moodData.filter { it > 0 }.minOrNull() }
        val moodDays = validMoods.size

        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, dpToPx(24), 0, 0)
            }

            // Title
            val title = TextView(this@OverviewActivity).apply {
                text = "Weekly Summary"
                textSize = 16f
                setTextColor(ContextCompat.getColor(this@OverviewActivity, R.color.text_primary))
                setTypeface(typeface, Typeface.BOLD)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, dpToPx(12))
                }
            }
            addView(title)

            // Stats Grid
            val statsGrid = LinearLayout(this@OverviewActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                weightSum = 2f
            }

            // Left Column
            val leftColumn = LinearLayout(this@OverviewActivity).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            }

            // Average Mood
            leftColumn.addView(createStatItem(
                "Average Mood",
                getMoodDescription(averageMood.toFloat()),
                getMoodEmoji(averageMood.toFloat()),
                getMoodColor(averageMood.toFloat())
            ))

            // Tracked Days
            leftColumn.addView(createStatItem(
                "Tracked Days",
                "$moodDays/7 days",
                "📅",
                Color.parseColor("#2196F3")
            ))

            // Right Column
            val rightColumn = LinearLayout(this@OverviewActivity).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            }

            // Best Day
            val bestDayText = if (bestDayIndex != -1 && moodData[bestDayIndex] > 0)
                days[bestDayIndex] else "No data"
            rightColumn.addView(createStatItem(
                "Best Day",
                bestDayText,
                "⭐",
                Color.parseColor("#FFA726")
            ))

            // Worst Day
            val worstDayText = if (worstDayIndex != -1)
                days[worstDayIndex] else "No data"
            rightColumn.addView(createStatItem(
                "Worst Day",
                worstDayText,
                "💪",
                Color.parseColor("#EF476F")
            ))

            statsGrid.addView(leftColumn)
            statsGrid.addView(rightColumn)
            addView(statsGrid)
        }
    }

    private fun createStatItem(title: String, value: String, emoji: String, color: Int): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, dpToPx(8), dpToPx(12))
            }
            setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12))
            setBackgroundResource(R.drawable.white_rounded_background)

            // Emoji and value
            val topRow = LinearLayout(this@OverviewActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                gravity = Gravity.CENTER_VERTICAL
            }

            val emojiView = TextView(this@OverviewActivity).apply {
                text = emoji
                textSize = 20f
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, dpToPx(8), 0)
                }
            }

            val valueView = TextView(this@OverviewActivity).apply {
                text = value
                textSize = 16f
                setTextColor(color)
                setTypeface(typeface, Typeface.BOLD)
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            }

            topRow.addView(emojiView)
            topRow.addView(valueView)

            // Title
            val titleView = TextView(this@OverviewActivity).apply {
                text = title
                textSize = 12f
                setTextColor(ContextCompat.getColor(this@OverviewActivity, R.color.text_secondary))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            addView(topRow)
            addView(titleView)
        }
    }

    private fun getMoodDescription(moodValue: Float): String {
        return when {
            moodValue >= 4.5 -> "Excellent"
            moodValue >= 3.5 -> "Good"
            moodValue >= 2.5 -> "Neutral"
            moodValue >= 1.5 -> "Not great"
            else -> "Poor"
        }
    }

    private fun setupHydrationChart() {
        val hydrationData = getLast7DaysHydrationData()
        val chartLayout = findViewById<LinearLayout>(R.id.hydrationChartLayout)
        chartLayout.removeAllViews()
        chartLayout.orientation = LinearLayout.HORIZONTAL
        chartLayout.gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL

        val days = getLast7DaysLabels()

        days.forEachIndexed { index, day ->
            val dayLayout = createDayChartLayout(
                day = day,
                value = hydrationData[index],
                maxValue = getMaxHydrationValue(),
                unit = "L",
                color = Color.parseColor("#118AB2")
            )
            chartLayout.addView(dayLayout)
        }
    }

    private fun setupHabitsStats() {
        val chartLayout = findViewById<LinearLayout>(R.id.habitsChartLayout)
        chartLayout.removeAllViews()
        chartLayout.orientation = LinearLayout.VERTICAL

        val statsView = createHabitsStatsView()
        chartLayout.addView(statsView)
    }

    private fun createHabitsStatsView(): LinearLayout {
        val allHabits = HabitManager.getAllHabits()
        val completedToday = allHabits.count { it.isCompleted }
        val totalHabits = allHabits.size
        val completionRate = if (totalHabits > 0) (completedToday * 100) / totalHabits else 0
        val bestStreak = allHabits.maxOfOrNull { it.completedDays } ?: 0
        val totalCompletions = allHabits.sumOf { it.completedDays }

        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            // Title
            val title = TextView(this@OverviewActivity).apply {
                text = "Habit Statistics"
                textSize = 16f
                setTextColor(ContextCompat.getColor(this@OverviewActivity, R.color.text_primary))
                setTypeface(typeface, Typeface.BOLD)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, dpToPx(16))
                }
            }
            addView(title)

            // Stats Grid
            val statsGrid = LinearLayout(this@OverviewActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                weightSum = 2f
            }

            // Left Column
            val leftColumn = LinearLayout(this@OverviewActivity).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            }

            leftColumn.addView(createHabitStatCard(
                "Today's Progress",
                "$completedToday/$totalHabits",
                "📊",
                Color.parseColor("#FFA726")
            ))

            leftColumn.addView(createHabitStatCard(
                "Completion Rate",
                "$completionRate%",
                "✅",
                Color.parseColor("#06D6A0")
            ))

            // Right Column
            val rightColumn = LinearLayout(this@OverviewActivity).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            }

            rightColumn.addView(createHabitStatCard(
                "Best Streak",
                "$bestStreak days",
                "🔥",
                Color.parseColor("#EF476F")
            ))

            rightColumn.addView(createHabitStatCard(
                "Total Completions",
                totalCompletions.toString(),
                "⭐",
                Color.parseColor("#118AB2")
            ))

            statsGrid.addView(leftColumn)
            statsGrid.addView(rightColumn)
            addView(statsGrid)

            // Habit list summary
            if (allHabits.isNotEmpty()) {
                val habitSummary = createHabitSummary(allHabits)
                addView(habitSummary)
            }
        }
    }

    private fun createHabitStatCard(title: String, value: String, emoji: String, color: Int): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4))
            }
            setPadding(dpToPx(12), dpToPx(16), dpToPx(12), dpToPx(16))
            setBackgroundResource(R.drawable.white_rounded_background)

            // Emoji and value
            val topRow = LinearLayout(this@OverviewActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                gravity = Gravity.CENTER_VERTICAL
            }

            val emojiView = TextView(this@OverviewActivity).apply {
                text = emoji
                textSize = 20f
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, dpToPx(8), 0)
                }
            }

            val valueView = TextView(this@OverviewActivity).apply {
                text = value
                textSize = 18f
                setTextColor(color)
                setTypeface(typeface, Typeface.BOLD)
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            }

            topRow.addView(emojiView)
            topRow.addView(valueView)

            // Title
            val titleView = TextView(this@OverviewActivity).apply {
                text = title
                textSize = 12f
                setTextColor(ContextCompat.getColor(this@OverviewActivity, R.color.text_secondary))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            addView(topRow)
            addView(titleView)
        }
    }

    private fun createHabitSummary(habits: List<com.example.aurawellnesstracker.model.Habit>): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, dpToPx(16), 0, 0)
            }

            // Title
            val title = TextView(this@OverviewActivity).apply {
                text = "Your Habits"
                textSize = 16f
                setTextColor(ContextCompat.getColor(this@OverviewActivity, R.color.text_primary))
                setTypeface(typeface, Typeface.BOLD)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, dpToPx(12))
                }
            }
            addView(title)

            // Habit items
            habits.forEach { habit ->
                addView(createHabitItem(habit))
            }
        }
    }

    private fun createHabitItem(habit: com.example.aurawellnesstracker.model.Habit): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, dpToPx(8))
            }
            setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12))
            setBackgroundResource(R.drawable.white_rounded_background)
            gravity = Gravity.CENTER_VERTICAL

            // Status indicator
            val statusIndicator = TextView(this@OverviewActivity).apply {
                text = if (habit.isCompleted) "✅" else "⏳"
                textSize = 16f
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, dpToPx(12), 0)
                }
            }

            // Habit info
            val habitInfo = LinearLayout(this@OverviewActivity).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            }

            val habitName = TextView(this@OverviewActivity).apply {
                text = habit.title
                textSize = 14f
                setTextColor(ContextCompat.getColor(this@OverviewActivity, R.color.text_primary))
                setTypeface(typeface, Typeface.BOLD)
            }

            val habitStreak = TextView(this@OverviewActivity).apply {
                text = "${habit.completedDays} day streak"
                textSize = 12f
                setTextColor(ContextCompat.getColor(this@OverviewActivity, R.color.text_secondary))
            }

            habitInfo.addView(habitName)
            habitInfo.addView(habitStreak)

            addView(statusIndicator)
            addView(habitInfo)
        }
    }

    private fun getLast7DaysMoodData(): List<Float> {
        val moodData = mutableListOf<Float>()
        val calendar = Calendar.getInstance()

        // Get data for last 7 days
        for (i in 6 downTo 0) {
            val currentCalendar = Calendar.getInstance()
            currentCalendar.add(Calendar.DAY_OF_MONTH, -i)

            val moodEntries = moodManager.getMoodEntriesByDate(currentCalendar.time)
            val averageMood = if (moodEntries.isNotEmpty()) {
                moodEntries.map { moodEntry ->
                    when (moodEntry.moodType.toLowerCase(Locale.ROOT)) {
                        "excellent", "amazing" -> 5f
                        "good", "happy" -> 4f
                        "okay", "neutral" -> 3f
                        "bad", "sad" -> 2f
                        "terrible", "awful" -> 1f
                        else -> 3f
                    }
                }.average().toFloat()
            } else {
                0f
            }

            moodData.add(averageMood)
        }

        return moodData
    }

    private fun getLast7DaysHydrationData(): List<Float> {
        val weeklyData = hydrationManager.getWeeklyWaterData()
        return weeklyData.map { data ->
            (data.totalWater / 1000.0f)
        }
    }

    private fun getMaxHydrationValue(): Float {
        val goal = hydrationManager.getDailyGoal()
        return (goal / 1000.0f) * 1.2f
    }

    private fun createDayChartLayout(day: String, value: Float, maxValue: Float, unit: String, color: Int): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                setMargins(8, 0, 8, 0)
            }
            gravity = Gravity.CENTER_HORIZONTAL

            // Day label
            val dayLabel = TextView(this@OverviewActivity).apply {
                text = day
                textSize = 12f
                setTextColor(ContextCompat.getColor(this@OverviewActivity, R.color.text_secondary))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 8)
                }
            }

            // Progress bar container
            val progressContainer = LinearLayout(this@OverviewActivity).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    dpToPx(24),
                    dpToPx(120)
                )
                gravity = Gravity.BOTTOM
                setBackgroundResource(R.drawable.white_rounded_background)
            }

            // Progress fill
            val progressFill = View(this@OverviewActivity).apply {
                val barHeight = if (maxValue > 0) {
                    (value / maxValue * dpToPx(120)).toInt()
                } else {
                    0
                }
                val minHeight = dpToPx(4)
                val actualHeight = maxOf(barHeight, minHeight)

                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    actualHeight
                ).apply {
                    setMargins(dpToPx(2), 0, dpToPx(2), 0)
                }
                setBackgroundColor(color)
            }

            // Value text
            val valueText = TextView(this@OverviewActivity).apply {
                val displayValue = if (unit == "%") value.toInt() else value
                text = if (unit == "%") "$displayValue$unit" else String.format("%.1f$unit", value)
                textSize = 10f
                setTextColor(ContextCompat.getColor(this@OverviewActivity, R.color.text_primary))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 8, 0, 0)
                }
            }

            progressContainer.addView(progressFill)
            addView(dayLabel)
            addView(progressContainer)
            addView(valueText)
        }
    }

    private fun getLast7DaysLabels(): Array<String> {
        val calendar = Calendar.getInstance()
        val days = mutableListOf<String>()
        val dayNames = arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

        for (i in 6 downTo 0) {
            calendar.add(Calendar.DAY_OF_MONTH, -i)
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1
            days.add(dayNames[dayOfWeek])
            calendar.add(Calendar.DAY_OF_MONTH, i)
        }

        return days.toTypedArray()
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}