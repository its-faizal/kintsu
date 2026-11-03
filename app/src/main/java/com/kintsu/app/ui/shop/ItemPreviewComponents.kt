package com.kintsu.app.ui.shop

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kintsu.app.data.model.ShopItemEntity
import com.kintsu.app.ui.components.IconCheck
import com.kintsu.app.ui.components.IconGoldSeam
import com.kintsu.app.ui.theme.AppIconManager
import com.kintsu.app.ui.theme.AppTheme
import com.kintsu.app.ui.theme.KintsuTheme
import com.kintsu.app.ui.theme.getThemePalette

/**
 * Renders an authentic preview component corresponding to the item type.
 */
@Composable
fun ItemPreviewThumbnail(
    item: ShopItemEntity,
    modifier: Modifier = Modifier
) {
    val colors = KintsuTheme.colors

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surfaceElevated),
        contentAlignment = Alignment.Center
    ) {
        when (item.itemType) {
            "THEME" -> ThemeThumbnail(item)
            "APP_ICON" -> AppIconThumbnail(item)
            "WIDGET_SKIN" -> WidgetSkinThumbnail(item)
            "BACKGROUND" -> BackgroundThumbnail(item)
            "FOCUS_FACE", "FOCUS_SCENE" -> FocusThumbnail(item)
            "HABIT_STYLE" -> HabitStyleThumbnail(item)
            "PROFILE_FRAME", "PROFILE_TITLE" -> ProfileThumbnail(item)
            "SOUNDSCAPE" -> SoundscapeThumbnail(item)
            "GOLD_REPAIR_STYLE" -> GoldRepairThumbnail(item)
            "BUNDLE" -> BundleThumbnail(item)
            else -> GenericThumbnail(item)
        }
    }
}

@Composable
fun ThemeThumbnail(item: ShopItemEntity) {
    val cleanId = item.id.replace("theme_", "").uppercase()
    val themeEnum = AppTheme.values().find { it.name.uppercase() == cleanId } ?: AppTheme.SAGE_IVORY
    val palette = themeEnum.previewPalette

    Column(
        modifier = Modifier.padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            palette.forEach { color ->
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(0.5.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .width(52.dp)
                .height(28.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(palette.first())
                .border(1.dp, palette.getOrElse(2) { palette.first() }, RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .width(32.dp)
                    .height(8.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(palette.getOrElse(2) { Color.Gray })
            )
        }
    }
}

@Composable
fun AppIconThumbnail(item: ShopItemEntity) {
    val iconInfo = AppIconManager.getIconById(item.id)
    Image(
        painter = painterResource(id = iconInfo.drawableResId),
        contentDescription = item.title,
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(10.dp))
    )
}

@Composable
fun WidgetSkinThumbnail(item: ShopItemEntity) {
    val colors = KintsuTheme.colors
    val skinColor = when (item.id) {
        "widget_frosted_pearl" -> Color(0xFFE2E7EA)
        "widget_warm_linen" -> Color(0xFFEADCC9)
        "widget_botanical_sage" -> Color(0xFF7E9675)
        "widget_rose_glass" -> Color(0xFFC78F85)
        "widget_midnight_bronze" -> Color(0xFF262523)
        "widget_minimal_paper" -> Color(0xFFF5F3EF)
        "widget_ocean_mist" -> Color(0xFF568E96)
        "widget_gold_vein" -> Color(0xFFD4AF37)
        else -> colors.goldSeam
    }

    Box(
        modifier = Modifier
            .width(56.dp)
            .height(34.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(skinColor.copy(alpha = 0.35f))
            .border(1.5.dp, skinColor, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(skinColor))
            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(skinColor))
            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(skinColor))
        }
    }
}

