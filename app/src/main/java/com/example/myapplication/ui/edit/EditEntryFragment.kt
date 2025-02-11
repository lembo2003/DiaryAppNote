package com.example.myapplication.ui.edit

import android.os.Bundle
import android.view.*
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.base.BaseFragment
import com.example.myapplication.databinding.FragmentEditEntryBinding
import com.example.myapplication.ui.create.Feeling
import com.example.myapplication.ui.create.FeelingAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.google.android.material.datepicker.MaterialDatePicker
import java.time.Instant
import java.time.ZoneId
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import androidx.navigation.fragment.navArgs
import com.example.myapplication.ui.diary.DiaryViewModel
import com.example.myapplication.data.model.DiaryEntry
import com.example.myapplication.ui.diary.SelectedImagesAdapter
import com.example.myapplication.ui.dialog.ImagePreviewDialog
import com.example.myapplication.ui.shared.SharedImageViewModel
import androidx.fragment.app.activityViewModels

class EditEntryFragment : BaseFragment<FragmentEditEntryBinding>() {

    private val viewModel: DiaryViewModel by viewModels()
    private val sharedViewModel: SharedImageViewModel by activityViewModels()
    private val args: EditEntryFragmentArgs by navArgs()
    private val feelingAdapter = FeelingAdapter()
    private var selectedFeeling: Feeling? = null
    private var isEditMode = false
    private var selectedDate = LocalDateTime.now()
    private var hasUnsavedChanges = false
    private var currentEntry: DiaryEntry? = null
    private val selectedImagesAdapter = SelectedImagesAdapter()

    override fun inflateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentEditEntryBinding {
        return FragmentEditEntryBinding.inflate(inflater, container, false)
    }

    override fun setupUI() {
        setupToolbar()
        setupContent()
        setupFeelings()
        loadEntry()
        setupSelectedImages()

        // Observe navigation result for selected images
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<ArrayList<String>>("selected_images")?.observe(
            viewLifecycleOwner
        ) { images ->
            if (images != null && images.isNotEmpty()) {
                viewModel.setSelectedImages(images)

                hasUnsavedChanges = true
                // Clear the saved state handle
                findNavController().currentBackStackEntry?.savedStateHandle?.remove<ArrayList<String>>("selected_images")
            }
        }
    }

    private fun setupToolbar() {
        binding.includeToolbar.apply {
            btnBack.setOnClickListener {
                handleBack()
            }

            // Initially disable image and delete buttons
            btnImage.isEnabled = false
            btnToolbarDelete.isEnabled = false

            // Setup date picker
            tvDate.apply {
                text = selectedDate.format(DateTimeFormatter.ofPattern("EEE, MMM d"))
                setOnClickListener { 
                    if (isEditMode) {
                        showDatePicker() 
                    }
                }
            }

            btnImage.setOnClickListener {
                if (isEditMode) {
                    // Save current state to the backstack before navigating
                    findNavController().previousBackStackEntry?.savedStateHandle?.set(
                        "temp_images", 
                        ArrayList(viewModel.selectedImages.value)
                    )
                    
                    findNavController().navigate(
                        EditEntryFragmentDirections.actionEditToPermission(
                            viewModel.selectedImages.value.toTypedArray()
                        )
                    )
                }
            }

            btnEdit.setOnClickListener {
                toggleEditMode()
            }

            btnToolbarDelete.apply {
                isVisible = true
                setOnClickListener {
                    showDeleteConfirmationDialog()
                }
            }
        }
    }

    private fun setupContent() {
        binding.apply {
            // Disable editing by default
            etTitle.isEnabled = false
            etContent.isEnabled = false
            rvFeelings.isEnabled = false
            btnSave.isVisible = false

            // Text change listeners
            etTitle.doAfterTextChanged { 
                tilTitle.error = null
                hasUnsavedChanges = true
            }
            
            etContent.doAfterTextChanged { 
                tilContent.error = null
                hasUnsavedChanges = true
            }

            btnSave.setOnClickListener {
                if (validateInput()) {
                    saveChanges()
                }
            }
        }
    }

