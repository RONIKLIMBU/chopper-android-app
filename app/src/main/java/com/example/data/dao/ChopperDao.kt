package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ChatMessageEntity
import com.example.data.model.ReminderEntity
import com.example.data.model.TriageNotificationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChopperDao {
    // Reminders
    @Query("SELECT * FROM reminders ORDER BY targetTimestamp ASC")
    fun getAllReminders(): Flow<List<ReminderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderEntity): Long

    @Update
    suspend fun updateReminder(reminder: ReminderEntity)

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteReminder(id: Long)

    @Query("UPDATE reminders SET status = :status WHERE id = :id")
    suspend fun updateReminderStatus(id: Long, status: String)

    @Query("UPDATE reminders SET targetTimestamp = :newTimestamp, status = 'ACTIVE' WHERE id = :id")
    suspend fun rescheduleReminder(id: Long, newTimestamp: Long)

    // Triage Notifications
    @Query("SELECT * FROM triage_notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<TriageNotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: TriageNotificationEntity): Long

    @Update
    suspend fun updateNotification(notification: TriageNotificationEntity)

    @Query("UPDATE triage_notifications SET status = :status WHERE id = :id")
    suspend fun updateNotificationStatus(id: Long, status: String)

    // Chat History
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllChatMessages(): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessageEntity): Long

    @Query("DELETE FROM chat_messages")
    suspend fun clearChatHistory()
}
