package com.example.myapplication.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "diary_entries")
data class DiaryEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val date: LocalDateTime,
    val feeling: String,
    val images: List<String> = emptyList(),
    val lastModified: LocalDateTime = LocalDateTime.now()
) 