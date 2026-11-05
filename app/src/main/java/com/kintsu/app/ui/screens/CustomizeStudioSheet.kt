package com.kintsu.app.ui.screens

import android.content.Context
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kintsu.app.data.ContentDensity
import com.kintsu.app.data.KintsuSettings
import com.kintsu.app.data.model.StylePresetEntity
import androidx.compose.runtime.collectAsState
import com.kintsu.app.data.ItemPreviewManager
import com.kintsu.app.data.model.ShopItemEntity
import com.kintsu.app.ui.components.IconLock
import com.kintsu.app.ui.components.IconCheck
import com.kintsu.app.ui.components.IconClose
import com.kintsu.app.ui.components.KintsuBottomSheet
import com.kintsu.app.ui.components.KintsuButton
import com.kintsu.app.ui.components.KintsuButtonVariant
import com.kintsu.app.ui.components.KintsuCard
import com.kintsu.app.ui.components.KintsuDialog
import com.kintsu.app.ui.components.KintsuFilterChip
import com.kintsu.app.ui.theme.AccentOption
import com.kintsu.app.ui.theme.AppIconManager
import com.kintsu.app.ui.theme.AppTheme
import com.kintsu.app.ui.theme.KintsuTheme
import com.kintsu.app.ui.theme.LauncherIconInfo
import com.kintsu.app.ui.theme.ThemeMode

private enum class CustomizeTab(val label: String) {
    PACKS("Packs"),
    THEMES("Themes"),
    ICONS("Icons"),
    APPEARANCE("Colors"),
    MOTION("Motion")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomizeStudioSheet(
    currentSettings: KintsuSettings,
    stylePresets: List<StylePresetEntity>,
    onApplyPreset: (StylePresetEntity) -> Unit,
    onSavePreset: (name: String, theme: String, accent: String, icon: String, skin: String, face: String) -> Unit,
    onDeletePreset: (StylePresetEntity) -> Unit,
    onRestoreDefaultPresets: () -> Unit,
    onUpdateAppTheme: (AppTheme) -> Unit,
    onUpdateAccent: (AccentOption) -> Unit,
    onUpdateThemeMode: (ThemeMode) -> Unit,
    onUpdateContentDensity: (ContentDensity) -> Unit,
    onUpdateLauncherIcon: (String) -> Unit,
    onUpdateHapticsEnabled: (Boolean) -> Unit,
    onUpdateReducedMotion: (Boolean) -> Unit,
    allShopItems: List<ShopItemEntity> = emptyList(),
    onOpenShop: () -> Unit = {},
    onDismiss: () -> Unit
) {
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography
    val context = LocalContext.current

    var selectedTab by remember { mutableStateOf(CustomizeTab.PACKS) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var newPresetName by remember { mutableStateOf("") }

    KintsuBottomSheet(
        onDismissRequest = onDismiss,
        title = "Customize",
        scrollable = false,
        showCloseButton = false
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = spacing.s)
        ) {
            // Header Description
            Text(
                text = "Instant themes, curated packs, app icons, and fluid motion. Unlock in Shop or test with 10-minute preview.",
                style = typography.caption,
                color = colors.textSecondary,
                modifier = Modifier.padding(bottom = spacing.m)
            )

            // Segmented Tab Bar (Apple Pill Style)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(bottom = spacing.m),
                horizontalArrangement = Arrangement.spacedBy(spacing.xs)
            ) {
                CustomizeTab.values().forEach { tab ->
                    val isSelected = selectedTab == tab
                    val bg by animateColorAsState(
                        targetValue = if (isSelected) colors.accent else colors.surfaceElevated,
                        animationSpec = spring(stiffness = 300f),
                        label = "tabBg"
                    )
                    val textColor by animateColorAsState(
                        targetValue = if (isSelected) colors.background else colors.textSecondary,
                        animationSpec = spring(stiffness = 300f),
                        label = "tabTextColor"
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(bg)
                            .clickable { selectedTab = tab }
                            .padding(horizontal = spacing.m, vertical = spacing.xs)
                            .semantics {
                                role = Role.Tab
                                contentDescription = "${tab.label} tab"
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab.label,
                            style = typography.caption.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp
                            ),
                            color = textColor
                        )
                    }
                }
            }

