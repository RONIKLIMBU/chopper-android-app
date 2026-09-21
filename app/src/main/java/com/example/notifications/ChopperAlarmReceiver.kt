package com.example.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ChopperAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager = ChopperNotificationManager(context)

        when (intent.action) {
            ChopperNotificationManager.ACTION_REMINDER_ALERT -> {
                val reminderId = intent.getLongExtra(ChopperNotificationManager.EXTRA_REMINDER_ID, 0L)
                val title = intent.getStringExtra(ChopperNotificationManager.EXTRA_REMINDER_TITLE) ?: "Reminder"
                val stage = intent.getStringExtra(ChopperNotificationManager.EXTRA_STAGE) ?: "STAGE_2"
                val notes = intent.getStringExtra(ChopperNotificationManager.EXTRA_NOTES) ?: ""

                notificationManager.showReminderNotification(
                    reminderId = reminderId,
                    title = title,
                    stage = stage,
                    notes = notes
                )
            }
            ChopperNotificationManager.ACTION_DAILY_CHECKIN -> {
                val routineType = intent.getStringExtra(ChopperNotificationManager.EXTRA_ROUTINE_TYPE) ?: "MORNING"
                notificationManager.showDailyCheckInNotification(routineType)
                // Reschedule for next day
                notificationManager.scheduleDailyCheckIns()
            }
            Intent.ACTION_BOOT_COMPLETED -> {
                // Re-arm daily check-ins after device reboot
                notificationManager.scheduleDailyCheckIns()
            }
        }
    }
}
