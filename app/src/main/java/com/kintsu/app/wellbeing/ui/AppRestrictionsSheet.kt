package com.kintsu.app.wellbeing.ui

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kintsu.app.data.KintsuDatabase
import com.kintsu.app.data.model.AppGroupEntity
import com.kintsu.app.data.model.BlockRuleEntity
import com.kintsu.app.ui.components.AppLogo
import com.kintsu.app.ui.components.AppLogoCache
import com.kintsu.app.ui.components.KintsuBottomSheet
import com.kintsu.app.ui.components.KintsuButton
import com.kintsu.app.ui.components.KintsuButtonVariant
import com.kintsu.app.ui.theme.KintsuTheme
import com.kintsu.app.wellbeing.domain.ProtectedApps
import com.kintsu.app.wellbeing.domain.ScreenTimeHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class InstalledAppItem(
    val packageName: String,
    val appName: String,
    val usageMinutesToday: Long,
    val currentLimitMinutes: Int = 0,
    val isRestricted: Boolean = false
)

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun AppRestrictionsSheet(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var appsList by remember { mutableStateOf<List<InstalledAppItem>>(emptyList()) }
    var selectedAppForEdit by remember { mutableStateOf<InstalledAppItem?>(null) }
    var selectedLimitMinutes by remember { mutableIntStateOf(60) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val pm = context.packageManager
            val installed = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            AppLogoCache.preload(context, installed.map { it.packageName })
            val db = KintsuDatabase.getDatabase(context)
            val allGroups = db.wellbeingDao().getAllAppGroups().first()
            val allRules = db.wellbeingDao().getAllBlockRules().first()

            val items = installed.mapNotNull { appInfo ->
                val pkg = appInfo.packageName
                if (ProtectedApps.isProtected(pkg)) return@mapNotNull null
                val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0 &&
                        (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0
                if (isSystem && !pkg.contains("chrome") && !pkg.contains("browser") && !pkg.contains("youtube")) return@mapNotNull null

                val label = try { pm.getApplicationLabel(appInfo).toString() } catch (_: Exception) { pkg }
                val usageMin = ScreenTimeHelper.getPackageUsageTodayMinutes(context, pkg)

                // Check if has rule
                val group = allGroups.find { it.packageNames.split(",").map { p -> p.trim() }.contains(pkg) }
                val rule = if (group != null) allRules.find { it.appGroupId == group.id && it.isEnabled } else null

                InstalledAppItem(
                    packageName = pkg,
                    appName = label,
                    usageMinutesToday = usageMin,
                    currentLimitMinutes = rule?.dailyLimitMinutes ?: 0,
                    isRestricted = rule != null && rule.isEnabled
                )
            }.sortedWith(compareByDescending<InstalledAppItem> { it.isRestricted }
                .thenByDescending { it.usageMinutesToday }
                .thenBy { it.appName.lowercase() })

            withContext(Dispatchers.Main) {
                appsList = items
            }
        }
    }

    val filteredApps = remember(appsList, searchQuery) {
        if (searchQuery.isBlank()) appsList
        else appsList.filter { it.appName.contains(searchQuery, ignoreCase = true) || it.packageName.contains(searchQuery, ignoreCase = true) }
    }

    KintsuBottomSheet(
        onDismissRequest = onDismiss,
        title = "App Restrictions & Timers"
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.m),
            verticalArrangement = Arrangement.spacedBy(spacing.m)
        ) {
            Text(
                text = "Set strict daily timers and session limits per app. Once exceeded, Kintsu Shield locks access.",
                style = typography.caption,
                color = colors.textSecondary
            )

            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search installed apps...", color = colors.textTertiary, fontSize = 13.sp) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.goldSeam,
                    unfocusedBorderColor = colors.border,
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary,
                    cursorColor = colors.goldSeam
                )
            )

            // Edit App Limit Card if selected
            if (selectedAppForEdit != null) {
                val target = selectedAppForEdit!!
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.surfaceElevated)
                        .border(1.2.dp, colors.goldSeam, RoundedCornerShape(16.dp))
                        .padding(spacing.m)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(spacing.s)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                AppLogo(
                                    packageName = target.packageName,
                                    size = 42.dp,
                                    shape = RoundedCornerShape(10.dp),
                                    fallbackAppName = target.appName
                                )
                                Column {
                                    Text(
                                        text = target.appName,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = "Used today: ${target.usageMinutesToday}m",
                                        fontSize = 11.sp,
                                        color = colors.textSecondary
                                    )
                                }
                            }

                            Text(
                                text = "Close ✕",
                                fontSize = 12.sp,
                                color = colors.textTertiary,
                                modifier = Modifier.clickable { selectedAppForEdit = null }
                            )
                        }

                        Text(
                            text = "Daily Usage Quota Timer:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.goldSeam
                        )

                        // Timer Presets
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(15, 30, 45, 60, 90).forEach { limit ->
                                val isSel = selectedLimitMinutes == limit
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSel) colors.goldSeamSoft else colors.surface)
                                        .border(1.dp, if (isSel) colors.goldSeam else colors.border, RoundedCornerShape(8.dp))
                                        .clickable { selectedLimitMinutes = limit }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${limit}m",
                                        fontSize = 12.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSel) colors.goldSeam else colors.textPrimary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(spacing.m)
                        ) {
                            KintsuButton(
                                text = "Remove Limit",
                                onClick = {
                                    scope.launch(Dispatchers.IO) {
                                        val db = KintsuDatabase.getDatabase(context)
                                        val allGroups = db.wellbeingDao().getAllAppGroups().first()
                                        val grp = allGroups.find { it.packageNames.contains(target.packageName) }
                                        if (grp != null) {
                                            db.wellbeingDao().deleteAppGroup(grp)
                                        }
                                        withContext(Dispatchers.Main) {
                                            selectedAppForEdit = null
                                            // Refresh
                                            appsList = appsList.map {
                                                if (it.packageName == target.packageName) it.copy(isRestricted = false, currentLimitMinutes = 0) else it
                                            }
                                        }
                                    }
                                },
                                variant = KintsuButtonVariant.SECONDARY,
                                modifier = Modifier.weight(1f)
                            )

                            KintsuButton(
                                text = "Save ${selectedLimitMinutes}m Timer",
                                onClick = {
                                    scope.launch(Dispatchers.IO) {
                                        val db = KintsuDatabase.getDatabase(context)
                                        val allGroups = db.wellbeingDao().getAllAppGroups().first()
                                        var grp = allGroups.find { it.packageNames.contains(target.packageName) }
                                        val grpId = if (grp == null) {
                                            db.wellbeingDao().insertAppGroup(
                                                AppGroupEntity(
                                                    name = target.appName,
                                                    categoryType = "CUSTOM",
                                                    packageNames = target.packageName
                                                )
                                            )
                                        } else grp.id

                                        val existingRules = db.wellbeingDao().getRulesForGroup(grpId)
                                        if (existingRules.isNotEmpty()) {
                                            db.wellbeingDao().updateBlockRule(
                                                existingRules[0].copy(
                                                    dailyLimitMinutes = selectedLimitMinutes,
                                                    isEnabled = true,
                                                    strictnessLevel = "STRICT"
                                                )
                                            )
                                        } else {
                                            db.wellbeingDao().insertBlockRule(
                                                BlockRuleEntity(
                                                    appGroupId = grpId,
                                                    dailyLimitMinutes = selectedLimitMinutes,
                                                    mode = "DAILY_LIMIT",
                                                    strictnessLevel = "STRICT",
                                                    isEnabled = true
                                                )
                                            )
                                        }

                                        withContext(Dispatchers.Main) {
                                            selectedAppForEdit = null
                                            appsList = appsList.map {
                                                if (it.packageName == target.packageName) it.copy(isRestricted = true, currentLimitMinutes = selectedLimitMinutes) else it
                                            }
                                        }
                                    }
                                },
                                variant = KintsuButtonVariant.PRIMARY,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Apps List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredApps, key = { it.packageName }) { app ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.surfaceElevated)
                            .border(1.dp, if (app.isRestricted) colors.goldSeam.copy(alpha = 0.6f) else colors.border.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .clickable {
                                selectedAppForEdit = app
                                selectedLimitMinutes = if (app.currentLimitMinutes > 0) app.currentLimitMinutes else 30
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            AppLogo(
                                packageName = app.packageName,
                                size = 38.dp,
                                shape = RoundedCornerShape(10.dp),
                                fallbackAppName = app.appName
                            )

                            Column {
                                Text(
                                    text = app.appName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "Used today: ${app.usageMinutesToday}m" + if (app.isRestricted) " • Limit: ${app.currentLimitMinutes}m" else "",
                                    fontSize = 11.sp,
                                    color = if (app.isRestricted) colors.goldSeam else colors.textSecondary
                                )
                            }
                        }

                        Text(
                            text = if (app.isRestricted) "Restricted ✓" else "Set Timer +",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (app.isRestricted) colors.goldSeam else colors.accent
                        )
                    }
                }
            }
        }
    }
}
