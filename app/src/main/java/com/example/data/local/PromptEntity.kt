package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prompts")
data class PromptEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val category: String, // "GENERATOR", "OPTIMIZER", "CODE_TO_PROMPT", "TEMPLATE"
    val role: String = "",
    val topic: String = "",
    val outputFormat: String = "",
    val tone: String = "",
    val originalPrompt: String? = null,
    val improvements: String? = null,
    val scoreBefore: Int = 0,
    val scoreAfter: Int = 0,
    val sourceLanguage: String? = null,
    val sourceCode: String? = null,
    val isFavorite: Boolean = false,
    val isPublic: Boolean = false,
    val shareCode: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
