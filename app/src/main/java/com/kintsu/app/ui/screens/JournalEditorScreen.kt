package com.kintsu.app.ui.screens

import android.Manifest
import android.app.DatePickerDialog
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.kintsu.app.data.model.JournalCollectionEntity
import com.kintsu.app.data.model.JournalEntryEntity
import com.kintsu.app.ui.components.IconAlignCenter
import com.kintsu.app.ui.components.IconAlignLeft
import com.kintsu.app.ui.components.IconAlignRight
import com.kintsu.app.ui.components.IconBack
import com.kintsu.app.ui.components.IconClose
import com.kintsu.app.ui.components.IconClock
import com.kintsu.app.ui.components.KintsuFilterChip
import com.kintsu.app.ui.theme.KintsuTheme
import kotlinx.coroutines.delay
import java.io.File
import java.time.LocalDate
import java.util.Locale

data class RichStyleSpan(
    val start: Int,
    val end: Int,
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val colorHex: String? = null,
    val highlightHex: String? = null,
    val fontSizeSp: Int? = null
)

fun isColorLight(hex: String): Boolean {
    return try {
        val colorInt = android.graphics.Color.parseColor(hex)
        val r = android.graphics.Color.red(colorInt) / 255.0
        val g = android.graphics.Color.green(colorInt) / 255.0
        val b = android.graphics.Color.blue(colorInt) / 255.0
        val luminance = 0.299 * r + 0.587 * g + 0.114 * b
        luminance > 0.5
    } catch (_: Exception) {
        false
    }
}

fun colorToHex(color: Color): String {
    val argb = color.toArgb()
    return String.format("#%06X", 0xFFFFFF and argb)
}

fun parseMarkdownToRich(md: String): Pair<String, List<RichStyleSpan>> {
    val spans = mutableListOf<RichStyleSpan>()
    val sb = StringBuilder()
    var i = 0
    while (i < md.length) {
        if (i + 1 < md.length && md[i] == '*' && md[i + 1] == '*') {
            val endIdx = md.indexOf("**", i + 2)
            if (endIdx != -1) {
                val textSegment = md.substring(i + 2, endIdx)
                val start = sb.length
                sb.append(textSegment)
                val end = sb.length
                spans.add(RichStyleSpan(start = start, end = end, isBold = true))
                i = endIdx + 2
                continue
            }
        } else if (md[i] == '*' && (i + 1 >= md.length || md[i + 1] != '*')) {
            val endIdx = md.indexOf("*", i + 1)
            if (endIdx != -1 && (endIdx + 1 >= md.length || md[endIdx + 1] != '*')) {
                val textSegment = md.substring(i + 1, endIdx)
                val start = sb.length
                sb.append(textSegment)
                val end = sb.length
                spans.add(RichStyleSpan(start = start, end = end, isItalic = true))
                i = endIdx + 1
                continue
            }
        }
        sb.append(md[i])
        i++
    }
    return sb.toString() to spans
}

fun serializeRichToMarkdown(plain: String, spans: List<RichStyleSpan>): String {
    if (spans.isEmpty()) return plain
    val markers = mutableListOf<Triple<Int, String, Boolean>>()
    spans.forEach { span ->
        val s = span.start.coerceIn(0, plain.length)
        val e = span.end.coerceIn(0, plain.length)
        if (s < e) {
            if (span.isBold) {
                markers.add(Triple(s, "**", true))
                markers.add(Triple(e, "**", false))
            }
            if (span.isItalic) {
                markers.add(Triple(s, "*", true))
                markers.add(Triple(e, "*", false))
            }
        }
    }
    markers.sortWith(compareByDescending<Triple<Int, String, Boolean>> { it.first }.thenBy { if (it.third) 1 else 0 })
    val sb = StringBuilder(plain)
    for ((pos, marker, _) in markers) {
        val clamped = pos.coerceIn(0, sb.length)
        sb.insert(clamped, marker)
    }
    return sb.toString()
}

// Vector list icons with no text labels
@Composable
fun IconBulletList(color: Color, size: Dp = 18.dp, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(size)) {
        val stroke = 1.8.dp.toPx()
        val dotRadius = 1.6.dp.toPx()
        val y1 = this.size.height * 0.22f
        val y2 = this.size.height * 0.5f
        val y3 = this.size.height * 0.78f
        val dotX = this.size.width * 0.16f
        val lineStartX = this.size.width * 0.38f
        val lineEndX = this.size.width * 0.92f

        drawCircle(color, dotRadius, Offset(dotX, y1))
        drawCircle(color, dotRadius, Offset(dotX, y2))
        drawCircle(color, dotRadius, Offset(dotX, y3))

        drawLine(color, Offset(lineStartX, y1), Offset(lineEndX, y1), stroke, StrokeCap.Round)
        drawLine(color, Offset(lineStartX, y2), Offset(lineEndX, y2), stroke, StrokeCap.Round)
        drawLine(color, Offset(lineStartX, y3), Offset(lineEndX, y3), stroke, StrokeCap.Round)
    }
}

