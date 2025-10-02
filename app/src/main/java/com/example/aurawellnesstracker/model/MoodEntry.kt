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

        fun getColorForMood(moodType: String): String {
            return when (moodType) {
                MOOD_EXCELLENT -> "#118AB2"
                MOOD_GOOD -> "#06D6A0"
                MOOD_NEUTRAL -> "#FFD166"
                MOOD_LOW -> "#FFA726"
                MOOD_ANXIOUS -> "#F44336"
                MOOD_TIRED -> "#9C27B0"
                MOOD_FRUSTRATED -> "#FF9800"
                MOOD_LOVED -> "#4CAF50"
                else -> "#666666"
            }
        }
    }
}