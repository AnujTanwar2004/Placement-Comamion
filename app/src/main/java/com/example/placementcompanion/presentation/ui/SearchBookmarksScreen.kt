package com.example.placementcompanion.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalUriHandler
import com.example.placementcompanion.presentation.viewmodel.SearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBookmarksScreen(
    viewModel: SearchViewModel,
    onBack: () -> Unit,
    onBrowseQuestions: (String) -> Unit = {}
) {
    val uriHandler = LocalUriHandler.current
    var linkError by remember { mutableStateOf(false) }
    val searchQuery by viewModel.searchQuery.collectAsState()
    val companyResults by viewModel.companyResults.collectAsState()
    val questionResults by viewModel.questionResults.collectAsState()
    val noteResults by viewModel.noteResults.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Global Search") },
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
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Search across companies, PYQs, and notes...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))
            if (linkError) Text("No browser available.", color = MaterialTheme.colorScheme.error)

            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (searchQuery.isNotBlank()) item {
                    OutlinedButton(onClick = { onBrowseQuestions(searchQuery) }, modifier = Modifier.fillMaxWidth()) {
                        Text("Browse all matching company questions")
                    }
                }
                if (companyResults.isNotEmpty()) {
                    item {
                        Text("Companies (${companyResults.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    items(companyResults) { comp ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(comp.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Text("${comp.companyType} • ${comp.hiringRoles}", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }

                if (questionResults.isNotEmpty()) {
                    item {
                        Text("Questions / PYQs (${questionResults.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    items(questionResults) { q ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(q.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Text("${q.companyName} • ${q.topic} (${q.difficulty})", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                                if (q.solutionLink.isNotBlank()) TextButton(onClick = {
                                    try { uriHandler.openUri(q.solutionLink); linkError = false } catch (_: Exception) { linkError = true }
                                }) { Text("Open in LeetCode") }
                            }
                        }
                    }
                }

                if (noteResults.isNotEmpty()) {
                    item {
                        Text("Notes (${noteResults.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    items(noteResults) { note ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(note.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Text(note.content, style = MaterialTheme.typography.bodyMedium, maxLines = 2)
                            }
                        }
                    }
                }
            }
        }
    }
}
