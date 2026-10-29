package com.kintsu.app.sync.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kintsu.app.sync.model.SyncQueueEntity
import com.kintsu.app.sync.model.SyncRegistryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncDao {

    // --- Sync Registry Operations ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRegistry(entry: SyncRegistryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRegistryBatch(entries: List<SyncRegistryEntity>)

    @Query("SELECT * FROM sync_registry WHERE localTableName = :tableName AND localRecordId = :localId LIMIT 1")
    suspend fun getRegistryEntry(tableName: String, localId: Long): SyncRegistryEntity?

    @Query("SELECT * FROM sync_registry WHERE syncUuid = :syncUuid LIMIT 1")
    suspend fun getRegistryByUuid(syncUuid: String): SyncRegistryEntity?

    @Query("SELECT * FROM sync_registry WHERE localTableName = :tableName")
    suspend fun getRegistryForTable(tableName: String): List<SyncRegistryEntity>

    @Query("SELECT * FROM sync_registry WHERE updatedAt > :sinceEpochMillis")
    suspend fun getModifiedSince(sinceEpochMillis: Long): List<SyncRegistryEntity>

    @Query("SELECT * FROM sync_registry")
    fun getAllRegistryFlow(): Flow<List<SyncRegistryEntity>>

    @Query("DELETE FROM sync_registry WHERE localTableName = :tableName AND localRecordId = :localId")
    suspend fun deleteRegistryEntry(tableName: String, localId: Long)

    @Query("DELETE FROM sync_registry")
    suspend fun clearRegistry()

    // --- Sync Queue Operations ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun enqueue(mutation: SyncQueueEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun enqueueBatch(mutations: List<SyncQueueEntity>)

    @Query("SELECT * FROM sync_queue ORDER BY id ASC LIMIT :limit")
    suspend fun peekQueue(limit: Int = 100): List<SyncQueueEntity>

    @Query("DELETE FROM sync_queue WHERE id = :id")
    suspend fun dequeue(id: Long)

    @Query("DELETE FROM sync_queue WHERE id IN (:ids)")
    suspend fun dequeueBatch(ids: List<Long>)

    @Query("UPDATE sync_queue SET retryCount = retryCount + 1 WHERE id = :id")
    suspend fun incrementRetryCount(id: Long)

    @Query("SELECT COUNT(*) FROM sync_queue")
    suspend fun getQueueCount(): Int

    @Query("DELETE FROM sync_queue")
    suspend fun clearQueue()
}
