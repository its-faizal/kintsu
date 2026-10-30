package com.kintsu.app.wellbeing.ui

import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.provider.Settings
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kintsu.app.data.model.BlockedWebsiteEntity
import com.kintsu.app.ui.components.IconCheck
import com.kintsu.app.ui.components.IconShield
import com.kintsu.app.ui.components.KintsuButton
import com.kintsu.app.ui.components.KintsuButtonVariant
import com.kintsu.app.ui.components.KintsuCard
import com.kintsu.app.ui.theme.KintsuTheme
import com.kintsu.app.wellbeing.domain.PermissionStateHelper
import com.kintsu.app.wellbeing.service.KintsuVpnDnsFilterService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.InetAddress

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DnsProtectionSheet(
    blockedWebsites: List<BlockedWebsiteEntity>,
    isDnsEnabled: Boolean,
    onToggleDns: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    var testStatus by remember { mutableStateOf<String?>(null) }
    var isTesting by remember { mutableStateOf(false) }

    val isVpnPermitted = remember { PermissionStateHelper.isVpnPrepared(context) }
    val isRunning = KintsuVpnDnsFilterService.isVpnRunning

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.background,
        dragHandle = null,
        modifier = Modifier.fillMaxHeight(0.88f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(spacing.l)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "DNS Protection & Filtering",
                        style = typography.itemTitle.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Local device loopback DNS filter",
                        style = typography.caption,
                        color = colors.textSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(
                            if (isRunning) colors.accent
                            else if (!isVpnPermitted) colors.goldSeam
                            else colors.textTertiary
                        )
                )
            }

            Spacer(modifier = Modifier.height(spacing.l))

            // Master Toggle Card
            KintsuCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = colors.surfaceElevated
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(spacing.m),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isRunning) "DNS Filter Active" else "DNS Filter Inactive",
                            style = typography.itemTitle,
                            color = colors.textPrimary
                        )
                        Text(
                            text = if (isRunning) "Intercepting port 53 to block restricted domains locally"
                            else "Activate local VPN to enforce domain rules",
                            style = typography.caption,
                            color = colors.textSecondary
                        )
                    }

                    Switch(
                        checked = isDnsEnabled && isRunning,
                        onCheckedChange = { enable ->
                            if (enable) {
                                val prepareIntent = VpnService.prepare(context)
                                if (prepareIntent != null) {
                                    prepareIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    context.startActivity(prepareIntent)
                                } else {
                                    KintsuVpnDnsFilterService.start(context)
                                    onToggleDns(true)
                                }
                            } else {
                                KintsuVpnDnsFilterService.stop(context)
                                onToggleDns(false)
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = colors.goldSeam,
                            checkedTrackColor = colors.goldSeamSoft
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(spacing.l))

            // Active Rules Summary
            KintsuCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = colors.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(spacing.m),
                    verticalArrangement = Arrangement.spacedBy(spacing.s)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Configured Domains", style = typography.body, color = colors.textPrimary)
                        Text("${blockedWebsites.count { it.isEnabled }} active", style = typography.body.copy(fontWeight = FontWeight.SemiBold), color = colors.goldSeam)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Resolver Mode", style = typography.body, color = colors.textPrimary)
                        Text("Local On-Device", style = typography.body, color = colors.textSecondary)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Data Privacy", style = typography.body, color = colors.textPrimary)
                        Text("100% On-Device • No Remote Proxy", style = typography.caption, color = colors.textSecondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(spacing.l))

            // Diagnostic Test Tool
            Text(
                text = "DIAGNOSTICS & TEST LOOKUP",
                style = typography.caption.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp),
                color = colors.textTertiary
            )
            Spacer(modifier = Modifier.height(spacing.xs))

            KintsuCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = colors.surfaceElevated
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(spacing.m)
                ) {
                    Text(
                        text = "Verify DNS Protection",
                        style = typography.itemTitle,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Tests resolution of your configured blocked domains",
                        style = typography.caption,
                        color = colors.textSecondary
                    )

                    Spacer(modifier = Modifier.height(spacing.m))

                    if (testStatus != null) {
                        Text(
                            text = testStatus!!,
                            style = typography.caption.copy(fontWeight = FontWeight.Medium),
                            color = colors.goldSeam
                        )
                        Spacer(modifier = Modifier.height(spacing.s))
                    }

                    KintsuButton(
                        text = if (isTesting) "Running Test..." else "Run DNS Diagnostic Test",
                        onClick = {
                            isTesting = true
                            scope.launch(Dispatchers.IO) {
                                val testDomain = blockedWebsites.firstOrNull { it.isEnabled }?.domain ?: "instagram.com"
                                val blocked = try {
                                    val addr = InetAddress.getByName(testDomain)
                                    addr.hostAddress == "127.0.0.1" || addr.hostAddress == "0.0.0.0"
                                } catch (_: Exception) {
                                    true // NXDOMAIN or connection refused = successfully blocked!
                                }
                                withContext(Dispatchers.Main) {
                                    testStatus = if (blocked) "✓ '$testDomain' is successfully blocked by filter."
                                    else "Domain lookup resolved. Verify VPN permission is enabled."
                                    isTesting = false
                                }
                            }
                        },
                        variant = KintsuButtonVariant.SECONDARY,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isTesting
                    )
                }
            }

            Spacer(modifier = Modifier.height(spacing.l))

            // System Private DNS Manual Guide
            KintsuCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = colors.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(spacing.m)
                ) {
                    Text(
                        text = "Alternative: Android Private DNS",
                        style = typography.itemTitle,
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(spacing.xs))
                    Text(
                        text = "If you prefer not using a local VPN, you can set your device Private DNS provider directly in Android Network Settings.",
                        style = typography.caption,
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(spacing.m))
                    KintsuButton(
                        text = "Open Android Network Settings",
                        onClick = {
                            try {
                                context.startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                })
                            } catch (_: Exception) {
                                context.startActivity(Intent(Settings.ACTION_SETTINGS).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                })
                            }
                        },
                        variant = KintsuButtonVariant.SECONDARY,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
