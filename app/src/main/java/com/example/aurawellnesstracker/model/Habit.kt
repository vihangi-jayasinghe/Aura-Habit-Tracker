package com.example.aurawellnesstracker.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date
import java.util.UUID

@Parcelize
data class Habit(
    val id: String = UUID.randomUUID().toString(),
    var title: String,
    var description: String = "",
    var category: HabitCategory = HabitCategory.HEALTH,
    var isCompleted: Boolean = false,
    var createdAt: Date = Date(),
    var targetDays: Int = 7,
    var completedDays: Int = 0,
    var reminderTime: String? = null
) : Parcelable

enum class HabitCategory {
    HEALTH, FITNESS, PRODUCTIVITY, MINDFULNESS, LEARNING, ALL
}