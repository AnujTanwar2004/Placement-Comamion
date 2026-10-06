package com.example.placementcompanion.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.placementcompanion.presentation.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onNavigateToPyqs: () -> Unit,
    onNavigateToExperiences: () -> Unit,
    onNavigateToNotes: () -> Unit,
    onNavigateToAiDoubt: () -> Unit,
    onNavigateToMockInterview: () -> Unit,
    onResetOnboarding: () -> Unit
) {
    val user by viewModel.user.collectAsState()
    val progress by viewModel.progress.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Student Profile & Analytics") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // User Information Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(user?.name ?: "Student", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text("${user?.course} • ${user?.college}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AssistChip(onClick = {}, label = { Text("Target: ${user?.preferredRole}") })
                        AssistChip(onClick = {}, label = { Text("${user?.dailyStudyHours} hrs/day") })
                    }
                    Text("Target Companies: ${user?.targetCompanies}", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 4.dp))
                }
            }

            // Placement Readiness Dashboard
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Preparation Readiness", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("${progress.percent}% Overall", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(progress = { progress.fraction }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Study tasks: ${progress.completedTasks} / ${progress.totalTasks} completed")
                    Text("Distinct PYQs solved: ${progress.solvedQuestions} / ${progress.questionGoal} practice goal")
                    progress.subjects.forEach { subject ->
                        Text("${subject.subject}: ${subject.completed} / ${subject.total} tasks completed",
                            style = MaterialTheme.typography.bodyMedium)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Score = 50% study-plan completion + 50% progress towards 20 distinct PYQs (capped at 20). The same LeetCode problem across companies counts once. No tasks means 0 for the study-plan part.",
                        style = MaterialTheme.typography.bodySmall)
                    Text("Tracks preparation activity; it does not measure interview performance.",
                        style = MaterialTheme.typography.bodySmall)
                }
            }

            // Navigation Sections
            Text("Placement Utilities", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            OutlinedButton(onClick = onNavigateToPyqs, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.AutoMirrored.Filled.HelpOutline, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Company PYQs & Questions")
            }

            OutlinedButton(onClick = onNavigateToExperiences, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.RecordVoiceOver, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Interview Experiences")
            }

            OutlinedButton(onClick = onNavigateToNotes, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.EditNote, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("My Notes & Summaries")
            }

            OutlinedButton(onClick = onNavigateToAiDoubt, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ask AI Doubt Assistant")
            }

            OutlinedButton(onClick = onNavigateToMockInterview, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Psychology, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("AI Mock Interview Mode")
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = { viewModel.resetOnboarding(onResetOnboarding) },
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Reset Onboarding & Preferences")
            }
        }
    }
}
