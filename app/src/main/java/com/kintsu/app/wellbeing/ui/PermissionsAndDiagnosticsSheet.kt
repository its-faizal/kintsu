package com.kintsu.app.wellbeing.ui

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.kintsu.app.ui.components.IconCheck
import com.kintsu.app.ui.components.IconShield
import com.kintsu.app.ui.components.IconSparkle
import com.kintsu.app.ui.components.KintsuButton
import com.kintsu.app.ui.components.KintsuButtonVariant
import com.kintsu.app.ui.components.KintsuCard
import com.kintsu.app.ui.theme.KintsuTheme
import com.kintsu.app.wellbeing.domain.PermissionDetail
import com.kintsu.app.wellbeing.domain.PermissionStateHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionsAndDiagnosticsSheet(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var permissions by remember { mutableStateOf<List<PermissionDetail>>(emptyList()) }

    fun refreshPermissions() {
        permissions = PermissionStateHelper.getAllPermissionDetails(context)
    }

    LaunchedEffect(Unit) {
        refreshPermissions()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.background,
        dragHandle = null,
        modifier = Modifier.fillMaxHeight(0.94f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(spacing.l)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Protection Diagnostics & Permissions",
                        style = typography.itemTitle.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Transparent disclosures and granular capability controls",
                        style = typography.caption,
                        color = colors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(spacing.l))

            // Test Protection Simulator Action (Section 13 requirement)
            KintsuCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = colors.surfaceElevated,
                borderColor = colors.goldSeam.copy(alpha = 0.5f)
            ) {
                Column(modifier = Modifier.padding(spacing.m)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Test Mindful Protection",
                                style = typography.itemTitle,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "Safely preview the breathing intervention shield right now",
                                style = typography.caption,
                                color = colors.textSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(spacing.m))

                    KintsuButton(
                        text = "Simulate Intervention Shield",
                        onClick = {
                            val intent = Intent(context, BlockActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                putExtra(BlockActivity.EXTRA_PACKAGE_NAME, "com.kintsu.test.sample")
                                putExtra(BlockActivity.EXTRA_RULE_MODE, "TEST_SIMULATION")
                                putExtra(BlockActivity.EXTRA_STRICTNESS, "GENTLE")
                                putExtra(BlockActivity.EXTRA_PROMPT, "This is a safe test preview of your mindful breathing shield.")
                                putExtra(BlockActivity.EXTRA_PAUSE_SECONDS, 5)
                            }
                            context.startActivity(intent)
                        },
                        variant = KintsuButtonVariant.PRIMARY,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(spacing.xl))

            Text(
                text = "SYSTEM CAPABILITIES & PERMISSION STATUS",
                style = typography.caption.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp),
                color = colors.textTertiary
            )
            Spacer(modifier = Modifier.height(spacing.s))

            // Detailed Permission Breakdown (Section 14)
            permissions.forEach { perm ->
                PermissionDisclosureCard(
                    permission = perm,
                    onOpenSettings = {
                        val intent = perm.openSettingsIntent(context)
                        if (intent != null) {
                            try {
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        }
                    }
                )
                Spacer(modifier = Modifier.height(spacing.m))
            }
        }
    }
}

@Composable
private fun PermissionDisclosureCard(
    permission: PermissionDetail,
    onOpenSettings: () -> Unit
) {
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography

    KintsuCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = colors.surfaceElevated,
        borderColor = if (permission.isGranted) colors.accent.copy(alpha = 0.4f) else colors.border
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.m)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (permission.isGranted) colors.accent else colors.goldSeam)
                    )
                    Spacer(modifier = Modifier.width(spacing.s))
                    Column {
                        Text(
                            text = permission.title,
                            style = typography.itemTitle,
                            color = colors.textPrimary
                        )
                        Text(
                            text = permission.subtitle,
                            style = typography.caption,
                            color = colors.textSecondary
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (permission.isGranted) colors.accentSoft else colors.goldSeamSoft)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (permission.isGranted) "Granted" else if (permission.isOptional) "Optional" else "Action Needed",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (permission.isGranted) colors.accent else colors.goldSeam
                    )
                }
            }

            Spacer(modifier = Modifier.height(spacing.s))

            // Explanation Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.surface)
                    .padding(spacing.s),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "• Why needed: ${permission.whyNeeded}",
                    fontSize = 11.sp,
                    color = colors.textSecondary,
                    lineHeight = 15.sp
                )
                Text(
                    text = "• Features: ${permission.featuresDependent}",
                    fontSize = 11.sp,
                    color = colors.textSecondary,
                    lineHeight = 15.sp
                )
                Text(
                    text = "• Privacy: ${permission.informationAccessed}",
                    fontSize = 11.sp,
                    color = colors.textTertiary,
                    lineHeight = 15.sp
                )
            }

            if (!permission.isGranted) {
                Spacer(modifier = Modifier.height(spacing.s))
                KintsuButton(
                    text = "Grant in Settings",
                    onClick = onOpenSettings,
                    variant = KintsuButtonVariant.SECONDARY,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
