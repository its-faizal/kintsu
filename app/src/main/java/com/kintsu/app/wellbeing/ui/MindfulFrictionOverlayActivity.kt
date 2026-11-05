package com.kintsu.app.wellbeing.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.kintsu.app.data.KintsuDatabase
import com.kintsu.app.data.KintsuRepository
import com.kintsu.app.service.KintsuAccessibilityBlockerService
import com.kintsu.app.ui.theme.KintsuTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

class MindfulFrictionOverlayActivity : ComponentActivity() {

    companion object {
        const val EXTRA_TARGET_PACKAGE = "extra_target_package"
        const val EXTRA_APP_LABEL = "extra_app_label"

        // Track active approved micro passes: packageName -> expirationTimestampMillis
        private val activeMicroPasses = ConcurrentHashMap<String, Long>()

        fun isMicroPassActive(packageName: String): Boolean {
            val expiresAt = activeMicroPasses[packageName] ?: return false
            if (System.currentTimeMillis() < expiresAt) {
                return true
            }
            activeMicroPasses.remove(packageName)
            return false
        }

        fun getRemainingMicroPassSeconds(packageName: String): Long {
            val expiresAt = activeMicroPasses[packageName] ?: return 0L
            val rem = (expiresAt - System.currentTimeMillis()) / 1000L
            return rem.coerceAtLeast(0L)
        }

        fun grantMicroPass(packageName: String, durationMinutes: Int) {
            val expiresAt = System.currentTimeMillis() + (durationMinutes * 60 * 1000L)
            activeMicroPasses[packageName] = expiresAt
        }

        fun clearMicroPass(packageName: String) {
            activeMicroPasses.remove(packageName)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val targetPackage = intent.getStringExtra(EXTRA_TARGET_PACKAGE) ?: ""
        val appLabel = intent.getStringExtra(EXTRA_APP_LABEL) ?: targetPackage

        // Intercept back navigation: always return home to prevent fall-through
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navigateHome()
            }
        })

        setContent {
            KintsuTheme {
                MindfulFrictionContent(
                    packageName = targetPackage,
                    appLabel = appLabel,
                    onGrantPass = { minutes ->
                        grantMicroPass(targetPackage, minutes)
                        finish()
                    },
                    onStepBack = {
                        navigateHome()
                    }
                )
            }
        }
    }

    private fun navigateHome() {
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(homeIntent)
        finish()
    }
}

@Composable
fun MindfulFrictionContent(
    packageName: String,
    appLabel: String,
    onGrantPass: (minutes: Int) -> Unit,
    onStepBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var countdownSeconds by remember { mutableIntStateOf(5) }

    // Repository for coin rewards on stepping back
    val repository = remember {
        val db = KintsuDatabase.getDatabase(context.applicationContext)
        KintsuRepository.getInstance(context.applicationContext, db)
    }

    // App icon loader
    val appIcon = remember(packageName) {
        try {
            val pm = context.packageManager
            pm.getApplicationIcon(packageName)
        } catch (_: Exception) {
            null
        }
    }

    // Breathing pulse animation (4 seconds inhale, 4 seconds exhale)
    val infiniteTransition = rememberInfiniteTransition(label = "breath_pulse")
    val breathScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breath_scale"
    )

    // Countdown 5-second mindfulness timer
    LaunchedEffect(Unit) {
        while (countdownSeconds > 0) {
            delay(1000L)
            countdownSeconds--
        }
    }

    val breathPhase = if (breathScale > 1.05f) "Inhale deeply" else "Exhale softly"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xF50D0C0A))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF161412))
                .border(1.5.dp, Color(0x60D4AF37), RoundedCornerShape(24.dp))
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Target App Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF221F1B))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                if (appIcon != null) {
                    Image(
                        bitmap = appIcon.toBitmap(64, 64).asImageBitmap(),
                        contentDescription = appLabel,
                        modifier = Modifier
                            .size(20.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = appLabel,
                    color = Color(0xFFF5F2EB),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Animated Breathing Circle
            Box(
                modifier = Modifier
                    .size(130.dp),
                contentAlignment = Alignment.Center
            ) {
                // Expanding Gold Aura
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .scale(breathScale)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color(0x35D4AF37), Color(0x05D4AF37), Color.Transparent)
                            )
                        )
                        .border(1.dp, Color(0x40D4AF37), CircleShape)
                )

                // Core Circle
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E1A15))
                        .border(1.5.dp, Color(0xFFD4AF37), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (countdownSeconds > 0) "${countdownSeconds}s" else "Presence",
                        color = Color(0xFFD4AF37),
                        fontSize = if (countdownSeconds > 0) 20.sp else 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = breathPhase,
                color = Color(0xFFD4AF37),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Are you opening $appLabel with clear intent?",
                color = Color(0xFFA6A29A),
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Intention Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 5 Minutes Micro-Pass
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF24201A))
                        .border(1.dp, Color(0x40D4AF37), RoundedCornerShape(12.dp))
                        .clickable { onGrantPass(5) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "5 Minutes",
                        color = Color(0xFFF5F2EB),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // 15 Minutes Pass
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF24201A))
                        .border(1.dp, Color(0x40D4AF37), RoundedCornerShape(12.dp))
                        .clickable { onGrantPass(15) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "15 Minutes",
                        color = Color(0xFFF5F2EB),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Step Back Button (+5 Coins Reward)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFFE5C058), Color(0xFFD4AF37), Color(0xFFB5942D))
                        )
                    )
                    .clickable {
                        scope.launch {
                            repository.addCoins(5, "Conscious restraint: Stepped back from $appLabel")
                            Toast.makeText(context, "Conscious restraint honored. +5 Coins", Toast.LENGTH_SHORT).show()
                            onStepBack()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Reflect & Step Back (+5 Coins)",
                    color = Color(0xFF141210),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}
