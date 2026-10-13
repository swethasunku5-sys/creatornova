package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CampaignBrief
import com.example.ui.MainViewModel
import com.example.ui.components.formatInr
import com.example.ui.theme.NovaAmber
import com.example.ui.theme.NovaEcoGreen

@Composable
fun AiBriefBuilderScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val ideaInput by viewModel.briefIdeaInput.collectAsState()
    val isGenerating by viewModel.isGeneratingBrief.collectAsState()
    val generatedBrief by viewModel.generatedBrief.collectAsState()
    val isLocalFallback by viewModel.isBriefLocalFallback.collectAsState()

    // Form editing state for generated brief
    var editableTitle by remember(generatedBrief) { mutableStateOf(generatedBrief?.title ?: "") }
    var editableBrand by remember(generatedBrief) { mutableStateOf(generatedBrief?.brandName ?: "") }
    var editableObjective by remember(generatedBrief) { mutableStateOf(generatedBrief?.objective ?: "") }
    var editableAudience by remember(generatedBrief) { mutableStateOf(generatedBrief?.targetAudience ?: "") }
    var editableContentType by remember(generatedBrief) { mutableStateOf(generatedBrief?.contentType ?: "") }
    var editableStyle by remember(generatedBrief) { mutableStateOf(generatedBrief?.creativeStyle ?: "") }
    var editableTools by remember(generatedBrief) { mutableStateOf(generatedBrief?.preferredAiTools ?: "") }
    var editableDeliverables by remember(generatedBrief) { mutableStateOf(generatedBrief?.deliverables ?: "") }
    var editableBudgetStr by remember(generatedBrief) { mutableStateOf(generatedBrief?.budgetInr?.toString() ?: "75000") }
    var editableDeadline by remember(generatedBrief) { mutableStateOf(generatedBrief?.deadline ?: "2026-11-20") }
    var editableTerms by remember(generatedBrief) { mutableStateOf(generatedBrief?.commercialTerms ?: "") }
    var editableSuccess by remember(generatedBrief) { mutableStateOf(generatedBrief?.successCriteria ?: "") }

    val samplePills = listOf(
        "Sustainability college campaign in India with reels",
        "Cyberpunk Diwali luxury apparel teaser",
        "Plant-based protein snack viral hooks",
        "Electric vehicle road trip documentary across Ladakh"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("ai_brief_builder_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Page Title & Introduction
        item {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AI Campaign Brief Builder",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Text(
                    text = "Convert rough creative concepts into complete, actionable campaign briefs in seconds.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // Input Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Describe your campaign idea",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = ideaInput,
                        onValueChange = { viewModel.briefIdeaInput.value = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("brief_idea_input"),
                        placeholder = { Text("e.g. Create a short sustainability awareness campaign for college students in India using engaging AI reels...") },
                        minLines = 3,
                        maxLines = 5,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Quick Inspiration Ideas:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        samplePills.forEach { pill ->
                            SuggestionChip(
                                onClick = { viewModel.briefIdeaInput.value = pill },
                                label = { Text(pill, fontSize = 11.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { viewModel.generateBriefFromIdea() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("generate_brief_button"),
                        shape = RoundedCornerShape(10.dp),
                        enabled = ideaInput.isNotBlank() && !isGenerating
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generating with AI...")
                        } else {
                            Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generate Structured Brief")
                        }
                    }
                }
            }
        }

        // Generated Brief Editor & Form
        if (generatedBrief != null) {
            item {
                if (isLocalFallback) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = NovaAmber.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = NovaAmber, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Local demo generator — no external AI model was called.",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                } else {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = NovaEcoGreen.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NovaEcoGreen, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Generated live with Gemini 3.5 Flash!",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = NovaEcoGreen
                            )
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Editable Structured Brief",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val text = "Title: $editableTitle\nBrand: $editableBrand\nObjective: $editableObjective\nBudget: ₹$editableBudgetStr\nDeliverables: $editableDeliverables"
                                        clipboard.setPrimaryClip(ClipData.newPlainText("CreatorNova Brief", text))
                                        Toast.makeText(context, "Brief copied to clipboard!", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy to Clipboard")
                                }
                                IconButton(
                                    onClick = { viewModel.generatedBrief.value = null }
                                ) {
                                    Icon(Icons.Default.RestartAlt, contentDescription = "Reset")
                                }
                            }
                        }

                        OutlinedTextField(
                            value = editableTitle,
                            onValueChange = { editableTitle = it },
                            label = { Text("Campaign Title") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = editableBrand,
                                onValueChange = { editableBrand = it },
                                label = { Text("Brand Name") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = editableBudgetStr,
                                onValueChange = { editableBudgetStr = it.filter { c -> c.isDigit() } },
                                label = { Text("Budget (₹ INR)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }

                        OutlinedTextField(
                            value = editableObjective,
                            onValueChange = { editableObjective = it },
                            label = { Text("Campaign Objective") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            maxLines = 4
                        )

                        OutlinedTextField(
                            value = editableAudience,
                            onValueChange = { editableAudience = it },
                            label = { Text("Target Audience") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = editableContentType,
                                onValueChange = { editableContentType = it },
                                label = { Text("Content Type") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = editableDeadline,
                                onValueChange = { editableDeadline = it },
                                label = { Text("Deadline") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }

                        OutlinedTextField(
                            value = editableTools,
                            onValueChange = { editableTools = it },
                            label = { Text("Suggested AI Tools") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = editableDeliverables,
                            onValueChange = { editableDeliverables = it },
                            label = { Text("Deliverables") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = editableTerms,
                            onValueChange = { editableTerms = it },
                            label = { Text("Commercial & Licensing Terms") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                val budget = editableBudgetStr.toLongOrNull() ?: 75000L
                                val finalBrief = CampaignBrief(
                                    id = "b_${System.currentTimeMillis()}",
                                    title = editableTitle,
                                    brandName = editableBrand,
                                    objective = editableObjective,
                                    targetAudience = editableAudience,
                                    contentType = editableContentType,
                                    creativeStyle = editableStyle,
                                    platform = "Instagram & YouTube",
                                    aspectRatio = "9:16",
                                    preferredAiTools = editableTools,
                                    deliverables = editableDeliverables,
                                    budgetInr = budget,
                                    deadline = editableDeadline,
                                    commercialTerms = editableTerms,
                                    additionalInstructions = "",
                                    successCriteria = editableSuccess,
                                    status = com.example.data.model.BriefStatus.PUBLISHED
                                )
                                viewModel.repository.addBrief(finalBrief)
                                Toast.makeText(context, "Brief saved to Campaign Briefs!", Toast.LENGTH_SHORT).show()
                                viewModel.navigateTo(com.example.data.model.AppNavDestination.BRIEFS)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("save_generated_brief_button"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Save Directly to Campaign Briefs")
                        }
                    }
                }
            }
        }
    }
}
