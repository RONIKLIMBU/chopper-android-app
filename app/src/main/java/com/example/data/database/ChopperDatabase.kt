package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.ChopperDao
import com.example.data.model.ChatMessageEntity
import com.example.data.model.ReminderEntity
import com.example.data.model.TriageNotificationEntity

@Database(
    entities = [
        ReminderEntity::class,
        TriageNotificationEntity::class,
        ChatMessageEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ChopperDatabase : RoomDatabase() {
    abstract fun chopperDao(): ChopperDao

    companion object {
        @Volatile
        private var INSTANCE: ChopperDatabase? = null

        fun getDatabase(context: Context): ChopperDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ChopperDatabase::class.java,
                    "chopper_database"
                ).fallbackToDestructiveMigration(true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
