package com.kintsu.app.ui.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kintsu.app.ui.theme.KintsuTheme
import java.util.Locale

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun KintsuTimePickerSheet(
    initialHour: Int,
    initialMinute: Int,
    title: String = "Select Time Window",
    subtitle: String = "10-minute window will remain active from this time.",
    onDismiss: () -> Unit,
    onConfirm: (hour: Int, minute: Int) -> Unit
) {
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography

    var isPm by remember { mutableStateOf(initialHour >= 12) }
    var selectedHour12 by remember {
        val h = when {
            initialHour == 0 -> 12
            initialHour > 12 -> initialHour - 12
            else -> initialHour
        }
        mutableIntStateOf(h)
    }
    var selectedMinute by remember { mutableIntStateOf(initialMinute) }

    val computed24Hour = remember(selectedHour12, isPm) {
        when {
            isPm && selectedHour12 < 12 -> selectedHour12 + 12
            !isPm && selectedHour12 == 12 -> 0
            else -> selectedHour12
        }
    }

    val endMinuteRaw = selectedMinute + 10
    val endHour = (computed24Hour + endMinuteRaw / 60) % 24
    val endMin = endMinuteRaw % 60

    fun formatAmPm(h: Int, m: Int): String {
        val amPm = if (h < 12) "AM" else "PM"
        val dispH = when {
            h == 0 -> 12
            h > 12 -> h - 12
            else -> h
        }
        return String.format(Locale.US, "%d:%02d %s", dispH, m, amPm)
    }

    KintsuBottomSheet(
        onDismissRequest = onDismiss,
        title = title
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.m),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(spacing.l)
        ) {
            Text(
                text = subtitle,
                style = typography.caption,
                color = colors.textSecondary,
                textAlign = TextAlign.Center
            )

            // Large Time Display Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(colors.surfaceElevated)
                    .border(1.2.dp, colors.goldSeam.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                .padding(vertical = spacing.l, horizontal = spacing.m),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Hour selector display
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(colors.surface)
                                .clickable {
                                    selectedHour12 = if (selectedHour12 >= 12) 1 else selectedHour12 + 1
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("▲", fontSize = 12.sp, color = colors.goldSeam)
                        }

                        Text(
                            text = String.format(Locale.US, "%02d", selectedHour12),
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(colors.surface)
                                .clickable {
                                    selectedHour12 = if (selectedHour12 <= 1) 12 else selectedHour12 - 1
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("▼", fontSize = 12.sp, color = colors.goldSeam)
                        }
                    }

                    Text(
                        text = ":",
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.goldSeam,
                        modifier = Modifier.padding(horizontal = spacing.m)
                    )

                    // Minute selector display
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(colors.surface)
                                .clickable {
                                    selectedMinute = (selectedMinute + 5) % 60
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("▲", fontSize = 12.sp, color = colors.goldSeam)
                        }

                        Text(
                            text = String.format(Locale.US, "%02d", selectedMinute),
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(colors.surface)
                                .clickable {
                                    selectedMinute = if (selectedMinute - 5 < 0) 55 else selectedMinute - 5
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("▼", fontSize = 12.sp, color = colors.goldSeam)
                        }
                    }

                    Spacer(modifier = Modifier.width(spacing.l))

                    // AM / PM Toggle Pills
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (!isPm) colors.accentSoft else colors.surface)
                                .border(1.dp, if (!isPm) colors.goldSeam else Color.Transparent, RoundedCornerShape(8.dp))
                                .clickable { isPm = false }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "AM",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (!isPm) colors.goldSeam else colors.textSecondary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isPm) colors.accentSoft else colors.surface)
                                .border(1.dp, if (isPm) colors.goldSeam else Color.Transparent, RoundedCornerShape(8.dp))
                                .clickable { isPm = true }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "PM",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isPm) colors.goldSeam else colors.textSecondary
                            )
                        }
                    }
                }
            }

            // Window Duration Preview Pill
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(spacing.m),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Active Daily: ${formatAmPm(computed24Hour, selectedMinute)} - ${formatAmPm(endHour, endMin)}",
                    style = typography.caption.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
                    color = colors.goldSeam
                )
            }

            // Buttons: Save & Cancel
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.m)
            ) {
                KintsuButton(
                    text = "Cancel",
                    onClick = onDismiss,
                    variant = KintsuButtonVariant.SECONDARY,
                    modifier = Modifier.weight(1f)
                )
                KintsuButton(
                    text = "Set Window",
                    onClick = {
                        onConfirm(computed24Hour, selectedMinute)
                    },
                    variant = KintsuButtonVariant.PRIMARY,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
