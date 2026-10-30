package com.kintsu.app.ui.screens

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.kintsu.app.ui.components.IconTrash
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.kintsu.app.ui.components.PermissionRequestSheet
import com.kintsu.app.util.PermissionHelper
import com.kintsu.app.data.BackupData
import com.kintsu.app.data.BackupRestoreManager
import com.kintsu.app.data.ImportMode
import com.kintsu.app.data.KintsuSettings
import com.kintsu.app.ui.components.GoogleOneTapBottomSheet
import com.kintsu.app.ui.components.GoogleOneTapLogo
import com.kintsu.app.ui.components.IconCheck
import com.kintsu.app.ui.components.IconClose
import com.kintsu.app.ui.components.IconGoldSeam
import com.kintsu.app.wellbeing.domain.StrictnessLevel
import com.kintsu.app.wellbeing.service.KintsuAccessibilityBlockerService
import com.kintsu.app.wellbeing.ui.ProtectionStatusCard
import com.kintsu.app.ui.components.KintsuBottomSheet
import com.kintsu.app.ui.components.KintsuButton
import com.kintsu.app.ui.components.KintsuButtonVariant
import com.kintsu.app.ui.components.KintsuDivider
import com.kintsu.app.ui.components.KintsuFilterChip
import com.kintsu.app.ui.components.KintsuGroupedSection
import com.kintsu.app.ui.components.KintsuIconButton
import com.kintsu.app.ui.theme.AccentOption
import com.kintsu.app.ui.theme.AppTheme
import com.kintsu.app.ui.theme.KintsuTheme
import com.kintsu.app.ui.theme.ThemeMode
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: KintsuSettings,
    syncState: com.kintsu.app.sync.model.SyncState = com.kintsu.app.sync.model.SyncState.IDLE,
    syncError: String? = null,
    onSyncNow: () -> Unit = {},
    onUpdateSyncEnabled: (Boolean) -> Unit = {},
    onSignIn: (email: String, pass: String, onResult: (Result<com.kintsu.app.sync.AuthSession>) -> Unit) -> Unit = { _, _, _ -> },
    onSignUp: (email: String, pass: String, name: String?, onResult: (Result<com.kintsu.app.sync.AuthSession>) -> Unit) -> Unit = { _, _, _, _ -> },
    onSignInWithGoogle: (idToken: String, email: String?, name: String?, onResult: (Result<com.kintsu.app.sync.AuthSession>) -> Unit) -> Unit = { _, _, _, _ -> },
    onUpdateCloudConfig: (url: String, key: String) -> Unit = { _, _ -> },
    onSignOut: (keepLocalData: Boolean) -> Unit = {},
    onDeleteCloudData: (onResult: (Result<Unit>) -> Unit) -> Unit = {},
    onDeleteAccount: (onResult: (Result<Unit>) -> Unit) -> Unit = {},
    onUpdateDisplayName: (String) -> Unit,
    onUpdateThemeMode: (ThemeMode) -> Unit,
    onUpdateAppTheme: (AppTheme) -> Unit = {},
    onUpdateAccent: (AccentOption) -> Unit,
    onUpdateContentDensity: (com.kintsu.app.data.ContentDensity) -> Unit = {},
    onUpdateHapticsEnabled: (Boolean) -> Unit = {},
    onUpdateReducedMotion: (Boolean) -> Unit = {},
    onUpdateNotifications: (Boolean) -> Unit,
    onUpdateReminderTime: (String) -> Unit,
    onUpdateWeekStartMonday: (Boolean) -> Unit,
    onUpdateGoldRepairEnabled: (Boolean) -> Unit = {},
    onUpdateNotifyOnlyIfNotDone: (Boolean) -> Unit = {},
    onUpdateGoldRepairReminderEnabled: (Boolean) -> Unit = {},
    onUpdateFocusEnabled: (Boolean) -> Unit = {},
    onUpdateFocusDefaultPreset: (Int) -> Unit = {},
    onUpdateFocusBreakMinutes: (Int) -> Unit = {},
    onUpdateFocusSoundEnabled: (Boolean) -> Unit = {},
    onUpdateFocusVibrationEnabled: (Boolean) -> Unit = {},
    onUpdateDailyIntentionEnabled: (Boolean) -> Unit = {},
    onUpdateDailyIntentionReminderTime: (String) -> Unit = {},
    onUpdateJournalEnabled: (Boolean) -> Unit = {},
    onUpdateJournalReminderTime: (String) -> Unit = {},
    onUpdateWeeklyReviewEnabled: (Boolean) -> Unit = {},
    onUpdateWeeklyReviewReminderTime: (String) -> Unit = {},
    onUpdateHomeSectionOrder: (String) -> Unit = {},
    onUpdateHiddenHomeSections: (String) -> Unit = {},
    onUpdateQuickActionsOrder: (String) -> Unit = {},
    onResetHomePreferences: () -> Unit = {},
    onExportData: suspend () -> String,
    onExportToFile: suspend (Uri) -> Unit = {},
    onImportFromFile: suspend (Uri, ImportMode) -> Result<Unit> = { _, _ -> Result.success(Unit) },
    onOpenWellbeingWizard: () -> Unit = {},
    onOpenDetox: () -> Unit = {},
    onOpenQuitHabits: () -> Unit = {},
    onOpenShop: () -> Unit = {},
    onUpdateWellbeingEnabled: (Boolean) -> Unit = {},
    onUpdateStrictness: (String) -> Unit = {},
    onUpdateScreenTimeGoalMinutes: (Int) -> Unit = {},
    onResetFailSafe: () -> Unit = {},
    onResetData: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showExportSheet by remember { mutableStateOf(false) }
    var exportedJson by remember { mutableStateOf("") }
    var showResetCautionDialog by remember { mutableStateOf(false) }
    var resetConfirmationText by remember { mutableStateOf("") }
    var resetConfirmedCheckbox by remember { mutableStateOf(false) }

    var showDeleteAccountStep1 by remember { mutableStateOf(false) }
    var showDeleteAccountStep2 by remember { mutableStateOf(false) }
    var deleteAccountConfirmText by remember { mutableStateOf("") }
    var deleteAccountLoading by remember { mutableStateOf(false) }
    var showWidgetShowcaseSheet by remember { mutableStateOf(false) }

    var isEditingName by remember { mutableStateOf(false) }
    var tempName by remember { mutableStateOf(settings.displayName) }

    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }
    var pendingImportData by remember { mutableStateOf<BackupData?>(null) }
    var showImportConfirmDialog by remember { mutableStateOf(false) }
    var importErrorMessage by remember { mutableStateOf<String?>(null) }

    var showAuthSheet by remember { mutableStateOf(false) }
    var authModeIsSignUp by remember { mutableStateOf(false) }
    var authEmail by remember { mutableStateOf("") }
    var authPassword by remember { mutableStateOf("") }
    var authDisplayName by remember { mutableStateOf("") }
    var authLoading by remember { mutableStateOf(false) }
    var authError by remember { mutableStateOf<String?>(null) }
    var showSignOutDialog by remember { mutableStateOf(false) }
    var showDeleteCloudDialog by remember { mutableStateOf(false) }
    var showPermissionSheet by remember { mutableStateOf(false) }
    var showGoogleOneTapSheet by remember { mutableStateOf(false) }
    var showEndpointDialog by remember { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current
    var hasNotificationPermission by remember {
        mutableStateOf(PermissionHelper.isNotificationPermissionGranted(context))
    }
    var hasExactAlarmPermission by remember {
        mutableStateOf(PermissionHelper.isExactAlarmPermissionGranted(context))
    }
    var isBatteryUnrestricted by remember {
        mutableStateOf(PermissionHelper.isBatteryOptimizationIgnored(context))
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasNotificationPermission = PermissionHelper.isNotificationPermissionGranted(context)
                hasExactAlarmPermission = PermissionHelper.isExactAlarmPermissionGranted(context)
                isBatteryUnrestricted = PermissionHelper.isBatteryOptimizationIgnored(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val postNotificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasNotificationPermission = granted
    }

    val createDocLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            scope.launch {
                onExportToFile(it)
                Toast.makeText(context, "Backup exported successfully", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val openDocLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            scope.launch {
                try {
                    val content = context.contentResolver.openInputStream(it)?.bufferedReader()?.use { r -> r.readText() } ?: ""
                    val validation = BackupRestoreManager.validateBackup(content)
                    if (validation.isValid && validation.data != null) {
                        pendingImportUri = it
                        pendingImportData = validation.data
                        showImportConfirmDialog = true
                    } else {
                        importErrorMessage = validation.errorMessage ?: "Invalid backup file"
                    }
                } catch (e: Exception) {
                    importErrorMessage = "Failed to read backup file: ${e.localizedMessage}"
                }
            }
        }
    }

    BackHandler {
        onClose()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .widthIn(max = 640.dp)
                .fillMaxWidth()
        ) {
            // Settings Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.xl, vertical = spacing.l),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Settings",
                    style = typography.screenTitle,
                    color = colors.textPrimary
                )
                KintsuIconButton(
                    icon = { IconClose(color = it, size = 20.dp) },
                    contentDescription = "Close Settings",
                    onClick = onClose
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = spacing.xl),
                verticalArrangement = Arrangement.spacedBy(spacing.l)
            ) {
            // Profile Section
            item {
                Text(
                    text = "PROFILE",
                    style = typography.caption,
                    color = colors.textTertiary,
                    modifier = Modifier.padding(start = spacing.xs, bottom = spacing.xs)
                )
                KintsuGroupedSection {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(spacing.m),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Display Name",
                                style = typography.itemTitle,
                                color = colors.textPrimary
                            )
                            Text(
                                text = settings.displayName,
                                style = typography.secondary,
                                color = colors.textSecondary
                            )
                        }

                        KintsuButton(
                            text = if (isEditingName) "Done" else "Edit",
                            onClick = {
                                if (isEditingName && tempName.isNotBlank()) {
                                    onUpdateDisplayName(tempName)
                                }
                                isEditingName = !isEditingName
                            },
                            variant = KintsuButtonVariant.TEXT
                        )
                    }

                    if (isEditingName) {
                        KintsuDivider()
                        Box(modifier = Modifier.padding(spacing.m)) {
                            OutlinedTextField(
                                value = tempName,
                                onValueChange = { tempName = it },
                                placeholder = { Text("Your name or moniker", color = colors.textTertiary) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = KintsuTheme.shapes.medium,
                                singleLine = true
                            )
                        }
                    }
                }
            }

            // Account & Cloud Sync Section
            item {
                Text(
                    text = "ACCOUNT & CLOUD SYNC",
                    style = typography.caption,
                    color = colors.textTertiary,
                    modifier = Modifier.padding(start = spacing.xs, bottom = spacing.xs)
                )
                KintsuGroupedSection {
                    val isSignedIn = !settings.accountEmail.isNullOrBlank()

                    // Account identity & action row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(spacing.m),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = spacing.s)) {
                            Text(
                                text = if (isSignedIn) settings.accountEmail ?: "Signed In" else "Guest Mode (Local Only)",
                                style = typography.itemTitle,
                                color = colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isSignedIn) {
                                    val name = settings.accountDisplayName
                                    if (!name.isNullOrBlank()) "Linked as $name • Cloud backup enabled" else "Cloud backup enabled across devices"
                                } else {
                                    "Your data is stored locally on this phone. Sign in to sync across devices."
                                },
                                style = typography.caption,
                                color = colors.textSecondary
                            )
                        }

                        if (isSignedIn) {
                            KintsuButton(
                                text = "Sign Out",
                                onClick = { showSignOutDialog = true },
                                variant = KintsuButtonVariant.SECONDARY
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(spacing.s)
                            ) {
                                Button(
                                    onClick = {
                                        authError = null
                                        showGoogleOneTapSheet = true
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF1A73E8),
                                        contentColor = Color.White
                                    ),
                                    shape = CircleShape,
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        GoogleOneTapLogo(size = 18.dp)
                                        Text(
                                            text = "Google",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                                KintsuButton(
                                    text = "Email",
                                    onClick = {
                                        authError = null
                                        showAuthSheet = true
                                    },
                                    variant = KintsuButtonVariant.SECONDARY
                                )
                            }
                        }
                    }

                    if (isSignedIn) {
                        KintsuDivider()

                        // Sync enabled toggle
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(spacing.m),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = spacing.m)) {
                                Text(
                                    text = "Enable Cloud Sync",
                                    style = typography.itemTitle,
                                    color = colors.textPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Automatically synchronise rituals, tasks and reflections across your devices.",
                                    style = typography.caption,
                                    color = colors.textSecondary
                                )
                            }

                            Switch(
                                checked = settings.syncEnabled,
                                onCheckedChange = onUpdateSyncEnabled,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = colors.background,
                                    checkedTrackColor = colors.accent,
                                    uncheckedThumbColor = colors.textTertiary,
                                    uncheckedTrackColor = colors.surfaceElevated
                                )
                            )
                        }

                        KintsuDivider()

                        // Sync status & manual sync row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(spacing.m),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = spacing.s)) {
                                val statusLabel = when (syncState) {
                                    com.kintsu.app.sync.model.SyncState.SYNCING -> "Syncing..."
                                    com.kintsu.app.sync.model.SyncState.SUCCESS -> "All caught up"
                                    com.kintsu.app.sync.model.SyncState.OFFLINE -> "Offline (mutations queued)"
                                    com.kintsu.app.sync.model.SyncState.ERROR -> syncError ?: "Sync interrupted"
                                    com.kintsu.app.sync.model.SyncState.IDLE -> if (settings.syncEnabled) "Ready" else "Sync paused"
                                }
                                val statusColor = when (syncState) {
                                    com.kintsu.app.sync.model.SyncState.SYNCING -> colors.accent
                                    com.kintsu.app.sync.model.SyncState.SUCCESS -> colors.accent
                                    com.kintsu.app.sync.model.SyncState.OFFLINE -> colors.textTertiary
                                    com.kintsu.app.sync.model.SyncState.ERROR -> colors.warning
                                    com.kintsu.app.sync.model.SyncState.IDLE -> colors.textSecondary
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(statusColor)
                                    )
                                    Spacer(modifier = Modifier.width(spacing.xs))
                                    Text(
                                        text = statusLabel,
                                        style = typography.caption,
                                        color = statusColor
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                val lastSyncFormatted = if (settings.lastSyncTime > 0L) {
                                    val diffSec = (System.currentTimeMillis() - settings.lastSyncTime) / 1000
                                    when {
                                        diffSec < 60 -> "Just now"
                                        diffSec < 3600 -> "${diffSec / 60}m ago"
                                        else -> "${diffSec / 3600}h ago"
                                    }
                                } else "Never"
                                Text(
                                    text = "Last synced: $lastSyncFormatted",
                                    style = typography.caption,
                                    color = colors.textTertiary
                                )
                            }

                            KintsuButton(
                                text = if (syncState == com.kintsu.app.sync.model.SyncState.SYNCING) "Syncing..." else "Sync Now",
                                onClick = onSyncNow,
                                enabled = syncState != com.kintsu.app.sync.model.SyncState.SYNCING && settings.syncEnabled,
                                variant = KintsuButtonVariant.ACCENT_SOFT
                            )
                        }

                        KintsuDivider()

                        // Cloud Server Configuration row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(spacing.m),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = spacing.s)) {
                                Text(
                                    text = "Cloud Server Endpoint",
                                    style = typography.itemTitle,
                                    color = colors.textPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = settings.cloudEndpointUrl,
                                    style = typography.caption,
                                    color = colors.accent
                                )
                            }

                            KintsuButton(
                                text = "Configure",
                                onClick = { showEndpointDialog = true },
                                variant = KintsuButtonVariant.SECONDARY
                            )
                        }

                        KintsuDivider()

                        // Delete Cloud Data action
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(spacing.m),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = spacing.s)) {
                                Text(
                                    text = "Delete Cloud Data",
                                    style = typography.itemTitle,
                                    color = colors.textPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Wipes your backed up records from the cloud. Local data on this device will remain intact.",
                                    style = typography.caption,
                                    color = colors.textSecondary
                                )
                            }

                            KintsuButton(
                                text = "Delete Cloud Copy",
                                onClick = { showDeleteCloudDialog = true },
                                variant = KintsuButtonVariant.TEXT
                            )
                        }

                        KintsuDivider()

                        // Delete Account (2-Step Verification)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(spacing.m),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = spacing.s)) {
                                Text(
                                    text = "Delete Account",
                                    style = typography.itemTitle,
                                    color = colors.error
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Permanently deletes your account, cloud sync data, and wipes all local device records (requires 2-step verification).",
                                    style = typography.caption,
                                    color = colors.textSecondary
                                )
                            }

                            KintsuButton(
                                text = "Delete Account",
                                onClick = {
                                    deleteAccountConfirmText = ""
                                    showDeleteAccountStep1 = true
                                },
                                variant = KintsuButtonVariant.TEXT
                            )
                        }
                    }
                }
            }

            // System Permissions Section
            item {
                Text(
                    text = "SYSTEM PERMISSIONS",
                    style = typography.caption,
                    color = colors.textTertiary,
                    modifier = Modifier.padding(start = spacing.xs, bottom = spacing.xs)
                )
                KintsuGroupedSection {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(spacing.m),
                        verticalArrangement = Arrangement.spacedBy(spacing.m)
                    ) {
                        // Notifications
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Mindful Notifications",
                                    style = typography.itemTitle,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "Reminders, intentions & focus timer cues",
                                    style = typography.secondary,
                                    color = colors.textSecondary
                                )
                            }
                            KintsuButton(
                                text = if (hasNotificationPermission) "Allowed" else "Grant",
                                onClick = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
                                        postNotificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        PermissionHelper.openAppNotificationSettings(context)
                                    }
                                },
                                variant = if (hasNotificationPermission) KintsuButtonVariant.SECONDARY else KintsuButtonVariant.ACCENT_SOFT
                            )
                        }

                        // Exact Alarms
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Exact Alarms & Timers",
                                    style = typography.itemTitle,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "Accurate ritual timing without delays",
                                    style = typography.secondary,
                                    color = colors.textSecondary
                                )
                            }
                            KintsuButton(
                                text = if (hasExactAlarmPermission) "Granted" else "Configure",
                                onClick = {
                                    PermissionHelper.openExactAlarmSettings(context)
                                },
                                variant = if (hasExactAlarmPermission) KintsuButtonVariant.SECONDARY else KintsuButtonVariant.ACCENT_SOFT
                            )
                        }

                        // Background Execution / Battery Optimization
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Unrestricted Background",
                                    style = typography.itemTitle,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "Prevents OS from pausing timers and sync",
                                    style = typography.secondary,
                                    color = colors.textSecondary
                                )
                            }
                            KintsuButton(
                                text = if (isBatteryUnrestricted) "Unrestricted" else "Allow",
                                onClick = {
                                    PermissionHelper.openBatteryOptimizationSettings(context)
                                },
                                variant = if (isBatteryUnrestricted) KintsuButtonVariant.SECONDARY else KintsuButtonVariant.ACCENT_SOFT
                            )
                        }

                        // Review All Permissions button
                        KintsuButton(
                            text = "Review All Permissions",
                            onClick = { showPermissionSheet = true },
                            variant = KintsuButtonVariant.TEXT,
                            modifier = Modifier.align(Alignment.End)
                        )
                    }
                }
            }

            // Appearance Section
            item {
                Text(
                    text = "APPEARANCE",
                    style = typography.caption,
                    color = colors.textTertiary,
                    modifier = Modifier.padding(start = spacing.xs, bottom = spacing.xs)
                )
                KintsuGroupedSection {
                    // App Theme Previews
                    Column(modifier = Modifier.padding(spacing.m)) {
                        Text(
                            text = "App Theme",
                            style = typography.itemTitle,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(spacing.xs))
                        Text(
                            text = "Five curated atmospheric palettes crafted for mindful clarity.",
                            style = typography.caption,
                            color = colors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(spacing.m))

                        Column(verticalArrangement = Arrangement.spacedBy(spacing.s)) {
                            AppTheme.values().forEach { theme ->
                                val isSelected = settings.appTheme == theme
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(KintsuTheme.shapes.medium)
                                        .background(if (isSelected) colors.surfaceElevated else Color.Transparent)
                                        .border(
                                            width = if (isSelected) 1.5.dp else 1.dp,
                                            color = if (isSelected) colors.accent else colors.border,
                                            shape = KintsuTheme.shapes.medium
                                        )
                                        .clickable { onUpdateAppTheme(theme) }
                                        .padding(horizontal = spacing.m, vertical = spacing.s)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = theme.displayName,
                                                    style = typography.itemTitle,
                                                    color = if (isSelected) colors.accent else colors.textPrimary
                                                )
                                                if (theme == AppTheme.SAGE_IVORY) {
                                                    Spacer(modifier = Modifier.width(spacing.xs))
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(KintsuTheme.shapes.full)
                                                            .background(colors.accentSoft)
                                                            .padding(horizontal = spacing.xs, vertical = 2.dp)
                                                    ) {
                                                        Text(
                                                            text = "Default",
                                                            style = typography.caption,
                                                            color = colors.accent
                                                        )
                                                    }
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = theme.subtitle,
                                                style = typography.caption,
                                                color = colors.textSecondary
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(spacing.s))

                                        // 4 Representative Palette Swatches
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            theme.previewPalette.forEach { swatch ->
                                                Box(
                                                    modifier = Modifier
                                                        .size(16.dp)
                                                        .clip(CircleShape)
                                                        .background(swatch)
                                                        .border(0.5.dp, colors.border.copy(alpha = 0.5f), CircleShape)
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(spacing.xs))

                                            // Selected Indicator
                                            Box(
                                                modifier = Modifier
                                                    .size(22.dp)
                                                    .clip(CircleShape)
                                                    .border(
                                                        width = 1.5.dp,
                                                        color = if (isSelected) colors.accent else colors.border,
                                                        shape = CircleShape
                                                    )
                                                    .background(if (isSelected) colors.accent else Color.Transparent),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (isSelected) {
                                                    IconCheck(color = colors.background, size = 12.dp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    KintsuDivider()

                    // Theme Mode Selector
                    Column(modifier = Modifier.padding(spacing.m)) {
                        Text(
                            text = "Theme Mode",
                            style = typography.itemTitle,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(spacing.s))
                        Row(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
                            ThemeMode.values().forEach { mode ->
                                KintsuFilterChip(
                                    label = mode.name.lowercase().replaceFirstChar { it.uppercase() },
                                    selected = settings.themeMode == mode,
                                    onClick = { onUpdateThemeMode(mode) }
                                )
                            }
                        }
                    }

                    KintsuDivider()

                    // Accent Color Picker
                    Column(modifier = Modifier.padding(spacing.m)) {
                        Text(
                            text = "Accent Color",
                            style = typography.itemTitle,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(spacing.s))
                        Row(horizontalArrangement = Arrangement.spacedBy(spacing.s)) {
                            AccentOption.values().forEach { accent ->
                                val isSelected = settings.accentOption == accent
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(accent.color)
                                        .clickable(onClickLabel = "Select ${accent.displayName}") { onUpdateAccent(accent) }
                                        .then(
                                            if (isSelected) Modifier.border(2.5.dp, colors.textPrimary, CircleShape) else Modifier
                                        )
                                        .semantics {
                                            role = Role.RadioButton
                                            selected = isSelected
                                            contentDescription = "${accent.displayName} accent"
                                        }
                                )
                            }
                        }
                    }

                    KintsuDivider()

                    // Content Density Selector
                    Column(modifier = Modifier.padding(spacing.m)) {
                        Text(
                            text = "Content Density",
                            style = typography.itemTitle,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Choose comfortable roominess or compact view for more content.",
                            style = typography.caption,
                            color = colors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(spacing.s))
                        Row(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
                            com.kintsu.app.data.ContentDensity.values().forEach { density ->
                                val label = if (density == com.kintsu.app.data.ContentDensity.COMFORTABLE) "Comfortable" else "Compact"
                                KintsuFilterChip(
                                    label = label,
                                    selected = settings.contentDensity == density,
                                    onClick = { onUpdateContentDensity(density) }
                                )
                            }
                        }
                    }

                    KintsuDivider()

                    // Haptic Feedback Switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(spacing.m),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = spacing.m)) {
                            Text(
                                text = "Haptic Feedback",
                                style = typography.itemTitle,
                                color = colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Subtle tactile feedback for completing rituals and pressing buttons.",
                                style = typography.caption,
                                color = colors.textSecondary
                            )
                        }

                        Switch(
                            checked = settings.hapticsEnabled,
                            onCheckedChange = onUpdateHapticsEnabled,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = colors.background,
                                checkedTrackColor = colors.accent,
                                uncheckedThumbColor = colors.textTertiary,
                                uncheckedTrackColor = colors.surfaceElevated
                            )
                        )
                    }

                    KintsuDivider()

                    // Reduced Motion Switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(spacing.m),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = spacing.m)) {
                            Text(
                                text = "Reduced Motion",
                                style = typography.itemTitle,
                                color = colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Minimise decorative transitions and animations for a still experience.",
                                style = typography.caption,
                                color = colors.textSecondary
                            )
                        }

                        Switch(
                            checked = settings.reducedMotion,
                            onCheckedChange = onUpdateReducedMotion,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = colors.background,
                                checkedTrackColor = colors.accent,
                                uncheckedThumbColor = colors.textTertiary,
                                uncheckedTrackColor = colors.surfaceElevated
                            )
                        )
                    }
                }
            }

            // Home Screen Widgets Section
            item {
                Text(
                    text = "HOME SCREEN WIDGETS",
                    style = typography.caption,
                    color = colors.textTertiary,
                    modifier = Modifier.padding(start = spacing.xs, bottom = spacing.xs)
                )
                KintsuGroupedSection {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showWidgetShowcaseSheet = true }
                            .padding(spacing.m),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(colors.accent.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            com.kintsu.app.ui.components.IconWidget(
                                color = colors.accent,
                                size = 20.dp
                            )
                        }

                        Spacer(modifier = Modifier.width(spacing.m))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Home Screen Widgets",
                                    style = typography.itemTitle,
                                    color = colors.textPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .background(colors.goldSeamSoft, CircleShape)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "11 Live",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.goldSeam
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Showcase widgets, view definitions, and pin directly to home screen",
                                style = typography.caption,
                                color = colors.textSecondary
                            )
                        }

                        com.kintsu.app.ui.components.IconChevronRight(
                            color = colors.textTertiary,
                            size = 18.dp
                        )
                    }
                }
            }

            // Kintsugi Gold Repair Section
            item {
                Text(
                    text = "KINTSUGI GOLD REPAIR",
                    style = typography.caption,
                    color = colors.textTertiary,
                    modifier = Modifier.padding(start = spacing.xs, bottom = spacing.xs)
                )
                KintsuGroupedSection {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(spacing.m),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = spacing.m)) {
                            Text(
                                text = "Enable Gold Repair",
                                style = typography.itemTitle,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "A missed day is not failure, it is a crack you repair with gold. Completing within 48h heals it.",
                                style = typography.secondary,
                                color = colors.textSecondary
                            )
                        }

                        Switch(
                            checked = settings.goldRepairEnabled,
                            onCheckedChange = onUpdateGoldRepairEnabled,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = colors.background,
                                checkedTrackColor = colors.goldSeam,
                                uncheckedThumbColor = colors.textTertiary,
                                uncheckedTrackColor = colors.surfaceElevated
                            )
                        )
                    }
                }
            }

            // Home Hub Customization Section
            item {
                Text(
                    text = "HOME HUB",
                    style = typography.caption,
                    color = colors.textTertiary,
                    modifier = Modifier.padding(start = spacing.xs, bottom = spacing.xs)
                )
                KintsuGroupedSection {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(spacing.m)
                    ) {
                        Text(
                            text = "Visible Elements",
                            style = typography.itemTitle,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Header and Today rituals stay fixed at the top. Reorder or toggle the rest.",
                            style = typography.caption,
                            color = colors.textSecondary
                        )

                        Spacer(modifier = Modifier.height(spacing.m))

                        val allSections = listOf(
                            "DAILY_INTENTION" to "Daily Intention",
                            "FOCUS" to "Focus Quick-Start",
                            "KEY_NUMBERS" to "Key Numbers",
                            "QUICK_ACTIONS" to "Quick Actions"
                        )
                        val currentOrder = settings.homeSectionOrder.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                        val hiddenSet = settings.hiddenHomeSections.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()

                        currentOrder.forEachIndexed { index, secId ->
                            val secTitle = allSections.find { it.first == secId }?.second ?: secId
                            val isVisible = !hiddenSet.contains(secId)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = spacing.xs),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "↑",
                                        modifier = Modifier
                                            .clickable(enabled = index > 0) {
                                                val newOrder = currentOrder.toMutableList()
                                                val temp = newOrder[index]
                                                newOrder[index] = newOrder[index - 1]
                                                newOrder[index - 1] = temp
                                                onUpdateHomeSectionOrder(newOrder.joinToString(","))
                                            }
                                            .padding(horizontal = spacing.xs, vertical = spacing.xxs),
                                        style = typography.itemTitle,
                                        color = if (index > 0) colors.accent else colors.textTertiary
                                    )
                                    Text(
                                        text = "↓",
                                        modifier = Modifier
                                            .clickable(enabled = index < currentOrder.size - 1) {
                                                val newOrder = currentOrder.toMutableList()
                                                val temp = newOrder[index]
                                                newOrder[index] = newOrder[index + 1]
                                                newOrder[index + 1] = temp
                                                onUpdateHomeSectionOrder(newOrder.joinToString(","))
                                            }
                                            .padding(horizontal = spacing.xs, vertical = spacing.xxs),
                                        style = typography.itemTitle,
                                        color = if (index < currentOrder.size - 1) colors.accent else colors.textTertiary
                                    )
                                    Spacer(modifier = Modifier.width(spacing.xs))
                                    Text(
                                        text = secTitle,
                                        style = typography.body,
                                        color = if (isVisible) colors.textPrimary else colors.textTertiary
                                    )
                                }

                                Switch(
                                    checked = isVisible,
                                    onCheckedChange = { checked ->
                                        val newHidden = if (checked) {
                                            hiddenSet - secId
                                        } else {
                                            hiddenSet + secId
                                        }
                                        onUpdateHiddenHomeSections(newHidden.joinToString(","))
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = colors.background,
                                        checkedTrackColor = colors.accent,
                                        uncheckedThumbColor = colors.textTertiary,
                                        uncheckedTrackColor = colors.surfaceElevated
                                    )
                                )
                            }
                        }

                        KintsuDivider(modifier = Modifier.padding(vertical = spacing.m))

                        Text(
                            text = "Quick Actions (up to 5)",
                            style = typography.itemTitle,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Choose which shortcuts appear on your home hub.",
                            style = typography.caption,
                            color = colors.textSecondary
                        )

                        Spacer(modifier = Modifier.height(spacing.s))

                        val availableActions = listOf(
                            "ADD_HABIT" to "Habit",
                            "ADD_TASK" to "Task",
                            "START_FOCUS" to "Focus",
                            "JOURNAL" to "Journal",
                            "NEW_GOAL" to "Goal",
                            "WEEKLY_REVIEW" to "Review"
                        )
                        val activeActions = settings.quickActionsOrder.split(",").map { it.trim() }.filter { it.isNotEmpty() }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(spacing.xs),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            availableActions.take(3).forEach { (actId, actName) ->
                                val isSelected = activeActions.contains(actId)
                                KintsuFilterChip(
                                    label = actName,
                                    selected = isSelected,
                                    onClick = {
                                        val newActions = if (isSelected) {
                                            if (activeActions.size > 1) activeActions - actId else activeActions
                                        } else {
                                            if (activeActions.size < 5) activeActions + actId else activeActions
                                        }
                                        onUpdateQuickActionsOrder(newActions.joinToString(","))
                                    }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(spacing.xs))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(spacing.xs),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            availableActions.drop(3).forEach { (actId, actName) ->
                                val isSelected = activeActions.contains(actId)
                                KintsuFilterChip(
                                    label = actName,
                                    selected = isSelected,
                                    onClick = {
                                        val newActions = if (isSelected) {
                                            if (activeActions.size > 1) activeActions - actId else activeActions
                                        } else {
                                            if (activeActions.size < 5) activeActions + actId else activeActions
                                        }
                                        onUpdateQuickActionsOrder(newActions.joinToString(","))
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(spacing.m))

                        KintsuButton(
                            text = "Reset Home Layout",
                            onClick = onResetHomePreferences,
                            variant = KintsuButtonVariant.SECONDARY,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Rhythm & Notifications Section
            item {
                Text(
                    text = "RHYTHM & NOTIFICATIONS",
                    style = typography.caption,
                    color = colors.textTertiary,
                    modifier = Modifier.padding(start = spacing.xs, bottom = spacing.xs)
                )
                KintsuGroupedSection {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(spacing.m),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Daily Reminders",
                                style = typography.itemTitle,
                                color = colors.textPrimary
                            )
                            Text(
                                text = if (settings.notificationsEnabled) "Active at ${settings.defaultReminderTime}" else "Quiet / Off",
                                style = typography.secondary,
                                color = colors.textSecondary
                            )
                        }

                        Switch(
                            checked = settings.notificationsEnabled,
                            onCheckedChange = onUpdateNotifications,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = colors.background,
                                checkedTrackColor = colors.accent,
                                uncheckedThumbColor = colors.textTertiary,
                                uncheckedTrackColor = colors.surfaceElevated
                            )
                        )
                    }

                    if (settings.notificationsEnabled) {
                        KintsuDivider()

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(spacing.m),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = spacing.m)) {
                                Text(
                                    text = "Only If Incomplete",
                                    style = typography.itemTitle,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "Skip notification if the ritual is already done today",
                                    style = typography.secondary,
                                    color = colors.textSecondary
                                )
                            }

                            Switch(
                                checked = settings.notifyOnlyIfNotDone,
                                onCheckedChange = onUpdateNotifyOnlyIfNotDone,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = colors.background,
                                    checkedTrackColor = colors.accent,
                                    uncheckedThumbColor = colors.textTertiary,
                                    uncheckedTrackColor = colors.surfaceElevated
                                )
                            )
                        }

                        KintsuDivider()

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(spacing.m),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = spacing.m)) {
                                Text(
                                    text = "Gentle Repair Alerts",
                                    style = typography.itemTitle,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "Notify when a missed day can be mended with gold",
                                    style = typography.secondary,
                                    color = colors.textSecondary
                                )
                            }

                            Switch(
                                checked = settings.goldRepairReminderEnabled,
                                onCheckedChange = onUpdateGoldRepairReminderEnabled,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = colors.background,
                                    checkedTrackColor = colors.goldSeam,
                                    uncheckedThumbColor = colors.textTertiary,
                                    uncheckedTrackColor = colors.surfaceElevated
                                )
                            )
                        }
                    }

                    KintsuDivider()

                    // Week Start Day
                    Column(modifier = Modifier.padding(spacing.m)) {
                        Text(
                            text = "First Day of the Week",
                            style = typography.itemTitle,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(spacing.s))
                        Row(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
                            KintsuFilterChip(
                                label = "Monday",
                                selected = settings.weekStartMonday,
                                onClick = { onUpdateWeekStartMonday(true) }
                            )
                            KintsuFilterChip(
                                label = "Sunday",
                                selected = !settings.weekStartMonday,
                                onClick = { onUpdateWeekStartMonday(false) }
                            )
                        }
                    }
                }
            }

            // Digital Wellbeing & Screen Time Suite
            item {
                Text(
                    text = "DIGITAL WELLBEING & FOCUS SHIELD",
                    style = typography.caption,
                    color = colors.textTertiary,
                    modifier = Modifier.padding(start = spacing.xs, bottom = spacing.xs)
                )
                KintsuGroupedSection {
                    // Real-time Protection Status Card
                    Box(modifier = Modifier.padding(spacing.m)) {
                        ProtectionStatusCard(
                            isProtectionActive = settings.protectionActive,
                            isFailSafeTripped = settings.protectionFailSafeTripped,
                            missingPermissionName = if (!KintsuAccessibilityBlockerService.isServiceRunning) "Accessibility Service" else null,
                            onFixPermission = onOpenWellbeingWizard,
                            onResetFailSafe = onResetFailSafe,
                            onOpenWizard = onOpenWellbeingWizard
                        )
                    }

                    KintsuDivider()

                    // Master Shield Switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(spacing.m),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = spacing.s)) {
                            Text(
                                text = "Addiction Blocker & Shield",
                                style = typography.itemTitle,
                                color = colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Enforce mindful pauses, app schedules & digital detox",
                                style = typography.secondary,
                                color = colors.textSecondary
                            )
                        }
                        Switch(
                            checked = settings.wellbeingEnabled,
                            onCheckedChange = { enabled ->
                                onUpdateWellbeingEnabled(enabled)
                                if (enabled && !KintsuAccessibilityBlockerService.isServiceRunning) {
                                    onOpenWellbeingWizard()
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = colors.background,
                                checkedTrackColor = colors.accent,
                                uncheckedThumbColor = colors.textTertiary,
                                uncheckedTrackColor = colors.surfaceElevated
                            )
                        )
                    }

                    if (settings.wellbeingEnabled) {
                        KintsuDivider()

                        // Strictness Level
                        Column(modifier = Modifier.padding(spacing.m)) {
                            Text(
                                text = "Strictness Level",
                                style = typography.itemTitle,
                                color = colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(spacing.xs))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(spacing.xs)
                            ) {
                                val currentStrictness = settings.wellbeingStrictness
                                listOf(
                                    StrictnessLevel.GENTLE.name to "Gentle",
                                    StrictnessLevel.BALANCED.name to "Balanced",
                                    StrictnessLevel.STRICT.name to "Strict"
                                ).forEach { (levelKey, label) ->
                                    KintsuFilterChip(
                                        selected = currentStrictness == levelKey,
                                        onClick = { onUpdateStrictness(levelKey) },
                                        label = label
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(spacing.xs))
                            Text(
                                text = when (settings.wellbeingStrictness) {
                                    "STRICT" -> "Strict: 60-second delay with typed intent to exit. 24h cooling off on weakening."
                                    "BALANCED" -> "Balanced: 10-second delay with mindful reason picker before dismissing."
                                    else -> "Gentle: Soft breathing pause. Always easy to dismiss non-judgmentally."
                                },
                                style = typography.caption,
                                color = colors.textTertiary
                            )
                        }

                        KintsuDivider()

                        // Daily Screen-Time Goal
                        Column(modifier = Modifier.padding(spacing.m)) {
                            Text(
                                text = "Daily Screen-Time Goal",
                                style = typography.itemTitle,
                                color = colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(spacing.xs))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(spacing.xs)
                            ) {
                                listOf(60 to "1h", 120 to "2h", 180 to "3h", 240 to "4h").forEach { (mins, label) ->
                                    KintsuFilterChip(
                                        selected = settings.screenTimeGoalMinutes == mins,
                                        onClick = { onUpdateScreenTimeGoalMinutes(mins) },
                                        label = label
                                    )
                                }
                            }
                        }

                        KintsuDivider()

                        // Fast actions: Detox & Break a Habit
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(spacing.m),
                            horizontalArrangement = Arrangement.spacedBy(spacing.s)
                        ) {
                            KintsuButton(
                                text = "Digital Detox",
                                onClick = onOpenDetox,
                                variant = KintsuButtonVariant.SECONDARY,
                                modifier = Modifier.weight(1f)
                            )
                            KintsuButton(
                                text = "Break a Habit",
                                onClick = onOpenQuitHabits,
                                variant = KintsuButtonVariant.SECONDARY,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        KintsuDivider()

                        // Setup Wizard / Permissions Review
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(spacing.m),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = spacing.s)) {
                                Text(
                                    text = "Review Protection Permissions",
                                    style = typography.itemTitle,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "Accessibility, Usage Access, Overlay & DND",
                                    style = typography.caption,
                                    color = colors.textSecondary
                                )
                            }
                            KintsuButton(
                                text = "Review",
                                onClick = onOpenWellbeingWizard,
                                variant = KintsuButtonVariant.TEXT
                            )
                        }
                    }
                }
            }

            // Sanctuary Economy & Shop Section
            item {
                Text(
                    text = "SANCTUARY ECONOMY",
                    style = typography.caption,
                    color = colors.textTertiary,
                    modifier = Modifier.padding(start = spacing.xs, bottom = spacing.xs)
                )
                KintsuGroupedSection {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(spacing.m),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(spacing.s),
                            modifier = Modifier.weight(1f).padding(end = spacing.s)
                        ) {
                            IconGoldSeam(color = colors.accent, size = 24.dp)
                            Column {
                                Text(
                                    text = "${settings.coinsBalance} Kintsugi Coins",
                                    style = typography.itemTitle,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "Earned from completions, streaks & pauses",
                                    style = typography.caption,
                                    color = colors.textSecondary
                                )
                            }
                        }
                        KintsuButton(
                            text = "Open Shop",
                            onClick = onOpenShop,
                            variant = KintsuButtonVariant.ACCENT_SOFT
                        )
                    }
                }
            }

            // Mindful Focus Timer
            item {
                Text(
                    text = "MINDFUL FOCUS",
                    style = typography.caption,
                    color = colors.textTertiary,
                    modifier = Modifier.padding(start = spacing.xs, bottom = spacing.xs)
                )
                KintsuGroupedSection {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(spacing.m),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Focus Timer Module",
                                style = typography.itemTitle,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "Conscious intervals with foreground tracking",
                                style = typography.secondary,
                                color = colors.textSecondary
                            )
                        }
                        Switch(
                            checked = settings.focusEnabled,
                            onCheckedChange = onUpdateFocusEnabled,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = colors.background,
                                checkedTrackColor = colors.accent,
                                uncheckedThumbColor = colors.textTertiary,
                                uncheckedTrackColor = colors.surfaceElevated
                            )
                        )
                    }

                    if (settings.focusEnabled) {
                        KintsuDivider()

                        Column(modifier = Modifier.padding(spacing.m)) {
                            Text(
                                text = "Default Session Length",
                                style = typography.itemTitle,
                                color = colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(spacing.xs))
                            Row(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
                                listOf(15, 25, 45).forEach { min ->
                                    KintsuFilterChip(
                                        label = "$min min",
                                        selected = settings.focusDefaultPresetMinutes == min,
                                        onClick = { onUpdateFocusDefaultPreset(min) }
                                    )
                                }
                            }
                        }

                        KintsuDivider()

                        Column(modifier = Modifier.padding(spacing.m)) {
                            Text(
                                text = "Short Rest Interval",
                                style = typography.itemTitle,
                                color = colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(spacing.xs))
                            Row(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
                                listOf(3, 5, 10).forEach { min ->
                                    KintsuFilterChip(
                                        label = "$min min",
                                        selected = settings.focusBreakMinutes == min,
                                        onClick = { onUpdateFocusBreakMinutes(min) }
                                    )
                                }
                            }
                        }

                        KintsuDivider()

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(spacing.m),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Vibration Cue",
                                    style = typography.itemTitle,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "Gentle haptic pulse on completion",
                                    style = typography.secondary,
                                    color = colors.textSecondary
                                )
                            }
                            Switch(
                                checked = settings.focusVibrationEnabled,
                                onCheckedChange = onUpdateFocusVibrationEnabled,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = colors.background,
                                    checkedTrackColor = colors.accent,
                                    uncheckedThumbColor = colors.textTertiary,
                                    uncheckedTrackColor = colors.surfaceElevated
                                )
                            )
                        }
                    }
                }
            }

            // Daily Intention
            item {
                Text(
                    text = "DAILY INTENTION",
                    style = typography.caption,
                    color = colors.textTertiary,
                    modifier = Modifier.padding(start = spacing.xs, bottom = spacing.xs)
                )
                KintsuGroupedSection {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(spacing.m),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Daily Intention Prompt",
                                style = typography.itemTitle,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "Morning intention on Home (\"What matters most?\")",
                                style = typography.secondary,
                                color = colors.textSecondary
                            )
                        }
                        Switch(
                            checked = settings.dailyIntentionEnabled,
                            onCheckedChange = onUpdateDailyIntentionEnabled,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = colors.background,
                                checkedTrackColor = colors.accent,
                                uncheckedThumbColor = colors.textTertiary,
                                uncheckedTrackColor = colors.surfaceElevated
                            )
                        )
                    }

                    if (settings.dailyIntentionEnabled) {
                        KintsuDivider()
                        Column(modifier = Modifier.padding(spacing.m)) {
                            Text(
                                text = "Morning Intention Reminder",
                                style = typography.itemTitle,
                                color = colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(spacing.xs))
                            Row(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
                                listOf("07:00", "08:00", "09:00").forEach { time ->
                                    KintsuFilterChip(
                                        label = time,
                                        selected = settings.dailyIntentionReminderTime == time,
                                        onClick = { onUpdateDailyIntentionReminderTime(time) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Quiet Journal
            item {
                Text(
                    text = "QUIET JOURNAL",
                    style = typography.caption,
                    color = colors.textTertiary,
                    modifier = Modifier.padding(start = spacing.xs, bottom = spacing.xs)
                )
                KintsuGroupedSection {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(spacing.m),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Evening Reflection",
                                style = typography.itemTitle,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "Private, local end-of-day journal",
                                style = typography.secondary,
                                color = colors.textSecondary
                            )
                        }
                        Switch(
                            checked = settings.journalEnabled,
                            onCheckedChange = onUpdateJournalEnabled,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = colors.background,
                                checkedTrackColor = colors.accent,
                                uncheckedThumbColor = colors.textTertiary,
                                uncheckedTrackColor = colors.surfaceElevated
                            )
                        )
                    }

                    if (settings.journalEnabled) {
                        KintsuDivider()
                        Column(modifier = Modifier.padding(spacing.m)) {
                            Text(
                                text = "Evening Reflection Reminder",
                                style = typography.itemTitle,
                                color = colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(spacing.xs))
                            Row(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
                                listOf("20:00", "21:00", "22:00").forEach { time ->
                                    KintsuFilterChip(
                                        label = time,
                                        selected = settings.journalReminderTime == time,
                                        onClick = { onUpdateJournalReminderTime(time) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Weekly Review
            item {
                Text(
                    text = "WEEKLY REVIEW & PERSPECTIVE",
                    style = typography.caption,
                    color = colors.textTertiary,
                    modifier = Modifier.padding(start = spacing.xs, bottom = spacing.xs)
                )
                KintsuGroupedSection {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(spacing.m),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Weekly Review",
                                style = typography.itemTitle,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "End-of-week perspective from real stored data",
                                style = typography.secondary,
                                color = colors.textSecondary
                            )
                        }
                        Switch(
                            checked = settings.weeklyReviewEnabled,
                            onCheckedChange = onUpdateWeeklyReviewEnabled,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = colors.background,
                                checkedTrackColor = colors.accent,
                                uncheckedThumbColor = colors.textTertiary,
                                uncheckedTrackColor = colors.surfaceElevated
                            )
                        )
                    }

                    if (settings.weeklyReviewEnabled) {
                        KintsuDivider()
                        Column(modifier = Modifier.padding(spacing.m)) {
                            Text(
                                text = "Weekend Review Reminder",
                                style = typography.itemTitle,
                                color = colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(spacing.xs))
                            Row(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
                                listOf("17:00", "18:00", "19:00", "20:00").forEach { time ->
                                    KintsuFilterChip(
                                        label = time,
                                        selected = settings.weeklyReviewReminderTime == time,
                                        onClick = { onUpdateWeeklyReviewReminderTime(time) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Data & Storage Section
            item {
                Text(
                    text = "DATA & STORAGE",
                    style = typography.caption,
                    color = colors.textTertiary,
                    modifier = Modifier.padding(start = spacing.xs, bottom = spacing.xs)
                )
                KintsuGroupedSection {
                    // Export to JSON file via SAF
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                createDocLauncher.launch("kintsu-backup-${System.currentTimeMillis()}.json")
                            }
                            .padding(spacing.m),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Export Backup File",
                                style = typography.itemTitle,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "Save versioned JSON backup to device storage",
                                style = typography.secondary,
                                color = colors.textSecondary
                            )
                        }
                    }

                    KintsuDivider()

                    // Import from JSON file via SAF
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                openDocLauncher.launch(arrayOf("application/json", "text/*", "*/*"))
                            }
                            .padding(spacing.m),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Import Backup File",
                                style = typography.itemTitle,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "Restore from file with Replace or Merge choice",
                                style = typography.secondary,
                                color = colors.textSecondary
                            )
                        }
                    }

                    KintsuDivider()

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                scope.launch {
                                    exportedJson = onExportData()
                                    showExportSheet = true
                                }
                            }
                            .padding(spacing.m),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "View Raw JSON",
                                style = typography.itemTitle,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "Inspect raw JSON and copy to clipboard",
                                style = typography.secondary,
                                color = colors.textSecondary
                            )
                        }
                    }

                    KintsuDivider()

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                resetConfirmationText = ""
                                resetConfirmedCheckbox = false
                                showResetCautionDialog = true
                            }
                            .padding(spacing.m),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Reset All Data",
                                style = typography.itemTitle,
                                color = colors.error
                            )
                            Text(
                                text = "Erase all habits, tasks, logs, and settings",
                                style = typography.secondary,
                                color = colors.textSecondary
                            )
                        }
                    }
                }
            }

            // Danger Zone Section at Bottom of Settings
            item {
                Spacer(modifier = Modifier.height(spacing.l))
                Text(
                    text = "DANGER ZONE",
                    style = typography.caption,
                    color = colors.error,
                    modifier = Modifier.padding(start = spacing.xs, bottom = spacing.xs)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(KintsuTheme.shapes.large)
                        .background(colors.error.copy(alpha = 0.05f))
                        .border(1.5.dp, colors.error.copy(alpha = 0.35f), KintsuTheme.shapes.large)
                        .padding(spacing.m)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(spacing.m)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(spacing.s)
                        ) {
                            IconTrash(color = colors.error, size = 20.dp)
                            Text(
                                text = "Factory Reset & Account Deletion",
                                style = typography.sectionHeader,
                                color = colors.error
                            )
                        }

                        Text(
                            text = "Actions in this section are destructive and cannot be undone. Please proceed with extreme caution.",
                            style = typography.caption,
                            color = colors.textSecondary
                        )

                        // 1. Reset App Data
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(KintsuTheme.shapes.medium)
                                .background(colors.surfaceElevated)
                                .padding(spacing.m),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = spacing.s)) {
                                Text(
                                    text = "Reset All Data",
                                    style = typography.itemTitle,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "Wipes all local habits, tasks, goals, journals, and wellbeing rules. Coins balance reset to 100.",
                                    style = typography.caption,
                                    color = colors.textSecondary
                                )
                            }
                            Button(
                                onClick = {
                                    resetConfirmationText = ""
                                    resetConfirmedCheckbox = false
                                    showResetCautionDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.error.copy(alpha = 0.15f),
                                    contentColor = colors.error
                                ),
                                shape = KintsuTheme.shapes.medium
                            ) {
                                Text("Reset Data")
                            }
                        }

                        // 2. Delete Account (2-Step Verification)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(KintsuTheme.shapes.medium)
                                .background(colors.surfaceElevated)
                                .padding(spacing.m),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = spacing.s)) {
                                Text(
                                    text = "Delete Account",
                                    style = typography.itemTitle,
                                    color = colors.error
                                )
                                Text(
                                    text = "Irrevocably deletes cloud account, purges all remote sync data, and wipes this device (2-step verification).",
                                    style = typography.caption,
                                    color = colors.textSecondary
                                )
                            }
                            Button(
                                onClick = {
                                    deleteAccountConfirmText = ""
                                    showDeleteAccountStep1 = true
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.error,
                                    contentColor = Color.White
                                ),
                                shape = KintsuTheme.shapes.medium
                            ) {
                                Text("Delete Account")
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(spacing.xxxl))
            }
        }
    }

    if (showExportSheet) {
        KintsuBottomSheet(
            onDismissRequest = { showExportSheet = false },
            title = "Exported JSON Data"
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.m)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .clip(KintsuTheme.shapes.medium)
                        .background(colors.surfaceElevated)
                        .padding(spacing.m)
                ) {
                    Text(
                        text = exportedJson,
                        style = typography.secondary,
                        color = colors.textPrimary
                    )
                }

                KintsuButton(
                    text = "Copy to Clipboard",
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Kintsu Backup", exportedJson))
                        Toast.makeText(context, "Copied backup to clipboard", Toast.LENGTH_SHORT).show()
                        showExportSheet = false
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    if (showResetCautionDialog) {
        AlertDialog(
            onDismissRequest = {
                showResetCautionDialog = false
                resetConfirmationText = ""
                resetConfirmedCheckbox = false
            },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.s)
                ) {
                    IconTrash(color = colors.error, size = 22.dp)
                    Text(
                        text = "Reset All Data?",
                        style = typography.sectionHeader,
                        color = colors.error
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(spacing.s)
                ) {
                    Text(
                        text = "CRITICAL WARNING: This action will permanently wipe ALL local data from this device. It cannot be recovered:",
                        style = typography.caption,
                        color = colors.textPrimary
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(KintsuTheme.shapes.medium)
                            .background(colors.error.copy(alpha = 0.08f))
                            .border(1.dp, colors.error.copy(alpha = 0.25f), KintsuTheme.shapes.medium)
                            .padding(spacing.s)
                    ) {
                        Text(
                            text = "• All habits, streaks, and repair logs\n• All tasks, subtasks, and schedule blocks\n• All goals, journal entries, and reviews\n• All focus sessions & wellbeing logs\n• All shop purchases (coins reset to 100 starter coins)",
                            style = typography.secondary,
                            color = colors.textPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(spacing.xs))
                    Text(
                        text = "To confirm, type RESET in all capital letters below:",
                        style = typography.caption,
                        color = colors.textSecondary
                    )
                    OutlinedTextField(
                        value = resetConfirmationText,
                        onValueChange = { resetConfirmationText = it },
                        placeholder = { Text("RESET", color = colors.textSecondary.copy(alpha = 0.5f)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.error,
                            unfocusedBorderColor = colors.border,
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { resetConfirmedCheckbox = !resetConfirmedCheckbox }
                            .padding(vertical = spacing.xs),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = resetConfirmedCheckbox,
                            onCheckedChange = { resetConfirmedCheckbox = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = colors.error,
                                checkmarkColor = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.width(spacing.xs))
                        Text(
                            text = "I understand that all local data will be permanently wiped.",
                            style = typography.caption,
                            color = colors.textPrimary
                        )
                    }
                }
            },
            confirmButton = {
                val isResetReady = resetConfirmationText.trim() == "RESET" && resetConfirmedCheckbox
                Button(
                    onClick = {
                        onResetData()
                        showResetCautionDialog = false
                        resetConfirmationText = ""
                        resetConfirmedCheckbox = false
                        Toast.makeText(context, "All data has been reset to defaults", Toast.LENGTH_LONG).show()
                    },
                    enabled = isResetReady,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.error,
                        contentColor = Color.White,
                        disabledContainerColor = colors.error.copy(alpha = 0.2f),
                        disabledContentColor = Color.White.copy(alpha = 0.5f)
                    ),
                    shape = KintsuTheme.shapes.medium
                ) {
                    Text("Wipe & Reset Everything")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showResetCautionDialog = false
                        resetConfirmationText = ""
                        resetConfirmedCheckbox = false
                    }
                ) {
                    Text("Cancel", color = colors.textPrimary)
                }
            },
            containerColor = colors.surface,
            shape = KintsuTheme.shapes.large
        )
    }

    if (showDeleteAccountStep1) {
        AlertDialog(
            onDismissRequest = {
                showDeleteAccountStep1 = false
                deleteAccountConfirmText = ""
            },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.s)
                ) {
                    IconTrash(color = colors.error, size = 22.dp)
                    Text(
                        text = "Delete Account (Step 1/2)",
                        style = typography.sectionHeader,
                        color = colors.error
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(spacing.s)
                ) {
                    val accountIdentifier = settings.accountEmail?.takeIf { it.isNotBlank() } ?: settings.displayName.ifBlank { "Local Profile" }
                    Text(
                        text = "You are initiating permanent deletion for account: $accountIdentifier",
                        style = typography.body,
                        color = colors.textPrimary
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(KintsuTheme.shapes.medium)
                            .background(colors.error.copy(alpha = 0.08f))
                            .border(1.dp, colors.error.copy(alpha = 0.25f), KintsuTheme.shapes.medium)
                            .padding(spacing.s)
                    ) {
                        Text(
                            text = "• All remote cloud sync backups will be permanently purged\n• Authentication session tokens will be permanently revoked\n• All local device data, habits, tasks, and coins will be wiped\n• This action cannot be reversed or undone",
                            style = typography.secondary,
                            color = colors.textPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(spacing.xs))
                    Text(
                        text = "To proceed to final confirmation, type DELETE below:",
                        style = typography.caption,
                        color = colors.textSecondary
                    )
                    OutlinedTextField(
                        value = deleteAccountConfirmText,
                        onValueChange = { deleteAccountConfirmText = it },
                        placeholder = { Text("DELETE", color = colors.textSecondary.copy(alpha = 0.5f)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.error,
                            unfocusedBorderColor = colors.border,
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                val isDeleteReady = deleteAccountConfirmText.trim() == "DELETE"
                Button(
                    onClick = {
                        showDeleteAccountStep1 = false
                        showDeleteAccountStep2 = true
                    },
                    enabled = isDeleteReady,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.error,
                        contentColor = Color.White,
                        disabledContainerColor = colors.error.copy(alpha = 0.2f),
                        disabledContentColor = Color.White.copy(alpha = 0.5f)
                    ),
                    shape = KintsuTheme.shapes.medium
                ) {
                    Text("Proceed to Step 2 →")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteAccountStep1 = false
                        deleteAccountConfirmText = ""
                    }
                ) {
                    Text("Cancel", color = colors.textPrimary)
                }
            },
            containerColor = colors.surface,
            shape = KintsuTheme.shapes.large
        )
    }

    if (showDeleteAccountStep2) {
        AlertDialog(
            onDismissRequest = {
                if (!deleteAccountLoading) {
                    showDeleteAccountStep2 = false
                    deleteAccountConfirmText = ""
                }
            },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.s)
                ) {
                    IconTrash(color = colors.error, size = 22.dp)
                    Text(
                        text = "Irreversible Deletion (Step 2/2)",
                        style = typography.sectionHeader,
                        color = colors.error
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(spacing.s)
                ) {
                    Text(
                        text = "FINAL WARNING: Once you press the button below, your account, credentials, remote backups, and local records will be immediately destroyed.",
                        style = typography.body,
                        color = colors.error
                    )
                    Text(
                        text = "There is no undo, recovery window, or grace period.",
                        style = typography.caption,
                        color = colors.textSecondary
                    )

                    if (deleteAccountLoading) {
                        Spacer(modifier = Modifier.height(spacing.s))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(spacing.s)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = colors.error,
                                strokeWidth = 2.dp
                            )
                            Text(
                                text = "Purging account & wiping data...",
                                style = typography.secondary,
                                color = colors.textPrimary
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        deleteAccountLoading = true
                        onDeleteAccount { result ->
                            deleteAccountLoading = false
                            showDeleteAccountStep2 = false
                            deleteAccountConfirmText = ""
                            result.fold(
                                onSuccess = {
                                    Toast.makeText(context, "Account permanently deleted and device data wiped", Toast.LENGTH_LONG).show()
                                },
                                onFailure = { err ->
                                    Toast.makeText(context, "Device wiped. Notice: ${err.localizedMessage ?: "Completed"}", Toast.LENGTH_LONG).show()
                                }
                            )
                        }
                    },
                    enabled = !deleteAccountLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.error,
                        contentColor = Color.White
                    ),
                    shape = KintsuTheme.shapes.medium
                ) {
                    Text("Permanently Delete Account")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        if (!deleteAccountLoading) {
                            showDeleteAccountStep2 = false
                            deleteAccountConfirmText = ""
                        }
                    },
                    enabled = !deleteAccountLoading
                ) {
                    Text("Cancel / Keep Account", color = colors.textPrimary)
                }
            },
            containerColor = colors.surface,
            shape = KintsuTheme.shapes.large
        )
    }

    if (showImportConfirmDialog && pendingImportUri != null) {
        AlertDialog(
            onDismissRequest = { showImportConfirmDialog = false },
            title = {
                Text(
                    text = "Restore Backup",
                    style = typography.sectionHeader,
                    color = colors.textPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(spacing.s)) {
                    Text(
                        text = "Backup contains ${pendingImportData?.habits?.size ?: 0} habits, ${pendingImportData?.tasks?.size ?: 0} tasks, and ${pendingImportData?.goals?.size ?: 0} goals.",
                        style = typography.body,
                        color = colors.textSecondary
                    )
                    Text(
                        text = "Choose how to apply:\n• Replace: Clears current data and restores the backup\n• Merge: Combines backup data with existing items",
                        style = typography.secondary,
                        color = colors.textSecondary
                    )
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
                    TextButton(
                        onClick = {
                            val uri = pendingImportUri ?: return@TextButton
                            scope.launch {
                                val res = onImportFromFile(uri, ImportMode.MERGE)
                                showImportConfirmDialog = false
                                if (res.isSuccess) {
                                    Toast.makeText(context, "Data merged successfully", Toast.LENGTH_SHORT).show()
                                } else {
                                    importErrorMessage = res.exceptionOrNull()?.localizedMessage ?: "Merge failed"
                                }
                            }
                        }
                    ) {
                        Text("Merge", color = colors.accent)
                    }
                    TextButton(
                        onClick = {
                            val uri = pendingImportUri ?: return@TextButton
                            scope.launch {
                                val res = onImportFromFile(uri, ImportMode.REPLACE)
                                showImportConfirmDialog = false
                                if (res.isSuccess) {
                                    Toast.makeText(context, "Backup restored (replaced)", Toast.LENGTH_SHORT).show()
                                } else {
                                    importErrorMessage = res.exceptionOrNull()?.localizedMessage ?: "Restore failed"
                                }
                            }
                        }
                    ) {
                        Text("Replace", color = colors.goldSeam)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportConfirmDialog = false }) {
                    Text("Cancel", color = colors.textPrimary)
                }
            },
            containerColor = colors.surface,
            shape = KintsuTheme.shapes.large
        )
    }

    importErrorMessage?.let { error ->
        AlertDialog(
            onDismissRequest = { importErrorMessage = null },
            title = {
                Text(
                    text = "Import Notice",
                    style = typography.sectionHeader,
                    color = colors.textPrimary
                )
            },
            text = {
                Text(
                    text = error,
                    style = typography.body,
                    color = colors.textSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = { importErrorMessage = null }) {
                    Text("OK", color = colors.accent)
                }
            },
            containerColor = colors.surface,
            shape = KintsuTheme.shapes.large
        )
    }

    // --- V3.0 Account & Sync Dialogs and Auth Sheet ---

    if (showAuthSheet) {
        var passwordVisible by remember { mutableStateOf(false) }
        KintsuBottomSheet(
            onDismissRequest = {
                if (!authLoading) showAuthSheet = false
            },
            title = if (authModeIsSignUp) "Create Cloud Account" else "Sign In to Kintsu"
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(spacing.m)
            ) {
                // Mode Toggle (Sign In vs Create Account)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing.xs)
                ) {
                    KintsuFilterChip(
                        label = "Sign In",
                        selected = !authModeIsSignUp,
                        onClick = {
                            authModeIsSignUp = false
                            authError = null
                        }
                    )
                    KintsuFilterChip(
                        label = "Create Account",
                        selected = authModeIsSignUp,
                        onClick = {
                            authModeIsSignUp = true
                            authError = null
                        }
                    )
                }

                Spacer(modifier = Modifier.height(spacing.m))

                // Preserving data notice
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(KintsuTheme.shapes.medium)
                        .background(colors.accentSoft.copy(alpha = 0.5f))
                        .padding(spacing.s)
                ) {
                    Text(
                        text = "Your existing local rituals, tasks and reflections will be safely preserved and merged into your account.",
                        style = typography.caption,
                        color = colors.textPrimary
                    )
                }

                Spacer(modifier = Modifier.height(spacing.m))

                // Email
                OutlinedTextField(
                    value = authEmail,
                    onValueChange = { authEmail = it; authError = null },
                    label = { Text("Email Address") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = KintsuTheme.shapes.medium,
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(spacing.s))

                // Password
                OutlinedTextField(
                    value = authPassword,
                    onValueChange = { authPassword = it; authError = null },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = KintsuTheme.shapes.medium,
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Text(
                                text = if (passwordVisible) "Hide" else "Show",
                                style = typography.caption,
                                color = colors.accentText
                            )
                        }
                    }
                )

                if (authModeIsSignUp) {
                    Spacer(modifier = Modifier.height(spacing.s))
                    OutlinedTextField(
                        value = authDisplayName,
                        onValueChange = { authDisplayName = it },
                        label = { Text("Display Name (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = KintsuTheme.shapes.medium,
                        singleLine = true
                    )
                }

                if (!authError.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(spacing.s))
                    Text(
                        text = authError ?: "",
                        style = typography.caption,
                        color = colors.error
                    )
                }

                Spacer(modifier = Modifier.height(spacing.l))

                // Action Buttons
                KintsuButton(
                    text = when {
                        authLoading -> "Connecting..."
                        authModeIsSignUp -> "Create Account & Sync"
                        else -> "Sign In & Sync"
                    },
                    onClick = {
                        if (authEmail.isBlank() || authPassword.isBlank()) {
                            authError = "Please enter both email and password"
                            return@KintsuButton
                        }
                        authLoading = true
                        authError = null
                        if (authModeIsSignUp) {
                            onSignUp(authEmail.trim(), authPassword, authDisplayName.ifBlank { null }) { res ->
                                authLoading = false
                                if (res.isSuccess) {
                                    showAuthSheet = false
                                    Toast.makeText(context, "Account created and sync enabled", Toast.LENGTH_SHORT).show()
                                } else {
                                    authError = res.exceptionOrNull()?.localizedMessage ?: "Failed to create account"
                                }
                            }
                        } else {
                            onSignIn(authEmail.trim(), authPassword) { res ->
                                authLoading = false
                                if (res.isSuccess) {
                                    showAuthSheet = false
                                    Toast.makeText(context, "Signed in and sync enabled", Toast.LENGTH_SHORT).show()
                                } else {
                                    authError = res.exceptionOrNull()?.localizedMessage ?: "Failed to sign in"
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !authLoading,
                    variant = KintsuButtonVariant.PRIMARY
                )

                Spacer(modifier = Modifier.height(spacing.s))

                // Seamless Google One-Tap Sign In
                Button(
                    onClick = {
                        showAuthSheet = false
                        showGoogleOneTapSheet = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.surfaceElevated,
                        contentColor = colors.textPrimary
                    ),
                    shape = CircleShape,
                    border = androidx.compose.foundation.BorderStroke(1.dp, colors.border)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(spacing.s)
                    ) {
                        GoogleOneTapLogo(size = 20.dp)
                        Text(
                            text = "Continue with Google",
                            style = typography.body.copy(fontWeight = FontWeight.Medium)
                        )
                    }
                }
            }
        }
    }

    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = {
                Text(
                    text = "Sign Out of Account",
                    style = typography.sectionHeader,
                    color = colors.textPrimary
                )
            },
            text = {
                Text(
                    text = "Your local records will remain intact on this device. Future changes will not sync to the cloud until you sign in again.",
                    style = typography.body,
                    color = colors.textSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onSignOut(true)
                        showSignOutDialog = false
                        Toast.makeText(context, "Signed out", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Sign Out", color = colors.accent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text("Cancel", color = colors.textPrimary)
                }
            },
            containerColor = colors.surface,
            shape = KintsuTheme.shapes.large
        )
    }

    if (showDeleteCloudDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteCloudDialog = false },
            title = {
                Text(
                    text = "Delete Cloud Data",
                    style = typography.sectionHeader,
                    color = colors.textPrimary
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to wipe your backed-up data from the cloud? Local records on this device will NOT be deleted.",
                    style = typography.body,
                    color = colors.textSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteCloudData { res ->
                            if (res.isSuccess) {
                                Toast.makeText(context, "Cloud copy removed", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Cloud deletion notice: ${res.exceptionOrNull()?.localizedMessage}", Toast.LENGTH_LONG).show()
                            }
                        }
                        showDeleteCloudDialog = false
                    }
                ) {
                    Text("Delete Cloud Copy", color = Color(0xFFEF4444))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteCloudDialog = false }) {
                    Text("Cancel", color = colors.textPrimary)
                }
            },
            containerColor = colors.surface,
            shape = KintsuTheme.shapes.large
        )
    }

    if (showPermissionSheet) {
        PermissionRequestSheet(
            onDismissRequest = { showPermissionSheet = false },
            onContinue = { showPermissionSheet = false }
        )
    }

    if (showGoogleOneTapSheet) {
        GoogleOneTapBottomSheet(
            onDismissRequest = { showGoogleOneTapSheet = false },
            onAccountSelected = { email, name ->
                authLoading = true
                val token = "google_${email.trim()}"
                onSignInWithGoogle(token, email.trim(), name.trim()) { res ->
                    authLoading = false
                    showGoogleOneTapSheet = false
                    if (res.isSuccess) {
                        showAuthSheet = false
                        Toast.makeText(context, "Signed in with Google as $email", Toast.LENGTH_SHORT).show()
                    } else {
                        val errMsg = res.exceptionOrNull()?.localizedMessage ?: "Google sign in failed"
                        Toast.makeText(context, errMsg, Toast.LENGTH_SHORT).show()
                    }
                }
            },
            defaultDisplayName = settings.displayName,
            isLoading = authLoading
        )
    }

    if (showEndpointDialog) {
        var tempEndpoint by remember { mutableStateOf(settings.cloudEndpointUrl) }
        AlertDialog(
            onDismissRequest = { showEndpointDialog = false },
            title = {
                Text(
                    text = "Cloud Server Endpoint",
                    style = typography.sectionHeader,
                    color = colors.textPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(spacing.m)) {
                    Text(
                        text = "Specify the backend sync server URL. Select a quick preset or enter custom host.",
                        style = typography.body,
                        color = colors.textSecondary
                    )
                    OutlinedTextField(
                        value = tempEndpoint,
                        onValueChange = { tempEndpoint = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = KintsuTheme.shapes.medium,
                        singleLine = true
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(spacing.s)) {
                        KintsuButton(
                            text = "USB Reverse",
                            onClick = { tempEndpoint = "http://127.0.0.1:8080" },
                            variant = KintsuButtonVariant.SECONDARY
                        )
                        KintsuButton(
                            text = "Wi-Fi LAN",
                            onClick = { tempEndpoint = "http://192.168.29.155:8080" },
                            variant = KintsuButtonVariant.SECONDARY
                        )
                    }
                }
            },
            confirmButton = {
                KintsuButton(
                    text = "Save Endpoint",
                    onClick = {
                        onUpdateCloudConfig(tempEndpoint.trim().trimEnd('/'), settings.cloudApiKey)
                        showEndpointDialog = false
                        Toast.makeText(context, "Server endpoint updated", Toast.LENGTH_SHORT).show()
                    },
                    variant = KintsuButtonVariant.PRIMARY
                )
            },
            dismissButton = {
                TextButton(onClick = { showEndpointDialog = false }) {
                    Text("Cancel", color = colors.textPrimary)
                }
            },
            containerColor = colors.surface,
            shape = KintsuTheme.shapes.large
        )
    }

    if (showWidgetShowcaseSheet) {
        WidgetShowcaseSheet(
            onDismiss = { showWidgetShowcaseSheet = false }
        )
    }
}
}