@Composable
fun BackgroundThumbnail(item: ShopItemEntity) {
    val gradientBrush = when {
        item.id.contains("misty") || item.id.contains("mountain") -> Brush.verticalGradient(listOf(Color(0xFF8AA5BA), Color(0xFFD6E2E9)))
        item.id.contains("lake") || item.id.contains("coastal") -> Brush.verticalGradient(listOf(Color(0xFF547F9E), Color(0xFFA6C5D7)))
        item.id.contains("forest") || item.id.contains("meadow") -> Brush.verticalGradient(listOf(Color(0xFF476949), Color(0xFF87A878)))
        item.id.contains("sakura") -> Brush.verticalGradient(listOf(Color(0xFFE898AC), Color(0xFFFDE8EE)))
        item.id.contains("golden") -> Brush.verticalGradient(listOf(Color(0xFFD98A3B), Color(0xFFFBE5C3)))
        item.id.contains("midnight") -> Brush.verticalGradient(listOf(Color(0xFF0F1420), Color(0xFF273146)))
        else -> Brush.verticalGradient(listOf(Color(0xFF6B7280), Color(0xFFE5E7EB)))
    }

    Box(
        modifier = Modifier
            .width(52.dp)
            .height(38.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(gradientBrush)
            .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
    )
}

@Composable
fun FocusThumbnail(item: ShopItemEntity) {
    val colors = KintsuTheme.colors
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .border(2.5.dp, colors.goldSeam, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text("25", color = colors.goldSeam, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun HabitStyleThumbnail(item: ShopItemEntity) {
    val colors = KintsuTheme.colors
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(colors.accentSoft)
            .border(1.5.dp, colors.accent, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        IconCheck(color = colors.accent, size = 18.dp)
    }
}

@Composable
fun ProfileThumbnail(item: ShopItemEntity) {
    val colors = KintsuTheme.colors
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(colors.surface)
            .border(2.5.dp, colors.goldSeam, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text("K", color = colors.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun SoundscapeThumbnail(item: ShopItemEntity) {
    val colors = KintsuTheme.colors
    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        modifier = Modifier.padding(6.dp)
    ) {
        Box(modifier = Modifier.width(4.dp).height(12.dp).background(colors.goldSeam, RoundedCornerShape(2.dp)))
        Box(modifier = Modifier.width(4.dp).height(24.dp).background(colors.goldSeam, RoundedCornerShape(2.dp)))
        Box(modifier = Modifier.width(4.dp).height(18.dp).background(colors.goldSeam, RoundedCornerShape(2.dp)))
        Box(modifier = Modifier.width(4.dp).height(10.dp).background(colors.goldSeam, RoundedCornerShape(2.dp)))
    }
}

@Composable
fun GoldRepairThumbnail(item: ShopItemEntity) {
    val colors = KintsuTheme.colors
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(colors.surfaceElevated)
            .border(1.dp, colors.goldSeam.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        IconGoldSeam(color = colors.goldSeam, size = 26.dp)
    }
}

@Composable
fun BundleThumbnail(item: ShopItemEntity) {
    val colors = KintsuTheme.colors
    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
                Brush.linearGradient(
                    listOf(colors.goldSeam.copy(alpha = 0.3f), colors.accent.copy(alpha = 0.3f))
                )
            )
            .border(1.5.dp, colors.goldSeam, RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        IconGoldSeam(color = colors.goldSeam, size = 22.dp)
    }
}

@Composable
fun GenericThumbnail(item: ShopItemEntity) {
    val colors = KintsuTheme.colors
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(colors.accentSoft),
        contentAlignment = Alignment.Center
    ) {
        IconGoldSeam(color = colors.accent, size = 20.dp)
    }
}

/**
 * Large Interactive Preview Hero for ItemDetailPreviewSheet
 */
@Composable
fun LargeItemInteractivePreview(
    item: ShopItemEntity,
    modifier: Modifier = Modifier
) {
    val colors = KintsuTheme.colors
    val typography = KintsuTheme.typography

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surfaceElevated)
            .border(1.dp, colors.border, RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        when (item.itemType) {
            "THEME" -> LargeThemePreview(item)
            "APP_ICON" -> LargeAppIconMasksPreview(item)
            "WIDGET_SKIN" -> LargeWidgetSkinPreview(item)
            "BACKGROUND" -> LargeBackgroundPreview(item)
            "FOCUS_FACE", "FOCUS_SCENE" -> LargeFocusPreview(item)
            "HABIT_STYLE" -> LargeHabitStylePreview(item)
            "PROFILE_FRAME", "PROFILE_TITLE" -> LargeProfilePreview(item)
            "SOUNDSCAPE" -> LargeSoundPreview(item)
            "GOLD_REPAIR_STYLE" -> LargeGoldRepairPreview(item)
            "BUNDLE" -> LargeBundlePreview(item)
            else -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(item.title, style = typography.itemTitle, color = colors.textPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(item.category, style = typography.secondary, color = colors.textSecondary)
                }
            }
        }
    }
}

@Composable
fun LargeThemePreview(item: ShopItemEntity) {
    val cleanId = item.id.replace("theme_", "").uppercase()
    val themeEnum = AppTheme.values().find { it.name.uppercase() == cleanId } ?: AppTheme.SAGE_IVORY
    val palette = getThemePalette(themeEnum, isDark = false)

    // Render a miniature interactive Kintsu card
    Box(
        modifier = Modifier
            .width(260.dp)
            .height(150.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(palette.background)
            .border(1.dp, palette.border, RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Kintsu Journey", color = palette.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(palette.accent)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(palette.surface)
                    .border(1.dp, palette.border.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(palette.goldSeam))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Mindful Morning Ritual", color = palette.textPrimary, fontSize = 11.sp)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(90.dp)
                        .height(26.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(palette.accent),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Focus Now", color = palette.onAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Text("3 / 4 completed", color = palette.textSecondary, fontSize = 10.sp)
            }
        }
    }
}

@Composable
fun LargeAppIconMasksPreview(item: ShopItemEntity) {
    val iconInfo = AppIconManager.getIconById(item.id)
    val colors = KintsuTheme.colors

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Launcher Adaptive Shapes", style = KintsuTheme.typography.secondary, color = colors.textSecondary)
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            // Circle mask
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(id = iconInfo.drawableResId),
                    contentDescription = "Circle",
                    modifier = Modifier.size(52.dp).clip(CircleShape).border(1.dp, colors.border, CircleShape)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text("Circle", fontSize = 10.sp, color = colors.textSecondary)
            }
            // Squircle mask
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(id = iconInfo.drawableResId),
                    contentDescription = "Squircle",
                    modifier = Modifier.size(52.dp).clip(RoundedCornerShape(18.dp)).border(1.dp, colors.border, RoundedCornerShape(18.dp))
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text("Squircle", fontSize = 10.sp, color = colors.textSecondary)
            }
            // Rounded Square mask
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(id = iconInfo.drawableResId),
                    contentDescription = "Square",
                    modifier = Modifier.size(52.dp).clip(RoundedCornerShape(10.dp)).border(1.dp, colors.border, RoundedCornerShape(10.dp))
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text("Square", fontSize = 10.sp, color = colors.textSecondary)
            }
        }
    }
}

