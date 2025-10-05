package com.example.aurawellnesstracker.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.aurawellnesstracker.model.WaterEntry
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class HydrationManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("HydrationPrefs", Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = sharedPreferences.edit()
    private val gson = Gson()
    private val reminderScheduler = ReminderScheduler(context)

    companion object {
        private const val KEY_WATER_ENTRIES = "water_entries"
        private const val KEY_DAILY_GOAL = "daily_water_goal"
        private const val KEY_REMINDER_ENABLED = "water_reminder_enabled"
        private const val KEY_REMINDER_INTERVAL = "water_reminder_interval"
        private const val DEFAULT_GOAL = 2000 // 2L in ml
    }

    init {
        // Schedule reminders if they're enabled - use exact timing
        if (isReminderEnabled()) {
            val interval = getReminderInterval()
            Log.d("HydrationManager", "Initializing with reminders enabled, interval: $interval minutes")
            reminderScheduler.updateReminder(true, interval)
        } else {
            Log.d("HydrationManager", "Initializing with reminders disabled")
            reminderScheduler.cancelReminder()
        }
    }

    // Add water entry
    fun addWaterEntry(entry: WaterEntry): Boolean {
        try {
            val allEntries = getAllWaterEntries().toMutableList()
            allEntries.add(entry)
            val entriesJson = gson.toJson(allEntries)
            editor.putString(KEY_WATER_ENTRIES, entriesJson)
            return editor.commit()
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    // Get all water entries
    fun getAllWaterEntries(): List<WaterEntry> {
        return try {
            val entriesJson = sharedPreferences.getString(KEY_WATER_ENTRIES, null)
            if (entriesJson != null) {
                val type = object : TypeToken<List<WaterEntry>>() {}.type
                gson.fromJson<List<WaterEntry>>(entriesJson, type) ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Get today's water entries
    fun getTodayWaterEntries(): List<WaterEntry> {
        val today = System.currentTimeMillis()
        val calendar = java.util.Calendar.getInstance().apply {
            timeInMillis = today
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        val startOfDay = calendar.timeInMillis

        calendar.add(java.util.Calendar.DAY_OF_MONTH, 1)
        val endOfDay = calendar.timeInMillis - 1

        return getAllWaterEntries().filter {
            it.timestamp in startOfDay..endOfDay
        }
    }

    // Get today's total water intake
    fun getTodayTotalWater(): Int {
        return getTodayWaterEntries().sumOf { it.amount }
    }

    // Delete water entry
    fun deleteWaterEntry(entryId: String): Boolean {
        try {
            val allEntries = getAllWaterEntries().toMutableList()
            allEntries.removeAll { it.id == entryId }
            val entriesJson = gson.toJson(allEntries)
            editor.putString(KEY_WATER_ENTRIES, entriesJson)
            return editor.commit()
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    // Set daily water goal
    fun setDailyGoal(goal: Int): Boolean {
        return editor.putInt(KEY_DAILY_GOAL, goal).commit()
    }

    // Get daily water goal
    fun getDailyGoal(): Int {
        return sharedPreferences.getInt(KEY_DAILY_GOAL, DEFAULT_GOAL)
    }

    // Set reminder enabled
    fun setReminderEnabled(enabled: Boolean): Boolean {
        val success = editor.putBoolean(KEY_REMINDER_ENABLED, enabled).commit()
        if (success) {
            val interval = getReminderInterval()
            Log.d("HydrationManager", "Setting reminder enabled: $enabled, interval: $interval minutes")
            reminderScheduler.updateReminder(enabled, interval)
        }
        return success
    }

    // Get reminder enabled
    fun isReminderEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_REMINDER_ENABLED, true)
    }

    // Set reminder interval
    fun setReminderInterval(interval: Int): Boolean {
        val success = editor.putInt(KEY_REMINDER_INTERVAL, interval).commit()
        if (success) {
            // Always update the scheduler when interval changes
            val isEnabled = isReminderEnabled()
            Log.d("HydrationManager", "Setting reminder interval: $interval minutes, enabled: $isEnabled")
            reminderScheduler.updateReminder(isEnabled, interval)
        }
        return success
    }

    // Get reminder interval
    fun getReminderInterval(): Int {
        return sharedPreferences.getInt(KEY_REMINDER_INTERVAL, 10)
    }

    // Get completion percentage
    fun getCompletionPercentage(): Int {
        val todayTotal = getTodayTotalWater()
        val goal = getDailyGoal()
        return if (goal > 0) {
            (todayTotal * 100) / goal
        } else {
            0
        }
    }
}