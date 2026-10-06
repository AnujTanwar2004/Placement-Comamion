package com.example.placementcompanion.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.placementcompanion.presentation.viewmodel.CompanyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanyListScreen(
    viewModel: CompanyViewModel,
    onCompanyClick: (Long) -> Unit
) {
    val companies by viewModel.companies.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()

    val filters = listOf("All", "Product", "Service", "Startup", "Core")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Company Placement Directory") }
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
                placeholder = { Text("Search companies or roles...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filters) { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { viewModel.setFilter(filter) },
                        label = { Text(filter) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (companies.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No companies found matching criteria.")
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(companies) { company ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onCompanyClick(company.id) }
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = company.name,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    IconButton(onClick = { viewModel.toggleBookmark(company.id, company.isBookmarked) }) {
                                        Icon(
                                            imageVector = if (company.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                            contentDescription = "Bookmark"
                                        )
                                    }
                                }
                                Text(
                                    text = company.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 2
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AssistChip(
                                        onClick = {},
                                        label = { Text(company.companyType) }
                                    )
                                    AssistChip(
                                        onClick = {},
                                        label = { Text("Difficulty: ${company.difficulty}") }
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Roles: ${company.hiringRoles}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