@Composable
fun LargeWidgetSkinPreview(item: ShopItemEntity) {
    val colors = KintsuTheme.colors
    Box(
        modifier = Modifier
            .width(280.dp)
            .height(100.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface)
            .border(1.5.dp, colors.goldSeam, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Daily Rituals", style = KintsuTheme.typography.itemTitle, color = colors.textPrimary)
                Text("Today", style = KintsuTheme.typography.secondary, color = colors.goldSeam)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(4) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(colors.goldSeam.copy(alpha = 0.2f))
                            .border(1.5.dp, colors.goldSeam, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        IconCheck(color = colors.goldSeam, size = 16.dp)
                    }
                }
            }
        }
    }
}

@Composable
fun LargeBackgroundPreview(item: ShopItemEntity) {
    BackgroundThumbnail(item)
}

@Composable
fun LargeFocusPreview(item: ShopItemEntity) {
    val colors = KintsuTheme.colors
    Box(
        modifier = Modifier
            .size(110.dp)
            .clip(CircleShape)
            .background(colors.surface)
            .border(4.dp, colors.goldSeam, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("25:00", style = KintsuTheme.typography.screenTitle, color = colors.textPrimary)
            Text("Focus Session", style = KintsuTheme.typography.secondary, color = colors.goldSeam)
        }
    }
}

@Composable
fun LargeHabitStylePreview(item: ShopItemEntity) {
    val colors = KintsuTheme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(colors.accentSoft)
                .border(2.5.dp, colors.goldSeam, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            IconCheck(color = colors.goldSeam, size = 26.dp)
        }
        Column {
            Text("Mindful Meditation", style = KintsuTheme.typography.itemTitle, color = colors.textPrimary)
            Text("7-day streak intact", style = KintsuTheme.typography.secondary, color = colors.textSecondary)
        }
    }
}

@Composable
fun LargeProfilePreview(item: ShopItemEntity) {
    val colors = KintsuTheme.colors
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(70.dp)
                .clip(CircleShape)
                .background(colors.surface)
                .border(3.dp, colors.goldSeam, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("K", color = colors.textPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(item.title, style = KintsuTheme.typography.itemTitle, color = colors.textPrimary)
    }
}

@Composable
fun LargeSoundPreview(item: ShopItemEntity) {
    val colors = KintsuTheme.colors
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        SoundscapeThumbnail(item)
        Spacer(modifier = Modifier.height(8.dp))
        Text(item.title, style = KintsuTheme.typography.itemTitle, color = colors.textPrimary)
        Text("Harmonic mindful acoustic tone", style = KintsuTheme.typography.secondary, color = colors.textSecondary)
    }
}

@Composable
fun LargeGoldRepairPreview(item: ShopItemEntity) {
    val colors = KintsuTheme.colors
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconGoldSeam(color = colors.goldSeam, size = 48.dp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(item.title, style = KintsuTheme.typography.itemTitle, color = colors.textPrimary)
        Text("Restores habit streaks with golden lacquer", style = KintsuTheme.typography.secondary, color = colors.textSecondary)
    }
}

@Composable
fun LargeBundlePreview(item: ShopItemEntity) {
    val colors = KintsuTheme.colors
    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("" + item.title, style = KintsuTheme.typography.itemTitle, color = colors.goldSeam)
        Spacer(modifier = Modifier.height(6.dp))
        Text(item.description, style = KintsuTheme.typography.secondary, color = colors.textSecondary, maxLines = 3)
    }
}
