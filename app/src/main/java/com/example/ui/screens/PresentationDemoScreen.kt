package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppNavDestination
import com.example.ui.MainViewModel
import com.example.ui.theme.NovaEcoGreen
import com.example.ui.theme.NovaPrimary
import com.example.ui.theme.NovaSecondary

@Composable
fun PresentationDemoScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("presentation_demo_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PlayCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Interactive Platform Tour",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Text(
                    text = "A guided 6-step walkthrough designed for hackathon judges to experience CreatorNova's end-to-end user journey.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        item {
            TourStepCard(
                step = "Step 1",
                title = "Discover AI Creators",
                description = "Explore 10+ seeded Indian AI creators across Mumbai, Bengaluru, Hyderabad, and Delhi. Filter by specialized AI tools, verified status, and rates in ₹ INR.",
                icon = Icons.Default.Explore,
                actionLabel = "Open Discover",
                onAction = { viewModel.navigateTo(AppNavDestination.DISCOVER) }
            )
        }

        item {
            TourStepCard(
                step = "Step 2",
                title = "AI Campaign Brief Builder",
                description = "Enter any raw creative thought (e.g. 'Campus plastic bottle ban reels') and let Gemini or our local template engine generate deliverables, budget, and toolsets.",
                icon = Icons.Default.AutoAwesome,
                actionLabel = "Try Brief Builder",
                onAction = { viewModel.navigateTo(AppNavDestination.AI_BRIEF_BUILDER) }
            )
        }

        item {
            TourStepCard(
                step = "Step 3",
                title = "Shortlist & Compare Matrix",
                description = "Star creators to build a candidate pool, then open the side-by-side comparison table to evaluate toolchains, turnaround times, and rates.",
                icon = Icons.Default.Star,
                actionLabel = "View Shortlist",
                onAction = { viewModel.navigateTo(AppNavDestination.SHORTLIST) }
            )
        }

        item {
            TourStepCard(
                step = "Step 4",
                title = "Nova EcoMatch — Impact Predictor",
                description = "Configure sustainability topics and budgets to estimate potential views, cost per thousand (CPM in ₹), and transparent eco-audience fit scores.",
                icon = Icons.Default.Eco,
                actionLabel = "Test EcoMatch",
                onAction = { viewModel.navigateTo(AppNavDestination.ECOMATCH) }
            )
        }

        item {
            TourStepCard(
                step = "Step 5",
                title = "8-Stage Engagement Tracker",
                description = "Simulate contract milestones from Discovered to Accepted, In Progress, Review, and Approval with clear notes and deliverable sign-offs.",
                icon = Icons.Default.Timeline,
                actionLabel = "Track Engagements",
                onAction = { viewModel.navigateTo(AppNavDestination.ENGAGEMENTS) }
            )
        }

        item {
            TourStepCard(
                step = "Step 6",
                title = "Creator Verification Center",
                description = "Audit tools, workflows, original models, and commercial licensing rights with immediate platform-wide synchronization.",
                icon = Icons.Default.VerifiedUser,
                actionLabel = "Open Verification",
                onAction = { viewModel.navigateTo(AppNavDestination.VERIFICATION) }
            )
        }
    }
}

@Composable
fun TourStepCard(
    step: String,
    title: String,
    description: String,
    icon: ImageVector,
    actionLabel: String,
    onAction: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                        text = step,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onAction,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(actionLabel)
            }
        }
    }
}
