package com.example.aurawellnesstracker.utils

import android.content.Context
import com.example.aurawellnesstracker.model.Habit
import com.example.aurawellnesstracker.model.HabitCategory
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date

object HabitManager {
    private const val PREFS_NAME = "habit_preferences"
    private const val HABITS_KEY = "habits_list"
    private val habits = mutableListOf<Habit>()
    private var isInitialized = false

    // Initialize with context
    fun initialize(context: Context) {
        if (!isInitialized) {
            loadHabits(context)
            isInitialized = true
        }
    }

    // Add a new habit
    fun addHabit(context: Context, habit: Habit): Boolean {
        return try {
            habits.add(habit)
            saveHabits(context)
            true
        } catch (e: Exception) {
            false
        }
    }

    // Update an existing habit
    fun updateHabit(context: Context, habitId: String, updatedHabit: Habit): Boolean {
        val index = habits.indexOfFirst { it.id == habitId }
        return if (index != -1) {
            habits[index] = updatedHabit.copy(id = habitId) // Preserve original ID
            saveHabits(context)
            true
        } else {
            false
        }
    }

    // Delete a habit
    fun deleteHabit(context: Context, habitId: String): Boolean {
        val success = habits.removeIf { it.id == habitId }
        if (success) {
            saveHabits(context)
        }
        return success
    }

    // Get all habits
    fun getAllHabits(): List<Habit> {
        return habits.toList()
    }

    // Get habits by category
    fun getHabitsByCategory(category: HabitCategory): List<Habit> {
        return if (category == HabitCategory.ALL) {
            getAllHabits()
        } else {
            habits.filter { it.category == category }
        }
    }

    // Get habit by ID
    fun getHabitById(habitId: String): Habit? {
        return habits.find { it.id == habitId }
    }

    // Toggle habit completion status
    fun toggleHabitCompletion(context: Context, habitId: String): Boolean {
        val habit = getHabitById(habitId)
        return habit?.let {
            it.isCompleted = !it.isCompleted
            if (it.isCompleted) {
                it.completedDays++
            } else if (it.completedDays > 0) {
                it.completedDays--
            }
            saveHabits(context)
            true
        } ?: false
    }

    // Complete all habits for today
    fun completeAllHabits(context: Context): Boolean {
        return try {
            habits.forEach { habit ->
                if (!habit.isCompleted) {
                    habit.isCompleted = true
                    habit.completedDays++
                }
            }
            saveHabits(context)
            true
        } catch (e: Exception) {
            false
        }
    }

    // Reset all habits completion status for new day
    fun resetDailyHabits(context: Context) {
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(
            Date()
        )
        val lastResetDate = getLastResetDate(context)

        if (lastResetDate != today) {
            habits.forEach { habit ->
                habit.isCompleted = false
            }
            saveHabits(context)
            setLastResetDate(context, today)
        }
    }

    // Get today's progress stats
    fun getTodayProgress(): Triple<Int, Int, Int> {
        val completed = habits.count { it.isCompleted }
        val total = habits.size
        val completionRate = if (total > 0) (completed * 100 / total) else 0

        return Triple(completed, total, completionRate)
    }

    // Get habits completion streak (simplified)
    fun getHabitStreak(habitId: String): Int {
        return getHabitById(habitId)?.completedDays ?: 0
    }

    // Search habits by title
    fun searchHabits(query: String): List<Habit> {
        return habits.filter {
            it.title.contains(query, ignoreCase = true) ||
                    it.description.contains(query, ignoreCase = true)
        }
    }

    // Clear all habits (for testing/reset)
    fun clearAllHabits(context: Context) {
        habits.clear()
        saveHabits(context)
    }

    // Private methods for persistence
    private fun saveHabits(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        val gson = Gson()
        val json = gson.toJson(habits)
        editor.putString(HABITS_KEY, json)
        editor.apply()
    }

    private fun loadHabits(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val gson = Gson()
        val json = prefs.getString(HABITS_KEY, null)
        val type = object : TypeToken<List<Habit>>() {}.type

        habits.clear()
        if (json != null) {
            val loadedHabits: List<Habit> = gson.fromJson(json, type)
            habits.addAll(loadedHabits)
        }
    }

    private fun getLastResetDate(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString("last_reset_date", "") ?: ""
    }

    private fun setLastResetDate(context: Context, date: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString("last_reset_date", date)
        editor.apply()
    }
}