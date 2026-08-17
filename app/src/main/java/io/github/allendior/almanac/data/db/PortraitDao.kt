package io.github.allendior.almanac.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PortraitDao {

    /** Newest capture first, across the whole archive — same-day entries sort by time. */
    @Query("SELECT * FROM portrait_entries ORDER BY captured_at_epoch_ms DESC")
    fun observeAll(): Flow<List<PortraitEntryEntity>>

    @Query("SELECT * FROM portrait_entries ORDER BY captured_at_epoch_ms DESC")
    suspend fun getAll(): List<PortraitEntryEntity>

    @Query("SELECT * FROM portrait_entries WHERE id = :id")
    suspend fun findById(id: String): PortraitEntryEntity?

    @Query("SELECT * FROM portrait_entries WHERE day_id = :dayId ORDER BY captured_at_epoch_ms DESC")
    suspend fun findAllByDay(dayId: String): List<PortraitEntryEntity>

    @Query("SELECT COUNT(*) FROM portrait_entries")
    suspend fun count(): Int

    /** Import must never overwrite an entry that's already in the archive. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfAbsent(entry: PortraitEntryEntity): Long

    /** A fresh capture is always a new row — multiple entries per day are allowed. */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entry: PortraitEntryEntity)

    @Query("UPDATE portrait_entries SET note = :note WHERE id = :id")
    suspend fun updateNote(id: String, note: String?)

    @Query("DELETE FROM portrait_entries WHERE id = :id")
    suspend fun delete(id: String)
}
