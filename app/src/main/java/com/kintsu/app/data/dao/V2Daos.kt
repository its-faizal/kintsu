package com.kintsu.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kintsu.app.data.model.DailyIntentionEntity
import com.kintsu.app.data.model.FocusSessionEntity
import com.kintsu.app.data.model.JournalCollectionEntity
import com.kintsu.app.data.model.JournalEntryEntity
import com.kintsu.app.data.model.JournalTemplateEntity
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
    @Query("SELECT * FROM journal_entries ORDER BY dateEpochDay DESC, createdAt DESC")
    fun getAllEntries(): Flow<List<JournalEntryEntity>>

    @Query("SELECT * FROM journal_entries WHERE dateEpochDay = :dateEpochDay ORDER BY createdAt DESC LIMIT 1")
    fun getEntryForDay(dateEpochDay: Long): Flow<JournalEntryEntity?>

    @Query("SELECT * FROM journal_entries WHERE dateEpochDay = :dateEpochDay ORDER BY createdAt DESC")
    fun getEntriesForDayAll(dateEpochDay: Long): Flow<List<JournalEntryEntity>>

    @Query("SELECT * FROM journal_entries WHERE id = :id LIMIT 1")
    fun getEntryById(id: Long): Flow<JournalEntryEntity?>

    @Query("SELECT * FROM journal_entries WHERE collectionId = :collectionId ORDER BY dateEpochDay DESC, createdAt DESC")
    fun getEntriesByCollection(collectionId: Long): Flow<List<JournalEntryEntity>>

    @Query("SELECT * FROM journal_entries WHERE isFavorite = 1 ORDER BY dateEpochDay DESC, createdAt DESC")
    fun getFavoriteEntries(): Flow<List<JournalEntryEntity>>

    @Query("SELECT * FROM journal_entries WHERE photoUris != '' AND photoUris IS NOT NULL ORDER BY dateEpochDay DESC")
    fun getEntriesWithPhotos(): Flow<List<JournalEntryEntity>>

    @Query("SELECT * FROM journal_entries WHERE content LIKE '%' || :query || '%' OR title LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%' ORDER BY dateEpochDay DESC")
    fun searchEntries(query: String): Flow<List<JournalEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entry: JournalEntryEntity): Long

    @Delete
    suspend fun delete(entry: JournalEntryEntity)

    @Query("DELETE FROM journal_entries")
    suspend fun clearAll()

    // Collections
    @Query("SELECT * FROM journal_collections WHERE isArchived = 0 ORDER BY id ASC")
    fun getAllCollections(): Flow<List<JournalCollectionEntity>>

    @Query("SELECT * FROM journal_collections WHERE id = :id LIMIT 1")
    fun getCollectionById(id: Long): Flow<JournalCollectionEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollection(collection: JournalCollectionEntity): Long

    @Update
    suspend fun updateCollection(collection: JournalCollectionEntity)

    @Delete
    suspend fun deleteCollection(collection: JournalCollectionEntity)

    @Query("UPDATE journal_entries SET collectionId = :toCollectionId WHERE collectionId = :fromCollectionId")
    suspend fun moveEntriesToCollection(fromCollectionId: Long, toCollectionId: Long)

    // Templates
    @Query("SELECT * FROM journal_templates ORDER BY isFavorite DESC, id ASC")
    fun getAllTemplates(): Flow<List<JournalTemplateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: JournalTemplateEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTemplates(templates: List<JournalTemplateEntity>)

    @Update
    suspend fun updateTemplate(template: JournalTemplateEntity)

    @Delete
    suspend fun deleteTemplate(template: JournalTemplateEntity)
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
