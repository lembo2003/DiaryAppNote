package com.example.myapplication.ui.image

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ImageViewModel(application: Application) : AndroidViewModel(application) {
    private val _selectedImages = MutableStateFlow<List<String>>(emptyList())
    val selectedImages: StateFlow<List<String>> = _selectedImages

    // Add initial images
    fun initializeSelectedImages(images: List<String>) {
        if (_selectedImages.value.isEmpty()) {
            _selectedImages.value = images
        }
    }

    fun setSelectedImages(images: List<String>) {
        viewModelScope.launch {
            _selectedImages.value = images
        }
    }
} 