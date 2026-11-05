package com.kintsu.app.wellbeing.ui

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kintsu.app.data.model.AppGroupEntity
import com.kintsu.app.data.model.BlockRuleEntity
import com.kintsu.app.ui.components.AppLogo
import com.kintsu.app.ui.components.IconCheck
import com.kintsu.app.ui.components.IconSearch
import com.kintsu.app.ui.components.KintsuButton
import com.kintsu.app.ui.components.KintsuButtonVariant
import com.kintsu.app.ui.components.KintsuCard
import com.kintsu.app.ui.theme.KintsuTheme
import com.kintsu.app.wellbeing.domain.ProtectedApps
import com.kintsu.app.wellbeing.domain.ScreenTimeHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class EligibleAppInfo(
    val packageName: String,
    val appName: String,
    val iconBitmap: Bitmap?,
    val usageMinutesToday: Long,
    val category: String = "Other",
    val activeRuleMode: String = "ALLOW",
    val isSelected: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBlockingSheet(
    appGroups: List<AppGroupEntity>,
    blockRules: List<BlockRuleEntity>,
    onSaveAppGroup: (name: String, category: String, packages: String) -> Unit,
    onSaveRule: (groupId: Long, mode: String, dailyLimitMinutes: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var appsList by remember { mutableStateOf<List<EligibleAppInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedSort by remember { mutableStateOf("USAGE") } // USAGE, ALPHA, BLOCKED
    var selectedCategoryFilter by remember { mutableStateOf("ALL") }

    val selectedPackages = remember { mutableStateMapOf<String, Boolean>() }
    var showGroupDialog by remember { mutableStateOf(false) }
    var groupNameInput by remember { mutableStateOf("") }
    var selectedRuleMode by remember { mutableStateOf("MINDFUL_PAUSE") } // ALLOW, MINDFUL_PAUSE, DAILY_LIMIT, SCHEDULE, FOCUS, COMBINED

    // Load installed launchable apps and daily usage asynchronously
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val pm = context.packageManager
            val intent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolveInfos = pm.queryIntentActivities(intent, 0)
            val usageMap = ScreenTimeHelper.getPerAppUsageTodayMinutes(context)

            val blockedPackages = appGroups.flatMap { it.packageNames.split(",") }.map { it.trim() }.toSet()

            val items = resolveInfos.mapNotNull { ri ->
                val pkg = ri.activityInfo.packageName
                if (ProtectedApps.isProtected(pkg)) return@mapNotNull null
                val label = ri.loadLabel(pm).toString()
                val iconDrawable = try { ri.loadIcon(pm) } catch (_: Exception) { null }
                val bmp = drawableToBitmap(iconDrawable)
                val usage = usageMap[pkg] ?: 0L

                val isBlocked = blockedPackages.contains(pkg)
                EligibleAppInfo(
                    packageName = pkg,
                    appName = label,
                    iconBitmap = bmp,
                    usageMinutesToday = usage,
                    activeRuleMode = if (isBlocked) "MINDFUL_PAUSE" else "ALLOW",
                    isSelected = isBlocked
                )
            }.distinctBy { it.packageName }

            withContext(Dispatchers.Main) {
                appsList = items
                items.forEach {
                    if (it.isSelected) selectedPackages[it.packageName] = true
                }
                isLoading = false
            }
        }
    }

    val filteredApps = remember(appsList, searchQuery, selectedSort, selectedCategoryFilter, selectedPackages.toMap()) {
        appsList.filter { app ->
            val matchesQuery = searchQuery.isBlank() ||
                    app.appName.contains(searchQuery, ignoreCase = true) ||
                    app.packageName.contains(searchQuery, ignoreCase = true)
            matchesQuery
        }.sortedWith { a, b ->
            when (selectedSort) {
                "USAGE" -> b.usageMinutesToday.compareTo(a.usageMinutesToday)
                "ALPHA" -> a.appName.compareTo(b.appName, ignoreCase = true)
                "BLOCKED" -> {
                    val aSel = selectedPackages[a.packageName] == true
                    val bSel = selectedPackages[b.packageName] == true
                    bSel.compareTo(aSel)
                }
                else -> b.usageMinutesToday.compareTo(a.usageMinutesToday)
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.background,
        dragHandle = null,
        modifier = Modifier.fillMaxHeight(0.92f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .padding(spacing.m)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "App Distraction Shield",
                        style = typography.itemTitle.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Select apps to pause, restrict or block",
                        style = typography.caption,
                        color = colors.textSecondary
                    )
                }

                KintsuButton(
                    text = "Save Group",
                    onClick = { showGroupDialog = true },
                    variant = KintsuButtonVariant.PRIMARY,
                    enabled = selectedPackages.values.any { it }
                )
            }

            Spacer(modifier = Modifier.height(spacing.m))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search installed applications...", style = typography.body, color = colors.textTertiary) },
                singleLine = true,
                textStyle = typography.body.copy(color = colors.textPrimary),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.goldSeam,
                    unfocusedBorderColor = colors.border,
                    focusedContainerColor = colors.surfaceElevated,
                    unfocusedContainerColor = colors.surfaceElevated
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(spacing.s))

            // Sort & Filter Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.xs)
            ) {
                FilterSortPill(
                    label = "Most Used",
                    isActive = selectedSort == "USAGE",
                    onClick = { selectedSort = "USAGE" }
                )
                FilterSortPill(
                    label = "A to Z",
                    isActive = selectedSort == "ALPHA",
                    onClick = { selectedSort = "ALPHA" }
                )
                FilterSortPill(
                    label = "Selected",
                    isActive = selectedSort == "BLOCKED",
                    onClick = { selectedSort = "BLOCKED" }
                )
            }

            Spacer(modifier = Modifier.height(spacing.m))

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Scanning eligible apps...", style = typography.body, color = colors.textSecondary)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(spacing.s)
                ) {
                    items(filteredApps, key = { it.packageName }) { app ->
                        val isChecked = selectedPackages[app.packageName] == true
                        AppRowItem(
                            app = app,
                            isChecked = isChecked,
                            onToggle = { checked ->
                                if (checked) {
                                    selectedPackages[app.packageName] = true
                                } else {
                                    selectedPackages.remove(app.packageName)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showGroupDialog) {
        com.kintsu.app.ui.components.KintsuDialog(
            title = "Create Shield Group",
            onDismissRequest = { showGroupDialog = false },
            confirmButton = {
                KintsuButton(
                    text = "Apply Rule",
                    onClick = {
                        val pkgs = selectedPackages.filter { it.value }.keys.joinToString(",")
                        val groupName = groupNameInput.ifBlank { "Distraction Shield" }
                        onSaveAppGroup(groupName, "CUSTOM", pkgs)
                        showGroupDialog = false
                        onDismiss()
                    },
                    variant = KintsuButtonVariant.PRIMARY
                )
            },
            dismissButton = {
                KintsuButton(
                    text = "Cancel",
                    onClick = { showGroupDialog = false },
                    variant = KintsuButtonVariant.SECONDARY
                )
            }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.m)) {
                Text(
                    text = "Selected apps (${selectedPackages.count { it.value }}):",
                    style = typography.caption,
                    color = colors.textSecondary
                )
                OutlinedTextField(
                    value = groupNameInput,
                    onValueChange = { groupNameInput = it },
                    label = { Text("Group Name (e.g. Social, Games, Study)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Default Rule Mode:",
                    style = typography.caption.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.textPrimary
                )

                val modes = listOf(
                    "MINDFUL_PAUSE" to "Mindful Breathing Pause",
                    "DAILY_LIMIT" to "Daily Screen-Time Limit",
                    "FOCUS" to "Restricted During Focus Lock",
                    "SCHEDULE" to "Scheduled Window Blocking"
                )

                modes.forEach { (modeKey, modeTitle) ->
                    val isModeSel = selectedRuleMode == modeKey
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isModeSel) colors.goldSeamSoft else colors.surface)
                            .border(1.dp, if (isModeSel) colors.goldSeam else colors.border, RoundedCornerShape(8.dp))
                            .clickable { selectedRuleMode = modeKey }
                            .padding(horizontal = spacing.m, vertical = spacing.s)
                    ) {
                        Text(
                            text = modeTitle,
                            style = typography.body,
                            color = if (isModeSel) colors.goldSeam else colors.textPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AppRowItem(
    app: EligibleAppInfo,
    isChecked: Boolean,
    onToggle: (Boolean) -> Unit
) {
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography

    KintsuCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle(!isChecked) },
        backgroundColor = if (isChecked) colors.surfaceElevated else colors.surface,
        borderColor = if (isChecked) colors.goldSeam.copy(alpha = 0.5f) else colors.border
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.m),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (app.iconBitmap != null) {
                Image(
                    bitmap = app.iconBitmap.asImageBitmap(),
                    contentDescription = app.appName,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                )
            } else {
                AppLogo(
                    packageName = app.packageName,
                    size = 42.dp,
                    shape = RoundedCornerShape(10.dp),
                    fallbackAppName = app.appName
                )
            }

            Spacer(modifier = Modifier.width(spacing.m))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.appName,
                    style = typography.itemTitle.copy(fontWeight = FontWeight.Medium),
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (app.usageMinutesToday > 0) "${app.usageMinutesToday}m used today" else "No usage today",
                    style = typography.caption,
                    color = if (app.usageMinutesToday > 30) colors.goldSeam else colors.textTertiary
                )
            }

            Checkbox(
                checked = isChecked,
                onCheckedChange = { onToggle(it) },
                colors = CheckboxDefaults.colors(
                    checkedColor = colors.goldSeam,
                    uncheckedColor = colors.border,
                    checkmarkColor = colors.background
                )
            )
        }
    }
}

@Composable
private fun FilterSortPill(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val colors = KintsuTheme.colors
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isActive) colors.goldSeamSoft else colors.surfaceElevated)
            .border(1.dp, if (isActive) colors.goldSeam else colors.border, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isActive) colors.goldSeam else colors.textSecondary
        )
    }
}

private fun drawableToBitmap(drawable: Drawable?): Bitmap? {
    if (drawable == null) return null
    if (drawable is BitmapDrawable && drawable.bitmap != null) {
        return drawable.bitmap
    }
    return try {
        val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 96
        val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 96
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        bitmap
    } catch (_: Exception) {
        null
    }
}
