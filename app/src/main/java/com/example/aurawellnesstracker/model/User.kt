package com.example.aurawellnesstracker.model

data class User(
    val name: String,
    val email: String,
    val profileImage: String? = null,
    val joinDate: Long = System.currentTimeMillis()
)