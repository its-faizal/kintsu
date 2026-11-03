package com.kintsu.app.ui.screens

import android.media.MediaPlayer
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kintsu.app.data.model.JournalCollectionEntity
import com.kintsu.app.data.model.JournalEntryEntity
import com.kintsu.app.ui.components.IconBook
import com.kintsu.app.ui.components.IconCalendar
import com.kintsu.app.ui.components.IconClose
import com.kintsu.app.ui.components.IconFavorite
import com.kintsu.app.ui.components.IconSearch
import com.kintsu.app.ui.components.IconTrash
import com.kintsu.app.ui.components.KintsuButton
import com.kintsu.app.ui.components.KintsuButtonVariant
import com.kintsu.app.ui.components.KintsuCard
import com.kintsu.app.ui.components.KintsuDivider
import com.kintsu.app.ui.components.KintsuFilterChip
import com.kintsu.app.ui.theme.KintsuTheme
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.YearMonth
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class JournalViewMode {
    TIMELINE,
    CALENDAR
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun JournalStudioSheet(
    allEntries: List<JournalEntryEntity>,
    collections: List<JournalCollectionEntity> = emptyList(),
    onDismiss: () -> Unit,
    onSaveEntry: (JournalEntryEntity) -> Unit,
    onDeleteEntry: (JournalEntryEntity) -> Unit,
    onToggleFavorite: (JournalEntryEntity) -> Unit,
    initialEditingEntry: JournalEntryEntity? = null,
    startInComposer: Boolean = false,
    prefilledPrompt: String? = null
) {
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography

    // Navigation state: whether the full-screen editor is active
    var isEditorOpen by remember { mutableStateOf(startInComposer || initialEditingEntry != null) }
    var activeEditingEntry by remember { mutableStateOf(initialEditingEntry) }

    // When Editor is active, present the dedicated full-screen editor
    if (isEditorOpen) {
        JournalEditorScreen(
            initialEntry = activeEditingEntry,
            prefilledPrompt = prefilledPrompt,
            collections = collections,
            onDismiss = {
                isEditorOpen = false
                activeEditingEntry = null
                if (startInComposer) {
                    onDismiss()
                }
            },
            onSaveEntry = { entry ->
                onSaveEntry(entry)
                isEditorOpen = false
                activeEditingEntry = null
                if (startInComposer) {
                    onDismiss()
                }
            },
            onDeleteEntry = { entry ->
                onDeleteEntry(entry)
                isEditorOpen = false
                activeEditingEntry = null
                if (startInComposer) {
                    onDismiss()
                }
            }
        )
        return
    }

    // --- JOURNAL HOME SCREEN ---
    var viewMode by remember { mutableStateOf(JournalViewMode.TIMELINE) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchExpanded by remember { mutableStateOf(false) }
    var selectedMoodFilter by remember { mutableStateOf<String?>(null) }
    var filterOnlyFavorites by remember { mutableStateOf(false) }
    var selectedCollectionFilter by remember { mutableStateOf<Long?>(null) }
    var showCollectionPicker by remember { mutableStateOf(false) }
    var entryToDelete by remember { mutableStateOf<JournalEntryEntity?>(null) }

    // Calendar states
    var currentYearMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedCalendarDate by remember { mutableStateOf(LocalDate.now()) }

    // Audio preview player
    var audioPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    DisposableEffect(Unit) {
        onDispose {
            try { audioPlayer?.release() } catch (_: Exception) {}
        }
    }

    // System Back Handler
    BackHandler {
        onDismiss()
    }

    // Time-based greeting (Screenshot 2: Moon icon + "Good evening")
    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 5..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            else -> "Good evening"
        }
    }
    // Emojis strictly removed

    // Filtered entries for Timeline
    val filteredTimelineEntries = remember(allEntries, searchQuery, selectedMoodFilter, filterOnlyFavorites, selectedCollectionFilter) {
        allEntries.filter { entry ->
            val matchesSearch = searchQuery.isBlank() ||
                (entry.title?.contains(searchQuery, ignoreCase = true) == true) ||
                entry.content.contains(searchQuery, ignoreCase = true) ||
                entry.tags.contains(searchQuery, ignoreCase = true)

            val matchesMood = selectedMoodFilter == null || entry.mood.equals(selectedMoodFilter, ignoreCase = true)
            val matchesFav = !filterOnlyFavorites || entry.isFavorite
            val matchesCollection = selectedCollectionFilter == null || entry.collectionId == selectedCollectionFilter

            matchesSearch && matchesMood && matchesFav && matchesCollection
        }.sortedByDescending { it.dateEpochDay }
    }

    // Grouping entries by Month-Year (Screenshot 2: "NOV 2026", "OCT 2026", etc.)
    val groupedEntriesByMonth = remember(filteredTimelineEntries) {
        val map = linkedMapOf<String, MutableList<JournalEntryEntity>>()
        filteredTimelineEntries.forEach { entry ->
            val date = LocalDate.ofEpochDay(entry.dateEpochDay)
            val monthKey = "${date.month.name.take(3)} ${date.year}"
            map.getOrPut(monthKey) { mutableListOf() }.add(entry)
        }
        map
    }

    // Entries for selected Calendar date
    val calendarDayEntries = remember(allEntries, selectedCalendarDate) {
        allEntries.filter { it.dateEpochDay == selectedCalendarDate.toEpochDay() }
    }

    val entryDatesSet = remember(allEntries) {
        allEntries.map { it.dateEpochDay }.toSet()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // --- 1. HEADER (Screenshot 2: Moon icon + "Good evening", Search icon & notebook icon, NO BACK ARROW) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.xl, vertical = spacing.m),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = greeting,
                        style = typography.screenTitle.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                        color = colors.textPrimary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.s)
                ) {
                    // Search toggle
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(colors.surfaceElevated.copy(alpha = 0.6f))
                            .clickable { isSearchExpanded = !isSearchExpanded },
                        contentAlignment = Alignment.Center
                    ) {
                        IconSearch(color = colors.textSecondary, size = 18.dp)
                    }

                    // Collection/Notebook filter icon
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(colors.surfaceElevated.copy(alpha = 0.6f))
                            .clickable { showCollectionPicker = !showCollectionPicker },
                        contentAlignment = Alignment.Center
                    ) {
                        IconBook(color = colors.textSecondary, size = 18.dp)
                    }
                }
            }

            // Search bar (collapsible)
            AnimatedVisibility(visible = isSearchExpanded) {
                Box(modifier = Modifier.padding(horizontal = spacing.xl, vertical = spacing.xs)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search reflections, titles, #tags…", color = colors.textTertiary) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                Text(
                                    text = "✕",
                                    color = colors.textSecondary,
                                    modifier = Modifier
                                        .clickable { searchQuery = "" }
                                        .padding(8.dp)
                                )
                            }
                        }
                    )
                }
            }

            // --- 2. NAVIGATION SEGMENTED SWITCHER (Timeline / Calendar) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.xl, vertical = spacing.xs)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.surfaceElevated.copy(alpha = 0.6f))
                    .border(1.dp, colors.border.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(9.dp))
                        .background(if (viewMode == JournalViewMode.TIMELINE) colors.accentSoft.copy(alpha = 0.85f) else Color.Transparent)
                        .clickable { viewMode = JournalViewMode.TIMELINE }
                        .padding(vertical = spacing.xs + 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Timeline",
                        style = typography.caption,
                        fontWeight = if (viewMode == JournalViewMode.TIMELINE) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (viewMode == JournalViewMode.TIMELINE) colors.textPrimary else colors.textSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(9.dp))
                        .background(if (viewMode == JournalViewMode.CALENDAR) colors.accentSoft.copy(alpha = 0.85f) else Color.Transparent)
                        .clickable { viewMode = JournalViewMode.CALENDAR }
                        .padding(vertical = spacing.xs + 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Calendar",
                        style = typography.caption,
                        fontWeight = if (viewMode == JournalViewMode.CALENDAR) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (viewMode == JournalViewMode.CALENDAR) colors.textPrimary else colors.textSecondary
                    )
                }
            }

            // --- 3. MAIN CONTENT: TIMELINE or CALENDAR ---
            if (viewMode == JournalViewMode.TIMELINE) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = spacing.xl),
                    verticalArrangement = Arrangement.spacedBy(spacing.m)
                ) {
                    if (filteredTimelineEntries.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = spacing.xl * 2),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(spacing.s)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(CircleShape)
                                            .background(colors.accentSoft),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        IconBook(color = colors.accent, size = 24.dp)
                                    }
                                    Text(
                                        text = if (allEntries.isEmpty()) "Your Journal is Quiet" else "No matching entries",
                                        style = typography.itemTitle,
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = if (allEntries.isEmpty()) "Tap the + button to begin your first conscious entry." else "Try adjusting your search terms or filters.",
                                        style = typography.caption,
                                        color = colors.textSecondary
                                    )
                                }
                            }
                        }
                    } else {
                        // Render Grouped by Month (Screenshot 2)
                        groupedEntriesByMonth.forEach { (monthLabel, monthEntries) ->
                            // Month Header: e.g. "NOV 2026"
                            item(key = "header_$monthLabel") {
                                Text(
                                    text = monthLabel.uppercase(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 2.sp,
                                    color = colors.textTertiary,
                                    modifier = Modifier.padding(top = spacing.m, bottom = spacing.xs)
                                )
                            }

                            // Entries in Month
                            items(monthEntries, key = { it.id }) { entry ->
                                ReferenceJournalCard(
                                    entry = entry,
                                    onClick = {
                                        activeEditingEntry = entry
                                        isEditorOpen = true
                                    },
                                    onToggleFavorite = { onToggleFavorite(entry) },
                                    onDelete = { entryToDelete = entry }
                                )
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            } else {
                // Calendar View
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = spacing.xl),
                    verticalArrangement = Arrangement.spacedBy(spacing.m)
                ) {
                    item {
                        CalendarMonthGrid(
                            currentYearMonth = currentYearMonth,
                            selectedDate = selectedCalendarDate,
                            entryDatesSet = entryDatesSet,
                            onMonthChange = { currentYearMonth = it },
                            onDateSelect = { selectedCalendarDate = it }
                        )
                    }

                    item {
                        Text(
                            text = "Entries for ${selectedCalendarDate.dayOfMonth} ${selectedCalendarDate.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }}",
                            style = typography.caption.copy(fontWeight = FontWeight.SemiBold),
                            color = colors.textSecondary
                        )
                    }

                    if (calendarDayEntries.isEmpty()) {
                        item {
                            Text(
                                text = "No entries recorded on this day.",
                                style = typography.caption,
                                color = colors.textTertiary,
                                modifier = Modifier.padding(vertical = spacing.s)
                            )
                        }
                    } else {
                        items(calendarDayEntries, key = { it.id }) { entry ->
                            ReferenceJournalCard(
                                entry = entry,
                                onClick = {
                                    activeEditingEntry = entry
                                    isEditorOpen = true
                                },
                                onToggleFavorite = { onToggleFavorite(entry) },
                                onDelete = { entryToDelete = entry }
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }

        // --- 4. FLOATING ACTION BUTTON '+ New Entry' (Anchored to Bottom-Right) ---
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = spacing.xl, bottom = 24.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(colors.accent)
                .clickable {
                    activeEditingEntry = null
                    isEditorOpen = true
                }
                .padding(horizontal = 20.dp, vertical = 13.dp)
                .semantics { role = Role.Button },
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "+",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "New Entry",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
    }

    // Delete Confirmation Dialog
    entryToDelete?.let { entry ->
        AlertDialog(
            onDismissRequest = { entryToDelete = null },
            title = {
                Text(
                    text = "Delete Journal Entry?",
                    style = typography.itemTitle,
                    color = colors.textPrimary
                )
            },
            text = {
                Text(
                    text = "This will permanently remove this reflection. This action cannot be undone.",
                    style = typography.secondary,
                    color = colors.textSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteEntry(entry)
                        entryToDelete = null
                    }
                ) {
                    Text("Delete", color = colors.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { entryToDelete = null }) {
                    Text("Cancel", color = colors.textPrimary)
                }
            },
            containerColor = colors.surfaceElevated,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

/**
 * Journal Card following Reference UI (Screenshot 2):
 * Day number + Day name (e.g. 02 Mon), Draft pill, Title, Content Snippet, Heart/Options.
 */
@Composable
fun ReferenceJournalCard(
    entry: JournalEntryEntity,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KintsuTheme.colors
    val typography = KintsuTheme.typography

    val date = remember(entry.dateEpochDay) { LocalDate.ofEpochDay(entry.dateEpochDay) }
    val dayNum = String.format(Locale.US, "%02d", date.dayOfMonth)
    val dayName = date.dayOfWeek.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
    val isDraft = entry.content.isBlank() || entry.title.isNullOrBlank()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(colors.surface).border(1.dp, colors.border.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Header Row: 02 Mon  [Draft]  Mood
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = dayNum,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = dayName,
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                    if (isDraft) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF5A4418))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Draft",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFC107)
                            )
                        }
                    }
                }

                // Mood Indicator
                entry.mood?.let { m ->
                    Text(text = m, fontSize = 11.sp, color = colors.goldSeam)
                }
            }

            // Title
            if (!entry.title.isNullOrBlank()) {
                Text(
                    text = entry.title ?: "",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
            }

            // Content Snippet
            if (entry.content.isNotBlank()) {
                Text(
                    text = entry.content,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = colors.textSecondary,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Bottom Actions: Heart (Favorite), Delete/Options
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Heart Icon
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .clickable { onToggleFavorite() },
                    contentAlignment = Alignment.Center
                ) {
                    IconFavorite(
                        color = if (entry.isFavorite) Color(0xFFFF5252) else Color(0xFF888890),
                        size = 16.dp
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Delete Icon
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .clickable { onDelete() },
                    contentAlignment = Alignment.Center
                ) {
                    IconTrash(color = colors.textTertiary, size = 15.dp)
                }
            }
        }
    }
}

