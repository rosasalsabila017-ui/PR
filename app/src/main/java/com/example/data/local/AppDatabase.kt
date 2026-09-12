package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [PromptEntity::class, UserEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun promptDao(): PromptDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "prompt_cepat_db"
                ).addCallback(DatabaseCallback(scope)).build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.promptDao(), database.userDao())
                    }
                }
            }

            suspend fun populateInitialData(promptDao: PromptDao, userDao: UserDao) {
                userDao.insertOrUpdateUser(
                    UserEntity(
                        id = "primary_user",
                        name = "Pengguna Kreatif",
                        email = "user@promptcepat.ai",
                        isPro = true,
                        promptLanguage = "id",
                        themeMode = "SYSTEM",
                        preferredModel = "gemini-3.5-flash",
                        temperature = 0.7f,
                        totalPromptsGenerated = 3,
                        isLoggedIn = true
                    )
                )

                promptDao.insertPrompt(
                    PromptEntity(
                        title = "Copywriting Landing Page SaaS",
                        content = "Bertindaklah sebagai Senior Conversion Copywriter berpengalaman 10+ tahun. Buat draft landing page bernilai konversi tinggi untuk aplikasi manajemen proyek berbasis AI bernama 'FlowSync'.\n\nStruktur yang dibutuhkan:\n1. Hero Section: Headline tajam, subheadline persuasif, dan 2 variasi CTA.\n2. Problem Agitation: 3 rasa sakit utama project manager konvensional.\n3. Solution & Key Features: 4 pilar solusi terstruktur dengan benefit-driven bullets.\n4. Social Proof: 2 kutipan testimonial realistis beserta metrik peningkatan produktivitas (%).\n5. FAQ: 4 pertanyaan krusial seputar keamanan data dan integrasi API.\n\nGaya bahasa: Profesional, dinamis, persuasif namun elegan tanpa over-hyped buzzwords.",
                        category = "GENERATOR",
                        role = "Senior Copywriter",
                        topic = "Landing Page SaaS Manajemen Proyek",
                        outputFormat = "Markdown Terstruktur",
                        tone = "Persuasif & Profesional",
                        isFavorite = true,
                        shareCode = "saas-landing-99x"
                    )
                )

                promptDao.insertPrompt(
                    PromptEntity(
                        title = "Optimasi Prompt Riset Kompetitor",
                        originalPrompt = "Bikinin analisis kompetitor untuk bisnis kedai kopi kekinian di Jakarta Selatan dong.",
                        content = "Bertindaklah sebagai Konsultan Strategi Bisnis F&B senior. Lakukan audit dan analisis kompetitor komprehensif untuk peluncuran kedai kopi specialty di area Jakarta Selatan.\n\nKonteks Bisnis:\n- Target: Profesional muda, remote worker, dan penikmat kopi artisanal usia 22-35 tahun.\n- Diferensiasi: Biji kopi single origin Nusantara, ruangan hening ramah laptop, dan sistem pemesanan via mini-app.\n\nFormat Output:\n1. Matriks SWOT: 3 pesaing terkuat (Point Coffee, Fore, Kopi Toko Djawa).\n2. Analisis Gap Pasar: Peluang harga dan pengalaman yang belum tergarap.\n3. Rekomendasi USP (Unique Selling Proposition): 3 pilar proposisi nilai unik.\n4. Rencana Aksi 30 Hari: Langkah taktis pra-peluncuran.\n\nBatasan: Hindari saran klise; sertakan estimasi margin dan tren preferensi konsumen lokal 2026.",
                        category = "OPTIMIZER",
                        role = "Konsultan Strategi Bisnis",
                        topic = "Analisis Kompetitor Coffee Shop",
                        outputFormat = "Matriks & Rencana Aksi",
                        tone = "Analitis & Taktis",
                        improvements = "• Ditambahkan persona spesifik konsultan F&B\n• Ditambahkan parameter konteks target demografis dan diferensiasi\n• Output distrukturkan ke 4 bagian taktis (SWOT, Gap Pasar, USP, Timeline)\n• Diberikan batasan eksplisit agar tidak menghasilkan saran generik",
                        scoreBefore = 58,
                        scoreAfter = 96,
                        isFavorite = true,
                        shareCode = "fnb-audit-44k"
                    )
                )

                promptDao.insertPrompt(
                    PromptEntity(
                        title = "Analisis & Optimasi Query SQL",
                        content = "Bertindaklah sebagai Database Administrator (DBA) dan Senior PostgreSQL Architect. Analisis snippet query SQL berikut ini yang mengalami slow query pada tabel transaksi dengan 5 juta baris:\n\n```sql\nSELECT u.id, u.name, COUNT(o.id) as total_orders, SUM(o.total_amount) as spent\nFROM users u\nLEFT JOIN orders o ON u.id = o.user_id\nWHERE o.status = 'COMPLETED' AND o.created_at >= '2026-01-01'\nGROUP BY u.id, u.name\nHAVING SUM(o.total_amount) > 1000000\nORDER BY spent DESC\nLIMIT 50;\n```\n\nTugas Anda:\n1. Jelaskan potensi bottleneck performa (misal: penempatan klausa WHERE pada LEFT JOIN, indeks yang hilang).\n2. Berikan versi query yang telah dioptimasi.\n3. Tuliskan DDL rekomendasi indeks gabungan (composite / partial index) yang wajib dibuat.\n4. Sertakan penjelasan estimasi execution plan (EXPLAIN ANALYZE).",
                        category = "CODE_TO_PROMPT",
                        role = "PostgreSQL DBA",
                        topic = "Optimasi Slow Query Transaksi",
                        outputFormat = "Kode & Penjelasan Teknis",
                        tone = "Teknis & Presisi",
                        sourceLanguage = "SQL",
                        sourceCode = "SELECT u.id, u.name, COUNT(o.id) as total_orders, SUM(o.total_amount) as spent\nFROM users u\nLEFT JOIN orders o ON u.id = o.user_id\nWHERE o.status = 'COMPLETED' AND o.created_at >= '2026-01-01'\nGROUP BY u.id, u.name\nHAVING SUM(o.total_amount) > 1000000\nORDER BY spent DESC\nLIMIT 50;",
                        isFavorite = false,
                        shareCode = "sql-opt-82z"
                    )
                )
            }
        }
    }
}
