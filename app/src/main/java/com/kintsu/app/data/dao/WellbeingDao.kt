package com.kintsu.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kintsu.app.data.model.AchievementEntity
import com.kintsu.app.data.model.AppGroupEntity
import com.kintsu.app.data.model.BlockedWebsiteEntity
import com.kintsu.app.data.model.BlockRuleEntity
import com.kintsu.app.data.model.BlockScheduleEntity
import com.kintsu.app.data.model.CommitmentLockEntity
import com.kintsu.app.data.model.DigitalDetoxEntity
import com.kintsu.app.data.model.DistractionInterventionLogEntity
import com.kintsu.app.data.model.FocusLockSessionEntity
import com.kintsu.app.data.model.IntentionalBreakLogEntity
import com.kintsu.app.data.model.PersonalRewardEntity
import com.kintsu.app.data.model.QuitHabitEntity
import com.kintsu.app.data.model.ShopItemEntity
import com.kintsu.app.data.model.StylePresetEntity
import com.kintsu.app.data.model.UrgeLogEntity
import com.kintsu.app.data.model.WalletTransactionEntity
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

    @Query("UPDATE shop_inventory SET isUnlocked = 1, costCoins = 0")
    suspend fun unlockAllShopItems()

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

    // --- Blocked Websites ---
    @Query("SELECT * FROM blocked_websites ORDER BY id ASC")
    fun getAllBlockedWebsites(): Flow<List<BlockedWebsiteEntity>>

    @Query("SELECT * FROM blocked_websites WHERE isEnabled = 1")
    fun getActiveBlockedWebsites(): Flow<List<BlockedWebsiteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlockedWebsite(website: BlockedWebsiteEntity): Long

    @Update
    suspend fun updateBlockedWebsite(website: BlockedWebsiteEntity)

    @Delete
    suspend fun deleteBlockedWebsite(website: BlockedWebsiteEntity)

    @Query("DELETE FROM blocked_websites")
    suspend fun clearBlockedWebsites()

    // --- Focus Lock Sessions ---
    @Query("SELECT * FROM focus_lock_sessions ORDER BY startTimeMillis DESC")
    fun getAllFocusLockSessions(): Flow<List<FocusLockSessionEntity>>

    @Query("SELECT * FROM focus_lock_sessions WHERE isCompleted = 0 AND endTimeMillis > :currentTime ORDER BY id DESC LIMIT 1")
    suspend fun getActiveFocusLockSession(currentTime: Long): FocusLockSessionEntity?

    @Query("SELECT * FROM focus_lock_sessions WHERE isCompleted = 0 AND endTimeMillis > :currentTime ORDER BY id DESC LIMIT 1")
    fun observeActiveFocusLockSession(currentTime: Long): Flow<FocusLockSessionEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFocusLockSession(session: FocusLockSessionEntity): Long

    @Update
    suspend fun updateFocusLockSession(session: FocusLockSessionEntity)

    @Query("DELETE FROM focus_lock_sessions")
    suspend fun clearFocusLockSessions()

    // --- Distraction Intervention Logs ---
    @Query("SELECT * FROM distraction_interventions ORDER BY timestampMillis DESC")
    fun getAllInterventions(): Flow<List<DistractionInterventionLogEntity>>

    @Query("SELECT * FROM distraction_interventions WHERE timestampMillis >= :sinceMillis ORDER BY timestampMillis DESC")
    fun getInterventionsSince(sinceMillis: Long): Flow<List<DistractionInterventionLogEntity>>

    @Query("SELECT COUNT(*) FROM distraction_interventions WHERE timestampMillis >= :startOfDayMillis")
    fun getInterventionsCountToday(startOfDayMillis: Long): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIntervention(log: DistractionInterventionLogEntity): Long

    @Query("DELETE FROM distraction_interventions")
    suspend fun clearDistractionInterventions()

    // --- Intentional Breaks ---
    @Query("SELECT * FROM intentional_breaks ORDER BY timestampMillis DESC")
    fun getAllBreaks(): Flow<List<IntentionalBreakLogEntity>>

    @Query("SELECT * FROM intentional_breaks WHERE timestampMillis >= :startOfDayMillis ORDER BY timestampMillis DESC")
    fun getBreaksToday(startOfDayMillis: Long): Flow<List<IntentionalBreakLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBreak(breakLog: IntentionalBreakLogEntity): Long

    @Query("DELETE FROM intentional_breaks")
    suspend fun clearIntentionalBreaks()

    // --- Shop Category & Wishlist ---
    @Query("SELECT * FROM shop_inventory WHERE category = :category ORDER BY costCoins ASC")
    fun getShopItemsByCategory(category: String): Flow<List<ShopItemEntity>>

    @Query("SELECT * FROM shop_inventory WHERE bundleId = :bundleId")
    suspend fun getShopItemsByBundle(bundleId: String): List<ShopItemEntity>

    @Query("UPDATE shop_inventory SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateShopItemFavorite(id: String, isFavorite: Boolean)

    // --- Wallet Transactions Ledger ---
    @Query("SELECT * FROM wallet_transactions ORDER BY timestampMillis DESC")
    fun getAllWalletTransactions(): Flow<List<WalletTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWalletTransaction(tx: WalletTransactionEntity): Long

    @Query("SELECT COUNT(*) FROM wallet_transactions WHERE sourceEventId = :sourceEventId")
    suspend fun countTransactionsForSource(sourceEventId: String): Int

    @Query("DELETE FROM wallet_transactions")
    suspend fun clearWalletTransactions()

    // --- Personal Rewards ---
    @Query("SELECT * FROM personal_rewards WHERE isArchived = 0 ORDER BY createdAtMillis DESC")
    fun getActivePersonalRewards(): Flow<List<PersonalRewardEntity>>

    @Query("SELECT * FROM personal_rewards ORDER BY createdAtMillis DESC")
    fun getAllPersonalRewards(): Flow<List<PersonalRewardEntity>>

    @Query("SELECT * FROM personal_rewards WHERE id = :id LIMIT 1")
    suspend fun getPersonalRewardById(id: Long): PersonalRewardEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPersonalReward(reward: PersonalRewardEntity): Long

    @Update
    suspend fun updatePersonalReward(reward: PersonalRewardEntity)

    @Delete
    suspend fun deletePersonalReward(reward: PersonalRewardEntity)

    @Query("DELETE FROM personal_rewards")
    suspend fun clearPersonalRewards()

    // --- Style Presets ---
    @Query("SELECT * FROM style_presets ORDER BY createdAtMillis DESC")
    fun getAllStylePresets(): Flow<List<StylePresetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStylePreset(preset: StylePresetEntity): Long

    @Delete
    suspend fun deleteStylePreset(preset: StylePresetEntity)

    @Query("DELETE FROM style_presets")
    suspend fun clearStylePresets()

    // --- Achievements ---
    @Query("SELECT * FROM achievements ORDER BY isEarned DESC, earnedAtMillis DESC, id ASC")
    fun getAllAchievements(): Flow<List<AchievementEntity>>

    @Query("SELECT * FROM achievements WHERE isEarned = 1")
    fun getEarnedAchievements(): Flow<List<AchievementEntity>>

    @Query("SELECT * FROM achievements WHERE id = :id LIMIT 1")
    suspend fun getAchievementById(id: String): AchievementEntity?

    @Query("SELECT * FROM achievements")
    suspend fun getAllAchievementsList(): List<AchievementEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAchievements(achievements: List<AchievementEntity>)

    @Update
    suspend fun updateAchievement(achievement: AchievementEntity)
}
