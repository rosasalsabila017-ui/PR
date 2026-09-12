package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.PromptEntity
import com.example.ui.components.PromptCard
import com.example.ui.components.PromptDetailDialog
import com.example.ui.viewmodel.PromptViewModel

@Composable
fun HistoryScreen(
    viewModel: PromptViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prompts by viewModel.filteredPrompts.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedCategoryFilter.collectAsStateWithLifecycle()

    var selectedPromptForDetail by remember { mutableStateOf<PromptEntity?>(null) }

    val filterOptions = listOf(
        Pair("ALL", "Semua"),
        Pair("FAVORITE", "Favorit ⭐"),
        Pair("GENERATOR", "Generator"),
        Pair("OPTIMIZER", "Perbaikan"),
        Pair("CODE_TO_PROMPT", "Dari Kode")
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Search Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.searchQuery.value = it },
            placeholder = { Text("Cari judul, topik, atau kata kunci prompt...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Hapus")
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("history_search_input"),
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Filter chips horizontal scroll
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filterOptions.forEach { filter ->
                val isSelected = selectedFilter == filter.first
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.selectedCategoryFilter.value = filter.first },
                    label = { Text(filter.second, fontSize = 12.sp) },
                    shape = RoundedCornerShape(8.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // List or Empty state
        if (prompts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "Tidak ada hasil pencarian" else "Belum Ada Riwayat",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "Coba gunakan kata kunci pencarian yang lain." else "Buat prompt pertama Anda menggunakan menu Generator atau Perbaikan Prompt!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items = prompts, key = { it.id }) { prompt ->
                    PromptCard(
                        prompt = prompt,
                        onClick = { selectedPromptForDetail = prompt },
                        onCopy = { viewModel.copyToClipboard(it, prompt.title) },
                        onShare = { viewModel.sharePrompt(context, prompt) },
                        onToggleFavorite = { viewModel.toggleFavorite(prompt) },
                        onDelete = { viewModel.deletePrompt(prompt) },
                        onOpenPrompter = { p ->
                            viewModel.loadIntoPrompter(p.title, p.content, launchFullscreen = false)
                        }
                    )
                }
            }
        }
    }

    // Detail Dialog
    selectedPromptForDetail?.let { prompt ->
        PromptDetailDialog(
            prompt = prompt,
            onDismiss = { selectedPromptForDetail = null },
            onCopy = { viewModel.copyToClipboard(it, prompt.title) },
            onShare = { viewModel.sharePrompt(context, prompt) },
            onToggleFavorite = { viewModel.toggleFavorite(prompt) },
            onOpenPrompter = { p ->
                viewModel.loadIntoPrompter(p.title, p.content, launchFullscreen = false)
            }
        )
    }
}
