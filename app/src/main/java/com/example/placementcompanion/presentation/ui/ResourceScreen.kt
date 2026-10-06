package com.example.placementcompanion.presentation.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.placementcompanion.presentation.viewmodel.ResourceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResourceScreen(
    viewModel: ResourceViewModel
) {
    val resources by viewModel.resources.collectAsState()
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedUri = uri
            showAddDialog = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Study Materials & PDF Resources") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { filePickerLauncher.launch("*/*") }) {
                Icon(Icons.Default.Add, contentDescription = "Add Resource")
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
                text = "Stored Study Materials (PDF, PPT, Notes)",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            if (resources.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No PDF/PPT resources added.", style = MaterialTheme.typography.titleMedium)
                        Text("Tap + to import documents from device storage.", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(resources) { res ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW).apply {
                                            setDataAndType(Uri.parse(res.fileUri), if (res.fileType.equals("PDF", ignoreCase = true)) "application/pdf" else "*/*")
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        // Handle no app found
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(
                                        imageVector = Icons.Default.PictureAsPdf,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(res.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        Text("${res.subject} • ${res.category} (${res.fileType})", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                IconButton(onClick = { viewModel.deleteResource(res.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog && selectedUri != null) {
        AddResourceDialog(
            uri = selectedUri!!,
            onDismiss = {
                showAddDialog = false
                selectedUri = null
            },
            onConfirm = { title, subject, category, type ->
                viewModel.addResource(title, selectedUri.toString(), type, subject, category)
                showAddDialog = false
                selectedUri = null
            }
        )
    }
}

@Composable
fun AddResourceDialog(
    uri: Uri,
    onDismiss: () -> Unit,
    onConfirm: (title: String, subject: String, category: String, type: String) -> Unit
) {
    var title by remember { mutableStateOf(uri.lastPathSegment ?: "New Document") }
    var subject by remember { mutableStateOf("DBMS") }
    var category by remember { mutableStateOf("Notes") }
    var fileType by remember { mutableStateOf("PDF") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Categorize Document") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Resource Title") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("Subject (DBMS, OS, DSA, etc.)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category (Notes, Cheatsheet, Syllabus)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = fileType, onValueChange = { fileType = it }, label = { Text("File Type (PDF, PPT, DOC)") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = { if (title.isNotBlank()) onConfirm(title, subject, category, fileType) }) {
                Text("Save Resource")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
