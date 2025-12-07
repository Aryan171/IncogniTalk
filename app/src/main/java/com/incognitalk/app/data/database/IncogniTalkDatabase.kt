package com.incognitalk.app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.incognitalk.app.data.dao.ChatDao
import com.incognitalk.app.data.dao.MessageDao
import com.incognitalk.app.data.model.Chat
import com.incognitalk.app.data.model.Message

@Database(entities = [Chat::class, Message::class], version = 1, exportSchema = false)
abstract class IncogniTalkDatabase : RoomDatabase() {

    abstract fun chatDao(): ChatDao
    abstract fun messageDao(): MessageDao

    companion object {
        @Volatile
        private var INSTANCE: IncogniTalkDatabase? = null

        fun getDatabase(context: Context): IncogniTalkDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    IncogniTalkDatabase::class.java,
                    "incognitalk_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
