package com.example.myapplication.ui.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.base.BaseFragment
import com.example.myapplication.databinding.FragmentCreateEntryBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.datepicker.MaterialDatePicker
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import com.example.myapplication.ui.diary.DiaryViewModel
import androidx.core.view.isVisible
import com.example.myapplication.ui.diary.SelectedImagesAdapter
import com.example.myapplication.ui.dialog.ImagePreviewDialog
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope

class CreateEntryFragment : BaseFragment<FragmentCreateEntryBinding>() {

    private val viewModel: DiaryViewModel by viewModels()
    private val feelingAdapter = FeelingAdapter()
    private var selectedFeeling: Feeling? = null
    private var hasUnsavedChanges = false
    private var selectedDate = LocalDateTime.now()
    private val selectedImagesAdapter = SelectedImagesAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    handleBack()
                }
            }
        )
    }

    override fun inflateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentCreateEntryBinding {
        return FragmentCreateEntryBinding.inflate(inflater, container, false)
    }

    override fun setupUI() {
        binding.apply {
            // Setup toolbar
            includeToolbar.btnBack.setOnClickListener {
                handleBack()
            }

            // Set current date
            includeToolbar.tvDate.apply {
                text = selectedDate.format(DateTimeFormatter.ofPattern("EEE, MMM d"))
                setOnClickListener { showDatePicker() }
            }

            // Setup feelings recycler
            rvFeelings.apply {
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                adapter = feelingAdapter
            }

            feelingAdapter.onFeelingSelected = { feeling ->
                selectedFeeling = feeling
                hasUnsavedChanges = true
            }

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
                    saveEntry()
                }
            }

            // Setup selected images RecyclerView
            setupSelectedImages()

            // Setup image button
            includeToolbar.btnImage.setOnClickListener {
                saveCurrentState()
                findNavController().navigate(
                    CreateEntryFragmentDirections.actionCreateToPermission(
                        viewModel.selectedImages.value.toTypedArray()
                    )
                )
            }
        }

        // Observe selected images from navigation
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<ArrayList<String>>("selected_images")?.observe(
            viewLifecycleOwner
        ) { images ->
            if (images != null) {
                viewModel.setSelectedImages(images)
                hasUnsavedChanges = true
                // Clear the saved state handle after consuming it
                findNavController().currentBackStackEntry?.savedStateHandle?.remove<ArrayList<String>>("selected_images")
            }
        }
    }

    private fun setupSelectedImages() {
        binding.rvSelectedImages.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = selectedImagesAdapter
        }

        selectedImagesAdapter.apply {
            isEditMode = true  // Always true for create screen
            onImageClick = { uri ->
                ImagePreviewDialog.newInstance(uri)
                    .show(childFragmentManager, "preview")
            }
            
            onDeleteClick = { uri ->
                val currentImages = viewModel.selectedImages.value.toMutableList()
                currentImages.remove(uri)
                viewModel.setSelectedImages(currentImages)
                hasUnsavedChanges = true
            }
        }

        // Also observe the ViewModel's selected images
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.selectedImages.collect { images ->
                updateSelectedImages(images)
            }
        }
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

    private fun handleBack() {
        if (hasUnsavedChanges) {
            showDiscardDialog()
        } else {
            findNavController().navigateUp()
        }
    }

    private fun showDiscardDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setView(R.layout.dialog_discard_changes)
            .create()
            .apply {
                setOnShowListener {
                    findViewById<Button>(R.id.btnCancel)?.setOnClickListener {
                        dismiss()
                    }
                    findViewById<Button>(R.id.btnDiscard)?.setOnClickListener {
                        dismiss()
                        findNavController().navigateUp()
                    }
                }
                window?.setBackgroundDrawableResource(R.drawable.bg_dialog)
            }
            .show()
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

    private fun saveEntry() {
        binding.apply {
            viewModel.addEntry(
                title = etTitle.text.toString(),
                content = etContent.text.toString(),
                date = selectedDate,
                feeling = selectedFeeling?.name ?: Feeling.NEUTRAL.name,
                images = viewModel.selectedImages.value
            )
            findNavController().navigateUp()
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
        updateSelectedImages(viewModel.selectedImages.value)
    }

    private fun updateSelectedImages(images: List<String>) {
        binding.rvSelectedImages.isVisible = images.isNotEmpty()
        selectedImagesAdapter.submitList(images)
    }
} 