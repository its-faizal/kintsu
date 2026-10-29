package com.kintsu.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kintsu.app.data.model.DailyIntentionEntity
import com.kintsu.app.data.model.FocusSessionEntity
import com.kintsu.app.data.model.JournalEntryEntity
import com.kintsu.app.data.model.WeeklyReviewEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FocusDao {
    @Query("SELECT * FROM focus_sessions ORDER BY startTimeMillis DESC")
    fun getAllSessions(): Flow<List<FocusSessionEntity>>

    @Query("SELECT * FROM focus_sessions WHERE dateEpochDay = :dateEpochDay ORDER BY startTimeMillis DESC")
    fun getSessionsForDay(dateEpochDay: Long): Flow<List<FocusSessionEntity>>

    @Query("SELECT * FROM focus_sessions WHERE dateEpochDay >= :startEpochDay AND dateEpochDay <= :endEpochDay ORDER BY startTimeMillis DESC")
    fun getSessionsForRange(startEpochDay: Long, endEpochDay: Long): Flow<List<FocusSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: FocusSessionEntity): Long

    @Delete
    suspend fun deleteSession(session: FocusSessionEntity)

    @Query("DELETE FROM focus_sessions")
    suspend fun clearAllSessions()
}

@Dao
interface DailyIntentionDao {
    @Query("SELECT * FROM daily_intentions WHERE dateEpochDay = :dateEpochDay LIMIT 1")
    fun getIntentionForDay(dateEpochDay: Long): Flow<DailyIntentionEntity?>

    @Query("SELECT * FROM daily_intentions ORDER BY dateEpochDay DESC")
    fun getAllIntentions(): Flow<List<DailyIntentionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(intention: DailyIntentionEntity): Long

    @Delete
    suspend fun delete(intention: DailyIntentionEntity)

    @Query("DELETE FROM daily_intentions")
    suspend fun clearAll()
}

@Dao
interface JournalDao {
    @Query("SELECT * FROM journal_entries ORDER BY dateEpochDay DESC")
    fun getAllEntries(): Flow<List<JournalEntryEntity>>

    @Query("SELECT * FROM journal_entries WHERE dateEpochDay = :dateEpochDay LIMIT 1")
    fun getEntryForDay(dateEpochDay: Long): Flow<JournalEntryEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entry: JournalEntryEntity): Long

    @Delete
    suspend fun delete(entry: JournalEntryEntity)

    @Query("DELETE FROM journal_entries")
    suspend fun clearAll()
}

@Dao
interface WeeklyReviewDao {
    @Query("SELECT * FROM weekly_reviews ORDER BY weekStartEpochDay DESC")
    fun getAllReviews(): Flow<List<WeeklyReviewEntity>>

    @Query("SELECT * FROM weekly_reviews WHERE weekStartEpochDay = :weekStartEpochDay LIMIT 1")
    fun getReviewForWeek(weekStartEpochDay: Long): Flow<WeeklyReviewEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(review: WeeklyReviewEntity): Long

    @Delete
    suspend fun delete(review: WeeklyReviewEntity)

    @Query("DELETE FROM weekly_reviews")
    suspend fun clearAll()
}
