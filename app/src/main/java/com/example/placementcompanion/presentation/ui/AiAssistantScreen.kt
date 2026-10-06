package com.example.placementcompanion.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
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
fun AiAssistantScreen(
    viewModel: AiViewModel,
    onBack: () -> Unit
) {
    val chatHistory by viewModel.chatHistory.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var inputQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ask AI Doubt Assistant") },
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
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(chatHistory) { msg ->
                    val isUser = msg.sender == "User"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (!isUser) {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    Text(
                                        text = if (isUser) "You" else "Groq AI",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(msg.message, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }

                if (isLoading) {
                    item {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputQuery,
                    onValueChange = { inputQuery = it },
                    placeholder = { Text("Ask any question (e.g. Explain Deadlock)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (inputQuery.isNotBlank()) {
                            viewModel.askDoubt(inputQuery)
                            inputQuery = ""
                        }
                    },
                    enabled = !isLoading
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                }
            }
        }
    }
}
