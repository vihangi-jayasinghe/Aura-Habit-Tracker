package com.example.aurawellnesstracker.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class HydrationReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("HydrationReminder", "🔔 Broadcast received at: ${System.currentTimeMillis()}")

        when (intent.action) {
            ReminderScheduler.REMINDER_ACTION -> {
                val intervalMinutes = intent.getIntExtra(ReminderScheduler.EXTRA_INTERVAL, 15)

                val hydrationManager = HydrationManager(context)
                if (hydrationManager.isReminderEnabled()) {
                    Log.d("HydrationReminder", "Showing notification for interval: $intervalMinutes minutes")
                    NotificationHelper(context).showHydrationReminder()

                    if (hydrationManager.isReminderEnabled()) {
                        ReminderScheduler(context).scheduleReminder(intervalMinutes)
                    }
                } else {
                    Log.d("HydrationReminder", "Reminders disabled, cancelling")
                    ReminderScheduler(context).cancelReminder()
                }
            }
        }
    }
}