package com.example.placementcompanion.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.LocalContext
import com.example.placementcompanion.utils.DataStoreManager
import kotlinx.coroutines.launch
import com.example.placementcompanion.data.database.CompanyEntity
import com.example.placementcompanion.data.database.QuestionEntity
import com.example.placementcompanion.presentation.viewmodel.CompanyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanyDetailScreen(
    companyId: Long,
    viewModel: CompanyViewModel,
    onBack: () -> Unit
) {
    var company by remember { mutableStateOf<CompanyEntity?>(null) }
    val questionsFlow = remember(companyId) { viewModel.getQuestionsForCompany(companyId) }
    val questions by questionsFlow.collectAsState(initial = emptyList())

    val context = LocalContext.current
    val settings = remember(context) { DataStoreManager(context) }
    val checklistFlow = remember(companyId) { settings.companyChecklist(companyId) }
    val completedItems by checklistFlow.collectAsState(initial = emptySet())
    val scope = rememberCoroutineScope()
    val defaultChecklist = remember(companyId, completedItems) {
        mutableStateListOf(
            "Resume finalized & tailored for role" to false,
            "Arrays & Strings revised" to false,
            "Trees & Graphs revised" to false,
            "Dynamic Programming practice" to false,
            "DBMS & SQL queries revised" to false,
            "Operating Systems & Networks concepts" to false,
            "20 Company PYQs completed" to false,
            "Mock coding test & timed assessment" to false,
            "HR Behavioral questions (STAR method)" to false
        ).apply { indices.forEach { i -> this[i] = this[i].first to (this[i].first in completedItems) } }
    }

    LaunchedEffect(companyId) {
        company = viewModel.getCompanyById(companyId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(company?.name ?: "Company Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        company?.let { comp ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Overview
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(comp.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                            Text(comp.companyType + " Company • " + comp.difficulty + " Difficulty", color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(comp.description, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }

                // Recruitment Process
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Recruitment Process", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            val steps = comp.recruitmentProcess.split("|")
                            steps.forEach { step ->
                                Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(step.trim(), style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }

                // Eligibility & Strategy
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Eligibility & Strategy", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Eligibility: ${comp.eligibilityCriteria}", style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Skills Expected: ${comp.expectedSkills}", style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Preparation Strategy:", fontWeight = FontWeight.SemiBold)
                            Text(comp.preparationStrategy, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                // Preparation Checklist
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            val completedCount = defaultChecklist.count { it.second }
                            Text("Company Preparation Checklist", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("$completedCount / ${defaultChecklist.size} Completed", style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            defaultChecklist.forEach { pair ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = pair.second,
                                        onCheckedChange = { checked ->
                                            scope.launch { settings.setChecklistItem(companyId, pair.first, checked) }
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(pair.first, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }

                // Associated PYQs
                item {
                    Text("Company interview questions (${questions.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                if (questions.isEmpty()) {
                    item {
                        Text("No matching questions in the offline snapshot yet.", style = MaterialTheme.typography.bodyMedium)
                    }
                } else {
                    items(questions) { q ->
                        QuestionCardItem(q)
                    }
                }
            }
        } ?: run {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun QuestionCardItem(q: QuestionEntity) {
    val uriHandler = LocalUriHandler.current
    var linkError by remember { mutableStateOf(false) }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(q.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                AssistChip(onClick = {}, label = { Text(q.difficulty) })
            }
            Text(q.description, style = MaterialTheme.typography.bodyMedium, maxLines = 3)
            Spacer(modifier = Modifier.height(4.dp))
            if (q.leetcodeId != null) {
                Text("LeetCode #${q.leetcodeId} · Snapshot ${q.snapshotDate}", style = MaterialTheme.typography.labelMedium)
            } else Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Topic: ${q.topic}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Text("Round: ${q.round}", style = MaterialTheme.typography.labelMedium)
                Text("Year: ${q.year}", style = MaterialTheme.typography.labelMedium)
            }
            Text("Source: ${q.source}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (q.solutionLink.isNotBlank()) TextButton(onClick = {
                try { uriHandler.openUri(q.solutionLink); linkError = false } catch (_: Exception) { linkError = true }
            }) { Text("Open in LeetCode") }
            if (linkError) Text("No browser available.", color = MaterialTheme.colorScheme.error)
        }
    }
}
