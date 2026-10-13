package com.example.ui.screens

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Creator
import com.example.ui.MainViewModel
import com.example.ui.components.CreatorCard
import com.example.ui.components.StatusBadge
import com.example.ui.components.formatInr
import com.example.ui.theme.NovaEcoGreen

@Composable
fun ShortlistScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val allCreators by viewModel.repository.creators.collectAsState()
    val shortlistedIds by viewModel.repository.shortlistedIds.collectAsState()
    val shortlistedCreators = allCreators.filter { shortlistedIds.contains(it.id) }

    var isCompareMode by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("shortlist_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Shortlisted Creators",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "${shortlistedCreators.size} saved AI creators for upcoming campaigns",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (shortlistedCreators.size >= 2) {
                    FilledTonalButton(
                        onClick = { isCompareMode = !isCompareMode },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = if (isCompareMode) Icons.Default.ViewList else Icons.Default.CompareArrows,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isCompareMode) "List View" else "Compare Side-by-Side", fontSize = 12.sp)
                    }
                }
            }
        }

        // Empty state
        if (shortlistedCreators.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.StarOutline,
                            contentDescription = null,
                            modifier = Modifier.size(54.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No creators shortlisted yet",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Browse the Discover marketplace and tap the star icon to save creators here for side-by-side comparison.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.navigateTo(com.example.data.model.AppNavDestination.DISCOVER) }
                        ) {
                            Text("Discover AI Creators")
                        }
                    }
                }
            }
        } else if (isCompareMode) {
            // Side-by-Side Comparison Table
            item {
                ShortlistComparisonTable(
                    creators = shortlistedCreators,
                    onSelect = { viewModel.selectCreator(it) },
                    onRemove = { viewModel.repository.toggleShortlist(it.id) }
                )
            }
        } else {
            // Responsive Card Grid
            item {
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val isWide = maxWidth >= 840.dp
                    val cols = if (maxWidth >= 1200.dp) 3 else if (isWide) 2 else 1
                    if (cols > 1) {
                        val chunked = shortlistedCreators.chunked(cols)
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            chunked.forEach { rowCreators ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    rowCreators.forEach { creator ->
                                        CreatorCard(
                                            creator = creator,
                                            isShortlisted = true,
                                            onToggleShortlist = { viewModel.repository.toggleShortlist(creator.id) },
                                            onSelect = { viewModel.selectCreator(creator) },
                                            onInvite = {
                                                val firstBrief = viewModel.repository.campaignBriefs.value.firstOrNull()
                                                if (firstBrief != null) {
                                                    viewModel.repository.createEngagement(creator.id, firstBrief.id)
                                                    viewModel.navigateTo(com.example.data.model.AppNavDestination.ENGAGEMENTS)
                                                } else {
                                                    viewModel.selectCreator(creator)
                                                }
                                            },
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
                            shortlistedCreators.forEach { creator ->
                                CreatorCard(
                                    creator = creator,
                                    isShortlisted = true,
                                    onToggleShortlist = { viewModel.repository.toggleShortlist(creator.id) },
                                    onSelect = { viewModel.selectCreator(creator) },
                                    onInvite = {
                                        val firstBrief = viewModel.repository.campaignBriefs.value.firstOrNull()
                                        if (firstBrief != null) {
                                            viewModel.repository.createEngagement(creator.id, firstBrief.id)
                                            viewModel.navigateTo(com.example.data.model.AppNavDestination.ENGAGEMENTS)
                                        } else {
                                            viewModel.selectCreator(creator)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ShortlistComparisonTable(
    creators: List<Creator>,
    onSelect: (Creator) -> Unit,
    onRemove: (Creator) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .horizontalScroll(rememberScrollState())
        ) {
            Text(
                text = "Side-by-Side Creator Matrix",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Header row with creator names
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(modifier = Modifier.width(130.dp)) {
                    Text("Attribute", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                creators.forEach { c ->
                    Box(modifier = Modifier.width(150.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(c.name, fontWeight = FontWeight.Bold, maxLines = 1)
                            IconButton(onClick = { onRemove(c) }, modifier = Modifier.size(20.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Specialization
            ComparisonRow(label = "Specialization", creators = creators) { it.specialization }

            // Indicative Rate in INR
            ComparisonRow(label = "Indicative Rate", creators = creators) { formatInr(it.indicativeRateInr) }

            // EcoMatch Score
            ComparisonRow(label = "EcoMatch Score", creators = creators) { "${it.sustainabilityFitScore}/100" }

            // Experience
            ComparisonRow(label = "Experience", creators = creators) { it.experienceLevel }

            // Completed Work
            ComparisonRow(label = "Completed", creators = creators) { "${it.completedCampaigns} campaigns" }

            // AI Tools
            ComparisonRow(label = "Core AI Tools", creators = creators) { it.aiTools.take(2).joinToString(", ") }

            // Location
            ComparisonRow(label = "Location", creators = creators) { it.location.split(",")[0] }

            // Verification
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(vertical = 6.dp)) {
                Box(modifier = Modifier.width(130.dp)) {
                    Text("Verification", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                creators.forEach { c ->
                    Box(modifier = Modifier.width(150.dp)) {
                        StatusBadge(status = c.verificationStatus)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Actions row
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(modifier = Modifier.width(130.dp))
                creators.forEach { c ->
                    Box(modifier = Modifier.width(150.dp)) {
                        Button(
                            onClick = { onSelect(c) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            Text("Profile", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ComparisonRow(
    label: String,
    creators: List<Creator>,
    getValue: (Creator) -> String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(vertical = 6.dp)
    ) {
        Box(modifier = Modifier.width(130.dp)) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        creators.forEach { c ->
            Box(modifier = Modifier.width(150.dp)) {
                Text(getValue(c), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium))
            }
        }
    }
}
