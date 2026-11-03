package com.kintsu.app.ui.components

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kintsu.app.ui.theme.KintsuTheme
import java.io.File

data class SignatureAvatarItem(
    val id: String,
    val title: String
)

val SIGNATURE_AVATARS_24 = listOf(
    SignatureAvatarItem("avatar_botanical", "Botanical Sage"),
    SignatureAvatarItem("avatar_lotus", "Golden Lotus"),
    SignatureAvatarItem("avatar_kintsugi", "Kintsugi Seam"),
    SignatureAvatarItem("avatar_mountain", "Zen Mountain"),
    SignatureAvatarItem("avatar_sun", "Dawn Radiance"),
    SignatureAvatarItem("avatar_crane", "Wisdom Chronicle"),
    SignatureAvatarItem("avatar_shield", "Guardian Aegis"),
    SignatureAvatarItem("avatar_seeker", "Mindful Seeker"),
    SignatureAvatarItem("avatar_focus", "Pure Focus"),
    SignatureAvatarItem("avatar_sparkle", "Equilibrium"),
    SignatureAvatarItem("avatar_cosmos", "Cosmic Sphere"),
    SignatureAvatarItem("avatar_calendar", "Timeless Cycle"),
    SignatureAvatarItem("avatar_rhythm", "Infinite Rhythm"),
    SignatureAvatarItem("avatar_flow", "Dynamic Flow"),
    SignatureAvatarItem("avatar_sanctuary", "Sanctuary Haven"),
    SignatureAvatarItem("avatar_presence", "Still Presence"),
    SignatureAvatarItem("avatar_resolve", "Steadfast Resolve"),
    SignatureAvatarItem("avatar_memory", "Echoing Memory"),
    SignatureAvatarItem("avatar_archive", "Silent Archive"),
    SignatureAvatarItem("avatar_ascent", "Ascent Horizon"),
    SignatureAvatarItem("avatar_treasure", "Inner Wealth"),
    SignatureAvatarItem("avatar_compassion", "Compassionate Heart"),
    SignatureAvatarItem("avatar_clarity", "Clear Intention"),
    SignatureAvatarItem("avatar_artisan", "Master Artisan")
)

@Composable
fun ProfileAvatarVisual(
    avatarId: String,
    size: Dp = 40.dp,
    iconSize: Dp = 20.dp,
    modifier: Modifier = Modifier
) {
    val colors = KintsuTheme.colors
    val context = LocalContext.current

    // Check if custom user-selected photo
    if (avatarId.startsWith("custom:")) {
        val pathOrUri = avatarId.removePrefix("custom:")
        val bitmap = remember(pathOrUri) {
            try {
                if (pathOrUri.startsWith("content://")) {
                    context.contentResolver.openInputStream(Uri.parse(pathOrUri))?.use {
                        BitmapFactory.decodeStream(it)
                    }
                } else {
                    val file = File(pathOrUri)
                    if (file.exists()) {
                        BitmapFactory.decodeFile(file.absolutePath)
                    } else null
                }
            } catch (_: Exception) {
                null
            }
        }

        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Profile Avatar",
                contentScale = ContentScale.Crop,
                modifier = modifier
                    .size(size)
                    .clip(CircleShape)
            )
            return
        }
    }

    // 24 Signature Zen Avatars
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(colors.goldSeam.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
    ) {
        when (avatarId) {
            "avatar_botanical" -> IconLeaf(color = colors.goldSeam, size = iconSize)
            "avatar_lotus" -> IconGoal(color = colors.goldSeam, size = iconSize)
            "avatar_kintsugi" -> IconGoldSeam(color = colors.goldSeam, size = iconSize + 2.dp)
            "avatar_mountain" -> IconHabit(color = colors.goldSeam, size = iconSize)
            "avatar_sun" -> IconFlame(color = colors.goldSeam, size = iconSize)
            "avatar_crane" -> IconBook(color = colors.goldSeam, size = iconSize)
            "avatar_shield" -> IconShield(color = colors.goldSeam, size = iconSize)
            "avatar_seeker" -> IconUser(color = colors.goldSeam, size = iconSize)
            "avatar_focus" -> IconFocus(color = colors.goldSeam, size = iconSize)
            "avatar_sparkle" -> IconSparkle(color = colors.goldSeam, size = iconSize)
            "avatar_cosmos" -> IconGlobe(color = colors.goldSeam, size = iconSize)
            "avatar_calendar" -> IconCalendar(color = colors.goldSeam, size = iconSize)
            "avatar_rhythm" -> IconRepeat(color = colors.goldSeam, size = iconSize)
            "avatar_flow" -> IconPlay(color = colors.goldSeam, size = iconSize)
            "avatar_sanctuary" -> IconHome(color = colors.goldSeam, size = iconSize)
            "avatar_presence" -> IconClock(color = colors.goldSeam, size = iconSize)
            "avatar_resolve" -> IconLock(color = colors.goldSeam, size = iconSize)
            "avatar_memory" -> IconHistory(color = colors.goldSeam, size = iconSize)
            "avatar_archive" -> IconArchive(color = colors.goldSeam, size = iconSize)
            "avatar_ascent" -> IconProgress(color = colors.goldSeam, size = iconSize)
            "avatar_treasure" -> IconShop(color = colors.goldSeam, size = iconSize)
            "avatar_compassion" -> IconFavorite(color = colors.goldSeam, size = iconSize)
            "avatar_clarity" -> IconCheck(color = colors.goldSeam, size = iconSize)
            "avatar_artisan" -> IconCustomizer(color = colors.goldSeam, size = iconSize)
            else -> IconUser(color = colors.goldSeam, size = iconSize)
        }
    }
}
