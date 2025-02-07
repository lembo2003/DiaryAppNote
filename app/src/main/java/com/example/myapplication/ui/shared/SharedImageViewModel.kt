package com.example.myapplication.ui.shared

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SharedImageViewModel : ViewModel() {
    private val _selectedImages = MutableStateFlow<List<String>>(emptyList())
    val selectedImages: StateFlow<List<String>> = _selectedImages

    fun setSelectedImages(images: List<String>) {
        _selectedImages.value = images
    }
} 