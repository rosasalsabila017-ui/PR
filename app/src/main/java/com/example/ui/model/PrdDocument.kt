package com.example.ui.model

data class PrdSection(
    val title: String,
    val summary: String,
    val content: String
)

object PrdDocument {
    val title = "Product Requirement Document (PRD): Prompt Cepat AI"
    val version = "v1.0.0 (Production Architecture)"

    val sections = listOf(
        PrdSection(
            title = "1. Executive Summary & Visi Produk",
            summary = "Platform AI Prompt Engineering komprehensif untuk developer, marketer, dan profesional.",
            content = """
                ### 1.1 Visi Produk
                Prompt Cepat AI adalah platform cerdas satu atap untuk merancang, mengoptimalkan, dan mengelola prompt Artificial Intelligence (AI) berdaya guna tinggi. Platform ini dirancang untuk mengatasi fenomena "garbage in, garbage out" pada Large Language Models (LLM) dengan mengubah ide kasar atau kode mentah pengguna menjadi master prompt berstandar industri.

                ### 1.2 Target Pengguna
                1. Software Engineers & Tech Leads (Code-to-prompt, debugging, refactoring, test suites).
                2. Marketers & Copywriters (Sales funnel, landing pages, social media hooks).
                3. Business Analysts & Product Managers (PRD generator, competitor audits, SWOT analysis).
                4. Content Creators & Researchers (Ideation, scholarly prompts, structured workflows).

                ### 1.3 Metrik Keberhasilan (KPIs)
                - Prompt Clarity & Effectiveness Improvement Rate: Rata-rata peningkatan skor > 40%.
                - Time-to-Prompt: Mengurangi waktu penyusunan prompt dari 15 menit menjadi < 20 detik.
                - Prompt Reuse & Retention: > 60% pengguna menyimpan dan mengelompokkan prompt favorit.
            """.trimIndent()
        ),
        PrdSection(
            title = "2. User Flow & Arsitektur Modul",
            summary = "Alur perjalanan pengguna mulai dari input parameter, perbaikan, hingga ekspor.",
            content = """
                ### 2.1 Alur Pengguna (User Journey)
                1. **Alur Generator Parameter:**
                   User memilih Role -> Menentukan Topik & Konteks -> Memilih Format Output & Tone -> Klik "Hasilkan Master Prompt" -> Sistem menyusun prompt via Gemini LLM -> User menyalin/menyimpan/membagikan.
                2. **Alur Perbaikan Prompt (Optimizer):**
                   User mengetik prompt mentah -> Memilih fokus optimasi (Konteks, Batasan, Few-shot) -> Sistem menampilkan komparasi Before & After, daftar peningkatan, dan skor efektivitas (misal 52 -> 97).
                3. **Alur Code-to-Prompt:**
                   User menempelkan cuplikan kode -> Memilih bahasa pemrograman & sasaran (Bug Fix, Explain, Unit Test, Refactor) -> Sistem mengekstraksi konteks teknis dan merangkum prompt instruksi mendalam.
                4. **Alur Manajemen Riwayat & Berbagi:**
                   Pencarian realtime -> Filter kategori & favorit -> Ekspor ke Markdown/Teks -> Generate tautan publik.
            """.trimIndent()
        ),
        PrdSection(
            title = "3. Functional Requirements (FR)",
            summary = "Daftar kebutuhan fungsional mendalam untuk setiap modul aplikasi.",
            content = """
                ### FR-1: Modul Generator Prompt
                - **FR-1.1:** Menyediakan seleksi preset role populer serta opsi custom persona.
                - **FR-1.2:** Mendukung 8+ format output terstruktur (Markdown, Step-by-step, Table, JSON, Code).
                - **FR-1.3:** Mendukung parameter nada (Tone), tingkat kerumitan audiens, dan instruksi tambahan.
                - **FR-1.4:** Mengintegrasikan model Gemini 3.5 Flash / 3.1 Pro Preview dengan fallback cerdas.

                ### FR-2: Modul Perbaikan Prompt (Optimizer)
                - **FR-2.1:** Komparasi visual berdampingan antara Prompt Asli dan Prompt Teroptimasi.
                - **FR-2.2:** Tampilan metrik Skor Efektivitas & Kejelasan (sebelum vs sesudah).
                - **FR-2.3:** Rincian bullet points peningkatan yang dilakukan oleh AI (Persona, Constraints, Structure).

                ### FR-3: Modul Prompt dari Source Code
                - **FR-3.1:** Input editor kode dengan deteksi/pilihan bahasa (Kotlin, Python, SQL, JS/TS, Java, Go).
                - **FR-3.2:** Empat mode analisis teknis: Bug Fix & Audit, Penjelasan & Dokumentasi, Unit Test Generator, Refactoring.
                - **FR-3.3:** Output mencakup snippet terformat dan batasan arsitektur (SOLID, DRY).

                ### FR-4: Riwayat, Favorit, Simpan & Bagikan
                - **FR-4.1:** Penyimpanan lokal reaktif berbasis Room Database (offline-first).
                - **FR-4.2:** Filter status favorit, pencarian kata kunci multi-field secara instan.
                - **FR-4.3:** Fitur 1-tap Copy, Android System Share Intent, dan simulasi tautan publik unik.

                ### FR-5: Autentikasi, Profil & Pengaturan
                - **FR-5.1:** State management akun (Daftar, Masuk, Lupa Password, Status Langganan Pro).
                - **FR-5.2:** Pengaturan preferensi: Bahasa prompt default (ID/EN), Mode Tema (Dark/Light/System).
                - **FR-5.3:** Opsi kustomisasi model Gemini dan slider kreativitas (Temperature 0.0 - 1.0).
            """.trimIndent()
        ),
        PrdSection(
            title = "4. Database Schema (ERD Relasional)",
            summary = "Desain struktur tabel database untuk implementasi SQLite/PostgreSQL/Supabase.",
            content = """
                ```sql
                -- 1. Tabel Users
                CREATE TABLE users (
                    id VARCHAR(36) PRIMARY KEY,
                    email VARCHAR(255) UNIQUE NOT NULL,
                    name VARCHAR(100) NOT NULL,
                    avatar_url TEXT,
                    tier VARCHAR(20) DEFAULT 'FREE', -- 'FREE', 'PRO', 'ENTERPRISE'
                    prompt_language VARCHAR(5) DEFAULT 'id',
                    theme_mode VARCHAR(10) DEFAULT 'SYSTEM',
                    preferred_model VARCHAR(50) DEFAULT 'gemini-3.5-flash',
                    temperature NUMERIC(3,2) DEFAULT 0.70,
                    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
                    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
                );

                -- 2. Tabel Prompts
                CREATE TABLE prompts (
                    id BIGSERIAL PRIMARY KEY,
                    user_id VARCHAR(36) REFERENCES users(id) ON DELETE CASCADE,
                    title VARCHAR(200) NOT NULL,
                    content TEXT NOT NULL,
                    category VARCHAR(30) NOT NULL, -- 'GENERATOR', 'OPTIMIZER', 'CODE_TO_PROMPT'
                    role VARCHAR(100),
                    topic TEXT,
                    output_format VARCHAR(50),
                    tone VARCHAR(50),
                    original_prompt TEXT,
                    improvements TEXT,
                    score_before INT DEFAULT 0,
                    score_after INT DEFAULT 0,
                    source_language VARCHAR(30),
                    source_code TEXT,
                    is_favorite BOOLEAN DEFAULT FALSE,
                    is_public BOOLEAN DEFAULT FALSE,
                    share_code VARCHAR(50) UNIQUE,
                    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
                    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
                );

                -- 3. Tabel Indeks Performa
                CREATE INDEX idx_prompts_user_id ON prompts(user_id);
                CREATE INDEX idx_prompts_category ON prompts(category);
                CREATE INDEX idx_prompts_is_favorite ON prompts(is_favorite);
                CREATE INDEX idx_prompts_created_at ON prompts(created_at DESC);
                ```
            """.trimIndent()
        ),
        PrdSection(
            title = "5. API Endpoints & Kontrak Data",
            summary = "Spesifikasi RESTful API untuk integrasi backend scalable.",
            content = """
                ### 5.1 Endpoint Prompt AI
                - `POST /api/v1/prompts/generate`
                  - Request: `{ role, topic, outputFormat, tone, audience, additionalContext, model, temperature }`
                  - Response: `{ status: "success", prompt: { id, title, content, shareCode } }`
                
                - `POST /api/v1/prompts/optimize`
                  - Request: `{ originalPrompt, focusOptions: [], language }`
                  - Response: `{ status: "success", optimizedPrompt, improvements: [], scoreBefore: 52, scoreAfter: 97 }`
                
                - `POST /api/v1/prompts/code-to-prompt`
                  - Request: `{ sourceCode, language, analysisMode }`
                  - Response: `{ status: "success", promptText, meta: { taskType, lineCount } }`

                ### 5.2 Endpoint Riwayat & Sinkronisasi
                - `GET /api/v1/prompts?category=&favorite=&query=&page=1&limit=20`
                - `POST /api/v1/prompts/:id/favorite` -> Toggle bookmark
                - `DELETE /api/v1/prompts/:id` -> Hapus riwayat
                - `GET /api/v1/share/:shareCode` -> Akses publik prompt
            """.trimIndent()
        ),
        PrdSection(
            title = "6. Usulan Stack Teknologi Skalabel",
            summary = "Rekomendasi stack modern full-stack multi-platform berkinerja tinggi.",
            content = """
                ### 6.1 Client Native Android:
                - **UI:** Jetpack Compose (Declarative, Material 3 Design).
                - **Database:** Android Room Database (KSP, reactive Flow).
                - **Network:** OkHttp + Retrofit (Coroutine async, 60s timeout).
                - **Architecture:** Clean Architecture + MVVM (Unidirectional Data Flow).

                ### 6.2 Web Frontend & Backend Scalable:
                - **Web Frontend:** Next.js 15 (App Router, Server Components), Tailwind CSS, Lucide Icons.
                - **Backend & Auth:** Supabase / Firebase Auth (JWT, Row Level Security, Edge Functions).
                - **Cache & Rate Limiter:** Upstash Redis (Rate limiting per user tier).
                - **AI Engine:** Google Gemini 3.5 Flash & Gemini 3.1 Pro via Google GenAI SDK.
            """.trimIndent()
        )
    )
}
