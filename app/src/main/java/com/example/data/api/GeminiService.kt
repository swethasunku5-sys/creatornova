package com.example.data.api

import android.content.Context
import android.speech.tts.TextToSpeech
import com.example.BuildConfig
import com.example.data.model.CampaignBrief
import com.example.data.model.ChatMessage
import com.example.data.model.MessageSender
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale
import java.util.concurrent.TimeUnit

class GeminiService(private val context: Context) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private var tts: TextToSpeech? = null
    private var isTtsInitialized = false

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.ENGLISH
                isTtsInitialized = true
            }
        }
    }

    fun speak(text: String) {
        if (isTtsInitialized && tts != null) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "NovaTTS_${System.currentTimeMillis()}")
        }
    }

    fun stopSpeaking() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }

    suspend fun generateBrief(promptIdea: String): Pair<CampaignBrief, Boolean> = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
                val systemPrompt = """
                    You are an expert AI advertising strategist for CreatorNova.
                    Analyze this campaign idea and generate a structured JSON campaign brief with these exact keys:
                    title, brandName, objective, targetAudience, contentType, creativeStyle, platform, aspectRatio, preferredAiTools, deliverables, budgetInr (as number), deadline, commercialTerms, additionalInstructions, successCriteria.
                    Budget must be realistic in Indian Rupees (₹ INR), typically between 40000 and 150000.
                    Output ONLY valid JSON.
                """.trimIndent()

                val requestJson = JSONObject().apply {
                    put("contents", JSONArray().apply {
                        put(JSONObject().apply {
                            put("role", "user")
                            put("parts", JSONArray().apply {
                                put(JSONObject().put("text", "Generate structured campaign brief for: $promptIdea"))
                            })
                        })
                    })
                    put("systemInstruction", JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", systemPrompt))
                        })
                    })
                    put("generationConfig", JSONObject().apply {
                        put("responseMimeType", "application/json")
                        put("temperature", 0.7)
                    })
                }

                val mediaType = "application/json; charset=utf-8".toMediaType()
                val body = requestJson.toString().toRequestBody(mediaType)
                val request = Request.Builder().url(url).post(body).build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val respStr = response.body?.string().orEmpty()
                    val jsonResp = JSONObject(respStr)
                    val candidates = jsonResp.optJSONArray("candidates")
                    val contentObj = candidates?.optJSONObject(0)?.optJSONObject("content")
                    val parts = contentObj?.optJSONArray("parts")
                    val text = parts?.optJSONObject(0)?.optString("text").orEmpty()

                    if (text.isNotBlank()) {
                        val parsed = JSONObject(text)
                        val brief = CampaignBrief(
                            id = "gen_${System.currentTimeMillis()}",
                            title = parsed.optString("title", "AI Creator Campaign"),
                            brandName = parsed.optString("brandName", "Brand Sponsor"),
                            objective = parsed.optString("objective", promptIdea),
                            targetAudience = parsed.optString("targetAudience", "Urban Indian Youth (18-28)"),
                            contentType = parsed.optString("contentType", "AI Video Reels & Posters"),
                            creativeStyle = parsed.optString("creativeStyle", "Cinematic, High-energy"),
                            platform = parsed.optString("platform", "Instagram & YouTube Shorts"),
                            aspectRatio = parsed.optString("aspectRatio", "9:16 (Vertical)"),
                            preferredAiTools = parsed.optString("preferredAiTools", "Runway Gen-3, Midjourney v6"),
                            deliverables = parsed.optString("deliverables", "3x Reels, 2x Static Posters"),
                            budgetInr = parsed.optLong("budgetInr", 65000L),
                            deadline = parsed.optString("deadline", "2026-11-15"),
                            commercialTerms = parsed.optString("commercialTerms", "Full Digital Rights (12 Months)"),
                            additionalInstructions = parsed.optString("additionalInstructions", "Incorporate local cultural nuances and high visual fidelity."),
                            successCriteria = parsed.optString("successCriteria", "100k+ impressions with 6%+ engagement rate")
                        )
                        return@withContext Pair(brief, false) // false = not local demo fallback
                    }
                }
            } catch (e: Exception) {
                // fall through to local generator
            }
        }

        // Deterministic, high-quality local generator fallback
        val localBrief = generateLocalBrief(promptIdea)
        Pair(localBrief, true) // true = local demo fallback
    }

    private fun generateLocalBrief(idea: String): CampaignBrief {
        val lower = idea.lowercase()
        val isEco = lower.contains("sustainab") || lower.contains("eco") || lower.contains("green") || lower.contains("waste") || lower.contains("climate")
        val isFashion = lower.contains("fashion") || lower.contains("apparel") || lower.contains("cloth") || lower.contains("wear")
        val isFestive = lower.contains("festiv") || lower.contains("diwali") || lower.contains("holi") || lower.contains("celebrat")

        val title = when {
            isEco -> "EcoPulse India: Next-Gen Sustainability Campaign"
            isFashion -> "VogueVerse: AI Fashion & Digital Weaves Showcase"
            isFestive -> "Utsav AI: Festive Spectacle for Modern India"
            else -> "PulseLaunch: AI-Powered Brand Movement"
        }

        val brandName = when {
            isEco -> "Prakriti Essentials"
            isFashion -> "Aura Apparel Lab"
            isFestive -> "Swarna Heritage"
            else -> "Nova Innovations"
        }

        val objective = if (idea.isNotBlank()) {
            idea
        } else {
            "Drive viral cultural awareness and customer engagement across Indian youth using bleeding-edge generative AI storytelling."
        }

        val targetAudience = when {
            isEco -> "Eco-conscious urban college students and young professionals across Bengaluru, Mumbai, Pune, and Delhi (ages 18-30)"
            isFashion -> "Style-first Gen-Z & millennials interested in digital fashion, streetwear, and indie labels (ages 18-28)"
            else -> "Digital natives, creators, and modern consumers across metropolitan India (ages 18-35)"
        }

        val contentType = when {
            isEco -> "Speculative Documentary Reels & Kinetic Infographics"
            isFashion -> "Hyperrealistic Virtual Lookbooks & 3D Runway Clips"
            else -> "High-tempo AI Video Reels, Banners & Audio Hooks"
        }

        val creativeStyle = when {
            isEco -> "Organic textures, solar optimism, deep greens & warm daylight"
            isFashion -> "Cyber-couture, metallic luminescence, slow-motion fabric dynamics"
            else -> "Vibrant electric neon, sharp camera motions, bold Indian typography"
        }

        val tools = when {
            isEco -> "Runway Gen-3, Midjourney v6, Claude 3.5, TouchDesigner"
            isFashion -> "Flux.1 Dev, Kling 1.5, Magnific AI, Blender Octane"
            else -> "Runway Gen-3, Midjourney v6, Kling 1.5, ElevenLabs"
        }

        val budget = when {
            isEco -> 75000L
            isFashion -> 95000L
            isFestive -> 85000L
            else -> 60000L
        }

        return CampaignBrief(
            id = "gen_local_${System.currentTimeMillis()}",
            title = title,
            brandName = brandName,
            objective = objective,
            targetAudience = targetAudience,
            contentType = contentType,
            creativeStyle = creativeStyle,
            platform = "Instagram Reels & YouTube Shorts",
            aspectRatio = "9:16 (Vertical)",
            preferredAiTools = tools,
            deliverables = "3x Short-form Reels (30s each), 2x High-resolution Static Poster Carousels",
            budgetInr = budget,
            deadline = "2026-11-20",
            commercialTerms = "Full Digital Commercial Rights with Creator Attribution",
            additionalInstructions = "Emphasize optimistic future-forward messaging. Avoid repetitive AI visual artifacts and verify typography accuracy.",
            successCriteria = "Reach 120,000+ views with minimum 6.5% engagement rate across platforms"
        )
    }

    suspend fun chatWithNova(
        history: List<ChatMessage>,
        userPrompt: String
    ): Pair<String, Boolean> = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
                val systemPrompt = """
                    You are Nova AI, the floating intelligence of CreatorNova (ByteXL × Kampus.VC Hackathon).
                    Tagline: Create. Connect. Collaborate.
                    You assist brands, agencies, and creators on our AI Content Creator Marketplace.
                    Indian Rupee (₹ INR) is our primary currency.
                    Key creators: Aanya Sharma (AI Filmmaker, Mumbai), Arjun Reddy (Gen Video, Hyderabad), Meera Nair (Motion Design, Kochi), Rohan Verma (AI Ads, Bengaluru), Kavya Iyer (Brand Design, Chennai), Ishaan Patel (AI Animator, Ahmedabad), Ananya Rao (Sustainability, Bengaluru), Siddharth Menon (Product Viz, Pune), Diya Kulkarni (Social Strategy, Delhi), Neel Joshi (Generative Art, Mumbai).
                    Key unique features:
                    1. AI Brief Builder (turns rough ideas into structured briefs).
                    2. Engagement Tracker (Discovered -> Shortlisted -> Invited -> Accepted -> In Progress -> Review -> Delivered -> Completed).
                    3. Verification Center (Transparent demo review indicators: Tools, Workflow, Portfolio, Licensing).
                    4. Nova EcoMatch (Sustainability impact predictor calculating impressions, CPM in ₹, and audience alignment).
                    Keep answers snappy, professional, actionable, and encouraging!
                """.trimIndent()

                val contentsArray = JSONArray()
                // Take up to last 4 conversation turns to respect context
                val recentHistory = history.takeLast(6)
                for (msg in recentHistory) {
                    val role = if (msg.sender == MessageSender.USER) "user" else "model"
                    contentsArray.put(JSONObject().apply {
                        put("role", role)
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", msg.content))
                        })
                    })
                }
                // Add current prompt
                contentsArray.put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", userPrompt))
                    })
                })

                val requestJson = JSONObject().apply {
                    put("contents", contentsArray)
                    put("systemInstruction", JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", systemPrompt))
                        })
                    })
                    put("generationConfig", JSONObject().apply {
                        put("temperature", 0.7)
                    })
                }

                val mediaType = "application/json; charset=utf-8".toMediaType()
                val body = requestJson.toString().toRequestBody(mediaType)
                val request = Request.Builder().url(url).post(body).build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val respStr = response.body?.string().orEmpty()
                    val jsonResp = JSONObject(respStr)
                    val candidates = jsonResp.optJSONArray("candidates")
                    val contentObj = candidates?.optJSONObject(0)?.optJSONObject("content")
                    val parts = contentObj?.optJSONArray("parts")
                    val text = parts?.optJSONObject(0)?.optString("text").orEmpty()
                    if (text.isNotBlank()) {
                        return@withContext Pair(text.trim(), false)
                    }
                }
            } catch (e: Exception) {
                // fallback to local rule-based assistant
            }
        }

        // Local intelligent rule-based knowledge engine
        val localReply = answerWithLocalEngine(userPrompt)
        Pair(localReply, true)
    }

    private fun answerWithLocalEngine(query: String): String {
        val q = query.lowercase()
        return when {
            q.contains("ecomatch") || q.contains("sustainab") || q.contains("impact") || q.contains("eco") -> {
                "🌿 **Nova EcoMatch — Impact Predictor** helps brands evaluate and match with AI creators for environmental campaigns. It calculates:\n" +
                        "1. **Estimated Reach**: Expected impressions = views per item × planned items.\n" +
                        "2. **Estimated CPM**: (Allocated Cost / Impressions) × 1,000 (in ₹ INR).\n" +
                        "3. **Sustainability Fit Score (0-100)**: Evaluates topic alignment, green credentials, and audience engagement.\n" +
                        "4. **Budget Utilization**: Real-time allocation vs campaign cap.\n\n" +
                        "Top green creator recommendation: **Ananya Rao** (98% EcoFit, Bengaluru) & **Meera Nair** (91% EcoFit, Kochi)."
            }
            q.contains("creator") || q.contains("recommend") || q.contains("who") || q.contains("find") -> {
                "⭐ **Top Indian AI Creators on CreatorNova**:\n" +
                        "• **Aanya Sharma** (Mumbai) — Cinematic AI Filmmaker (₹65,000 avg, Runway Gen-3)\n" +
                        "• **Arjun Reddy** (Hyderabad) — Generative Video & Hyperlapse (₹45,000 avg, Kling 1.5)\n" +
                        "• **Rohan Verma** (Bengaluru) — AI Advertising Strategist (₹75,000 avg, Flux.1 & Claude)\n" +
                        "• **Ananya Rao** (Bengaluru) — Sustainability Content Specialist (₹55,000 avg, 98% EcoFit)\n" +
                        "• **Siddharth Menon** (Pune) — 3D Product Visualization (₹60,000 avg, Octane + CAD)\n\n" +
                        "Tap **Discover Creators** in navigation to filter by budget, tool, or specialization!"
            }
            q.contains("brief") || q.contains("generate") || q.contains("builder") -> {
                "💡 You can use our **AI Brief Builder** to transform a simple prompt into a production-grade campaign brief! Head to the AI Brief Builder tab, enter an idea (e.g., 'Eco college campus reel'), and CreatorNova will structure deliverables, suggested AI tools, target demographics, and budgets in ₹ INR."
            }
            q.contains("verification") || q.contains("verified") || q.contains("trust") -> {
                "🛡️ **Creator Verification Center**: We evaluate creators across 5 transparent demo pillars:\n" +
                        "1. AI Tools Proficiency (Gen-3, Kling, Flux licenses)\n" +
                        "2. Workflow Authenticity (Screen recordings & project files)\n" +
                        "3. Portfolio Originality (Zero plagiarism/copyright checks)\n" +
                        "4. Past Client Work records\n" +
                        "5. Commercial Licensing compliance.\n\n" +
                        "*Disclosure: These are internal simulated review indicators for the CreatorNova demo.*"
            }
            q.contains("lifecycle") || q.contains("engagement") || q.contains("track") -> {
                "🔄 **Campaign Engagement Lifecycle**:\n" +
                        "**Discovered → Shortlisted → Invited → Accepted → In Progress → Review → Delivered → Completed**\n\n" +
                        "Every engagement connects a verified creator with a campaign brief, tracking deliverables, milestone notes, client feedback, and approval sign-off."
            }
            q.contains("hackathon") || q.contains("bytexl") || q.contains("kampus") || q.contains("rubric") -> {
                "🏆 **ByteXL × Kampus.VC Hackathon Alignment**:\n" +
                        "• **Innovation**: AI Brief Builder + Nova EcoMatch sustainability predictor.\n" +
                        "• **UX/UI Excellence**: Material 3 dark/light deep-navy theme with intuitive navigation.\n" +
                        "• **Technical Execution**: Reactive architecture, Kotlin Coroutines, Gemini REST integration.\n" +
                        "• **Commercial Viability**: Transparent marketplace fee & milestone escrow mechanics in ₹ INR."
            }
            q.contains("price") || q.contains("cost") || q.contains("rate") || q.contains("inr") || q.contains("budget") -> {
                "💰 **CreatorNova Pricing in ₹ INR**:\n" +
                        "Creator rates range from **₹20,000 to ₹1,50,000+** depending on deliverables, turnaround, and AI compute intensity. All engagements support transparent escrow milestones."
            }
            else -> {
                "Hello! I am **Nova AI**, your guide across CreatorNova. I can help you discover top AI creators in India, draft campaign briefs, track live engagements, or run sustainability predictions with Nova EcoMatch. What would you like to explore?"
            }
        }
    }
}
