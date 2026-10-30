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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kintsu.app.data.model.PersonalRewardEntity
import com.kintsu.app.ui.components.IconGoldSeam
import com.kintsu.app.ui.components.KintsuBottomSheet
import com.kintsu.app.ui.components.KintsuButton
import com.kintsu.app.ui.components.KintsuButtonVariant
import com.kintsu.app.ui.components.KintsuCard
import com.kintsu.app.ui.components.KintsuDialog
import com.kintsu.app.ui.theme.KintsuTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalRewardsSheet(
    coinsBalance: Int,
    rewards: List<PersonalRewardEntity>,
    onCreateReward: (title: String, desc: String, cost: Int, category: String, icon: String) -> Unit,
    onRedeemReward: (PersonalRewardEntity) -> Unit,
    onArchiveReward: (PersonalRewardEntity) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography

    var showCreateDialog by remember { mutableStateOf(false) }
    var rewardToRedeem by remember { mutableStateOf<PersonalRewardEntity?>(null) }

    // Dialog state for new reward
    var titleInput by remember { mutableStateOf("") }
    var descInput by remember { mutableStateOf("") }
    var costInput by remember { mutableStateOf("50") }
    var categoryInput by remember { mutableStateOf("Leisure") }

    KintsuBottomSheet(
        onDismissRequest = onDismiss,
        title = "Personal Rewards",
        scrollable = false,
        trailingAction = {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.accent)
                    .clickable { showCreateDialog = true }
                    .padding(horizontal = spacing.s, vertical = spacing.xs)
            ) {
                Text(
                    text = "+ New Reward",
                    style = typography.caption.copy(color = colors.onAccent, fontWeight = FontWeight.Bold)
                )
            }
        }
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(spacing.m)
        ) {
            item {
                Text(
                    text = "Spend your earned gold on meaningful real-world rewards and creative breaks that motivate your progress.",
                    style = typography.secondary,
                    color = colors.textSecondary
                )
            }

            if (rewards.isEmpty()) {
                item {
                    KintsuCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = colors.surfaceElevated
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(spacing.l),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No personal rewards yet",
                                style = typography.itemTitle,
                                color = colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(spacing.xs))
                            Text(
                                text = "Create rewards like 'Watch an episode', 'Enjoy coffee at a café', or 'Creative afternoon break' to celebrate milestones.",
                                style = typography.secondary,
                                color = colors.textSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(spacing.m))
                            KintsuButton(
                                text = "Create Your First Reward",
                                onClick = { showCreateDialog = true },
                                variant = KintsuButtonVariant.PRIMARY
                            )
                        }
                    }
                }
            } else {
                items(rewards, key = { it.id }) { reward ->
                    val canAfford = coinsBalance >= reward.costCoins

                    KintsuCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = colors.surfaceElevated
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(spacing.m),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = reward.title,
                                        style = typography.itemTitle.copy(fontSize = 14.sp),
                                        color = colors.textPrimary
                                    )
                                    Spacer(modifier = Modifier.width(spacing.xs))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(colors.accentSoft)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = reward.category,
                                            style = typography.caption.copy(color = colors.accent, fontSize = 9.sp)
                                        )
                                    }
                                }
                                if (reward.description.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = reward.description,
                                        style = typography.secondary.copy(fontSize = 12.sp),
                                        color = colors.textSecondary
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconGoldSeam(color = colors.goldSeam, size = 12.dp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${reward.costCoins} Gold",
                                        style = typography.caption.copy(color = colors.goldSeam, fontWeight = FontWeight.Bold)
                                    )
                                    if (reward.redemptionsCount > 0) {
                                        Text(
                                            text = " • Redeemed ${reward.redemptionsCount}x",
                                            style = typography.caption.copy(color = colors.textTertiary)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(spacing.s))

                            KintsuButton(
                                text = "Redeem",
                                onClick = { rewardToRedeem = reward },
                                enabled = canAfford,
                                variant = if (canAfford) KintsuButtonVariant.PRIMARY else KintsuButtonVariant.SECONDARY
                            )
                        }
                    }
                }
            }
        }
    }

    // Create Reward Dialog
    if (showCreateDialog) {
        KintsuDialog(
            title = "New Personal Reward",
            onDismissRequest = { showCreateDialog = false },
            confirmButton = {
                KintsuButton(
                    text = "Create",
                    onClick = {
                        val cost = costInput.toIntOrNull() ?: 50
                        if (titleInput.isNotBlank()) {
                            onCreateReward(titleInput.trim(), descInput.trim(), cost, categoryInput, "Star")
                            showCreateDialog = false
                            titleInput = ""
                            descInput = ""
                            costInput = "50"
                        }
                    },
                    enabled = titleInput.isNotBlank(),
                    variant = KintsuButtonVariant.PRIMARY
                )
            },
            dismissButton = {
                KintsuButton(
                    text = "Cancel",
                    onClick = { showCreateDialog = false },
                    variant = KintsuButtonVariant.SECONDARY
                )
            }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.s)) {
                OutlinedTextField(
                    value = titleInput,
                    onValueChange = { titleInput = it },
                    label = { Text("Reward Name") },
                    placeholder = { Text("e.g. Watch an episode") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.accent,
                        unfocusedBorderColor = colors.border
                    )
                )

                OutlinedTextField(
                    value = descInput,
                    onValueChange = { descInput = it },
                    label = { Text("Description (Optional)") },
                    placeholder = { Text("e.g. 45 min creative break") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.accent,
                        unfocusedBorderColor = colors.border
                    )
                )

                OutlinedTextField(
                    value = costInput,
                    onValueChange = { costInput = it.filter { char -> char.isDigit() } },
                    label = { Text("Point Cost (Gold)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.accent,
                        unfocusedBorderColor = colors.border
                    )
                )
            }
        }
    }

    // Confirm Redemption Dialog
    rewardToRedeem?.let { reward ->
        KintsuDialog(
            title = "Redeem Reward",
            onDismissRequest = { rewardToRedeem = null },
            confirmButton = {
                KintsuButton(
                    text = "Confirm (${reward.costCoins} Gold)",
                    onClick = {
                        onRedeemReward(reward)
                        rewardToRedeem = null
                    },
                    variant = KintsuButtonVariant.PRIMARY
                )
            },
            dismissButton = {
                KintsuButton(
                    text = "Cancel",
                    onClick = { rewardToRedeem = null },
                    variant = KintsuButtonVariant.SECONDARY
                )
            }
        ) {
            Text(
                text = "Are you ready to enjoy \"${reward.title}\"? This will deduct ${reward.costCoins} Kintsu Gold from your wallet.",
                style = typography.body,
                color = colors.textPrimary
            )
        }
    }
}
