package com.almanac.portrait.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [PortraitEntryEntity::class], version = 1, exportSchema = true)
abstract class AlmanacDatabase : RoomDatabase() {

    abstract fun portraitDao(): PortraitDao

    companion object {
        @Volatile
        private var instance: AlmanacDatabase? = null

        fun get(context: Context): AlmanacDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                AlmanacDatabase::class.java,
                "almanac.db",
            )
                // No destructive fallback: a schema surprise must never silently
                // discard ten years of metadata. A future version ships a migration.
                .build()
                .also { instance = it }
        }
    }
}
