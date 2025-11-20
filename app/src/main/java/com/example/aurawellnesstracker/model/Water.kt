package com.example.aurawellnesstracker.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class WaterEntry(
    val id: String = "",
    val amount: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = ""
) : Parcelable