package com.example.data.repository

import com.example.data.dao.ChopperDao
import com.example.data.model.ChatMessageEntity
import com.example.data.model.ReminderEntity
import com.example.data.model.TriageNotificationEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.util.Calendar

class ChopperRepository(private val dao: ChopperDao) {

    val reminders: Flow<List<ReminderEntity>> = dao.getAllReminders()
    val notifications: Flow<List<TriageNotificationEntity>> = dao.getAllNotifications()
    val chatMessages: Flow<List<ChatMessageEntity>> = dao.getAllChatMessages()

    suspend fun ensurePermanentRemindersExist() {
        val existingReminders = dao.getAllReminders().firstOrNull() ?: emptyList()
        val now = System.currentTimeMillis()

        // 1. 7:00 AM Permanent Good Morning Greetings
        if (existingReminders.none { it.title.contains("7:00 AM", ignoreCase = true) || it.title.contains("Good Morning", ignoreCase = true) }) {
            val morningCal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 7)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (timeInMillis <= now) add(Calendar.DAY_OF_YEAR, 1)
            }
            dao.insertReminder(
                ReminderEntity(
                    title = "☀️ 7:00 AM Good Morning Greetings",
                    targetTimestamp = morningCal.timeInMillis,
                    remindDayBefore = false,
                    remindDayOf = true,
                    isDayBeforeNotified = false,
                    isDayOfNotified = false,
                    status = "ACTIVE",
                    notes = "[PERMANENT DAILY ROUTINE] ☀️ 7:00 AM Morning greetings & daily briefing with Chopper! Drink water and review schedule!"
                )
            )
        }

        // 2. 2:00 PM Permanent Lunch Reminder
        if (existingReminders.none { it.title.contains("Lunch", ignoreCase = true) || it.title.contains("2:00 PM", ignoreCase = true) }) {
            val lunchCal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 14)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (timeInMillis <= now) add(Calendar.DAY_OF_YEAR, 1)
            }
            dao.insertReminder(
                ReminderEntity(
                    title = "🍱 2:00 PM Lunch Reminder",
                    targetTimestamp = lunchCal.timeInMillis,
                    remindDayBefore = false,
                    remindDayOf = true,
                    isDayBeforeNotified = false,
                    isDayOfNotified = false,
                    status = "ACTIVE",
                    notes = "[PERMANENT DAILY ROUTINE] 🍱 2:00 PM Lunch break. Doctor Chopper orders you to step away from work, fuel up, and eat well!"
                )
            )
        }

        // 3. 11:00 PM Permanent Goodnight Greetings
        if (existingReminders.none { it.title.contains("Goodnight", ignoreCase = true) || it.title.contains("11:00 PM", ignoreCase = true) }) {
            val nightCal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (timeInMillis <= now) add(Calendar.DAY_OF_YEAR, 1)
            }
            dao.insertReminder(
                ReminderEntity(
                    title = "🌙 11:00 PM Goodnight Greetings",
                    targetTimestamp = nightCal.timeInMillis,
                    remindDayBefore = false,
                    remindDayOf = true,
                    isDayBeforeNotified = false,
                    isDayOfNotified = false,
                    status = "ACTIVE",
                    notes = "[PERMANENT DAILY ROUTINE] 🌙 11:00 PM Bedtime rest. Doctor Chopper prescribes 8 hours of peaceful sleep for proper recovery!"
                )
            )
        }

        // 4. Built-in Drinking Water Reminder
        if (existingReminders.none { it.title.contains("Water", ignoreCase = true) || it.title.contains("Hydration", ignoreCase = true) }) {
            val waterCal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 10)
                set(Calendar.MINUTE, 30)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (timeInMillis <= now) add(Calendar.DAY_OF_YEAR, 1)
            }
            dao.insertReminder(
                ReminderEntity(
                    title = "💧 Drink Water & Stay Hydrated",
                    targetTimestamp = waterCal.timeInMillis,
                    remindDayBefore = false,
                    remindDayOf = true,
                    isDayBeforeNotified = false,
                    isDayOfNotified = false,
                    status = "ACTIVE",
                    notes = "[BUILT-IN HEALTH CARE] 💧 Doctor Chopper hydration reminder: grab a fresh glass of water to keep your mind and body energized!"
                )
            )
        }

        // 5. Built-in Take Care Notification
        if (existingReminders.none { it.title.contains("Take Care", ignoreCase = true) || it.title.contains("Doctor Chopper Care", ignoreCase = true) }) {
            val careCal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 16)
                set(Calendar.MINUTE, 45)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (timeInMillis <= now) add(Calendar.DAY_OF_YEAR, 1)
            }
            dao.insertReminder(
                ReminderEntity(
                    title = "🩺 Doctor Chopper Take Care Check-in",
                    targetTimestamp = careCal.timeInMillis,
                    remindDayBefore = false,
                    remindDayOf = true,
                    isDayBeforeNotified = false,
                    isDayOfNotified = false,
                    status = "ACTIVE",
                    notes = "[BUILT-IN HEALTH CARE] 🩺 Take a 2-minute break: roll your shoulders, stretch your spine, and breathe deeply. You're doing great, Boss!"
                )
            )
        }
    }

    suspend fun initializeDefaultDataIfEmpty() {
        val existingMessages = dao.getAllChatMessages().firstOrNull()
        if (existingMessages.isNullOrEmpty()) {
            // Initial warm greeting from Chopper
            dao.insertChatMessage(
                ChatMessageEntity(
                    sender = "CHOPPER",
                    text = "Hello, Boss! Chopper is here and ready to help! 🌸\n\nI'll look after your schedule, remind you of important events a day before AND on the day, and help you triage incoming calls and messages. Whenever you need me, just say 'Chopper' or ask away, Boss!",
                    timestamp = System.currentTimeMillis()
                )
            )
        }

        val existingReminders = dao.getAllReminders().firstOrNull()
        if (existingReminders.isNullOrEmpty()) {
            val now = System.currentTimeMillis()
            val oneDayMs = 24 * 60 * 60 * 1000L
            // Tomorrow's event (triggers 1-day-before alert!)
            dao.insertReminder(
                ReminderEntity(
                    title = "Quarterly Strategy Review with Team",
                    targetTimestamp = now + oneDayMs + (2 * 3600 * 1000L),
                    remindDayBefore = true,
                    remindDayOf = true,
                    isDayBeforeNotified = true,
                    isDayOfNotified = false,
                    status = "ACTIVE",
                    notes = "Prepare slide deck and review metrics."
                )
            )
            // Today's event (triggers Day-of alert!)
            dao.insertReminder(
                ReminderEntity(
                    title = "Health Check & Vitamin Restock",
                    targetTimestamp = now + (3 * 3600 * 1000L),
                    remindDayBefore = true,
                    remindDayOf = true,
                    isDayBeforeNotified = true,
                    isDayOfNotified = true,
                    status = "ACTIVE",
                    notes = "Doctor Chopper's special prescription: stay hydrated and rest!"
                )
            )
        }

        val existingNotifications = dao.getAllNotifications().firstOrNull()
        if (existingNotifications.isNullOrEmpty()) {
            val now = System.currentTimeMillis()
            dao.insertNotification(
                TriageNotificationEntity(
                    type = "SMS",
                    sender = "Nami",
                    content = "Boss! Have you looked over the ship budget for this month? Need your sign-off before noon!",
                    timestamp = now - 15 * 60 * 1000L,
                    chopperQuestion = "Boss, you received an urgent text from Nami! Should I reply that you'll review it in 30 minutes, or call her back?",
                    suggestedActions = "Reply: Reviewing in 30m, Call Nami, Remind later",
                    status = "UNHANDLED"
                )
            )
            dao.insertNotification(
                TriageNotificationEntity(
                    type = "CALL",
                    sender = "Zoro (Missed Call)",
                    content = "Missed incoming call (rang for 24s)",
                    timestamp = now - 45 * 60 * 1000L,
                    chopperQuestion = "Boss! Zoro called earlier and you missed it! He probably got lost again... should I help you call him back?",
                    suggestedActions = "Call Back, Send SMS: I'll call you later, Dismiss",
                    status = "UNHANDLED"
                )
            )
            dao.insertNotification(
                TriageNotificationEntity(
                    type = "EMAIL",
                    sender = "Robin (Fleet Logistics)",
                    content = "Subject: Ancient Map expedition schedule and research notes attached.",
                    timestamp = now - 90 * 60 * 1000L,
                    chopperQuestion = "Boss, Robin sent research documentation via email. Would you like me to schedule a reading block tonight?",
                    suggestedActions = "Schedule Reading Block, Mark Handled, Ignore",
                    status = "UNHANDLED"
                )
            )
        }
    }

    suspend fun insertReminder(reminder: ReminderEntity): Long = dao.insertReminder(reminder)
    suspend fun updateReminder(reminder: ReminderEntity) = dao.updateReminder(reminder)
    suspend fun deleteReminder(id: Long) = dao.deleteReminder(id)
    suspend fun updateReminderStatus(id: Long, status: String) = dao.updateReminderStatus(id, status)
    suspend fun rescheduleReminder(id: Long, newTimestamp: Long) = dao.rescheduleReminder(id, newTimestamp)

    suspend fun insertNotification(notification: TriageNotificationEntity): Long = dao.insertNotification(notification)
    suspend fun updateNotification(notification: TriageNotificationEntity) = dao.updateNotification(notification)
    suspend fun updateNotificationStatus(id: Long, status: String) = dao.updateNotificationStatus(id, status)

    suspend fun insertChatMessage(message: ChatMessageEntity): Long = dao.insertChatMessage(message)
    suspend fun clearChatHistory() = dao.clearChatHistory()
}
