package com.example.placementcompanion.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.placementcompanion.presentation.viewmodel.NoteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteScreen(
    viewModel: NoteViewModel,
    onBack: () -> Unit
) {
    val notes by viewModel.notes.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Placement Notes") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Note")
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
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Search notes by title or subject...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (notes.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No notes found. Tap + to create one.")
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(notes) { note ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (note.isPinned) {
                                            Icon(Icons.Default.PushPin, contentDescription = "Pinned", tint = MaterialTheme.colorScheme.primary)
                                            Spacer(modifier = Modifier.width(4.dp))
                                        }
                                        Text(note.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    }
                                    IconButton(onClick = { viewModel.deleteNote(note.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                                Text(note.content, style = MaterialTheme.typography.bodyMedium)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    AssistChip(onClick = {}, label = { Text(note.subject) })
                                    if (note.companyName.isNotBlank()) {
                                        AssistChip(onClick = {}, label = { Text(note.companyName) })
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
        AddNoteDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, content, subject, company, pinned ->
                viewModel.addNote(title, content, subject, company, pinned)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun AddNoteDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, content: String, subject: String, company: String, isPinned: Boolean) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("DSA") }
    var company by remember { mutableStateOf("") }
    var isPinned by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Study Note") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Note Title") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = content, onValueChange = { content = it }, label = { Text("Content / Formula / Summary") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("Subject (e.g. OS, DBMS, DSA)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = company, onValueChange = { company = it }, label = { Text("Company (Optional)") }, modifier = Modifier.fillMaxWidth())
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isPinned, onCheckedChange = { isPinned = it })
                    Text("Pin Note to Top")
                }
            }
        },
        confirmButton = {
            Button(onClick = { if (title.isNotBlank()) onConfirm(title, content, subject, company, isPinned) }) {
                Text("Save Note")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
