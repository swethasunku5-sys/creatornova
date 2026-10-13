package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiService
import com.example.data.datasource.SeedData
import com.example.data.model.*
import com.example.data.repository.CreatorNovaRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class EcoMatchConfig(
    val topic: String = "Zero-Waste Urban Living & Reusable Essentials",
    val targetAudience: String = "Urban college students & Gen-Z (18-26)",
    val totalBudgetInr: Long = 100000L,
    val durationWeeks: Int = 4,
    val plannedItemsPerCreator: Int = 3,
    val assumedActionRate: Double = 0.04, // 4% assumed awareness-action scenario rate
    val selectedCreatorIds: Set<String> = setOf("c7", "c3", "c1")
)

data class EcoCreatorMetric(
    val creator: Creator,
    val plannedItems: Int,
    val estimatedImpressions: Long,
    val allocatedCostInr: Long,
    val estimatedCpmInr: Double?,
    val ecoFitScore: Int,
    val potentialEcoActions: Long,
    val strengths: String,
    val limitations: String,
    val rankExplanation: String
)

data class EcoMatchAnalysis(
    val totalImpressions: Long,
    val totalAllocatedCostInr: Long,
    val remainingBudgetInr: Long,
    val budgetUtilizationPercent: Double,
    val overallCpmInr: Double?,
    val totalPotentialActions: Long,
    val isOverBudget: Boolean,
    val topCreatorMetrics: List<EcoCreatorMetric>
)

enum class ViewportMode(val label: String, val widthDp: Int?) {
    AUTO("Auto", null),
    MOBILE("Mobile (390dp)", 390),
    TABLET("Tablet (768dp)", 768),
    LAPTOP("Laptop (1280dp)", 1280),
    DESKTOP("Desktop (1440dp)", 1440)
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val repository = CreatorNovaRepository()
    val geminiService = GeminiService(application)

    // Viewport preview switcher
    val viewportMode = MutableStateFlow(ViewportMode.AUTO)

    // Current navigation tab
    private val _currentDestination = MutableStateFlow(AppNavDestination.DASHBOARD)
    val currentDestination: StateFlow<AppNavDestination> = _currentDestination.asStateFlow()

    fun navigateTo(dest: AppNavDestination) {
        _currentDestination.value = dest
    }

    // Modal & Selection state
    private val _selectedCreator = MutableStateFlow<Creator?>(null)
    val selectedCreator: StateFlow<Creator?> = _selectedCreator.asStateFlow()

    fun selectCreator(creator: Creator?) {
        _selectedCreator.value = creator
    }

    private val _selectedBrief = MutableStateFlow<CampaignBrief?>(null)
    val selectedBrief: StateFlow<CampaignBrief?> = _selectedBrief.asStateFlow()

    fun selectBrief(brief: CampaignBrief?) {
        _selectedBrief.value = brief
    }

    private val _selectedEngagement = MutableStateFlow<Engagement?>(null)
    val selectedEngagement: StateFlow<Engagement?> = _selectedEngagement.asStateFlow()

    fun selectEngagement(engagement: Engagement?) {
        _selectedEngagement.value = engagement
    }

    // --- Discover Filter State ---
    val searchQuery = MutableStateFlow("")
    val filterSpecialization = MutableStateFlow<String?>(null)
    val filterAiTool = MutableStateFlow<String?>(null)
    val filterLocation = MutableStateFlow<String?>(null)
    val filterVerification = MutableStateFlow<VerificationStatus?>(null)
    val filterMinBudget = MutableStateFlow<Long?>(null)
    val filterMaxBudget = MutableStateFlow<Long?>(null)
    val filterEcoFriendlyOnly = MutableStateFlow(false)
    val sortBy = MutableStateFlow("Relevance") // Relevance, Budget Low-High, Budget High-Low, EcoMatch, Experience

    fun clearAllFilters() {
        searchQuery.value = ""
        filterSpecialization.value = null
        filterAiTool.value = null
        filterLocation.value = null
        filterVerification.value = null
        filterMinBudget.value = null
        filterMaxBudget.value = null
        filterEcoFriendlyOnly.value = false
        sortBy.value = "Relevance"
    }