            // Tab Content
            Box(modifier = Modifier.weight(1f)) {
                Crossfade(
                    targetState = selectedTab,
                    label = "tabContent"
                ) { tab ->
                    when (tab) {
                        CustomizeTab.PACKS -> {
                            PacksTabContent(
                                stylePresets = stylePresets,
                                currentSettings = currentSettings,
                                onApplyPreset = onApplyPreset,
                                onSaveClick = { showSaveDialog = true },
                                onDeletePreset = onDeletePreset,
                                onRestoreDefaults = onRestoreDefaultPresets,
                                allShopItems = allShopItems,
                                onOpenShop = onOpenShop,
                                onPreviewPack = { preset ->
                                    val themeObj = AppTheme.values().find {
                                        it.displayName.equals(preset.themeName, ignoreCase = true) || it.name.equals(preset.themeName, ignoreCase = true)
                                    } ?: AppTheme.SAGE_IVORY
                                    ItemPreviewManager.startPackPreview(
                                        context = context,
                                        packId = "pack_${preset.name.lowercase().replace(' ', '_')}",
                                        themeObj = themeObj,
                                        iconId = preset.iconId,
                                        currentThemeName = currentSettings.appTheme.name,
                                        currentIconId = currentSettings.launcherIconId,
                                        onApply = { t, ic ->
                                            onUpdateAppTheme(t)
                                            onUpdateLauncherIcon(ic)
                                        }
                                    )
                                }
                            )
                        }
                        CustomizeTab.THEMES -> {
                            ThemesTabContent(
                                currentSettings = currentSettings,
                                onUpdateAppTheme = onUpdateAppTheme,
                                allShopItems = allShopItems,
                                onOpenShop = onOpenShop
                            )
                        }
                        CustomizeTab.ICONS -> {
                            IconsTabContent(
                                currentIconId = currentSettings.launcherIconId,
                                onSelectIcon = onUpdateLauncherIcon,
                                allShopItems = allShopItems,
                                onOpenShop = onOpenShop
                            )
                        }
                        CustomizeTab.APPEARANCE -> {
                            AppearanceTabContent(
                                currentSettings = currentSettings,
                                onUpdateThemeMode = onUpdateThemeMode,
                                onUpdateAccent = onUpdateAccent,
                                onUpdateContentDensity = onUpdateContentDensity
                            )
                        }
                        CustomizeTab.MOTION -> {
                            MotionTabContent(
                                currentSettings = currentSettings,
                                onUpdateHapticsEnabled = onUpdateHapticsEnabled,
                                onUpdateReducedMotion = onUpdateReducedMotion
                            )
                        }
                    }
                }
            }
        }
    }

    // Save Preset Dialog
    if (showSaveDialog) {
        KintsuDialog(
            title = "Save Appearance Look",
            onDismissRequest = { showSaveDialog = false },
            confirmButton = {
                KintsuButton(
                    text = "Save",
                    onClick = {
                        if (newPresetName.isNotBlank()) {
                            onSavePreset(
                                newPresetName.trim(),
                                currentSettings.appTheme.displayName,
                                currentSettings.accentOption.name,
                                currentSettings.launcherIconId,
                                currentSettings.widgetSkinId,
                                currentSettings.focusTimerFaceId
                            )
                            newPresetName = ""
                            showSaveDialog = false
                        }
                    },
                    enabled = newPresetName.isNotBlank(),
                    variant = KintsuButtonVariant.PRIMARY
                )
            },
            dismissButton = {
                KintsuButton(
                    text = "Cancel",
                    onClick = { showSaveDialog = false },
                    variant = KintsuButtonVariant.SECONDARY
                )
            }
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(spacing.s)
            ) {
                Text(
                    text = "Save your current theme, accent, and icon as a reusable style preset.",
                    style = typography.caption,
                    color = colors.textSecondary
                )
                OutlinedTextField(
                    value = newPresetName,
                    onValueChange = { newPresetName = it },
                    label = { Text("Style Preset Name") },
                    placeholder = { Text("e.g. My Studio Calm") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.accent,
                        unfocusedBorderColor = colors.border,
                        focusedLabelColor = colors.accent
                    )
                )
            }
        }
    }
}

