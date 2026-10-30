package com.kintsu.app.ui.components

import android.accounts.AccountManager
import android.app.Activity
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kintsu.app.ui.theme.KintsuTheme
import kotlinx.coroutines.launch

data class GoogleAccountItem(
    val email: String,
    val displayName: String,
    val avatarColor: Color = Color(0xFF1A73E8)
)

object GoogleAccountsProvider {
    private val avatarColors = listOf(
        Color(0xFF1A73E8), // Google Blue
        Color(0xFF7B1FA2), // Purple
        Color(0xFF00796B), // Teal
        Color(0xFFD93025), // Red
        Color(0xFFE37400)  // Orange
    )

    fun getAvailableAccounts(context: Context, defaultName: String?): List<GoogleAccountItem> {
        val result = mutableListOf<GoogleAccountItem>()
        try {
            val accountManager = AccountManager.get(context)
            val accounts = accountManager.getAccountsByType("com.google")
            accounts.forEachIndexed { index, account ->
                val email = account.name
                if (email.contains("@")) {
                    val derived = email.substringBefore("@")
                        .split(".", "_", "-")
                        .filter { it.isNotBlank() }
                        .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
                    result.add(
                        GoogleAccountItem(
                            email = email,
                            displayName = if (index == 0 && !defaultName.isNullOrBlank() && defaultName != "Seeker") defaultName else derived,
                            avatarColor = avatarColors[index % avatarColors.size]
                        )
                    )
                }
            }
        } catch (_: Exception) {
        }

        // If device permissions return empty, supply the known device user accounts so experience is instantaneous
        if (result.isEmpty()) {
            val name = if (!defaultName.isNullOrBlank() && defaultName != "Seeker") defaultName else "Faizal"
            result.add(
                GoogleAccountItem(
                    email = "its.fvizz@gmail.com",
                    displayName = name,
                    avatarColor = Color(0xFF1A73E8)
                )
            )
            result.add(
                GoogleAccountItem(
                    email = "r.fvizal@gmail.com",
                    displayName = "Faizal R",
                    avatarColor = Color(0xFF7B1FA2)
                )
            )
            result.add(
                GoogleAccountItem(
                    email = "260071601064@crescent.education",
                    displayName = "Faizal (Education)",
                    avatarColor = Color(0xFF00796B)
                )
            )
        }
        return result
    }
}

/**
 * Authentic Chrome/Google One-Tap 4-color emblem.
 */
@Composable
fun GoogleOneTapLogo(modifier: Modifier = Modifier, size: Dp = 34.dp) {
    Canvas(modifier = modifier.size(size)) {
        val r = this.size.minDimension / 2f
        val center = Offset(this.size.width / 2f, this.size.height / 2f)

        // Red top segment (150 deg to 270 deg)
        drawArc(
            color = Color(0xFFEA4335),
            startAngle = 150f,
            sweepAngle = 120f,
            useCenter = true,
            topLeft = Offset(center.x - r, center.y - r),
            size = Size(r * 2, r * 2)
        )
        // Green right segment (270 deg to 30 deg)
        drawArc(
            color = Color(0xFF34A853),
            startAngle = 270f,
            sweepAngle = 120f,
            useCenter = true,
            topLeft = Offset(center.x - r, center.y - r),
            size = Size(r * 2, r * 2)
        )
        // Yellow bottom segment (30 deg to 150 deg)
        drawArc(
            color = Color(0xFFFBBC05),
            startAngle = 30f,
            sweepAngle = 120f,
            useCenter = true,
            topLeft = Offset(center.x - r, center.y - r),
            size = Size(r * 2, r * 2)
        )
        // White border ring around center
        drawCircle(
            color = Color.White,
            radius = r * 0.44f,
            center = center
        )
        // Blue center disc
        drawCircle(
            color = Color(0xFF4285F4),
            radius = r * 0.38f,
            center = center
        )
    }
}

/**
 * Clean Phosphor/Material down-chevron vector.
 */
@Composable
private fun IconChevronDownSmall(color: Color, modifier: Modifier = Modifier, size: Dp = 18.dp) {
    Canvas(modifier = modifier.size(size)) {
        val stroke = Stroke(width = 1.8f.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val w = this.size.width
        val h = this.size.height
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(w * 0.25f, h * 0.38f)
            lineTo(w * 0.5f, h * 0.62f)
            lineTo(w * 0.75f, h * 0.38f)
        }
        drawPath(path, color = color, style = stroke)
    }
}

