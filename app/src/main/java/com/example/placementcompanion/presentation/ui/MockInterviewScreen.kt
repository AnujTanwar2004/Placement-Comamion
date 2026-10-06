package com.example.placementcompanion.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.placementcompanion.presentation.viewmodel.AiViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MockInterviewScreen(
    viewModel: AiViewModel,
    onBack: () -> Unit
) {
    val feedback by viewModel.mockFeedback.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var company by remember { mutableStateOf("Amazon") }
    var role by remember { mutableStateOf("SDE-1") }
    var round by remember { mutableStateOf("Technical Interview") }
    var question by remember { mutableStateOf("Explain how a Hash Map handles collisions under high load.") }
    var userAnswer by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Mock Interview Mode") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
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
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Simulated Interview Session", fontWeight = FontWeight.Bold)
                    }
                    Text("Answer questions one at a time. The AI will evaluate your accuracy and suggest improvements.")
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Interview Question", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(question, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)

                    OutlinedTextField(
                        value = userAnswer,
                        onValueChange = { userAnswer = it },
                        label = { Text("Your Explanation / Solution") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4
                    )

                    Button(
                        onClick = {
                            viewModel.submitMockAnswer(company, role, round, question, userAnswer)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading && userAnswer.isNotBlank()
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Text("Submit Answer for AI Evaluation")
                        }
                    }
                }
            }

            if (feedback.isNotBlank()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("AI Evaluation & Next Steps", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(feedback, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
