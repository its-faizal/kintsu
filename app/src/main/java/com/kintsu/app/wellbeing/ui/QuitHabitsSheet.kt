package com.kintsu.app.wellbeing.ui

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kintsu.app.data.model.QuitHabitEntity
import com.kintsu.app.data.model.UrgeLogEntity
import com.kintsu.app.ui.components.IconCheck
import com.kintsu.app.ui.components.IconFlame
import com.kintsu.app.ui.components.IconGoldSeam
import com.kintsu.app.ui.components.KintsuBottomSheet
import com.kintsu.app.ui.components.KintsuButton
import com.kintsu.app.ui.components.KintsuButtonVariant
import com.kintsu.app.ui.components.KintsuCard
import com.kintsu.app.ui.theme.KintsuTheme
import com.kintsu.app.wellbeing.domain.QuitHabitTracker
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuitHabitsSheet(
    quitHabits: List<QuitHabitEntity>,
    onAddQuitHabit: (name: String, ifThen: String, altActivities: String, contactName: String?, contactPhone: String?) -> Unit,
    onRecordSlip: (QuitHabitEntity) -> Unit,
    onRepairSlip: (QuitHabitEntity) -> Unit,
    onLogUrge: (quitHabitId: Long, intensity: Int, tags: String, note: String) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography
    val context = LocalContext.current

    var isCreating by remember { mutableStateOf(false) }
    var activeSosHabit by remember { mutableStateOf<QuitHabitEntity?>(null) }
    var loggingUrgeHabit by remember { mutableStateOf<QuitHabitEntity?>(null) }

    // Creation inputs
    var newName by remember { mutableStateOf("") }
    var newIfThen by remember { mutableStateOf("") }
    var newAltActivities by remember { mutableStateOf("Drink warm tea\n5-minute walk outside\n10 deep mindful breaths") }
    var newContactName by remember { mutableStateOf("") }
    var newContactPhone by remember { mutableStateOf("") }

    // Urge inputs
    var urgeIntensity by remember { mutableFloatStateOf(5f) }
    var urgeTags by remember { mutableStateOf("Boredom, Evening") }
    var urgeNote by remember { mutableStateOf("") }

    // SOS timer
    var sosRemainingSeconds by remember { mutableIntStateOf(600) } // 10 minutes
    LaunchedEffect(activeSosHabit) {
        if (activeSosHabit != null) {
            sosRemainingSeconds = 600
            while (sosRemainingSeconds > 0) {
                delay(1000)
                sosRemainingSeconds--
            }
        }
    }

    KintsuBottomSheet(
        onDismissRequest = onDismiss,
        title = "Break a Habit"
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.xl)
        ) {
            when {
                activeSosHabit != null -> {
                    // SOS Urge Surfing Screen
                    val habit = activeSosHabit!!
                    val mins = sosRemainingSeconds / 60
                    val secs = sosRemainingSeconds % 60

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Urge Surfing • ${habit.name}",
                            style = typography.screenTitle,
                            color = colors.goldSeam
                        )
                        Spacer(modifier = Modifier.height(spacing.xxs))
                        Text(
                            text = "Urges are like waves: they peak and pass within 10 minutes.",
                            style = typography.caption,
                            color = colors.textSecondary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(spacing.l))

                        Box(
                            modifier = Modifier
                                .size(140.dp)
                                .clip(CircleShape)
                                .background(colors.accentSoft.copy(alpha = 0.35f))
                                .border(1.5.dp, colors.goldSeam, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = String.format("%02d:%02d", mins, secs),
                                    style = typography.screenTitle.copy(fontSize = 28.sp),
                                    color = colors.goldSeam
                                )
                                Text(
                                    text = "Riding the wave",
                                    style = typography.caption,
                                    color = colors.textSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(spacing.l))

                        // If-Then plan
                        if (!habit.ifThenPlan.isNullOrBlank()) {
                            KintsuCard(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(spacing.m)) {
                                    Text(text = "Your If-Then Plan", style = typography.caption, color = colors.goldSeam)
                                    Spacer(modifier = Modifier.height(spacing.xxs))
                                    Text(text = habit.ifThenPlan, style = typography.body, color = colors.textPrimary)
                                }
                            }
                            Spacer(modifier = Modifier.height(spacing.m))
                        }

                        // Alternative activities
                        if (!habit.alternativeActivities.isNullOrBlank()) {
                            KintsuCard(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(spacing.m)) {
                                    Text(text = "Grounding Alternatives", style = typography.caption, color = colors.accent)
                                    Spacer(modifier = Modifier.height(spacing.xxs))
                                    habit.alternativeActivities.split("\n").filter { it.isNotBlank() }.forEach { act ->
                                        Text(text = "• ${act.trim()}", style = typography.body, color = colors.textPrimary)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(spacing.m))
                        }

                        // Trusted contact call/sms
                        if (!habit.emergencyContactPhone.isNullOrBlank()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(spacing.s)
                            ) {
                                KintsuButton(
                                    text = "Call ${habit.emergencyContactName ?: "Contact"}",
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${habit.emergencyContactPhone}"))
                                        context.startActivity(intent)
                                    },
                                    variant = KintsuButtonVariant.ACCENT_SOFT,
                                    modifier = Modifier.weight(1f)
                                )
                                KintsuButton(
                                    text = "Text Message",
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:${habit.emergencyContactPhone}")).apply {
                                            putExtra("sms_body", "Hi, I am surfing a craving right now and could use a quick chat.")
                                        }
                                        context.startActivity(intent)
                                    },
                                    variant = KintsuButtonVariant.SECONDARY,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Spacer(modifier = Modifier.height(spacing.m))
                        }

                        KintsuButton(
                            text = "Wave Passed • Reclaim Calm",
                            onClick = { activeSosHabit = null },
                            variant = KintsuButtonVariant.PRIMARY,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                loggingUrgeHabit != null -> {
                    // Quick Urge Logging
                    val habit = loggingUrgeHabit!!
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(text = "Log Urge • ${habit.name}", style = typography.screenTitle, color = colors.textPrimary)
                        Spacer(modifier = Modifier.height(spacing.xs))
                        Text(text = "Acknowledge the impulse without judging yourself.", style = typography.caption, color = colors.textSecondary)

                        Spacer(modifier = Modifier.height(spacing.l))

                        Text(text = "Intensity: ${urgeIntensity.toInt()}/10", style = typography.caption, color = colors.goldSeam)
                        Slider(
                            value = urgeIntensity,
                            onValueChange = { urgeIntensity = it },
                            valueRange = 1f..10f,
                            steps = 8,
                            colors = SliderDefaults.colors(
                                thumbColor = colors.goldSeam,
                                activeTrackColor = colors.goldSeam
                            )
                        )

                        Spacer(modifier = Modifier.height(spacing.m))

                        OutlinedTextField(
                            value = urgeTags,
                            onValueChange = { urgeTags = it },
                            label = { Text("Trigger Tags (e.g. Boredom, Late Night, Stress)") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colors.goldSeam,
                                unfocusedBorderColor = colors.border
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(spacing.m))

                        OutlinedTextField(
                            value = urgeNote,
                            onValueChange = { urgeNote = it },
                            label = { Text("Optional Reflection") },
                            maxLines = 3,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colors.goldSeam,
                                unfocusedBorderColor = colors.border
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(spacing.xl))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(spacing.s)
                        ) {
                            KintsuButton(
                                text = "Cancel",
                                onClick = { loggingUrgeHabit = null },
                                variant = KintsuButtonVariant.SECONDARY,
                                modifier = Modifier.weight(1f)
                            )
                            KintsuButton(
                                text = "Save Entry",
                                onClick = {
                                    onLogUrge(habit.id, urgeIntensity.toInt(), urgeTags, urgeNote)
                                    loggingUrgeHabit = null
                                },
                                variant = KintsuButtonVariant.PRIMARY,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                isCreating -> {
                    // Add Quit Habit Form
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(text = "Break a Habit", style = typography.screenTitle, color = colors.textPrimary)
                        Spacer(modifier = Modifier.height(spacing.xs))
                        Text(text = "Track days clean, surf urges, and repair slips with gold.", style = typography.caption, color = colors.textSecondary)

                        Spacer(modifier = Modifier.height(spacing.m))

                        OutlinedTextField(
                            value = newName,
                            onValueChange = { newName = it },
                            label = { Text("Habit to Break (e.g. Doomscrolling, Late Night Snacking)") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colors.goldSeam,
                                unfocusedBorderColor = colors.border
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(spacing.m))

                        OutlinedTextField(
                            value = newIfThen,
                            onValueChange = { newIfThen = it },
                            label = { Text("If-Then Plan (e.g. If I crave scrolling, I will read 2 pages)") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colors.goldSeam,
                                unfocusedBorderColor = colors.border
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(spacing.m))

                        OutlinedTextField(
                            value = newAltActivities,
                            onValueChange = { newAltActivities = it },
                            label = { Text("Alternative Activities (one per line)") },
                            maxLines = 3,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colors.goldSeam,
                                unfocusedBorderColor = colors.border
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(spacing.m))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(spacing.s)
                        ) {
                            OutlinedTextField(
                                value = newContactName,
                                onValueChange = { newContactName = it },
                                label = { Text("Trusted Name") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = newContactPhone,
                                onValueChange = { newContactPhone = it },
                                label = { Text("Phone Number") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(spacing.xl))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(spacing.s)
                        ) {
                            KintsuButton(
                                text = "Cancel",
                                onClick = { isCreating = false },
                                variant = KintsuButtonVariant.SECONDARY,
                                modifier = Modifier.weight(1f)
                            )
                            KintsuButton(
                                text = "Create",
                                onClick = {
                                    if (newName.isNotBlank()) {
                                        onAddQuitHabit(
                                            newName.trim(),
                                            newIfThen.trim(),
                                            newAltActivities.trim(),
                                            newContactName.ifBlank { null },
                                            newContactPhone.ifBlank { null }
                                        )
                                        isCreating = false
                                        newName = ""
                                    }
                                },
                                enabled = newName.isNotBlank(),
                                variant = KintsuButtonVariant.PRIMARY,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                else -> {
                    // Habits List Screen
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Break a Habit", style = typography.screenTitle, color = colors.textPrimary)
                            Text(text = "Gentle support for things you wish to stop", style = typography.caption, color = colors.textSecondary)
                        }
                        KintsuButton(
                            text = "+ New",
                            onClick = { isCreating = true },
                            variant = KintsuButtonVariant.ACCENT_SOFT
                        )
                    }

                    Spacer(modifier = Modifier.height(spacing.m))

                    if (quitHabits.isEmpty()) {
                        KintsuCard(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(spacing.l),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = "No Quit-Habits Active", style = typography.itemTitle, color = colors.textPrimary)
                                Spacer(modifier = Modifier.height(spacing.xs))
                                Text(
                                    text = "Ready to let go of an unwanted pattern? Tap '+ New' to create an intention with streak tracking and an emergency SOS toolkit.",
                                    style = typography.caption,
                                    color = colors.textSecondary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(spacing.m),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(quitHabits, key = { it.id }) { habit ->
                                val stats = QuitHabitTracker.calculateStats(habit)

                                KintsuCard(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(spacing.m)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(text = habit.name, style = typography.itemTitle, color = colors.textPrimary)
                                                Text(
                                                    text = "${stats.cleanDays}d ${stats.cleanHours}h clean • Best: ${habit.bestStreakDays}d",
                                                    style = typography.caption,
                                                    color = colors.goldSeam
                                                )
                                            }

                                            // SOS Urge Surfing Button
                                            KintsuButton(
                                                text = "SOS",
                                                onClick = { activeSosHabit = habit },
                                                variant = KintsuButtonVariant.ACCENT_SOFT
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(spacing.m))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(spacing.s)
                                        ) {
                                            KintsuButton(
                                                text = "Log Urge",
                                                onClick = { loggingUrgeHabit = habit },
                                                variant = KintsuButtonVariant.SECONDARY,
                                                modifier = Modifier.weight(1f)
                                            )

                                            if (stats.isEligibleForGoldRepair) {
                                                KintsuButton(
                                                    text = "Mend with Gold (${stats.repairWindowRemainingHours}h)",
                                                    onClick = { onRepairSlip(habit) },
                                                    variant = KintsuButtonVariant.ACCENT_SOFT,
                                                    modifier = Modifier.weight(1f)
                                                )
                                            } else {
                                                KintsuButton(
                                                    text = "Record Slip",
                                                    onClick = { onRecordSlip(habit) },
                                                    variant = KintsuButtonVariant.SECONDARY,
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(spacing.l))

                    // Mindful Disclaimer
                    Text(
                        text = QuitHabitTracker.DISCLAIMER_TEXT,
                        style = typography.caption.copy(fontSize = 11.sp),
                        color = colors.textTertiary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
