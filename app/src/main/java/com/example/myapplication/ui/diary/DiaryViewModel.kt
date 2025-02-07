package com.example.myapplication.ui.diary

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.Result
import com.example.myapplication.data.local.DiaryDatabase
import com.example.myapplication.data.model.DiaryEntry
import com.example.myapplication.data.repository.DiaryRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import kotlinx.coroutines.Dispatchers
import com.example.myapplication.util.ImageUtils
import kotlinx.coroutines.withContext

class DiaryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: DiaryRepository
    private val _entries = MutableStateFlow<Result<List<DiaryEntry>>>(Result.Loading)
    val entries: StateFlow<Result<List<DiaryEntry>>> = _entries

    private val _currentEntry = MutableStateFlow<Result<DiaryEntry?>>(Result.Loading)
    val currentEntry: StateFlow<Result<DiaryEntry?>> = _currentEntry

    private val _selectedImages = MutableStateFlow<List<String>>(emptyList())
    val selectedImages: StateFlow<List<String>> = _selectedImages

    private val _temporaryState = MutableStateFlow<EntryState?>(null)
    val temporaryState: StateFlow<EntryState?> = _temporaryState

    init {
        val database = DiaryDatabase.getDatabase(application)
        repository = DiaryRepository(database.diaryDao())
        loadEntries()
    }

    private fun loadEntries() {
        viewModelScope.launch {
            try {
                repository.getAllEntries().collect { entries ->
                    _entries.value = Result.Success(entries)
                }
            } catch (e: Exception) {
                _entries.value = Result.Error(e)
            }
        }
    }

    suspend fun getEntry(id: Long): DiaryEntry? {
        return try {
            val entry = repository.getEntry(id)
            entry?.also { 
                _selectedImages.value = it.images
            }
        } catch (e: Exception) {
            null
        }
    }

    fun setSelectedImages(images: List<String>) {
        viewModelScope.launch {
            _selectedImages.value = images
        }
    }

    fun addEntry(
        title: String,
        content: String,
        date: LocalDateTime,
        feeling: String,
        images: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            try {
                // Compress and save images before creating entry
                val compressedImages = withContext(Dispatchers.IO) {
                    images.map { ImageUtils.compressAndSaveImage(getApplication(), it) }
                }
                
                val entry = DiaryEntry(
                    title = title,
                    content = content,
                    date = date,
                    feeling = feeling,
                    images = compressedImages,
                    lastModified = LocalDateTime.now()
                )
                repository.insertEntry(entry)
                // Clear selected images after successful save
                _selectedImages.value = emptyList()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun updateEntry(entry: DiaryEntry) {
        viewModelScope.launch {
            try {
                // Compress any new images
                val existingImages = entry.images.filter { it.startsWith(getApplication<Application>().filesDir.absolutePath) }
                val newImages = entry.images.filter { !it.startsWith(getApplication<Application>().filesDir.absolutePath) }
                
                val compressedNewImages = withContext(Dispatchers.IO) {
                    newImages.map { ImageUtils.compressAndSaveImage(getApplication(), it) }
                }
                
                val updatedEntry = entry.copy(
                    images = existingImages + compressedNewImages,
                    lastModified = LocalDateTime.now()
                )
                repository.updateEntry(updatedEntry)
                // Clear selected images after successful update
                _selectedImages.value = emptyList()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun deleteEntry(entry: DiaryEntry) {
        viewModelScope.launch {
            try {
                repository.deleteEntry(entry)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun saveTemporaryState(
        title: String,
        content: String,
        date: LocalDateTime,
        feeling: String,
        images: List<String> = emptyList()
    ) {
        _temporaryState.value = EntryState(
            title = title,
            content = content,
            date = date,
            feeling = feeling,
            images = images
        )
    }

    fun restoreTemporaryState() {
        _temporaryState.value?.let { state ->
            _selectedImages.value = state.images
        }
    }

    fun clearTemporaryState() {
        _temporaryState.value = null
    }

    fun removeImage(uri: String) {
        viewModelScope.launch {
            val currentImages = _selectedImages.value.toMutableList()
            currentImages.remove(uri)
            _selectedImages.value = currentImages
        }
    }
}

data class EntryState(
    val title: String,
    val content: String,
    val date: LocalDateTime,
    val feeling: String,
    val images: List<String> = emptyList()
) 