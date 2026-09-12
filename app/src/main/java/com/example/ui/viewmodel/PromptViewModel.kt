package com.example.ui.viewmodel

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.PromptEntity
import com.example.data.local.UserEntity
import com.example.data.repository.PromptRepository
import com.example.ui.model.PromptPresets
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PromptViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PromptRepository

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = PromptRepository(database.promptDao(), database.userDao())
    }

    val currentUser: StateFlow<UserEntity?> = repository.currentUser.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    // Generator UI State
    val selectedRole = MutableStateFlow(PromptPresets.roles[0])
    val customRole = MutableStateFlow("")
    val topic = MutableStateFlow("")
    val outputFormat = MutableStateFlow(PromptPresets.outputFormats[0])
    val tone = MutableStateFlow(PromptPresets.tones[0])
    val audience = MutableStateFlow(PromptPresets.audiences[1])
    val additionalContext = MutableStateFlow("")
    val isGenerating = MutableStateFlow(false)
    val generatedResult = MutableStateFlow<PromptEntity?>(null)

    // Optimizer UI State
    val rawPromptInput = MutableStateFlow("")
    val selectedFocusOptions = MutableStateFlow<Set<String>>(
        setOf(PromptPresets.optimizationFocus[0], PromptPresets.optimizationFocus[1], PromptPresets.optimizationFocus[3])
    )
    val isOptimizing = MutableStateFlow(false)
    val optimizedResult = MutableStateFlow<PromptEntity?>(null)

    // Code-to-Prompt UI State
    val selectedLanguage = MutableStateFlow(PromptPresets.programmingLanguages[0])
    val sourceCodeInput = MutableStateFlow(PromptPresets.sampleCodeSnippets["Kotlin"] ?: "")
    val analysisMode = MutableStateFlow("BUG_FIX") // "BUG_FIX", "EXPLAIN", "UNIT_TEST", "REFACTOR"
    val isAnalyzingCode = MutableStateFlow(false)
    val codePromptResult = MutableStateFlow<PromptEntity?>(null)

    // History & Search Filter State
    val searchQuery = MutableStateFlow("")
    val selectedCategoryFilter = MutableStateFlow("ALL") // "ALL", "FAVORITE", "GENERATOR", "OPTIMIZER", "CODE_TO_PROMPT"

    val filteredPrompts: StateFlow<List<PromptEntity>> = combine(
        repository.allPrompts,
        searchQuery,
        selectedCategoryFilter
    ) { prompts, query, filter ->
        prompts.filter { item ->
            val matchesFilter = when (filter) {
                "FAVORITE" -> item.isFavorite
                "ALL" -> true
                else -> item.category.equals(filter, ignoreCase = true)
            }
            val matchesQuery = if (query.isBlank()) true else {
                item.title.contains(query, ignoreCase = true) ||
                        item.content.contains(query, ignoreCase = true) ||
                        item.topic.contains(query, ignoreCase = true) ||
                        item.role.contains(query, ignoreCase = true)
            }
            matchesFilter && matchesQuery
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // System Feedback / Snackbars
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun showMessage(msg: String) {
        _userMessage.value = msg
    }

    // Generator Action
    fun generatePrompt() {
        val activeRole = if (selectedRole.value.contains("Custom")) {
            customRole.value.ifBlank { "Senior Expert" }
        } else {
            selectedRole.value
        }

        if (topic.value.isBlank()) {
            showMessage("Mohon isi topik atau konteks prompt terlebih dahulu")
            return
        }

        viewModelScope.launch {
            isGenerating.value = true
            try {
                val user = currentUser.value
                val apiKey = user?.customApiKey ?: ""
                val model = user?.preferredModel ?: "gemini-3.5-flash"
                val temperature = user?.temperature ?: 0.7f
                val lang = user?.promptLanguage ?: "id"

                val result = repository.generatePrompt(
                    role = activeRole,
                    topic = topic.value.trim(),
                    outputFormat = outputFormat.value,
                    tone = tone.value,
                    audience = audience.value,
                    additionalContext = additionalContext.value.trim(),
                    apiKey = apiKey,
                    model = model,
                    temperature = temperature,
                    language = lang
                )

                val savedId = repository.savePrompt(result)
                val fullResult = result.copy(id = savedId)
                generatedResult.value = fullResult
                showMessage("Master prompt berhasil dibuat dan disimpan ke riwayat!")
            } catch (e: Exception) {
                showMessage("Gagal membuat prompt: ${e.localizedMessage}")
            } finally {
                isGenerating.value = false
            }
        }
    }

    // Optimizer Action
    fun optimizePrompt() {
        if (rawPromptInput.value.isBlank()) {
            showMessage("Masukkan prompt mentah yang ingin dioptimasi")
            return
        }

        viewModelScope.launch {
            isOptimizing.value = true
            try {
                val user = currentUser.value
                val apiKey = user?.customApiKey ?: ""
                val model = user?.preferredModel ?: "gemini-3.5-flash"
                val temperature = user?.temperature ?: 0.7f
                val lang = user?.promptLanguage ?: "id"

                val result = repository.optimizePrompt(
                    originalPrompt = rawPromptInput.value.trim(),
                    focusOptions = selectedFocusOptions.value.toList(),
                    apiKey = apiKey,
                    model = model,
                    temperature = temperature,
                    language = lang
                )

                val savedId = repository.savePrompt(result)
                val fullResult = result.copy(id = savedId)
                optimizedResult.value = fullResult
                showMessage("Prompt berhasil dioptimasi dengan skor efektivitas ${fullResult.scoreAfter}/100!")
            } catch (e: Exception) {
                showMessage("Gagal mengoptimasi prompt: ${e.localizedMessage}")
            } finally {
                isOptimizing.value = false
            }
        }
    }

    fun toggleFocusOption(option: String) {
        val current = selectedFocusOptions.value.toMutableSet()
        if (current.contains(option)) {
            if (current.size > 1) {
                current.remove(option)
            }
        } else {
            current.add(option)
        }
        selectedFocusOptions.value = current
    }

    // Code-to-Prompt Action
    fun generateCodePrompt() {
        if (sourceCodeInput.value.isBlank()) {
            showMessage("Masukkan kode sumber yang ingin dianalisis")
            return
        }

        viewModelScope.launch {
            isAnalyzingCode.value = true
            try {
                val user = currentUser.value
                val apiKey = user?.customApiKey ?: ""
                val model = user?.preferredModel ?: "gemini-3.5-flash"
                val temperature = user?.temperature ?: 0.7f

                val result = repository.generateCodePrompt(
                    sourceCode = sourceCodeInput.value.trim(),
                    language = selectedLanguage.value,
                    analysisMode = analysisMode.value,
                    apiKey = apiKey,
                    model = model,
                    temperature = temperature
                )

                val savedId = repository.savePrompt(result)
                val fullResult = result.copy(id = savedId)
                codePromptResult.value = fullResult
                showMessage("Prompt analisis kode berhasil dirancang dan disimpan!")
            } catch (e: Exception) {
                showMessage("Gagal menganalisis kode: ${e.localizedMessage}")
            } finally {
                isAnalyzingCode.value = false
            }
        }
    }

    fun loadSampleCode(lang: String) {
        selectedLanguage.value = lang
        PromptPresets.sampleCodeSnippets[lang]?.let { snippet ->
            sourceCodeInput.value = snippet
        }
    }

    // History Actions
    fun toggleFavorite(prompt: PromptEntity) {
        viewModelScope.launch {
            val newStatus = !prompt.isFavorite
            repository.toggleFavorite(prompt.id, newStatus)
            showMessage(if (newStatus) "Ditambahkan ke Favorit" else "Dihapus dari Favorit")
        }
    }

    fun deletePrompt(prompt: PromptEntity) {
        viewModelScope.launch {
            repository.deletePrompt(prompt)
            showMessage("Prompt telah dihapus dari riwayat")
        }
    }

    fun copyToClipboard(text: String, label: String = "Prompt") {
        val context = getApplication<Application>()
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        showMessage("Teks prompt berhasil disalin ke clipboard!")
    }

    fun sharePrompt(context: Context, prompt: PromptEntity) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, prompt.title)
            putExtra(Intent.EXTRA_TEXT, "${prompt.title}\n\n${prompt.content}\n\n(Dibuat dengan aplikasi Prompt Cepat AI)")
        }
        context.startActivity(Intent.createChooser(shareIntent, "Bagikan Prompt"))
    }

    // Settings & User Profile
    fun updateLanguage(lang: String) {
        val current = currentUser.value ?: return
        viewModelScope.launch {
            repository.updateUser(current.copy(promptLanguage = lang))
            showMessage("Bahasa prompt default diubah ke ${if (lang == "id") "Bahasa Indonesia" else "English"}")
        }
    }

    fun updateThemeMode(mode: String) {
        val current = currentUser.value ?: return
        viewModelScope.launch {
            repository.updateUser(current.copy(themeMode = mode))
        }
    }

    fun updateModelPreference(model: String) {
        val current = currentUser.value ?: return
        viewModelScope.launch {
            repository.updateUser(current.copy(preferredModel = model))
            showMessage("Model AI diubah ke $model")
        }
    }

    fun updateTemperature(temp: Float) {
        val current = currentUser.value ?: return
        viewModelScope.launch {
            repository.updateUser(current.copy(temperature = temp))
        }
    }

    fun updateCustomApiKey(key: String) {
        val current = currentUser.value ?: return
        viewModelScope.launch {
            repository.updateUser(current.copy(customApiKey = key.trim()))
            showMessage("API Key kustom berhasil diperbarui!")
        }
    }

    fun updateProfile(name: String, email: String) {
        val current = currentUser.value ?: return
        viewModelScope.launch {
            repository.updateUser(current.copy(name = name.trim(), email = email.trim()))
            showMessage("Profil berhasil diperbarui!")
        }
    }

    fun toggleProTier() {
        val current = currentUser.value ?: return
        viewModelScope.launch {
            val nextState = !current.isPro
            repository.updateUser(current.copy(isPro = nextState))
            showMessage(if (nextState) "Selamat! Paket Pro Aktif" else "Paket beralih ke Free Tier")
        }
    }
}
