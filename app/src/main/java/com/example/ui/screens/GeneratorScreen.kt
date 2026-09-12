package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.ResultDisplayCard
import com.example.ui.model.PromptPresets
import com.example.ui.viewmodel.PromptViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun GeneratorScreen(
    viewModel: PromptViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val selectedRole by viewModel.selectedRole.collectAsStateWithLifecycle()
    val customRole by viewModel.customRole.collectAsStateWithLifecycle()
    val topic by viewModel.topic.collectAsStateWithLifecycle()
    val outputFormat by viewModel.outputFormat.collectAsStateWithLifecycle()
    val tone by viewModel.tone.collectAsStateWithLifecycle()
    val audience by viewModel.audience.collectAsStateWithLifecycle()
    val additionalContext by viewModel.additionalContext.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    val generatedResult by viewModel.generatedResult.collectAsStateWithLifecycle()

    var roleDropdownExpanded by remember { mutableStateOf(false) }

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
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                                MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
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
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Generator Prompt",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Generator Prompt AI",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Susun instruksi master prompt berbasis parameter terstruktur",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 1. Parameter: Role / Persona
        Text(
            text = "1. Peran & Persona Ahli",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = roleDropdownExpanded,
            onExpandedChange = { roleDropdownExpanded = !roleDropdownExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedRole,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleDropdownExpanded) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
                    .testTag("role_dropdown"),
                shape = RoundedCornerShape(12.dp)
            )

            ExposedDropdownMenu(
                expanded = roleDropdownExpanded,
                onDismissRequest = { roleDropdownExpanded = false }
            ) {
                PromptPresets.roles.forEach { roleOption ->
                    DropdownMenuItem(
                        text = { Text(roleOption) },
                        onClick = {
                            viewModel.selectedRole.value = roleOption
                            roleDropdownExpanded = false
                        }
                    )
                }
            }
        }

        if (selectedRole.contains("Custom")) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = customRole,
                onValueChange = { viewModel.customRole.value = it },
                placeholder = { Text("Contoh: Ahli Mikrobiologi Forensik") },
                label = { Text("Tulis Peran Kustom") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("custom_role_input"),
                shape = RoundedCornerShape(12.dp)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 2. Parameter: Topik / Konteks
        Text(
            text = "2. Topik & Konteks Tugas",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = topic,
            onValueChange = { viewModel.topic.value = it },
            placeholder = { Text("Contoh: Buat strategi peluncuran produk kopi specialty kemasan dingin untuk milenial") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("topic_input"),
            minLines = 3,
            maxLines = 6,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(18.dp))

        // 3. Parameter: Format Output
        Text(
            text = "3. Format Output AI",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            PromptPresets.outputFormats.forEach { fmt ->
                val isSelected = outputFormat == fmt
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.outputFormat.value = fmt },
                    label = { Text(fmt, fontSize = 12.sp) },
                    shape = RoundedCornerShape(8.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 4. Parameter: Tone & Gaya Bahasa
        Text(
            text = "4. Nada & Gaya Bahasa",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            PromptPresets.tones.forEach { t ->
                val isSelected = tone == t
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.tone.value = t },
                    label = { Text(t, fontSize = 12.sp) },
                    shape = RoundedCornerShape(8.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 5. Target Audiens
        Text(
            text = "5. Target Audiens",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PromptPresets.audiences.forEach { aud ->
                val isSelected = audience == aud
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.audience.value = aud },
                    label = { Text(aud, fontSize = 12.sp) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 6. Catatan Tambahan
        Text(
            text = "6. Batasan / Catatan Tambahan (Opsional)",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = additionalContext,
            onValueChange = { viewModel.additionalContext.value = it },
            placeholder = { Text("Contoh: Hindari jargon berlebihan, batasi maksimal 500 kata") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("additional_context_input"),
            minLines = 2,
            maxLines = 4,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Generate Action Button
        Button(
            onClick = { viewModel.generatePrompt() },
            enabled = !isGenerating && topic.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("generate_prompt_button"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            if (isGenerating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.5.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = "Merancang Master Prompt...")
            } else {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Hasilkan Master Prompt",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Result Display
        AnimatedVisibility(
            visible = generatedResult != null,
            enter = fadeIn() + slideInVertically()
        ) {
            generatedResult?.let { prompt ->
                Column {
                    Spacer(modifier = Modifier.height(24.dp))
                    ResultDisplayCard(
                        prompt = prompt,
                        onCopy = { viewModel.copyToClipboard(it, "Prompt") },
                        onShare = { viewModel.sharePrompt(context, prompt) },
                        onToggleFavorite = { viewModel.toggleFavorite(prompt) },
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
