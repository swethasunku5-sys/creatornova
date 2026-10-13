package com.example.data.model

enum class VerificationStatus(val label: String) {
    DEMO_VERIFIED("Demo Verified"),
    PENDING_REVIEW("Pending Review"),
    NOT_VERIFIED("Not Verified")
}

data class VerificationDetails(
    val toolsAudit: String,
    val workflowAudit: String,
    val portfolioAudit: String,
    val licensingAudit: String,
    val lastReviewedBy: String = "Demo Review Board",
    val reviewNotes: String = "Internal simulated verification indicator for CreatorNova sandbox."
)

data class PortfolioItem(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val toolsUsed: List<String>,
    val workflow: String,
    val aspectRatio: String = "9:16",
    val turnaroundDays: Int = 3,
    val licensing: String = "Full Commercial & Digital Rights with AI Attribution",
    val primaryColorHex: Long = 0xFF4F46E5,
    val secondaryColorHex: Long = 0xFF06B6D4
)

data class Creator(
    val id: String,
    val name: String,
    val avatarInitials: String,
    val headline: String,
    val bio: String,
    val location: String,
    val experienceLevel: String, // Junior, Mid-Level, Senior, Lead
    val specialization: String,
    val skills: List<String>,
    val aiTools: List<String>,
    val workflow: String,
    val contentTypes: List<String>,
    val languages: List<String>,
    val availability: String, // Available, Booked, In Discussion
    val followerCount: Long,
    val avgViews: Long,
    val engagementRate: Double, // e.g. 5.2%
    val indicativeRateInr: Long,
    val minBudgetInr: Long,
    val maxBudgetInr: Long,
    val verificationStatus: VerificationStatus,
    val verificationDetails: VerificationDetails,
    val sustainabilityFitScore: Int, // 0..100
    val sustainabilityInterests: List<String>,
    val portfolioItems: List<PortfolioItem>,
    val rating: Double = 4.9,
    val completedCampaigns: Int = 12
)

enum class BriefStatus(val label: String) {
    DRAFT("Draft"),
    PUBLISHED("Published"),
    ACTIVE("Active"),
    COMPLETED("Completed"),
    ARCHIVED("Archived")
}

data class CampaignBrief(
    val id: String,
    val title: String,
    val brandName: String,
    val objective: String,
    val targetAudience: String,
    val contentType: String,
    val creativeStyle: String,
    val platform: String,
    val aspectRatio: String,
    val preferredAiTools: String,
    val deliverables: String,
    val budgetInr: Long,
    val deadline: String,
    val commercialTerms: String,
    val additionalInstructions: String,
    val successCriteria: String,
    val status: BriefStatus = BriefStatus.PUBLISHED,
    val createdAt: String = "Today"
)

enum class EngagementStatus(val stepIndex: Int, val label: String) {
    DISCOVERED(0, "Discovered"),
    SHORTLISTED(1, "Shortlisted"),
    INVITED(2, "Invited"),
    ACCEPTED(3, "Accepted"),
    IN_PROGRESS(4, "In Progress"),
    REVIEW(5, "Review"),
    DELIVERED(6, "Delivered"),
    COMPLETED(7, "Completed"),
    CANCELLED(-1, "Cancelled");

    val isTerminal: Boolean
        get() = this == COMPLETED || this == CANCELLED
}

data class StatusHistoryItem(
    val status: EngagementStatus,
    val timestamp: String,
    val note: String
)

data class Engagement(
    val id: String,
    val creatorId: String,
    val creatorName: String,
    val briefId: String,
    val briefTitle: String,
    val status: EngagementStatus,
    val budgetInr: Long,
    val startDate: String,
    val deadline: String,
    val notes: String = "",
    val feedback: String = "",
    val deliverablesSubmitted: Boolean = false,
    val deliveryApproved: Boolean = false,
    val statusHistory: List<StatusHistoryItem> = emptyList(),
    val createdAt: String = "2026-10-10",
    val updatedAt: String = "2026-10-12"
)

enum class MessageSender {
    USER,
    NOVA
}

data class ChatMessage(
    val id: String,
    val sender: MessageSender,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isDemoFallback: Boolean = false
)

enum class AppNavDestination(val label: String) {
    DASHBOARD("Dashboard"),
    DISCOVER("Discover Creators"),
    BRIEFS("Campaign Briefs"),
    AI_BRIEF_BUILDER("AI Brief Builder"),
    SHORTLIST("Shortlist & Compare"),
    ENGAGEMENTS("Engagement Tracker"),
    VERIFICATION("Verification Center"),
    ECOMATCH("Nova EcoMatch"),
    ANALYTICS("Analytics"),
    EVALUATION("Evaluation Guide"),
    PRESENTATION("Hackathon Demo"),
    SETTINGS("Settings")
}
