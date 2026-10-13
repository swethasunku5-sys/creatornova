package com.example.data.repository

import com.example.data.datasource.SeedData
import com.example.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CreatorNovaRepository {

    private val _creators = MutableStateFlow<List<Creator>>(SeedData.demoCreators)
    val creators: StateFlow<List<Creator>> = _creators.asStateFlow()

    private val _campaignBriefs = MutableStateFlow<List<CampaignBrief>>(SeedData.demoCampaignBriefs)
    val campaignBriefs: StateFlow<List<CampaignBrief>> = _campaignBriefs.asStateFlow()

    private val _shortlistedIds = MutableStateFlow<Set<String>>(setOf("c1", "c7"))
    val shortlistedIds: StateFlow<Set<String>> = _shortlistedIds.asStateFlow()

    private val _engagements = MutableStateFlow<List<Engagement>>(SeedData.demoEngagements)
    val engagements: StateFlow<List<Engagement>> = _engagements.asStateFlow()

    private val _isDarkMode = MutableStateFlow<Boolean>(true)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    fun toggleDarkMode() {
        _isDarkMode.update { !it }
    }

    fun setDarkMode(dark: Boolean) {
        _isDarkMode.value = dark
    }

    // --- Shortlist Operations ---
    fun toggleShortlist(creatorId: String) {
        _shortlistedIds.update { current ->
            if (current.contains(creatorId)) {
                current - creatorId
            } else {
                current + creatorId
            }
        }
    }

    fun isShortlisted(creatorId: String): Boolean {
        return _shortlistedIds.value.contains(creatorId)
    }

    // --- Creator & Verification Operations ---
    fun updateCreatorVerification(creatorId: String, newStatus: VerificationStatus, note: String = "") {
        _creators.update { list ->
            list.map { creator ->
                if (creator.id == creatorId) {
                    val updatedDetails = creator.verificationDetails.copy(
                        reviewNotes = if (note.isNotBlank()) note else creator.verificationDetails.reviewNotes
                    )
                    creator.copy(
                        verificationStatus = newStatus,
                        verificationDetails = updatedDetails
                    )
                } else creator
            }
        }
    }

    // --- Campaign Brief Operations ---
    fun addBrief(brief: CampaignBrief) {
        _campaignBriefs.update { listOf(brief) + it }
    }

    fun updateBrief(brief: CampaignBrief) {
        _campaignBriefs.update { list ->
            list.map { if (it.id == brief.id) brief else it }
        }
    }

    fun duplicateBrief(briefId: String) {
        val original = _campaignBriefs.value.find { it.id == briefId } ?: return
        val copy = original.copy(
            id = "b_${System.currentTimeMillis()}",
            title = "${original.title} (Copy)",
            status = BriefStatus.DRAFT,
            createdAt = "Just now"
        )
        _campaignBriefs.update { listOf(copy) + it }
    }

    fun deleteBrief(briefId: String) {
        _campaignBriefs.update { list -> list.filterNot { it.id == briefId } }
    }

    // --- Engagement Operations ---
    fun createEngagement(creatorId: String, briefId: String): Engagement? {
        val creator = _creators.value.find { it.id == creatorId } ?: return null
        val brief = _campaignBriefs.value.find { it.id == briefId } ?: return null

        val newEngagement = Engagement(
            id = "eng_${System.currentTimeMillis()}",
            creatorId = creator.id,
            creatorName = creator.name,
            briefId = brief.id,
            briefTitle = brief.title,
            status = EngagementStatus.INVITED,
            budgetInr = creator.indicativeRateInr,
            startDate = "2026-10-12",
            deadline = brief.deadline,
            notes = "Engagement initialized via CreatorNova marketplace.",
            statusHistory = listOf(
                StatusHistoryItem(EngagementStatus.DISCOVERED, "2026-10-12", "Creator selected from marketplace."),
                StatusHistoryItem(EngagementStatus.INVITED, "2026-10-12", "Formal invitation created for brief: ${brief.title}")
            )
        )

        _engagements.update { listOf(newEngagement) + it }
        return newEngagement
    }

    fun updateEngagementStatus(engagementId: String, targetStatus: EngagementStatus, note: String = ""): Boolean {
        var updated = false
        _engagements.update { list ->
            list.map { eng ->
                if (eng.id == engagementId) {
                    val history = eng.statusHistory + StatusHistoryItem(
                        status = targetStatus,
                        timestamp = "2026-10-12",
                        note = if (note.isNotBlank()) note else "Transitioned to ${targetStatus.label}"
                    )
                    updated = true
                    eng.copy(
                        status = targetStatus,
                        statusHistory = history,
                        deliverablesSubmitted = if (targetStatus == EngagementStatus.DELIVERED || targetStatus == EngagementStatus.COMPLETED) true else eng.deliverablesSubmitted,
                        deliveryApproved = if (targetStatus == EngagementStatus.COMPLETED) true else eng.deliveryApproved,
                        updatedAt = "2026-10-12"
                    )
                } else eng
            }
        }
        return updated
    }

    fun addEngagementFeedback(engagementId: String, feedbackText: String) {
        _engagements.update { list ->
            list.map { eng ->
                if (eng.id == engagementId) {
                    eng.copy(feedback = feedbackText, updatedAt = "2026-10-12")
                } else eng
            }
        }
    }

    fun markDeliverableSubmitted(engagementId: String) {
        _engagements.update { list ->
            list.map { eng ->
                if (eng.id == engagementId) {
                    val nextStatus = if (eng.status == EngagementStatus.IN_PROGRESS) EngagementStatus.REVIEW else eng.status
                    eng.copy(
                        deliverablesSubmitted = true,
                        status = nextStatus,
                        statusHistory = eng.statusHistory + StatusHistoryItem(
                            status = nextStatus,
                            timestamp = "2026-10-12",
                            note = "Creator marked deliverables submitted for review."
                        ),
                        updatedAt = "2026-10-12"
                    )
                } else eng
            }
        }
    }

    fun approveDelivery(engagementId: String) {
        _engagements.update { list ->
            list.map { eng ->
                if (eng.id == engagementId) {
                    eng.copy(
                        deliveryApproved = true,
                        status = EngagementStatus.COMPLETED,
                        statusHistory = eng.statusHistory + StatusHistoryItem(
                            status = EngagementStatus.COMPLETED,
                            timestamp = "2026-10-12",
                            note = "Client approved final deliverables and completed engagement."
                        ),
                        updatedAt = "2026-10-12"
                    )
                } else eng
            }
        }
    }

    fun cancelEngagement(engagementId: String, reason: String = "Cancelled by user") {
        _engagements.update { list ->
            list.map { eng ->
                if (eng.id == engagementId) {
                    eng.copy(
                        status = EngagementStatus.CANCELLED,
                        statusHistory = eng.statusHistory + StatusHistoryItem(
                            status = EngagementStatus.CANCELLED,
                            timestamp = "2026-10-12",
                            note = reason
                        ),
                        updatedAt = "2026-10-12"
                    )
                } else eng
            }
        }
    }

    // --- Reset to Demo Seed Data ---
    fun resetDemoData() {
        _creators.value = SeedData.demoCreators
        _campaignBriefs.value = SeedData.demoCampaignBriefs
        _engagements.value = SeedData.demoEngagements
        _shortlistedIds.value = setOf("c1", "c7")
    }
}
