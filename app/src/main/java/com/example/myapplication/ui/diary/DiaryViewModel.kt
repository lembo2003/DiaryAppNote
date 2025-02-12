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

    private var originalImages: List<String> = emptyList()
    private var deletedImages = mutableSetOf<String>()  // Track deleted images

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
                // Don't reset images if we're returning from image selection
                if (_selectedImages.value.isEmpty()) {
                    _selectedImages.value = it.images
                }
                // Only clear deleted images when first loading entry
                if (deletedImages.isEmpty()) {
                    deletedImages.clear()
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    fun setSelectedImages(images: List<String>) {
        // Keep track of deleted images and don't restore them
        val newImages = images.filter { it !in deletedImages }
        // Only update if the new list is different
        if (newImages != _selectedImages.value) {
            _selectedImages.value = newImages
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
                val finalImages = entry.images.filter { it !in deletedImages }
                
                // Compress any new images
                val existingImages = finalImages.filter { it.startsWith(getApplication<Application>().filesDir.absolutePath) }
                val newImages = finalImages.filter { !it.startsWith(getApplication<Application>().filesDir.absolutePath) }
                
                val compressedNewImages = withContext(Dispatchers.IO) {
                    newImages.map { ImageUtils.compressAndSaveImage(getApplication(), it) }
                }
                
                val updatedEntry = entry.copy(
                    images = existingImages + compressedNewImages,
                    lastModified = LocalDateTime.now()
                )
                repository.updateEntry(updatedEntry)
                
                // Only clear states after successful update
                _selectedImages.value = emptyList()
                deletedImages.clear()
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
        // Also update selected images to maintain consistency
        _selectedImages.value = images
    }

    fun restoreTemporaryState() {
        _temporaryState.value?.let { state ->
            _selectedImages.value = state.images
        }
    }

    fun clearTemporaryState() {
        _temporaryState.value = null
        _selectedImages.value = emptyList()
        deletedImages.clear()
    }

    fun removeImage(uri: String) {
        deletedImages.add(uri)
        _selectedImages.value = _selectedImages.value.filter { it != uri }
    }
}

data class EntryState(
    val title: String,
    val content: String,
    val date: LocalDateTime,
    val feeling: String,
    val images: List<String> = emptyList()
) 