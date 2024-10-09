package com.shanenworks.omnizient.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shanenworks.omnizient.data.local.entity.Document
import com.shanenworks.omnizient.data.repository.DocumentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DocumentViewModel @Inject constructor(
    private val repository: DocumentRepository
) : ViewModel() {

    private val _documents = MutableStateFlow<List<Document>>(emptyList())
    val documents: StateFlow<List<Document>> = _documents

    private val _searchResults = MutableStateFlow<List<Document>>(emptyList())
    val searchResults: StateFlow<List<Document>> = _searchResults

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _openFileEvent = MutableSharedFlow<Document>()
    val openFileEvent: SharedFlow<Document> = _openFileEvent

    private val _openDirectoryEvent = MutableSharedFlow<Document>()
    val openDirectoryEvent: SharedFlow<Document> = _openDirectoryEvent

    init {
        loadDocuments()
    }

    private fun loadDocuments() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getAllDocuments()
                .catch { e ->
                    _error.value = "Error loading documents: ${e.message}"
                }
                .collect { documentList ->
                    _documents.value = documentList
                    _isLoading.value = false
                }
        }
    }

    fun searchDocuments(query: String, fileExtensions: List<String> = emptyList()) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val regularSearchResults = repository.searchDocuments(query, fileExtensions).first()
                val fullTextSearchResults = repository.searchDocumentsFullText(query, fileExtensions).first()
                val semanticSearchResults = repository.searchDocumentsSemantic(query, fileExtensions)
                
                val combinedResults = (regularSearchResults + fullTextSearchResults + semanticSearchResults)
                    .distinctBy { it.id }
                    .sortedByDescending { it.lastModified }
                
                _searchResults.value = combinedResults
            } catch (e: Exception) {
                _error.value = "Error searching documents: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearSearch() {
        _searchResults.value = emptyList()
    }

    fun openFile(document: Document) {
        viewModelScope.launch {
            _openFileEvent.emit(document)
        }
    }

    fun openDirectory(document: Document) {
        viewModelScope.launch {
            _openDirectoryEvent.emit(document)
        }
    }

    fun toggleWebUiBroadcasting(enabled: Boolean) {
        // Implement the logic to toggle Web UI broadcasting
        // This might involve starting or stopping the WebUiBroadcastService
    }
}
