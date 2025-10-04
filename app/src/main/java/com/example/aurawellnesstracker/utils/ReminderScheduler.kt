package com.example.aurawellnesstracker.utils

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class ReminderScheduler(private val context: Context) {

    companion object {
        const val REMINDER_WORK_NAME = "hydration_reminder_work"
    }

    private val workManager = WorkManager.getInstance(context)

    fun scheduleReminder(intervalMinutes: Int) {
        // Cancel existing reminders first
        cancelReminder()

        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()

        val reminderWork: PeriodicWorkRequest =
            PeriodicWorkRequestBuilder<HydrationReminderWorker>(
                intervalMinutes.toLong(),
                TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .build()

        workManager.enqueueUniquePeriodicWork(
            REMINDER_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            reminderWork
        )
    }

    fun cancelReminder() {
        workManager.cancelUniqueWork(REMINDER_WORK_NAME)
    }

    fun updateReminder(enabled: Boolean, intervalMinutes: Int) {
        if (enabled) {
            scheduleReminder(intervalMinutes)
        } else {
            cancelReminder()
        }
    }
}