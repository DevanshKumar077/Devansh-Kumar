package com.example.ai

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.Course
import com.example.data.model.Faq
import com.example.data.model.InstituteProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class AiResponseResult(
    val replyText: String,
    val isEscalated: Boolean,
    val groundingSource: String,
    val suggestedFollowUp: String? = null
)

object GeminiAiService {
    private const val TAG = "GeminiAiService"
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun answerEnquiry(
        studentQuery: String,
        profile: InstituteProfile,
        courses: List<Course>,
        faqs: List<Faq>,
        studentName: String = "Student"
    ): AiResponseResult = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY

        // Check for immediate human escalation triggers
        val lowerQuery = studentQuery.lowercase()
        val escalationWords = profile.escalationKeywords.split(",").map { it.trim().lowercase() }
        val triggeredKeyword = escalationWords.firstOrNull { it.isNotEmpty() && lowerQuery.contains(it) }

        if (triggeredKeyword != null) {
            return@withContext AiResponseResult(
                replyText = "Namaste $studentName. I understand you're asking about $triggeredKeyword. As an AI admissions assistant, I want to make sure you receive official, personalized assistance. I have prioritized this conversation and notified our Senior Admissions Counsellor (${profile.phone}) to contact you directly.",
                isEscalated = true,
                groundingSource = "Escalation Policy: Keyword [$triggeredKeyword]",
                suggestedFollowUp = "Senior Counsellor Callback requested"
            )
        }

