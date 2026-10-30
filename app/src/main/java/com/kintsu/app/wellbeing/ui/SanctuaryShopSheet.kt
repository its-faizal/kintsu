package com.kintsu.app.wellbeing.ui

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kintsu.app.data.model.ShopItemEntity
import com.kintsu.app.ui.components.IconCheck
import com.kintsu.app.ui.components.IconGoldSeam
import com.kintsu.app.ui.components.KintsuBottomSheet
import com.kintsu.app.ui.components.KintsuButton
import com.kintsu.app.ui.components.KintsuButtonVariant
import com.kintsu.app.ui.components.KintsuCard
import com.kintsu.app.ui.theme.KintsuTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SanctuaryShopSheet(
    coinsBalance: Int,
    shopItems: List<ShopItemEntity>,
    onPurchaseItem: (ShopItemEntity) -> Unit,
    onEquipItem: (ShopItemEntity) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography

    KintsuBottomSheet(
        onDismissRequest = onDismiss,
        title = "Sanctuary Shop",
        scrollable = false,
        trailingAction = {
            // Balance Badge in header
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.goldSeam.copy(alpha = 0.15f))
                    .border(1.dp, colors.goldSeam.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .padding(horizontal = spacing.m, vertical = spacing.xs)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconGoldSeam(color = colors.goldSeam, size = 16.dp)
                    Spacer(modifier = Modifier.width(spacing.xs))
                    Text(
                        text = "$coinsBalance",
                        style = typography.itemTitle.copy(color = colors.goldSeam, fontSize = 16.sp)
                    )
                }
            }
        }
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(spacing.m),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                Text(
                    text = "Spend mindful coins earned from rituals & focus",
                    style = typography.secondary,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(bottom = spacing.xs)
                )
            }

            items(shopItems, key = { it.id }) { item ->
                val canAfford = coinsBalance >= item.costCoins

                KintsuCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = if (item.isEquipped) colors.accentSoft.copy(alpha = 0.35f) else colors.surfaceElevated,
                    borderColor = if (item.isEquipped) colors.goldSeam else colors.border
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(spacing.m),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = item.title,
                                    style = typography.itemTitle,
                                    color = colors.textPrimary
                                )
                                if (item.isEquipped) {
                                    Spacer(modifier = Modifier.width(spacing.xs))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(colors.goldSeam.copy(alpha = 0.2f))
                                            .padding(horizontal = spacing.xs, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "Equipped",
                                            style = typography.caption.copy(fontSize = 10.sp, color = colors.goldSeam)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(spacing.xxs))
                            Text(
                                text = item.description,
                                style = typography.caption,
                                color = colors.textSecondary
                            )
                        }

                        Spacer(modifier = Modifier.width(spacing.m))

                        when {
                            item.isEquipped -> {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(colors.goldSeam.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    IconCheck(color = colors.goldSeam, size = 16.dp)
                                }
                            }
                            item.isUnlocked -> {
                                KintsuButton(
                                    text = "Equip",
                                    onClick = { onEquipItem(item) },
                                    variant = KintsuButtonVariant.ACCENT_SOFT
                                )
                            }
                            else -> {
                                KintsuButton(
                                    text = "${item.costCoins} 🪙",
                                    onClick = { onPurchaseItem(item) },
                                    enabled = canAfford,
                                    variant = if (canAfford) KintsuButtonVariant.PRIMARY else KintsuButtonVariant.SECONDARY
                                )
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Earn +5 coins per completed ritual, +3 per task, +10 per focus session, and +15 per digital detox.",
                    style = typography.caption.copy(fontSize = 11.sp),
                    color = colors.textTertiary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = spacing.m, bottom = spacing.xl)
                )
            }
        }
    }
}
