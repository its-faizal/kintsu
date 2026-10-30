package com.kintsu.app.ui.shop

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kintsu.app.data.KintsuSettings
import com.kintsu.app.data.ShopCatalog
import com.kintsu.app.data.model.PersonalRewardEntity
import com.kintsu.app.data.model.ShopItemEntity
import com.kintsu.app.data.model.StylePresetEntity
import com.kintsu.app.data.model.WalletTransactionEntity
import com.kintsu.app.ui.components.IconGoldSeam
import com.kintsu.app.ui.components.IconSearch
import com.kintsu.app.ui.components.KintsuBottomSheet
import com.kintsu.app.ui.components.KintsuButton
import com.kintsu.app.ui.components.KintsuButtonVariant
import com.kintsu.app.ui.components.KintsuCard
import com.kintsu.app.ui.theme.KintsuTheme

enum class ShopSortOption(val label: String) {
    FEATURED("Featured"),
    PRICE_LOW_HIGH("Price: Low to High"),
    PRICE_HIGH_LOW("Price: High to Low"),
    NAME_A_Z("Name: A to Z")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopHomeScreen(
    coinsBalance: Int,
    coinsEarnedTotal: Int,
    currentSettings: KintsuSettings,
    allShopItems: List<ShopItemEntity>,
    personalRewards: List<PersonalRewardEntity>,
    walletTransactions: List<WalletTransactionEntity>,
    stylePresets: List<StylePresetEntity>,
    onPurchaseItem: (ShopItemEntity) -> Unit,
    onEquipItem: (ShopItemEntity) -> Unit,
    onCreatePersonalReward: (title: String, desc: String, cost: Int, category: String, icon: String) -> Unit,
    onRedeemPersonalReward: (PersonalRewardEntity) -> Unit,
    onArchivePersonalReward: (PersonalRewardEntity) -> Unit,
    onSaveStylePreset: (name: String, theme: String, accent: String, icon: String, skin: String, face: String) -> Unit,
    onApplyStylePreset: (StylePresetEntity) -> Unit,
    onDeleteStylePreset: (StylePresetEntity) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography

    // Active Category & Search / Filters State
    var selectedCategory by remember { mutableStateOf("Discover") }
    var searchQuery by remember { mutableStateOf("") }
    var filterOwnedOnly by remember { mutableStateOf(false) }
    var filterAffordableOnly by remember { mutableStateOf(false) }
    var activeSort by remember { mutableStateOf(ShopSortOption.FEATURED) }

    // Sheets navigation
    var selectedItemForDetail by remember { mutableStateOf<ShopItemEntity?>(null) }
    var showWalletSheet by remember { mutableStateOf(false) }
    var showPersonalRewardsSheet by remember { mutableStateOf(false) }
    var showMyCollectionSheet by remember { mutableStateOf(false) }
    var showPresetsSheet by remember { mutableStateOf(false) }

    // Filter items based on active criteria
    val displayedItems = remember(
        allShopItems,
        selectedCategory,
        searchQuery,
        filterOwnedOnly,
        filterAffordableOnly,
        activeSort,
        coinsBalance
    ) {
        var list = allShopItems

        // 1. Category Filter
        if (selectedCategory != "Discover") {
            list = list.filter { it.category.equals(selectedCategory, ignoreCase = true) }
        }

        // 2. Search Query
        if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim().lowercase()
            list = list.filter { it.title.lowercase().contains(q) || it.description.lowercase().contains(q) }
        }

        // 3. Owned Filter
        if (filterOwnedOnly) {
            list = list.filter { it.isUnlocked }
        }

        // 4. Affordable Filter
        if (filterAffordableOnly) {
            list = list.filter { coinsBalance >= it.costCoins || it.isUnlocked }
        }

        // 5. Sorting
        when (activeSort) {
            ShopSortOption.FEATURED -> list.sortedBy { it.costCoins }
            ShopSortOption.PRICE_LOW_HIGH -> list.sortedBy { it.costCoins }
            ShopSortOption.PRICE_HIGH_LOW -> list.sortedByDescending { it.costCoins }
            ShopSortOption.NAME_A_Z -> list.sortedBy { it.title.lowercase() }
        }
    }

