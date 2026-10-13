package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppNavDestination
import com.example.data.model.VerificationStatus
import com.example.ui.MainViewModel
import com.example.ui.components.CreatorCard
import com.example.ui.theme.NovaEcoGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val creators by viewModel.filteredCreators.collectAsState()
    val shortlistedIds by viewModel.repository.shortlistedIds.collectAsState()

    val searchQuery by viewModel.searchQuery.collectAsState()
    val filterSpecialization by viewModel.filterSpecialization.collectAsState()
    val filterAiTool by viewModel.filterAiTool.collectAsState()
    val filterVerification by viewModel.filterVerification.collectAsState()
    val filterEcoOnly by viewModel.filterEcoFriendlyOnly.collectAsState()
    val sortBy by viewModel.sortBy.collectAsState()

    var showFilterSheet by remember { mutableStateOf(false) }

    val specializations = listOf(
        "AI Filmmaker",
        "Generative Video",
        "Motion Design",
        "AI Advertising",
        "Graphic & Brand",
        "AI Animation",
        "Sustainability Content",
        "Product Visualization",
        "Creative Strategy",
        "Generative Art"
    )

    val tools = listOf("Runway Gen-3", "Midjourney v6", "Kling 1.5", "Flux.1", "ComfyUI", "TouchDesigner")

    BoxWithConstraints(modifier = modifier.fillMaxSize().testTag("discover_screen")) {
        val isWide = maxWidth >= 840.dp
        val columnsCount = if (maxWidth >= 1200.dp) 3 else 2

        if (isWide) {
            // Desktop / Laptop: 2-Panel Layout (Left Filter Sidebar + Right Responsive Grid)
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Left Filter Panel (~280dp)
                Card(
                    modifier = Modifier
                        .width(280.dp)
                        .fillMaxHeight(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            Text(
                                text = "Filters & Refinements",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        // Search
                        item {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { viewModel.searchQuery.value = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("creator_search_field_desktop"),
                                placeholder = { Text("Search keywords...", fontSize = 12.sp) },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true
                            )
                        }

                        // Sort By
                        item {
                            Text("Sort Results", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            listOf("Relevance", "Budget: Low to High", "Budget: High to Low", "EcoMatch Score", "Experience").forEach { s ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = sortBy == s,
                                        onClick = { viewModel.sortBy.value = s }
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(s, fontSize = 12.sp)
                                }
                            }
                        }

                        // Verification Filter
                        item {
                            Text("Verification", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = filterVerification == VerificationStatus.DEMO_VERIFIED,
                                    onCheckedChange = { checked ->
                                        viewModel.filterVerification.value = if (checked) VerificationStatus.DEMO_VERIFIED else null
                                    }
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Demo Verified Only", fontSize = 12.sp)
                            }
                        }

                        // EcoMatch Priority Switch
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("EcoMatch Priority (75%+)", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                Switch(
                                    checked = filterEcoOnly,
                                    onCheckedChange = { viewModel.filterEcoFriendlyOnly.value = it }
                                )
                            }
                        }

                        // Categories / Specializations
                        item {
                            Text("Specializations", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            FilterChip(
                                selected = filterSpecialization == null,
                                onClick = { viewModel.filterSpecialization.value = null },
                                label = { Text("All Categories", fontSize = 11.sp) },
                                modifier = Modifier.fillMaxWidth()
                            )
                            specializations.forEach { spec ->
                                FilterChip(
                                    selected = filterSpecialization == spec,
                                    onClick = {
                                        viewModel.filterSpecialization.value = if (filterSpecialization == spec) null else spec
                                    },
                                    label = { Text(spec, fontSize = 11.sp) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        // Reset button
                        item {
                            OutlinedButton(
                                onClick = { viewModel.clearAllFilters() },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Clear All Filters", fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Right Content Area: Multi-Column Creator Grid
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${creators.size} AI Creators Matching Criteria",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        val hasAny = searchQuery.isNotEmpty() || filterSpecialization != null || filterAiTool != null || filterVerification != null || filterEcoOnly
                        if (hasAny) {
                            TextButton(onClick = { viewModel.clearAllFilters() }) {
                                Text("Reset Filters", fontSize = 12.sp)
                            }
                        }
                    }

                    if (creators.isEmpty()) {
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
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.SearchOff, contentDescription = null, modifier = Modifier.size(54.dp))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("No matching creators found", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                Text("Try clearing or broadening your filter criteria in the left panel.", style = MaterialTheme.typography.bodySmall)
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(onClick = { viewModel.clearAllFilters() }) {
                                    Text("Reset Filters")
                                }
                            }
                        }
                    } else {
                        // Chunk creators into rows based on desktop width (2 or 3 per row)
                        val chunked = creators.chunked(columnsCount)
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(chunked) { rowCreators ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    rowCreators.forEach { creator ->
                                        CreatorCard(
                                            creator = creator,
                                            isShortlisted = shortlistedIds.contains(creator.id),
                                            onToggleShortlist = { viewModel.repository.toggleShortlist(creator.id) },
                                            onSelect = { viewModel.selectCreator(creator) },
                                            onInvite = {
                                                val firstBrief = viewModel.repository.campaignBriefs.value.firstOrNull()
                                                if (firstBrief != null) {
                                                    viewModel.repository.createEngagement(creator.id, firstBrief.id)
                                                    viewModel.navigateTo(AppNavDestination.ENGAGEMENTS)
                                                } else {
                                                    viewModel.selectCreator(creator)
                                                }
                                            },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    // Filler boxes if row isn't complete
                                    if (rowCreators.size < columnsCount) {
                                        repeat(columnsCount - rowCreators.size) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Mobile Compact Layout
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Search & Filter Action Bar
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.searchQuery.value = it },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("creator_search_field"),
                            placeholder = { Text("Search by name, skills, tools, city...") },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = "Search")
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear")
                                    }
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        IconButton(
                            onClick = { showFilterSheet = true },
                            modifier = Modifier.testTag("filter_button")
                        ) {
                            val hasActiveFilters = filterSpecialization != null || filterAiTool != null ||
                                    filterVerification != null || filterEcoOnly || sortBy != "Relevance"
                            BadgedBox(badge = {
                                if (hasActiveFilters) {
                                    Badge { Text("!") }
                                }
                            }) {
                                Icon(Icons.Default.FilterList, contentDescription = "Filters")
                            }
                        }
                    }
                }

                // Quick Category Filter Chips Horizontal Scroll
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = filterSpecialization == null,
                            onClick = { viewModel.filterSpecialization.value = null },
                            label = { Text("All Categories") }
                        )
                        specializations.forEach { spec ->
                            FilterChip(
                                selected = filterSpecialization == spec,
                                onClick = {
                                    viewModel.filterSpecialization.value = if (filterSpecialization == spec) null else spec
                                },
                                label = { Text(spec) }
                            )
                        }
                    }
                }

                // Active Filters Summary & Count
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${creators.size} AI Creators Available",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        val hasAnyFilter = searchQuery.isNotEmpty() || filterSpecialization != null ||
                                filterAiTool != null || filterVerification != null || filterEcoOnly
                        if (hasAnyFilter) {
                            TextButton(
                                onClick = { viewModel.clearAllFilters() },
                                modifier = Modifier.testTag("clear_filters_button")
                            ) {
                                Text("Clear Filters", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Creator Cards List
                if (creators.isEmpty()) {
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
                                Icon(
                                    imageVector = Icons.Default.SearchOff,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "No creators found",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Try adjusting your search keywords, budget limits, or category filters.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(onClick = { viewModel.clearAllFilters() }) {
                                    Text("Reset All Filters")
                                }
                            }
                        }
                    }
                } else {
                    items(creators, key = { it.id }) { creator ->
                        CreatorCard(
                            creator = creator,
                            isShortlisted = shortlistedIds.contains(creator.id),
                            onToggleShortlist = { viewModel.repository.toggleShortlist(creator.id) },
                            onSelect = { viewModel.selectCreator(creator) },
                            onInvite = {
                                val firstBrief = viewModel.repository.campaignBriefs.value.firstOrNull()
                                if (firstBrief != null) {
                                    viewModel.repository.createEngagement(creator.id, firstBrief.id)
                                    viewModel.navigateTo(AppNavDestination.ENGAGEMENTS)
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

    // Filter BottomSheet for Mobile
    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Filter & Sort Creators",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )

                // Sort Dropdown
                Text("Sort By", style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Relevance", "Budget: Low to High", "Budget: High to Low", "EcoMatch Score", "Experience").forEach { s ->
                        FilterChip(
                            selected = sortBy == s,
                            onClick = { viewModel.sortBy.value = s },
                            label = { Text(s) }
                        )
                    }
                }

                // Verification Filter
                Text("Verification Status", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = filterVerification == null,
                        onClick = { viewModel.filterVerification.value = null },
                        label = { Text("Any") }
                    )
                    FilterChip(
                        selected = filterVerification == VerificationStatus.DEMO_VERIFIED,
                        onClick = {
                            viewModel.filterVerification.value = if (filterVerification == VerificationStatus.DEMO_VERIFIED) null else VerificationStatus.DEMO_VERIFIED
                        },
                        label = { Text("Demo Verified Only") }
                    )
                }

                // Eco-Friendly Filter Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Nova EcoMatch Priority", style = MaterialTheme.typography.labelLarge)
                        Text(
                            "Creators with 75%+ sustainability score",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = filterEcoOnly,
                        onCheckedChange = { viewModel.filterEcoFriendlyOnly.value = it }
                    )
                }

                // AI Tool filter
                Text("Filter by AI Tool", style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tools.forEach { t ->
                        FilterChip(
                            selected = filterAiTool == t,
                            onClick = { viewModel.filterAiTool.value = if (filterAiTool == t) null else t },
                            label = { Text(t) }
                        )
                    }
                }

                Button(
                    onClick = { showFilterSheet = false },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Apply Filters (${creators.size} Results)")
                }
            }
        }
    }
}