    private fun setupFeelings() {
        binding.rvFeelings.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = feelingAdapter
        }

        feelingAdapter.onFeelingSelected = { feeling ->
            selectedFeeling = feeling
            hasUnsavedChanges = true
        }
    }

    private fun toggleEditMode() {
        isEditMode = !isEditMode
        binding.includeToolbar.apply {
            btnEdit.setImageResource(
                if (isEditMode) R.drawable.ic_close else R.drawable.ic_edit
            )
            btnImage.isEnabled = isEditMode
            btnToolbarDelete.apply {
                isEnabled = isEditMode
                alpha = if (isEditMode) 1f else 0.5f
            }
            tvDate.isEnabled = isEditMode
        }
        
        // Enable/disable text fields
        binding.apply {
            etTitle.isEnabled = isEditMode
            etContent.isEnabled = isEditMode
            
            // Enable/disable feelings
            rvFeelings.isEnabled = isEditMode
            feelingAdapter.isEnabled = isEditMode
            
            // Show/hide save button
            btnSave.isVisible = isEditMode
        }
        
        // Update images edit mode
        selectedImagesAdapter.isEditMode = isEditMode
        selectedImagesAdapter.notifyDataSetChanged()
    }

    private fun validateInput(): Boolean {
        var isValid = true
        binding.apply {
            if (etTitle.text.isNullOrBlank()) {
                tilTitle.error = getString(R.string.title_required)
                isValid = false
            }
            if (etContent.text.isNullOrBlank()) {
                tilContent.error = getString(R.string.content_required)
                isValid = false
            }
        }
        return isValid
    }

    private fun showDeleteConfirmationDialog() {
        binding.apply {
            // Show background and dialog
            dialogBackground.visibility = View.VISIBLE
            dialogContainer.visibility = View.VISIBLE

            // Setup click listeners
            includeDeleteDialog.apply {
                // Set dialog text if needed
                titleTextView.text = getString(R.string.wait)
                messageTextView.text = getString(R.string.are_you_sure_you_want_to_delete)
                
                btnDelete.setOnClickListener {
                    hideDeleteDialog()
                    deleteEntry()
                }

                btnCancel.setOnClickListener {
                    hideDeleteDialog()
                }
            }

            // Optional: dismiss on background click
            dialogBackground.setOnClickListener {
                hideDeleteDialog()
            }
        }
    }

    private fun hideDeleteDialog() {
        binding.apply {
            dialogBackground.visibility = View.GONE
            dialogContainer.visibility = View.GONE
        }
    }

    private fun showDatePicker() {
        MaterialDatePicker.Builder.datePicker()
            .setSelection(
                selectedDate.atZone(ZoneId.systemDefault())
                    .toInstant().toEpochMilli()
            )
            .build()
            .apply {
                addOnPositiveButtonClickListener { timestamp ->
                    selectedDate = Instant.ofEpochMilli(timestamp)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime()
                    binding.includeToolbar.tvDate.text = selectedDate.format(
                        DateTimeFormatter.ofPattern("EEE, MMM d")
                    )
                    hasUnsavedChanges = true
                }
            }
            .show(childFragmentManager, "date_picker")
    }

    private fun loadEntry() {
        viewLifecycleOwner.lifecycleScope.launch {
            binding.progressBar.isVisible = true
            currentEntry = viewModel.getEntry(args.entryId)
            if (currentEntry != null) {
                binding.apply {
                    progressBar.isVisible = false
                    etTitle.setText(currentEntry!!.title)
                    etContent.setText(currentEntry!!.content)
                    selectedDate = currentEntry!!.date
                    includeToolbar.tvDate.text = selectedDate.format(
                        DateTimeFormatter.ofPattern("EEE, MMM d")
                    )
                    selectedFeeling = Feeling.values().find { it.name == currentEntry!!.feeling }
                    feelingAdapter.setSelectedFeeling(selectedFeeling)
                    
                    // Make edit and delete buttons visible
                    includeToolbar.btnEdit.isVisible = true
                    includeToolbar.btnToolbarDelete.isVisible = true
                    
                    // Load and display images - just set in ViewModel, collector will handle UI update
                    val entryImages = currentEntry!!.images
                    viewModel.setSelectedImages(entryImages)
                }
            } else {
                // Handle entry not found
                showError(getString(R.string.entry_not_found))
                findNavController().navigateUp()
            }
        }
    }

    private fun showError(message: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.error))
            .setMessage(message)
            .setPositiveButton(getString(R.string.ok)) { _, _ ->
                findNavController().navigateUp()
            }
            .show()
    }

    private fun saveChanges() {
        currentEntry?.let { entry ->
            viewModel.updateEntry(
                entry.copy(
                    title = binding.etTitle.text.toString(),
                    content = binding.etContent.text.toString(),
                    date = selectedDate,
                    feeling = selectedFeeling?.name ?: Feeling.NEUTRAL.name,
                    images = viewModel.selectedImages.value,
                    lastModified = LocalDateTime.now()
                )
            )
        }
        findNavController().navigateUp()
    }

    private fun setupSelectedImages() {
        binding.rvSelectedImages.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = selectedImagesAdapter
        }

        selectedImagesAdapter.apply {
            isEditMode = false
            onImageClick = { clickedUri ->
                ImagePreviewDialog.newInstance(
                    imageUris = viewModel.selectedImages.value,
                    selectedUri = clickedUri
                ).show(childFragmentManager, "image_preview")
            }
            onDeleteClick = { uri ->
                if (isEditMode) {
                    viewModel.removeImage(uri)
                    hasUnsavedChanges = true
                }
            }
        }

        // Observe selected images changes
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.selectedImages.collect { images ->
                binding.rvSelectedImages.isVisible = images.isNotEmpty()
                selectedImagesAdapter.submitList(images)
            }
        }
    }

    private fun saveCurrentState() {
        binding.apply {
            viewModel.saveTemporaryState(
                title = etTitle.text.toString(),
                content = etContent.text.toString(),
                date = selectedDate,
                feeling = selectedFeeling?.name ?: Feeling.NEUTRAL.name,
                images = viewModel.selectedImages.value
            )
        }
    }

    private fun restoreState() {
        viewModel.temporaryState.value?.let { state ->
            binding.apply {
                etTitle.setText(state.title)
                etContent.setText(state.content)
                selectedDate = state.date
                includeToolbar.tvDate.text = selectedDate.format(
                    DateTimeFormatter.ofPattern("EEE, MMM d")
                )
                selectedFeeling = Feeling.values().find { it.name == state.feeling }
                feelingAdapter.setSelectedFeeling(selectedFeeling)
            }
        }
    }

    private fun deleteEntry() {
        currentEntry?.let { entry ->
            viewModel.deleteEntry(entry)
            findNavController().navigate(
                EditEntryFragmentDirections.actionEditToHome()
            )
        }
    }

    private fun handleBack() {
        if (hasUnsavedChanges) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.wait))
                .setMessage(getString(R.string.discard_changes_message))
                .setPositiveButton(getString(R.string.discard)) { _, _ ->
                    findNavController().navigateUp()
                }
                .setNegativeButton(getString(R.string.cancel), null)
                .show()
        } else {
            findNavController().navigateUp()
        }
    }

    override fun onResume() {
        super.onResume()
        // Restore images from backstack if they exist
        findNavController().currentBackStackEntry?.savedStateHandle?.remove<ArrayList<String>>("temp_images")?.let { images ->
            viewModel.setSelectedImages(images)
        }
    }
} 