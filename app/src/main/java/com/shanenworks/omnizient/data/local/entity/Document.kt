package com.shanenworks.omnizient.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "documents")
data class Document(
    @PrimaryKey val id: String,
    val fileName: String,
    val filePath: String,
    val mimeType: String,
    val size: Long,
    val lastModified: Long,
    val indexedAt: Long
)
