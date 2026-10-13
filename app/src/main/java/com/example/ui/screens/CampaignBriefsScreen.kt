package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BriefStatus
import com.example.data.model.CampaignBrief
import com.example.ui.MainViewModel
import com.example.ui.components.formatInr
import com.example.ui.theme.NovaEcoGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampaignBriefsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val briefs by viewModel.repository.campaignBriefs.collectAsState()
    var selectedStatusFilter by remember { mutableStateOf<BriefStatus?>(null) }
    var briefToEdit by remember { mutableStateOf<CampaignBrief?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var briefToDelete by remember { mutableStateOf<CampaignBrief?>(null) }
    var briefForDetail by remember { mutableStateOf<CampaignBrief?>(null) }

    val filteredBriefs = briefs.filter {
        selectedStatusFilter == null || it.status == selectedStatusFilter
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("create_brief_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Campaign Brief")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("campaign_briefs_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header & Status Filters
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Campaign Briefs",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Manage brand briefs and project specifications in ₹ INR",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(
                        onClick = { viewModel.navigateTo(com.example.data.model.AppNavDestination.AI_BRIEF_BUILDER) },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("AI Builder", fontSize = 12.sp)
                    }
                }
            }

            // Status Filter Chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedStatusFilter == null,
                        onClick = { selectedStatusFilter = null },
                        label = { Text("All (${briefs.size})") }
                    )
                    BriefStatus.values().forEach { status ->
                        val count = briefs.count { it.status == status }
                        FilterChip(
                            selected = selectedStatusFilter == status,
                            onClick = { selectedStatusFilter = if (selectedStatusFilter == status) null else status },
                            label = { Text("${status.label} ($count)") }
                        )
                    }
                }
            }

            // Briefs List
            if (filteredBriefs.isEmpty()) {
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
                            Icon(Icons.Default.AssignmentLate, contentDescription = null, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No campaign briefs found", style = MaterialTheme.typography.titleMedium)
                            Text("Create your first campaign brief or generate one with Nova AI.", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            } else {
                item {
                    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                        val isWide = maxWidth >= 840.dp
                        val cols = if (maxWidth >= 1200.dp) 3 else if (isWide) 2 else 1

                        if (cols > 1) {
                            val chunked = filteredBriefs.chunked(cols)
                            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                chunked.forEach { rowBriefs ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                                    ) {
                                        rowBriefs.forEach { brief ->
                                            CampaignBriefCard(
                                                brief = brief,
                                                onViewDetails = { briefForDetail = brief },
                                                onEdit = { briefToEdit = brief },
                                                onDuplicate = { viewModel.repository.duplicateBrief(brief.id) },
                                                onDelete = { briefToDelete = brief },
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                        if (rowBriefs.size < cols) {
                                            repeat(cols - rowBriefs.size) {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                filteredBriefs.forEach { brief ->
                                    CampaignBriefCard(
                                        brief = brief,
                                        onViewDetails = { briefForDetail = brief },
                                        onEdit = { briefToEdit = brief },
                                        onDuplicate = { viewModel.repository.duplicateBrief(brief.id) },
                                        onDelete = { briefToDelete = brief }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Create / Edit Dialog
    if (showCreateDialog || briefToEdit != null) {
        BriefEditorDialog(
            initialBrief = briefToEdit,
            onDismiss = {
                showCreateDialog = false
                briefToEdit = null
            },
            onSave = { savedBrief ->
                if (briefToEdit != null) {
                    viewModel.repository.updateBrief(savedBrief)
                } else {
                    viewModel.repository.addBrief(savedBrief)
                }
                showCreateDialog = false
                briefToEdit = null
            }
        )
    }

    // Delete Confirmation Dialog
    if (briefToDelete != null) {
        AlertDialog(
            onDismissRequest = { briefToDelete = null },
            title = { Text("Delete Campaign Brief?") },
            text = { Text("Are you sure you want to delete '${briefToDelete?.title}'? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        briefToDelete?.let { viewModel.repository.deleteBrief(it.id) }
                        briefToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { briefToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Detail Modal
    if (briefForDetail != null) {
        BriefDetailModal(
            brief = briefForDetail!!,
            onDismiss = { briefForDetail = null },
            onInviteCreator = {
                briefForDetail = null
                viewModel.navigateTo(com.example.data.model.AppNavDestination.DISCOVER)
            }
        )
    }
}

@Composable
fun CampaignBriefCard(
    brief: CampaignBrief,
    onViewDetails: () -> Unit,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("brief_card_${brief.id}")
            .clickable { onViewDetails() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = brief.status.label,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Text(
                    text = formatInr(brief.budgetInr),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = NovaEcoGreen
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = brief.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Brand: ${brief.brandName} • Deadline: ${brief.deadline}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = brief.objective,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Deliverables: ${brief.deliverables}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onDuplicate, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Duplicate", modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun BriefEditorDialog(
    initialBrief: CampaignBrief?,
    onDismiss: () -> Unit,
    onSave: (CampaignBrief) -> Unit
) {
    var title by remember { mutableStateOf(initialBrief?.title ?: "") }
    var brandName by remember { mutableStateOf(initialBrief?.brandName ?: "") }
    var objective by remember { mutableStateOf(initialBrief?.objective ?: "") }
    var targetAudience by remember { mutableStateOf(initialBrief?.targetAudience ?: "Urban Indian Youth (18-28)") }
    var contentType by remember { mutableStateOf(initialBrief?.contentType ?: "AI Video Reels & Posters") }
    var deliverables by remember { mutableStateOf(initialBrief?.deliverables ?: "3x 30s Reels, 2x Static Posters") }
    var budgetStr by remember { mutableStateOf(initialBrief?.budgetInr?.toString() ?: "75000") }
    var deadline by remember { mutableStateOf(initialBrief?.deadline ?: "2026-11-15") }
    var commercialTerms by remember { mutableStateOf(initialBrief?.commercialTerms ?: "Full Digital Commercial Rights") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialBrief != null) "Edit Campaign Brief" else "New Campaign Brief") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Campaign Title *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = brandName,
                    onValueChange = { brandName = it },
                    label = { Text("Brand Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = budgetStr,
                    onValueChange = { budgetStr = it.filter { char -> char.isDigit() } },
                    label = { Text("Budget in ₹ INR *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = objective,
                    onValueChange = { objective = it },
                    label = { Text("Campaign Objective *") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3
                )
                OutlinedTextField(
                    value = deliverables,
                    onValueChange = { deliverables = it },
                    label = { Text("Deliverables") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val budget = budgetStr.toLongOrNull() ?: 50000L
                    val brief = (initialBrief ?: CampaignBrief(
                        id = "b_${System.currentTimeMillis()}",
                        title = title,
                        brandName = brandName,
                        objective = objective,
                        targetAudience = targetAudience,
                        contentType = contentType,
                        creativeStyle = "Cinematic modern",
                        platform = "Instagram & YouTube",
                        aspectRatio = "9:16",
                        preferredAiTools = "Runway Gen-3, Midjourney v6",
                        deliverables = deliverables,
                        budgetInr = budget,
                        deadline = deadline,
                        commercialTerms = commercialTerms,
                        additionalInstructions = "",
                        successCriteria = "100k+ impressions",
                        status = BriefStatus.PUBLISHED
                    )).copy(
                        title = title,
                        brandName = brandName,
                        objective = objective,
                        deliverables = deliverables,
                        budgetInr = budget
                    )
                    onSave(brief)
                },
                enabled = title.isNotBlank() && brandName.isNotBlank() && objective.isNotBlank()
            ) {
                Text("Save Brief")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun BriefDetailModal(
    brief: CampaignBrief,
    onDismiss: () -> Unit,
    onInviteCreator: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(brief.title) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Brand: ${brief.brandName}", fontWeight = FontWeight.Bold)
                Text("Budget: ${formatInr(brief.budgetInr)}", color = NovaEcoGreen, fontWeight = FontWeight.Bold)
                Text("Deadline: ${brief.deadline}")
                HorizontalDivider()
                Text("Objective:", fontWeight = FontWeight.SemiBold)
                Text(brief.objective, style = MaterialTheme.typography.bodySmall)
                Text("Target Audience:", fontWeight = FontWeight.SemiBold)
                Text(brief.targetAudience, style = MaterialTheme.typography.bodySmall)
                Text("Deliverables:", fontWeight = FontWeight.SemiBold)
                Text(brief.deliverables, style = MaterialTheme.typography.bodySmall)
                Text("Licensing Terms:", fontWeight = FontWeight.SemiBold)
                Text(brief.commercialTerms, style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            Button(onClick = onInviteCreator) {
                Text("Find Creators for Brief")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
