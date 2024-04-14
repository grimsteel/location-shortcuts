package com.grimsteel.locationshortcuts.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Shortcut::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class ShortcutDatabase : RoomDatabase() {
    abstract fun shortcutDao(): ShortcutDao

    companion object {
        @Volatile
        private var Instance: ShortcutDatabase? = null

        fun getDatabase(context: Context): ShortcutDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, ShortcutDatabase::class.java, "shortcut_database")
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }
    }
}