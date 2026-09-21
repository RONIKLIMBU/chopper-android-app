package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "triage_notifications")
data class TriageNotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // "SMS", "CALL", "EMAIL"
    val sender: String,
    val content: String,
    val timestamp: Long,
    val chopperQuestion: String,
    val suggestedActions: String, // Comma separated actions
    val status: String = "UNHANDLED" // "UNHANDLED", "HANDLED", "DISMISSED"
)
