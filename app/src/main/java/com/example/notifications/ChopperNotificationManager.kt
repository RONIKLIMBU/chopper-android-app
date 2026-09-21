package com.example.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.ReminderEntity
import java.util.Calendar

class ChopperNotificationManager(private val context: Context) {

    companion object {
        const val CHANNEL_REMINDERS = "chopper_reminders_channel"
        const val CHANNEL_CHECKINS = "chopper_checkins_channel"

        const val ACTION_REMINDER_ALERT = "com.example.ACTION_REMINDER_ALERT"
        const val ACTION_DAILY_CHECKIN = "com.example.ACTION_DAILY_CHECKIN"

        const val EXTRA_REMINDER_ID = "extra_reminder_id"
        const val EXTRA_REMINDER_TITLE = "extra_reminder_title"
        const val EXTRA_STAGE = "extra_stage"
        const val EXTRA_NOTES = "extra_notes"
        const val EXTRA_ROUTINE_TYPE = "extra_routine_type"

        const val REQUEST_CODE_MORNING_CHECKIN = 7001
        const val REQUEST_CODE_NIGHT_CHECKIN = 7002
        const val REQUEST_CODE_LUNCH_CHECKIN = 7003
        const val REQUEST_CODE_WATER_CHECKIN_1 = 7004
        const val REQUEST_CODE_WATER_CHECKIN_2 = 7005
        const val REQUEST_CODE_CARE_CHECKIN = 7006
    }

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Reminders Channel
            val remindersChannel = NotificationChannel(
                CHANNEL_REMINDERS,
                "Chopper Reminders & Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Timely stage 1 (1 day before) and stage 2 (day of event) schedule alerts from Chopper"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 200, 300)
            }

            // Daily Check-ins Channel
            val checkinsChannel = NotificationChannel(
                CHANNEL_CHECKINS,
                "Chopper Daily Care & Check-ins",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Morning 7:00 AM check-ins, hydration reminders, and bedtime health checks"
                enableVibration(true)
            }

            notificationManager.createNotificationChannel(remindersChannel)
            notificationManager.createNotificationChannel(checkinsChannel)
        }
    }

    /**
     * Schedules 2-stage alerts for a reminder:
     * Stage 1: 24 hours before the event.
     * Stage 2: At the exact time of the event.
     */
    fun scheduleReminderAlerts(reminder: ReminderEntity) {
        val now = System.currentTimeMillis()

        // Stage 1: 1 day (24 hours) before
        if (reminder.remindDayBefore) {
            val stage1Time = reminder.targetTimestamp - (24 * 60 * 60 * 1000L)
            if (stage1Time > now) {
                scheduleAlarm(
                    triggerAtMillis = stage1Time,
                    requestCode = (reminder.id * 10 + 1).toInt(),
                    intent = Intent(context, ChopperAlarmReceiver::class.java).apply {
                        action = ACTION_REMINDER_ALERT
                        putExtra(EXTRA_REMINDER_ID, reminder.id)
                        putExtra(EXTRA_REMINDER_TITLE, reminder.title)
                        putExtra(EXTRA_STAGE, "STAGE_1")
                        putExtra(EXTRA_NOTES, reminder.notes)
                    }
                )
            }
        }

        // Stage 2: Day of the event
        if (reminder.remindDayOf) {
            val stage2Time = reminder.targetTimestamp
            if (stage2Time > now) {
                scheduleAlarm(
                    triggerAtMillis = stage2Time,
                    requestCode = (reminder.id * 10 + 2).toInt(),
                    intent = Intent(context, ChopperAlarmReceiver::class.java).apply {
                        action = ACTION_REMINDER_ALERT
                        putExtra(EXTRA_REMINDER_ID, reminder.id)
                        putExtra(EXTRA_REMINDER_TITLE, reminder.title)
                        putExtra(EXTRA_STAGE, "STAGE_2")
                        putExtra(EXTRA_NOTES, reminder.notes)
                    }
                )
            }
        }
    }

    fun cancelReminderAlerts(reminderId: Long) {
        // Cancel Stage 1
        val stage1Intent = Intent(context, ChopperAlarmReceiver::class.java).apply {
            action = ACTION_REMINDER_ALERT
        }
        val stage1Pending = PendingIntent.getBroadcast(
            context,
            (reminderId * 10 + 1).toInt(),
            stage1Intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        stage1Pending?.let {
            alarmManager.cancel(it)
            it.cancel()
        }

        // Cancel Stage 2
        val stage2Intent = Intent(context, ChopperAlarmReceiver::class.java).apply {
            action = ACTION_REMINDER_ALERT
        }
        val stage2Pending = PendingIntent.getBroadcast(
            context,
            (reminderId * 10 + 2).toInt(),
            stage2Intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        stage2Pending?.let {
            alarmManager.cancel(it)
            it.cancel()
        }
    }

    /**
     * Schedules recurring permanent check-ins:
     * - Morning Check-in at 7:00 AM
     * - Lunch Reminder at 2:00 PM (14:00)
     * - Night Rest Check-in at 11:00 PM (23:00)
     * - Built-in Drinking Water Reminders (10:30 AM & 3:30 PM)
     * - Built-in Take Care Wellness Notification (4:45 PM)
     */
    fun scheduleDailyCheckIns() {
        val now = System.currentTimeMillis()

        // 1. Morning 7:00 AM
        scheduleRecurringDailyAlarm(7, 0, REQUEST_CODE_MORNING_CHECKIN, "MORNING", now)

        // 2. Lunch 2:00 PM (14:00)
        scheduleRecurringDailyAlarm(14, 0, REQUEST_CODE_LUNCH_CHECKIN, "LUNCH", now)

        // 3. Night 11:00 PM (23:00)
        scheduleRecurringDailyAlarm(23, 0, REQUEST_CODE_NIGHT_CHECKIN, "NIGHT", now)

        // 4. Built-in Drinking Water Reminders (10:30 AM and 3:30 PM)
        scheduleRecurringDailyAlarm(10, 30, REQUEST_CODE_WATER_CHECKIN_1, "WATER", now)
        scheduleRecurringDailyAlarm(15, 30, REQUEST_CODE_WATER_CHECKIN_2, "WATER", now)

        // 5. Built-in Take Care Notification (4:45 PM)
        scheduleRecurringDailyAlarm(16, 45, REQUEST_CODE_CARE_CHECKIN, "CARE", now)
    }

    private fun scheduleRecurringDailyAlarm(hour: Int, minute: Int, requestCode: Int, routineType: String, now: Long) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= now) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        scheduleAlarm(
            triggerAtMillis = cal.timeInMillis,
            requestCode = requestCode,
            intent = Intent(context, ChopperAlarmReceiver::class.java).apply {
                action = ACTION_DAILY_CHECKIN
                putExtra(EXTRA_ROUTINE_TYPE, routineType)
            }
        )
    }

    private fun scheduleAlarm(triggerAtMillis: Long, requestCode: Int, intent: Intent) {
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            // Fallback if SCHEDULE_EXACT_ALARM is not granted
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    /**
     * Posts a notification for reminder alert
     */
    fun showReminderNotification(
        reminderId: Long,
        title: String,
        stage: String,
        notes: String = ""
    ) {
        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("selected_tab", 1) // Reminders tab
        }
        val tapPendingIntent = PendingIntent.getActivity(
            context,
            (reminderId + 500).toInt(),
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val headerText = if (stage == "STAGE_1") {
            "🌸 Chopper Reminder (Tomorrow!)"
        } else {
            "🚨 Chopper Alert: Event Today!"
        }

        val bodyText = if (stage == "STAGE_1") {
            "Boss! Tomorrow is '$title'! Don't forget to prepare! ${if (notes.isNotBlank()) "Notes: $notes" else ""}"
        } else {
            "Boss! It's time for '$title'! Chopper is cheering you on! ${if (notes.isNotBlank()) "Notes: $notes" else ""}"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_REMINDERS)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(headerText)
            .setContentText(bodyText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bodyText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(tapPendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(reminderId.toInt(), notification)
        } catch (e: SecurityException) {
            // Permission not granted
        }
    }

    /**
     * Posts a daily routine check-in notification
     */
    fun showDailyCheckInNotification(routineType: String) {
        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("selected_tab", 0) // Chat & Care tab
        }
        val tapPendingIntent = PendingIntent.getActivity(
            context,
            8888,
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val (notifId, title, message, iconRes) = when (routineType) {
            "MORNING" -> Quadruple(
                7001,
                "☀️ 7:00 AM Morning Greetings with Chopper",
                "Good morning, Boss! 🌸 It's 7:00 AM! Chopper is awake! Drink a glass of fresh water and let's review today's schedule!",
                android.R.drawable.ic_dialog_info
            )
            "LUNCH" -> Quadruple(
                7003,
                "🍱 2:00 PM Lunch Time with Chopper",
                "Boss, it's 2:00 PM! Time to step away for a healthy lunch! Even the greatest captain needs nutrition! Doctor Chopper orders you to eat well! 🍱🌸",
                android.R.drawable.ic_dialog_info
            )
            "NIGHT" -> Quadruple(
                7002,
                "🌙 11:00 PM Goodnight Greetings & Sleep",
                "Boss, it's 11:00 PM! You've worked so hard today! Time to wrap up and sleep. Chopper prescribes 8 hours of peaceful rest! 💤✨",
                android.R.drawable.ic_lock_idle_alarm
            )
            "WATER" -> Quadruple(
                7004,
                "💧 Doctor Chopper: Time to Drink Water!",
                "Boss! Hydration check! 💧 Grab a glass of water right now to keep your brain and body energized! Doctor's orders! 🦌🌸",
                android.R.drawable.ic_dialog_info
            )
            "CARE" -> Quadruple(
                7006,
                "🩺 Doctor Chopper: Take Care Check-in",
                "Boss, take a 2-minute breather! Stretch your back, roll your shoulders, and breathe deeply. You're doing amazing, Boss! 🌸",
                android.R.drawable.ic_dialog_alert
            )
            else -> Quadruple(
                7000,
                "🌸 Chopper Care Reminder",
                "Boss, Chopper is checking in on you! Let me know if you need anything!",
                android.R.drawable.ic_dialog_info
            )
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_CHECKINS)
            .setSmallIcon(iconRes)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(tapPendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notifId, notification)
        } catch (e: SecurityException) {
            // Ignored if permission not granted yet
        }
    }

    private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

    /**
     * Instant test notification for immediate UI verification
     */
    fun showInstantNotification(title: String, message: String, isCare: Boolean = false) {
        val channelId = if (isCare) CHANNEL_CHECKINS else CHANNEL_REMINDERS
        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val tapPendingIntent = PendingIntent.getActivity(
            context,
            9999,
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(tapPendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(System.currentTimeMillis().toInt(), notification)
        } catch (e: SecurityException) {
            // Ignored
        }
    }
}
