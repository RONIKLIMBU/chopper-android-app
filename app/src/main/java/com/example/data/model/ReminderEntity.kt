package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val targetTimestamp: Long,
    val remindDayBefore: Boolean = true,
    val remindDayOf: Boolean = true,
    val isDayBeforeNotified: Boolean = false,
    val isDayOfNotified: Boolean = false,
    val status: String = "ACTIVE", // "ACTIVE", "COMPLETED", "RESCHEDULED"
    val notes: String = ""
)
