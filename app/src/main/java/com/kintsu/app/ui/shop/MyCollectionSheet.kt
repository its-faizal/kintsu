package com.kintsu.app.ui.shop

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kintsu.app.data.model.ShopItemEntity
import com.kintsu.app.ui.components.KintsuBottomSheet
import com.kintsu.app.ui.components.KintsuButton
import com.kintsu.app.ui.components.KintsuButtonVariant
import com.kintsu.app.ui.components.KintsuCard
import com.kintsu.app.ui.theme.KintsuTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyCollectionSheet(
    ownedItems: List<ShopItemEntity>,
    onEquip: (ShopItemEntity) -> Unit,
    onPreview: (ShopItemEntity) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography

    val slots = listOf("All", "Equipped", "Themes", "App Icons", "Widgets", "Focus", "Habits", "Profile", "Sounds", "Gold Repair")
    var selectedSlot by remember { mutableStateOf("All") }

    val filteredItems = remember(ownedItems, selectedSlot) {
        when (selectedSlot) {
            "All" -> ownedItems.filter { it.isUnlocked }
            "Equipped" -> ownedItems.filter { it.isUnlocked && it.isEquipped }
            else -> ownedItems.filter { it.isUnlocked && it.category.equals(selectedSlot, ignoreCase = true) }
        }
    }

    KintsuBottomSheet(
        onDismissRequest = onDismiss,
        title = "My Collection",
        scrollable = false,
        trailingAction = {
            Text(
                text = "${ownedItems.count { it.isUnlocked }} Owned",
                style = typography.caption.copy(color = colors.textSecondary, fontWeight = FontWeight.SemiBold)
            )
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(spacing.m)
        ) {
            // Horizontal Slot Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(spacing.xs)
            ) {
                slots.forEach { slot ->
                    val isSelected = selectedSlot == slot
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) colors.accent else colors.surfaceElevated)
                            .border(
                                1.dp,
                                if (isSelected) colors.accent else colors.border,
                                RoundedCornerShape(16.dp)
                            )
                            .clickable { selectedSlot = slot }
                            .padding(horizontal = spacing.m, vertical = spacing.xs)
                    ) {
                        Text(
                            text = slot,
                            style = typography.caption.copy(
                                color = if (isSelected) colors.onAccent else colors.textPrimary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        )
                    }
                }
            }

            // Items List
            if (filteredItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = spacing.xl),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No items in '$selectedSlot' unlocked yet.\nExplore the Shop catalogue to unlock cosmetics.",
                        style = typography.secondary,
                        color = colors.textSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(spacing.s)
                ) {
                    items(filteredItems, key = { it.id }) { item ->
                        KintsuCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = if (item.isEquipped) colors.accentSoft.copy(alpha = 0.3f) else colors.surfaceElevated,
                            borderColor = if (item.isEquipped) colors.accent else colors.border
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(spacing.m),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    ItemPreviewThumbnail(
                                        item = item,
                                        modifier = Modifier.size(44.dp)
                                    )
                                    Spacer(modifier = Modifier.width(spacing.m))
                                    Column {
                                        Text(
                                            text = item.title,
                                            style = typography.itemTitle.copy(fontSize = 14.sp),
                                            color = colors.textPrimary
                                        )
                                        Text(
                                            text = item.category,
                                            style = typography.caption.copy(fontSize = 11.sp),
                                            color = colors.textSecondary
                                        )
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
                                    KintsuButton(
                                        text = "Preview",
                                        onClick = { onPreview(item) },
                                        variant = KintsuButtonVariant.SECONDARY
                                    )

                                    if (item.isEquipped) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(colors.accent.copy(alpha = 0.15f))
                                                .padding(horizontal = 10.dp, vertical = 8.dp)
                                        ) {
                                            Text("Active", color = colors.accent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    } else {
                                        KintsuButton(
                                            text = "Equip",
                                            onClick = { onEquip(item) },
                                            variant = KintsuButtonVariant.PRIMARY
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
