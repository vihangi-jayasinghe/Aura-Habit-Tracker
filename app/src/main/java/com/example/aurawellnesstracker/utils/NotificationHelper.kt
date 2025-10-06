package com.example.aurawellnesstracker.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.aurawellnesstracker.R

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "hydration_reminder_channel"
        const val NOTIFICATION_ID = 1001
        const val POST_NOTIFICATIONS_PERMISSION = android.Manifest.permission.POST_NOTIFICATIONS
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Hydration Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders to drink water regularly"
                enableVibration(true)
                setShowBadge(true)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showHydrationReminder() {
        // Check if we have notification permission
        if (!hasNotificationPermission()) {
            // Permission not granted, can't show notification
            return
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_water)
            .setContentTitle("Stay Hydrated! 💧")
            .setContentText("Time to drink some water and stay healthy!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun hasNotificationPermission(): Boolean {
        // For Android 12 and below, no runtime permission needed for notifications
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return true
        }

        // For Android 13+, check if we have the POST_NOTIFICATIONS permission
        return ContextCompat.checkSelfPermission(
            context,
            POST_NOTIFICATIONS_PERMISSION
        ) == PackageManager.PERMISSION_GRANTED
    }
}