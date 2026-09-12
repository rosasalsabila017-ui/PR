package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.ResultDisplayCard
import com.example.ui.model.PromptPresets
import com.example.ui.viewmodel.PromptViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CodeToPromptScreen(
    viewModel: PromptViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val selectedLanguage by viewModel.selectedLanguage.collectAsStateWithLifecycle()
    val sourceCodeInput by viewModel.sourceCodeInput.collectAsStateWithLifecycle()
    val analysisMode by viewModel.analysisMode.collectAsStateWithLifecycle()
    val isAnalyzingCode by viewModel.isAnalyzingCode.collectAsStateWithLifecycle()
    val codePromptResult by viewModel.codePromptResult.collectAsStateWithLifecycle()

    val analysisModes = listOf(
        Triple("BUG_FIX", "Perbaikan Bug & Audit", Icons.Default.BugReport),
        Triple("EXPLAIN", "Penjelasan & Dokumen", Icons.Default.Description),
        Triple("UNIT_TEST", "Unit Test Suite", Icons.Default.FactCheck),
        Triple("REFACTOR", "Refactor & Clean Code", Icons.Default.Transform)
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Hero Header Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFF8B5CF6).copy(alpha = 0.25f),
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                            )
                        )
                    )
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF8B5CF6)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = "Prompt dari Code",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Prompt dari Source Code",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Analisis kode dan ciptakan master prompt perbaikan otomatis",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 1. Language selector chips
        Text(
            text = "Bahasa Pemrograman",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            PromptPresets.programmingLanguages.take(6).forEach { lang ->
                val isSelected = selectedLanguage == lang
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.loadSampleCode(lang) },
                    label = { Text(lang, fontSize = 12.sp) },
                    shape = RoundedCornerShape(8.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF8B5CF6).copy(alpha = 0.2f),
                        selectedLabelColor = Color(0xFF6D28D9)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 2. Analysis Mode Selection
        Text(
            text = "Mode Analisis Teknis",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            analysisModes.forEach { mode ->
                val isSelected = analysisMode == mode.first
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.analysisMode.value = mode.first },
                    leadingIcon = {
                        Icon(
                            imageVector = mode.third,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    label = { Text(mode.second, fontSize = 12.sp) },
                    shape = RoundedCornerShape(8.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 3. Source Code Editor Area
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Cuplikan Kode Sumber ($selectedLanguage)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            OutlinedButton(
                onClick = { viewModel.loadSampleCode(selectedLanguage) },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Muat Contoh", fontSize = 11.sp)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            OutlinedTextField(
                value = sourceCodeInput,
                onValueChange = { viewModel.sourceCodeInput.value = it },
                placeholder = { Text("// Tempelkan kode Anda di sini...") },
                textStyle = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("source_code_input"),
                minLines = 8,
                maxLines = 14,
                shape = RoundedCornerShape(12.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Action Button
        Button(
            onClick = { viewModel.generateCodePrompt() },
            enabled = !isAnalyzingCode && sourceCodeInput.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("analyze_code_button"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF8B5CF6)
            )
        ) {
            if (isAnalyzingCode) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.5.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text("Menganalisis Kode & Menyusun Prompt...", color = Color.White)
            } else {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Analisis & Buat Master Prompt",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        // Output Result
        AnimatedVisibility(
            visible = codePromptResult != null,
            enter = fadeIn() + slideInVertically()
        ) {
            codePromptResult?.let { result ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    ResultDisplayCard(
                        prompt = result,
                        onCopy = { viewModel.copyToClipboard(it, "Prompt Analisis Kode") },
                        onShare = { viewModel.sharePrompt(context, result) },
                        onToggleFavorite = { viewModel.toggleFavorite(result) },
                        onOpenPrompter = { p ->
                            viewModel.loadIntoPrompter(p.title, p.content, launchFullscreen = false)
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}
