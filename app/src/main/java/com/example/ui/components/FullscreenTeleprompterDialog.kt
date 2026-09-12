package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun FullscreenTeleprompterDialog(
    title: String,
    content: String,
    initialSpeed: Float,
    initialFontSize: Int,
    initialMirrorMode: Boolean,
    initialFocusGuide: Boolean,
    onDismiss: () -> Unit,
    onSpeedChange: (Float) -> Unit,
    onFontSizeChange: (Int) -> Unit,
    onMirrorToggle: () -> Unit,
    onFocusGuideToggle: () -> Unit
) {
    var speed by remember { mutableStateOf(initialSpeed) }
    var fontSize by remember { mutableIntStateOf(initialFontSize) }
    var isMirrorMode by remember { mutableStateOf(initialMirrorMode) }
    var isFocusGuide by remember { mutableStateOf(initialFocusGuide) }
    var isPlaying by remember { mutableStateOf(false) }
    var countdown by remember { mutableIntStateOf(0) }
    var showControls by remember { mutableStateOf(true) }

    val scrollState = rememberScrollState()
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()

    // Handle Countdown before playing
    LaunchedEffect(countdown) {
        if (countdown > 0) {
            delay(1000L)
            countdown -= 1
            if (countdown == 0) {
                isPlaying = true
            }
        }
    }

    // Auto-scroll loop
    LaunchedEffect(isPlaying, speed) {
        while (isPlaying) {
            delay(16L) // ~60 FPS smooth scrolling
            val step = speed * 0.9f
            scrollState.scrollBy(step)
            if (scrollState.value >= scrollState.maxValue && scrollState.maxValue > 0) {
                isPlaying = false
                break
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF0A0A0A) // Studio OLED Dark
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        // Tap anywhere on canvas to toggle play/pause or show controls
                        if (isPlaying) {
                            isPlaying = false
                        } else {
                            showControls = !showControls
                        }
                    }
            ) {
                // Main Scrolling Content
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                        .graphicsLayer {
                            scaleX = if (isMirrorMode) -1f else 1f
                        }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(top = 180.dp, bottom = 320.dp)
                    ) {
                        Text(
                            text = content,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = fontSize.sp,
                                lineHeight = (fontSize * 1.5).sp,
                                letterSpacing = 0.5.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = Color(0xFFF1F5F9),
                            textAlign = TextAlign.Start
                        )
                    }
                }

                // Reading Focus Guide (Center Highlight Band)
                if (isFocusGuide) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(84.dp)
                            .align(Alignment.Center)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0x003B82F6),
                                        Color(0x2B3B82F6),
                                        Color(0x003B82F6)
                                    )
                                )
                            )
                            .border(
                                width = 1.dp,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0x5560A5FA),
                                        Color(0x0060A5FA)
                                    )
                                ),
                                shape = RectangleShape
                            )
                    )
                }

                // Countdown Overlay (3.. 2.. 1..)
                AnimatedVisibility(
                    visible = countdown > 0,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Color(0xDD2563EB)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$countdown",
                            fontSize = 64.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }

                // Top Header Bar
                AnimatedVisibility(
                    visible = showControls,
                    enter = fadeIn(tween(200)),
                    exit = fadeOut(tween(200)),
                    modifier = Modifier.align(Alignment.TopCenter)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xE6000000), Color(0x00000000))
                                )
                            )
                            .padding(horizontal = 16.dp, vertical = 28.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = if (isMirrorMode) "Mode Cermin (Mirror Rig Aktif)" else "Mode Layar Normal",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isMirrorMode) Color(0xFFF59E0B) else Color(0xFF94A3B8)
                            )
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0x33FFFFFF))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Tutup Prompter",
                                tint = Color.White
                            )
                        }
                    }
                }

                // Bottom Floating Control Deck
                AnimatedVisibility(
                    visible = showControls,
                    enter = fadeIn(tween(200)),
                    exit = fadeOut(tween(200)),
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0x00000000), Color(0xF0050505), Color(0xFF000000))
                                )
                            )
                            .padding(horizontal = 16.dp, vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Quick Settings Row (Speed, Font, Mirror, Focus)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0x33FFFFFF))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            // Speed Control
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Speed,
                                    contentDescription = null,
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(
                                    onClick = {
                                        if (speed > 1f) {
                                            speed -= 0.5f
                                            onSpeedChange(speed)
                                        }
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "Perlambat", tint = Color.White)
                                }
                                Text(
                                    text = String.format(java.util.Locale.US, "%.1fx", speed),
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                IconButton(
                                    onClick = {
                                        if (speed < 10f) {
                                            speed += 0.5f
                                            onSpeedChange(speed)
                                        }
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = "Percepat", tint = Color.White)
                                }
                            }

                            // Font Size Control
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.TextFields,
                                    contentDescription = null,
                                    tint = Color(0xFFA78BFA),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(
                                    onClick = {
                                        if (fontSize > 16) {
                                            fontSize -= 2
                                            onFontSizeChange(fontSize)
                                        }
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "Kecilkan Font", tint = Color.White)
                                }
                                Text(
                                    text = "${fontSize}sp",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                IconButton(
                                    onClick = {
                                        if (fontSize < 48) {
                                            fontSize += 2
                                            onFontSizeChange(fontSize)
                                        }
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = "Besarkan Font", tint = Color.White)
                                }
                            }

                            // Mirror Mode Toggle
                            IconButton(
                                onClick = {
                                    isMirrorMode = !isMirrorMode
                                    onMirrorToggle()
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isMirrorMode) Color(0xFFF59E0B) else Color.Transparent)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Flip,
                                    contentDescription = "Mirror Mode",
                                    tint = if (isMirrorMode) Color.Black else Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Focus Guide Toggle
                            IconButton(
                                onClick = {
                                    isFocusGuide = !isFocusGuide
                                    onFocusGuideToggle()
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isFocusGuide) Color(0xFF3B82F6) else Color.Transparent)
                            ) {
                                Icon(
                                    imageVector = if (isFocusGuide) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Garis Fokus",
                                    tint = if (isFocusGuide) Color.White else Color(0xFF64748B),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Primary Play / Pause & Rewind Deck
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            // Rewind to top
                            IconButton(
                                onClick = {
                                    isPlaying = false
                                    coroutineScope.launch {
                                        scrollState.scrollTo(0)
                                    }
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x22FFFFFF))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RestartAlt,
                                    contentDescription = "Ulang ke Awal",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            // Big Master Play/Pause Button
                            Surface(
                                shape = CircleShape,
                                color = if (isPlaying) Color(0xFFEF4444) else Color(0xFF2563EB),
                                modifier = Modifier
                                    .size(72.dp)
                                    .clickable {
                                        if (isPlaying) {
                                            isPlaying = false
                                        } else {
                                            // Start countdown 3.. 2.. 1..
                                            countdown = 3
                                        }
                                    }
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = if (isPlaying) "Jeda" else "Putar",
                                        tint = Color.White,
                                        modifier = Modifier.size(38.dp)
                                    )
                                }
                            }

                            // Tap to toggle controls hint
                            Text(
                                text = if (isPlaying) "Ketuk layar\nuntuk jeda" else "Siap rekam\nTekan putar",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF94A3B8),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}
