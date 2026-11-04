package com.kintsu.app.ui.shop

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kintsu.app.coin.CoinSecurityGuard
import com.kintsu.app.coin.FireStreakEngine
import com.kintsu.app.data.model.WalletTransactionEntity
import com.kintsu.app.ui.components.IconGoldSeam
import com.kintsu.app.ui.components.KintsuBottomSheet
import com.kintsu.app.ui.components.KintsuCard
import com.kintsu.app.ui.theme.KintsuTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class WalletFilterTab {
    ALL,
    EARNED,
    SPENT,
    DEDUCTIONS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletHistorySheet(
    coinsBalance: Int,
    coinsEarnedTotal: Int,
    transactions: List<WalletTransactionEntity>,
    streakDays: Int = 0,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography
    val dateFormat = remember { SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()) }

    var selectedTab by remember { mutableStateOf(WalletFilterTab.ALL) }
    val tier = remember(streakDays) { FireStreakEngine.getTier(streakDays) }

    // Run cryptographic verification on the full hash chain
    val verificationResult = remember(transactions) {
        CoinSecurityGuard.verifyLedgerAndReconcile(context, transactions)
    }

    val earnedList = remember(transactions) { transactions.filter { it.amount > 0 } }
    val spentList = remember(transactions) { transactions.filter { it.amount < 0 && !it.isDeduction } }
    val deductionList = remember(transactions) { transactions.filter { it.isDeduction || it.type == "DEDUCTION" } }

    val displayedTransactions = remember(selectedTab, transactions) {
        when (selectedTab) {
            WalletFilterTab.ALL -> transactions
            WalletFilterTab.EARNED -> earnedList
            WalletFilterTab.SPENT -> spentList
            WalletFilterTab.DEDUCTIONS -> deductionList
        }
    }

    KintsuBottomSheet(
        onDismissRequest = onDismiss,
        title = "Sanctuary Passbook",
        scrollable = false
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(spacing.m)
        ) {
            // 1. Balance & Fire Streak Rank Hero Card
            item {
                KintsuCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = colors.surfaceElevated,
                    borderColor = colors.goldSeam.copy(alpha = 0.5f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(spacing.l),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(colors.goldSeam.copy(alpha = 0.15f))
                                    .border(1.5.dp, colors.goldSeam, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = tier.rankBadge,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.goldSeam
                                )
                            }

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "$coinsBalance",
                                        style = typography.screenTitle.copy(
                                            color = colors.goldSeam,
                                            fontSize = 32.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Kintsu Gold",
                                        style = typography.itemTitle.copy(color = colors.textPrimary, fontSize = 16.sp)
                                    )
                                }
                                Text(
                                    text = "${tier.title} • ${streakDays}d Streak (${tier.multiplier}x Multiplier)",
                                    style = typography.caption.copy(color = colors.accent, fontWeight = FontWeight.SemiBold)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(spacing.s))
                        Text(
                            text = tier.description,
                            style = typography.secondary.copy(fontSize = 12.sp, textAlign = TextAlign.Center),
                            color = colors.textSecondary
                        )
                    }
                }
            }

            // 2. Cryptographic Security Guard Seal Card
            item {
                KintsuCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = colors.surfaceElevated,
                    borderColor = if (verificationResult.isValid) colors.accent.copy(alpha = 0.4f) else colors.error.copy(alpha = 0.6f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(spacing.m)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(if (verificationResult.isValid) colors.accentSoft else colors.errorContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (verificationResult.isValid) "✓" else "!",
                                        color = if (verificationResult.isValid) colors.accent else colors.error,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = if (verificationResult.isValid) "Ledger Cryptographically Verified" else "Ledger Verification Warning",
                                    style = typography.itemTitle.copy(
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (verificationResult.isValid) colors.textPrimary else colors.error
                                    )
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(colors.surface)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Android KeyStore",
                                    style = typography.caption.copy(fontSize = 10.sp, color = colors.textSecondary)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(spacing.xs))
                        Text(
                            text = "HMAC-SHA256 chained audit with hardware root of trust • Monotonic anti-tamper clock active • Zero-sum reconciled (+${verificationResult.totalEarned} / -${verificationResult.totalSpent} / -${verificationResult.totalDeducted})",
                            style = typography.caption.copy(fontSize = 11.sp, lineHeight = 16.sp),
                            color = colors.textSecondary
                        )
                    }
                }
            }

            // 3. Economy Rules & Attention Taxes Reference Card
            item {
                KintsuCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = colors.accentSoft.copy(alpha = 0.15f),
                    borderColor = colors.accent.copy(alpha = 0.25f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(spacing.m)
                    ) {
                        Text(
                            text = "Strict Discipline Economy & Attention Taxes",
                            style = typography.itemTitle.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
                            color = colors.accent
                        )
                        Spacer(modifier = Modifier.height(spacing.xs))
                        Text(
                            text = "• Earning Scale: 1 Gold (Low Task / Intention / Reel Resist) • 2 Gold (Habit / Med Task / Focus / Journal) • 3 Gold (High Task / 45m+ Focus / Review)\n• Early Bird Bonus: +2 Gold for morning habit completed before 09:00 AM\n• Fire Multiplier: Spark (1.0x) up to Phoenix (2.0x Max at 60 Days). Ranks 90-150d gate prestigious catalog masterpieces\n• Attention Taxes: -3 Reel Binge Overstay • -5 Bedtime Curfew Violation • -2 Marathon Overstay • -1 Sloth Decay per missed habit",
                            style = typography.secondary.copy(fontSize = 11.sp, lineHeight = 16.sp),
                            color = colors.textPrimary
                        )
                    }
                }
            }

            // 4. Filter Tabs Row
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing.s)
                ) {
                    item {
                        FilterPill(
                            label = "All (${transactions.size})",
                            selected = selectedTab == WalletFilterTab.ALL,
                            onClick = { selectedTab = WalletFilterTab.ALL }
                        )
                    }
                    item {
                        FilterPill(
                            label = "Earned (${earnedList.size})",
                            selected = selectedTab == WalletFilterTab.EARNED,
                            onClick = { selectedTab = WalletFilterTab.EARNED }
                        )
                    }
                    item {
                        FilterPill(
                            label = "Spent (${spentList.size})",
                            selected = selectedTab == WalletFilterTab.SPENT,
                            onClick = { selectedTab = WalletFilterTab.SPENT }
                        )
                    }
                    item {
                        FilterPill(
                            label = "Attention Taxes (${deductionList.size})",
                            selected = selectedTab == WalletFilterTab.DEDUCTIONS,
                            onClick = { selectedTab = WalletFilterTab.DEDUCTIONS }
                        )
                    }
                }
            }

            // 5. Transaction Statement Items
            if (displayedTransactions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = spacing.xl),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No recorded transactions in this view.",
                            style = typography.secondary,
                            color = colors.textSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(displayedTransactions, key = { it.id }) { tx ->
                    val isEarned = tx.amount > 0
                    val isDeduction = tx.isDeduction || tx.type == "DEDUCTION"

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.surfaceElevated)
                            .border(
                                width = 1.dp,
                                color = if (isDeduction) colors.error.copy(alpha = 0.35f) else colors.border,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .padding(horizontal = spacing.m, vertical = spacing.s),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isDeduction -> colors.errorContainer.copy(alpha = 0.4f)
                                            isEarned -> colors.accentSoft
                                            else -> colors.surface
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = when {
                                        isDeduction -> "!"
                                        isEarned -> "+"
                                        else -> "-"
                                    },
                                    color = when {
                                        isDeduction -> colors.error
                                        isEarned -> colors.accent
                                        else -> colors.textSecondary
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(spacing.m))

                            Column {
                                Text(
                                    text = tx.reason,
                                    style = typography.itemTitle.copy(fontSize = 13.sp),
                                    color = colors.textPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = dateFormat.format(Date(tx.timestampMillis)),
                                        style = typography.caption.copy(fontSize = 11.sp),
                                        color = colors.textSecondary
                                    )
                                    if (tx.transactionHash.isNotBlank()) {
                                        Text(
                                            text = " • #${tx.id} ${tx.transactionHash.take(6)}...",
                                            style = typography.caption.copy(
                                                fontSize = 10.sp,
                                                fontFamily = FontFamily.Monospace,
                                                color = colors.textSecondary.copy(alpha = 0.7f)
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(spacing.s))

                        Text(
                            text = if (isEarned) "+${tx.amount}" else "${tx.amount}",
                            style = typography.itemTitle.copy(
                                color = when {
                                    isDeduction -> colors.error
                                    isEarned -> colors.goldSeam
                                    else -> colors.textSecondary
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = KintsuTheme.colors
    val typography = KintsuTheme.typography

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) colors.accentSoft else colors.surfaceElevated)
            .border(
                width = 1.dp,
                color = if (selected) colors.accent else colors.border,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            style = typography.caption.copy(
                color = if (selected) colors.accent else colors.textSecondary,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 12.sp
            )
        )
    }
}