    KintsuBottomSheet(
        onDismissRequest = onDismiss,
        title = "Kintsu Shop",
        scrollable = false,
        trailingAction = {
            // Live Reward Point Balance Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.goldSeam.copy(alpha = 0.15f))
                    .border(1.dp, colors.goldSeam.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .clickable { showWalletSheet = true }
                    .padding(horizontal = spacing.m, vertical = spacing.xs)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconGoldSeam(color = colors.goldSeam, size = 16.dp)
                    Spacer(modifier = Modifier.width(spacing.xs))
                    Text(
                        text = "$coinsBalance Gold",
                        style = typography.itemTitle.copy(color = colors.goldSeam, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(spacing.m)
        ) {
            // Subtitle & Navigation Shortcuts Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Make your journey your own.",
                        style = typography.secondary,
                        color = colors.textSecondary
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.surfaceElevated)
                                .clickable { showMyCollectionSheet = true }
                                .padding(horizontal = spacing.s, vertical = spacing.xs)
                        ) {
                            Text("Inventory", style = typography.caption.copy(color = colors.textPrimary, fontWeight = FontWeight.SemiBold))
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.surfaceElevated)
                                .clickable { showPresetsSheet = true }
                                .padding(horizontal = spacing.s, vertical = spacing.xs)
                        ) {
                            Text("Presets", style = typography.caption.copy(color = colors.textPrimary, fontWeight = FontWeight.SemiBold))
                        }
                    }
                }
            }

            // Featured Curated Collection Card (Only on "Discover" tab)
            if (selectedCategory == "Discover" && searchQuery.isBlank()) {
                item {
                    val featuredBundle = allShopItems.find { it.id == "bundle_matcha" }
                    if (featuredBundle != null) {
                        KintsuCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedItemForDetail = featuredBundle },
                            backgroundColor = colors.accentSoft.copy(alpha = 0.25f),
                            borderColor = colors.goldSeam
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
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(colors.goldSeam)
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "FEATURED COLLECTION",
                                                style = typography.caption.copy(color = colors.onAccent, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(spacing.xs))
                                        Text(
                                            text = "5 Included Items",
                                            style = typography.caption.copy(color = colors.textSecondary)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(spacing.xs))
                                    Text(
                                        text = featuredBundle.title,
                                        style = typography.itemTitle.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                                        color = colors.textPrimary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Japanese wabi-sabi matcha aesthetic: theme, app icon, widget skin, and zen scene.",
                                        style = typography.secondary.copy(fontSize = 12.sp),
                                        color = colors.textSecondary,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Spacer(modifier = Modifier.width(spacing.m))

                                Column(horizontalAlignment = Alignment.End) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconGoldSeam(color = colors.goldSeam, size = 14.dp)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${featuredBundle.costCoins}",
                                            style = typography.itemTitle.copy(color = colors.goldSeam, fontSize = 16.sp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(spacing.xs))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(colors.accent)
                                            .padding(horizontal = spacing.s, vertical = spacing.xs)
                                    ) {
                                        Text("Inspect", style = typography.caption.copy(color = colors.onAccent, fontWeight = FontWeight.Bold))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Horizontal Categories Navigation Bar
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(spacing.xs)
                ) {
                    ShopCatalog.CATEGORIES.forEach { cat ->
                        val isSelected = selectedCategory == cat
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) colors.accent else colors.surfaceElevated)
                                .border(
                                    1.dp,
                                    if (isSelected) colors.accent else colors.border,
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable { selectedCategory = cat }
                                .padding(horizontal = spacing.m, vertical = spacing.xs)
                        ) {
                            Text(
                                text = cat,
                                style = typography.caption.copy(
                                    color = if (isSelected) colors.onAccent else colors.textPrimary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        }
                    }

                    // Personal Rewards Tab Chip
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(colors.surfaceElevated)
                            .border(1.dp, colors.goldSeam.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                            .clickable { showPersonalRewardsSheet = true }
                            .padding(horizontal = spacing.m, vertical = spacing.xs)
                    ) {
                        Text(
                            text = "★ Personal Rewards",
                            style = typography.caption.copy(color = colors.goldSeam, fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            // Search Bar & Filter Chips Row
            item {
                Column(verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search catalogue items...") },
                        leadingIcon = { IconSearch(color = colors.textSecondary, size = 18.dp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.accent,
                            unfocusedBorderColor = colors.border,
                            focusedContainerColor = colors.surfaceElevated,
                            unfocusedContainerColor = colors.surfaceElevated
                        )
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(spacing.xs)
                    ) {
                        // Filter Owned
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (filterOwnedOnly) colors.accentSoft else colors.surfaceElevated)
                                .border(1.dp, if (filterOwnedOnly) colors.accent else colors.border, RoundedCornerShape(12.dp))
                                .clickable { filterOwnedOnly = !filterOwnedOnly }
                                .padding(horizontal = spacing.s, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (filterOwnedOnly) "Owned Only ✓" else "Owned",
                                style = typography.caption.copy(color = colors.textPrimary)
                            )
                        }

                        // Filter Affordable
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (filterAffordableOnly) colors.accentSoft else colors.surfaceElevated)
                                .border(1.dp, if (filterAffordableOnly) colors.accent else colors.border, RoundedCornerShape(12.dp))
                                .clickable { filterAffordableOnly = !filterAffordableOnly }
                                .padding(horizontal = spacing.s, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (filterAffordableOnly) "Affordable (≤ $coinsBalance) ✓" else "Affordable",
                                style = typography.caption.copy(color = colors.textPrimary)
                            )
                        }

                        // Sort Toggle
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.surfaceElevated)
                                .border(1.dp, colors.border, RoundedCornerShape(12.dp))
                                .clickable {
                                    activeSort = when (activeSort) {
                                        ShopSortOption.FEATURED -> ShopSortOption.PRICE_LOW_HIGH
                                        ShopSortOption.PRICE_LOW_HIGH -> ShopSortOption.PRICE_HIGH_LOW
                                        ShopSortOption.PRICE_HIGH_LOW -> ShopSortOption.NAME_A_Z
                                        ShopSortOption.NAME_A_Z -> ShopSortOption.FEATURED
                                    }
                                }
                                .padding(horizontal = spacing.s, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Sort: ${activeSort.label}",
                                style = typography.caption.copy(color = colors.textSecondary)
                            )
                        }
                    }
                }
            }

            // Items Count Status
            item {
                Text(
                    text = "${displayedItems.size} items available",
                    style = typography.caption,
                    color = colors.textSecondary
                )
            }

            // Responsive Items Grid / List
            if (displayedItems.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = spacing.xl),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No matching items found.\nTry relaxing your search query or filters.",
                            style = typography.secondary,
                            color = colors.textSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                items(displayedItems, key = { it.id }) { item ->
                    val canAfford = coinsBalance >= item.costCoins || item.isUnlocked

                    KintsuCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedItemForDetail = item },
                        backgroundColor = if (item.isEquipped) colors.accentSoft.copy(alpha = 0.25f) else colors.surfaceElevated,
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
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.width(spacing.m))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = item.title,
                                            style = typography.itemTitle.copy(fontSize = 14.sp),
                                            color = colors.textPrimary
                                        )
                                        if (item.tag.isNotBlank()) {
                                            Spacer(modifier = Modifier.width(spacing.xs))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(colors.goldSeam.copy(alpha = 0.15f))
                                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                                            ) {
                                                Text(
                                                    text = item.tag,
                                                    style = typography.caption.copy(color = colors.goldSeam, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = item.category,
                                        style = typography.caption.copy(fontSize = 11.sp),
                                        color = colors.textSecondary
                                    )
                                    if (item.description.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = item.description,
                                            style = typography.secondary.copy(fontSize = 11.sp),
                                            color = colors.textSecondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(spacing.s))

                            // Price / State Pill
                            Column(horizontalAlignment = Alignment.End) {
                                when {
                                    item.isEquipped -> {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(colors.accent.copy(alpha = 0.15f))
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text("Equipped", color = colors.accent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    item.isUnlocked -> {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(colors.surfaceElevated)
                                                .border(1.dp, colors.border, RoundedCornerShape(8.dp))
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text("Owned", color = colors.textPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                    else -> {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(colors.goldSeam.copy(alpha = 0.12f))
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            IconGoldSeam(color = colors.goldSeam, size = 12.dp)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (item.costCoins == 0) "Free" else "${item.costCoins}",
                                                color = colors.goldSeam,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
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

    // Modal Item Detail & Preview Sheet
    selectedItemForDetail?.let { item ->
        ItemDetailPreviewSheet(
            item = item,
            coinsBalance = coinsBalance,
            onUnlock = { purchasedItem ->
                onPurchaseItem(purchasedItem)
                selectedItemForDetail = null
            },
            onEquip = { equippedItem ->
                onEquipItem(equippedItem)
                selectedItemForDetail = null
            },
            onDismiss = { selectedItemForDetail = null }
        )
    }

    // Modal Wallet History Sheet
    if (showWalletSheet) {
        WalletHistorySheet(
            coinsBalance = coinsBalance,
            coinsEarnedTotal = coinsEarnedTotal,
            transactions = walletTransactions,
            onDismiss = { showWalletSheet = false }
        )
    }

    // Modal Personal Rewards Sheet
    if (showPersonalRewardsSheet) {
        PersonalRewardsSheet(
            coinsBalance = coinsBalance,
            rewards = personalRewards,
            onCreateReward = onCreatePersonalReward,
            onRedeemReward = onRedeemPersonalReward,
            onArchiveReward = onArchivePersonalReward,
            onDismiss = { showPersonalRewardsSheet = false }
        )
    }

    // Modal My Collection Sheet
    if (showMyCollectionSheet) {
        MyCollectionSheet(
            ownedItems = allShopItems,
            onEquip = onEquipItem,
            onPreview = { itemToPreview ->
                showMyCollectionSheet = false
                selectedItemForDetail = itemToPreview
            },
            onDismiss = { showMyCollectionSheet = false }
        )
    }

    // Modal Style Presets Sheet
    if (showPresetsSheet) {
        StylePresetsSheet(
            presets = stylePresets,
            currentSettings = currentSettings,
            onSavePreset = onSaveStylePreset,
            onApplyPreset = onApplyStylePreset,
            onDeletePreset = onDeleteStylePreset,
            onDismiss = { showPresetsSheet = false }
        )
    }
}
