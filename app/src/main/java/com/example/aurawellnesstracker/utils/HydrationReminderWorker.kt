package com.example.aurawellnesstracker.utils

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.text.SimpleDateFormat
import java.util.*

class HydrationReminderWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        val currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        Log.d("HydrationReminder", "=== WORKER EXECUTED at: $currentTime ===")
        Log.d("HydrationReminder", "Worker ID: ${this.id}")

        val hydrationManager = HydrationManager(applicationContext)
        val isEnabled = hydrationManager.isReminderEnabled()
        val interval = hydrationManager.getReminderInterval()

        Log.d("HydrationReminder", "Settings - Enabled: $isEnabled, Interval: $interval minutes")
        Log.d("HydrationReminder", "Today's total water: ${hydrationManager.getTodayTotalWater()}ml")

        if (isEnabled) {
            Log.d("HydrationReminder", "Showing notification...")
            NotificationHelper(applicationContext).showHydrationReminder()
            Log.d("HydrationReminder", "Notification shown successfully")
        } else {
            Log.d("HydrationReminder", "Reminders disabled, skipping notification")
            ReminderScheduler(applicationContext).cancelReminder()
        }

        Log.d("HydrationReminder", "=== WORKER COMPLETED ===")
        return Result.success()
    }
}