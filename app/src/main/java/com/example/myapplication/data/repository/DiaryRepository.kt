package com.example.myapplication.data.repository

import com.example.myapplication.data.local.DiaryDao
import com.example.myapplication.data.model.DiaryEntry
import kotlinx.coroutines.flow.Flow

class DiaryRepository(
    private val diaryDao: DiaryDao
) {
    fun getAllEntries(): Flow<List<DiaryEntry>> = diaryDao.getAllEntries()

    suspend fun getEntry(id: Long): DiaryEntry? = diaryDao.getEntry(id)

    suspend fun insertEntry(entry: DiaryEntry): Long = diaryDao.insertEntry(entry)

    suspend fun updateEntry(entry: DiaryEntry) = diaryDao.updateEntry(entry)

    suspend fun deleteEntry(entry: DiaryEntry) = diaryDao.deleteEntry(entry)
} 