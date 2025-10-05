package com.example.aurawellnesstracker.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import android.util.Log
import androidx.work.WorkManager

class ReminderScheduler(private val context: Context) {

    companion object {
        const val REMINDER_WORK_NAME = "hydration_reminder_work"
        const val REMINDER_ACTION = "HYDRATION_REMINDER_ACTION"
        const val EXTRA_INTERVAL = "reminder_interval"
        private const val REQUEST_CODE = 1001
    }

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleReminder(intervalMinutes: Int) {
        Log.d("ReminderScheduler", "🔄 SCHEDULING reminder with interval: $intervalMinutes minutes")

        // Cancel existing reminders first
        cancelReminder()

        val intervalMillis = intervalMinutes * 60 * 1000L

        // Create pending intent for the reminder
        val intent = Intent(context, HydrationReminderReceiver::class.java).apply {
            action = REMINDER_ACTION
            putExtra(EXTRA_INTERVAL, intervalMinutes)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Use set() instead of setExact for better compatibility
        // This will trigger after the specified interval from now
        val triggerTime = SystemClock.elapsedRealtime() + intervalMillis

        alarmManager.set(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            triggerTime,
            pendingIntent
        )

        Log.d("ReminderScheduler", "✅ Reminder scheduled for $intervalMinutes minutes from now")
    }

    fun cancelReminder() {
        Log.d("ReminderScheduler", "❌ Cancelling existing reminders")

        val intent = Intent(context, HydrationReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()

        // Also cancel any WorkManager work
        WorkManager.getInstance(context).cancelUniqueWork(REMINDER_WORK_NAME)
    }

    fun updateReminder(enabled: Boolean, intervalMinutes: Int) {
        Log.d("ReminderScheduler", "⚙️ Updating reminder - Enabled: $enabled, Interval: $intervalMinutes minutes")
        if (enabled) {
            scheduleReminder(intervalMinutes)
        } else {
            cancelReminder()
        }
    }
}