package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.IntegrationInstructions
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VideoCameraFront
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.FillVariablesDialog
import com.example.ui.components.FullscreenTeleprompterDialog
import com.example.ui.model.PrompterPresets
import com.example.ui.viewmodel.PromptViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PrompterStudioScreen(
    viewModel: PromptViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val prompterTitle by viewModel.prompterTitle.collectAsStateWithLifecycle()
    val prompterContent by viewModel.prompterContent.collectAsStateWithLifecycle()
    val scrollSpeed by viewModel.prompterScrollSpeed.collectAsStateWithLifecycle()
    val fontSize by viewModel.prompterFontSize.collectAsStateWithLifecycle()
    val isMirrorMode by viewModel.prompterIsMirrorMode.collectAsStateWithLifecycle()
    val isFocusGuide by viewModel.prompterIsFocusGuideEnabled.collectAsStateWithLifecycle()
    val showFullscreen by viewModel.showFullscreenTeleprompter.collectAsStateWithLifecycle()

    var selectedStudioTab by remember { mutableIntStateOf(0) } // 0: Teleprompter, 1: AI Prompt Lab
    var showVariablesDialog by remember { mutableStateOf(false) }

    // Teleprompter Preview scroll state
    var isPreviewPlaying by remember { mutableStateOf(false) }
    val previewScrollState = rememberScrollState()

    // Variable extraction regex (finds {{variable}} and [variable])
    val detectedVariables by remember(prompterContent) {
        derivedStateOf {
            val braceRegex = Regex("""\{\{([a-zA-Z0-9_-]+)\}\}""")
            val bracketRegex = Regex("""\[([a-zA-Z0-9_-]+)\]""")

            val braceMatches = braceRegex.findAll(prompterContent).map { it.groupValues[1] }
            val bracketMatches = bracketRegex.findAll(prompterContent).map { it.groupValues[1] }

            (braceMatches + bracketMatches).distinct().filter { it.isNotBlank() }.toList()
        }
    }

    // Auto-scroll loop for preview
    LaunchedEffect(isPreviewPlaying, scrollSpeed) {
        while (isPreviewPlaying) {
            delay(16L)
            val step = scrollSpeed * 0.7f
            previewScrollState.scrollBy(step)
            if (previewScrollState.value >= previewScrollState.maxValue && previewScrollState.maxValue > 0) {
                isPreviewPlaying = false
                break
            }
        }
    }

    // Metrics calculations
    val wordCount = remember(prompterContent) {
        prompterContent.trim().split(Regex("\\s+")).filter { it.isNotBlank() }.size
    }
    val estimatedTokens = remember(wordCount) {
        (wordCount * 1.33).toInt().coerceAtLeast(1)
    }
    val estimatedReadMinutes = remember(wordCount) {
        // Average reading speed ~ 130 WPM
        val minutes = wordCount / 130.0
        val totalSeconds = (minutes * 60).toInt()
        val m = totalSeconds / 60
        val s = totalSeconds % 60
        String.format(Locale.US, "%02d:%02d", m, s)
    }

    // Fullscreen Teleprompter Modal
    if (showFullscreen) {
        FullscreenTeleprompterDialog(
            title = prompterTitle,
            content = prompterContent,
            initialSpeed = scrollSpeed,
            initialFontSize = fontSize,
            initialMirrorMode = isMirrorMode,
            initialFocusGuide = isFocusGuide,
            onDismiss = { viewModel.setFullscreenTeleprompter(false) },
            onSpeedChange = { viewModel.setPrompterSpeed(it) },
            onFontSizeChange = { viewModel.setPrompterFontSize(it) },
            onMirrorToggle = { viewModel.togglePrompterMirrorMode() },
            onFocusGuideToggle = { viewModel.togglePrompterFocusGuide() }
        )
    }

    // Fill Variables Modal
    if (showVariablesDialog && detectedVariables.isNotEmpty()) {
        FillVariablesDialog(
            variables = detectedVariables,
            onDismiss = { showVariablesDialog = false },
            onApply = { values ->
                viewModel.applyVariableReplacements(values)
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Hero Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF0F172A),
                                Color(0xFF1E293B),
                                Color(0xFF2563EB)
                            )
                        )
                    )
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF38BDF8).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VideoCameraFront,
                                    contentDescription = null,
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Studio Prompter Pro",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Teleprompter live, variabel dinamis, & AI lab",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }

                        // Fullscreen quick launch button
                        IconButton(
                            onClick = { viewModel.setFullscreenTeleprompter(true) },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0xFF2563EB))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fullscreen,
                                contentDescription = "Buka Layar Penuh",
                                tint = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Live Stat Pill Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x33000000))
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$wordCount Kata",
                            color = Color(0xFFE2E8F0),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "~$estimatedTokens Token",
                            color = Color(0xFF38BDF8),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "⏱ $estimatedReadMinutes Menit",
                            color = Color(0xFFFBBF24),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (detectedVariables.isNotEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF8B5CF6)
                            ) {
                                Text(
                                    text = "${detectedVariables.size} Variabel",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Mode Navigation TabRow
        TabRow(
            selectedTabIndex = selectedStudioTab,
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
        ) {
            Tab(
                selected = selectedStudioTab == 0,
                onClick = { selectedStudioTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Teleprompter Live", fontWeight = FontWeight.Bold)
                    }
                }
            )
            Tab(
                selected = selectedStudioTab == 1,
                onClick = { selectedStudioTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("AI Prompter Lab", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tab Content
        if (selectedStudioTab == 0) {
            // === TAB 0: TELEPROMPTER STUDIO ===
            // 1. Live Interactive Preview Monitor
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Title & Action Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "MONITOR TELEPROMPTER",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF38BDF8),
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = prompterTitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        // Fullscreen Studio Button
                        Button(
                            onClick = { viewModel.setFullscreenTeleprompter(true) },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fullscreen,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Layar Penuh", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Monitor Canvas
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF050505))
                            .border(1.dp, Color(0xFF334155), RoundedCornerShape(14.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                isPreviewPlaying = !isPreviewPlaying
                            }
                    ) {
                        // Scrolling Text
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp)
                                .graphicsLayer {
                                    scaleX = if (isMirrorMode) -1f else 1f
                                }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(previewScrollState)
                                    .padding(top = 90.dp, bottom = 120.dp)
                            ) {
                                Text(
                                    text = prompterContent,
                                    fontSize = (fontSize - 4).sp,
                                    lineHeight = ((fontSize - 4) * 1.45).sp,
                                    color = Color(0xFFF8FAFC),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Reading Focus Guide (Center Highlight)
                        if (isFocusGuide) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .align(Alignment.Center)
                                    .background(Color(0x1F3B82F6))
                                    .border(
                                        width = 1.dp,
                                        color = Color(0x6660A5FA)
                                    )
                            )
                        }

                        // Play/Pause Overlay indicator
                        Box(
                            modifier = Modifier
                                .padding(10.dp)
                                .align(Alignment.BottomEnd)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x99000000))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isPreviewPlaying) "● Auto-Scroll Aktif" else "❚❚ Dijeda (Ketuk untuk putar)",
                                color = if (isPreviewPlaying) Color(0xFF34D399) else Color(0xFF94A3B8),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Player Control Deck
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Play / Pause Button
                        Button(
                            onClick = { isPreviewPlaying = !isPreviewPlaying },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isPreviewPlaying) Color(0xFFEF4444) else Color(0xFF10B981)
                            ),
                            modifier = Modifier.weight(1.2f)
                        ) {
                            Icon(
                                imageVector = if (isPreviewPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isPreviewPlaying) "Jeda Scroll" else "Putar Scroll")
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Rewind to top
                        IconButton(
                            onClick = {
                                isPreviewPlaying = false
                                coroutineScope.launch { previewScrollState.scrollTo(0) }
                            },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0xFF1E293B))
                        ) {
                            Icon(Icons.Default.RestartAlt, contentDescription = "Ulang", tint = Color.White)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Mirror Mode Toggle
                        IconButton(
                            onClick = { viewModel.togglePrompterMirrorMode() },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (isMirrorMode) Color(0xFFF59E0B) else Color(0xFF1E293B))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Flip,
                                contentDescription = "Mode Cermin",
                                tint = if (isMirrorMode) Color.Black else Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Focus Guide Toggle
                        IconButton(
                            onClick = { viewModel.togglePrompterFocusGuide() },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (isFocusGuide) Color(0xFF3B82F6) else Color(0xFF1E293B))
                        ) {
                            Icon(
                                imageVector = if (isFocusGuide) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Garis Panduan",
                                tint = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Speed Slider Control
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Kecepatan: ${String.format(Locale.US, "%.1fx", scrollSpeed)}",
                            color = Color(0xFFE2E8F0),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.width(110.dp)
                        )
                        Slider(
                            value = scrollSpeed,
                            onValueChange = { viewModel.setPrompterSpeed(it) },
                            valueRange = 1.0f..10.0f,
                            steps = 17,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Font Size Slider Control
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.TextFields,
                            contentDescription = null,
                            tint = Color(0xFFA78BFA),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Ukuran Font: ${fontSize}sp",
                            color = Color(0xFFE2E8F0),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.width(110.dp)
                        )
                        Slider(
                            value = fontSize.toFloat(),
                            onValueChange = { viewModel.setPrompterFontSize(it.toInt()) },
                            valueRange = 18f..44f,
                            steps = 12,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Script Editor Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Naskah & Teks Prompter",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        if (detectedVariables.isNotEmpty()) {
                            Button(
                                onClick = { showVariablesDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(Icons.Default.DataObject, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Isi Variabel", fontSize = 11.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = prompterTitle,
                        onValueChange = { viewModel.updatePrompterTitle(it) },
                        label = { Text("Judul Naskah") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = prompterContent,
                        onValueChange = { viewModel.updatePrompterContent(it) },
                        label = { Text("Isi Naskah / Teks Prompt") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        shape = RoundedCornerShape(12.dp),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Quick Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.savePrompterAsPrompt() },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Simpan", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = { viewModel.copyToClipboard(prompterContent, prompterTitle) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Salin", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = { viewModel.resetPrompterToStarter() },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Reset")
                        }
                    }
                }
            }

        } else {
            // === TAB 1: AI PROMPTER LAB ===
            // 1. Pro Framework Scaffolding
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Framework Prompt Standar Dunia",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Pilih struktur terbaik untuk mengunci kualitas jawaban AI tanpa halusinasi.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PrompterPresets.frameworks.forEach { framework ->
                            FilterChip(
                                selected = false,
                                onClick = { viewModel.insertFrameworkTemplate(framework.key) },
                                label = { Text(framework.name, fontWeight = FontWeight.SemiBold) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Dynamic Variables Engine
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.DataObject,
                                contentDescription = null,
                                tint = Color(0xFF8B5CF6),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Variabel Dinamis {{placeholder}}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (detectedVariables.isNotEmpty()) Color(0xFF8B5CF6).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "${detectedVariables.size} Ditemukan",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (detectedVariables.isNotEmpty()) Color(0xFF8B5CF6) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (detectedVariables.isEmpty()) {
                        Text(
                            text = "Ketik {{nama_variabel}} atau [variabel] di dalam naskah prompt Anda untuk membuat slot isian dinamis otomatis!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = "Placeholder berikut terdeteksi dalam naskah:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            detectedVariables.forEach { variableName ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF8B5CF6).copy(alpha = 0.12f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.4f))
                                ) {
                                    Text(
                                        text = "{${variableName}}",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF7C3AED)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { showVariablesDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.DataObject, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Isi Semua Nilai Variabel Sekarang")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Pro Template Library
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Koleksi Template Kreator & Eksekutif",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    PrompterPresets.proTemplates.forEach { template ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { viewModel.insertProTemplate(template.id) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = template.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = template.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Pakai",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Token & Cost Estimator Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MonetizationOn,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Kalkulator Token & Estimasi Biaya",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Model Comparison Rows
                    val geminiFlashCost = String.format(Locale.US, "$%.6f", (estimatedTokens / 1_000_000.0) * 0.075)
                    val geminiProCost = String.format(Locale.US, "$%.5f", (estimatedTokens / 1_000_000.0) * 1.25)
                    val gpt4oCost = String.format(Locale.US, "$%.4f", (estimatedTokens / 1_000_000.0) * 5.0)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Gemini 1.5 Flash", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("Hemat & Ultra Cepat", color = Color(0xFF10B981), fontSize = 11.sp)
                        }
                        Text(geminiFlashCost, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Gemini 1.5 Pro", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("Reasoning Kompleks", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                        }
                        Text(geminiProCost, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("GPT-4o (OpenAI)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("Standard Flagship", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                        }
                        Text(gpt4oCost, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 5. Multi-Format Exporter Suite
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.IntegrationInstructions,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Ekspor Multi-Format Profesional",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val jsonPayload = """
                                {
                                  "role": "system",
                                  "content": ${prompterContent.replace("\"", "\\\"").replace("\n", "\\n")}
                                }
                                """.trimIndent()
                                viewModel.copyToClipboard(jsonPayload, "JSON Payload")
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("JSON API", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                val pythonSnippet = """
from google import genai

client = genai.Client()
response = client.models.generate_content(
    model="gemini-2.5-flash",
    contents=\"\"\"${prompterContent}\"\"\"
)
print(response.text)
                                """.trimIndent()
                                viewModel.copyToClipboard(pythonSnippet, "Python SDK")
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Python SDK", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.copyToClipboard(prompterContent, "Markdown")
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Markdown", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}
