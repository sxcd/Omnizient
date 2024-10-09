package com.shanenworks.omnizient.data.local.dao

import androidx.room.*
import com.shanenworks.omnizient.data.local.entity.IndexEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface IndexEntryDao {
    @Query("SELECT * FROM index_entries WHERE documentId = :documentId")
    fun getEntriesForDocument(documentId: String): Flow<List<IndexEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIndexEntry(indexEntry: IndexEntry)

    @Query("DELETE FROM index_entries WHERE documentId = :documentId")
    suspend fun deleteEntriesForDocument(documentId: String)

    @Query("SELECT DISTINCT documentId FROM index_entries WHERE content LIKE '%' || :query || '%'")
    suspend fun searchIndexEntries(query: String): List<String>

    @Query("SELECT DISTINCT ie.documentId FROM index_entries ie " +
            "INNER JOIN documents d ON ie.documentId = d.id " +
            "WHERE ie.content LIKE '%' || :query || '%' " +
            "AND (:fileExtensions IS NULL OR substr(d.fileName, instr(d.fileName, '.') + 1) IN (:fileExtensions))")
    suspend fun searchIndexEntriesWithFileExtensions(query: String, fileExtensions: List<String>? = null): List<String>

    @Query("SELECT * FROM index_entries WHERE documentId IN (:documentIds) AND content LIKE '%' || :query || '%'")
    suspend fun getMatchingEntriesForDocuments(documentIds: List<String>, query: String): List<IndexEntry>

    @Query("SELECT COUNT(*) FROM index_entries WHERE documentId = :documentId")
    suspend fun getIndexEntryCountForDocument(documentId: String): Int

    @Transaction
    suspend fun insertOrUpdateIndexEntries(documentId: String, entries: List<IndexEntry>) {
        deleteEntriesForDocument(documentId)
        entries.forEach { insertIndexEntry(it) }
    }
}
