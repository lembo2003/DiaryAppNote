package com.example.myapplication.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.databinding.DialogImagePreviewBinding

class ImagePreviewDialog : DialogFragment() {

    private var _binding: DialogImagePreviewBinding? = null
    private val binding get() = _binding!!
    private val thumbnailAdapter = ImageThumbnailAdapter()
    private var imageUris: List<String> = emptyList()
    private var currentImageUri: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_App_Dialog_FullScreen)
        
        arguments?.let {
            imageUris = it.getStringArray(ARG_IMAGE_URIS)?.toList() ?: emptyList()
            currentImageUri = it.getString(ARG_SELECTED_URI)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogImagePreviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViews()
        setupThumbnails()
        loadMainImage(currentImageUri)
    }

    private fun setupViews() {
        binding.apply {
            btnBack.setOnClickListener { dismiss() }
            
            // Setup RecyclerView
            rvThumbnails.apply {
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                adapter = thumbnailAdapter
            }
        }
    }

    private fun setupThumbnails() {
        thumbnailAdapter.submitList(imageUris)
        currentImageUri?.let { uri ->
            thumbnailAdapter.setSelectedImage(uri)
        }

        thumbnailAdapter.onThumbnailClick = { clickedUri ->
            loadMainImage(clickedUri)
            thumbnailAdapter.setSelectedImage(clickedUri)
            currentImageUri = clickedUri
        }
    }

    private fun loadMainImage(uri: String?) {
        uri?.let {
            Glide.with(this)
                .load(it)
                .into(binding.ivMainPreview)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_IMAGE_URIS = "image_uris"
        private const val ARG_SELECTED_URI = "selected_uri"

        fun newInstance(imageUris: List<String>, selectedUri: String) = ImagePreviewDialog().apply {
            arguments = Bundle().apply {
                putStringArray(ARG_IMAGE_URIS, imageUris.toTypedArray())
                putString(ARG_SELECTED_URI, selectedUri)
            }
        }
    }
} 