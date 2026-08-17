package io.github.allendior.almanac.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [PortraitEntryEntity::class], version = 2, exportSchema = true)
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
                .addMigrations(MIGRATION_1_2)
                // No destructive fallback: a schema surprise must never silently
                // discard ten years of metadata. Every version bump ships a real
                // migration instead.
                .build()
                .also { instance = it }
        }

        /**
         * v1 keyed rows by day_id (one entry per day, enforced by the schema itself).
         * v2 allows more than one portrait per day, so day_id can no longer be the
         * primary key — every existing row is given a freshly generated UUID as its new
         * identity, and day_id becomes an indexed, non-unique column. No row is
         * dropped or altered beyond gaining that id.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS portrait_entries_new (
                        id TEXT NOT NULL PRIMARY KEY,
                        day_id TEXT NOT NULL,
                        captured_at_epoch_ms INTEGER NOT NULL,
                        utc_offset_minutes INTEGER NOT NULL,
                        file_name TEXT NOT NULL,
                        sha256 TEXT NOT NULL,
                        mood TEXT,
                        number_value REAL,
                        camera_facing TEXT NOT NULL,
                        note TEXT
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    INSERT INTO portrait_entries_new
                        (id, day_id, captured_at_epoch_ms, utc_offset_minutes, file_name, sha256, mood, number_value, camera_facing, note)
                    SELECT
                        lower(
                            hex(randomblob(4)) || '-' || hex(randomblob(2)) || '-4' ||
                            substr(hex(randomblob(2)), 2) || '-' ||
                            substr('89ab', abs(random()) % 4 + 1, 1) || substr(hex(randomblob(2)), 2) || '-' ||
                            hex(randomblob(6))
                        ),
                        day_id, captured_at_epoch_ms, utc_offset_minutes, file_name, sha256, mood, number_value, camera_facing, note
                    FROM portrait_entries
                    """.trimIndent(),
                )
                db.execSQL("DROP TABLE portrait_entries")
                db.execSQL("ALTER TABLE portrait_entries_new RENAME TO portrait_entries")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_portrait_entries_day_id ON portrait_entries(day_id)")
            }
        }
    }
}
