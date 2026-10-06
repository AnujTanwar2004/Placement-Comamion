package com.example.placementcompanion.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.placementcompanion.data.database.QuestionEntity
import com.example.placementcompanion.presentation.viewmodel.PyqViewModel
import com.example.placementcompanion.repository.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PyqScreen(viewModel: PyqViewModel, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    val filters by viewModel.filters.collectAsState()
    val companies by viewModel.companies.collectAsState()
    var showFilters by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current
    var linkError by remember { mutableStateOf<String?>(null) }
    fun open(url: String) { try { uriHandler.openUri(url); linkError = null } catch (_: Exception) { linkError = "No browser available to open this link." } }
    Scaffold(topBar = { TopAppBar(title = { Text("Company questions / PYQs") }, navigationIcon = {
        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
    }) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
            OutlinedTextField(value = filters.query, onValueChange = viewModel::setSearchQuery,
                placeholder = { Text("Search company or question (e.g. Amazon)") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = { if (filters.query.isNotEmpty()) IconButton(onClick = { viewModel.setSearchQuery("") }) { Icon(Icons.Default.Close, "Clear search") } },
                modifier = Modifier.fillMaxWidth(), singleLine = true)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                item { FilterChip(selected = showFilters, onClick = { showFilters = !showFilters }, label = { Text("Filters") }, leadingIcon = { Icon(Icons.Default.FilterList, null) }) }
                item { FilterChip(selected = filters.savedOnly, onClick = { viewModel.setSavedOnly(!filters.savedOnly) }, label = { Text("Bookmarked") }) }
                item { TextButton(onClick = viewModel::clearFilters) { Text("Reset") } }
            }
            TabRow(selectedTabIndex = if (filters.completion == QuestionCompletion.COMPLETED) 1 else 0) {
                Tab(selected = filters.completion == QuestionCompletion.PENDING,
                    onClick = { viewModel.setCompletion(QuestionCompletion.PENDING) }, text = { Text("To solve") })
                Tab(selected = filters.completion == QuestionCompletion.COMPLETED,
                    onClick = { viewModel.setCompletion(QuestionCompletion.COMPLETED) }, text = { Text("Completed") })
            }
            Text("Tap the circle to mark a question complete. Find it in Completed; uncheck to practise again.",
                style = MaterialTheme.typography.labelSmall)
            if (showFilters) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CompanyQuestionPicker(companies, filters.company, viewModel::setCompany, Modifier.weight(1f))
                    QuestionPicker(filters.sort.label, QuestionSort.entries.map { it.label }, { label -> viewModel.setSort(QuestionSort.entries.first { it.label == label }) }, Modifier.weight(1f))
                }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { items(QuestionWindow.entries.toList()) { window ->
                    FilterChip(selected = filters.window == window, onClick = { viewModel.setWindow(window) }, label = { Text(window.label) })
                } }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    QuestionPicker("Difficulty: ${filters.difficulty}", listOf("All", "Easy", "Medium", "Hard"), viewModel::setDifficulty, Modifier.weight(1f))
                    QuestionPicker("Topic: ${filters.topic}", listOf("All", "Coding", "Arrays", "Trees", "Graphs", "System Design", "Aptitude", "DBMS", "OS"), viewModel::setTopic, Modifier.weight(1f))
                }
            }
            state.catalog?.let { Text("${it.companyCount} companies · Offline · Snapshot ${it.snapshotDate}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            Text("Community interview practice; exact interview dates and rounds are not supplied.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            linkError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall) }
            Spacer(Modifier.height(8.dp))
            when {
                state.loading -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) { CircularProgressIndicator(); Text("Loading offline questions…") }
                }
                state.error != null -> Text("Unable to load questions: ${state.error}", color = MaterialTheme.colorScheme.error)
                else -> {
                    Text("${state.total} matching questions · Showing ${state.questions.size}", style = MaterialTheme.typography.labelMedium)
                    LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (state.questions.isEmpty()) item { Text(if (filters.completion == QuestionCompletion.COMPLETED)
                            "No completed questions match these filters yet." else "No questions left to solve with these filters. Check Completed or reset filters.", Modifier.padding(vertical = 24.dp)) }
                        items(state.questions, key = { it.id }) { q -> PyqQuestionCard(q, filters.window,
                            { viewModel.toggleSolved(q.id, q.isSolved) }, { viewModel.toggleBookmark(q.id, q.isBookmarked) }, { open(q.solutionLink) }) }
                        if (state.questions.size < state.total) item { OutlinedButton(onClick = viewModel::loadMore, modifier = Modifier.fillMaxWidth()) { Text("Load more questions") } }
                        item { TextButton(onClick = { open("https://github.com/snehasishroy/leetcode-companywise-interview-questions") }) { Text("Dataset source on GitHub") } }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuestionPicker(label: String, values: List<String>, onChoose: (String) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier) {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) { Text(label, maxLines = 1) }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            values.forEach { value -> DropdownMenuItem(text = { Text(value) }, onClick = { onChoose(value); expanded = false }) }
        }
    }
}

@Composable
private fun CompanyQuestionPicker(companies: List<String>, selected: String, onChoose: (String) -> Unit, modifier: Modifier) {
    var expanded by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    Box(modifier) {
        OutlinedButton(onClick = { query = ""; expanded = true }, modifier = Modifier.fillMaxWidth()) { Text(selected, maxLines = 1) }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.heightIn(max = 380.dp)) {
            OutlinedTextField(value = query, onValueChange = { query = it }, placeholder = { Text("Find company") }, singleLine = true, modifier = Modifier.padding(8.dp).width(240.dp))
            DropdownMenuItem(text = { Text("All companies") }, onClick = { onChoose("All companies"); expanded = false })
            val matches = companies.filter { it.contains(query, ignoreCase = true) }
            matches.take(30).forEach { name -> DropdownMenuItem(text = { Text(name) }, onClick = { onChoose(name); expanded = false }) }
            if (matches.size > 30) Text("Type to find more companies", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun PyqQuestionCard(q: QuestionEntity, window: QuestionWindow, onSolved: () -> Unit, onBookmark: () -> Unit, onOpen: () -> Unit) {
    Card(Modifier.fillMaxWidth().testTag("question-${q.id}")) { Column(Modifier.padding(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onSolved) { Icon(if (q.isSolved) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked, if (q.isSolved) "Mark unsolved" else "Mark solved") }
            Column(Modifier.weight(1f)) {
                Text(q.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text("${q.companyName} · ${q.difficulty}", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium)
            }
            IconButton(onClick = onBookmark) { Icon(if (q.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder, if (q.isBookmarked) "Remove bookmark" else "Bookmark") }
        }
        if (q.leetcodeId != null) {
            val frequency = q.frequency(window)?.let { String.format(Locale.US, "%.1f%%", it) } ?: "Not supplied"
            val acceptance = q.acceptanceRate?.let { String.format(Locale.US, "%.1f%%", it) } ?: "Not supplied"
            Text("LeetCode #${q.leetcodeId} · Frequency $frequency · Acceptance $acceptance", style = MaterialTheme.typography.labelMedium)
            Text("${window.label} within the ${q.snapshotDate} snapshot", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            Text(q.description, style = MaterialTheme.typography.bodyMedium)
            Text("${q.topic} · ${q.round} · ${q.year}", style = MaterialTheme.typography.labelMedium)
        }
        Text("Source: ${q.source}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (q.solutionLink.isNotBlank()) TextButton(onClick = onOpen) { Text("Open in LeetCode") }
    } }
}
