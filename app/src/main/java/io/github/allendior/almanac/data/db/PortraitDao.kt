package io.github.allendior.almanac.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PortraitDao {

    @Query("SELECT * FROM portrait_entries ORDER BY day_id DESC")
    fun observeAll(): Flow<List<PortraitEntryEntity>>

    @Query("SELECT * FROM portrait_entries ORDER BY day_id DESC")
    suspend fun getAll(): List<PortraitEntryEntity>

    @Query("SELECT * FROM portrait_entries WHERE day_id = :dayId")
    suspend fun findByDay(dayId: String): PortraitEntryEntity?

    @Query("SELECT COUNT(*) FROM portrait_entries")
    suspend fun count(): Int

    /** Import must never overwrite what is already in the archive. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfAbsent(entry: PortraitEntryEntity): Long

    /** Capture replaces today deliberately, after the caller has removed the old file. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: PortraitEntryEntity)

    @Query("UPDATE portrait_entries SET note = :note WHERE day_id = :dayId")
    suspend fun updateNote(dayId: String, note: String?)

    @Query("DELETE FROM portrait_entries WHERE day_id = :dayId")
    suspend fun delete(dayId: String)
}
