package com.example.placementcompanion.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.placementcompanion.presentation.viewmodel.OnboardingViewModel

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onFinish: () -> Unit
) {
    val step by viewModel.step.collectAsState()
    val name by viewModel.name.collectAsState()
    val selectedRole by viewModel.selectedRole.collectAsState()
    val selectedSubjects by viewModel.selectedSubjects.collectAsState()
    val targetCompanies by viewModel.targetCompanies.collectAsState()
    val dailyHours by viewModel.dailyHours.collectAsState()

    val roles = listOf("Software Engineer", "Data Analyst", "Data Scientist", "QA / Automation", "DevOps", "Core Engineer")
    val subjectsList = listOf("DSA", "DBMS", "Operating Systems", "Computer Networks", "OOP", "System Design", "Aptitude", "Logical Reasoning")

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Placement Companion",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { step / 4f },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(24.dp))

                when (step) {
                    1 -> {
                        Text("Welcome! What's your name?", style = MaterialTheme.typography.headlineMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Let's personalize your placement preparation experience.", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(24.dp))
                        OutlinedTextField(
                            value = name,
                            onValueChange = { viewModel.updateName(it) },
                            label = { Text("Your Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                    2 -> {
                        Text("Select Target Role", style = MaterialTheme.typography.headlineMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Choose your primary placement role objective.", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(16.dp))
                        roles.forEach { role ->
                            FilterChip(
                                selected = selectedRole == role,
                                onClick = { viewModel.updateRole(role) },
                                label = { Text(role) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            )
                        }
                    }
                    3 -> {
                        Text("Select Preparation Subjects", style = MaterialTheme.typography.headlineMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Choose subjects to include in your daily timetable.", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(16.dp))
                        subjectsList.forEach { subj ->
                            FilterChip(
                                selected = selectedSubjects.contains(subj),
                                onClick = { viewModel.toggleSubject(subj) },
                                label = { Text(subj) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            )
                        }
                    }
                    4 -> {
                        Text("Target Companies & Study Hours", style = MaterialTheme.typography.headlineMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Specify target companies and your daily study hours.", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = targetCompanies,
                            onValueChange = { viewModel.updateTargetCompanies(it) },
                            label = { Text("Target Companies (comma separated)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Daily Preparation Time: $dailyHours Hours", fontWeight = FontWeight.SemiBold)
                        Slider(
                            value = dailyHours.toFloat(),
                            onValueChange = { viewModel.updateDailyHours(it.toInt()) },
                            valueRange = 1f..10f,
                            steps = 8
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (step > 1) {
                    OutlinedButton(onClick = { viewModel.prevStep() }) {
                        Text("Back")
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Button(
                    onClick = {
                        if (step < 4) {
                            viewModel.nextStep()
                        } else {
                            viewModel.completeOnboarding(onFinish)
                        }
                    }
                ) {
                    Text(if (step < 4) "Continue" else "Get Started")
                }
            }
        }
    }
}
