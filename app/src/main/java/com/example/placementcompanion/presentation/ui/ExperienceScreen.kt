package com.example.placementcompanion.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.placementcompanion.presentation.viewmodel.ExperienceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExperienceScreen(
    viewModel: ExperienceViewModel,
    onBack: () -> Unit
) {
    val experiences by viewModel.experiences.collectAsState()
    val searchCompany by viewModel.searchCompany.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Interview Experiences") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Contribute Experience")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = searchCompany,
                onValueChange = { viewModel.setSearchCompany(it) },
                placeholder = { Text("Search by company or role...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (experiences.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No interview experiences submitted yet.")
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(experiences) { exp ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(exp.companyName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                        Text("${exp.role} • ${exp.studentName} (${exp.college})", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    AssistChip(
                                        onClick = {},
                                        label = { Text(exp.result) },
                                        colors = AssistChipDefaults.assistChipColors(containerColor = if (exp.result.equals("Selected", ignoreCase = true)) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(exp.experienceText, style = MaterialTheme.typography.bodyLarge)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Topics Asked: ${exp.topicsAsked}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Rounds: ${exp.numberOfRounds} • Difficulty: ${exp.difficulty}", style = MaterialTheme.typography.labelMedium)
                                    IconButton(onClick = { viewModel.upvote(exp.id) }) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.ThumbUp, contentDescription = "Upvote", modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("${exp.helpfulVotes}")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddExperienceDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { company, role, name, college, result, rounds, diff, text, topics ->
                viewModel.addExperience(company, role, name, college, result, rounds, diff, text, topics)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun AddExperienceDialog(
    onDismiss: () -> Unit,
    onConfirm: (company: String, role: String, name: String, college: String, result: String, rounds: Int, difficulty: String, text: String, topics: String) -> Unit
) {
    var company by remember { mutableStateOf("Amazon") }
    var role by remember { mutableStateOf("SDE-1") }
    var name by remember { mutableStateOf("Anonymous") }
    var college by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("Selected") }
    var text by remember { mutableStateOf("") }
    var topics by remember { mutableStateOf("Arrays, Trees, System Design") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Contribute Interview Experience") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = company, onValueChange = { company = it }, label = { Text("Company Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = role, onValueChange = { role = it }, label = { Text("Role") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Your Name or Anonymous") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = text, onValueChange = { text = it }, label = { Text("Detailed Round-by-Round Experience") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                OutlinedTextField(value = topics, onValueChange = { topics = it }, label = { Text("Key Topics Asked") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = { if (company.isNotBlank() && text.isNotBlank()) onConfirm(company, role, name, college, result, 3, "Medium", text, topics) }) {
                Text("Submit Experience")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
