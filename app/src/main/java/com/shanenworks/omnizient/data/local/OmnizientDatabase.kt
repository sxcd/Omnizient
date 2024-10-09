package com.shanenworks.omnizient.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.shanenworks.omnizient.data.local.dao.DocumentDao
import com.shanenworks.omnizient.data.local.dao.IndexEntryDao
import com.shanenworks.omnizient.data.local.entity.Document
import com.shanenworks.omnizient.data.local.entity.IndexEntry

@Database(entities = [Document::class, IndexEntry::class], version = 1, exportSchema = false)
abstract class OmnizientDatabase : RoomDatabase() {

    abstract fun documentDao(): DocumentDao
    abstract fun indexEntryDao(): IndexEntryDao

    companion object {
        @Volatile
        private var INSTANCE: OmnizientDatabase? = null

        fun getDatabase(context: Context): OmnizientDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    OmnizientDatabase::class.java,
                    "omnizient_database"
                )
                    .fallbackToDestructiveMigration() // Add this line
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
