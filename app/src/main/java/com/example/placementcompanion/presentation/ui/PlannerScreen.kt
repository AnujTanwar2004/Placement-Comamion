package com.example.placementcompanion.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import java.time.LocalDate
import androidx.compose.ui.unit.dp
import com.example.placementcompanion.presentation.viewmodel.PlannerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlannerScreen(
    viewModel: PlannerViewModel,
    onNavigateToAiPlanner: () -> Unit
) {
    val allTasks by viewModel.allTasks.collectAsState()
    var showAddTaskDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Study Planner & Timetable") },
                actions = {
                    Button(onClick = onNavigateToAiPlanner, modifier = Modifier.padding(end = 8.dp)) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("AI Plan Mode")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddTaskDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Task")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Your Scheduled Preparation Schedule",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            if (allTasks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.EventNote, contentDescription = null, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No tasks added yet.", style = MaterialTheme.typography.titleMedium)
                        Text("Tap + to add a study task or use AI Plan Mode.", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(allTasks, key = { it.id }) { task ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (task.isCompleted) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Checkbox(
                                        modifier = Modifier.semantics { contentDescription = if (task.isCompleted) "Mark incomplete: ${task.title}" else "Complete: ${task.title}" },
                                        checked = task.isCompleted,
                                        onCheckedChange = { viewModel.toggleTaskCompleted(task.id, it) }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = task.title,
                                            textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Text(
                                            text = "${task.subject} • ${task.scheduledDate} (${task.timeSlot})",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (task.isAiGenerated) {
                                            Text(
                                                text = "✨ AI Generated Plan",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                                IconButton(onClick = { viewModel.deleteTask(task.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddTaskDialog) {
        AddTaskDialog(
            onDismiss = { showAddTaskDialog = false },
            onConfirm = { title, subject, company, date, timeSlot, priority ->
                viewModel.addTask(
                    title = title,
                    subject = subject,
                    companyName = company,
                    scheduledDate = date,
                    timeSlot = timeSlot,
                    priority = priority
                )
                showAddTaskDialog = false
            }
        )
    }
}

@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, subject: String, company: String, date: String, timeSlot: String, priority: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("DSA") }
    var company by remember { mutableStateOf("Amazon") }
    var date by remember { mutableStateOf(LocalDate.now().toString()) }
    var timeSlot by remember { mutableStateOf("07:00 PM - 08:00 PM") }
    var priority by remember { mutableStateOf("High") }
    val dateValid = runCatching { LocalDate.parse(date) }.isSuccess

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Study Task") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title (e.g. Arrays Revision)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject (DSA, DBMS, OS, etc.)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = company,
                    onValueChange = { company = it },
                    label = { Text("Target Company (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date (YYYY-MM-DD)") },
                    isError = !dateValid,
                    supportingText = { if (!dateValid) Text("Enter a valid date in YYYY-MM-DD format") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = timeSlot,
                    onValueChange = { timeSlot = it },
                    label = { Text("Time Slot") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                enabled = title.isNotBlank() && subject.isNotBlank() && timeSlot.isNotBlank() && dateValid,
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(title, subject, company, date, timeSlot, priority)
                    }
                }
            ) {
                Text("Add Task")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
