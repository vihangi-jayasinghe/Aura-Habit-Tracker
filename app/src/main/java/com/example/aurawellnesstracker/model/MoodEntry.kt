package com.example.aurawellnesstracker.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class MoodEntry(
    val id: String = UUID.randomUUID().toString(),
    val moodType: String,
    val emoji: String,
    val notes: String = "",
    val date: Date = Date(),
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable {

    companion object {
        const val MOOD_EXCELLENT = "Excellent"
        const val MOOD_GOOD = "Good"
        const val MOOD_NEUTRAL = "Neutral"
        const val MOOD_LOW = "Low"
        const val MOOD_ANXIOUS = "Anxious"
        const val MOOD_TIRED = "Tired"
        const val MOOD_FRUSTRATED = "Frustrated"
        const val MOOD_LOVED = "Loved"

        fun getEmojiForMood(moodType: String): String {
            return when (moodType) {
                MOOD_EXCELLENT -> "😄"
                MOOD_GOOD -> "😊"
                MOOD_NEUTRAL -> "😐"
                MOOD_LOW -> "😕"
                MOOD_ANXIOUS -> "😰"
                MOOD_TIRED -> "😴"
                MOOD_FRUSTRATED -> "😠"
                MOOD_LOVED -> "🥰"
                else -> "😐"
            }
        }

        // This method is no longer used but kept for compatibility
        fun getColorForMood(moodType: String): String {
            return "#6750A4" // Return primary color for all moods
        }
    }
}