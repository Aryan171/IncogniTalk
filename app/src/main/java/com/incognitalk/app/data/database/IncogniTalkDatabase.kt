package com.incognitalk.app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.incognitalk.app.data.dao.*
import com.incognitalk.app.data.model.Chat
import com.incognitalk.app.data.model.Message
import com.incognitalk.app.data.model.User
import com.incognitalk.app.data.model.keystore.*

@Database(
    entities = [
        Chat::class, Message::class, IdentityKey::class, PreKey::class, SignedPreKey::class, Session::class, RemoteIdentity::class, User::class
    ],
    version = 3,
    exportSchema = false
)
abstract class IncogniTalkDatabase : RoomDatabase() {

    abstract fun chatDao(): ChatDao
    abstract fun messageDao(): MessageDao
    abstract fun identityKeyDao(): IdentityKeyDao
    abstract fun preKeyDao(): PreKeyDao
    abstract fun signedPreKeyDao(): SignedPreKeyDao
    abstract fun sessionDao(): SessionDao
    abstract fun remoteIdentityDao(): RemoteIdentityDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: IncogniTalkDatabase? = null

        fun getDatabase(context: Context): IncogniTalkDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    IncogniTalkDatabase::class.java,
                    "incognitalk_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
