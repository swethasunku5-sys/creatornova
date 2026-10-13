package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.MainViewModel
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val creators by viewModel.repository.creators.collectAsState()
    val briefs by viewModel.repository.campaignBriefs.collectAsState()
    val shortlistedIds by viewModel.repository.shortlistedIds.collectAsState()
    val engagements by viewModel.repository.engagements.collectAsState()

    // Calculated dynamic metrics
    val totalCreators = creators.size
    val verifiedCount = creators.count { it.verificationStatus == VerificationStatus.DEMO_VERIFIED }
    val openBriefs = briefs.count { it.status == BriefStatus.PUBLISHED || it.status == BriefStatus.ACTIVE }
    val shortlistedCount = shortlistedIds.size
    val activeEngagements = engagements.count { !it.status.isTerminal }
    val completedEngagements = engagements.count { it.status == EngagementStatus.COMPLETED }
    val totalAllocatedBudgetInr = engagements.sumOf { it.budgetInr }

    BoxWithConstraints(modifier = modifier.fillMaxSize().testTag("dashboard_screen")) {
        val isWide = maxWidth >= 840.dp

        if (isWide) {
            // Desktop / Laptop Full-Width Dashboard
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Top Row: 4 KPI Cards spanning full width (1280px / 1440px)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        StatCard(
                            title = "Total Creators",
                            value = "$totalCreators",
                            subtitle = "$verifiedCount Demo Verified",
                            icon = Icons.Default.People,
                            iconTint = NovaPrimary,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.navigateTo(AppNavDestination.DISCOVER) }
                        )
                        StatCard(
                            title = "Active Engagements",
                            value = "$activeEngagements",
                            subtitle = "$completedEngagements Completed",
                            icon = Icons.Default.Timeline,
                            iconTint = NovaSecondary,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.navigateTo(AppNavDestination.ENGAGEMENTS) }
                        )
                        StatCard(
                            title = "Allocated Budget",
                            value = formatInr(totalAllocatedBudgetInr),
                            subtitle = "Across ${engagements.size} contracts",
                            icon = Icons.Default.CurrencyRupee,
                            iconTint = NovaEcoGreen,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.navigateTo(AppNavDestination.ANALYTICS) }
                        )
                        StatCard(
                            title = "Campaign Briefs",
                            value = "${briefs.size}",
                            subtitle = "$openBriefs open briefs",
                            icon = Icons.Default.Assignment,
                            iconTint = NovaTertiary,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.navigateTo(AppNavDestination.BRIEFS) }
                        )
                    }
                }

                // 2-Column Split Body Layout
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Left Column (65% width): Hero Banner + Featured Creators + Milestones
                        Column(
                            modifier = Modifier.weight(1.8f),
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            // Hero Banner
                            DashboardHeroBanner(viewModel = viewModel)

                            // Featured Creators Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Featured Indian AI Creators",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                TextButton(onClick = { viewModel.navigateTo(AppNavDestination.DISCOVER) }) {
                                    Text("View All ($totalCreators)")
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                creators.take(3).forEach { creator ->
                                    FeaturedCreatorItem(
                                        creator = creator,
                                        isShortlisted = shortlistedIds.contains(creator.id),
                                        onToggleShortlist = { viewModel.repository.toggleShortlist(creator.id) },
                                        onSelect = { viewModel.selectCreator(creator) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            // Recent Milestones
                            Text(
                                text = "Recent Engagement Milestones",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )

                            engagements.take(3).forEach { eng ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.selectEngagement(eng)
                                            viewModel.navigateTo(AppNavDestination.ENGAGEMENTS)
                                        },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = eng.briefTitle,
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "Creator: ${eng.creatorName} • Deadline: ${eng.deadline}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        EngagementStatusBadge(status = eng.status)
                                    }
                                }
                            }
                        }

                        // Right Column (35% width): Quick Actions + Eco Highlights + Hackathon Info
                        Column(
                            modifier = Modifier.weight(1.2f),
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            // Quick Actions Card
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Quick Actions",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        QuickActionButton(
                                            label = "Discover",
                                            icon = Icons.Default.Explore,
                                            modifier = Modifier.weight(1f)
                                        ) { viewModel.navigateTo(AppNavDestination.DISCOVER) }

                                        QuickActionButton(
                                            label = "AI Brief",
                                            icon = Icons.Default.AutoAwesome,
                                            modifier = Modifier.weight(1f)
                                        ) { viewModel.navigateTo(AppNavDestination.AI_BRIEF_BUILDER) }
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        QuickActionButton(
                                            label = "Shortlist (${shortlistedCount})",
                                            icon = Icons.Default.Star,
                                            modifier = Modifier.weight(1f)
                                        ) { viewModel.navigateTo(AppNavDestination.SHORTLIST) }

                                        QuickActionButton(
                                            label = "Nova EcoMatch",
                                            icon = Icons.Default.Eco,
                                            modifier = Modifier.weight(1f)
                                        ) { viewModel.navigateTo(AppNavDestination.ECOMATCH) }
                                    }
                                }
                            }

                            // EcoMatch Top Creators Highlight Card
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Nova EcoMatch Leaders",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        TextButton(onClick = { viewModel.navigateTo(AppNavDestination.ECOMATCH) }) {
                                            Text("Predictor", fontSize = 12.sp)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    creators.sortedByDescending { it.sustainabilityFitScore }.take(3).forEach { c ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { viewModel.selectCreator(c) }
                                                .padding(vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text(c.name, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                                                Text(c.specialization, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = NovaEcoGreen.copy(alpha = 0.15f)
                                            ) {
                                                Text(
                                                    text = "${c.sustainabilityFitScore}% Fit",
                                                    color = NovaEcoGreen,
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Verification Center Callout Card
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = NovaEcoGreen, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Transparent Review Indicators", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "$verifiedCount of $totalCreators creators verified with transparent tools, workflow, and licensing audits.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    OutlinedButton(
                                        onClick = { viewModel.navigateTo(AppNavDestination.VERIFICATION) },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Open Verification Center", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Mobile / Compact Layout (Preserved original experience)
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Welcome Hero Banner
                item {
                    DashboardHeroBanner(viewModel = viewModel)
                }

                // Live KPI Grid
                item {
                    Text(
                        text = "Platform Overview",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            title = "Total Creators",
                            value = "$totalCreators",
                            subtitle = "$verifiedCount Demo Verified",
                            icon = Icons.Default.People,
                            iconTint = NovaPrimary,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.navigateTo(AppNavDestination.DISCOVER) }
                        )
                        StatCard(
                            title = "Active Engagements",
                            value = "$activeEngagements",
                            subtitle = "$completedEngagements Completed",
                            icon = Icons.Default.Timeline,
                            iconTint = NovaSecondary,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.navigateTo(AppNavDestination.ENGAGEMENTS) }
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            title = "Allocated Budget",
                            value = formatInr(totalAllocatedBudgetInr),
                            subtitle = "Across ${engagements.size} contracts",
                            icon = Icons.Default.CurrencyRupee,
                            iconTint = NovaEcoGreen,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.navigateTo(AppNavDestination.ANALYTICS) }
                        )
                        StatCard(
                            title = "Campaign Briefs",
                            value = "${briefs.size}",
                            subtitle = "$openBriefs open briefs",
                            icon = Icons.Default.Assignment,
                            iconTint = NovaTertiary,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.navigateTo(AppNavDestination.BRIEFS) }
                        )
                    }
                }

                // Quick Actions
                item {
                    Text(
                        text = "Quick Actions",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuickActionButton(
                            label = "Discover",
                            icon = Icons.Default.Explore,
                            modifier = Modifier.weight(1f)
                        ) { viewModel.navigateTo(AppNavDestination.DISCOVER) }

                        QuickActionButton(
                            label = "AI Brief",
                            icon = Icons.Default.AutoAwesome,
                            modifier = Modifier.weight(1f)
                        ) { viewModel.navigateTo(AppNavDestination.AI_BRIEF_BUILDER) }

                        QuickActionButton(
                            label = "Shortlist",
                            icon = Icons.Default.Star,
                            modifier = Modifier.weight(1f)
                        ) { viewModel.navigateTo(AppNavDestination.SHORTLIST) }

                        QuickActionButton(
                            label = "EcoMatch",
                            icon = Icons.Default.Eco,
                            modifier = Modifier.weight(1f)
                        ) { viewModel.navigateTo(AppNavDestination.ECOMATCH) }
                    }
                }

                // Recommended Creators Carousel
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Featured AI Creators",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        TextButton(onClick = { viewModel.navigateTo(AppNavDestination.DISCOVER) }) {
                            Text("View All ($totalCreators)")
                        }
                    }
                }

                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(creators.take(5)) { creator ->
                            FeaturedCreatorItem(
                                creator = creator,
                                isShortlisted = shortlistedIds.contains(creator.id),
                                onToggleShortlist = { viewModel.repository.toggleShortlist(creator.id) },
                                onSelect = { viewModel.selectCreator(creator) }
                            )
                        }
                    }
                }

                // Recent Engagements & Upcoming Deadlines
                item {
                    Text(
                        text = "Recent Engagement Milestones",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                items(engagements.take(3)) { eng ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.selectEngagement(eng)
                                viewModel.navigateTo(AppNavDestination.ENGAGEMENTS)
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = eng.briefTitle,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Creator: ${eng.creatorName} • Deadline: ${eng.deadline}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            EngagementStatusBadge(status = eng.status)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardHeroBanner(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            NovaPrimary.copy(alpha = 0.15f),
                            NovaSecondary.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = NovaPrimary.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "ByteXL × Kampus.VC Hackathon",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = NovaEcoGreen.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "Live Demo Mode",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = NovaEcoGreen,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "AI Content Creator Marketplace",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Connect with top Indian AI filmmakers, motion artists, and advertising creatives. Structure campaign briefs with Gemini and forecast sustainability impact.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { viewModel.navigateTo(AppNavDestination.DISCOVER) },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.testTag("hero_discover_button")
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Discover Creators")
                    }
                    OutlinedButton(
                        onClick = { viewModel.navigateTo(AppNavDestination.AI_BRIEF_BUILDER) },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("hero_brief_builder_button")
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("AI Brief Builder")
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionButton(
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun FeaturedCreatorItem(
    creator: Creator,
    isShortlisted: Boolean,
    onToggleShortlist: () -> Unit,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .widthIn(min = 180.dp, max = 340.dp)
            .clickable { onSelect() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(NovaPrimary, NovaSecondary))),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = creator.avatarInitials,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = creator.name,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = creator.specialization,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 10.sp,
                        maxLines = 1
                    )
                }
                IconButton(
                    onClick = onToggleShortlist,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (isShortlisted) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Shortlist",
                        tint = if (isShortlisted) NovaAmber else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = creator.bio,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${formatInr(creator.indicativeRateInr)}",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = NovaEcoGreen.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "${creator.sustainabilityFitScore}% EcoFit",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = NovaEcoGreen,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
