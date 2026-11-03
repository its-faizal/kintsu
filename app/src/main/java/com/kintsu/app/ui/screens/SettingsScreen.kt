package com.kintsu.app.ui.screens

import android.content.Intent
import android.provider.Settings
import com.kintsu.app.wellbeing.domain.PermissionStateHelper
import com.kintsu.app.wellbeing.domain.ScreenTimeHelper
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.kintsu.app.ui.components.IconLeaf
import com.kintsu.app.ui.components.IconGoal
import com.kintsu.app.ui.components.IconHabit
import com.kintsu.app.ui.components.IconFlame
import com.kintsu.app.ui.components.IconBook
import com.kintsu.app.ui.components.IconShield
import com.kintsu.app.ui.components.IconUser
import com.kintsu.app.ui.components.ProfileAvatarVisual
import com.kintsu.app.ui.components.SIGNATURE_AVATARS_24

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import com.kintsu.app.ui.components.VisualPatternCanvas
import com.kintsu.app.util.PermissionHelper
import com.kintsu.app.data.BackupData
import com.kintsu.app.data.BackupRestoreManager
import com.kintsu.app.security.AppLockManager
import com.kintsu.app.security.AppLockType
import com.kintsu.app.data.SecureEncryptedBackupManager
import com.kintsu.app.data.SecureBackupInfo
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
    onUpdateProfileAvatar: (String) -> Unit = {},
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
    onUpdateAppLockEnabled: (Boolean) -> Unit = {},
    onUpdateAppLockType: (String) -> Unit = {},
    onUpdateAppLockCode: (String) -> Unit = {},
    onUpdateAppLockBiometricEnabled: (Boolean) -> Unit = {},
    onTriggerEncryptedBackup: (onResult: (Result<SecureBackupInfo>) -> Unit) -> Unit = {},
    onRestoreEncryptedBackup: (onResult: (Result<Int>) -> Unit) -> Unit = {},
    onExportData: suspend () -> String,
    onExportToFile: suspend (Uri) -> Unit = {},
    onImportFromFile: suspend (Uri, ImportMode) -> Result<Unit> = { _, _ -> Result.success(Unit) },
    onOpenDigitalBalance: () -> Unit = {},
    onOpenWellbeingWizard: () -> Unit = {},
    onOpenDetox: () -> Unit = {},
    onOpenQuitHabits: () -> Unit = {},
    onOpenShop: () -> Unit = {},
    stylePresets: List<com.kintsu.app.data.model.StylePresetEntity> = emptyList(),
    onApplyStylePreset: (com.kintsu.app.data.model.StylePresetEntity) -> Unit = {},
    onSaveStylePreset: (String, String, String, String, String, String) -> Unit = { _, _, _, _, _, _ -> },
    onDeleteStylePreset: (com.kintsu.app.data.model.StylePresetEntity) -> Unit = {},
    onRestoreDefaultPresets: () -> Unit = {},
    onUpdateLauncherIcon: (String) -> Unit = {},
    allAchievements: List<com.kintsu.app.data.model.AchievementEntity> = emptyList(),
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
    var showAchievementsSheet by remember { mutableStateOf(false) }

    var isEditingName by remember { mutableStateOf(false) }
    var showAvatarPicker by remember { mutableStateOf(false) }
    var tempName by remember { mutableStateOf(settings.displayName) }

    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }
    var pendingImportData by remember { mutableStateOf<BackupData?>(null) }
    var showImportConfirmDialog by remember { mutableStateOf(false) }
    var importErrorMessage by remember { mutableStateOf<String?>(null) }

    var showAuthSheet by remember { mutableStateOf(false) }
    var showSetPasscodeDialog by remember { mutableStateOf(false) }
    var tempPasscode by remember { mutableStateOf("") }
    var tempPasscodeConfirm by remember { mutableStateOf("") }
    var passcodeDialogError by remember { mutableStateOf<String?>(null) }
    var showRestoreEncryptedConfirmDialog by remember { mutableStateOf(false) }
    var isBackingUp by remember { mutableStateOf(false) }
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
    // Endpoint dialog removed

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
    var hasUsageAccessPermission by remember {
        mutableStateOf(ScreenTimeHelper.hasUsageStatsPermission(context))
    }
    var hasAccessibilityPermission by remember {
        mutableStateOf(PermissionStateHelper.isAccessibilityEnabled(context))
    }
    var hasOverlayPermission by remember {
        mutableStateOf(PermissionStateHelper.isOverlayGranted(context))
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasNotificationPermission = PermissionHelper.isNotificationPermissionGranted(context)
                hasExactAlarmPermission = PermissionHelper.isExactAlarmPermissionGranted(context)
                isBatteryUnrestricted = PermissionHelper.isBatteryOptimizationIgnored(context)
                hasUsageAccessPermission = ScreenTimeHelper.hasUsageStatsPermission(context)
                hasAccessibilityPermission = PermissionStateHelper.isAccessibilityEnabled(context)
                hasOverlayPermission = PermissionStateHelper.isOverlayGranted(context)
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
                    KintsuDivider()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showAvatarPicker = true }
                            .padding(spacing.m),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(spacing.m)
                        ) {
                            ProfileAvatarVisual(
                                avatarId = settings.profileAvatarId,
                                size = 44.dp,
                                iconSize = 22.dp
                            )
                            Column {
                                Text(
                                    text = "Profile Picture",
                                    style = typography.itemTitle,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "24 signature avatars or your own photo",
                                    style = typography.caption,
                                    color = colors.textSecondary
                                )
                            }
                        }

                        KintsuButton(
                            text = "Change",
                            onClick = { showAvatarPicker = true },
                            variant = KintsuButtonVariant.SECONDARY
                        )
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

                        

                    }
                }
            }

            // Security & App Lock Section (PIN / Pattern / Password + Biometric)
            item {
                Text(
                    text = "SECURITY & APP LOCK",
                    style = typography.caption,
                    color = colors.textTertiary,
                    modifier = Modifier.padding(start = spacing.xs, bottom = spacing.xs)
                )
                KintsuGroupedSection {
                    // App Lock Enable Switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(spacing.m),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = spacing.m)) {
                            Text(
                                text = "App Lock",
                                style = typography.itemTitle,
                                color = colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (settings.appLockEnabled) "Sanctuary protected with ${settings.appLockType}" else "Require passcode to access your rituals and journal",
                                style = typography.caption,
                                color = colors.textSecondary
                            )
                        }

                        Switch(
                            checked = settings.appLockEnabled,
                            onCheckedChange = { enable ->
                                if (enable && settings.appLockCode.isBlank()) {
                                    showSetPasscodeDialog = true
                                } else {
                                    onUpdateAppLockEnabled(enable)
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = colors.background,
                                checkedTrackColor = colors.goldSeam,
                                uncheckedThumbColor = colors.textTertiary,
                                uncheckedTrackColor = colors.surfaceElevated
                            )
                        )
                    }

                    if (settings.appLockEnabled || settings.appLockCode.isNotBlank()) {
                        KintsuDivider()

                        // Lock Type Selector (PIN / Pattern / Password)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(spacing.m),
                            verticalArrangement = Arrangement.spacedBy(spacing.xs)
                        ) {
                            Text(
                                text = "Lock Mechanism",
                                style = typography.caption,
                                color = colors.textSecondary
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
                                listOf("PIN", "PATTERN", "PASSWORD").forEach { type ->
                                    val isSelected = settings.appLockType.equals(type, ignoreCase = true)
                                    KintsuFilterChip(
                                        label = type,
                                        selected = isSelected,
                                        onClick = {
                                            onUpdateAppLockType(type)
                                            if (settings.appLockCode.isBlank()) {
                                                showSetPasscodeDialog = true
                                            }
                                        }
                                    )
                                }
                            }
                        }

                        KintsuDivider()

                        // Passcode Management Button
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(spacing.m),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = spacing.s)) {
                                Text(
                                    text = if (settings.appLockCode.isNotBlank()) "Change ${settings.appLockType}" else "Set ${settings.appLockType}",
                                    style = typography.itemTitle,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = if (settings.appLockCode.isNotBlank()) "Status: Active & Hashed" else "Action needed: configure passcode",
                                    style = typography.caption,
                                    color = if (settings.appLockCode.isNotBlank()) colors.accent else colors.warning
                                )
                            }

                            KintsuButton(
                                text = if (settings.appLockCode.isNotBlank()) "Change" else "Configure",
                                onClick = {
                                    tempPasscode = ""
                                    tempPasscodeConfirm = ""
                                    passcodeDialogError = null
                                    showSetPasscodeDialog = true
                                },
                                variant = KintsuButtonVariant.SECONDARY
                            )
                        }

                        KintsuDivider()

                        // Biometric Toggle
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(spacing.m),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = spacing.m)) {
                                Text(
                                    text = "Biometric Authentication",
                                    style = typography.itemTitle,
                                    color = colors.textPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Unlock seamlessly using Fingerprint or Face ID",
                                    style = typography.caption,
                                    color = colors.textSecondary
                                )
                            }

                            Switch(
                                checked = settings.appLockBiometricEnabled,
                                onCheckedChange = { onUpdateAppLockBiometricEnabled(it) },
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

            // Secure Encrypted Compressed Auto-Backup Section
            item {
                Text(
                    text = "ENCRYPTED AUTO-BACKUP & CLOUD STORAGE",
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
                        Text(
                            text = "Kintsu Cryptographic Vault",
                            style = typography.itemTitle,
                            color = colors.goldSeam
                        )
                        Text(
                            text = "Backups are compressed with GZIP and encrypted with AES-256-GCM. Stored in sandboxed mobile storage strictly accessible to Kintsu, and automatically saved to your cloud account whenever logged in.",
                            style = typography.caption,
                            color = colors.textSecondary
                        )

                        val backupInfo = remember(settings.lastAutoBackupTime, isBackingUp) { SecureEncryptedBackupManager.getBackupInfo(context) }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = spacing.s)) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (backupInfo.exists) colors.accentSoft.copy(alpha = 0.5f) else colors.surfaceElevated)
                                        .border(
                                            width = 1.dp,
                                            color = if (backupInfo.exists) colors.accent.copy(alpha = 0.35f) else colors.border.copy(alpha = 0.3f),
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = if (backupInfo.exists) "Secured locally (${backupInfo.fileSizeBytes / 1024} KB encrypted)" else "No local encrypted vault yet",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                        color = if (backupInfo.exists) colors.accent else colors.textTertiary
                                    )
                                }
                                val lastSaveText = if (settings.lastAutoBackupTime > 0L) {
                                    val diffMin = (System.currentTimeMillis() - settings.lastAutoBackupTime) / 60000
                                    "Last Auto-Save: ${if (diffMin < 2) "Just now" else "$diffMin mins ago"}"
                                } else {
                                    "Compressed GZIP & AES-256 encrypted"
                                }
                                Text(
                                    text = lastSaveText,
                                    style = typography.secondary,
                                    color = colors.textSecondary
                                )
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(spacing.xs),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                KintsuButton(
                                    text = if (isBackingUp) "Saving..." else "Backup",
                                    onClick = {
                                        isBackingUp = true
                                        onTriggerEncryptedBackup { res ->
                                            isBackingUp = false
                                            if (res.isSuccess) {
                                                val info = res.getOrNull()
                                                Toast.makeText(context, "Encrypted backup created (${(info?.fileSizeBytes ?: 0) / 1024} KB AES-GCM)", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "Backup failed: ${res.exceptionOrNull()?.localizedMessage}", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    },
                                    variant = KintsuButtonVariant.ACCENT_SOFT
                                )

                                if (backupInfo.exists) {
                                    KintsuButton(
                                        text = "Restore",
                                        onClick = { showRestoreEncryptedConfirmDialog = true },
                                        variant = KintsuButtonVariant.SECONDARY
                                    )
                                }
                            }
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
                        // 1. Notifications
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = spacing.s)) {
                                Text(
                                    text = "Mindful Notifications",
                                    style = typography.itemTitle,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "Habit alerts, intentions & focus timer intervals",
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

                        KintsuDivider()

                        // 2. Exact Alarms
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = spacing.s)) {
                                Text(
                                    text = "Exact Alarms & Timers",
                                    style = typography.itemTitle,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "Accurate ritual timing without system delays",
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

                        KintsuDivider()

                        // 3. Background Execution / Battery Optimization
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = spacing.s)) {
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

                        KintsuDivider()

                        // 4. App Usage Access (Digital Balance)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = spacing.s)) {
                                Text(
                                    text = "App Usage Access",
                                    style = typography.itemTitle,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "Track screen time and enforce Addiction Shield limits",
                                    style = typography.secondary,
                                    color = colors.textSecondary
                                )
                            }
                            KintsuButton(
                                text = if (hasUsageAccessPermission) "Granted" else "Allow",
                                onClick = {
                                    val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    }
                                    try { context.startActivity(intent) } catch (_: Exception) {}
                                },
                                variant = if (hasUsageAccessPermission) KintsuButtonVariant.SECONDARY else KintsuButtonVariant.ACCENT_SOFT
                            )
                        }

                        KintsuDivider()

                        // 5. App Intervention Service (Accessibility)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = spacing.s)) {
                                Text(
                                    text = "App Intervention Service",
                                    style = typography.itemTitle,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "Recognise distracting apps to present mindful pauses",
                                    style = typography.secondary,
                                    color = colors.textSecondary
                                )
                            }
                            KintsuButton(
                                text = if (hasAccessibilityPermission) "Active" else "Enable",
                                onClick = {
                                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    }
                                    try { context.startActivity(intent) } catch (_: Exception) {}
                                },
                                variant = if (hasAccessibilityPermission) KintsuButtonVariant.SECONDARY else KintsuButtonVariant.ACCENT_SOFT
                            )
                        }

                        KintsuDivider()

                        // 6. Display Over Other Apps (Overlay)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = spacing.s)) {
                                Text(
                                    text = "Display Over Other Apps",
                                    style = typography.itemTitle,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "Present calm breathing pauses over blocked apps",
                                    style = typography.secondary,
                                    color = colors.textSecondary
                                )
                            }
                            KintsuButton(
                                text = if (hasOverlayPermission) "Granted" else "Allow",
                                onClick = {
                                    val intent = Intent(
                                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                        Uri.parse("package:${context.packageName}")
                                    ).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    }
                                    try { context.startActivity(intent) } catch (_: Exception) {}
                                },
                                variant = if (hasOverlayPermission) KintsuButtonVariant.SECONDARY else KintsuButtonVariant.ACCENT_SOFT
                            )
                        }
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

            // Achievements & Milestones Section
            item {
                Text(
                    text = "ACHIEVEMENTS & MILESTONES",
                    style = typography.caption,
                    color = colors.textTertiary,
                    modifier = Modifier.padding(start = spacing.xs, bottom = spacing.xs)
                )
                KintsuGroupedSection {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showAchievementsSheet = true }
                            .padding(spacing.m),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(colors.accent.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "",
                                fontSize = 18.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(spacing.m))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Achievements & Collectibles",
                                    style = typography.itemTitle,
                                    color = colors.textPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                val earnedCount = allAchievements.count { it.isEarned }
                                Box(
                                    modifier = Modifier
                                        .background(colors.goldSeamSoft, CircleShape)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "$earnedCount/${allAchievements.size}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.goldSeam
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "View milestone badges, consistency badges, and gold awards",
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


                        // Delete Cloud Backup (if signed in)
                        val isSignedIn = !settings.accountEmail.isNullOrBlank()
                        if (isSignedIn) {
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
                                        text = "Delete Cloud Backup",
                                        style = typography.itemTitle,
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = "Wipes your backed up records from the cloud. Local data on this device will remain intact.",
                                        style = typography.caption,
                                        color = colors.textSecondary
                                    )
                                }
                                Button(
                                    onClick = { showDeleteCloudDialog = true },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = colors.error.copy(alpha = 0.15f),
                                        contentColor = colors.error
                                    ),
                                    shape = KintsuTheme.shapes.medium
                                ) {
                                    Text("Delete Cloud Data")
                                }
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

    if (showRestoreEncryptedConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreEncryptedConfirmDialog = false },
            title = {
                Text(
                    text = "Restore Encrypted Backup",
                    style = typography.sectionHeader,
                    color = colors.textPrimary
                )
            },
            text = {
                Text(
                    text = "Decrypt and restore your entire journal, habits, and tasks from the local AES-256 encrypted vault? Current data will be safely updated.",
                    style = typography.body,
                    color = colors.textSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRestoreEncryptedConfirmDialog = false
                        onRestoreEncryptedBackup { res ->
                            if (res.isSuccess) {
                                Toast.makeText(context, "Successfully restored from encrypted backup!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Restore failed: ${res.exceptionOrNull()?.localizedMessage}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                ) {
                    Text("Restore Now", color = colors.accent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreEncryptedConfirmDialog = false }) {
                    Text("Cancel", color = colors.textPrimary)
                }
            },
            containerColor = colors.surface,
            shape = KintsuTheme.shapes.large
        )
    }

    if (showSetPasscodeDialog) {
        val lockType = settings.appLockType
        var patternGridSize by remember { mutableStateOf(3) }
        var patternStep by remember { mutableStateOf(1) }
        val firstPattern = remember { mutableStateListOf<Int>() }
        val currentDrawnPattern = remember { mutableStateListOf<Int>() }
        var patternConfirmed by remember { mutableStateOf(false) }
        var showPasswordPlaintext by remember { mutableStateOf(false) }
        var showConfirmPasswordPlaintext by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showSetPasscodeDialog = false },
            title = {
                Text(
                    text = when (lockType) {
                        "PIN" -> "Set PIN (Up to 24 Digits)"
                        "PATTERN" -> "Draw Unlock Pattern (${patternGridSize} × ${patternGridSize})"
                        else -> "Set Sanctuary Password"
                    },
                    style = typography.sectionHeader,
                    color = colors.textPrimary
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(spacing.m),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    when (lockType) {
                        "PIN" -> {
                            Text(
                                text = "Enter a numeric PIN between 4 and 24 digits to protect your sanctuary.",
                                style = typography.body,
                                color = colors.textSecondary,
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = tempPasscode,
                                onValueChange = { input ->
                                    if (input.all { it.isDigit() } && input.length <= 24) {
                                        tempPasscode = input
                                        passcodeDialogError = null
                                    }
                                },
                                label = { Text("New PIN (4–24 digits)") },
                                supportingText = {
                                    Text("${tempPasscode.length} / 24 digits", color = colors.textSecondary)
                                },
                                visualTransformation = if (showPasswordPlaintext) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    Text(
                                        text = if (showPasswordPlaintext) "Hide" else "Show",
                                        fontSize = 12.sp,
                                        color = colors.accent,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier
                                            .clickable { showPasswordPlaintext = !showPasswordPlaintext }
                                            .padding(8.dp)
                                    )
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = tempPasscodeConfirm,
                                onValueChange = { input ->
                                    if (input.all { it.isDigit() } && input.length <= 24) {
                                        tempPasscodeConfirm = input
                                        passcodeDialogError = null
                                    }
                                },
                                label = { Text("Confirm PIN") },
                                supportingText = {
                                    Text("${tempPasscodeConfirm.length} / 24 digits", color = colors.textSecondary)
                                },
                                visualTransformation = if (showConfirmPasswordPlaintext) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    Text(
                                        text = if (showConfirmPasswordPlaintext) "Hide" else "Show",
                                        fontSize = 12.sp,
                                        color = colors.accent,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier
                                            .clickable { showConfirmPasswordPlaintext = !showConfirmPasswordPlaintext }
                                            .padding(8.dp)
                                    )
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        "PATTERN" -> {
                            // Grid selector (3x3 or 5x5)
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(colors.surfaceElevated)
                                    .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                    .padding(2.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf(3 to "3 × 3 Grid", 5 to "5 × 5 Grid").forEach { (size, label) ->
                                    val isSelected = patternGridSize == size
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isSelected) colors.accentSoft else Color.Transparent)
                                            .clickable {
                                                if (patternGridSize != size) {
                                                    patternGridSize = size
                                                    patternStep = 1
                                                    firstPattern.clear()
                                                    currentDrawnPattern.clear()
                                                    patternConfirmed = false
                                                    passcodeDialogError = null
                                                }
                                            }
                                            .padding(horizontal = 14.dp, vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            style = typography.caption,
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                            color = if (isSelected) colors.accent else colors.textSecondary
                                        )
                                    }
                                }
                            }

                            val instructionText = when {
                                patternConfirmed -> "✓ Pattern verified and matched!"
                                patternStep == 1 -> "Step 1: Draw a pattern (min 4 dots)"
                                else -> "Step 2: Redraw exact pattern to confirm"
                            }
                            Text(
                                text = instructionText,
                                style = typography.body,
                                color = if (patternConfirmed) colors.accent else colors.textPrimary,
                                fontWeight = FontWeight.Medium
                            )

                            // Touch visual pattern canvas
                            VisualPatternCanvas(
                                gridSize = patternGridSize,
                                selectedNodes = currentDrawnPattern,
                                onNodeSelected = { node ->
                                    currentDrawnPattern.add(node)
                                },
                                onDragStarted = {
                                    currentDrawnPattern.clear()
                                    passcodeDialogError = null
                                },
                                onDragEnded = {
                                    if (currentDrawnPattern.size < 4) {
                                        passcodeDialogError = "Connect at least 4 dots"
                                        currentDrawnPattern.clear()
                                    } else {
                                        if (patternStep == 1) {
                                            firstPattern.clear()
                                            firstPattern.addAll(currentDrawnPattern)
                                            currentDrawnPattern.clear()
                                            patternStep = 2
                                            passcodeDialogError = null
                                        } else if (patternStep == 2) {
                                            if (currentDrawnPattern.toList() == firstPattern.toList()) {
                                                patternConfirmed = true
                                                passcodeDialogError = null
                                            } else {
                                                passcodeDialogError = "Pattern did not match. Please redraw or start over."
                                                currentDrawnPattern.clear()
                                            }
                                        }
                                    }
                                },
                                canvasSize = 250.dp
                            )

                            TextButton(
                                onClick = {
                                    patternStep = 1
                                    firstPattern.clear()
                                    currentDrawnPattern.clear()
                                    patternConfirmed = false
                                    passcodeDialogError = null
                                }
                            ) {
                                Text("Clear & Draw Again", color = colors.accent, style = typography.caption)
                            }
                        }

                        else -> { // PASSWORD
                            Text(
                                text = "Enter a case-sensitive password. Letters (a-z, A-Z), numbers (0-9), and special characters (!@#$%^&*...) are supported.",
                                style = typography.body,
                                color = colors.textSecondary,
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = tempPasscode,
                                onValueChange = {
                                    tempPasscode = it
                                    passcodeDialogError = null
                                },
                                label = { Text("New Password (case-sensitive)") },
                                visualTransformation = if (showPasswordPlaintext) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    Text(
                                        text = if (showPasswordPlaintext) "Hide" else "Show",
                                        fontSize = 12.sp,
                                        color = colors.accent,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier
                                            .clickable { showPasswordPlaintext = !showPasswordPlaintext }
                                            .padding(8.dp)
                                    )
                                },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    autoCorrect = false
                                ),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = tempPasscodeConfirm,
                                onValueChange = {
                                    tempPasscodeConfirm = it
                                    passcodeDialogError = null
                                },
                                label = { Text("Confirm Password") },
                                visualTransformation = if (showConfirmPasswordPlaintext) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    Text(
                                        text = if (showConfirmPasswordPlaintext) "Hide" else "Show",
                                        fontSize = 12.sp,
                                        color = colors.accent,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier
                                            .clickable { showConfirmPasswordPlaintext = !showConfirmPasswordPlaintext }
                                            .padding(8.dp)
                                    )
                                },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    autoCorrect = false
                                ),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    if (passcodeDialogError != null) {
                        Text(
                            text = passcodeDialogError ?: "",
                            style = typography.caption,
                            color = colors.error,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            },
            confirmButton = {
                KintsuButton(
                    text = "Save",
                    onClick = {
                        when (lockType) {
                            "PATTERN" -> {
                                if (!patternConfirmed || firstPattern.size < 4) {
                                    passcodeDialogError = if (patternStep == 1) "Please draw a pattern" else "Please redraw and confirm pattern"
                                    return@KintsuButton
                                }
                                val patternStr = AppLockManager.patternToString(firstPattern.toList(), patternGridSize)
                                val hash = AppLockManager.hashSecret(patternStr)
                                onUpdateAppLockCode(hash)
                                onUpdateAppLockEnabled(true)
                                showSetPasscodeDialog = false
                                Toast.makeText(context, "Pattern (${patternGridSize}x${patternGridSize}) saved successfully!", Toast.LENGTH_SHORT).show()
                            }
                            "PIN" -> {
                                if (tempPasscode.length < 4) {
                                    passcodeDialogError = "PIN must be between 4 and 24 digits"
                                    return@KintsuButton
                                }
                                if (tempPasscode.length > 24) {
                                    passcodeDialogError = "PIN cannot exceed 24 digits"
                                    return@KintsuButton
                                }
                                if (tempPasscode != tempPasscodeConfirm) {
                                    passcodeDialogError = "PINs do not match"
                                    return@KintsuButton
                                }
                                val hash = AppLockManager.hashSecret(tempPasscode)
                                onUpdateAppLockCode(hash)
                                onUpdateAppLockEnabled(true)
                                showSetPasscodeDialog = false
                                Toast.makeText(context, "PIN code saved successfully!", Toast.LENGTH_SHORT).show()
                            }
                            else -> { // PASSWORD
                                if (tempPasscode.isEmpty()) {
                                    passcodeDialogError = "Password cannot be blank"
                                    return@KintsuButton
                                }
                                if (tempPasscode.length < 4) {
                                    passcodeDialogError = "Password must be at least 4 characters"
                                    return@KintsuButton
                                }
                                if (tempPasscode != tempPasscodeConfirm) {
                                    passcodeDialogError = "Passwords do not match (passwords are case-sensitive)"
                                    return@KintsuButton
                                }
                                val hash = AppLockManager.hashSecret(tempPasscode)
                                onUpdateAppLockCode(hash)
                                onUpdateAppLockEnabled(true)
                                showSetPasscodeDialog = false
                                Toast.makeText(context, "Password saved successfully!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    variant = KintsuButtonVariant.PRIMARY
                )
            },
            dismissButton = {
                TextButton(onClick = { showSetPasscodeDialog = false }) {
                    Text("Cancel", color = colors.textPrimary)
                }
            },
            containerColor = colors.surface,
            shape = KintsuTheme.shapes.large
        )
    }


    // Avatar Selector Dialog: 24 Signature Avatars + Custom Device Photo
    val customPhotoLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let {
            try {
                val avatarFile = java.io.File(context.filesDir, "profile_avatar.jpg")
                context.contentResolver.openInputStream(it)?.use { input ->
                    avatarFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                onUpdateProfileAvatar("custom:${avatarFile.absolutePath}")
                showAvatarPicker = false
                android.widget.Toast.makeText(context, "Custom profile picture applied", android.widget.Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                android.widget.Toast.makeText(context, "Failed to load photo", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    if (showAvatarPicker) {
        AlertDialog(
            onDismissRequest = { showAvatarPicker = false },
            title = {
                Text(
                    text = "Profile Picture",
                    style = typography.itemTitle,
                    color = colors.textPrimary
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(spacing.m)
                ) {
                    Text(
                        text = "Choose your own personal photo or select from 24 signature emblems.",
                        style = typography.caption,
                        color = colors.textSecondary
                    )

                    // Option 1: On their own (Custom Photo from Gallery)
                    val isCustom = settings.profileAvatarId.startsWith("custom:")
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isCustom) colors.accentSoft else colors.surfaceElevated)
                            .border(
                                width = 1.dp,
                                color = if (isCustom) colors.accent else colors.border.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { customPhotoLauncher.launch("image/*") }
                            .padding(horizontal = spacing.m, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(spacing.s)
                            ) {
                                if (isCustom) {
                                    ProfileAvatarVisual(avatarId = settings.profileAvatarId, size = 36.dp)
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(colors.accent.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        IconUser(color = colors.accent, size = 18.dp)
                                    }
                                }
                                Column {
                                    Text(
                                        text = "Upload Your Own Photo",
                                        style = typography.itemTitle.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = if (isCustom) "Custom photo currently active" else "Choose from device gallery",
                                        style = typography.caption.copy(fontSize = 11.sp),
                                        color = colors.textSecondary
                                    )
                                }
                            }

                            Text(
                                text = if (isCustom) "Active" else "Browse",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.accent
                            )
                        }
                    }

                    Text(
                        text = "OR 24 SIGNATURE AVATARS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = colors.textTertiary
                    )

                    // Option 2: 24 Signature Avatars
                    androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                        columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(3),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                    ) {
                        items(SIGNATURE_AVATARS_24.size) { index ->
                            val item = SIGNATURE_AVATARS_24[index]
                            val isSelected = settings.profileAvatarId == item.id
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) colors.goldSeam.copy(alpha = 0.2f) else colors.surfaceElevated)
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) colors.goldSeam else colors.border.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable {
                                        onUpdateProfileAvatar(item.id)
                                        showAvatarPicker = false
                                    }
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    ProfileAvatarVisual(
                                        avatarId = item.id,
                                        size = 34.dp,
                                        iconSize = 18.dp
                                    )
                                    Text(
                                        text = item.title,
                                        style = typography.caption.copy(fontSize = 10.sp),
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (isSelected) colors.goldSeam else colors.textSecondary,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAvatarPicker = false }) {
                    Text("Close", color = colors.textSecondary)
                }
            },
            containerColor = colors.background,
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

    if (showWidgetShowcaseSheet) {
        WidgetShowcaseSheet(
            onDismiss = { showWidgetShowcaseSheet = false }
        )
    }

    if (showAchievementsSheet) {
        com.kintsu.app.ui.shop.AchievementsGallerySheet(
            achievements = allAchievements,
            onDismiss = { showAchievementsSheet = false }
        )
    }
}
}
