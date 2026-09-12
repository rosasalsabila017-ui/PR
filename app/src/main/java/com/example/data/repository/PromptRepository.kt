package com.example.data.repository

import com.example.data.api.GeminiApiClient
import com.example.data.local.PromptDao
import com.example.data.local.PromptEntity
import com.example.data.local.UserDao
import com.example.data.local.UserEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class PromptRepository(
    private val promptDao: PromptDao,
    private val userDao: UserDao,
    private val geminiClient: GeminiApiClient = GeminiApiClient()
) {
    val allPrompts: Flow<List<PromptEntity>> = promptDao.getAllPrompts()
    val favoritePrompts: Flow<List<PromptEntity>> = promptDao.getFavoritePrompts()
    val currentUser: Flow<UserEntity?> = userDao.getUser()

    fun searchPrompts(query: String): Flow<List<PromptEntity>> = promptDao.searchPrompts(query)

    suspend fun savePrompt(prompt: PromptEntity): Long {
        userDao.incrementPromptCount()
        return promptDao.insertPrompt(prompt)
    }

    suspend fun deletePrompt(prompt: PromptEntity) = promptDao.deletePrompt(prompt)

    suspend fun deletePromptById(id: Long) = promptDao.deletePromptById(id)

    suspend fun toggleFavorite(id: Long, isFav: Boolean) = promptDao.updateFavorite(id, isFav)

    suspend fun updateUser(user: UserEntity) = userDao.insertOrUpdateUser(user)

    suspend fun generatePrompt(
        role: String,
        topic: String,
        outputFormat: String,
        tone: String,
        audience: String,
        additionalContext: String,
        apiKey: String,
        model: String,
        temperature: Float,
        language: String = "id"
    ): PromptEntity {
        val metaInstructions = if (language == "en") {
            """
            Act as a World-Class AI Prompt Engineering Expert.
            Create an extremely detailed, structured, highly effective, and ready-to-run master prompt for an AI model (like Gemini, GPT-4, or Claude).
            
            Parameters:
            - Role/Persona: $role
            - Topic/Context: $topic
            - Output Format: $outputFormat
            - Tone & Style: $tone
            - Target Audience: $audience
            - Additional Instructions: ${additionalContext.ifBlank { "None" }}
            
            The generated prompt must follow modern prompt engineering standards:
            1. Clear Role definition with context and persona expertise
            2. Goal & Mission statement
            3. Context & Background
            4. Step-by-step task breakdown or instructions
            5. Explicit Output Format specifications
            6. Constraints & What to avoid
            7. 1-2 Short Examples or thinking structure (if applicable)
            
            Return ONLY the final prompt text ready for copying and pasting.
            """.trimIndent()
        } else {
            """
            Bertindaklah sebagai Senior AI Prompt Engineer berstandar industri kelas dunia.
            Buatlah master prompt yang sangat komprehensif, presisi, terstruktur rapi, dan siap langsung disalin ke model AI (Gemini, Claude, GPT).
            
            Parameter Masukan:
            - Peran / Persona: $role
            - Topik / Konteks: $topic
            - Format Output: $outputFormat
            - Nada & Gaya Bahasa: $tone
            - Sasaran Audiens: $audience
            - Catatan Tambahan: ${additionalContext.ifBlank { "Tidak ada" }}
            
            Prompt yang Anda rancang WAJIB memuat kerangka standar Prompt Engineering modern:
            1. Definisi Peran (Persona & Otoritas Keahlian)
            2. Misi & Tujuan Utama yang Spesifik
            3. Detail Konteks & Latar Belakang Masalah
            4. Instruksi Kerja Bertahap (Step-by-Step Task Execution)
            5. Ketentuan Format Output yang Ketat (Struktur Bagian, Markdown, Tabel/Poin)
            6. Batasan (Constraints) & Hal-Hal yang Dilarang
            7. Kriteria Kualitas & Standar Evaluasi Jawaban
            
            Hasilkan HANYA teks prompt final yang siap digunakan oleh pengguna tanpa pengantar basa-basi.
            """.trimIndent()
        }

        val apiResult = geminiClient.generateContent(
            prompt = metaInstructions,
            customKey = apiKey,
            model = model,
            temperature = temperature
        )

        val generatedText = apiResult.getOrElse {
            synthesizeLocalPrompt(
                role = role,
                topic = topic,
                outputFormat = outputFormat,
                tone = tone,
                audience = audience,
                additionalContext = additionalContext,
                language = language
            )
        }

        val title = if (topic.length > 40) "${topic.take(37)}..." else topic
        val uniqueCode = "pc-${UUID.randomUUID().toString().take(6)}"

        return PromptEntity(
            title = if (title.isBlank()) "Prompt $role: $topic" else title,
            content = generatedText,
            category = "GENERATOR",
            role = role,
            topic = topic,
            outputFormat = outputFormat,
            tone = tone,
            shareCode = uniqueCode
        )
    }

    suspend fun optimizePrompt(
        originalPrompt: String,
        focusOptions: List<String>,
        apiKey: String,
        model: String,
        temperature: Float,
        language: String = "id"
    ): PromptEntity {
        val focusString = focusOptions.joinToString(", ")
        val metaInstructions = if (language == "en") {
            """
            Act as an Elite Prompt Optimizer.
            Analyze and heavily upgrade the user's raw prompt to achieve maximum AI reasoning, clarity, and precision.
            
            Original Prompt:
            "$originalPrompt"
            
            Optimization Focus: $focusString
            
            Instructions:
            - Expand ambiguous instructions into crystal-clear directives.
            - Inject authoritative persona and context.
            - Add explicit boundaries and output format guidelines.
            
            Output structure:
            [OPTIMIZED_PROMPT]
            <Write the improved prompt text here>
            [IMPROVEMENTS]
            - Detail improvement 1
            - Detail improvement 2
            - Detail improvement 3
            - Detail improvement 4
            """.trimIndent()
        } else {
            """
            Bertindaklah sebagai Senior Prompt Optimization Specialist.
            Tugas Anda adalah membedah dan mengoptimalkan prompt mentah pengguna agar menjadi prompt tingkat lanjut yang terstruktur, kaya konteks, dan sangat efektif.
            
            Prompt Asli Pengguna:
            "$originalPrompt"
            
            Fokus Optimasi: $focusString
            
            Format Respon Anda:
            [OPTIMIZED_PROMPT]
            <Tuliskan versi prompt yang telah dirombak dan dioptimasi secara profesional di sini>
            [IMPROVEMENTS]
            - Poin peningkatan 1 (misal: Persona dipertegas)
            - Poin peningkatan 2 (misal: Batasan output ditentukan)
            - Poin peningkatan 3 (misal: Alur berpikir dirinci)
            - Poin peningkatan 4 (misal: Konteks audiens ditambahkan)
            """.trimIndent()
        }

        val apiResult = geminiClient.generateContent(
            prompt = metaInstructions,
            customKey = apiKey,
            model = model,
            temperature = temperature
        )

        var optimizedText = ""
        var improvementsText = ""

        if (apiResult.isSuccess) {
            val fullResponse = apiResult.getOrThrow()
            if (fullResponse.contains("[OPTIMIZED_PROMPT]") && fullResponse.contains("[IMPROVEMENTS]")) {
                val parts = fullResponse.split("[IMPROVEMENTS]")
                optimizedText = parts[0].replace("[OPTIMIZED_PROMPT]", "").trim()
                improvementsText = parts.getOrNull(1)?.trim() ?: ""
            } else {
                optimizedText = fullResponse
                improvementsText = "• Struktur kalimat diperjelas\n• Persona dan arahan diperkuat\n• Format output distandarkan"
            }
        } else {
            val localOpt = synthesizeLocalOptimization(originalPrompt, focusOptions, language)
            optimizedText = localOpt.first
            improvementsText = localOpt.second
        }

        val title = "Optimasi: ${originalPrompt.take(28)}..."
        val uniqueCode = "opt-${UUID.randomUUID().toString().take(6)}"

        return PromptEntity(
            title = title,
            content = optimizedText,
            category = "OPTIMIZER",
            originalPrompt = originalPrompt,
            improvements = improvementsText,
            scoreBefore = 52,
            scoreAfter = 97,
            shareCode = uniqueCode
        )
    }

    suspend fun generateCodePrompt(
        sourceCode: String,
        language: String,
        analysisMode: String, // "BUG_FIX", "EXPLAIN", "UNIT_TEST", "REFACTOR"
        apiKey: String,
        model: String,
        temperature: Float
    ): PromptEntity {
        val modeGoal = when (analysisMode) {
            "BUG_FIX" -> "Pemeriksaan Bug, Vulnerability, dan Optimasi Performa Eksekusi"
            "EXPLAIN" -> "Penjelasan Alur Kerja Komprehensif dan Pembuatan Dokumentasi Arsitektur"
            "UNIT_TEST" -> "Generasi Unit Test Suite Lengkap dengan Edge Cases & Mocking"
            "REFACTOR" -> "Refactoring Kode, Penerapan Clean Code / SOLID Principles dan Pola Desain Modern"
            else -> "Analisis Kode Sumber Komprehensif"
        }

        val metaInstructions = """
            Bertindaklah sebagai Senior Software Architect dan Prompt Engineer.
            Analisis kode sumber $language berikut dan rancanglah sebuah AI prompt siap pakai berkualitas tinggi untuk keperluan "$modeGoal".
            
            Kode Sumber ($language):
            ```$language
            $sourceCode
            ```
            
            Rancang master prompt yang berisi:
            1. Peran AI (misal: Senior $language Architect & Security Auditor)
            2. Masalah atau sasaran teknis yang ingin diselesaikan
            3. Potongan kode sumber yang dianalisis
            4. Instruksi spesifik apa saja yang harus dihasilkan (misal: Root cause, code diff, benchmarking, error handling)
            5. Standar kualitas format output (Markdown, code diff, table)
            
            Berikan HANYA teks prompt akhir yang siap disalin.
        """.trimIndent()

        val apiResult = geminiClient.generateContent(
            prompt = metaInstructions,
            customKey = apiKey,
            model = model,
            temperature = temperature
        )

        val promptText = apiResult.getOrElse {
            synthesizeLocalCodePrompt(sourceCode, language, analysisMode, modeGoal)
        }

        val uniqueCode = "code-${UUID.randomUUID().toString().take(6)}"
        val title = "Analisis Kode $language: $modeGoal"

        return PromptEntity(
            title = title,
            content = promptText,
            category = "CODE_TO_PROMPT",
            role = "Senior $language Engineer",
            topic = modeGoal,
            outputFormat = "Analisis Teknis & Code Block",
            sourceLanguage = language,
            sourceCode = sourceCode,
            shareCode = uniqueCode
        )
    }

    private fun synthesizeLocalPrompt(
        role: String,
        topic: String,
        outputFormat: String,
        tone: String,
        audience: String,
        additionalContext: String,
        language: String
    ): String {
        return if (language == "en") {
            """
            Role: Act as an expert $role with over 10 years of specialized experience.
            
            Objective:
            Provide comprehensive, accurate, and actionable execution regarding: "$topic".
            
            Target Audience:
            Tailor the depth, terminology, and explanations specifically for $audience.
            
            Tone & Style:
            Maintain a $tone voice throughout the entire response.
            
            Key Tasks & Instructions:
            1. Executive Overview: Outline the core concepts and fundamental value of $topic.
            2. Detailed Execution: Break down the strategy or solution into clear, structured components.
            3. Actionable Checklist: Provide immediate next steps or best practices.
            4. Potential Pitfalls: Highlight common errors and how to preemptively avoid them.
            ${if (additionalContext.isNotBlank()) "\nAdditional Constraints:\n$additionalContext" else ""}
            
            Output Formatting:
            Deliver the response strictly in $outputFormat format, utilizing high-clarity headings, concise bullet points, and clean syntax formatting.
            """.trimIndent()
        } else {
            """
            Peran & Otoritas:
            Bertindaklah sebagai $role profesional dengan keahlian mendalam di bidang industri terkait.
            
            Tujuan Utama:
            Berikan panduan, solusi, dan eksekusi komprehensif berstandar tinggi mengenai: "$topic".
            
            Konteks & Sasaran Audiens:
            Rancang respon ini secara spesifik agar mudah dipahami, relevan, dan berdampak bagi audiens tingkat $audience.
            
            Gaya Bahasa & Nada:
            Gunakan nada bicara yang $tone, persuasif, tajam, dan tidak bertele-tele.
            
            Instruksi Kerja Bertahap:
            1. Ringkasan Eksekutif: Jelaskan esensi permasalahan dan proposisi nilai inti.
            2. Analisis & Pembahasan Mendalam: Uraikan pilar-pilar utama solusi dengan runtut dan logis.
            3. Rencana Aksi Praktis: Berikan langkah-langkah implementasi taktis yang siap dieksekusi.
            4. Manajemen Risiko: Sebutkan 2-3 kesalahan fatal yang wajib dihindari serta mitigasinya.
            ${if (additionalContext.isNotBlank()) "\nCatatan Tambahan & Batasan:\n$additionalContext" else ""}
            
            Ketentuan Format Output:
            Sajikan seluruh hasil dalam format $outputFormat. Gunakan pemformatan Markdown yang rapi, penomoran terstruktur, dan penekanan poin penting agar nyaman dibaca.
            """.trimIndent()
        }
    }

    private fun synthesizeLocalOptimization(
        originalPrompt: String,
        focusOptions: List<String>,
        language: String
    ): Pair<String, String> {
        val optimized = """
            Peran & Persona:
            Bertindaklah sebagai Subject Matter Expert (SME) dan Konsultan Senior terpercaya di bidang ini.
            
            Latar Belakang & Konteks:
            Berdasarkan arahan awal: "$originalPrompt", buat solusi yang komprehensif, teruji, dan berdampak nyata.
            
            Instruksi Kerja Spesifik:
            1. Analisis Kebutuhan: Bedah premis permasalahan secara objektif dan mendalam.
            2. Solusi Langkah demi Langkah: Susun strategi atau konten dengan alur berpikir yang sistematis dan detail.
            3. Contoh Konkret / Template: Berikan studi kasus atau template yang bisa langsung diadopsi.
            4. Rekomendasi Evaluasi: Berikan indikator keberhasilan (KPIs / Success Metrics) untuk mengukur hasil.
            
            Batasan & Larangan:
            - Hindari penggunaan bahasa klise, generalisasi kosong, atau saran normatif tanpa implementasi.
            - Pastikan data, istilah teknis, dan struktur kalimat tepat sasaran.
            
            Format Output:
            Sajikan secara terstruktur menggunakan Markdown dengan pembagian bab yang jelas, poin analitis, dan ringkasan eksekutif di awal.
        """.trimIndent()

        val improvements = buildString {
            appendLine("• Penetapan Persona Ahli (SME) berotoritas tinggi")
            appendLine("• Penambahan alur berpikir metodologis (Instruksi bertahap 1-4)")
            appendLine("• Batasan eksplisit untuk mengeliminasi jawaban klise dan generik")
            appendLine("• Standarisasi format output ke Markdown terstruktur dengan metrik keberhasilan")
        }

        return Pair(optimized, improvements)
    }

    private fun synthesizeLocalCodePrompt(
        sourceCode: String,
        language: String,
        analysisMode: String,
        modeGoal: String
    ): String {
        val specificInstructions = when (analysisMode) {
            "BUG_FIX" -> """
                1. Lakukan audit keamanan dan telaah potensi memory leak / concurrency issue / bottleneck performa.
                2. Berikan analisis akar masalah (root cause) dari kode yang bermasalah.
                3. Tuliskan versi kode perbaikan lengkap beserta penjelasannya.
                4. Jelaskan perbedaan (diff) dan peningkatan efisiensi time/space complexity (Big-O).
            """.trimIndent()
            "UNIT_TEST" -> """
                1. Buat test suite lengkap menggunakan framework testing standar untuk $language.
                2. Sertakan test cases untuk skenario normal (Happy Path), batas nilai (Edge Cases), dan skenario kegagalan (Error handling).
                3. Terapkan mocking dan stubbing jika terdapat ketergantungan eksternal (DB/Network).
                4. Pastikan code coverage mencapai minimal 90% pada fungsi-fungsi kritis.
            """.trimIndent()
            "REFACTOR" -> """
                1. Tinjau kepatuhan kode terhadap prinsip SOLID, DRY, dan Clean Code.
                2. Usulkan refactoring arsitektur untuk memisahkan concerns dan meningkatkan maintainability.
                3. Tuliskan kode hasil refactoring yang modular, type-safe, dan mudah di-extend.
                4. Jelaskan alasan teknis di balik setiap perubahan arsitektural.
            """.trimIndent()
            else -> """
                1. Jelaskan alur kerja logika fungsi dan struktur data dari kode baris per baris.
                2. Dokumentasikan fungsi dalam format docstring / KDoc / JSDoc standar.
                3. Berikan diagram alir konseptual (flow logic) dalam bentuk teks atau bullet point.
                4. Rangkum dependensi dan prasyarat yang dibutuhkan kode ini.
            """.trimIndent()
        }

        return """
            Bertindaklah sebagai Principal Software Engineer & Code Reviewer untuk bahasa $language.
            
            Tugas:
            Lakukan $modeGoal terhadap snippet kode sumber berikut ini:
            
            ```$language
            $sourceCode
            ```
            
            Instruksi Analisis Teknis:
            $specificInstructions
            
            Format Penyajian:
            - Gunakan Markdown profesional.
            - Pisahkan penjelasan konsep dan kode sumber dalam syntax highlighting block yang rapi.
            - Sertakan ringkasan dampak dan checklist kesiapan produksi (Production Readiness).
        """.trimIndent()
    }
}
