package com.example.aurawellnesstracker.utils

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class HydrationReminderWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        val hydrationManager = HydrationManager(applicationContext)

        // Only show notification if reminders are enabled
        if (hydrationManager.isReminderEnabled()) {
            NotificationHelper(applicationContext).showHydrationReminder()
        }

        return Result.success()
    }
}