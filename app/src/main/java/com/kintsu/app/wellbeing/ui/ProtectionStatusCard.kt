package com.kintsu.app.wellbeing.ui

import androidx.compose.foundation.background
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.kintsu.app.ui.components.IconCheck
import com.kintsu.app.ui.components.KintsuButton
import com.kintsu.app.ui.components.KintsuButtonVariant
import com.kintsu.app.ui.components.KintsuCard
import com.kintsu.app.ui.theme.KintsuTheme

@Composable
fun ProtectionStatusCard(
    isProtectionActive: Boolean,
    isFailSafeTripped: Boolean,
    missingPermissionName: String?,
    onFixPermission: () -> Unit,
    onResetFailSafe: () -> Unit,
    onOpenWizard: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography

    KintsuCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = if (isFailSafeTripped) {
            colors.accentSoft.copy(alpha = 0.2f)
        } else if (isProtectionActive) {
            colors.surfaceElevated
        } else {
            colors.surfaceElevated.copy(alpha = 0.7f)
        },
        borderColor = if (isFailSafeTripped) colors.goldSeam else colors.border
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
                            .background(
                                if (isFailSafeTripped) colors.goldSeam
                                else if (isProtectionActive) colors.accent
                                else colors.textTertiary
                            )
                    )
                    Spacer(modifier = Modifier.width(spacing.s))
                    Column {
                        Text(
                            text = if (isFailSafeTripped) {
                                "Protection Paused (Failsafe)"
                            } else if (isProtectionActive) {
                                "Wellbeing Protection Active"
                            } else {
                                "Protection Inactive"
                            },
                            style = typography.itemTitle,
                            color = colors.textPrimary
                        )
                        Text(
                            text = if (isFailSafeTripped) {
                                "Paused after 3 errors to prevent lock-out"
                            } else if (isProtectionActive) {
                                "Mindful shields and time limits monitored"
                            } else if (missingPermissionName != null) {
                                "Needs attention: Grant $missingPermissionName"
                            } else {
                                "Set up your mindful app boundaries"
                            },
                            style = typography.caption,
                            color = if (isFailSafeTripped) colors.goldSeam else colors.textSecondary
                        )
                    }
                }

                if (isFailSafeTripped) {
                    KintsuButton(
                        text = "Resume",
                        onClick = onResetFailSafe,
                        variant = KintsuButtonVariant.ACCENT_SOFT
                    )
                } else if (!isProtectionActive && missingPermissionName != null) {
                    KintsuButton(
                        text = "Grant",
                        onClick = onFixPermission,
                        variant = KintsuButtonVariant.PRIMARY
                    )
                } else if (!isProtectionActive) {
                    KintsuButton(
                        text = "Set Up",
                        onClick = onOpenWizard,
                        variant = KintsuButtonVariant.ACCENT_SOFT
                    )
                }
            }
        }
    }
}
