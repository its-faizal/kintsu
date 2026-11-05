package com.kintsu.app.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kintsu.app.ui.theme.KintsuTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

/**
 * Thread-safe high performance in-memory cache for loaded app icons and logos.
 */
object AppLogoCache {
    private val cache = ConcurrentHashMap<String, Bitmap>()

    fun getBitmap(packageName: String): Bitmap? = cache[packageName]

    fun getOrLoad(context: Context, packageName: String): Bitmap? {
        val existing = cache[packageName]
        if (existing != null) return existing

        return try {
            val pm = context.packageManager
            val drawable = pm.getApplicationIcon(packageName)
            val bmp = drawableToBitmap(drawable)
            if (bmp != null) {
                cache[packageName] = bmp
            }
            bmp
        } catch (_: Exception) {
            null
        }
    }

    fun preload(context: Context, packageNames: Collection<String>) {
        val pm = context.packageManager
        for (pkg in packageNames) {
            if (!cache.containsKey(pkg)) {
                try {
                    val d = pm.getApplicationIcon(pkg)
                    val bmp = drawableToBitmap(d)
                    if (bmp != null) {
                        cache[pkg] = bmp
                    }
                } catch (_: Exception) {}
            }
        }
    }

    private fun drawableToBitmap(drawable: Drawable?): Bitmap? {
        if (drawable == null) return null
        if (drawable is BitmapDrawable && drawable.bitmap != null && !drawable.bitmap.isRecycled) {
            return drawable.bitmap
        }
        return try {
            val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 96
            val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 96
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        } catch (_: Exception) {
            null
        }
    }
}

/**
 * High-performance App Logo Composable: Displays the true native application icon
 * loaded from Android's PackageManager, eliminating letter monograms.
 */
@Composable
fun AppLogo(
    packageName: String,
    modifier: Modifier = Modifier,
    size: Dp = 38.dp,
    shape: Shape = RoundedCornerShape(10.dp),
    fallbackAppName: String = ""
) {
    val context = LocalContext.current
    val cached = remember(packageName) { AppLogoCache.getBitmap(packageName) }
    var bitmap by remember(packageName) { mutableStateOf(cached) }

    LaunchedEffect(packageName) {
        if (bitmap == null) {
            withContext(Dispatchers.IO) {
                val loaded = AppLogoCache.getOrLoad(context, packageName)
                withContext(Dispatchers.Main) {
                    bitmap = loaded
                }
            }
        }
    }

    val colors = KintsuTheme.colors

    if (bitmap != null) {
        Image(
            bitmap = bitmap!!.asImageBitmap(),
            contentDescription = fallbackAppName,
            modifier = modifier
                .size(size)
                .clip(shape)
        )
    } else {
        // Aesthetic ceramic app placeholder while resolving
        Box(
            modifier = modifier
                .size(size)
                .clip(shape)
                .background(colors.surfaceElevated)
                .border(1.dp, colors.border.copy(alpha = 0.5f), shape),
            contentAlignment = Alignment.Center
        ) {
            IconAppSquare(size = size * 0.45f, color = colors.goldSeam)
        }
    }
}

/**
 * Dedicated Group / Category Restriction Logo:
 * Renders a bespoke category shield logo (Social, Video, Gaming, Productivity, Custom).
 */
@Composable
fun GroupCategoryBadge(
    categoryType: String,
    modifier: Modifier = Modifier,
    size: Dp = 42.dp
) {
    val colors = KintsuTheme.colors
    val normalized = categoryType.uppercase()

    val badgeColor = when {
        normalized.contains("SOCIAL") -> colors.goldSeam
        normalized.contains("STREAM") || normalized.contains("VIDEO") -> Color(0xFFE57373)
        normalized.contains("GAME") -> Color(0xFF81C784)
        normalized.contains("PROD") -> Color(0xFF64B5F6)
        else -> colors.accent
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(12.dp))
            .background(badgeColor.copy(alpha = 0.12f))
            .border(1.dp, badgeColor.copy(alpha = 0.45f), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        when {
            normalized.contains("SOCIAL") -> IconSocialCategory(size = size * 0.52f, color = badgeColor)
            normalized.contains("STREAM") || normalized.contains("VIDEO") -> IconVideoCategory(size = size * 0.52f, color = badgeColor)
            normalized.contains("GAME") -> IconGamepadCategory(size = size * 0.52f, color = badgeColor)
            normalized.contains("PROD") -> IconProductivityCategory(size = size * 0.52f, color = badgeColor)
            else -> IconShield(size = size * 0.52f, color = badgeColor)
        }
    }
}

/**
 * Shows the actual member app logos inside a Group Restriction row.
 */
