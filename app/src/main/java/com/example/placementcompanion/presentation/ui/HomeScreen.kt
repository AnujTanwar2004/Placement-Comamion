package com.example.placementcompanion.presentation.ui

import androidx.compose.foundation.background
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.placementcompanion.presentation.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToTasks: () -> Unit,
    onNavigateToNotes: () -> Unit,
    onNavigateToAi: () -> Unit,
    onNavigateToCompanies: () -> Unit,
    onNavigateToSearch: () -> Unit
) {
    val user by viewModel.user.collectAsState()
    val todayTasks by viewModel.todayTasks.collectAsState()
    val companies by viewModel.companies.collectAsState()

    val completedCount = todayTasks.count { it.isCompleted }
    val totalCount = todayTasks.size
    val progressFraction = if (totalCount > 0) completedCount.toFloat() / totalCount.toFloat() else 0f

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Hi, ${user?.name ?: "Student"} 👋",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "Target: ${user?.preferredRole ?: "SDE"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    AssistChip(
                        onClick = { },
                        label = { Text("$completedCount tasks done today") },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    IconButton(onClick = onNavigateToSearch) {
                        Icon(Icons.Default.Search, contentDescription = "Search & Saved")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Today's Progress Summary Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Today's Progress",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "$completedCount / $totalCount Completed",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = { progressFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Completed task duration: ${todayTasks.filter { it.isCompleted }.sumOf { it.durationMinutes }} planned min",
                                style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            // Quick Actions Bar
            item {
                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        QuickActionButton(
                            icon = Icons.Default.PlayArrow,
                            label = "Study",
                            onClick = onNavigateToTasks
                        )
                    }
                    item {
                        QuickActionButton(
                            icon = Icons.Default.AutoAwesome,
                            label = "AI Planner",
                            onClick = onNavigateToAi
                        )
                    }
                    item {
                        QuickActionButton(
                            icon = Icons.Default.EditNote,
                            label = "Add Note",
                            onClick = onNavigateToNotes
                        )
                    }
                    item {
                        QuickActionButton(
                            icon = Icons.Default.Business,
                            label = "Companies",
                            onClick = onNavigateToCompanies
                        )
                    }
                }
            }

            // Smart Recommendation Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = "Smart Recommendation",
                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Smart Recommendation",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = todayTasks.firstOrNull { !it.isCompleted }?.let { "Next task: ${it.title}" }
                                    ?: if (todayTasks.isEmpty()) "Add a study task to start tracking today's progress." else "All of today's tasks are complete.",
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // Today's Scheduled Tasks
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Today's Scheduled Tasks",
                        style = MaterialTheme.typography.titleMedium
                    )
                    TextButton(onClick = onNavigateToTasks) {
                        Text("View All")
                    }
                }
            }

            if (todayTasks.isEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.CheckCircleOutline,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No tasks scheduled for today yet.")
                            Button(onClick = onNavigateToTasks, modifier = Modifier.padding(top = 8.dp)) {
                                Text("Add Task")
                            }
                        }
                    }
                }
            } else {
                items(todayTasks, key = { it.id }) { task ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (task.isCompleted) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Checkbox(
                                    modifier = Modifier.semantics { contentDescription = if (task.isCompleted) "Mark incomplete: ${task.title}" else "Complete: ${task.title}" },
                                    checked = task.isCompleted,
                                    onCheckedChange = { checked ->
                                        viewModel.toggleTaskCompleted(task.id, checked)
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = task.title,
                                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                                        fontWeight = FontWeight.SemiBold,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        text = "${task.timeSlot} • ${task.subject}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            AssistChip(
                                onClick = { },
                                label = { Text(if (task.isCompleted) "Completed" else task.priority) }
                            )
                        }
                    }
                }
            }

            // Target Companies Highlights
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Target Companies",
                        style = MaterialTheme.typography.titleMedium
                    )
                    TextButton(onClick = onNavigateToCompanies) {
                        Text("See All")
                    }
                }
            }

            items(companies.take(3)) { company ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToCompanies() }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = company.name,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "${company.companyType} • ${company.difficulty} Difficulty",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Button(onClick = onNavigateToCompanies) {
                            Text("Prepare")
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun QuickActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(100.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.onPrimary)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