    val filteredCreators: StateFlow<List<Creator>> = combine(
        repository.creators,
        searchQuery,
        filterSpecialization,
        filterAiTool,
        filterVerification,
        filterMinBudget,
        filterMaxBudget,
        filterEcoFriendlyOnly,
        sortBy
    ) { params ->
        val list = params[0] as List<Creator>
        val query = (params[1] as String).trim().lowercase()
        val spec = params[2] as String?
        val tool = params[3] as String?
        val verif = params[4] as VerificationStatus?
        val minB = params[5] as Long?
        val maxB = params[6] as Long?
        val ecoOnly = params[7] as Boolean
        val sort = params[8] as String

        var result = list.filter { creator ->
            val matchesQuery = query.isEmpty() ||
                    creator.name.lowercase().contains(query) ||
                    creator.headline.lowercase().contains(query) ||
                    creator.bio.lowercase().contains(query) ||
                    creator.location.lowercase().contains(query) ||
                    creator.skills.any { it.lowercase().contains(query) } ||
                    creator.aiTools.any { it.lowercase().contains(query) }

            val matchesSpec = spec == null || creator.specialization == spec
            val matchesTool = tool == null || creator.aiTools.any { it.contains(tool, ignoreCase = true) }
            val matchesVerif = verif == null || creator.verificationStatus == verif
            val matchesMinB = minB == null || creator.indicativeRateInr >= minB
            val matchesMaxB = maxB == null || creator.indicativeRateInr <= maxB
            val matchesEco = !ecoOnly || creator.sustainabilityFitScore >= 75

            matchesQuery && matchesSpec && matchesTool && matchesVerif && matchesMinB && matchesMaxB && matchesEco
        }

        when (sort) {
            "Budget: Low to High" -> result.sortedBy { it.indicativeRateInr }
            "Budget: High to Low" -> result.sortedByDescending { it.indicativeRateInr }
            "EcoMatch Score" -> result.sortedByDescending { it.sustainabilityFitScore }
            "Experience" -> result.sortedByDescending { it.completedCampaigns }
            "Name" -> result.sortedBy { it.name }
            else -> result.sortedByDescending { it.rating }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SeedData.demoCreators)

    // --- AI Brief Builder State ---
    val briefIdeaInput = MutableStateFlow("Create a high-energy short video sustainability campaign for college students in India to stop using plastic bottles and adopt reusable steel flasks.")
    val isGeneratingBrief = MutableStateFlow(false)
    val generatedBrief = MutableStateFlow<CampaignBrief?>(null)
    val isBriefLocalFallback = MutableStateFlow(false)
    val briefNotificationMsg = MutableStateFlow<String?>(null)

    fun generateBriefFromIdea() {
        val idea = briefIdeaInput.value.trim()
        if (idea.isBlank()) return
        viewModelScope.launch {
            isGeneratingBrief.value = true
            try {
                val (brief, isFallback) = geminiService.generateBrief(idea)
                generatedBrief.value = brief
                isBriefLocalFallback.value = isFallback
            } catch (e: Exception) {
                // error fallback
            } finally {
                isGeneratingBrief.value = false
            }
        }
    }

    fun saveGeneratedBrief() {
        val brief = generatedBrief.value ?: return
        repository.addBrief(brief)
        briefNotificationMsg.value = "Brief '${brief.title}' saved to Campaign Briefs!"
        _currentDestination.value = AppNavDestination.BRIEFS
    }

    // --- Nova AI Chat State ---
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                id = "m1",
                sender = MessageSender.NOVA,
                content = "✨ Namaste! I am **Nova AI**, your guide on CreatorNova.\n\nI can help you:\n• Match with top Indian AI video & motion creators\n• Generate campaign briefs with Gemini intelligence\n• Calculate sustainability reach & CPM with **Nova EcoMatch**\n• Track real-time engagement milestones in ₹ INR\n\nWhat would you like to explore today?"
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    val chatInputText = MutableStateFlow("")
    val isChatSending = MutableStateFlow(false)
    val isNovaChatOpen = MutableStateFlow(false)

    fun openNovaChat() {
        isNovaChatOpen.value = true
    }

    fun closeNovaChat() {
        isNovaChatOpen.value = false
        geminiService.stopSpeaking()
    }

    fun sendChatMessage(text: String? = null) {
        val msg = (text ?: chatInputText.value).trim()
        if (msg.isBlank() || isChatSending.value) return

        chatInputText.value = ""
        val userMsg = ChatMessage(
            id = "usr_${System.currentTimeMillis()}",
            sender = MessageSender.USER,
            content = msg
        )
        _chatMessages.update { it + userMsg }

        viewModelScope.launch {
            isChatSending.value = true
            try {
                val (reply, isFallback) = geminiService.chatWithNova(_chatMessages.value, msg)
                val novaMsg = ChatMessage(
                    id = "nova_${System.currentTimeMillis()}",
                    sender = MessageSender.NOVA,
                    content = reply,
                    isDemoFallback = isFallback
                )
                _chatMessages.update { it + novaMsg }
            } catch (e: Exception) {
                val errMsg = ChatMessage(
                    id = "nova_${System.currentTimeMillis()}",
                    sender = MessageSender.NOVA,
                    content = "Nova AI is running in local mode. You can ask about creators, EcoMatch, verification, or briefs anytime!"
                )
                _chatMessages.update { it + errMsg }
            } finally {
                isChatSending.value = false
            }
        }
    }

    fun speakText(text: String) {
        // Strip markdown stars for speech
        val clean = text.replace("**", "").replace("#", "").replace("•", "")
        geminiService.speak(clean)
    }

    // --- Nova EcoMatch Sustainability Predictor State ---
    val ecoConfig = MutableStateFlow(EcoMatchConfig())

    fun updateEcoConfig(update: (EcoMatchConfig) -> EcoMatchConfig) {
        ecoConfig.update(update)
    }

    val ecoMatchAnalysis: StateFlow<EcoMatchAnalysis> = combine(
        repository.creators,
        ecoConfig
    ) { creatorsList, config ->
        val selectedCreators = creatorsList.filter { config.selectedCreatorIds.contains(it.id) }
        var totalImpressions = 0L
        var totalAllocatedCost = 0L

        val metrics = selectedCreators.map { creator ->
            val plannedItems = config.plannedItemsPerCreator
            // Formula A: Estimated impressions = expected views per item * planned content items
            // If avgViews available, use avgViews * plannedItems
            val impressionsPerItem = if (creator.avgViews > 0) creator.avgViews else (creator.followerCount * 0.35).toLong()
            val estImpressions = impressionsPerItem * plannedItems
            val cost = creator.indicativeRateInr

            // Formula B: CPM = (allocated cost / estimated impressions) * 1000
            val cpm = if (estImpressions > 0) (cost.toDouble() / estImpressions.toDouble()) * 1000.0 else null

            // Formula E: Illustrative potential eco-actions = impressions * assumedActionRate
            val potentialActions = (estImpressions * config.assumedActionRate).toLong()

            val strengths = when (creator.specialization) {
                "Sustainability Content" -> "High credibility, dedicated green audience, verified eco-portfolio."
                "Motion Design" -> "Organic aesthetic loops, zero visual fatigue, memorable artistic motifs."
                "AI Filmmaker" -> "Photorealistic worldbuilding, high emotional resonance and cinematic polish."
                "AI Advertising" -> "Data-driven hook testing, aggressive viewer retention, clear call-to-actions."
                else -> "Strong visual craft, high engagement rate, versatile tools pipeline."
            }

            val limitations = when {
                cost > 60000 -> "Higher budget allocation; best suited for cornerstone hero content."
                creator.verificationStatus != VerificationStatus.DEMO_VERIFIED -> "Verification review currently in progress."
                else -> "Audience primarily concentrated in metropolitan hubs."
            }

            val rankExp = when {
                creator.sustainabilityFitScore >= 95 -> "Ranked #1 for flawless sustainability alignment (98/100) & verified climate portfolio."
                creator.sustainabilityFitScore >= 85 -> "High recommendation for organic visual tone and balanced CPM."
                else -> "Strong visual multiplier to expand campaign reach beyond core green enthusiasts."
            }

            totalImpressions += estImpressions
            totalAllocatedCost += cost

            EcoCreatorMetric(
                creator = creator,
                plannedItems = plannedItems,
                estimatedImpressions = estImpressions,
                allocatedCostInr = cost,
                estimatedCpmInr = cpm,
                ecoFitScore = creator.sustainabilityFitScore,
                potentialEcoActions = potentialActions,
                strengths = strengths,
                limitations = limitations,
                rankExplanation = rankExp
            )
        }.sortedByDescending { it.ecoFitScore }

        val remainingBudget = config.totalBudgetInr - totalAllocatedCost
        val budgetUtil = if (config.totalBudgetInr > 0) (totalAllocatedCost.toDouble() / config.totalBudgetInr.toDouble()) * 100.0 else 0.0
        val overallCpm = if (totalImpressions > 0) (totalAllocatedCost.toDouble() / totalImpressions.toDouble()) * 1000.0 else null
        val totalPotentialActions = (totalImpressions * config.assumedActionRate).toLong()

        EcoMatchAnalysis(
            totalImpressions = totalImpressions,
            totalAllocatedCostInr = totalAllocatedCost,
            remainingBudgetInr = remainingBudget,
            budgetUtilizationPercent = budgetUtil,
            overallCpmInr = overallCpm,
            totalPotentialActions = totalPotentialActions,
            isOverBudget = totalAllocatedCost > config.totalBudgetInr,
            topCreatorMetrics = metrics
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        EcoMatchAnalysis(0, 0, 100000, 0.0, null, 0, false, emptyList())
    )

    override fun onCleared() {
        super.onCleared()
        geminiService.shutdown()
    }
}