@Composable
private fun PacksTabContent(
    stylePresets: List<StylePresetEntity>,
    currentSettings: KintsuSettings,
    onApplyPreset: (StylePresetEntity) -> Unit,
    onSaveClick: () -> Unit,
    onDeletePreset: (StylePresetEntity) -> Unit,
    onRestoreDefaults: () -> Unit,
    allShopItems: List<ShopItemEntity>,
    onOpenShop: () -> Unit,
    onPreviewPack: (StylePresetEntity) -> Unit
) {
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(spacing.s)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CURATED STYLE PACKS",
                    style = typography.caption,
                    color = colors.textTertiary
                )
                Row(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
                    Text(
                        text = "Reset Curated",
                        style = typography.caption.copy(color = colors.accent, fontWeight = FontWeight.SemiBold),
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { onRestoreDefaults() }
                            .padding(horizontal = spacing.xs, vertical = 2.dp)
                    )
                    Text(
                        text = "+ Save Look",
                        style = typography.caption.copy(color = colors.accent, fontWeight = FontWeight.SemiBold),
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { onSaveClick() }
                            .padding(horizontal = spacing.xs, vertical = 2.dp)
                    )
                }
            }
        }

        if (stylePresets.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = spacing.xl),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(spacing.s)
                    ) {
                        Text(
                            text = "No style presets available.",
                            style = typography.itemTitle,
                            color = colors.textPrimary
                        )
                        KintsuButton(
                            text = "Load Curated Packs",
                            onClick = onRestoreDefaults,
                            variant = KintsuButtonVariant.PRIMARY
                        )
                    }
                }
            }
        } else {
            items(stylePresets, key = { it.id }) { preset ->
                val isCurrent = currentSettings.appTheme.displayName.equals(preset.themeName, ignoreCase = true) &&
                        currentSettings.accentOption.name.equals(preset.accentName, ignoreCase = true)

                // Pick representative colors for preview
                val themeObj = AppTheme.values().find {
                    it.displayName.equals(preset.themeName, ignoreCase = true) || it.name.equals(preset.themeName, ignoreCase = true)
                } ?: AppTheme.SAGE_IVORY

                KintsuCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onApplyPreset(preset) },
                    backgroundColor = if (isCurrent) colors.surfaceElevated else colors.surface,
                    borderColor = if (isCurrent) colors.accent else colors.border
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
                                    text = preset.name,
                                    style = typography.itemTitle,
                                    color = if (isCurrent) colors.accent else colors.textPrimary
                                )
                                if (isCurrent) {
                                    Spacer(modifier = Modifier.width(spacing.xs))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(colors.accent.copy(alpha = 0.15f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "Active",
                                            style = typography.caption.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                            color = colors.accent
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${preset.themeName} • Accent: ${preset.accentName}",
                                style = typography.caption,
                                color = colors.textSecondary
                            )
                        }

                        // Palette Preview Circles
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            themeObj.previewPalette.take(3).forEach { swatch ->
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(swatch)
                                        .border(0.5.dp, colors.border.copy(alpha = 0.5f), CircleShape)
                                )
                            }
                            Spacer(modifier = Modifier.width(spacing.xs))
                            if (isCurrent) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(colors.accent),
                                    contentAlignment = Alignment.Center
                                ) {
                                    IconCheck(color = colors.background, size = 14.dp)
                                }
                            } else {
                                Text(
                                    text = "Equip",
                                    style = typography.caption.copy(fontWeight = FontWeight.SemiBold),
                                    color = colors.accent,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(colors.accentSoft.copy(alpha = 0.4f))
                                        .padding(horizontal = spacing.s, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemesTabContent(
    currentSettings: KintsuSettings,
    onUpdateAppTheme: (AppTheme) -> Unit,
    allShopItems: List<ShopItemEntity>,
    onOpenShop: () -> Unit
) {
    val context = LocalContext.current
    val activePreviewId by ItemPreviewManager.activePreviewId.collectAsState()
    val remainingSeconds by ItemPreviewManager.remainingSeconds.collectAsState()
    var lockedThemeToPreview by remember { mutableStateOf<AppTheme?>(null) }
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(spacing.s)
    ) {
        item {
            Text(
                text = "ATMOSPHERIC THEMES",
                style = typography.caption,
                color = colors.textTertiary
            )
        }

        items(AppTheme.values(), key = { it.id }) { theme ->
            val themeShopId = mapThemeToShopId(theme)
            val shopItem = allShopItems.find { it.id == themeShopId || it.title.equals(theme.displayName, ignoreCase = true) }
            val isUnlocked = (theme == AppTheme.SAGE_IVORY) || (shopItem?.isUnlocked == true)
            val isPreviewActive = ItemPreviewManager.isItemInPreview(themeShopId)
            val isSelected = currentSettings.appTheme == theme

            KintsuCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (isUnlocked) {
                            onUpdateAppTheme(theme)
                        } else {
                            lockedThemeToPreview = theme
                        }
                    },
                backgroundColor = if (isSelected) colors.surfaceElevated else colors.surface,
                borderColor = if (isSelected) colors.accent else (if (!isUnlocked) colors.border.copy(alpha = 0.5f) else colors.border)
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
                                text = theme.displayName,
                                style = typography.itemTitle,
                                color = if (isSelected) colors.accent else (if (!isUnlocked) colors.textSecondary else colors.textPrimary)
                            )
                            if (isSelected) {
                                Spacer(modifier = Modifier.width(spacing.xs))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(colors.accent.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Active",
                                        style = typography.caption.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                        color = colors.accent
                                    )
                                }
                            } else if (isPreviewActive) {
                                Spacer(modifier = Modifier.width(spacing.xs))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(colors.goldSeam.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${remainingSeconds / 60}m ${remainingSeconds % 60}s",
                                        style = typography.caption.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                        color = colors.goldSeam
                                    )
                                }
                            } else if (!isUnlocked) {
                                Spacer(modifier = Modifier.width(spacing.xs))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(colors.surfaceElevated)
                                        .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        IconLock(color = colors.textTertiary, size = 10.dp)
                                        Text(
                                            text = "Shop",
                                            style = typography.caption.copy(fontSize = 10.sp),
                                            color = colors.textTertiary
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = theme.subtitle,
                            style = typography.caption,
                            color = colors.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(spacing.s))

                    // Swatches
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        theme.previewPalette.forEach { swatch ->
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(swatch)
                                    .border(0.5.dp, colors.border.copy(alpha = 0.5f), CircleShape)
                            )
                        }
                        Spacer(modifier = Modifier.width(spacing.xs))
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(colors.accent),
                                contentAlignment = Alignment.Center
                            ) {
                                IconCheck(color = colors.background, size = 14.dp)
                            }
                        }
                    }
                }
            }
        }
    }

    // Locked Theme Preview & Shop Dialog
    lockedThemeToPreview?.let { theme ->
        val themeShopId = mapThemeToShopId(theme)
        val shopItem = allShopItems.find { it.id == themeShopId || it.title.equals(theme.displayName, ignoreCase = true) }
        val costCoins = shopItem?.costCoins ?: 250
        val canTrial = ItemPreviewManager.canPreview(context, themeShopId)
        val isPreviewActive = ItemPreviewManager.isItemInPreview(themeShopId)

        KintsuDialog(
            title = theme.displayName,
            onDismissRequest = { lockedThemeToPreview = null },
            confirmButton = {
                KintsuButton(
                    text = "Unlock ($costCoins Coins)",
                    onClick = {
                        lockedThemeToPreview = null
                        onOpenShop()
                    },
                    variant = KintsuButtonVariant.PRIMARY
                )
            },
            dismissButton = {
                if (canTrial && !isPreviewActive) {
                    KintsuButton(
                        text = "10-Min Preview",
                        onClick = {
                            ItemPreviewManager.startThemePreview(
                                context = context,
                                themeId = themeShopId,
                                themeObj = theme,
                                currentThemeName = currentSettings.appTheme.name,
                                onApply = onUpdateAppTheme
                            )
                            lockedThemeToPreview = null
                        },
                        variant = KintsuButtonVariant.ACCENT_SOFT
                    )
                } else {
                    KintsuButton(
                        text = "Close",
                        onClick = { lockedThemeToPreview = null },
                        variant = KintsuButtonVariant.SECONDARY
                    )
                }
            }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.s)) {
                Text(
                    text = "${theme.subtitle}\n\nExclusive Sanctuary aesthetic. Without unlocking in the Shop, it cannot be permanently equipped. You can try a 10-minute live functional preview (1 time) to test it on your device.",
                    style = typography.caption,
                    color = colors.textSecondary
                )
                if (isPreviewActive) {
                    Text(
                        text = "Active Preview: ${remainingSeconds / 60}m ${remainingSeconds % 60}s remaining",
                        style = typography.caption,
                        color = colors.goldSeam,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
@Composable
private fun IconsTabContent(
    currentIconId: String,
    onSelectIcon: (String) -> Unit,
    allShopItems: List<ShopItemEntity>,
    onOpenShop: () -> Unit
) {
    val context = LocalContext.current
    val activePreviewId by ItemPreviewManager.activePreviewId.collectAsState()
    val remainingSeconds by ItemPreviewManager.remainingSeconds.collectAsState()
    var lockedIconToPreview by remember { mutableStateOf<LauncherIconInfo?>(null) }
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography
    val icons = AppIconManager.ICONS

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(spacing.m)
    ) {
        item {
            Text(
                text = "APP LAUNCHER ICONS",
                style = typography.caption,
                color = colors.textTertiary
            )
        }

        items(icons, key = { it.id }) { iconInfo ->
            val shopItem = allShopItems.find { it.id == iconInfo.id }
            val isUnlocked = iconInfo.isDefault || (shopItem?.isUnlocked == true)
            val isPreviewActive = ItemPreviewManager.isItemInPreview(iconInfo.id)
            val isSelected = currentIconId == iconInfo.id

            KintsuCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (isUnlocked) {
                            onSelectIcon(iconInfo.id)
                        } else {
                            lockedIconToPreview = iconInfo
                        }
                    },
                backgroundColor = if (isSelected) colors.surfaceElevated else colors.surface,
                borderColor = if (isSelected) colors.accent else (if (!isUnlocked) colors.border.copy(alpha = 0.5f) else colors.border)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(spacing.m),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(spacing.m)
                    ) {
                        // Icon Preview
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.surfaceElevated)
                                .border(1.dp, if (isSelected) colors.accent else colors.border, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = iconInfo.drawableResId),
                                contentDescription = iconInfo.title,
                                modifier = Modifier.size(36.dp),
                                tint = Color.Unspecified
                            )
                        }

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = iconInfo.title,
                                    style = typography.itemTitle,
                                    color = if (isSelected) colors.accent else (if (!isUnlocked) colors.textSecondary else colors.textPrimary)
                                )
                                if (isSelected) {
                                    Spacer(modifier = Modifier.width(spacing.xs))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(colors.accent.copy(alpha = 0.15f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "Active",
                                            style = typography.caption.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                            color = colors.accent
                                        )
                                    }
                                } else if (isPreviewActive) {
                                    Spacer(modifier = Modifier.width(spacing.xs))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(colors.goldSeam.copy(alpha = 0.2f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "${remainingSeconds / 60}m ${remainingSeconds % 60}s",
                                            style = typography.caption.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                            color = colors.goldSeam
                                        )
                                    }
                                } else if (!isUnlocked) {
                                    Spacer(modifier = Modifier.width(spacing.xs))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(colors.surfaceElevated)
                                            .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                                        ) {
                                            IconLock(color = colors.textTertiary, size = 10.dp)
                                            Text(
                                                text = "Shop",
                                                style = typography.caption.copy(fontSize = 10.sp),
                                                color = colors.textTertiary
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = iconInfo.description,
                                style = typography.caption,
                                color = colors.textSecondary
                            )
                        }
                    }

                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(colors.accent),
                            contentAlignment = Alignment.Center
                        ) {
                            IconCheck(color = colors.background, size = 14.dp)
                        }
                    } else if (isUnlocked) {
                        Text(
                            text = "Apply",
                            style = typography.caption,
                            color = colors.accent,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }

    // Locked Icon Preview & Shop Dialog
    lockedIconToPreview?.let { iconInfo ->
        val costCoins = iconInfo.costCoins
        val canTrial = ItemPreviewManager.canPreview(context, iconInfo.id)
        val isPreviewActive = ItemPreviewManager.isItemInPreview(iconInfo.id)

        KintsuDialog(
            title = iconInfo.title,
            onDismissRequest = { lockedIconToPreview = null },
            confirmButton = {
                KintsuButton(
                    text = "Unlock ($costCoins Coins)",
                    onClick = {
                        lockedIconToPreview = null
                        onOpenShop()
                    },
                    variant = KintsuButtonVariant.PRIMARY
                )
            },
            dismissButton = {
                if (canTrial && !isPreviewActive) {
                    KintsuButton(
                        text = "10-Min Preview",
                        onClick = {
                            ItemPreviewManager.startIconPreview(
                                context = context,
                                iconId = iconInfo.id,
                                currentIconId = currentIconId,
                                onApply = onSelectIcon
                            )
                            lockedIconToPreview = null
                        },
                        variant = KintsuButtonVariant.ACCENT_SOFT
                    )
                } else {
                    KintsuButton(
                        text = "Close",
                        onClick = { lockedIconToPreview = null },
                        variant = KintsuButtonVariant.SECONDARY
                    )
                }
            }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.s)) {
                Text(
                    text = "${iconInfo.description}\n\nExclusive Sanctuary app icon. Without unlocking in the Shop, it cannot be permanently set. You can test it with a 10-minute live functional preview (1 time) to see it on your home screen.",
                    style = typography.caption,
                    color = colors.textSecondary
                )
                if (isPreviewActive) {
                    Text(
                        text = "Active Preview: ${remainingSeconds / 60}m ${remainingSeconds % 60}s remaining",
                        style = typography.caption,
                        color = colors.goldSeam,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
@Composable
private fun AppearanceTabContent(
    currentSettings: KintsuSettings,
    onUpdateThemeMode: (ThemeMode) -> Unit,
    onUpdateAccent: (AccentOption) -> Unit,
    onUpdateContentDensity: (ContentDensity) -> Unit
) {
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(spacing.l)
    ) {
        // Theme Mode
        item {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.s)) {
                Text(
                    text = "THEME MODE",
                    style = typography.caption,
                    color = colors.textTertiary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing.xs)
                ) {
                    ThemeMode.values().forEach { mode ->
                        val isSelected = currentSettings.themeMode == mode
                        KintsuFilterChip(
                            label = mode.name.lowercase().replaceFirstChar { it.uppercase() },
                            selected = isSelected,
                            onClick = { onUpdateThemeMode(mode) }
                        )
                    }
                }
            }
        }

        // Accent Colors
        item {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.s)) {
                Text(
                    text = "ACCENT COLOR",
                    style = typography.caption,
                    color = colors.textTertiary
                )
                Text(
                    text = "Active: ${currentSettings.accentOption.displayName}",
                    style = typography.caption,
                    color = colors.textSecondary
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(spacing.s)
                ) {
                    AccentOption.values().forEach { accent ->
                        val isSelected = currentSettings.accentOption == accent
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(accent.color)
                                .clickable { onUpdateAccent(accent) }
                                .then(
                                    if (isSelected) {
                                        Modifier.border(3.dp, colors.textPrimary, CircleShape)
                                    } else {
                                        Modifier.border(1.dp, colors.border.copy(alpha = 0.5f), CircleShape)
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                IconCheck(color = Color.White, size = 16.dp)
                            }
                        }
                    }
                }
            }
        }

        // Content Density
        item {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.s)) {
                Text(
                    text = "CONTENT DENSITY",
                    style = typography.caption,
                    color = colors.textTertiary
                )
                Text(
                    text = "Adjust padding and compactness for higher data visibility or breathing room.",
                    style = typography.caption,
                    color = colors.textSecondary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing.xs)
                ) {
                    ContentDensity.values().forEach { density ->
                        val label = if (density == ContentDensity.COMFORTABLE) "Comfortable" else "Compact"
                        KintsuFilterChip(
                            label = label,
                            selected = currentSettings.contentDensity == density,
                            onClick = { onUpdateContentDensity(density) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MotionTabContent(
    currentSettings: KintsuSettings,
    onUpdateHapticsEnabled: (Boolean) -> Unit,
    onUpdateReducedMotion: (Boolean) -> Unit
) {
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(spacing.m)
    ) {
        item {
            Text(
                text = "FLUID MOTION & HAPTICS (APPLE WWDC SPEC)",
                style = typography.caption,
                color = colors.textTertiary
            )
        }

        // Haptics Card
        item {
            KintsuCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = colors.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(spacing.m),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = spacing.m)) {
                        Text(
                            text = "Tactile Haptics",
                            style = typography.itemTitle,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Subtle physical clicks on habit completion, timer actions, and button presses.",
                            style = typography.caption,
                            color = colors.textSecondary
                        )
                    }
                    Switch(
                        checked = currentSettings.hapticsEnabled,
                        onCheckedChange = onUpdateHapticsEnabled,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = colors.surface,
                            checkedTrackColor = colors.accent,
                            uncheckedThumbColor = colors.textTertiary,
                            uncheckedTrackColor = colors.border
                        )
                    )
                }
            }
        }

        // Reduced Motion Card
        item {
            KintsuCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = colors.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(spacing.m),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = spacing.m)) {
                        Text(
                            text = "Reduced Motion",
                            style = typography.itemTitle,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Disables spring physics and transitions for users sensitive to motion.",
                            style = typography.caption,
                            color = colors.textSecondary
                        )
                    }
                    Switch(
                        checked = currentSettings.reducedMotion,
                        onCheckedChange = onUpdateReducedMotion,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = colors.surface,
                            checkedTrackColor = colors.accent,
                            uncheckedThumbColor = colors.textTertiary,
                            uncheckedTrackColor = colors.border
                        )
                    )
                }
            }
        }

        // Motion Philosophy Note
        item {
            KintsuCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = colors.surfaceElevated.copy(alpha = 0.5f),
                borderColor = colors.accent.copy(alpha = 0.3f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(spacing.m),
                    verticalArrangement = Arrangement.spacedBy(spacing.xs)
                ) {
                    Text(
                        text = "Apple Fluid Interface Architecture",
                        style = typography.caption.copy(fontWeight = FontWeight.Bold),
                        color = colors.accent
                    )
                    Text(
                        text = "Motion is continuous, velocity-aware, and interruptible at any moment. Every transition carries momentum forward and aligns interface physics directly with human touch.",
                        style = typography.caption,
                        color = colors.textSecondary
                    )
                }
            }
        }
    }
}


private fun mapThemeToShopId(theme: AppTheme): String {
    return when (theme) {
        AppTheme.SAGE_IVORY -> "theme_sage_ivory"
        AppTheme.MIDNIGHT_CHAMPAGNE -> "theme_obsidian_gold"
        AppTheme.NORDIC_FROST -> "theme_carrara_silver"
        AppTheme.MATCHA_ATELIER -> "theme_imperial_jade"
        AppTheme.COASTAL_MIST -> "theme_lapis_lazuli"
        AppTheme.SAKURA_DAWN -> "theme_rose_quartz"
        AppTheme.TERRACOTTA_LINEN -> "theme_terracotta_clay"
        AppTheme.LAVENDER_MIST -> "theme_opal_pearl"
        AppTheme.OCEAN_PEARL -> "theme_celestial_glass"
        AppTheme.ECLIPSE_GOLD -> "theme_brushed_titanium"
        else -> "theme_${theme.name.lowercase()}"
    }
}