        // Build grounded system prompt
        val knowledgeBaseContext = buildKnowledgeBaseText(profile, courses, faqs)

        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val response = callGeminiApi(apiKey, studentQuery, knowledgeBaseContext, profile.aiEmployeeName)
                if (response != null && response.isNotBlank()) {
                    val isEscalationSuggested = response.lowercase().contains("counsellor will call") ||
                            response.lowercase().contains("escalat") ||
                            response.lowercase().contains("contact our office")

                    return@withContext AiResponseResult(
                        replyText = response.trim(),
                        isEscalated = isEscalationSuggested,
                        groundingSource = "Gemini 3.5 Flash + Knowledge Base"
                    )
                }
            } catch (e: Exception) {
                Log.w(TAG, "Gemini API call failed, falling back to local grounded reasoning", e)
            }
        }

        // Local Grounded RAG Fallback
        return@withContext performLocalGroundedReasoning(studentQuery, profile, courses, faqs, studentName)
    }

    private fun buildKnowledgeBaseText(
        profile: InstituteProfile,
        courses: List<Course>,
        faqs: List<Faq>
    ): String {
        val sb = StringBuilder()
        sb.append("INSTITUTE: ${profile.name} (${profile.instituteType})\n")
        sb.append("LOCATION: ${profile.city}, ${profile.state} | PHONE: ${profile.phone} | EMAIL: ${profile.email}\n")
        sb.append("WORKING HOURS: ${profile.workingHours}\n\n")

        sb.append("OFFICIAL APPROVED COURSES & FEES:\n")
        courses.forEach { c ->
            sb.append("- Course: ${c.name} | Target: ${c.targetExam} | Duration: ${c.duration} | Fees: ${c.fees} | Batches: ${c.batches} | Eligibility: ${c.eligibility} | Details: ${c.description}\n")
        }

        sb.append("\nOFFICIAL APPROVED FAQS & POLICIES:\n")
        faqs.forEach { f ->
            sb.append("Q: ${f.question}\nA: ${f.answer}\n")
        }
        return sb.toString()
    }

    private fun callGeminiApi(
        apiKey: String,
        userMessage: String,
        knowledgeBase: String,
        employeeName: String
    ): String? {
        val url = "$BASE_URL/$MODEL_NAME:generateContent?key=$apiKey"

        val systemInstruction = """
            You are $employeeName, the official digital AI admissions employee for this coaching institute.
            Rules:
            1. Help prospective students and parents understand approved courses, fees, eligibility, batches, and counselling options.
            2. STRICT GROUNDING: Use ONLY the provided verified knowledge base. Never invent course fees, discounts, admission guarantees, or results.
            3. If information is missing or ambiguous, politely offer a human counsellor handoff.
            4. Keep answers concise, helpful, friendly, and professional (Indian coaching institute context).
            5. Always state you are the digital admissions assistant.
            
            KNOWLEDGE BASE:
            $knowledgeBase
        """.trimIndent()

        val jsonBody = JSONObject().apply {
            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().put("text", systemInstruction))
                })
            })
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", userMessage))
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.3)
                put("maxOutputTokens", 600)
            })
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonBody.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                Log.e(TAG, "Gemini API error code: ${response.code}")
                return null
            }
            val bodyString = response.body?.string() ?: return null
            val root = JSONObject(bodyString)
            val candidates = root.optJSONArray("candidates") ?: return null
            if (candidates.length() > 0) {
                val firstCandidate = candidates.getJSONObject(0)
                val content = firstCandidate.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                if (parts != null && parts.length() > 0) {
                    return parts.getJSONObject(0).optString("text")
                }
            }
        }
        return null
    }

    private fun performLocalGroundedReasoning(
        query: String,
        profile: InstituteProfile,
        courses: List<Course>,
        faqs: List<Faq>,
        studentName: String
    ): AiResponseResult {
        val q = query.lowercase()

        // 1. Fee search
        if (q.contains("fee") || q.contains("cost") || q.contains("price") || q.contains("charges")) {
            val matchedCourse = courses.firstOrNull { c ->
                q.contains(c.targetExam.lowercase()) ||
                        q.contains(c.name.lowercase().take(5)) ||
                        c.name.lowercase().split(" ").any { word -> word.length > 3 && q.contains(word) }
            } ?: courses.firstOrNull()

            return if (matchedCourse != null) {
                AiResponseResult(
                    replyText = "Namaste $studentName! For our ${matchedCourse.name} (${matchedCourse.targetExam}), the official fee is ${matchedCourse.fees} for ${matchedCourse.duration}. Batches available: ${matchedCourse.batches}. Would you like to schedule a 1-on-1 counselling session or demo class?",
                    isEscalated = false,
                    groundingSource = "Course Catalog: ${matchedCourse.name}"
                )
            } else {
                val summary = courses.take(3).joinToString("; ") { "${it.name}: ${it.fees}" }
                AiResponseResult(
                    replyText = "Namaste $studentName! Here are our primary course fees: $summary. Which course are you targeting?",
                    isEscalated = false,
                    groundingSource = "Course Catalog Overview"
                )
            }
        }

        // 2. Batch timings / dates
        if (q.contains("batch") || q.contains("timing") || q.contains("time") || q.contains("schedule") || q.contains("start")) {
            val matchedCourse = courses.firstOrNull { c ->
                q.contains(c.targetExam.lowercase()) || q.contains(c.name.lowercase().take(5))
            } ?: courses.firstOrNull()

            val batchInfo = matchedCourse?.batches ?: "Morning (8:30 AM) and Evening (4:30 PM) batches"
            return AiResponseResult(
                replyText = "Our upcoming batches for ${matchedCourse?.name ?: "flagship courses"} are: $batchInfo. We maintain small batch sizes for focused student doubt-clearing.",
                isEscalated = false,
                groundingSource = "Batch Schedule: ${matchedCourse?.name}"
            )
        }

        // 3. Eligibility / syllabus
        if (q.contains("eligib") || q.contains("10th") || q.contains("11th") || q.contains("12th") || q.contains("dropper") || q.contains("criteria")) {
            val matchedCourse = courses.firstOrNull { c ->
                c.eligibility.lowercase().split(" ").any { word -> word.length > 4 && q.contains(word) }
            } ?: courses.firstOrNull()

            return AiResponseResult(
                replyText = "For ${matchedCourse?.name ?: "our programs"}, the eligibility is: ${matchedCourse?.eligibility ?: "Students preparing for JEE/NEET/Board exams"}. What is your current class/grade?",
                isEscalated = false,
                groundingSource = "Eligibility Criteria: ${matchedCourse?.name}"
            )
        }

        // 4. Check FAQs
        val matchedFaq = faqs.firstOrNull { f ->
            val words = f.question.lowercase().split(" ").filter { it.length > 3 }
            words.count { q.contains(it) } >= 2 || q.contains(f.category.lowercase())
        }

        if (matchedFaq != null) {
            return AiResponseResult(
                replyText = "${matchedFaq.answer}\n\nFeel free to ask if you have more questions about admissions!",
                isEscalated = false,
                groundingSource = "FAQ [${matchedFaq.category}]: ${matchedFaq.question}"
            )
        }

        // 5. Counselling / Appointment request
        if (q.contains("counsel") || q.contains("appointment") || q.contains("visit") || q.contains("demo") || q.contains("meet")) {
            return AiResponseResult(
                replyText = "I would be glad to book a free 1-on-1 admissions counselling session for you at our ${profile.city} campus or via Google Meet. Would you prefer morning or afternoon tomorrow?",
                isEscalated = false,
                groundingSource = "Counselling Appointment Flow",
                suggestedFollowUp = "Counselling Slot Booking"
            )
        }

        // Default polite grounded response
        return AiResponseResult(
            replyText = "Namaste $studentName! I am ${profile.aiEmployeeName}, your digital admissions assistant at ${profile.name}. We offer specialized coaching for ${courses.take(3).joinToString(", ") { it.targetExam }}. How can I assist your exam preparation today?",
            isEscalated = false,
            groundingSource = "Institute Profile & Welcome"
        )
    }

    // Lead Scoring Engine (Explainable 0-100)
    fun calculateLeadScore(
        courseInterest: String,
        priority: String,
        status: String,
        hasPhone: Boolean,
        hasEmail: Boolean,
        hasAppointments: Boolean
    ): Pair<Int, String> {
        var score = 30
        val reasons = mutableListOf<String>()
        reasons.add("Base enquiry (+30)")

        if (courseInterest.isNotBlank()) {
            score += 20
            reasons.add("Target course identified (+20)")
        }

        if (priority == "HIGH") {
            score += 20
            reasons.add("High priority interest (+20)")
        } else if (priority == "MEDIUM") {
            score += 10
            reasons.add("Medium priority (+10)")
        }

        if (hasPhone) {
            score += 15
            reasons.add("Verified contact phone (+15)")
        }

        if (hasAppointments || status == "COUNSELLING_SCHEDULED") {
            score += 15
            reasons.add("Counselling booked (+15)")
        }

        if (status == "QUALIFIED") {
            score += 10
            reasons.add("Pre-qualified by AI (+10)")
        }

        val finalScore = score.coerceIn(10, 100)
        return Pair(finalScore, reasons.joinToString(" • "))
    }
}
