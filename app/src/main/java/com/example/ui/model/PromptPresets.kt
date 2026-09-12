package com.example.ui.model

object PromptPresets {
    val roles = listOf(
        "Software Architect",
        "Senior Copywriter",
        "Digital Marketing Strategist",
        "Data Scientist & AI Researcher",
        "Dosen & Edukator Spesialis",
        "Product Manager",
        "Konsultan Bisnis & Startup",
        "Content Creator & SEO Specialist",
        "UX Writer & Desainer Interaksi",
        "Customer Success Specialist",
        "Custom (Tulis Sendiri)"
    )

    val outputFormats = listOf(
        "Markdown Terstruktur",
        "Langkah demi Langkah (Step-by-Step)",
        "Daftar Poin (Bullet Points)",
        "Tabel Komparasi / Matriks",
        "Cuplikan Kode (Code Block)",
        "Format JSON / Schema",
        "Format Email Profesional",
        "Executive Summary (1 Halaman)"
    )

    val tones = listOf(
        "Profesional & Berwibawa",
        "Persuasif & Menjual",
        "Kreatif & Menginspirasi",
        "Ringkas & To-the-point",
        "Santai & Akrab (Casual)",
        "Akademis & Metodologis",
        "Humoris & Dinamis"
    )

    val audiences = listOf(
        "Pemula (Awam)",
        "Menengah (Praktisi)",
        "Ahli / Level Eksekutif (C-Level)"
    )

    val optimizationFocus = listOf(
        "Persona & Otoritas Ahli",
        "Konteks & Latar Belakang",
        "Batasan & Larangan Eksplisit",
        "Struktur & Alur Berpikir",
        "Format Output & Metrik Keberhasilan",
        "Contoh / Few-shot Guidance"
    )

    val programmingLanguages = listOf(
        "Kotlin",
        "Python",
        "JavaScript / TypeScript",
        "SQL",
        "Java",
        "Go",
        "PHP",
        "C++ / Rust",
        "HTML & CSS"
    )

    val sampleCodeSnippets = mapOf(
        "Kotlin" to """
fun processPayment(orderId: String, amount: Double): Boolean {
    val order = orderRepository.findById(orderId) ?: return false
    if (order.status != "PENDING") {
        return false
    }
    val result = paymentGateway.charge(amount)
    if (result.isSuccess) {
        order.status = "PAID"
        orderRepository.save(order)
        notificationService.sendReceipt(order.userId, amount)
        return true
    }
    return false
}
        """.trimIndent(),
        "Python" to """
def analyze_customer_churn(df):
    features = ['tenure', 'monthly_charges', 'total_charges', 'contract_type']
    X = df[features]
    y = df['churn']
    model = RandomForestClassifier(n_estimators=100)
    model.fit(X, y)
    importances = model.feature_importances_
    return dict(zip(features, importances))
        """.trimIndent(),
        "SQL" to """
SELECT u.id, u.name, COUNT(o.id) as total_orders, SUM(o.total_amount) as spent
FROM users u
LEFT JOIN orders o ON u.id = o.user_id
WHERE o.status = 'COMPLETED' AND o.created_at >= '2026-01-01'
GROUP BY u.id, u.name
HAVING SUM(o.total_amount) > 1000000
ORDER BY spent DESC
LIMIT 50;
        """.trimIndent(),
        "JavaScript / TypeScript" to """
export async function fetchUserData(userId: string) {
  const cacheKey = `user_${'$'}{userId}`;
  const cached = await redis.get(cacheKey);
  if (cached) return JSON.parse(cached);

  const res = await fetch(`/api/users/${'$'}{userId}`);
  const data = await res.json();
  await redis.set(cacheKey, JSON.stringify(data), 'EX', 3600);
  return data;
}
        """.trimIndent()
    )
}
