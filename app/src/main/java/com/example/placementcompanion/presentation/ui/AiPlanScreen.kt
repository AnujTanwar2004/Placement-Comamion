package com.example.placementcompanion.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.placementcompanion.presentation.viewmodel.AiPlanState
import com.example.placementcompanion.presentation.viewmodel.PlannerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiPlanScreen(
    viewModel: PlannerViewModel,
    onBack: () -> Unit
) {
    val aiState by viewModel.aiPlanState.collectAsState()

    var targetCompany by remember { mutableStateOf("Amazon") }
    var daysRemaining by remember { mutableStateOf("30") }
    var hoursPerDay by remember { mutableStateOf("3") }
    var skillLevel by remember { mutableStateOf("Intermediate") }
    var weakTopicsText by remember { mutableStateOf("Dynamic Programming, Operating Systems") }
    var strongTopicsText by remember { mutableStateOf("Arrays, DBMS, Strings") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Plan Generator (Groq API)") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Groq AI Powered Roadmap", fontWeight = FontWeight.Bold)
                            Text(
                                "Generate a customized weekly preparation roadmap for your target company and convert it directly into daily tasks.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Preparation Parameters", style = MaterialTheme.typography.titleMedium)

                        OutlinedTextField(
                            value = targetCompany,
                            onValueChange = { targetCompany = it },
                            label = { Text("Target Company (e.g. Amazon, Google, Microsoft)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = daysRemaining,
                                onValueChange = { daysRemaining = it },
                                label = { Text("Days Left") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = hoursPerDay,
                                onValueChange = { hoursPerDay = it },
                                label = { Text("Hours / Day") },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        OutlinedTextField(
                            value = weakTopicsText,
                            onValueChange = { weakTopicsText = it },
                            label = { Text("Weak Topics (comma separated)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = strongTopicsText,
                            onValueChange = { strongTopicsText = it },
                            label = { Text("Strong Topics (comma separated)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                viewModel.generateAiPlan(
                                    company = targetCompany,
                                    daysRemaining = daysRemaining.toIntOrNull() ?: 30,
                                    hoursPerDay = hoursPerDay.toIntOrNull() ?: 3,
                                    level = skillLevel,
                                    weakTopics = weakTopicsText.split(",").map { it.trim() },
                                    strongTopics = strongTopicsText.split(",").map { it.trim() }
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = aiState !is AiPlanState.Loading
                        ) {
                            if (aiState is AiPlanState.Loading) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Generating Roadmap with Groq...")
                            } else {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Generate AI Study Plan")
                            }
                        }
                    }
                }
            }

            when (val state = aiState) {
                is AiPlanState.Error -> {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                        ) {
                            Text(
                                text = "Error: ${state.message}",
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
                is AiPlanState.Success -> {
                    val plan = state.plan
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Generated Roadmap for ${plan.company}", style = MaterialTheme.typography.titleMedium)
                            Button(
                                onClick = {
                                    viewModel.convertAiPlanToTasks(plan)
                                    onBack()
                                }
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Save to Planner")
                            }
                        }
                    }

                    items(plan.weeks) { week ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Week ${week.week}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Focus Topics: ${week.topics.joinToString(", ")}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(8.dp))
                                week.tasks.forEach { task ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("• ${task.title}", modifier = Modifier.weight(1f))
                                        AssistChip(onClick = {}, label = { Text(task.subject) })
                                    }
                                }
                            }
                        }
                    }
                }
                else -> {}
            }
        }
    }
}
