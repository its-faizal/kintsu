package com.kintsu.app.wellbeing.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kintsu.app.data.model.BlockedWebsiteEntity
import com.kintsu.app.ui.components.IconCheck
import com.kintsu.app.ui.components.IconClose
import com.kintsu.app.ui.components.IconGlobe
import com.kintsu.app.ui.components.KintsuButton
import com.kintsu.app.ui.components.KintsuButtonVariant
import com.kintsu.app.ui.components.KintsuCard
import com.kintsu.app.ui.components.KintsuDialog
import com.kintsu.app.ui.theme.KintsuTheme
import com.kintsu.app.wellbeing.domain.WebsiteFilterManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebsiteBlockingSheet(
    blockedWebsites: List<BlockedWebsiteEntity>,
    onAddWebsite: (domain: String, group: String, mode: String, matchSubdomains: Boolean) -> Unit,
    onToggleWebsite: (BlockedWebsiteEntity) -> Unit,
    onDeleteWebsite: (BlockedWebsiteEntity) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var newDomainInput by remember { mutableStateOf("") }
    var newGroupInput by remember { mutableStateOf("Social") }
    var newModeInput by remember { mutableStateOf("ALWAYS_BLOCKED") } // ALWAYS_BLOCKED, SCHEDULED, FOCUS_ONLY, LIMITED_ACCESS
    var matchSubdomainsInput by remember { mutableStateOf(true) }

    val filteredWebsites = remember(blockedWebsites, searchQuery) {
        if (searchQuery.isBlank()) blockedWebsites
        else blockedWebsites.filter { it.domain.contains(searchQuery, ignoreCase = true) }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.background,
        dragHandle = null,
        modifier = Modifier.fillMaxHeight(0.9f)
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
                        text = "Website & Domain Shield",
                        style = typography.itemTitle.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Block distracting websites on device",
                        style = typography.caption,
                        color = colors.textSecondary
                    )
                }

                KintsuButton(
                    text = "Add Site",
                    onClick = { showAddDialog = true },
                    variant = KintsuButtonVariant.PRIMARY
                )
            }

            Spacer(modifier = Modifier.height(spacing.m))

            // Quick Recommendations
            Text(
                text = "QUICK ADD COMMON DISTRACTIONS",
                style = typography.caption.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp),
                color = colors.textTertiary
            )
            Spacer(modifier = Modifier.height(spacing.xs))

            val quickSuggestions = listOf("instagram.com", "tiktok.com", "reddit.com", "twitter.com", "youtube.com")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(spacing.xs)
            ) {
                quickSuggestions.forEach { suggestion ->
                    val isAlreadyAdded = blockedWebsites.any { it.domain.equals(suggestion, ignoreCase = true) }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isAlreadyAdded) colors.surfaceElevated else colors.goldSeamSoft)
                            .border(1.dp, if (isAlreadyAdded) colors.border else colors.goldSeam, RoundedCornerShape(8.dp))
                            .clickable(enabled = !isAlreadyAdded) {
                                onAddWebsite(suggestion, "Social", "ALWAYS_BLOCKED", true)
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (isAlreadyAdded) "✓ $suggestion" else "+ $suggestion",
                            fontSize = 12.sp,
                            color = if (isAlreadyAdded) colors.textTertiary else colors.goldSeam,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(spacing.m))

            // Search
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search blocked websites...", style = typography.body, color = colors.textTertiary) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.goldSeam,
                    unfocusedBorderColor = colors.border,
                    focusedContainerColor = colors.surfaceElevated,
                    unfocusedContainerColor = colors.surfaceElevated
                )
            )

            Spacer(modifier = Modifier.height(spacing.m))

            if (filteredWebsites.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (searchQuery.isBlank()) "No websites restricted yet." else "No matches found.",
                            style = typography.itemTitle,
                            color = colors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(spacing.xs))
                        Text(
                            text = "Add domains to restrict them during Focus or always.",
                            style = typography.caption,
                            color = colors.textTertiary
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(spacing.s)
                ) {
                    items(filteredWebsites, key = { it.id }) { site ->
                        WebsiteRowItem(
                            site = site,
                            onToggle = { onToggleWebsite(site.copy(isEnabled = !site.isEnabled)) },
                            onDelete = { onDeleteWebsite(site) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        KintsuDialog(
            title = "Add Blocked Website",
            onDismissRequest = { showAddDialog = false },
            confirmButton = {
                KintsuButton(
                    text = "Add Restriction",
                    onClick = {
                        val (cleaned, matchSubs) = WebsiteFilterManager.cleanDomainInput(newDomainInput)
                        if (cleaned.isNotBlank()) {
                            onAddWebsite(cleaned, newGroupInput, newModeInput, matchSubdomainsInput)
                            newDomainInput = ""
                            showAddDialog = false
                        }
                    },
                    variant = KintsuButtonVariant.PRIMARY,
                    enabled = newDomainInput.isNotBlank()
                )
            },
            dismissButton = {
                KintsuButton(
                    text = "Cancel",
                    onClick = { showAddDialog = false },
                    variant = KintsuButtonVariant.SECONDARY
                )
            }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.m)) {
                OutlinedTextField(
                    value = newDomainInput,
                    onValueChange = { newDomainInput = it },
                    label = { Text("Domain (e.g. tiktok.com, x.com)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { matchSubdomainsInput = !matchSubdomainsInput },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Include Subdomains (*.domain.com)",
                            style = typography.body,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Blocks m.site.com, api.site.com, etc.",
                            style = typography.caption,
                            color = colors.textSecondary
                        )
                    }
                    Switch(
                        checked = matchSubdomainsInput,
                        onCheckedChange = { matchSubdomainsInput = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = colors.goldSeam,
                            checkedTrackColor = colors.goldSeamSoft
                        )
                    )
                }

                Text(
                    text = "Enforcement Mode:",
                    style = typography.caption.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.textPrimary
                )

                val modes = listOf(
                    "ALWAYS_BLOCKED" to "Always Blocked",
                    "FOCUS_ONLY" to "Focus Lock Only",
                    "SCHEDULED" to "Scheduled Windows"
                )

                modes.forEach { (modeKey, modeTitle) ->
                    val isSel = newModeInput == modeKey
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSel) colors.goldSeamSoft else colors.surface)
                            .border(1.dp, if (isSel) colors.goldSeam else colors.border, RoundedCornerShape(8.dp))
                            .clickable { newModeInput = modeKey }
                            .padding(horizontal = spacing.m, vertical = spacing.s)
                    ) {
                        Text(
                            text = modeTitle,
                            style = typography.body,
                            color = if (isSel) colors.goldSeam else colors.textPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WebsiteRowItem(
    site: BlockedWebsiteEntity,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography

    KintsuCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = if (site.isEnabled) colors.surfaceElevated else colors.surface,
        borderColor = if (site.isEnabled) colors.goldSeam.copy(alpha = 0.4f) else colors.border
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.m),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.surface),
                contentAlignment = Alignment.Center
            ) {
                IconGlobe(size = 20.dp, color = colors.goldSeam)
            }

            Spacer(modifier = Modifier.width(spacing.m))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (site.matchSubdomains) "*.${site.domain}" else site.domain,
                    style = typography.itemTitle,
                    color = colors.textPrimary
                )
                Text(
                    text = when (site.mode) {
                        "FOCUS_ONLY" -> "Restricted during Focus"
                        "SCHEDULED" -> "Scheduled hours"
                        else -> "Always restricted"
                    },
                    style = typography.caption,
                    color = colors.textSecondary
                )
            }

            Switch(
                checked = site.isEnabled,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = colors.goldSeam,
                    checkedTrackColor = colors.goldSeamSoft
                )
            )

            Spacer(modifier = Modifier.width(spacing.xs))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable(onClick = onDelete)
                    .padding(spacing.xs)
            ) {
                IconClose(size = 18.dp, color = colors.textTertiary)
            }
        }
    }
}
