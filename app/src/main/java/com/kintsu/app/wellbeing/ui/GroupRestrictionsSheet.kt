package com.kintsu.app.wellbeing.ui

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
import com.kintsu.app.ui.components.KintsuBottomSheet
import com.kintsu.app.ui.components.KintsuButton
import com.kintsu.app.ui.components.KintsuButtonVariant
import com.kintsu.app.ui.theme.KintsuTheme
import com.kintsu.app.wellbeing.domain.ScreenTimeHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class GroupDisplayItem(
    val group: AppGroupEntity,
    val rule: BlockRuleEntity?,
    val aggregateUsageMinutesToday: Long,
    val appCount: Int
)

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun GroupRestrictionsSheet(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography
    val scope = rememberCoroutineScope()

    var groupsList by remember { mutableStateOf<List<GroupDisplayItem>>(emptyList()) }
    var selectedGroupForEdit by remember { mutableStateOf<GroupDisplayItem?>(null) }
    var selectedGroupLimitMinutes by remember { mutableIntStateOf(60) }

    suspend fun loadGroups(db: KintsuDatabase) {
        val groups = db.wellbeingDao().getAllAppGroups().first()
        val rules = db.wellbeingDao().getAllBlockRules().first()

        // Ensure default groups exist if empty
        if (groups.isEmpty()) {
            val defaults = listOf(
                AppGroupEntity(name = "Social Media", categoryType = "SOCIAL", packageNames = "com.instagram.android,com.zhiliaoapp.musically,com.twitter.android,com.snapchat.android,com.facebook.katana,com.reddit.frontpage"),
                AppGroupEntity(name = "Video & Entertainment", categoryType = "STREAMING", packageNames = "com.google.android.youtube,com.netflix.mediaclient,com.amazon.avod.thirdpartyclient,tv.twitch.android.app"),
                AppGroupEntity(name = "Gaming & Leisure", categoryType = "GAMES", packageNames = "com.king.candycrushsaga,com.roblox.client,com.supercell.clashroyale")
            )
            for (def in defaults) {
                val gId = db.wellbeingDao().insertAppGroup(def)
                db.wellbeingDao().insertBlockRule(
                    BlockRuleEntity(
                        appGroupId = gId,
                        dailyLimitMinutes = 60,
                        mode = "DAILY_LIMIT",
                        strictnessLevel = "STRICT",
                        isEnabled = false
                    )
                )
            }
        }

        val reloadedGroups = db.wellbeingDao().getAllAppGroups().first()
        val reloadedRules = db.wellbeingDao().getAllBlockRules().first()

        val items = reloadedGroups.map { grp ->
            val rule = reloadedRules.find { it.appGroupId == grp.id }
            val pkgs = grp.packageNames.split(",").map { it.trim() }.filter { it.isNotBlank() }
            val totalUsage = pkgs.sumOf { ScreenTimeHelper.getPackageUsageTodayMinutes(context, it) }

            GroupDisplayItem(
                group = grp,
                rule = rule,
                aggregateUsageMinutesToday = totalUsage,
                appCount = pkgs.size
            )
        }

        withContext(Dispatchers.Main) {
            groupsList = items
        }
    }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val db = KintsuDatabase.getDatabase(context)
            loadGroups(db)
        }
    }

    KintsuBottomSheet(
        onDismissRequest = onDismiss,
        title = "Category & Group Restrictions"
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.m),
            verticalArrangement = Arrangement.spacedBy(spacing.m)
        ) {
            Text(
                text = "Track and restrict overall category screen time. When a group quota is reached, all apps in that category lock simultaneously.",
                style = typography.caption,
                color = colors.textSecondary
            )

            // Edit Group Card
            if (selectedGroupForEdit != null) {
                val item = selectedGroupForEdit!!
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
                            Column {
                                Text(
                                    text = item.group.name,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "${item.appCount} apps • Combined usage today: ${item.aggregateUsageMinutesToday}m",
                                    fontSize = 11.sp,
                                    color = colors.textSecondary
                                )
                            }

                            Text(
                                text = "Close ✕",
                                fontSize = 12.sp,
                                color = colors.textTertiary,
                                modifier = Modifier.clickable { selectedGroupForEdit = null }
                            )
                        }

                        Text(
                            text = "Group Aggregate Daily Timer:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.goldSeam
                        )

                        // Timer Options
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(30, 45, 60, 90, 120).forEach { limit ->
                                val isSel = selectedGroupLimitMinutes == limit
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSel) colors.goldSeamSoft else colors.surface)
                                        .border(1.dp, if (isSel) colors.goldSeam else colors.border, RoundedCornerShape(8.dp))
                                        .clickable { selectedGroupLimitMinutes = limit }
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
                                text = "Disable Group Limit",
                                onClick = {
                                    scope.launch(Dispatchers.IO) {
                                        val db = KintsuDatabase.getDatabase(context)
                                        if (item.rule != null) {
                                            db.wellbeingDao().updateBlockRule(item.rule.copy(isEnabled = false))
                                        }
                                        loadGroups(db)
                                        withContext(Dispatchers.Main) {
                                            selectedGroupForEdit = null
                                        }
                                    }
                                },
                                variant = KintsuButtonVariant.SECONDARY,
                                modifier = Modifier.weight(1f)
                            )

                            KintsuButton(
                                text = "Save ${selectedGroupLimitMinutes}m Limit",
                                onClick = {
                                    scope.launch(Dispatchers.IO) {
                                        val db = KintsuDatabase.getDatabase(context)
                                        if (item.rule != null) {
                                            db.wellbeingDao().updateBlockRule(
                                                item.rule.copy(
                                                    dailyLimitMinutes = selectedGroupLimitMinutes,
                                                    isEnabled = true,
                                                    strictnessLevel = "STRICT"
                                                )
                                            )
                                        } else {
                                            db.wellbeingDao().insertBlockRule(
                                                BlockRuleEntity(
                                                    appGroupId = item.group.id,
                                                    dailyLimitMinutes = selectedGroupLimitMinutes,
                                                    mode = "DAILY_LIMIT",
                                                    strictnessLevel = "STRICT",
                                                    isEnabled = true
                                                )
                                            )
                                        }
                                        loadGroups(db)
                                        withContext(Dispatchers.Main) {
                                            selectedGroupForEdit = null
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

            // Groups List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(groupsList, key = { it.group.id }) { item ->
                    val isRuleActive = item.rule?.isEnabled == true
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.surfaceElevated)
                            .border(1.dp, if (isRuleActive) colors.goldSeam.copy(alpha = 0.6f) else colors.border.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .clickable {
                                selectedGroupForEdit = item
                                selectedGroupLimitMinutes = item.rule?.dailyLimitMinutes ?: 60
                            }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.group.name,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "Combined usage today: ${item.aggregateUsageMinutesToday}m" + if (isRuleActive) " • Limit: ${item.rule?.dailyLimitMinutes}m" else " • No Limit",
                                fontSize = 11.sp,
                                color = if (isRuleActive) colors.goldSeam else colors.textSecondary
                            )
                        }

                        Text(
                            text = if (isRuleActive) "Guarded ✓" else "Configure +",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isRuleActive) colors.goldSeam else colors.accent
                        )
                    }
                }
            }
        }
    }
}