@Composable
fun IconNumberList(color: Color, size: Dp = 18.dp, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(size)) {
        val stroke = 1.8.dp.toPx()
        val y1 = this.size.height * 0.22f
        val y2 = this.size.height * 0.5f
        val y3 = this.size.height * 0.78f
        val lineStartX = this.size.width * 0.44f
        val lineEndX = this.size.width * 0.92f

        // '1'
        drawLine(color, Offset(this.size.width * 0.18f, y1 - 3.dp.toPx()), Offset(this.size.width * 0.18f, y1 + 3.dp.toPx()), stroke * 0.9f, StrokeCap.Round)
        // '2'
        drawLine(color, Offset(this.size.width * 0.12f, y2 - 2.5.dp.toPx()), Offset(this.size.width * 0.24f, y2 - 2.5.dp.toPx()), stroke * 0.8f, StrokeCap.Round)
        drawLine(color, Offset(this.size.width * 0.12f, y2 + 2.5.dp.toPx()), Offset(this.size.width * 0.24f, y2 + 2.5.dp.toPx()), stroke * 0.8f, StrokeCap.Round)
        // '3'
        drawLine(color, Offset(this.size.width * 0.12f, y3 - 2.8.dp.toPx()), Offset(this.size.width * 0.24f, y3 - 2.8.dp.toPx()), stroke * 0.8f, StrokeCap.Round)
        drawLine(color, Offset(this.size.width * 0.16f, y3), Offset(this.size.width * 0.24f, y3), stroke * 0.8f, StrokeCap.Round)
        drawLine(color, Offset(this.size.width * 0.12f, y3 + 2.8.dp.toPx()), Offset(this.size.width * 0.24f, y3 + 2.8.dp.toPx()), stroke * 0.8f, StrokeCap.Round)

        // Lines
        drawLine(color, Offset(lineStartX, y1), Offset(lineEndX, y1), stroke, StrokeCap.Round)
        drawLine(color, Offset(lineStartX, y2), Offset(lineEndX, y2), stroke, StrokeCap.Round)
        drawLine(color, Offset(lineStartX, y3), Offset(lineEndX, y3), stroke, StrokeCap.Round)
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun JournalEditorScreen(
    initialEntry: JournalEntryEntity? = null,
    prefilledPrompt: String? = null,
    collections: List<JournalCollectionEntity> = emptyList(),
    onDismiss: () -> Unit,
    onSaveEntry: (JournalEntryEntity) -> Unit,
    onDeleteEntry: ((JournalEntryEntity) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography

    // 1. Initial Content Parsing
    val initialRawContent = when {
        initialEntry != null -> initialEntry.content
        !prefilledPrompt.isNullOrBlank() -> prefilledPrompt
        else -> ""
    }
    val (parsedInitialText, parsedInitialSpans) = remember(initialRawContent) {
        parseMarkdownToRich(initialRawContent)
    }

    var title by remember { mutableStateOf(initialEntry?.title ?: "") }
    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(parsedInitialText, TextRange(parsedInitialText.length)))
    }
    val spans = remember { mutableStateListOf<RichStyleSpan>().apply { addAll(parsedInitialSpans) } }

    // Revert / Undo Stack: stores snapshots of (TextFieldValue, List<RichStyleSpan>)
    val undoStack = remember { mutableStateListOf<Pair<TextFieldValue, List<RichStyleSpan>>>() }

    fun pushUndoSnapshot() {
        if (undoStack.size > 50) {
            undoStack.removeAt(0)
        }
        undoStack.add(Pair(textFieldValue, spans.toList()))
    }

    var selectedMood by remember { mutableStateOf(initialEntry?.mood) }
    var selectedWeather by remember { mutableStateOf<String?>(null) }
    var selectedAlignment by remember { mutableStateOf(TextAlign.Start) }

    // Custom Tags State
    val initialTags = remember(initialEntry) {
        initialEntry?.tags?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() } ?: emptyList()
    }
    val selectedTags = remember { mutableStateListOf<String>().apply { addAll(initialTags) } }
    var showAddTagDialog by remember { mutableStateOf(false) }
    var newTagInput by remember { mutableStateOf("") }

    // Date & Time (Clickable for previous date/time selection)
    var entryEpochDay by remember {
        mutableStateOf(initialEntry?.dateEpochDay ?: LocalDate.now().toEpochDay())
    }
    val localDate = remember(entryEpochDay) { LocalDate.ofEpochDay(entryEpochDay) }
    val dayNum = localDate.dayOfMonth.toString()
    val dayOfWeek = localDate.dayOfWeek.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
    val yearMonthStr = "${localDate.year}.${String.format(Locale.US, "%02d", localDate.monthValue)}"

    // Dialogs & Sheets
    var showReadyToLeaveDialog by remember { mutableStateOf(false) }
    var isDiscardConfirmed by remember { mutableStateOf(false) }
    var showTextSheet by remember { mutableStateOf(false) }
    var showHighlightSheet by remember { mutableStateOf(false) }
    var showMoodSheet by remember { mutableStateOf(false) }
    var showWeatherSheet by remember { mutableStateOf(false) }
    var showAttachMenu by remember { mutableStateOf(false) }
    var showMemoInputDialog by remember { mutableStateOf(false) }
    var showListMenu by remember { mutableStateOf(false) }
    var memoInputText by remember { mutableStateOf("") }
    var isDirty by remember { mutableStateOf(false) }

    // Highlight text color override in highlight sheet
    var highlightTextColorOverride by remember { mutableStateOf<String?>(null) }

    // Persistent / Default typing styles (set via text editor menu)
    var defaultBold by remember { mutableStateOf(false) }
    var defaultItalic by remember { mutableStateOf(false) }
    var defaultColorHex by remember { mutableStateOf<String?>(null) }
    var defaultHighlightHex by remember { mutableStateOf<String?>(null) }
    var defaultFontSizeSp by remember { mutableStateOf<Int?>(null) }

    // Media & Voice
    val attachedMedia = remember {
        mutableStateListOf<String>().apply {
            initialEntry?.photoUris?.split(",")?.filter { it.isNotBlank() && !it.startsWith("AUD:") }?.let { addAll(it) }
        }
    }
    var attachedAudioPath by remember {
        mutableStateOf(initialEntry?.photoUris?.split(",")?.firstOrNull { it.startsWith("AUD:") }?.removePrefix("AUD:"))
    }
    var isRecordingAudio by remember { mutableStateOf(false) }
    var recordDurationSeconds by remember { mutableIntStateOf(0) }
    var mediaRecorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var audioFileCandidate by remember { mutableStateOf<File?>(null) }

    // Audio Preview Player
    var isPlayingAudio by remember { mutableStateOf(false) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    // Save helpers
    fun buildCurrentEntry(): JournalEntryEntity {
        val serializedContent = serializeRichToMarkdown(textFieldValue.text, spans)
        val allAttachedUris = attachedMedia.toMutableList().apply {
            attachedAudioPath?.let { add("AUD:$it") }
        }.joinToString(",")

        return JournalEntryEntity(
            id = initialEntry?.id ?: 0L,
            dateEpochDay = entryEpochDay,
            title = title.trim().ifBlank { null },
            content = serializedContent,
            mood = selectedMood,
            tags = selectedTags.joinToString(","),
            photoUris = allAttachedUris,
            collectionId = initialEntry?.collectionId ?: 1L,
            isFavorite = initialEntry?.isFavorite ?: false,
            createdAt = initialEntry?.createdAt ?: System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }

    // Prevent double saving upon exit
    var hasBeenSaved by remember { mutableStateOf(false) }

    // Auto-save on exit without verification (only if NOT explicitly saved already)
    DisposableEffect(Unit) {
        onDispose {
            try {
                mediaRecorder?.release()
                mediaPlayer?.release()
            } catch (_: Exception) {}

            if (!hasBeenSaved && !isDiscardConfirmed && isDirty && (title.isNotBlank() || textFieldValue.text.isNotBlank())) {
                try {
                    hasBeenSaved = true
                    onSaveEntry(buildCurrentEntry())
                } catch (_: Exception) {}
            }
        }
    }

    LaunchedEffect(isRecordingAudio) {
        if (isRecordingAudio) {
            recordDurationSeconds = 0
            while (isRecordingAudio) {
                delay(1000L)
                recordDurationSeconds++
                if (recordDurationSeconds >= 120) {
                    try {
                        mediaRecorder?.stop()
                        mediaRecorder?.release()
                        mediaRecorder = null
                        audioFileCandidate?.let {
                            attachedAudioPath = it.absolutePath
                            isDirty = true
                        }
                    } catch (_: Exception) {}
                    isRecordingAudio = false
                    break
                }
            }
        }
    }

    // Activity Launchers for Attachments
    val photoPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            attachedMedia.add("IMG:$it")
            isDirty = true
        }
    }
    val videoPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            attachedMedia.add("VID:$it")
            isDirty = true
        }
    }
    val micPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            startAudioRecording(context) { rec, file ->
                mediaRecorder = rec
                audioFileCandidate = file
                isRecordingAudio = true
                isDirty = true
            }
        } else {
            Toast.makeText(context, "Microphone permission required for voice notes", Toast.LENGTH_SHORT).show()
        }
    }

    // Explicit Save and Exit
    fun saveAndExit() {
        if (hasBeenSaved) return
        hasBeenSaved = true
        isDirty = false
        onSaveEntry(buildCurrentEntry())
        onDismiss()
    }

    // Back navigation handler: prompt "Ready to leave?"
    fun handleBackPress() {
        if (isRecordingAudio) {
            try { mediaRecorder?.stop(); mediaRecorder?.release() } catch (_: Exception) {}
            isRecordingAudio = false
        }
        if (isPlayingAudio) {
            try { mediaPlayer?.stop(); mediaPlayer?.release() } catch (_: Exception) {}
            isPlayingAudio = false
        }

        val hasContent = title.isNotBlank() || textFieldValue.text.isNotBlank() || attachedMedia.isNotEmpty()
        if (hasContent) {
            showReadyToLeaveDialog = true
        } else {
            isDiscardConfirmed = true
            onDismiss()
        }
    }

    BackHandler {
        handleBackPress()
    }

    // UPDATE SPANS ON TEXT EDIT: removes effects when styled text is deleted & prevents new characters from inheriting deleted effects
    fun updateSpansOnTextChange(oldText: String, newText: String, cursor: Int) {
        if (oldText == newText) return
        val delta = newText.length - oldText.length

        // Case 1: Text was deleted
        if (delta < 0) {
            val deleteStart = cursor
            val deleteEnd = cursor - delta

            val updated = mutableListOf<RichStyleSpan>()
            for (span in spans) {
                when {
                    // Span is strictly after deletion: shift left
                    span.start >= deleteEnd -> {
                        updated.add(span.copy(start = span.start + delta, end = span.end + delta))
                    }
                    // Span is strictly before deletion: keep
                    span.end <= deleteStart -> {
                        updated.add(span)
                    }
                    // Span overlaps with deletion: shrink or remove
                    else -> {
                        val newStart = minOf(span.start, deleteStart)
                        val newEnd = maxOf(newStart, span.end + delta)
                        if (newEnd > newStart) {
                            updated.add(span.copy(start = newStart, end = newEnd))
                        }
                    }
                }
            }
            spans.clear()
            spans.addAll(updated)
            return
        }

        // Case 2: Text was inserted (delta > 0)
        // Newly typed characters do NOT inherit deleted or boundary effects
        val insertPos = cursor - delta
        val updated = mutableListOf<RichStyleSpan>()
        for (span in spans) {
            when {
                span.start >= insertPos -> {
                    updated.add(span.copy(start = span.start + delta, end = span.end + delta))
                }
                span.end <= insertPos -> {
                    updated.add(span)
                }
                else -> {
                    // Splitting/stopping so typed text does not inherit effect
                    updated.add(span.copy(end = insertPos))
                    if (span.end > insertPos) {
                        updated.add(span.copy(start = insertPos + delta, end = span.end + delta))
                    }
                }
            }
        }

        // Apply active default/sticky styles if set via text editor menu
        val hasDefaultStyle = defaultBold || defaultItalic || defaultColorHex != null || defaultHighlightHex != null || (defaultFontSizeSp != null && defaultFontSizeSp != 16)
        if (hasDefaultStyle) {
            updated.add(
                RichStyleSpan(
                    start = insertPos,
                    end = insertPos + delta,
                    isBold = defaultBold,
                    isItalic = defaultItalic,
                    colorHex = defaultColorHex,
                    highlightHex = defaultHighlightHex,
                    fontSizeSp = defaultFontSizeSp
                )
            )
        }

        spans.clear()
        spans.addAll(updated)
    }

    // Continuous bullet list and number list on enter
    fun handleTextChange(newValue: TextFieldValue) {
        val oldText = textFieldValue.text
        val newText = newValue.text

        pushUndoSnapshot()

        // Check if Enter was pressed
        if (newText.length == oldText.length + 1 && newValue.selection.start == newValue.selection.end) {
            val cursor = newValue.selection.start
            if (cursor > 0 && newText[cursor - 1] == '\n') {
                val prevLineEnd = cursor - 1
                val prevLineStart = newText.lastIndexOf('\n', (prevLineEnd - 1).coerceAtLeast(0)).let {
                    if (it == -1) 0 else it + 1
                }
                val prevLine = newText.substring(prevLineStart, prevLineEnd)

                // 1. Bullet list continuation: "• "
                if (prevLine.startsWith("• ")) {
                    if (prevLine.trim() == "•") {
                        val cleaned = newText.substring(0, prevLineStart) + newText.substring(cursor)
                        updateSpansOnTextChange(oldText, cleaned, prevLineStart)
                        textFieldValue = TextFieldValue(cleaned, TextRange(prevLineStart))
                        return
                    } else {
                        val continuation = "• "
                        val adjusted = newText.substring(0, cursor) + continuation + newText.substring(cursor)
                        updateSpansOnTextChange(oldText, adjusted, cursor + continuation.length)
                        textFieldValue = TextFieldValue(adjusted, TextRange(cursor + continuation.length))
                        isDirty = true
                        return
                    }
                }

                // 2. Numbered list continuation: "1. ", "2. ", etc.
                val numMatch = Regex("^([0-9]+)\\.\\s").find(prevLine)
                if (numMatch != null) {
                    if (prevLine.trim() == numMatch.value.trim()) {
                        val cleaned = newText.substring(0, prevLineStart) + newText.substring(cursor)
                        updateSpansOnTextChange(oldText, cleaned, prevLineStart)
                        textFieldValue = TextFieldValue(cleaned, TextRange(prevLineStart))
                        return
                    } else {
                        val currentNum = numMatch.groupValues[1].toIntOrNull() ?: 1
                        val continuation = "${currentNum + 1}. "
                        val adjusted = newText.substring(0, cursor) + continuation + newText.substring(cursor)
                        updateSpansOnTextChange(oldText, adjusted, cursor + continuation.length)
                        textFieldValue = TextFieldValue(adjusted, TextRange(cursor + continuation.length))
                        isDirty = true
                        return
                    }
                }
            }
        }

        updateSpansOnTextChange(oldText, newText, newValue.selection.start)
        textFieldValue = newValue
        isDirty = true
    }

    // Insert bullet list directly at cursor
    fun insertBulletList() {
        pushUndoSnapshot()
        val cur = textFieldValue.selection.start
        val text = textFieldValue.text
        val lineStart = text.lastIndexOf('\n', (cur - 1).coerceAtLeast(0)).let { if (it == -1) 0 else it + 1 }
        val newText = text.substring(0, lineStart) + "• " + text.substring(lineStart)
        textFieldValue = TextFieldValue(newText, TextRange(cur + 2))
        isDirty = true
    }

    // Insert numbered list directly at cursor
    fun insertNumberList() {
        pushUndoSnapshot()
        val cur = textFieldValue.selection.start
        val text = textFieldValue.text
        val lineStart = text.lastIndexOf('\n', (cur - 1).coerceAtLeast(0)).let { if (it == -1) 0 else it + 1 }
        val newText = text.substring(0, lineStart) + "1. " + text.substring(lineStart)
        textFieldValue = TextFieldValue(newText, TextRange(cur + 3))
        isDirty = true
    }

    // Formatting strictly on selected text range with toggle-off (unstyle) capability
    fun applyFormattingToSelection(
        bold: Boolean? = null,
        italic: Boolean? = null,
        colorHex: String? = null,
        highlightHex: String? = null,
        fontSizeSp: Int? = null,
        explicitTextColor: String? = null,
        clearColor: Boolean = false,
        clearHighlight: Boolean = false
    ) {
        val sel = textFieldValue.selection
        val start = minOf(sel.start, sel.end)
        val end = maxOf(sel.start, sel.end)
        if (start < end) {
            pushUndoSnapshot()
            val existing = spans.firstOrNull { it.start == start && it.end == end }
            spans.removeAll { it.start >= start && it.end <= end }

            val newBold = if (bold != null) bold else (existing?.isBold ?: false)
            val newItalic = if (italic != null) italic else (existing?.isItalic ?: false)
            val newHighlight = if (clearHighlight) null else (highlightHex ?: existing?.highlightHex)

            val finalTextColor = if (clearColor) null else (explicitTextColor ?: if (newHighlight != null) {
                if (isColorLight(newHighlight)) "#1A1A1A" else "#FFFFFF"
            } else {
                colorHex ?: existing?.colorHex
            })

            val finalFontSize = fontSizeSp ?: existing?.fontSizeSp

            val hasStyle = newBold || newItalic || finalTextColor != null || newHighlight != null || (finalFontSize != null && finalFontSize != 16)
            if (hasStyle) {
                spans.add(
                    RichStyleSpan(
                        start = start,
                        end = end,
                        isBold = newBold,
                        isItalic = newItalic,
                        colorHex = finalTextColor,
                        highlightHex = newHighlight,
                        fontSizeSp = finalFontSize
                    )
                )
            }
            isDirty = true
        } else {
            // No selection -> toggle default mode
            if (bold != null) defaultBold = bold
            if (italic != null) defaultItalic = italic
            if (fontSizeSp != null) defaultFontSizeSp = if (defaultFontSizeSp == fontSizeSp) null else fontSizeSp
            if (colorHex != null) defaultColorHex = if (defaultColorHex.equals(colorHex, ignoreCase = true)) null else colorHex
        }
    }

    // Live Visual Transformation for BasicTextField
    val richVisualTransformation = remember(spans.toList(), colors) {
        VisualTransformation { text ->
            val raw = text.text
            val builder = AnnotatedString.Builder(raw)

            spans.forEach { span ->
                val start = span.start.coerceIn(0, raw.length)
                val end = span.end.coerceIn(0, raw.length)
                if (start < end) {
                    val spanColor = span.colorHex?.let { Color(android.graphics.Color.parseColor(it)) } ?: colors.textPrimary
                    val spanBg = span.highlightHex?.let { Color(android.graphics.Color.parseColor(it)) } ?: Color.Transparent
                    val spanWeight = if (span.isBold) FontWeight.Bold else FontWeight.Normal
                    val spanStyle = if (span.isItalic) FontStyle.Italic else FontStyle.Normal
                    val spanSize = span.fontSizeSp?.sp ?: 16.sp

                    builder.addStyle(
                        SpanStyle(
                            color = spanColor,
                            background = spanBg,
                            fontWeight = spanWeight,
                            fontStyle = spanStyle,
                            fontSize = spanSize
                        ),
                        start,
                        end
                    )
                }
            }

            TransformedText(builder.toAnnotatedString(), OffsetMapping.Identity)
        }
    }

    // Native Date Picker for past dates
    fun showNativeDatePicker() {
        val cur = LocalDate.ofEpochDay(entryEpochDay)
        val dialog = DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val picked = LocalDate.of(year, month + 1, dayOfMonth)
                entryEpochDay = picked.toEpochDay()
                isDirty = true
            },
            cur.year,
            cur.monthValue - 1,
            cur.dayOfMonth
        )
        dialog.datePicker.maxDate = System.currentTimeMillis()
        dialog.show()
    }

    // Layout Root
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        // --- 1. HEADER (TIME on Left, SAVE on Right, ZERO BACK/X BUTTON) ---
        val timeFormatter = remember { java.time.format.DateTimeFormatter.ofPattern("HH:mm") }
        val currentTimeStr = remember { java.time.LocalTime.now().format(timeFormatter) }
        var entryTimeStr by remember { mutableStateOf(currentTimeStr) }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.l, vertical = spacing.s),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Time Display at top-left (Replaces back button, tap to adjust if desired)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        val now = java.time.LocalTime.now()
                        android.app.TimePickerDialog(
                            context,
                            { _, h, m ->
                                entryTimeStr = String.format(Locale.US, "%02d:%02d", h, m)
                                isDirty = true
                            },
                            now.hour,
                            now.minute,
                            true
                        ).show()
                    }
                    .padding(vertical = 4.dp, horizontal = 2.dp)
            ) {
                IconClock(color = colors.textSecondary, size = 16.dp)
                Text(
                    text = entryTimeStr,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary,
                    letterSpacing = 0.5.sp
                )
            }

            // Save Action Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.accent)
                    .clickable { saveAndExit() }
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .semantics { role = Role.Button },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Save",
                    style = typography.caption.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                )
            }
        }

        // --- 2. WRITING CANVAS ---
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = spacing.l)
                .widthIn(max = 768.dp)
        ) {
            Spacer(modifier = Modifier.height(spacing.xs))

            // Date & Time Display (Clean, clickable, WITHOUT "Tap to change date" helper text)
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { showNativeDatePicker() }
                    .padding(vertical = spacing.xs)
            ) {
                Text(
                    text = dayNum,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                    letterSpacing = (-1).sp
                )
                Column(modifier = Modifier.padding(bottom = 4.dp)) {
                    Text(
                        text = dayOfWeek,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textSecondary
                    )
                    Text(
                        text = yearMonthStr,
                        fontSize = 11.sp,
                        color = colors.textTertiary
                    )
                }
            }

            Spacer(modifier = Modifier.height(spacing.s))

            // Title Field
            BasicTextField(
                value = title,
                onValueChange = {
                    title = it
                    isDirty = true
                },
                textStyle = typography.screenTitle.copy(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                ),
                cursorBrush = SolidColor(colors.accent),
                singleLine = true,
                decorationBox = { innerTextField ->
                    if (title.isEmpty()) {
                        Text(
                            text = "Title",
                            style = typography.screenTitle.copy(
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textTertiary.copy(alpha = 0.5f)
                            )
                        )
                    }
                    innerTextField()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = spacing.s)
            )

            // Multiline Spacious Body Editor
            BasicTextField(
                value = textFieldValue,
                onValueChange = { handleTextChange(it) },
                visualTransformation = richVisualTransformation,
                textStyle = typography.body.copy(
                    fontSize = 16.sp,
                    lineHeight = 26.sp,
                    color = colors.textPrimary,
                    textAlign = selectedAlignment
                ),
                cursorBrush = SolidColor(colors.accent),
                decorationBox = { innerTextField ->
                    if (textFieldValue.text.isEmpty()) {
                        Text(
                            text = "Write something...",
                            style = typography.body.copy(
                                fontSize = 16.sp,
                                lineHeight = 26.sp,
                                color = colors.textTertiary.copy(alpha = 0.45f)
                            )
                        )
                    }
                    innerTextField()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            )

            Spacer(modifier = Modifier.height(spacing.s))
        }

        // --- 3. CONTEXTUAL FLOATING QUICK FORMATTING (Windows-style when text is selected) ---
        val isTextSelected = textFieldValue.selection.start != textFieldValue.selection.end
        AnimatedVisibility(
            visible = isTextSelected,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.l, vertical = 4.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.surfaceElevated)
                        .border(1.dp, colors.border, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "B",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = colors.textPrimary,
                            modifier = Modifier
                                .clickable { applyFormattingToSelection(bold = true) }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                        Text(
                            text = "I",
                            fontStyle = FontStyle.Italic,
                            fontSize = 15.sp,
                            color = colors.textPrimary,
                            modifier = Modifier
                                .clickable { applyFormattingToSelection(italic = true) }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                        Text(
                            text = "Highlight",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.goldSeam,
                            modifier = Modifier
                                .clickable { showHighlightSheet = true }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                        Text(
                            text = "Color",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.accent,
                            modifier = Modifier
                                .clickable { showTextSheet = true }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        // --- 4. ATTACHMENT PREVIEW STRIP (Placed next to/above Attach, NOT in text) ---
        if (attachedMedia.isNotEmpty() || attachedAudioPath != null) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.l, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Voice note item
                attachedAudioPath?.let { path ->
                    item {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(colors.surfaceElevated)
                                .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = if (isPlayingAudio) "Stop Voice" else "Play Voice",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.accent,
                                    modifier = Modifier.clickable {
                                        if (isPlayingAudio) {
                                            mediaPlayer?.stop()
                                            isPlayingAudio = false
                                        } else {
                                            try {
                                                mediaPlayer = MediaPlayer().apply {
                                                    setDataSource(path)
                                                    prepare()
                                                    start()
                                                    setOnCompletionListener { isPlayingAudio = false }
                                                }
                                                isPlayingAudio = true
                                            } catch (_: Exception) {}
                                        }
                                    }
                                )
                                Text(
                                    text = "x",
                                    fontSize = 11.sp,
                                    color = colors.textTertiary,
                                    modifier = Modifier.clickable {
                                        mediaPlayer?.stop()
                                        isPlayingAudio = false
                                        attachedAudioPath = null
                                        isDirty = true
                                    }
                                )
                            }
                        }
                    }
                }

                // Media items (Photo, Video, Memo)
                items(attachedMedia) { item ->
                    val typeLabel = when {
                        item.startsWith("IMG:") -> "Photo"
                        item.startsWith("VID:") -> "Video"
                        item.startsWith("MEMO:") -> "Memo: ${item.removePrefix("MEMO:").take(12)}..."
                        else -> "Attachment"
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.surfaceElevated)
                            .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = typeLabel,
                                fontSize = 11.sp,
                                color = colors.textSecondary
                            )
                            Text(
                                text = "x",
                                fontSize = 11.sp,
                                color = colors.textTertiary,
                                modifier = Modifier.clickable {
                                    attachedMedia.remove(item)
                                    isDirty = true
                                }
                            )
                        }
                    }
                }
            }
        }

        // --- 5. TAG OPTION NEXT TO MOOD, WEATHER & CHAR COUNT ANCHORED AT BOTTOM RIGHT ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.l, vertical = 4.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tag pill (Moved next to Mood!)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.surfaceElevated)
                        .clickable { showAddTagDialog = true }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (selectedTags.isEmpty()) "Tag" else "#${selectedTags.first()}${if (selectedTags.size > 1) " +${selectedTags.size - 1}" else ""}",
                        fontSize = 11.sp,
                        color = if (selectedTags.isEmpty()) colors.textSecondary else colors.accent,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Mood pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.surfaceElevated)
                        .clickable { showMoodSheet = true }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = selectedMood ?: "Mood",
                        fontSize = 11.sp,
                        color = if (selectedMood != null) colors.goldSeam else colors.textSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Weather pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.surfaceElevated)
                        .clickable { showWeatherSheet = true }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = selectedWeather ?: "Weather",
                        fontSize = 11.sp,
                        color = if (selectedWeather != null) colors.goldSeam else colors.textSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Character Count
                Text(
                    text = "${textFieldValue.text.length} chars",
                    fontSize = 11.sp,
                    color = colors.textTertiary,
                    fontWeight = FontWeight.Normal
                )
            }
        }

        // --- 6. LIST SELECTION POPUP (Shows the 2 list icons with NO text) ---
        AnimatedVisibility(
            visible = showListMenu,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.l, vertical = 2.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.surfaceElevated)
                        .border(1.dp, colors.border, RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Bullet List Icon
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable {
                                    insertBulletList()
                                    showListMenu = false
                                }
                                .padding(4.dp)
                        ) {
                            IconBulletList(color = colors.textPrimary, size = 20.dp)
                        }

                        // Numbered List Icon
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable {
                                    insertNumberList()
                                    showListMenu = false
                                }
                                .padding(4.dp)
                        ) {
                            IconNumberList(color = colors.textPrimary, size = 20.dp)
                        }
                    }
                }
            }
        }

        // --- 7. BOTTOM TOOLBAR (Clean, single list icon, alignment, and Attach button) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.surface)
                .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .padding(horizontal = spacing.l, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // A: Text Styling Sheet
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .clickable { showTextSheet = true },
                contentAlignment = Alignment.Center
            ) {
                Text("A", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
            }

            // Single List Icon (Clicking accesses the 2 list icons, NO text label)
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(if (showListMenu) colors.accentSoft else Color.Transparent)
                    .clickable { showListMenu = !showListMenu },
                contentAlignment = Alignment.Center
            ) {
                IconBulletList(
                    color = if (showListMenu) colors.accent else colors.textPrimary,
                    size = 19.dp
                )
            }

            // Alignment
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .clickable {
                        selectedAlignment = when (selectedAlignment) {
                            TextAlign.Start -> TextAlign.Center
                            TextAlign.Center -> TextAlign.End
                            else -> TextAlign.Start
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                when (selectedAlignment) {
                    TextAlign.Center -> IconAlignCenter(color = colors.textPrimary, size = 18.dp)
                    TextAlign.End -> IconAlignRight(color = colors.textPrimary, size = 18.dp)
                    else -> IconAlignLeft(color = colors.textPrimary, size = 18.dp)
                }
            }

            // Single Attach Button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.accentSoft)
                    .clickable { showAttachMenu = true }
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Attach",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.accent
                )
            }
        }
    }

    // --- 8. ATTACH MENU MODAL ---
    if (showAttachMenu) {
        AlertDialog(
            onDismissRequest = { showAttachMenu = false },
            title = { Text("Add Attachment", style = typography.itemTitle, color = colors.textPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Photo
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.surfaceElevated)
                            .clickable {
                                showAttachMenu = false
                                photoPickerLauncher.launch("image/*")
                            }
                            .padding(14.dp)
                    ) {
                        Text("Photo / Image", color = colors.textPrimary, style = typography.body)
                    }

                    // Video
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.surfaceElevated)
                            .clickable {
                                showAttachMenu = false
                                videoPickerLauncher.launch("video/*")
                            }
                            .padding(14.dp)
                    ) {
                        Text("Video", color = colors.textPrimary, style = typography.body)
                    }

                    // Voice Recording
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.surfaceElevated)
                            .clickable {
                                showAttachMenu = false
                                if (isRecordingAudio) {
                                    try {
                                        mediaRecorder?.stop()
                                        mediaRecorder?.release()
                                        mediaRecorder = null
                                        audioFileCandidate?.let {
                                            attachedAudioPath = it.absolutePath
                                            isDirty = true
                                        }
                                    } catch (_: Exception) {}
                                    isRecordingAudio = false
                                } else {
                                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                        startAudioRecording(context) { rec, file ->
                                            mediaRecorder = rec
                                            audioFileCandidate = file
                                            isRecordingAudio = true
                                            isDirty = true
                                        }
                                    } else {
                                        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                }
                            }
                            .padding(14.dp)
                    ) {
                        Text(
                            text = if (isRecordingAudio) "Stop Voice Recording" else "Voice Recording",
                            color = if (isRecordingAudio) colors.error else colors.textPrimary,
                            style = typography.body
                        )
                    }

                    // Quick Memo
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.surfaceElevated)
                            .clickable {
                                showAttachMenu = false
                                memoInputText = ""
                                showMemoInputDialog = true
                            }
                            .padding(14.dp)
                    ) {
                        Text("Quick Memo", color = colors.textPrimary, style = typography.body)
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAttachMenu = false }) {
                    Text("Cancel", color = colors.textSecondary)
                }
            },
            containerColor = colors.surfaceElevated,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // --- 9. MEMO INPUT DIALOG ---
    if (showMemoInputDialog) {
        AlertDialog(
            onDismissRequest = { showMemoInputDialog = false },
            title = { Text("Quick Memo", style = typography.itemTitle, color = colors.textPrimary) },
            text = {
                OutlinedTextField(
                    value = memoInputText,
                    onValueChange = { memoInputText = it },
                    placeholder = { Text("Note content...", color = colors.textTertiary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val clean = memoInputText.trim()
                        if (clean.isNotBlank()) {
                            attachedMedia.add("MEMO:$clean")
                            isDirty = true
                        }
                        showMemoInputDialog = false
                    }
                ) {
                    Text("Attach", color = colors.accent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showMemoInputDialog = false }) {
                    Text("Cancel", color = colors.textSecondary)
                }
            },
            containerColor = colors.surfaceElevated,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // --- 10. TEXT STYLING BOTTOM SHEET (Exact Reference UI: Image 3) ---
    var activeFontSizeSp by remember { mutableIntStateOf(15) }
    if (showTextSheet) {
        ModalBottomSheet(
            onDismissRequest = { showTextSheet = false },
            containerColor = colors.surfaceElevated,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.l, vertical = spacing.s)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header (Centered "Text" with "X" on the right - Image 3)
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Text",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .size(32.dp)
                            .clip(CircleShape)
                            .clickable { showTextSheet = false }
                            .semantics { role = Role.Button },
                        contentAlignment = Alignment.Center
                    ) {
                        IconClose(color = colors.textSecondary, size = 16.dp)
                    }
                }

                // Row 1: Alignment & Formatting Controls (Image 3: Left, Center, Right, |A|, A)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Left Align
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedAlignment == TextAlign.Start) colors.accentSoft else Color.Transparent)
                                .clickable { selectedAlignment = TextAlign.Start },
                            contentAlignment = Alignment.Center
                        ) {
                            IconAlignLeft(
                                color = if (selectedAlignment == TextAlign.Start) colors.accent else colors.textSecondary,
                                size = 20.dp
                            )
                        }

                        // Center Align
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedAlignment == TextAlign.Center) colors.accentSoft else Color.Transparent)
                                .clickable { selectedAlignment = TextAlign.Center },
                            contentAlignment = Alignment.Center
                        ) {
                            IconAlignCenter(
                                color = if (selectedAlignment == TextAlign.Center) colors.accent else colors.textSecondary,
                                size = 20.dp
                            )
                        }

                        // Right Align
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedAlignment == TextAlign.End) colors.accentSoft else Color.Transparent)
                                .clickable { selectedAlignment = TextAlign.End },
                            contentAlignment = Alignment.Center
                        ) {
                            IconAlignRight(
                                color = if (selectedAlignment == TextAlign.End) colors.accent else colors.textSecondary,
                                size = 20.dp
                            )
                        }
                    }

                    val selStart = minOf(textFieldValue.selection.start, textFieldValue.selection.end)
                    val selEnd = maxOf(textFieldValue.selection.start, textFieldValue.selection.end)
                    val hasSel = selStart < selEnd

                    val isBoldActive = if (hasSel) spans.any { it.start <= selStart && it.end >= selEnd && it.isBold } else defaultBold
                    val isItalicActive = if (hasSel) spans.any { it.start <= selStart && it.end >= selEnd && it.isItalic } else defaultItalic
                    val currentSize = if (hasSel) (spans.firstOrNull { it.start <= selStart && it.end >= selEnd && it.fontSizeSp != null }?.fontSizeSp ?: 16) else (defaultFontSizeSp ?: 16)
                    val currentColor = if (hasSel) (spans.firstOrNull { it.start <= selStart && it.end >= selEnd && it.colorHex != null }?.colorHex) else defaultColorHex

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        // |A| (Italic / Style toggle)
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isItalicActive) colors.accentSoft else Color.Transparent)
                                .border(1.dp, if (isItalicActive) colors.accent else Color.Transparent, RoundedCornerShape(8.dp))
                                .clickable {
                                    if (hasSel) {
                                        applyFormattingToSelection(italic = !isItalicActive)
                                    } else {
                                        defaultItalic = !defaultItalic
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "|A|",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isItalicActive) colors.accent else colors.textSecondary
                            )
                        }

                        // A (Bold / Primary Style)
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isBoldActive) colors.accentSoft else Color.Transparent)
                                .border(1.dp, if (isBoldActive) colors.accent else Color.Transparent, RoundedCornerShape(8.dp))
                                .clickable {
                                    if (hasSel) {
                                        applyFormattingToSelection(bold = !isBoldActive)
                                    } else {
                                        defaultBold = !defaultBold
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "A",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isBoldActive) colors.accent else colors.textPrimary
                            )
                        }
                    }
                }

                // Row 2: Font Sizes with Active Pill Highlight (Image 3: 12, 14, 15, 16, 17, 18, 20)
                val fontSizes = listOf(12, 14, 15, 16, 17, 18, 20)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.surface)
                        .padding(horizontal = 6.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val selStart = minOf(textFieldValue.selection.start, textFieldValue.selection.end)
                    val selEnd = maxOf(textFieldValue.selection.start, textFieldValue.selection.end)
                    val hasSel = selStart < selEnd
                    val activeSz = if (hasSel) (spans.firstOrNull { it.start <= selStart && it.end >= selEnd && it.fontSizeSp != null }?.fontSizeSp ?: 16) else (defaultFontSizeSp ?: 16)

                    fontSizes.forEach { sz ->
                        val isSelected = activeSz == sz
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) colors.accentSoft else Color.Transparent)
                                .border(1.dp, if (isSelected) colors.accent else Color.Transparent, RoundedCornerShape(8.dp))
                            .clickable {
                                if (hasSel) {
                                    if (activeSz == sz) {
                                        applyFormattingToSelection(fontSizeSp = 16)
                                    } else {
                                        applyFormattingToSelection(fontSizeSp = sz)
                                    }
                                } else {
                                    defaultFontSizeSp = if (defaultFontSizeSp == sz) null else sz
                                }
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$sz",
                                fontSize = 15.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) colors.accent else colors.textSecondary
                            )
                        }
                    }
                }

                // Row 3 & 4: Exact Color Circles from Reference Image 3
                val themeAccentHex = remember(colors.accent) { colorToHex(colors.accent) }
                val themeGoldHex = remember(colors.goldSeam) { colorToHex(colors.goldSeam) }

                // Row 3: Bold / Vivid Colors (8 swatches matching Image 3)
                val colorPaletteRow1 = listOf(
                    "#262626", "#EF5350", "#FF7043", "#26A69A", "#42A5F5", "#7E57C2", "#EC407A", themeAccentHex
                )
                // Row 4: Pastel / Soft Tones (8 swatches matching Image 3)
                val colorPaletteRow2 = listOf(
                    "#FFFFFF", "#FFCDD2", "#FFE0B2", "#C8E6C9", "#BBDEFB", "#D1C4E9", "#F8BBD0", themeGoldHex
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.surface)
                        .padding(horizontal = 10.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        colorPaletteRow1.forEach { hex ->
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(Color(android.graphics.Color.parseColor(hex)))
                                    .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                                    .clickable {
                                        val s = minOf(textFieldValue.selection.start, textFieldValue.selection.end)
                                        val e = maxOf(textFieldValue.selection.start, textFieldValue.selection.end)
                                        val hasS = s < e
                                        val cur = if (hasS) spans.firstOrNull { it.start <= s && it.end >= e && it.colorHex != null }?.colorHex else defaultColorHex
                                        val isMatch = cur.equals(hex, ignoreCase = true)
                                        if (hasS) {
                                            if (isMatch) applyFormattingToSelection(clearColor = true) else applyFormattingToSelection(colorHex = hex)
                                        } else {
                                            defaultColorHex = if (isMatch) null else hex
                                        }
                                    }
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        colorPaletteRow2.forEach { hex ->
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(Color(android.graphics.Color.parseColor(hex)))
                                    .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                                    .clickable {
                                        val s = minOf(textFieldValue.selection.start, textFieldValue.selection.end)
                                        val e = maxOf(textFieldValue.selection.start, textFieldValue.selection.end)
                                        val hasS = s < e
                                        val cur = if (hasS) spans.firstOrNull { it.start <= s && it.end >= e && it.colorHex != null }?.colorHex else defaultColorHex
                                        val isMatch = cur.equals(hex, ignoreCase = true)
                                        if (hasS) {
                                            if (isMatch) applyFormattingToSelection(clearColor = true) else applyFormattingToSelection(colorHex = hex)
                                        } else {
                                            defaultColorHex = if (isMatch) null else hex
                                        }
                                    }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(spacing.xs))
            }
        }
    }

    // --- 11. HIGHLIGHT PICKER: SHOWS CURRENT THEME COLOURS & ACCEPTS TEXT COLOUR CHANGE ---
    if (showHighlightSheet) {
        val themeAccentHex = remember(colors.accent) { colorToHex(colors.accent) }
        val themeGoldHex = remember(colors.goldSeam) { colorToHex(colors.goldSeam) }
        val themeSurfaceHex = remember(colors.surfaceElevated) { colorToHex(colors.surfaceElevated) }

        val highlightColors = listOf(
            themeGoldHex to "Theme Gold",
            themeAccentHex to "Theme Accent",
            themeSurfaceHex to "Theme Soft",
            "#FFF59D" to "Yellow",
            "#C8E6C9" to "Mint",
            "#BBDEFB" to "Sky",
            "#FFE0B2" to "Peach",
            "#E1BEE7" to "Lavender"
        )

        val inkTextColors = listOf(
            "#1A1A1A" to "Dark Ink",
            "#FFFFFF" to "Light Ink",
            themeGoldHex to "Gold Ink",
            themeAccentHex to "Accent Ink"
        )

        AlertDialog(
            onDismissRequest = {
                showHighlightSheet = false
                highlightTextColorOverride = null
            },
            title = {
                Text("Highlight & Ink Selection", style = typography.itemTitle, color = colors.textPrimary)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "Theme Palette Highlights",
                        style = typography.caption.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.textSecondary
                    )

                    // Current Theme Highlight Options
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        highlightColors.forEach { (hex, label) ->
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color(android.graphics.Color.parseColor(hex)))
                                    .border(1.2.dp, colors.border, CircleShape)
                                    .clickable {
                                        applyFormattingToSelection(
                                            highlightHex = hex,
                                            explicitTextColor = highlightTextColorOverride
                                        )
                                        showHighlightSheet = false
                                        highlightTextColorOverride = null
                                    }
                            )
                        }
                    }

                    // Accept Text Color change along with highlight
                    Text(
                        text = "Accept Ink Text Color",
                        style = typography.caption.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.textSecondary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        inkTextColors.forEach { (hex, label) ->
                            val isSelected = highlightTextColorOverride == hex
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) colors.accentSoft else colors.surface)
                                    .border(1.dp, if (isSelected) colors.accent else colors.border.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                    .clickable {
                                        highlightTextColorOverride = if (isSelected) null else hex
                                    }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(Color(android.graphics.Color.parseColor(hex)))
                                    )
                                    Text(
                                        text = label,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) colors.accent else colors.textSecondary
                                    )
                                }
                            }
                        }
                    }

                    TextButton(
                        onClick = {
                            applyFormattingToSelection(highlightHex = null, explicitTextColor = null)
                            showHighlightSheet = false
                            highlightTextColorOverride = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Clear Highlight", color = colors.error)
                    }
                }
            },
            confirmButton = {},
            containerColor = colors.surfaceElevated,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // --- 12. ADD TAG DIALOG ---
    if (showAddTagDialog) {
        AlertDialog(
            onDismissRequest = { showAddTagDialog = false },
            title = { Text("Add Tag", style = typography.itemTitle, color = colors.textPrimary) },
            text = {
                OutlinedTextField(
                    value = newTagInput,
                    onValueChange = { newTagInput = it },
                    placeholder = { Text("Tag name (e.g. Focus, Gratitude)", color = colors.textTertiary) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val clean = newTagInput.trim().replace("#", "").replace(",", "")
                        if (clean.isNotBlank() && !selectedTags.contains(clean)) {
                            selectedTags.add(clean)
                            isDirty = true
                        }
                        showAddTagDialog = false
                    }
                ) {
                    Text("Add", color = colors.accent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTagDialog = false }) {
                    Text("Cancel", color = colors.textSecondary)
                }
            },
            containerColor = colors.surfaceElevated,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // --- 13. MOOD SELECTION DIALOG ---
    if (showMoodSheet) {
        val moods = listOf("Calm", "Grounded", "Energized", "Peaceful", "Grateful", "Thoughtful")
        AlertDialog(
            onDismissRequest = { showMoodSheet = false },
            title = { Text("State of Mind", style = typography.itemTitle, color = colors.textPrimary) },
            text = {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    moods.forEach { m ->
                        KintsuFilterChip(
                            label = m,
                            selected = selectedMood == m,
                            onClick = {
                                selectedMood = if (selectedMood == m) null else m
                                isDirty = true
                                showMoodSheet = false
                            }
                        )
                    }
                }
            },
            confirmButton = {},
            containerColor = colors.surfaceElevated,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // --- 14. WEATHER SELECTION DIALOG ---
    if (showWeatherSheet) {
        val weathers = listOf("Sunny", "Cloudy", "Rainy", "Breezy", "Clear Night")
        AlertDialog(
            onDismissRequest = { showWeatherSheet = false },
            title = { Text("Atmosphere Weather", style = typography.itemTitle, color = colors.textPrimary) },
            text = {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    weathers.forEach { w ->
                        KintsuFilterChip(
                            label = w,
                            selected = selectedWeather == w,
                            onClick = {
                                selectedWeather = if (selectedWeather == w) null else w
                                isDirty = true
                                showWeatherSheet = false
                            }
                        )
                    }
                }
            },
            confirmButton = {},
            containerColor = colors.surfaceElevated,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // --- 15. "READY TO LEAVE?" VERIFICATION DIALOG ---
    if (showReadyToLeaveDialog) {
        AlertDialog(
            onDismissRequest = { showReadyToLeaveDialog = false },
            title = { Text("Ready to leave?", style = typography.itemTitle, color = colors.textPrimary) },
            text = {
                Text(
                    text = "Leaving now will discard this entry and your unsaved changes.",
                    style = typography.secondary,
                    color = colors.textSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        isDiscardConfirmed = true
                        showReadyToLeaveDialog = false
                        onDismiss()
                    }
                ) {
                    Text("Discard", color = colors.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showReadyToLeaveDialog = false }) {
                    Text("Keep Writing", color = colors.textPrimary)
                }
            },
            containerColor = colors.surfaceElevated,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

private fun startAudioRecording(
    context: Context,
    onStarted: (MediaRecorder, File) -> Unit
) {
    try {
        val recFile = File(context.cacheDir, "journal_voice_${System.currentTimeMillis()}.m4a")
        val rec = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }
        rec.setAudioSource(MediaRecorder.AudioSource.MIC)
        rec.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        rec.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        rec.setOutputFile(recFile.absolutePath)
        rec.prepare()
        rec.start()
        onStarted(rec, recFile)
    } catch (_: Exception) {}
}
