package com.shanenworks.omnizient.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "index_entries",
    foreignKeys = [
        ForeignKey(
            entity = Document::class,
            parentColumns = ["id"],
            childColumns = ["documentId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class IndexEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val documentId: String,
    val content: String,
    val pageNumber: Int?
)
