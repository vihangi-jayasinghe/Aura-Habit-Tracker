package com.example.aurawellnesstracker.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.aurawellnesstracker.model.MoodEntry
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

class MoodManager(private val context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("MoodPreferences", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val moodEntriesKey = "mood_entries"

    // Create
    fun addMoodEntry(moodEntry: MoodEntry) {
        val entries = getAllMoodEntries().toMutableList()
        entries.add(moodEntry)
        saveMoodEntries(entries)
    }

    // Read
    fun getAllMoodEntries(): List<MoodEntry> {
        val json = sharedPreferences.getString(moodEntriesKey, null)
        return if (json != null) {
            val type = object : TypeToken<List<MoodEntry>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } else {
            emptyList()
        }
    }

    fun getMoodEntryById(id: String): MoodEntry? {
        return getAllMoodEntries().find { it.id == id }
    }

    fun getMoodEntriesByDate(date: Date): List<MoodEntry> {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val startOfDay = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val endOfDay = calendar.timeInMillis

        return getAllMoodEntries().filter {
            it.timestamp in startOfDay until endOfDay
        }.sortedByDescending { it.timestamp }
    }

    fun getTodayMoodEntries(): List<MoodEntry> {
        return getMoodEntriesByDate(Date())
    }

    // Update
    fun updateMoodEntry(updatedEntry: MoodEntry): Boolean {
        val entries = getAllMoodEntries().toMutableList()
        val index = entries.indexOfFirst { it.id == updatedEntry.id }
        if (index != -1) {
            entries[index] = updatedEntry
            saveMoodEntries(entries)
            return true
        }
        return false
    }

    // Delete
    fun deleteMoodEntry(id: String): Boolean {
        val entries = getAllMoodEntries().toMutableList()
        val index = entries.indexOfFirst { it.id == id }
        if (index != -1) {
            entries.removeAt(index)
            saveMoodEntries(entries)
            return true
        }
        return false
    }

    fun deleteAllMoodEntries() {
        sharedPreferences.edit().remove(moodEntriesKey).apply()
    }

    private fun saveMoodEntries(entries: List<MoodEntry>) {
        val json = gson.toJson(entries)
        sharedPreferences.edit().putString(moodEntriesKey, json).apply()
    }
}