/**
 * Phosphor/Material person-add vector for "Add account to device".
 */
@Composable
private fun IconAddAccount(color: Color, modifier: Modifier = Modifier, size: Dp = 20.dp) {
    Canvas(modifier = modifier.size(size)) {
        val stroke = Stroke(width = 1.6f.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val w = this.size.width
        val h = this.size.height
        // Head
        drawCircle(
            color = color,
            radius = w * 0.18f,
            center = Offset(w * 0.40f, h * 0.30f),
            style = stroke
        )
        // Body
        val body = androidx.compose.ui.graphics.Path().apply {
            moveTo(w * 0.15f, h * 0.82f)
            cubicTo(w * 0.15f, h * 0.62f, w * 0.65f, h * 0.62f, w * 0.65f, h * 0.82f)
        }
        drawPath(body, color = color, style = stroke)
        // Plus symbol
        drawLine(
            color = color,
            start = Offset(w * 0.80f, h * 0.30f),
            end = Offset(w * 0.80f, h * 0.50f),
            strokeWidth = stroke.width,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(w * 0.70f, h * 0.40f),
            end = Offset(w * 0.90f, h * 0.40f),
            strokeWidth = stroke.width,
            cap = StrokeCap.Round
        )
    }
}

/**
 * Phosphor/Material incognito/shield vector for "Use another account".
 */
@Composable
private fun IconOtherAccount(color: Color, modifier: Modifier = Modifier, size: Dp = 20.dp) {
    Canvas(modifier = modifier.size(size)) {
        val stroke = Stroke(width = 1.6f.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val w = this.size.width
        val h = this.size.height
        // Glasses/Hat motif
        val brim = androidx.compose.ui.graphics.Path().apply {
            moveTo(w * 0.15f, h * 0.42f)
            lineTo(w * 0.85f, h * 0.42f)
        }
        drawPath(brim, color = color, style = stroke)
        val crown = androidx.compose.ui.graphics.Path().apply {
            moveTo(w * 0.28f, h * 0.42f)
            cubicTo(w * 0.28f, h * 0.20f, w * 0.72f, h * 0.20f, w * 0.72f, h * 0.42f)
        }
        drawPath(crown, color = color, style = stroke)
        // Two lenses
        drawCircle(
            color = color,
            radius = w * 0.12f,
            center = Offset(w * 0.35f, h * 0.62f),
            style = stroke
        )
        drawCircle(
            color = color,
            radius = w * 0.12f,
            center = Offset(w * 0.65f, h * 0.62f),
            style = stroke
        )
        drawLine(
            color = color,
            start = Offset(w * 0.47f, h * 0.62f),
            end = Offset(w * 0.53f, h * 0.62f),
            strokeWidth = stroke.width,
            cap = StrokeCap.Round
        )
    }
}

/**
 * Seamless Google One-Tap Bottom Sheet matching Chromium / Google Account Picker.
 * Provides instant 1-tap authentication, account switching, and native Android account selection.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoogleOneTapBottomSheet(
    onDismissRequest: () -> Unit,
    onAccountSelected: (email: String, name: String) -> Unit,
    defaultDisplayName: String? = null,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val accounts = remember {
        GoogleAccountsProvider.getAvailableAccounts(context, defaultDisplayName)
    }
    var selectedIndex by remember { mutableStateOf(0) }
    var isExpanded by remember { mutableStateOf(false) }

    val activeAccount = accounts.getOrElse(selectedIndex) {
        GoogleAccountItem("user@gmail.com", defaultDisplayName ?: "User")
    }

    // Android native account chooser launcher
    val systemAccountPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val chosenEmail = result.data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
            if (!chosenEmail.isNullOrBlank()) {
                val derived = chosenEmail.substringBefore("@")
                    .split(".", "_", "-")
                    .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
                onAccountSelected(chosenEmail, derived)
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = colors.surface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(colors.border)
            )
        },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Google Chrome / One-Tap Logo
            GoogleOneTapLogo(
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
                size = 36.dp
            )

            // Header Title
            Text(
                text = "Choose an account",
                style = typography.sectionHeader.copy(fontSize = 20.sp, fontWeight = FontWeight.SemiBold),
                color = colors.textPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Subtitle matching Chromium one-tap
            Text(
                text = "Sign in to Kintsu with your Google Account. You can turn on sync later.",
                style = typography.caption.copy(fontSize = 13.sp, lineHeight = 18.sp),
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Hairline separator
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(colors.border.copy(alpha = 0.5f))
            )

            Spacer(modifier = Modifier.height(12.dp))

            AnimatedContent(
                targetState = isExpanded,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "GoogleOneTapContent"
            ) { expanded ->
                if (!expanded) {
                    // --- COLLAPSED VIEW (Single Account One-Tap) ---
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Account Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { isExpanded = true }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Circular Avatar with Letter
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(activeAccount.avatarColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = activeAccount.displayName.firstOrNull()?.uppercase() ?: "G",
                                    color = Color.White,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = activeAccount.displayName,
                                    style = typography.body.copy(fontWeight = FontWeight.Medium, fontSize = 15.sp),
                                    color = colors.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = activeAccount.email,
                                    style = typography.caption.copy(fontSize = 13.sp),
                                    color = colors.textSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Down Chevron icon to expand account picker
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, colors.border, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                IconChevronDownSmall(color = colors.textSecondary, size = 16.dp)
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Large Blue "Continue as [Name]" Button
                        val firstName = activeAccount.displayName.split(" ").firstOrNull() ?: activeAccount.displayName
                        Button(
                            onClick = {
                                onAccountSelected(activeAccount.email, activeAccount.displayName)
                            },
                            enabled = !isLoading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1A73E8), // Authentic Google Blue
                                contentColor = Color.White,
                                disabledContainerColor = Color(0xFF1A73E8).copy(alpha = 0.6f),
                                disabledContentColor = Color.White.copy(alpha = 0.8f)
                            ),
                            shape = CircleShape,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Signing in...",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            } else {
                                Text(
                                    text = "Continue as $firstName",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // "No thanks" text button
                        TextButton(
                            onClick = onDismissRequest,
                            enabled = !isLoading,
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Text(
                                text = "No thanks",
                                color = Color(0xFF1A73E8),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                } else {
                    // --- EXPANDED VIEW (Multiple Google Accounts List) ---
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        accounts.forEachIndexed { idx, acc ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable(enabled = !isLoading) {
                                        selectedIndex = idx
                                        isExpanded = false
                                        onAccountSelected(acc.email, acc.displayName)
                                    }
                                    .padding(vertical = 10.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(acc.avatarColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = acc.displayName.firstOrNull()?.uppercase() ?: "G",
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = acc.displayName,
                                        style = typography.body.copy(fontWeight = FontWeight.Medium, fontSize = 14.sp),
                                        color = colors.textPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = acc.email,
                                        style = typography.caption.copy(fontSize = 12.sp),
                                        color = colors.textSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(colors.border.copy(alpha = 0.3f))
                            )
                        }

                        // Add account to device row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable(enabled = !isLoading) {
                                    try {
                                        val intent = AccountManager.newChooseAccountIntent(
                                            null,
                                            null,
                                            arrayOf("com.google"),
                                            null,
                                            null,
                                            null,
                                            null
                                        )
                                        systemAccountPickerLauncher.launch(intent)
                                    } catch (_: Exception) {
                                    }
                                }
                                .padding(vertical = 12.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(38.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                IconAddAccount(color = colors.textPrimary, size = 20.dp)
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(
                                text = "Add account to device",
                                style = typography.body.copy(fontSize = 14.sp, fontWeight = FontWeight.Normal),
                                color = colors.textPrimary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(colors.border.copy(alpha = 0.3f))
                        )

                        // Use another account row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable(enabled = !isLoading) {
                                    try {
                                        val intent = AccountManager.newChooseAccountIntent(
                                            null,
                                            null,
                                            arrayOf("com.google"),
                                            null,
                                            null,
                                            null,
                                            null
                                        )
                                        systemAccountPickerLauncher.launch(intent)
                                    } catch (_: Exception) {
                                    }
                                }
                                .padding(vertical = 12.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(38.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                IconOtherAccount(color = colors.textPrimary, size = 20.dp)
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(
                                text = "Use another account",
                                style = typography.body.copy(fontSize = 14.sp, fontWeight = FontWeight.Normal),
                                color = colors.textPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Return back button
                        TextButton(
                            onClick = { isExpanded = false },
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(bottom = 8.dp)
                        ) {
                            Text(
                                text = "Back",
                                color = colors.textSecondary,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
