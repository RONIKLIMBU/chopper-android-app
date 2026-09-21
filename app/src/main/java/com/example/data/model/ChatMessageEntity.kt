package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sender: String, // "USER", "CHOPPER"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isThinkingMode: Boolean = false,
    val thinkingSummary: String? = null
)
