package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userEmail: String,
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val colorIndex: Int = 0,
    val isArchived: Boolean = false
)