/**
 * Calendar Month Grid Component
 */
@Composable
fun CalendarMonthGrid(
    currentYearMonth: YearMonth,
    selectedDate: LocalDate,
    entryDatesSet: Set<Long>,
    onMonthChange: (YearMonth) -> Unit,
    onDateSelect: (LocalDate) -> Unit
) {
    val colors = KintsuTheme.colors
    val typography = KintsuTheme.typography

    val daysInMonth = currentYearMonth.lengthOfMonth()
    val firstDayOfWeek = currentYearMonth.atDay(1).dayOfWeek.value % 7

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface).border(1.dp, colors.border.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
            .padding(14.dp)
    ) {
        // Month Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${currentYearMonth.month.name.take(3)} ${currentYearMonth.year}",
                style = typography.itemTitle,
                color = colors.textPrimary
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "◀",
                    color = colors.textSecondary,
                    modifier = Modifier
                        .clickable { onMonthChange(currentYearMonth.minusMonths(1)) }
                        .padding(4.dp)
                )
                Text(
                    text = "▶",
                    color = colors.textSecondary,
                    modifier = Modifier
                        .clickable { onMonthChange(currentYearMonth.plusMonths(1)) }
                        .padding(4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Weekday headers
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            listOf("S", "M", "T", "W", "T", "F", "S").forEach { d ->
                Text(text = d, fontSize = 11.sp, color = colors.textTertiary)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Days
        val totalCells = ((firstDayOfWeek + daysInMonth + 6) / 7) * 7
        for (row in 0 until (totalCells / 7)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                for (col in 0 until 7) {
                    val cellIndex = row * 7 + col
                    val dayNum = cellIndex - firstDayOfWeek + 1
                    if (dayNum in 1..daysInMonth) {
                        val cellDate = currentYearMonth.atDay(dayNum)
                        val isSelected = cellDate == selectedDate
                        val hasEntry = entryDatesSet.contains(cellDate.toEpochDay())

                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) colors.goldSeam else Color.Transparent)
                                .clickable { onDateSelect(cellDate) },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$dayNum",
                                    fontSize = 12.sp,
                                    color = if (isSelected) Color.Black else colors.textPrimary,
                                    fontWeight = if (isSelected || hasEntry) FontWeight.Bold else FontWeight.Normal
                                )
                                if (hasEntry && !isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(4.dp)
                                            .clip(CircleShape)
                                            .background(colors.accent)
                                    )
                                }
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.size(34.dp))
                    }
                }
            }
        }
    }
}
