package com.kintsu.app.coin

import android.content.Context
import com.kintsu.app.data.KintsuDatabase
import com.kintsu.app.data.SettingsDataStore
import com.kintsu.app.data.model.WalletTransactionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

enum class DeductionType(
    val coins: Int,
    val reason: String,
    val sourceActivity: String,
    val cooldownMillis: Long
) {
    REEL_BINGE_OVERSTAY(
        coins = 3,
        reason = "Attention tax: Reel feed overstay past mindful intercept",
        sourceActivity = "REEL_BINGE_OVERSTAY",
        cooldownMillis = 10 * 60 * 1000L // 10 minutes
    ),
    BEDTIME_CURFEW_VIOLATION(
        coins = 5,
        reason = "Sanctuary curfew breach: Distraction opened during sleep window",
        sourceActivity = "BEDTIME_VIOLATION",
        cooldownMillis = 15 * 60 * 1000L // 15 minutes
    ),
    MARATHON_OVERSTAY(
        coins = 2,
        reason = "Focus breach: Continuous marathon session past limit",
        sourceActivity = "MARATHON_OVERSTAY",
        cooldownMillis = 15 * 60 * 1000L // 15 minutes
    ),
    SLOTH_DECAY(
        coins = 1,
        reason = "Sloth decay: Habit neglected yesterday without gold mend",
        sourceActivity = "SLOTH_DECAY",
        cooldownMillis = 24 * 60 * 60 * 1000L // Once per day
    )
}

object NegativeEconomyEngine {

    private val lastDeductionTimestamps = mutableMapOf<String, Long>()

    /**
     * Executes a negative coin deduction safely with hardware-signed audit record.
     */
    fun recordDeduction(
        context: Context,
        type: DeductionType,
        targetKey: String = "",
        customNote: String? = null
    ) {
        val now = System.currentTimeMillis()
        val cooldownKey = "${type.name}_$targetKey"
        val lastTime = lastDeductionTimestamps[cooldownKey] ?: 0L

        if (now - lastTime < type.cooldownMillis) {
            return // Skip within cooldown to avoid cascade penalties
        }
        lastDeductionTimestamps[cooldownKey] = now

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = KintsuDatabase.getDatabase(context)
                val settings = SettingsDataStore(context)

                // Deduct coins from balance
                settings.deductCoins(type.coins)

                // Fetch latest transaction for cryptographic hash chaining
                val latestTx = db.wellbeingDao().getLatestWalletTransaction()
                val prevHash = latestTx?.transactionHash ?: "GENESIS"

                val sourceEventId = "deduction_${type.sourceActivity}_${now}_$targetKey"
                val reason = customNote ?: type.reason

                val txHash = CoinSecurityGuard.computeTransactionHash(
                    previousHash = prevHash,
                    amount = -type.coins,
                    type = "DEDUCTION",
                    sourceActivity = type.sourceActivity,
                    sourceEventId = sourceEventId,
                    timestampMillis = now,
                    multiplier = 1.0f,
                    isDeduction = true
                )

                val signature = CoinSecurityGuard.signHash(context, txHash)

                db.wellbeingDao().insertWalletTransaction(
                    WalletTransactionEntity(
                        amount = -type.coins,
                        type = "DEDUCTION",
                        reason = reason,
                        sourceActivity = type.sourceActivity,
                        sourceEventId = sourceEventId,
                        timestampMillis = now,
                        previousHash = prevHash,
                        transactionHash = txHash,
                        signature = signature,
                        multiplier = 1.0f,
                        isDeduction = true
                    )
                )
            } catch (e: Exception) {
                // Safeguard against background thread crashes
            }
        }
    }

    /**
     * Checks yesterday's habit logs for missed/unrepaired habits and applies sloth decay.
     * Guaranteed idempotent: runs at most once per calendar day.
     */
    suspend fun checkDailySlothDecay(context: Context) {
        val db = KintsuDatabase.getDatabase(context)
        val todayEpochDay = LocalDate.now().toEpochDay()
        val yesterdayEpochDay = todayEpochDay - 1

        val checkKey = "sloth_check_$yesterdayEpochDay"
        val alreadyChecked = db.wellbeingDao().countTransactionsForSource(checkKey) > 0
        if (alreadyChecked) return

        val activeHabits = db.habitDao().getActiveHabitsList()
        val yesterdayLogs = db.habitDao().getLogsForDayList(yesterdayEpochDay)

        val completedHabitIds = yesterdayLogs
            .filter { it.isCompleted || it.isRepairedGold }
            .map { it.habitId }
            .toSet()

        val missedHabits = activeHabits.filter { it.id !in completedHabitIds && !it.isArchived }

        if (missedHabits.isNotEmpty()) {
            val totalDecay = missedHabits.size * DeductionType.SLOTH_DECAY.coins
            val settings = SettingsDataStore(context)
            settings.deductCoins(totalDecay)

            val latestTx = db.wellbeingDao().getLatestWalletTransaction()
            val prevHash = latestTx?.transactionHash ?: "GENESIS"
            val now = System.currentTimeMillis()

            val txHash = CoinSecurityGuard.computeTransactionHash(
                previousHash = prevHash,
                amount = -totalDecay,
                type = "DEDUCTION",
                sourceActivity = "SLOTH_DECAY",
                sourceEventId = checkKey,
                timestampMillis = now,
                multiplier = 1.0f,
                isDeduction = true
            )

            val signature = CoinSecurityGuard.signHash(context, txHash)

            db.wellbeingDao().insertWalletTransaction(
                WalletTransactionEntity(
                    amount = -totalDecay,
                    type = "DEDUCTION",
                    reason = "Sloth decay: ${missedHabits.size} habit(s) neglected yesterday",
                    sourceActivity = "SLOTH_DECAY",
                    sourceEventId = checkKey,
                    timestampMillis = now,
                    previousHash = prevHash,
                    transactionHash = txHash,
                    signature = signature,
                    multiplier = 1.0f,
                    isDeduction = true
                )
            )
        }
    }
}
