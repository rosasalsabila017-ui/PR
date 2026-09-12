package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_account")
data class UserEntity(
    @PrimaryKey
    val id: String = "primary_user",
    val name: String = "Pengguna Kreatif",
    val email: String = "user@promptcepat.ai",
    val isPro: Boolean = true,
    val promptLanguage: String = "id", // "id", "en"
    val themeMode: String = "SYSTEM", // "SYSTEM", "DARK", "LIGHT"
    val preferredModel: String = "gemini-3.5-flash", // "gemini-3.5-flash", "gemini-3.1-pro-preview"
    val temperature: Float = 0.7f,
    val customApiKey: String = "",
    val totalPromptsGenerated: Int = 18,
    val isLoggedIn: Boolean = true
)