@Composable
fun GroupAppLogosRow(
    packageNames: List<String>,
    modifier: Modifier = Modifier,
    maxLogos: Int = 4,
    logoSize: Dp = 22.dp
) {
    val cleanPkgs = remember(packageNames) {
        packageNames.map { it.trim() }.filter { it.isNotBlank() }
    }
    if (cleanPkgs.isEmpty()) return

    val displayPkgs = cleanPkgs.take(maxLogos)
    val remaining = cleanPkgs.size - displayPkgs.size
    val colors = KintsuTheme.colors

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (pkg in displayPkgs) {
            AppLogo(
                packageName = pkg,
                size = logoSize,
                shape = RoundedCornerShape(6.dp)
            )
        }

        if (remaining > 0) {
            Box(
                modifier = Modifier
                    .size(logoSize)
                    .clip(RoundedCornerShape(6.dp))
                    .background(colors.surface)
                    .border(0.8.dp, colors.border, RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+$remaining",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textSecondary
                )
            }
        }
    }
}

// --- Category Vector Canvas Icons ---

@Composable
fun IconAppSquare(color: Color, modifier: Modifier = Modifier, size: Dp = 18.dp) {
    androidx.compose.foundation.Canvas(modifier = modifier.size(size)) {
        val w = size.toPx()
        val h = size.toPx()
        drawRoundRect(
            color = color,
            topLeft = androidx.compose.ui.geometry.Offset(w * 0.15f, h * 0.15f),
            size = androidx.compose.ui.geometry.Size(w * 0.7f, h * 0.7f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.18f, h * 0.18f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.8.dp.toPx())
        )
    }
}

@Composable
fun IconSocialCategory(color: Color, modifier: Modifier = Modifier, size: Dp = 22.dp) {
    androidx.compose.foundation.Canvas(modifier = modifier.size(size)) {
        val w = size.toPx()
        val h = size.toPx()
        // Two connected chat/speech bubble nodes
        drawCircle(color = color, radius = w * 0.22f, center = androidx.compose.ui.geometry.Offset(w * 0.38f, h * 0.45f))
        drawCircle(color = color, radius = w * 0.18f, center = androidx.compose.ui.geometry.Offset(w * 0.68f, h * 0.35f))
        drawLine(
            color = color,
            start = androidx.compose.ui.geometry.Offset(w * 0.38f, h * 0.45f),
            end = androidx.compose.ui.geometry.Offset(w * 0.68f, h * 0.35f),
            strokeWidth = 2.dp.toPx()
        )
    }
}

@Composable
fun IconVideoCategory(color: Color, modifier: Modifier = Modifier, size: Dp = 22.dp) {
    androidx.compose.foundation.Canvas(modifier = modifier.size(size)) {
        val w = size.toPx()
        val h = size.toPx()
        // Video monitor / film card
        drawRoundRect(
            color = color,
            topLeft = androidx.compose.ui.geometry.Offset(w * 0.15f, h * 0.2f),
            size = androidx.compose.ui.geometry.Size(w * 0.7f, h * 0.6f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.12f, h * 0.12f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.8.dp.toPx())
        )
        // Play triangle inside
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(w * 0.45f, h * 0.36f)
            lineTo(w * 0.62f, h * 0.5f)
            lineTo(w * 0.45f, h * 0.64f)
            close()
        }
        drawPath(path = path, color = color)
    }
}

@Composable
fun IconGamepadCategory(color: Color, modifier: Modifier = Modifier, size: Dp = 22.dp) {
    androidx.compose.foundation.Canvas(modifier = modifier.size(size)) {
        val w = size.toPx()
        val h = size.toPx()
        // Game controller outline
        drawRoundRect(
            color = color,
            topLeft = androidx.compose.ui.geometry.Offset(w * 0.15f, h * 0.3f),
            size = androidx.compose.ui.geometry.Size(w * 0.7f, h * 0.42f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.2f, h * 0.2f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.8.dp.toPx())
        )
        // D-pad dot left
        drawCircle(color = color, radius = w * 0.06f, center = androidx.compose.ui.geometry.Offset(w * 0.32f, h * 0.51f))
        // Action button dot right
        drawCircle(color = color, radius = w * 0.06f, center = androidx.compose.ui.geometry.Offset(w * 0.68f, h * 0.51f))
    }
}

@Composable
fun IconProductivityCategory(color: Color, modifier: Modifier = Modifier, size: Dp = 22.dp) {
    androidx.compose.foundation.Canvas(modifier = modifier.size(size)) {
        val w = size.toPx()
        val h = size.toPx()
        // Checklist / Target card
        drawRoundRect(
            color = color,
            topLeft = androidx.compose.ui.geometry.Offset(w * 0.2f, h * 0.18f),
            size = androidx.compose.ui.geometry.Size(w * 0.6f, h * 0.64f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.1f, h * 0.1f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.8.dp.toPx())
        )
        drawLine(
            color = color,
            start = androidx.compose.ui.geometry.Offset(w * 0.32f, h * 0.4f),
            end = androidx.compose.ui.geometry.Offset(w * 0.68f, h * 0.4f),
            strokeWidth = 1.8.dp.toPx()
        )
        drawLine(
            color = color,
            start = androidx.compose.ui.geometry.Offset(w * 0.32f, h * 0.58f),
            end = androidx.compose.ui.geometry.Offset(w * 0.55f, h * 0.58f),
            strokeWidth = 1.8.dp.toPx()
        )
    }
}
