package com.kintsu.app.ui.shop

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kintsu.app.data.model.WalletTransactionEntity
import com.kintsu.app.ui.components.IconGoldSeam
import com.kintsu.app.ui.components.KintsuBottomSheet
import com.kintsu.app.ui.components.KintsuCard
import com.kintsu.app.ui.theme.KintsuTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletHistorySheet(
    coinsBalance: Int,
    coinsEarnedTotal: Int,
    transactions: List<WalletTransactionEntity>,
    onDismiss: () -> Unit
) {
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography
    val dateFormat = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())

    KintsuBottomSheet(
        onDismissRequest = onDismiss,
        title = "Kintsu Gold Wallet",
        scrollable = false
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(spacing.m)
        ) {
            // Balance Hero Card
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
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(colors.goldSeam.copy(alpha = 0.15f))
                                .border(1.5.dp, colors.goldSeam, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            IconGoldSeam(color = colors.goldSeam, size = 30.dp)
                        }
                        Spacer(modifier = Modifier.height(spacing.s))
                        Text(
                            text = "$coinsBalance",
                            style = typography.screenTitle.copy(color = colors.goldSeam, fontSize = 36.sp, fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Available Kintsu Gold",
                            style = typography.itemTitle.copy(color = colors.textPrimary, fontSize = 14.sp)
                        )
                        Spacer(modifier = Modifier.height(spacing.xs))
                        Text(
                            text = "Lifetime Earned: $coinsEarnedTotal Gold",
                            style = typography.caption.copy(color = colors.textSecondary)
                        )
                    }
                }
            }

            // Earning Rules Reference
            item {
                KintsuCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = colors.accentSoft.copy(alpha = 0.25f),
                    borderColor = colors.accent.copy(alpha = 0.3f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(spacing.m)
                    ) {
                        Text(
                            text = "How to Earn Mindful Gold",
                            style = typography.itemTitle.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
                            color = colors.accent
                        )
                        Spacer(modifier = Modifier.height(spacing.xs))
                        Text(
                            text = "• Daily scheduled habit: +5 Gold\n• Task completion: +3 Gold\n• 25m Deep Focus session: +10 Gold\n• Weekly review completion: +20 Gold\n• Goal milestone achieved: +25 Gold",
                            style = typography.secondary.copy(fontSize = 12.sp, lineHeight = 18.sp),
                            color = colors.textPrimary
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Transaction History",
                    style = typography.sectionHeader.copy(fontSize = 14.sp),
                    color = colors.textSecondary,
                    modifier = Modifier.padding(top = spacing.xs)
                )
            }

            if (transactions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = spacing.xl),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No recorded transactions yet.\nComplete habits or focus sessions to earn your first gold.",
                            style = typography.secondary,
                            color = colors.textSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                items(transactions, key = { it.id }) { tx ->
                    val isEarned = tx.amount > 0

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.surfaceElevated)
                            .padding(horizontal = spacing.m, vertical = spacing.s),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(if (isEarned) colors.accentSoft else colors.errorContainer.copy(alpha = 0.4f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isEarned) "+" else "−",
                                    color = if (isEarned) colors.accent else colors.error,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(spacing.m))
                            Column {
                                Text(
                                    text = tx.reason,
                                    style = typography.itemTitle.copy(fontSize = 13.sp),
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = dateFormat.format(Date(tx.timestampMillis)),
                                    style = typography.caption.copy(fontSize = 11.sp),
                                    color = colors.textSecondary
                                )
                            }
                        }

                        Text(
                            text = if (isEarned) "+${tx.amount}" else "${tx.amount}",
                            color = if (isEarned) colors.goldSeam else colors.textSecondary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
