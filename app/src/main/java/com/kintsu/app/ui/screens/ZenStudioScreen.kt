package com.kintsu.app.ui.screens

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kintsu.app.audio.ZenAudioPlayer
import com.kintsu.app.audio.ZenAudioService
import com.kintsu.app.audio.ZenSoundTrack
import com.kintsu.app.data.KintsuDatabase
import com.kintsu.app.data.KintsuRepository
import com.kintsu.app.ui.components.ZenFlipClock
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ZenStudioScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // Repository for coin rewards
    val repository = remember {
        val db = KintsuDatabase.getDatabase(context.applicationContext)
        KintsuRepository.getInstance(context.applicationContext, db)
    }

    // Audio state
    val currentTrack by ZenAudioPlayer.currentTrack.collectAsState()
    val isAudioPlaying by ZenAudioPlayer.isPlaying.collectAsState()
    val audioVolume by ZenAudioPlayer.volume.collectAsState()

    // Timer state
    var selectedDurationMinutes by remember { mutableIntStateOf(25) }
    var secondsRemaining by remember { mutableIntStateOf(25 * 60) }
    var isTimerRunning by remember { mutableStateOf(false) }
    var sessionCompleted by remember { mutableStateOf(false) }
    var earnedCoins by remember { mutableIntStateOf(0) }

    // Keep screen on during focus
    DisposableEffect(isTimerRunning) {
        val window = (context as? Activity)?.window
        if (isTimerRunning) {
            window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // Timer countdown loop
    LaunchedEffect(isTimerRunning, secondsRemaining) {
        if (isTimerRunning && secondsRemaining > 0) {
            delay(1000L)
            secondsRemaining--
            if (secondsRemaining == 0) {
                isTimerRunning = false
                sessionCompleted = true
                val reward = 25
                earnedCoins = reward
                scope.launch {
                    repository.addCoins(reward, "Completed ${selectedDurationMinutes}m Zen Focus session")
                }
            }
        }
    }

    val displayMinutes = secondsRemaining / 60
    val displaySeconds = secondsRemaining % 60

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF1F1A14), Color(0xFF0F0E0C), Color(0xFF0A0908)),
                    radius = 1200f
                )
            )
    ) {
        if (isLandscape) {
            // Landscape Layout: Clock on Left, Controls on Right
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Column: Split-Flap Clock
                Column(
                    modifier = Modifier.weight(1.2f),
                    horizontalAlignment = Alignment.CenterVertically,
                    verticalArrangement = Arrangement.Center
                ) {
                    ZenFlipClock(
                        minutes = displayMinutes,
                        seconds = displaySeconds,
                        digitWidth = 84.dp,
                        digitHeight = 120.dp,
                        fontSize = 62
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (isTimerRunning) "Focus Flow Active" else if (sessionCompleted) "Session Complete" else "Zen Desk Clock",
                        color = Color(0xFFD4AF37),
                        fontSize = 14.sp,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Right Column: Controls
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top Row with Back and Sound status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Exit Studio",
                                tint = Color(0xFFE8E5DF)
                            )
                        }

                        Text(
                            text = currentTrack?.title ?: "Ambient Silent",
                            color = Color(0xFFF5F2EB),
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Soundscapes Selector
                    SoundscapePillsRow(
                        currentTrack = currentTrack,
                        isPlaying = isAudioPlaying,
                        onSelectTrack = { track ->
                            ZenAudioService.startTrack(context, track)
                        },
                        onTogglePlay = {
                            currentTrack?.let {
                                ZenAudioPlayer.togglePlayPause(context, it)
                            } ?: ZenAudioService.startTrack(context, ZenSoundTrack.BAMBOO_RAIN)
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Start/Pause Button
                    ZenActionButton(
                        isTimerRunning = isTimerRunning,
                        sessionCompleted = sessionCompleted,
                        onToggle = {
                            if (sessionCompleted) {
                                secondsRemaining = selectedDurationMinutes * 60
                                sessionCompleted = false
                            }
                            isTimerRunning = !isTimerRunning
                            if (isTimerRunning && currentTrack == null) {
                                ZenAudioService.startTrack(context, ZenSoundTrack.BAMBOO_RAIN)
                            }
                        }
                    )
                }
            }
        } else {
            // Portrait Layout
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E1B18))
                            .border(1.dp, Color(0x30D4AF37), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFFE8E5DF),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "ZEN STUDIO",
                            color = Color(0xFFD4AF37),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 3.sp
                        )
                        Text(
                            text = "Deep Focus Sanctuary",
                            color = Color(0xFF9E9B95),
                            fontSize = 11.sp
                        )
                    }

                    Box(modifier = Modifier.size(40.dp))
                }

                // Center: Split-Flap Clock
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(vertical = 12.dp)
                ) {
                    ZenFlipClock(
                        minutes = displayMinutes,
                        seconds = displaySeconds,
                        digitWidth = 72.dp,
                        digitHeight = 104.dp,
                        fontSize = 52
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Focus Status Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF181512))
                            .border(1.dp, Color(0x40D4AF37), RoundedCornerShape(16.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (isTimerRunning) "FOCUS FLOW ACTIVE" else if (sessionCompleted) "SESSION COMPLETE" else "READY TO FOCUS",
                            color = if (isTimerRunning) Color(0xFFD4AF37) else Color(0xFFC4C0B6),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.5.sp
                        )
                    }
                }

                // Duration Selector Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf(15, 25, 45, 60).forEach { mins ->
                        val isSelected = selectedDurationMinutes == mins
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) Color(0xFF2E261B) else Color(0xFF161412))
                                .border(
                                    1.dp,
                                    if (isSelected) Color(0xFFD4AF37) else Color(0x20D4AF37),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    if (!isTimerRunning) {
                                        selectedDurationMinutes = mins
                                        secondsRemaining = mins * 60
                                        sessionCompleted = false
                                    }
                                }
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = "${mins}m",
                                color = if (isSelected) Color(0xFFF5F2EB) else Color(0xFF8E8B85),
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                // Bottom Section: Ambient Soundscapes & Action
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Volume Slider
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Soundscape Volume",
                            color = Color(0xFF9E9B95),
                            fontSize = 12.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Slider(
                            value = audioVolume,
                            onValueChange = { ZenAudioPlayer.setVolume(it) },
                            modifier = Modifier.weight(1.5f),
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFFD4AF37),
                                activeTrackColor = Color(0xFFD4AF37),
                                inactiveTrackColor = Color(0xFF2C2824)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 5 Ambient Soundscapes
                    SoundscapePillsRow(
                        currentTrack = currentTrack,
                        isPlaying = isAudioPlaying,
                        onSelectTrack = { track ->
                            ZenAudioService.startTrack(context, track)
                        },
                        onTogglePlay = {
                            currentTrack?.let {
                                ZenAudioPlayer.togglePlayPause(context, it)
                            } ?: ZenAudioService.startTrack(context, ZenSoundTrack.BAMBOO_RAIN)
                        }
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Primary Action Button
                    ZenActionButton(
                        isTimerRunning = isTimerRunning,
                        sessionCompleted = sessionCompleted,
                        onToggle = {
                            if (sessionCompleted) {
                                secondsRemaining = selectedDurationMinutes * 60
                                sessionCompleted = false
                            }
                            isTimerRunning = !isTimerRunning
                            if (isTimerRunning && currentTrack == null) {
                                ZenAudioService.startTrack(context, ZenSoundTrack.BAMBOO_RAIN)
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }

        // Session Completed Modal Overlay
        AnimatedVisibility(
            visible = sessionCompleted,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xE60D0C0A)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(32.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF1A1815))
                        .border(1.5.dp, Color(0xFFD4AF37), RoundedCornerShape(24.dp))
                        .padding(32.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2A2317))
                            .border(1.dp, Color(0xFFD4AF37), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Success",
                            tint = Color(0xFFD4AF37),
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "Focus Complete",
                        color = Color(0xFFF5F2EB),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "You maintained deep presence for $selectedDurationMinutes minutes.",
                        color = Color(0xFFA6A29A),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "+$earnedCoins Kintsu Coins",
                        color = Color(0xFFD4AF37),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFD4AF37))
                            .clickable {
                                secondsRemaining = selectedDurationMinutes * 60
                                sessionCompleted = false
                            }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Begin Another Session",
                            color = Color(0xFF141210),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SoundscapePillsRow(
    currentTrack: ZenSoundTrack?,
    isPlaying: Boolean,
    onSelectTrack: (ZenSoundTrack) -> Unit,
    onTogglePlay: () -> Unit
) {
    val scrollState = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ZenSoundTrack.values().forEach { track ->
            val isCurrent = currentTrack == track
            val isPlayingThis = isCurrent && isPlaying

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isCurrent) Color(0xFF262017) else Color(0xFF141210))
                    .border(
                        1.dp,
                        if (isCurrent) Color(0xFFD4AF37) else Color(0x25D4AF37),
                        RoundedCornerShape(14.dp)
                    )
                    .clickable {
                        if (isCurrent) {
                            onTogglePlay()
                        } else {
                            onSelectTrack(track)
                        }
                    }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(
                                if (isPlayingThis) Color(0xFFD4AF37)
                                else if (isCurrent) Color(0xFF8E8B85)
                                else Color(0xFF383531)
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = track.title,
                        color = if (isCurrent) Color(0xFFF5F2EB) else Color(0xFF8E8B85),
                        fontSize = 12.sp,
                        fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
fun ZenActionButton(
    isTimerRunning: Boolean,
    sessionCompleted: Boolean,
    onToggle: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    colors = if (isTimerRunning)
                        listOf(Color(0xFF2A241C), Color(0xFF1E1A14))
                    else
                        listOf(Color(0xFFE5C058), Color(0xFFD4AF37), Color(0xFFB5942D))
                )
            )
            .border(
                1.dp,
                if (isTimerRunning) Color(0xFFD4AF37) else Color.Transparent,
                RoundedCornerShape(16.dp)
            )
            .clickable { onToggle() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (sessionCompleted) "Reset Session" else if (isTimerRunning) "Pause Flow" else "Begin Deep Focus",
            color = if (isTimerRunning) Color(0xFFD4AF37) else Color(0xFF141210),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}
