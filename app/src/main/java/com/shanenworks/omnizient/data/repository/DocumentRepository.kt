package com.shanenworks.omnizient.data.repository

import com.shanenworks.omnizient.data.local.dao.DocumentDao
import com.shanenworks.omnizient.data.local.dao.IndexEntryDao
import com.shanenworks.omnizient.data.local.entity.Document
import com.shanenworks.omnizient.data.local.entity.IndexEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DocumentRepository @Inject constructor(
    private val documentDao: DocumentDao,
    private val indexEntryDao: IndexEntryDao
) {
    // Document operations
    fun getAllDocuments(): Flow<List<Document>> = documentDao.getAllDocuments()

    suspend fun getDocumentById(id: String): Document? = withContext(Dispatchers.IO) {
        documentDao.getDocumentById(id)
    }

    suspend fun insertDocument(document: Document) = withContext(Dispatchers.IO) {
        documentDao.insertDocument(document)
    }

    suspend fun deleteDocument(document: Document) = withContext(Dispatchers.IO) {
        documentDao.deleteDocument(document)
    }

    suspend fun deleteDocumentById(id: String) = withContext(Dispatchers.IO) {
        documentDao.deleteDocumentById(id)
    }

    fun searchDocuments(query: String, fileExtensions: List<String> = emptyList()): Flow<List<Document>> {
        val extensionsString: String? = if (fileExtensions.isEmpty()) null else fileExtensions.joinToString(",")
        return documentDao.searchDocuments("%$query%", extensionsString)
    }

    // Index entry operations
    fun getEntriesForDocument(documentId: String): Flow<List<IndexEntry>> =
        indexEntryDao.getEntriesForDocument(documentId)

    suspend fun insertIndexEntry(indexEntry: IndexEntry) = withContext(Dispatchers.IO) {
        indexEntryDao.insertIndexEntry(indexEntry)
    }

    suspend fun deleteEntriesForDocument(documentId: String) = withContext(Dispatchers.IO) {
        indexEntryDao.deleteEntriesForDocument(documentId)
    }

    suspend fun searchIndexEntries(query: String): List<String> = withContext(Dispatchers.IO) {
        indexEntryDao.searchIndexEntries("%$query%")
    }

    // Combined operations
    suspend fun indexDocument(document: Document, content: String, pageNumber: Int? = null) {
        withContext(Dispatchers.IO) {
            insertDocument(document)
            insertIndexEntry(IndexEntry(documentId = document.id, content = content, pageNumber = pageNumber))
        }
    }

    suspend fun searchDocumentsWithContent(query: String, fileExtensions: List<String> = emptyList()): List<Document> {
        return withContext(Dispatchers.IO) {
            val documentIds = searchIndexEntries(query)
            documentIds.mapNotNull { getDocumentById(it) }
                .filter { document -> fileExtensions.isEmpty() || fileExtensions.contains(getFileExtension(document.fileName)) }
        }
    }

    suspend fun searchDocumentsAndContent(query: String, fileExtensions: List<String> = emptyList()): Flow<List<Document>> = flow {
        val documentResults = searchDocuments(query, fileExtensions).first()
        val contentResults = searchDocumentsWithContent(query, fileExtensions)
        emit((documentResults + contentResults).distinctBy { it.id })
    }

    fun getFileExtension(fileName: String): String {
        return fileName.substringAfterLast('.', "")
    }

    fun searchDocumentsFullText(query: String, fileExtensions: List<String> = emptyList()): Flow<List<Document>> = flow {
        val extensionsList: List<String>? = if (fileExtensions.isEmpty()) null else fileExtensions
        val documentIds = indexEntryDao.searchIndexEntriesWithFileExtensions("%$query%", extensionsList)
        val documents = documentIds.mapNotNull { documentDao.getDocumentById(it) }
        emit(documents)
    }

    suspend fun mapFileSystem() {
        // Implement file system mapping logic here
    }

    suspend fun searchDocumentsSemantic(query: String, fileExtensions: List<String> = emptyList()): List<Document> {
        // Implement semantic search logic here
        return emptyList() // Placeholder return
    }
}
