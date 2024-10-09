package com.shanenworks.omnizient.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.shanenworks.omnizient.data.local.entity.Document
import com.shanenworks.omnizient.viewmodel.DocumentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: DocumentViewModel = hiltViewModel(),
    onToggleWebUiBroadcasting: (Boolean) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var fileExtensions by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var isWebUiBroadcasting by remember { mutableStateOf(false) }
    val searchResults by viewModel.searchResults.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    var selectedDocument by remember { mutableStateOf<Document?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Omnizient") },
                actions = {
                    IconToggleButton(
                        checked = isWebUiBroadcasting,
                        onCheckedChange = {
                            isWebUiBroadcasting = it
                            onToggleWebUiBroadcasting(it)
                        }
                    ) {
                        Icon(
                            imageVector = if (isWebUiBroadcasting) Icons.Default.Check else Icons.Default.Close,
                            contentDescription = "Toggle Web UI Broadcasting"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(
                visible = !isSearching,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                Text(
                    text = "Omnizient",
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.padding(bottom = 32.dp)
                )
            }

            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                onSearch = {
                    isSearching = true
                    viewModel.searchDocuments(searchQuery, fileExtensions.split("\n").filter { it.isNotBlank() })
                },
                onClear = {
                    searchQuery = ""
                    isSearching = false
                    viewModel.clearSearch()
                    selectedDocument = null
                },
                modifier = Modifier.animateContentSize()
            )

            AnimatedVisibility(visible = !isSearching) {
                OutlinedTextField(
                    value = fileExtensions,
                    onValueChange = { fileExtensions = it },
                    label = { Text("File Extensions (one per line)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .height(120.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        isSearching = true
                        viewModel.searchDocuments(searchQuery, fileExtensions.split("\n").filter { it.isNotBlank() })
                    })
                )
            }

            AnimatedVisibility(visible = isSearching) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                } else {
                    Row(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(end = 8.dp)
                        ) {
                            items(searchResults) { document ->
                                SearchResultItem(
                                    document = document,
                                    onOpenFile = { viewModel.openFile(it) },
                                    onOpenDirectory = { viewModel.openDirectory(it) },
                                    onSelect = { selectedDocument = it }
                                )
                            }
                        }
                        FilePreview(
                            document = selectedDocument,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(start = 8.dp)
                        )
                    }
                }
            }

            error?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        label = { Text("Search Files") },
        singleLine = true,
        modifier = modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch() }),
        trailingIcon = {
            Row {
                if (query.isNotEmpty()) {
                    IconButton(onClick = onClear) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear search")
                    }
                }
                IconButton(onClick = onSearch) {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchResultItem(
    document: Document,
    onOpenFile: (Document) -> Unit,
    onOpenDirectory: (Document) -> Unit,
    onSelect: (Document) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onSelect(document) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = document.fileName, style = MaterialTheme.typography.titleMedium)
            Text(text = "Type: ${document.mimeType}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Size: ${formatFileSize(document.size)}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Path: ${document.filePath}", style = MaterialTheme.typography.bodySmall)
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text("Open File") },
                onClick = {
                    onOpenFile(document)
                    showMenu = false
                }
            )
            DropdownMenuItem(
                text = { Text("Open Directory") },
                onClick = {
                    onOpenDirectory(document)
                    showMenu = false
                }
            )
        }
    }
}

@Composable
fun FilePreview(
    document: Document?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        if (document != null) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "File Preview", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Name: ${document.fileName}")
                Text(text = "Type: ${document.mimeType}")
                Text(text = "Size: ${formatFileSize(document.size)}")
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Content Preview:", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                // Here you would add logic to fetch and display a snippet of the file content
                Text(text = "File content preview not implemented yet.")
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Select a file to preview", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

// Helper function to format file size
fun formatFileSize(size: Long): String {
    val kb = size / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    return when {
        gb >= 1 -> String.format("%.2f GB", gb)
        mb >= 1 -> String.format("%.2f MB", mb)
        kb >= 1 -> String.format("%.2f KB", kb)
        else -> "$size bytes"
    }
}
