package com.shanenworks.omnizient.data.local.dao

import androidx.room.*
import com.shanenworks.omnizient.data.local.entity.Document
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {
    @Query("SELECT * FROM documents")
    fun getAllDocuments(): Flow<List<Document>>

    @Query("SELECT * FROM documents WHERE id = :id")
    suspend fun getDocumentById(id: String): Document?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: Document)

    @Delete
    suspend fun deleteDocument(document: Document)

    @Query("DELETE FROM documents WHERE id = :id")
    suspend fun deleteDocumentById(id: String)

    @Query("SELECT * FROM documents WHERE fileName LIKE '%' || :query || '%' " +
            "AND (:fileExtensions IS NULL OR :fileExtensions = '' OR substr(fileName, instr(fileName, '.') + 1) IN (:fileExtensions))")
    fun searchDocuments(query: String, fileExtensions: String? = null): Flow<List<Document>>

    @Query("SELECT * FROM documents WHERE mimeType LIKE :mimeType || '%'")
    fun getDocumentsByMimeType(mimeType: String): Flow<List<Document>>

    @Query("SELECT DISTINCT substr(fileName, instr(fileName, '.') + 1) AS extension FROM documents")
    suspend fun getAllFileExtensions(): List<String>

    @Query("SELECT * FROM documents WHERE id IN (:ids)")
    suspend fun getDocumentsByIds(ids: List<String>): List<Document>


}
