package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Creator
import com.example.data.model.VerificationStatus
import com.example.ui.MainViewModel
import com.example.ui.components.StatusBadge
import com.example.ui.theme.NovaAmber
import com.example.ui.theme.NovaEcoGreen

@Composable
fun VerificationCenterScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val creators by viewModel.repository.creators.collectAsState()
    var creatorToReview by remember { mutableStateOf<Creator?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("verification_center_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        item {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = null,
                        tint = NovaEcoGreen,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Creator Verification Center",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Text(
                    text = "Internal reviewer dashboard for auditing creator AI tools, workflows, original models, and commercial licensing rights.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // Mandatory Disclosure Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Audit Disclosure",
                        tint = NovaAmber,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Mandatory Platform Disclosure",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Verification statuses are internal demo review indicators. They do not establish real-world identity, credentials, or professional certification.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }

        // Overview stats
        item {
            val verified = creators.count { it.verificationStatus == VerificationStatus.DEMO_VERIFIED }
            val pending = creators.count { it.verificationStatus == VerificationStatus.PENDING_REVIEW }
            val notVerified = creators.count { it.verificationStatus == VerificationStatus.NOT_VERIFIED }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$verified", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = NovaEcoGreen)
                        Text("Verified", style = MaterialTheme.typography.labelSmall)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$pending", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = NovaAmber)
                        Text("Pending Review", style = MaterialTheme.typography.labelSmall)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$notVerified", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Not Verified", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        // List of creators with reviewer controls in responsive grid
        item {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val isWide = maxWidth >= 840.dp
                val cols = if (isWide) 2 else 1
                if (cols > 1) {
                    val chunked = creators.chunked(cols)
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        chunked.forEach { rowCreators ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                rowCreators.forEach { cr ->
                                    ReviewerCreatorCard(
                                        creator = cr,
                                        onOpenReview = { creatorToReview = cr },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (rowCreators.size < cols) {
                                    repeat(cols - rowCreators.size) { Spacer(modifier = Modifier.weight(1f)) }
                                }
                            }
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        creators.forEach { cr ->
                            ReviewerCreatorCard(
                                creator = cr,
                                onOpenReview = { creatorToReview = cr }
                            )
                        }
                    }
                }
            }
        }
    }

    // Reviewer Status Change Modal
    if (creatorToReview != null) {
        val cr = creatorToReview!!
        ReviewerAuditDialog(
            creator = cr,
            onDismiss = { creatorToReview = null },
            onUpdateStatus = { newStatus, notes ->
                viewModel.repository.updateCreatorVerification(cr.id, newStatus, notes)
                creatorToReview = null
            }
        )
    }
}

@Composable
fun ReviewerCreatorCard(
    creator: Creator,
    onOpenReview: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = creator.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "${creator.specialization} • ${creator.location.split(",")[0]}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatusBadge(status = creator.verificationStatus)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                AuditCheckItem(title = "Tools Proficiency", detail = creator.verificationDetails.toolsAudit)
                AuditCheckItem(title = "Workflow Originality", detail = creator.verificationDetails.workflowAudit)
                AuditCheckItem(title = "Commercial Licensing", detail = creator.verificationDetails.licensingAudit)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = onOpenReview,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Modify Review Status", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun AuditCheckItem(
    title: String,
    detail: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = NovaEcoGreen,
            modifier = Modifier
                .size(14.dp)
                .padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "$title: $detail",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp
        )
    }
}

@Composable
fun ReviewerAuditDialog(
    creator: Creator,
    onDismiss: () -> Unit,
    onUpdateStatus: (VerificationStatus, String) -> Unit
) {
    var selectedStatus by remember { mutableStateOf(creator.verificationStatus) }
    var reviewNote by remember { mutableStateOf(creator.verificationDetails.reviewNotes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Audit Review: ${creator.name}") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Select Internal Verification Status:", style = MaterialTheme.typography.labelLarge)

                VerificationStatus.values().forEach { st ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = if (selectedStatus == st) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedStatus == st,
                                onClick = { selectedStatus = st }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(st.label, fontWeight = if (selectedStatus == st) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }

                OutlinedTextField(
                    value = reviewNote,
                    onValueChange = { reviewNote = it },
                    label = { Text("Reviewer Notes & Audit Summary") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onUpdateStatus(selectedStatus, reviewNote) }
            ) {
                Text("Save Status")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
