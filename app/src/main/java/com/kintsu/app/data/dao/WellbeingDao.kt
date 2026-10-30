package com.kintsu.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kintsu.app.data.model.AppGroupEntity
import com.kintsu.app.data.model.BlockRuleEntity
import com.kintsu.app.data.model.BlockScheduleEntity
import com.kintsu.app.data.model.CommitmentLockEntity
import com.kintsu.app.data.model.DigitalDetoxEntity
import com.kintsu.app.data.model.QuitHabitEntity
import com.kintsu.app.data.model.ShopItemEntity
import com.kintsu.app.data.model.UrgeLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WellbeingDao {

    // App Groups
    @Query("SELECT * FROM app_groups ORDER BY id ASC")
    fun getAllAppGroups(): Flow<List<AppGroupEntity>>

    @Query("SELECT * FROM app_groups WHERE id = :id LIMIT 1")
    suspend fun getAppGroupById(id: Long): AppGroupEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppGroup(group: AppGroupEntity): Long

    @Update
    suspend fun updateAppGroup(group: AppGroupEntity)

    @Delete
    suspend fun deleteAppGroup(group: AppGroupEntity)

    // Block Rules
    @Query("SELECT * FROM block_rules ORDER BY id ASC")
    fun getAllBlockRules(): Flow<List<BlockRuleEntity>>

    @Query("SELECT * FROM block_rules WHERE isEnabled = 1")
    fun getActiveBlockRules(): Flow<List<BlockRuleEntity>>

    @Query("SELECT * FROM block_rules WHERE appGroupId = :groupId")
    suspend fun getRulesForGroup(groupId: Long): List<BlockRuleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlockRule(rule: BlockRuleEntity): Long

    @Update
    suspend fun updateBlockRule(rule: BlockRuleEntity)

    @Delete
    suspend fun deleteBlockRule(rule: BlockRuleEntity)

    // Block Schedules
    @Query("SELECT * FROM block_schedules ORDER BY id ASC")
    fun getAllSchedules(): Flow<List<BlockScheduleEntity>>

    @Query("SELECT * FROM block_schedules WHERE ruleId = :ruleId")
    suspend fun getSchedulesForRule(ruleId: Long): List<BlockScheduleEntity>

    @Query("SELECT * FROM block_schedules WHERE isEnabled = 1")
    fun getActiveSchedules(): Flow<List<BlockScheduleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: BlockScheduleEntity): Long

    @Update
    suspend fun updateSchedule(schedule: BlockScheduleEntity)

    @Delete
    suspend fun deleteSchedule(schedule: BlockScheduleEntity)

    // Digital Detox
    @Query("SELECT * FROM digital_detox_sessions ORDER BY startTimeMillis DESC")
    fun getAllDetoxSessions(): Flow<List<DigitalDetoxEntity>>

    @Query("SELECT * FROM digital_detox_sessions WHERE isCompleted = 0 AND endTimeMillis > :currentTime ORDER BY id DESC LIMIT 1")
    suspend fun getActiveDetox(currentTime: Long): DigitalDetoxEntity?

    @Query("SELECT * FROM digital_detox_sessions WHERE isCompleted = 1")
    fun getCompletedDetoxSessions(): Flow<List<DigitalDetoxEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDetoxSession(session: DigitalDetoxEntity): Long

    @Update
    suspend fun updateDetoxSession(session: DigitalDetoxEntity)

    // Quit Habits
    @Query("SELECT * FROM quit_habits ORDER BY id ASC")
    fun getAllQuitHabits(): Flow<List<QuitHabitEntity>>

    @Query("SELECT * FROM quit_habits WHERE isArchived = 0 ORDER BY id ASC")
    fun getActiveQuitHabits(): Flow<List<QuitHabitEntity>>

    @Query("SELECT * FROM quit_habits WHERE id = :id LIMIT 1")
    suspend fun getQuitHabitById(id: Long): QuitHabitEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuitHabit(quitHabit: QuitHabitEntity): Long

    @Update
    suspend fun updateQuitHabit(quitHabit: QuitHabitEntity)

    @Delete
    suspend fun deleteQuitHabit(quitHabit: QuitHabitEntity)

    // Urge Logs
    @Query("SELECT * FROM urge_logs WHERE quitHabitId = :quitHabitId ORDER BY timestampMillis DESC")
    fun getUrgeLogsForHabit(quitHabitId: Long): Flow<List<UrgeLogEntity>>

    @Query("SELECT * FROM urge_logs ORDER BY timestampMillis DESC")
    fun getAllUrgeLogs(): Flow<List<UrgeLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUrgeLog(log: UrgeLogEntity): Long

    // Commitment Locks
    @Query("SELECT * FROM commitment_locks WHERE isCancelled = 0 AND isApplied = 0 ORDER BY unlocksAtMillis ASC")
    fun getActiveCommitmentLocks(): Flow<List<CommitmentLockEntity>>

    @Query("SELECT * FROM commitment_locks WHERE isCancelled = 0 AND isApplied = 0 AND unlocksAtMillis <= :now")
    suspend fun getUnlockedCommitmentLocks(now: Long): List<CommitmentLockEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommitmentLock(lock: CommitmentLockEntity): Long

    @Update
    suspend fun updateCommitmentLock(lock: CommitmentLockEntity)

    // Sanctuary Shop & Inventory
    @Query("SELECT * FROM shop_inventory ORDER BY costCoins ASC")
    fun getAllShopItems(): Flow<List<ShopItemEntity>>

    @Query("SELECT * FROM shop_inventory WHERE id = :id LIMIT 1")
    suspend fun getShopItemById(id: String): ShopItemEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun seedShopItems(items: List<ShopItemEntity>)

    @Update
    suspend fun updateShopItem(item: ShopItemEntity)

    @Query("UPDATE shop_inventory SET isEquipped = 0 WHERE itemType = :itemType")
    suspend fun unequipAllOfType(itemType: String)

    // Clear all for testing / reset
    @Query("DELETE FROM app_groups")
    suspend fun clearAppGroups()

    @Query("DELETE FROM block_rules")
    suspend fun clearBlockRules()

    @Query("DELETE FROM block_schedules")
    suspend fun clearBlockSchedules()

    @Query("DELETE FROM digital_detox_sessions")
    suspend fun clearDetoxSessions()

    @Query("DELETE FROM quit_habits")
    suspend fun clearQuitHabits()

    @Query("DELETE FROM urge_logs")
    suspend fun clearUrgeLogs()

    @Query("DELETE FROM commitment_locks")
    suspend fun clearCommitmentLocks()

    @Query("DELETE FROM shop_inventory")
    suspend fun clearShopInventory()
}
