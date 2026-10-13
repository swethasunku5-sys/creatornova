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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.EcoCreatorMetric
import com.example.ui.MainViewModel
import com.example.ui.components.formatInr
import com.example.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

fun formatNumber(number: Long): String {
    val formatter = NumberFormat.getNumberInstance(Locale.US)
    return formatter.format(number)
}

@Composable
fun NovaEcoMatchScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val config by viewModel.ecoConfig.collectAsState()
    val analysis by viewModel.ecoMatchAnalysis.collectAsState()
    val allCreators by viewModel.repository.creators.collectAsState()

    var showExplanationModal by remember { mutableStateOf(false) }

    val presetTopics = listOf(
        "Zero-Waste Urban Living & Reusable Essentials",
        "Solar & Clean Energy Adoption in Rural Towns",
        "Circular Fashion & Upcycling Textiles",
        "River Cleanliness & Water Conservation"
    )

    BoxWithConstraints(modifier = modifier.fillMaxSize().testTag("nova_ecomatch_screen")) {
        val isWide = maxWidth >= 840.dp

        if (isWide) {
            // Desktop / Laptop 2-Panel Split Layout
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Left Panel (~420dp): Campaign Configuration
                Card(
                    modifier = Modifier
                        .width(420.dp)
                        .fillMaxHeight(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Eco, contentDescription = null, tint = NovaEcoGreen, modifier = Modifier.size(22.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Campaign Inputs", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                }
                                TextButton(onClick = { showExplanationModal = true }) {
                                    Icon(Icons.Default.HelpOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Formulas", fontSize = 11.sp)
                                }
                            }
                        }

                        // Topic selection
                        item {
                            Text("Campaign Topic Presets", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            presetTopics.forEach { t ->
                                FilterChip(
                                    selected = config.topic == t,
                                    onClick = { viewModel.updateEcoConfig { it.copy(topic = t) } },
                                    label = { Text(t, fontSize = 11.sp) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        // Budget & Content items
                        item {
                            OutlinedTextField(
                                value = config.totalBudgetInr.toString(),
                                onValueChange = { str ->
                                    val b = str.filter { it.isDigit() }.toLongOrNull() ?: 10000L
                                    viewModel.updateEcoConfig { it.copy(totalBudgetInr = b) }
                                },
                                label = { Text("Total Campaign Budget (₹ INR)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = config.plannedItemsPerCreator.toString(),
                                onValueChange = { str ->
                                    val count = str.filter { it.isDigit() }.toIntOrNull() ?: 1
                                    viewModel.updateEcoConfig { it.copy(plannedItemsPerCreator = count.coerceIn(1, 10)) }
                                },
                                label = { Text("Planned Content Items / Creator") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }

                        // Creator selection
                        item {
                            Text(
                                text = "Selected Creators (${config.selectedCreatorIds.size}):",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            allCreators.forEach { creator ->
                                val isChecked = config.selectedCreatorIds.contains(creator.id)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = { checked ->
                                            val newSet = if (checked) config.selectedCreatorIds + creator.id else config.selectedCreatorIds - creator.id
                                            viewModel.updateEcoConfig { it.copy(selectedCreatorIds = newSet) }
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(creator.name, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                        Text("${creator.specialization} • ${formatInr(creator.indicativeRateInr)}", style = MaterialTheme.typography.bodySmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Text("${creator.sustainabilityFitScore}%", color = NovaEcoGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Right Panel: Live Impact Forecast & Rankings
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header Banner
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Brush.linearGradient(
                                            listOf(NovaEcoGreen.copy(alpha = 0.15f), NovaSecondary.copy(alpha = 0.08f), Color.Transparent)
                                        )
                                    )
                                    .padding(18.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "Live Sustainability Forecast",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "Topic: ${config.topic}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    // 4 KPI Cards in a 2x2 grid
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("Est. Impressions", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = formatNumber(analysis.totalImpressions),
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text("Total audience exposure", style = MaterialTheme.typography.bodySmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("Est. CPM (₹ INR)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    val cpmText = if (analysis.overallCpmInr != null) "₹${String.format(Locale.US, "%.1f", analysis.overallCpmInr)}" else "Insufficient data"
                                    Text(
                                        text = cpmText,
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                        color = NovaEcoGreen
                                    )
                                    Text("Cost per 1,000 views", style = MaterialTheme.typography.bodySmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("Budget Utilization", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = formatInr(analysis.totalAllocatedCostInr),
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                        color = if (analysis.isOverBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text("${String.format(Locale.US, "%.1f", analysis.budgetUtilizationPercent)}% utilized", style = MaterialTheme.typography.bodySmall, fontSize = 10.sp)
                                }
                            }

                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("Scenario Actions", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = formatNumber(analysis.totalPotentialActions),
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                        color = NovaSecondary
                                    )
                                    Text("At 4% assumed rate", style = MaterialTheme.typography.bodySmall, fontSize = 10.sp)
                                }
                            }
                        }
                    }

                    if (analysis.isOverBudget) {
                        item {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.errorContainer
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Over Budget Alert: Allocated costs exceed total budget by ${formatInr(analysis.totalAllocatedCostInr - config.totalBudgetInr)}.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = "Creator Rankings & Rationale (${analysis.topCreatorMetrics.size} Evaluated)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    items(analysis.topCreatorMetrics) { metric ->
                        EcoCreatorCard(metric = metric)
                    }
                }
            }
        } else {
            // Mobile Compact View (Original stacked layout)
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            NovaEcoGreen.copy(alpha = 0.18f),
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
                                    Icon(Icons.Default.Eco, contentDescription = null, tint = NovaEcoGreen, modifier = Modifier.size(24.dp))
                                    Text("Nova EcoMatch — Impact Predictor", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold))
                                }
                                Text(
                                    text = "Transparent creator comparison and environmental awareness scenario forecasting for sustainable brand marketing.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }

                item {
                    Text("Campaign Impact Forecast", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("Est. Impressions", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = formatNumber(analysis.totalImpressions), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                                Text("Expected reach", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("Est. CPM (₹)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(4.dp))
                                val cpmText = if (analysis.overallCpmInr != null) "₹${String.format(Locale.US, "%.1f", analysis.overallCpmInr)}" else "Insufficient data"
                                Text(text = cpmText, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = NovaEcoGreen)
                                Text("Per 1,000 views", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("Budget Allocated", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = formatInr(analysis.totalAllocatedCostInr),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (analysis.isOverBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${String.format(Locale.US, "%.1f", analysis.budgetUtilizationPercent)}% of ${formatInr(config.totalBudgetInr)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = if (analysis.isOverBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("Illustrative Actions", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = formatNumber(analysis.totalPotentialActions), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = NovaSecondary)
                                Text("At ${(config.assumedActionRate * 100).toInt()}% scenario rate", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                if (analysis.isOverBudget) {
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.errorContainer
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Warning: Allocated costs exceed total budget by ${formatInr(analysis.totalAllocatedCostInr - config.totalBudgetInr)}.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }

                // Inputs
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Campaign Configuration", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                TextButton(onClick = { showExplanationModal = true }) {
                                    Icon(Icons.Default.HelpOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Formula Guide", fontSize = 12.sp)
                                }
                            }

                            Text("Select Sustainability Campaign Topic:", style = MaterialTheme.typography.labelSmall)
                            Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                presetTopics.forEach { t ->
                                    FilterChip(
                                        selected = config.topic == t,
                                        onClick = { viewModel.updateEcoConfig { it.copy(topic = t) } },
                                        label = { Text(t, fontSize = 11.sp) }
                                    )
                                }
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                OutlinedTextField(
                                    value = config.totalBudgetInr.toString(),
                                    onValueChange = { str ->
                                        val b = str.filter { it.isDigit() }.toLongOrNull() ?: 10000L
                                        viewModel.updateEcoConfig { it.copy(totalBudgetInr = b) }
                                    },
                                    label = { Text("Budget (₹ INR)") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = config.plannedItemsPerCreator.toString(),
                                    onValueChange = { str ->
                                        val count = str.filter { it.isDigit() }.toIntOrNull() ?: 1
                                        viewModel.updateEcoConfig { it.copy(plannedItemsPerCreator = count.coerceIn(1, 10)) }
                                    },
                                    label = { Text("Items / Creator") },
                                    modifier = Modifier.width(130.dp),
                                    singleLine = true
                                )
                            }

                            Text("Select Participating Creators (${config.selectedCreatorIds.size}):", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                            allCreators.forEach { creator ->
                                val isChecked = config.selectedCreatorIds.contains(creator.id)
                                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = { checked ->
                                            val newSet = if (checked) config.selectedCreatorIds + creator.id else config.selectedCreatorIds - creator.id
                                            viewModel.updateEcoConfig { it.copy(selectedCreatorIds = newSet) }
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(creator.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                        Text("${creator.specialization} • ${formatInr(creator.indicativeRateInr)}", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Text("${creator.sustainabilityFitScore}% Fit", color = NovaEcoGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                item {
                    Text("Creator Ranking & Suitability Rationale", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                }

                items(analysis.topCreatorMetrics) { metric ->
                    EcoCreatorCard(metric = metric)
                }
            }
        }
    }

    if (showExplanationModal) {
        AlertDialog(
            onDismissRequest = { showExplanationModal = false },
            title = { Text("Nova EcoMatch Methodological Guide") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("A. Potential Reach Formula:", fontWeight = FontWeight.Bold)
                    Text("Estimated Impressions = Expected Views per Item × Planned Items per Creator.\nAssumes partial audience overlap when combining multiple creators.", style = MaterialTheme.typography.bodySmall)

                    HorizontalDivider()

                    Text("B. Estimated CPM:", fontWeight = FontWeight.Bold)
                    Text("CPM = (Allocated Cost ÷ Estimated Impressions) × 1,000 (in ₹ INR per 1,000 views). Handled safely without division by zero.", style = MaterialTheme.typography.bodySmall)

                    HorizontalDivider()

                    Text("C. Sustainability Fit Score (0-100):", fontWeight = FontWeight.Bold)
                    Text("Transparent score based on declared environmental interests, past green campaign track records, and relevant content classification. Note: This is an estimated campaign suitability score, not a scientifically measured outcome.", style = MaterialTheme.typography.bodySmall)

                    HorizontalDivider()

                    Text("E. Illustrative Eco-Awareness Actions:", fontWeight = FontWeight.Bold)
                    Text("Illustrative Actions = Impressions × Assumed Awareness-Action Rate (default 4%).\nClear Disclosure: Scenario forecast based on user assumptions; impressions do not prove physical carbon savings or guaranteed real-world behavior change.", style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                Button(onClick = { showExplanationModal = false }) { Text("Got It") }
            }
        )
    }
}

@Composable
fun EcoCreatorCard(
    metric: EcoCreatorMetric,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
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
                Column {
                    Text(
                        text = metric.creator.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = metric.creator.specialization,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = NovaEcoGreen.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "${metric.ecoFitScore}/100 Match",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = NovaEcoGreen,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Metrics row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Cost", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(formatInr(metric.allocatedCostInr), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Column {
                    Text("Est. Views", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(formatNumber(metric.estimatedImpressions), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Column {
                    Text("CPM (₹)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    val cpm = if (metric.estimatedCpmInr != null) "₹${String.format(Locale.US, "%.1f", metric.estimatedCpmInr)}" else "N/A"
                    Text(cpm, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NovaEcoGreen)
                }
                Column {
                    Text("Actions", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(formatNumber(metric.potentialEcoActions), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NovaSecondary)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Rationale & Strengths
            Text(
                text = "Ranking Rationale: ${metric.rankExplanation}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Strengths: ${metric.strengths}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
            Text(
                text = "Considerations: ${metric.limitations}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
        }
    }
}
