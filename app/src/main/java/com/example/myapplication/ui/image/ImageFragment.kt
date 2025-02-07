package com.example.myapplication.ui.image

import android.content.ContentUris
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import com.example.myapplication.R
import com.example.myapplication.base.BaseFragment
import com.example.myapplication.databinding.FragmentImageBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.myapplication.ui.shared.SharedImageViewModel

class ImageFragment : BaseFragment<FragmentImageBinding>() {

    private val sharedViewModel: SharedImageViewModel by activityViewModels()
    private val imageAdapter = ImageAdapter()
    private var loadJob: Job? = null
    private val args: ImageFragmentArgs by navArgs()

    override fun inflateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentImageBinding {
        return FragmentImageBinding.inflate(inflater, container, false)
    }

    override fun setupUI() {
        binding.apply {
            btnBack.setOnClickListener {
                findNavController().popBackStack()
            }

            btnDone.setOnClickListener {
                val selectedImages = imageAdapter.getSelectedImages()
                if (selectedImages.isNotEmpty()) {
                    sharedViewModel.setSelectedImages(selectedImages)
                    findNavController().previousBackStackEntry?.savedStateHandle?.set(
                        "selected_images",
                        ArrayList(selectedImages)
                    )
                    findNavController().popBackStack()
                }
            }

            // Setup RecyclerView
            rvImages.apply {
                layoutManager = GridLayoutManager(context, 3)
                adapter = imageAdapter
                addItemDecoration(GridSpacingItemDecoration(3, 2))
            }

            imageAdapter.onSelectionChanged = { count ->
                btnDone.isVisible = count > 0
            }
        }

        // Pre-select images from arguments
        args.selectedImages?.let { images ->
            imageAdapter.preSelectImages(images.toList())
            sharedViewModel.setSelectedImages(images.toList())
        }

        // Load images
        loadImages()
    }

    private fun loadImages() {
        loadJob?.cancel()
        loadJob = viewLifecycleOwner.lifecycleScope.launch {
            try {
                binding.rvImages.isVisible = false
                val images = withContext(Dispatchers.IO) {
                    getGalleryImages()
                }
                if (images.isEmpty()) {
                    showError(getString(R.string.no_images_found))
                } else {
                    imageAdapter.submitList(images)
                    binding.rvImages.isVisible = true
                }
            } catch (e: Exception) {
                showError(e.message ?: getString(R.string.error_loading_images))
            }
        }
    }

    private fun getGalleryImages(): List<ImageItem> {
        val images = mutableListOf<ImageItem>()
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DATE_ADDED
        )

        requireContext().contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            "${MediaStore.Images.Media.DATE_ADDED} DESC"
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn)
                val path = cursor.getString(dataColumn)
                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                )
                images.add(ImageItem(contentUri.toString(), name))
            }
        }
        return images
    }

    private fun showError(message: String) {
        binding.rvImages.isVisible = false
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.error)
            .setMessage(message)
            .setPositiveButton(R.string.retry) { _, _ -> loadImages() }
            .setNegativeButton(R.string.cancel) { _, _ -> findNavController().navigateUp() }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        loadJob?.cancel()
        imageAdapter.clearSelections()
    }
} 