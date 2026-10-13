package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Engagement
import com.example.data.model.EngagementStatus
import com.example.ui.MainViewModel
import com.example.ui.components.EngagementStatusBadge
import com.example.ui.components.formatInr
import com.example.ui.theme.NovaAmber
import com.example.ui.theme.NovaEcoGreen
import com.example.ui.theme.NovaPrimary

@Composable
fun EngagementTrackerScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val engagements by viewModel.repository.engagements.collectAsState()
    var filterStatus by remember { mutableStateOf<EngagementStatus?>(null) }
    var selectedEngagementForDetails by remember { mutableStateOf<Engagement?>(null) }

    val filteredEngagements = engagements.filter {
        filterStatus == null || it.status == filterStatus
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("engagement_tracker_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "Engagement Lifecycle Tracker",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Track deliverables, milestone status transitions, client reviews, and approvals in ₹ INR",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Demo Disclosure Notice
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = NovaAmber, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Demo simulation workflow. Illustrates contractual milestone tracking without live external notification dispatch.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Lifecycle 8-step pipeline visual preview card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "8-Stage Engagement Progression",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val stages = listOf(
                            EngagementStatus.DISCOVERED,
                            EngagementStatus.SHORTLISTED,
                            EngagementStatus.INVITED,
                            EngagementStatus.ACCEPTED,
                            EngagementStatus.IN_PROGRESS,
                            EngagementStatus.REVIEW,
                            EngagementStatus.DELIVERED,
                            EngagementStatus.COMPLETED
                        )
                        stages.forEachIndexed { index, stage ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = "${index + 1}. ${stage.label}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                    fontSize = 10.sp
                                )
                            }
                            if (index < stages.size - 1) {
                                Icon(
                                    Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Filter Chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = filterStatus == null,
                    onClick = { filterStatus = null },
                    label = { Text("All (${engagements.size})") }
                )
                listOf(
                    EngagementStatus.IN_PROGRESS,
                    EngagementStatus.REVIEW,
                    EngagementStatus.DELIVERED,
                    EngagementStatus.COMPLETED,
                    EngagementStatus.ACCEPTED,
                    EngagementStatus.INVITED
                ).forEach { st ->
                    val count = engagements.count { it.status == st }
                    FilterChip(
                        selected = filterStatus == st,
                        onClick = { filterStatus = if (filterStatus == st) null else st },
                        label = { Text("${st.label} ($count)") }
                    )
                }
            }
        }

        // Engagement Cards
        if (filteredEngagements.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.PendingActions, contentDescription = null, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No engagements match this filter", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        } else {
            items(filteredEngagements, key = { it.id }) { eng ->
                EngagementItemCard(
                    engagement = eng,
                    onViewDetails = { selectedEngagementForDetails = eng },
                    onAdvanceStatus = { nextStatus ->
                        viewModel.repository.updateEngagementStatus(eng.id, nextStatus)
                    },
                    onMarkSubmitted = {
                        viewModel.repository.markDeliverableSubmitted(eng.id)
                    },
                    onApprove = {
                        viewModel.repository.approveDelivery(eng.id)
                    },
                    onCancel = {
                        viewModel.repository.cancelEngagement(eng.id)
                    }
                )
            }
        }
    }

    // Engagement Detail Modal
    if (selectedEngagementForDetails != null) {
        val eng = selectedEngagementForDetails!!
        EngagementDetailDialog(
            engagement = eng,
            onDismiss = { selectedEngagementForDetails = null },
            onAddFeedback = { fb ->
                viewModel.repository.addEngagementFeedback(eng.id, fb)
                selectedEngagementForDetails = null
            }
        )
    }
}

@Composable
fun EngagementItemCard(
    engagement: Engagement,
    onViewDetails: () -> Unit,
    onAdvanceStatus: (EngagementStatus) -> Unit,
    onMarkSubmitted: () -> Unit,
    onApprove: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("engagement_card_${engagement.id}")
            .clickable { onViewDetails() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Brief + Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = engagement.briefTitle,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Assigned Creator: ${engagement.creatorName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                EngagementStatusBadge(status = engagement.status)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Details row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Contract: ${formatInr(engagement.budgetInr)}",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = NovaEcoGreen
                )
                Text(
                    text = "Deadline: ${engagement.deadline}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (engagement.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Notes: ${engagement.notes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Stepper / Transition Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (engagement.status) {
                    EngagementStatus.INVITED -> {
                        Button(
                            onClick = { onAdvanceStatus(EngagementStatus.ACCEPTED) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Accept Invite", fontSize = 12.sp)
                        }
                    }
                    EngagementStatus.ACCEPTED -> {
                        Button(
                            onClick = { onAdvanceStatus(EngagementStatus.IN_PROGRESS) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Start Production", fontSize = 12.sp)
                        }
                    }
                    EngagementStatus.IN_PROGRESS -> {
                        Button(
                            onClick = onMarkSubmitted,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Submit for Review", fontSize = 12.sp)
                        }
                    }
                    EngagementStatus.REVIEW -> {
                        Button(
                            onClick = { onAdvanceStatus(EngagementStatus.DELIVERED) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Text("Deliver Masters", fontSize = 12.sp)
                        }
                    }
                    EngagementStatus.DELIVERED -> {
                        Button(
                            onClick = onApprove,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NovaEcoGreen)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Approve & Complete", fontSize = 12.sp)
                        }
                    }
                    EngagementStatus.COMPLETED -> {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            color = NovaEcoGreen.copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Verified, contentDescription = null, tint = NovaEcoGreen, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Engagement Completed", color = NovaEcoGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                    else -> {}
                }

                OutlinedButton(
                    onClick = onViewDetails,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("History", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun EngagementDetailDialog(
    engagement: Engagement,
    onDismiss: () -> Unit,
    onAddFeedback: (String) -> Unit
) {
    var newFeedbackText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(engagement.briefTitle) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Creator: ${engagement.creatorName}", fontWeight = FontWeight.Bold)
                Text("Current Stage: ${engagement.status.label}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                Text("Budget: ${formatInr(engagement.budgetInr)} • Deadline: ${engagement.deadline}")

                HorizontalDivider()

                Text("Milestone History Log:", fontWeight = FontWeight.Bold)
                engagement.statusHistory.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("• ${item.status.label}: ${item.note}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                        Text(item.timestamp, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                if (engagement.feedback.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Latest Client Feedback: \"${engagement.feedback}\"", style = MaterialTheme.typography.bodySmall, color = NovaPrimary)
                }

                OutlinedTextField(
                    value = newFeedbackText,
                    onValueChange = { newFeedbackText = it },
                    label = { Text("Add Review Notes / Feedback") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (newFeedbackText.isNotBlank()) {
                        onAddFeedback(newFeedbackText)
                    } else {
                        onDismiss()
                    }
                }
            ) {
                Text(if (newFeedbackText.isNotBlank()) "Submit Feedback" else "Close")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Dismiss") }
        }
    )
}
