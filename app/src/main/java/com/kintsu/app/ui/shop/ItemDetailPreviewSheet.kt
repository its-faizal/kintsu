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
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kintsu.app.data.model.ShopItemEntity
import com.kintsu.app.ui.components.IconCheck
import com.kintsu.app.ui.components.IconGoldSeam
import com.kintsu.app.ui.components.KintsuBottomSheet
import com.kintsu.app.ui.components.KintsuButton
import com.kintsu.app.ui.components.KintsuButtonVariant
import com.kintsu.app.ui.components.KintsuCard
import com.kintsu.app.ui.sound.SoundHapticHelper
import com.kintsu.app.ui.theme.KintsuTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailPreviewSheet(
    item: ShopItemEntity,
    coinsBalance: Int,
    streakDays: Int = 0,
    onUnlock: (ShopItemEntity) -> Unit,
    onEquip: (ShopItemEntity) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography
    val context = LocalContext.current

    var isPreviewActive by remember { mutableStateOf(false) }
    var isPurchasing by remember { mutableStateOf(false) }

    val isStreakEligible = streakDays >= item.requiredStreakDays
    val canAfford = (coinsBalance >= item.costCoins || item.isUnlocked) && isStreakEligible

    KintsuBottomSheet(
        onDismissRequest = onDismiss,
        title = item.title,
        scrollable = true,
        trailingAction = {
            if (item.collectionName.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.accentSoft)
                        .padding(horizontal = spacing.s, vertical = spacing.xs)
                ) {
                    Text(
                        text = item.collectionName,
                        style = typography.caption.copy(color = colors.accent, fontWeight = FontWeight.SemiBold)
                    )
                }
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = spacing.l),
            verticalArrangement = Arrangement.spacedBy(spacing.m)
        ) {
            // Category & Tag Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(colors.surfaceElevated)
                            .padding(horizontal = spacing.s, vertical = spacing.xxs)
                    ) {
                        Text(
                            text = item.category.uppercase(),
                            style = typography.caption.copy(color = colors.textSecondary, fontSize = 10.sp)
                        )
                    }

                    if (item.tag.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(colors.goldSeam.copy(alpha = 0.15f))
                                .border(0.5.dp, colors.goldSeam, RoundedCornerShape(6.dp))
                                .padding(horizontal = spacing.s, vertical = spacing.xxs)
                        ) {
                            Text(
                                text = item.tag,
                                style = typography.caption.copy(color = colors.goldSeam, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }

                // Ownership Status Badge
                when {
                    item.isEquipped -> {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.accent.copy(alpha = 0.15f))
                                .border(1.dp, colors.accent, RoundedCornerShape(12.dp))
                                .padding(horizontal = spacing.s, vertical = spacing.xxs)
                        ) {
                            Text("EQUIPPED", color = colors.accent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    item.isUnlocked -> {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.surfaceElevated)
                                .padding(horizontal = spacing.s, vertical = spacing.xxs)
                        ) {
                            Text("OWNED", color = colors.textSecondary, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    else -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconGoldSeam(color = colors.goldSeam, size = 14.dp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (item.costCoins == 0) "Free" else "${item.costCoins} Gold",
                                color = colors.goldSeam,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Hero Interactive Preview Card
            LargeItemInteractivePreview(item = item)

            // Audio Sample Trigger if Soundscape
            if (item.itemType == "SOUNDSCAPE") {
                KintsuButton(
                    text = "▶ Listen to Sound Preview",
                    onClick = {
                        SoundHapticHelper.playSoundById(context, item.id, enabled = true)
                    },
                    variant = KintsuButtonVariant.SECONDARY,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Description
            Text(
                text = item.description,
                style = typography.body,
                color = colors.textPrimary,
                lineHeight = 22.sp
            )

            // Streak Rank Barrier Check
            if (!item.isUnlocked && !isStreakEligible) {
                KintsuCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = colors.surfaceElevated,
                    borderColor = colors.goldSeam.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(spacing.m),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        IconGoldSeam(color = colors.goldSeam, size = 20.dp)
                        Column {
                            Text(
                                text = "Requires ${item.requiredStreakDays}-Day Fire Streak",
                                style = typography.itemTitle.copy(fontSize = 14.sp),
                                color = colors.goldSeam
                            )
                            Text(
                                text = "Current fire streak: $streakDays day${if (streakDays == 1) "" else "s"}. Maintain unbroken daily discipline to access this milestone rank.",
                                style = typography.secondary.copy(fontSize = 12.sp),
                                color = colors.textSecondary
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(spacing.s))
            }

            // Balance Status & Affordability Check
            if (!item.isUnlocked && item.costCoins > 0 && isStreakEligible) {
                KintsuCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = if (canAfford) colors.surfaceElevated else colors.errorContainer.copy(alpha = 0.25f),
                    borderColor = if (canAfford) colors.border else colors.error.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(spacing.m),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (canAfford) "Wallet Balance Sufficient" else "More Gold Needed",
                                style = typography.itemTitle.copy(fontSize = 14.sp),
                                color = if (canAfford) colors.textPrimary else colors.error
                            )
                            Text(
                                text = if (canAfford) "You have $coinsBalance Kintsu Gold" else "Need ${item.costCoins - coinsBalance} more Gold. Complete rituals or focus to earn.",
                                style = typography.secondary.copy(fontSize = 12.sp),
                                color = colors.textSecondary
                            )
                        }

                        IconGoldSeam(color = if (canAfford) colors.goldSeam else colors.error, size = 20.dp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(spacing.s))

            // Primary Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.s)
            ) {
                // Try / Preview Mode Button
                KintsuButton(
                    text = if (isPreviewActive) "Active Preview" else "Try Preview",
                    onClick = {
                        isPreviewActive = !isPreviewActive
                        SoundHapticHelper.performHaptic(context, SoundHapticHelper.HapticPreset.SUBTLE)
                    },
                    variant = if (isPreviewActive) KintsuButtonVariant.PRIMARY else KintsuButtonVariant.SECONDARY,
                    modifier = Modifier.weight(1f)
                )

                // Unlock or Equip Button
                when {
                    item.isEquipped -> {
                        KintsuButton(
                            text = "Equipped ✓",
                            onClick = { /* Already equipped */ },
                            variant = KintsuButtonVariant.SECONDARY,
                            modifier = Modifier.weight(1.3f)
                        )
                    }
                    item.isUnlocked -> {
                        KintsuButton(
                            text = "Equip Now",
                            onClick = {
                                onEquip(item)
                                SoundHapticHelper.performHaptic(context, SoundHapticHelper.HapticPreset.STANDARD)
                                onDismiss()
                            },
                            variant = KintsuButtonVariant.PRIMARY,
                            modifier = Modifier.weight(1.3f)
                        )
                    }
                    !isStreakEligible -> {
                        KintsuButton(
                            text = "Locked (${item.requiredStreakDays}d Streak)",
                            onClick = { /* Locked behind streak */ },
                            enabled = false,
                            variant = KintsuButtonVariant.SECONDARY,
                            modifier = Modifier.weight(1.3f)
                        )
                    }
                    else -> {
                        KintsuButton(
                            text = if (canAfford) "Unlock for ${item.costCoins} Gold" else "Insufficient Gold",
                            onClick = {
                                if (canAfford && !isPurchasing) {
                                    isPurchasing = true
                                    onUnlock(item)
                                    SoundHapticHelper.playSound(context, SoundHapticHelper.SoundEffect.QUIET_SPARKLE)
                                    SoundHapticHelper.performHaptic(context, SoundHapticHelper.HapticPreset.STANDARD)
                                    onDismiss()
                                }
                            },
                            enabled = canAfford && !isPurchasing,
                            variant = KintsuButtonVariant.PRIMARY,
                            modifier = Modifier.weight(1.3f)
                        )
                    }
                }
            }
        }
    }
}
