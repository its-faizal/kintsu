package com.kintsu.app.sync.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Maps a local Room entity (by table name and local ID) to a globally unique stable UUID
 * used for cloud synchronization across multiple devices.
 */
@Entity(
    tableName = "sync_registry",
    primaryKeys = ["localTableName", "localRecordId"],
    indices = [
        Index("syncUuid", unique = true),
        Index("localTableName")
    ]
)
data class SyncRegistryEntity(
    val localTableName: String,
    val localRecordId: Long,
    val syncUuid: String,
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false
)

/**
 * Persistent mutation queue storing pending local changes (UPSERT or DELETE)
 * to be uploaded to the cloud when network is available. Survives process death and reboots.
 */
@Entity(
    tableName = "sync_queue",
    indices = [
        Index("syncUuid"),
        Index("tableName")
    ]
)
data class SyncQueueEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val syncUuid: String,
    val tableName: String,
    val operation: String, // "UPSERT" or "DELETE"
    val payloadJson: String,
    val createdAt: Long = System.currentTimeMillis(),
    val retryCount: Int = 0
)

enum class SyncOperation {
    UPSERT,
    DELETE
}

enum class SyncState {
    IDLE,
    SYNCING,
    SUCCESS,
    OFFLINE,
    ERROR
}
